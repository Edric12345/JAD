package com.silvercare.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silvercare.models.Caregiver;
import com.silvercare.models.CaregiverBooking;
import com.silvercare.repositories.CaregiverBookingRepository;
import com.silvercare.repositories.CaregiverRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.AsyncContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/b2b/api/caregivers", "/b2b/api/bookings", "/b2b/api/checkin", "/b2b/api/checkout", "/b2b/stream"}, asyncSupported = true)
public class B2BServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(B2BServlet.class);
    private final ObjectMapper mapper = new ObjectMapper();

    private CaregiverRepository caregiverRepo;
    private CaregiverBookingRepository caregiverBookingRepo;

    // SSE clients (simple PrintWriter approach)
    private final List<PrintWriter> sseClients = new CopyOnWriteArrayList<>();

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            var ctx = org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            caregiverRepo = ctx.getBean(CaregiverRepository.class);
            caregiverBookingRepo = ctx.getBean(CaregiverBookingRepository.class);
        } catch (Exception e) {
            logger.warn("Failed to obtain Spring beans for B2B: {}", e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if (path.equals("/b2b/api/caregivers")) {
            resp.setContentType("application/json");
            List<Caregiver> list = (caregiverRepo != null) ? caregiverRepo.findAll() : Collections.<Caregiver>emptyList();
            // Convert to DTOs to avoid lazy-loading issues (do not serialize collections)
            List<Map<String,Object>> dto = new ArrayList<>();
            for (Caregiver c : list) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("name", c.getName());
                m.put("qualifications", c.getQualifications());
                // imagePath property used in model
                m.put("imagePath", c.getImagePath());
                m.put("availabilityStatus", c.getAvailabilityStatus());
                dto.add(m);
            }
            resp.getWriter().write(mapper.writeValueAsString(dto));
            return;
        }

        if (path.equals("/b2b/api/bookings")) {
            resp.setContentType("application/json");
            List<CaregiverBooking> bookings = (caregiverBookingRepo != null) ? caregiverBookingRepo.findAll() : Collections.<CaregiverBooking>emptyList();
            // Convert to JSON-friendly DTOs (avoid direct LocalDateTime serialization issues)
            List<Map<String,Object>> dto = new ArrayList<>();
            for (CaregiverBooking b : bookings) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", b.getId());
                m.put("customerName", b.getCustomerName());
                m.put("name", b.getName());
                m.put("qualifications", b.getQualifications());
                m.put("availabilityStatus", b.getAvailabilityStatus());
                m.put("checkedInAt", b.getCheckedInAt() != null ? b.getCheckedInAt().toString() : null);
                m.put("checkedOutAt", b.getCheckedOutAt() != null ? b.getCheckedOutAt().toString() : null);
                if (b.getCaregiver() != null) {
                    Map<String,Object> cg = new HashMap<>(); cg.put("id", b.getCaregiver().getId()); cg.put("name", b.getCaregiver().getName());
                    m.put("caregiver", cg);
                } else {
                    m.put("caregiver", null);
                }
                dto.add(m);
            }
            resp.getWriter().write(mapper.writeValueAsString(dto));
            return;
        }

        if (path.equals("/b2b/stream")) {
            // Accept SSE connections and keep them open using async context
            resp.setContentType("text/event-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Cache-Control", "no-cache");
            resp.setHeader("Connection", "keep-alive");

            // Start async so we can keep the response open
            final AsyncContext ac = req.startAsync();
            ac.setTimeout(0); // no timeout - rely on ping thread and client reconnect

            final PrintWriter out = ac.getResponse().getWriter();
            sseClients.add(out);
            // send initial comment to confirm connection
            out.write(": connected\n\n");
            out.flush();

            // Spawn a small ping thread to keep the connection alive and detect disconnects
            Thread ping = new Thread(() -> {
                try {
                    while (true) {
                        try {
                            Thread.sleep(15000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                        try {
                            out.write(": ping\n\n");
                            out.flush();
                            if (out.checkError()) break;
                        } catch (Exception e) {
                            break;
                        }
                    }
                } finally {
                    try { sseClients.remove(out); } catch (Exception ignored) {}
                    try { ac.complete(); } catch (Exception ignored) {}
                }
            }, "b2b-sse-ping");
            ping.setDaemon(true);
            ping.start();
            return;
        }

        resp.sendError(404);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if (path.equals("/b2b/api/checkin") || path.equals("/b2b/api/checkout")) {
            String type = path.endsWith("checkin") ? "CHECKIN" : "CHECKOUT";
            String idStr = req.getParameter("booking_id");
            if (idStr == null) {
                resp.sendError(400, "booking_id required");
                return;
            }
            try {
                int id = Integer.parseInt(idStr);
                Optional<CaregiverBooking> cbOpt = (caregiverBookingRepo != null) ? caregiverBookingRepo.findById(id) : Optional.empty();
                if (cbOpt.isEmpty()) { resp.sendError(404, "booking not found"); return; }
                CaregiverBooking cb = cbOpt.get();
                // Update snapshot status fields
                if (type.equals("CHECKIN")) {
                    cb.setAvailabilityStatus("ON_SITE");
                    cb.setCheckedInAt(LocalDateTime.now());
                    cb.setCheckedOutAt(null);
                } else {
                    cb.setAvailabilityStatus("COMPLETED");
                    cb.setCheckedOutAt(LocalDateTime.now());
                }
                // Save if repo available
                if (caregiverBookingRepo != null) caregiverBookingRepo.save(cb);

                // Broadcast SSE event to all connected clients
                Map<String,Object> evt = new HashMap<>();
                evt.put("type", type);
                evt.put("booking_id", cb.getId());
                evt.put("caregiver_name", cb.getName());
                evt.put("timestamp", LocalDateTime.now().toString());
                evt.put("status", cb.getAvailabilityStatus());
                evt.put("checkedInAt", cb.getCheckedInAt() != null ? cb.getCheckedInAt().toString() : null);
                evt.put("checkedOutAt", cb.getCheckedOutAt() != null ? cb.getCheckedOutAt().toString() : null);
                broadcastEvent("status-update", mapper.writeValueAsString(evt));

                resp.setContentType("application/json");
                resp.getWriter().write(mapper.writeValueAsString(evt));
                return;
            } catch (NumberFormatException nfe) {
                resp.sendError(400, "invalid booking_id");
                return;
            }
        }

        resp.sendError(404);
    }

    private void broadcastEvent(String event, String data) {
        String sse = "event: " + event + "\n" + "data: " + data + "\n\n";
        for (PrintWriter w : sseClients) {
            try {
                w.write(sse);
                w.flush();
            } catch (Exception e) {
                // remove dead clients
                sseClients.remove(w);
            }
        }
    }
}