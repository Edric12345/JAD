package com.silvercare.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.silvercare.repositories.ServiceRepository;
import com.silvercare.repositories.CategoryRepository;
import com.silvercare.repositories.AdminRepository;
import com.silvercare.repositories.CustomerRepository;
import com.silvercare.repositories.FeedbackRepository;
import com.silvercare.repositories.BookingRepository;
import com.silvercare.models.AdminUser;
import com.silvercare.models.Booking;
import com.silvercare.models.BookingDetails;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import java.time.format.DateTimeFormatter;

// This replaces @RequestMapping("/admin") and handles multiple URLs
@WebServlet(urlPatterns = {"/admin", "/admin/login", "/admin/dashboard", "/admin/manage-services", "/admin/manage-clients", "/admin/feedback", "/admin/manage-billings", "/admin/logout", "/admin/add-service", "/admin/edit-service/*", "/admin/update-service/*", "/admin/delete-service/*", "/admin/add-client", "/admin/edit-client/*", "/admin/update-client/*", "/admin/delete-client/*"})
@MultipartConfig // Required for handling file uploads (imageFile)
public class AdminServlet extends HttpServlet {

    // Note: In a pure Servlet (without Spring), you'd need a way to get your Repositories.
    // Assuming you are still using Spring for the Repositories:
    private ServiceRepository serviceRepo; 
    private AdminRepository adminRepo;
    private CustomerRepository customerRepo;
    private FeedbackRepository feedbackRepo;
    private BookingRepository bookingRepo;
    private CategoryRepository categoryRepo;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            this.serviceRepo = ctx.getBean(ServiceRepository.class);
            this.adminRepo = ctx.getBean(AdminRepository.class);
            try { this.categoryRepo = ctx.getBean(CategoryRepository.class); } catch (Exception ignored) {}
            // optional repositories used by admin pages
            try { this.customerRepo = ctx.getBean(CustomerRepository.class); } catch (Exception ignored) {}
            try { this.feedbackRepo = ctx.getBean(FeedbackRepository.class); } catch (Exception ignored) {}
            try { this.bookingRepo = ctx.getBean(BookingRepository.class); } catch (Exception ignored) {}
        } catch (Exception e) {
            // ignore - fall back to in-memory behavior
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Ensure Spring beans are available (some environments initialize servlets before Spring context)
        ensureBeansInitialized();
        
        String path = request.getServletPath();
        HttpSession session = request.getSession();

        // If requested /admin root, redirect to dashboard or login
        if (path.equals("/admin")) {
            if (session.getAttribute("admin_username") == null) {
                response.sendRedirect(request.getContextPath() + "/admin/login");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            }
            return;
        }

        // 1. LOGIN PAGE
        if (path.equals("/admin/login")) {
            request.getRequestDispatcher("/WEB-INF/jsp/admin/admin_login.jsp").forward(request, response);
            return;
        }

        // 2. PROTECTED ROUTES (Requires Session)
        if (session.getAttribute("admin_username") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        if (path.equals("/admin/dashboard")) {
            request.setAttribute("adminUser", session.getAttribute("admin_username"));
            request.getRequestDispatcher("/WEB-INF/jsp/admin/admin_dashboard.jsp").forward(request, response);
        
        } else if (path.equals("/admin/manage-services")) {
            String filter = request.getParameter("filter");
            String thresholdStr = request.getParameter("threshold");
            Integer threshold = null;
            try { if (thresholdStr != null && !thresholdStr.isBlank()) threshold = Integer.parseInt(thresholdStr); } catch (Exception ignored) {}

            List<com.silvercare.models.CareService> servicesList = new ArrayList<>();
            Map<Integer, Double> ratingsMap = new HashMap<>();
            Map<Integer, Long> demandMap = new HashMap<>();

            if (filter != null) {
                switch (filter) {
                    case "topRated":
                        if (feedbackRepo != null) {
                            // Get averages and build ratings map
                            List<Object[]> topRated = feedbackRepo.findTopRatedServices();
                            request.setAttribute("topRated", topRated);
                            // Populate ratingsMap from feedback
                            for (Object[] row : topRated) {
                                if (row == null || row.length < 2) continue;
                                com.silvercare.models.CareService s = (com.silvercare.models.CareService) row[0];
                                Double avg = row[1] == null ? null : ((Number) row[1]).doubleValue();
                                if (s != null && avg != null) ratingsMap.put(s.getId(), avg);
                            }
                            // Show all services but sorted by rating desc (rated services first)
                            servicesList = (serviceRepo != null) ? serviceRepo.findAll() : Collections.emptyList();
                            servicesList.sort((a,b) -> {
                                Double ra = ratingsMap.get(a.getId());
                                Double rb = ratingsMap.get(b.getId());
                                if (ra == null && rb == null) return a.getService_name().compareToIgnoreCase(b.getService_name());
                                if (ra == null) return 1; // put unrated at the end
                                if (rb == null) return -1;
                                return Double.compare(rb, ra);
                            });
                            request.setAttribute("metricLabel", "Avg Rating");
                        }
                        break;
                    case "lowRated":
                        if (feedbackRepo != null) {
                            // Get averages and build ratings map
                            List<Object[]> lowRated = feedbackRepo.findLowestRatedServices();
                            request.setAttribute("lowRated", lowRated);
                            for (Object[] row : lowRated) {
                                if (row == null || row.length < 2) continue;
                                com.silvercare.models.CareService s = (com.silvercare.models.CareService) row[0];
                                Double avg = row[1] == null ? null : ((Number) row[1]).doubleValue();
                                if (s != null && avg != null) ratingsMap.put(s.getId(), avg);
                            }
                            // Show all services but sorted by rating asc (lowest rated first)
                            servicesList = (serviceRepo != null) ? serviceRepo.findAll() : Collections.emptyList();
                            servicesList.sort((a,b) -> {
                                Double ra = ratingsMap.get(a.getId());
                                Double rb = ratingsMap.get(b.getId());
                                if (ra == null && rb == null) return a.getService_name().compareToIgnoreCase(b.getService_name());
                                if (ra == null) return 1; // consider unrated as higher than low-rated
                                if (rb == null) return -1;
                                return Double.compare(ra, rb);
                            });
                            request.setAttribute("metricLabel", "Avg Rating");
                        }
                        break;
                    case "highDemand":
                        if (bookingRepo != null) {
                            List<Object[]> highDemand = bookingRepo.findHighDemandServices();
                            for (Object[] row : highDemand) {
                                if (row == null || row.length < 2) continue;
                                com.silvercare.models.CareService s = (com.silvercare.models.CareService) row[0];
                                Long cnt = row[1] == null ? 0L : ((Number) row[1]).longValue();
                                servicesList.add(s);
                                demandMap.put(s.getId(), cnt);
                            }
                            request.setAttribute("metricLabel", "Bookings");
                        }
                        break;
                    case "lowAvailability":
                        int thr = (threshold == null) ? 3 : threshold.intValue();
                        if (serviceRepo != null) servicesList = serviceRepo.findLowAvailability(thr);
                        request.setAttribute("metricLabel", "Slots");
                        break;
                    default:
                        servicesList = (serviceRepo != null) ? serviceRepo.findAll() : Collections.emptyList();
                        request.setAttribute("metricLabel", "");
                }
            } else {
                servicesList = (serviceRepo != null) ? serviceRepo.findAll() : Collections.emptyList();
                request.setAttribute("metricLabel", "");
            }

            // Always prepare top/low rated lists for the page header (if feedbackRepo available)
            if (feedbackRepo != null) {
                try {
                    List<Object[]> allTop = feedbackRepo.findTopRatedServices();
                    List<Object[]> allLow = feedbackRepo.findLowestRatedServices();
                    request.setAttribute("topRated", allTop);
                    request.setAttribute("lowRated", allLow);
                    // Populate ratingsMap for all services so the table can reference averages even without filter
                    for (Object[] row : allTop) {
                        if (row == null || row.length < 2) continue;
                        com.silvercare.models.CareService s = (com.silvercare.models.CareService) row[0];
                        Double avg = row[1] == null ? null : ((Number) row[1]).doubleValue();
                        if (s != null && avg != null) ratingsMap.put(s.getId(), avg);
                    }
                    for (Object[] row : allLow) {
                        if (row == null || row.length < 2) continue;
                        com.silvercare.models.CareService s = (com.silvercare.models.CareService) row[0];
                        Double avg = row[1] == null ? null : ((Number) row[1]).doubleValue();
                        if (s != null && avg != null) ratingsMap.putIfAbsent(s.getId(), avg);
                    }
                } catch (Exception ignored) {}
            }

            request.setAttribute("services", servicesList);
            request.setAttribute("ratingsMap", ratingsMap);
            request.setAttribute("demandMap", demandMap);
            request.setAttribute("selectedFilter", filter);
            request.setAttribute("threshold", (threshold == null) ? 3 : threshold);

            request.getRequestDispatcher("/WEB-INF/jsp/admin/manage_services.jsp").forward(request, response);
        
        } else if (path.equals("/admin/add-service")) {
            // Show add form
            request.setAttribute("categories", (categoryRepo != null) ? categoryRepo.findAll() : Collections.emptyList());
            request.getRequestDispatcher("/WEB-INF/jsp/admin/add_service.jsp").forward(request, response);
        
        } else if (path.startsWith("/admin/edit-service")) {
            // Parse ID from URI and show edit form
            String full = request.getRequestURI().substring(request.getContextPath().length());
            String[] parts = full.split("/");
            Integer id = null;
            try { id = Integer.parseInt(parts[parts.length-1]); } catch (Exception ignored) {}
            if (id == null) { response.sendRedirect(request.getContextPath() + "/admin/manage-services"); return; }
            com.silvercare.models.CareService svc = (serviceRepo != null) ? serviceRepo.findById(id).orElse(null) : null;
            if (svc == null) { response.sendRedirect(request.getContextPath() + "/admin/manage-services"); return; }
            request.setAttribute("service", svc);
            request.setAttribute("categories", (categoryRepo != null) ? categoryRepo.findAll() : Collections.emptyList());
            request.getRequestDispatcher("/WEB-INF/jsp/admin/edit_service.jsp").forward(request, response);
        
        } else if (path.startsWith("/admin/delete-service")) {
            // Allow delete via GET link too
            String full = request.getRequestURI().substring(request.getContextPath().length());
            String[] parts = full.split("/");
            Integer id = null; try { id = Integer.parseInt(parts[parts.length-1]); } catch (Exception ignored) {}
            if (id != null && serviceRepo != null) {
                try {
                    // Prevent deleting a service that is referenced by booking_details
                    long refs = (bookingRepo != null) ? bookingRepo.countDetailsByServiceId(id) : 0L;
                    if (refs > 0) {
                        request.getSession().setAttribute("error", "Cannot delete service: it is referenced by existing bookings (" + refs + " references). Consider disabling the service instead.");
                    } else {
                        serviceRepo.deleteById(id);
                        request.getSession().setAttribute("msg", "Service deleted");
                    }
                } catch (Exception e) { request.getSession().setAttribute("error", "Failed to delete service"); }
            }
            response.sendRedirect(request.getContextPath() + "/admin/manage-services");
        } else if (path.equals("/admin/manage-clients")) {
            // 1. Capture the search parameters from the request
            String areaSearch = request.getParameter("area");
            String careNeedsSearch = request.getParameter("careNeeds");

            // 2. Get the full list of clients initially
            List<com.silvercare.models.Customer> clientsList = (customerRepo != null) ? customerRepo.findAll() : new ArrayList<>();

            // 3. Filter the list if either search box has text
            if ((areaSearch != null && !areaSearch.isBlank()) || (careNeedsSearch != null && !careNeedsSearch.isBlank())) {
                clientsList = clientsList.stream().filter(c -> {
                    boolean matchesArea = true;
                    boolean matchesNeeds = true;

                    // Filter by Area (Address)
                    if (areaSearch != null && !areaSearch.isBlank()) {
                        String query = areaSearch.toLowerCase().trim();
                        matchesArea = c.getAddress() != null && c.getAddress().toLowerCase().contains(query);
                    }

                    // Filter by Care Needs
                    if (careNeedsSearch != null && !careNeedsSearch.isBlank()) {
                        String query = careNeedsSearch.toLowerCase().trim();
                        // Note: Ensure your Customer model has a getCare_needs() or similar getter
                        matchesNeeds = c.getCarePreferences() != null && c.getCarePreferences().toLowerCase().contains(query);
                    }

                    return matchesArea && matchesNeeds;
                }).collect(Collectors.toList());
            }

            // 4. Set attributes for the JSP (including search terms so they stay in the boxes)
            request.setAttribute("clients", clientsList);
            request.setAttribute("areaQuery", areaSearch);
            request.setAttribute("needsQuery", careNeedsSearch);
            
            request.getRequestDispatcher("/WEB-INF/jsp/admin/manage_clients.jsp").forward(request, response);
        
        } else if (path.equals("/admin/add-client")) {
            request.getRequestDispatcher("/WEB-INF/jsp/admin/add_client.jsp").forward(request, response);
        
        } else if (path.startsWith("/admin/edit-client")) {
            String full = request.getRequestURI().substring(request.getContextPath().length());
            String[] parts = full.split("/");
            Integer id = null; try { id = Integer.parseInt(parts[parts.length-1]); } catch (Exception ignored) {}
            if (id == null) { response.sendRedirect(request.getContextPath() + "/admin/manage-clients"); return; }
            com.silvercare.models.Customer c = (customerRepo != null) ? customerRepo.findById(id).orElse(null) : null;
            if (c == null) { response.sendRedirect(request.getContextPath() + "/admin/manage-clients"); return; }
            request.setAttribute("client", c);
            request.getRequestDispatcher("/WEB-INF/jsp/admin/edit_client.jsp").forward(request, response);
        
        } else if (path.equals("/admin/feedback")) {
            request.setAttribute("feedbacks", (feedbackRepo != null) ? feedbackRepo.findAll() : Collections.emptyList());
            request.getRequestDispatcher("/WEB-INF/jsp/admin/manage_feedback.jsp").forward(request, response);
        
        } else if (path.equals("/admin/manage-billings")) {
            // Build bookings list (with optional filtering) and helper aggregates for the billing view
            List<Booking> bookings = (bookingRepo != null) ? bookingRepo.findAll() : new ArrayList<>();

            // Read optional filters from request
            String from = request.getParameter("from");
            String to = request.getParameter("to");
            String month = request.getParameter("month");
            String serviceIdStr = request.getParameter("serviceId");
            Integer parsedServiceId = null;
            try { if (serviceIdStr != null && !serviceIdStr.isBlank()) parsedServiceId = Integer.parseInt(serviceIdStr); } catch (Exception ignored) {}
            final Integer serviceId = parsedServiceId;

            final java.time.LocalDate fromDate = (from != null && !from.isBlank()) ? java.time.LocalDate.parse(from) : null;
            final java.time.LocalDate toDate = (to != null && !to.isBlank()) ? java.time.LocalDate.parse(to) : null;

            // Filter bookings by date range or serviceId if provided
            List<Booking> filtered = bookings.stream().filter(b -> {
                boolean ok = true;
                if (fromDate != null || toDate != null) {
                    if (b.getBooking_date() == null) return false;
                    java.time.LocalDate d = b.getBooking_date().toLocalDate();
                    if (fromDate != null && d.isBefore(fromDate)) ok = false;
                    if (toDate != null && d.isAfter(toDate)) ok = false;
                }
                if (serviceId != null) {
                    ok = b.getDetails() != null && b.getDetails().stream().anyMatch(dd -> dd.getService() != null && dd.getService().getId() == serviceId);
                }
                if (month != null && !month.isBlank()) {
                    try {
                        java.time.YearMonth ym = java.time.YearMonth.parse(month);
                        if (b.getBooking_date() == null) return false;
                        java.time.YearMonth by = java.time.YearMonth.from(b.getBooking_date());
                        ok = ok && by.equals(ym);
                    } catch (Exception ignored) {}
                }
                return ok;
            }).collect(Collectors.toList());

            // Sort newest first
            filtered.sort((a,b) -> {
                if (a.getBooking_date() == null && b.getBooking_date() == null) return 0;
                if (a.getBooking_date() == null) return 1;
                if (b.getBooking_date() == null) return -1;
                return b.getBooking_date().compareTo(a.getBooking_date());
            });

            // Provide the list under the name the JSP expects
            request.setAttribute("bookings", filtered);

            // Services list for the filter/select
            request.setAttribute("services", (serviceRepo != null) ? serviceRepo.findAll() : Collections.emptyList());

            // Top clients by spend (compute from filtered bookings)
            Map<Integer, Double> spendByClient = new HashMap<>();
            for (Booking b : filtered) {
                if (b.getCustomer() == null) continue;
                double sum = 0;
                if (b.getDetails() != null) {
                    for (BookingDetails dd : b.getDetails()) sum += (dd.getSubtotal());
                }
                spendByClient.merge(b.getCustomer().getId(), sum, Double::sum);
            }
            List<Object[]> topClients = spendByClient.entrySet().stream()
                    .sorted((e1,e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .map(e -> {
                        com.silvercare.models.Customer c = null;
                        if (customerRepo != null) c = customerRepo.findById(e.getKey()).orElse(null);
                        return new Object[] { c, e.getValue() };
                    }).collect(Collectors.toList());
            request.setAttribute("topClients", topClients);

            // Payments: build simple summaries from bookings (PAID first)
            List<Map<String,Object>> payments = new ArrayList<>();
            DateTimeFormatter paidFmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            for (Booking b : filtered) {
                Map<String,Object> p = new HashMap<>();
                p.put("customer", b.getCustomer());
                double total = 0;
                if (b.getDetails() != null) for (BookingDetails dd : b.getDetails()) total += dd.getSubtotal();
                p.put("totalAmount", total);
                String paidAt = null;
                try {
                    if (b.getBooking_date() != null) paidAt = b.getBooking_date().format(paidFmt);
                } catch (Exception ignored) {
                    paidAt = (b.getBooking_date() != null) ? b.getBooking_date().toString() : null;
                }
                p.put("paidAt", paidAt);
                payments.add(p);
            }
            request.setAttribute("payments", payments);

            // Clients who booked selected service (if any)
            List<com.silvercare.models.Customer> clientsByService = new ArrayList<>();
            if (serviceId != null) {
                Set<Integer> seen = new HashSet<>();
                for (Booking b : filtered) {
                    if (b.getDetails() == null) continue;
                    for (BookingDetails dd : b.getDetails()) {
                        if (dd.getService() != null && dd.getService().getId() == serviceId) {
                            if (b.getCustomer() != null && !seen.contains(b.getCustomer().getId())) {
                                clientsByService.add(b.getCustomer());
                                seen.add(b.getCustomer().getId());
                            }
                        }
                    }
                }
            }
            request.setAttribute("clientsByService", clientsByService);

            // Forward filter params back to the view
            request.setAttribute("serviceId", serviceId);
            request.setAttribute("from", from);
            request.setAttribute("to", to);
            request.setAttribute("month", month);

            request.getRequestDispatcher("/WEB-INF/jsp/admin/manage_billings.jsp").forward(request, response);
        
        } else if (path.equals("/admin/logout")) {
             session.invalidate();
             response.sendRedirect(request.getContextPath() + "/admin/login");
         }
     }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();

        if (path.equals("/admin/login")) {
            // Replaces @RequestParam
            String user = request.getParameter("username");
            String pass = request.getParameter("password");

            // Prefer DB-backed admin repository if available
            if (adminRepo != null) {
                try {
                    java.util.Optional<AdminUser> opt = adminRepo.findByUsernameAndPassword(user, pass);
                    if (opt.isPresent()) {
                        request.getSession().setAttribute("admin_username", opt.get().getUsername());
                        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                        return;
                    } else {
                        request.setAttribute("message", "Invalid credentials");
                        request.getRequestDispatcher("/WEB-INF/jsp/admin/admin_login.jsp").forward(request, response);
                        return;
                    }
                } catch (Exception e) {
                    // fall through to fallback
                }
            }

            // Fallback: simple hardcoded check (legacy)
            if ("admin".equals(user) && "password123".equals(pass)) {
                request.getSession().setAttribute("admin_username", user);
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                request.setAttribute("message", "Invalid credentials");
                request.getRequestDispatcher("/WEB-INF/jsp/admin/admin_login.jsp").forward(request, response);
            }
        } else if (path.equals("/admin/add-service")) {
            // Handle add service POST
            try {
                String name = request.getParameter("service_name");
                String desc = request.getParameter("description");
                String priceStr = request.getParameter("price");
                String availStr = request.getParameter("availability");
                String catStr = request.getParameter("category_id");
                double price = (priceStr != null && !priceStr.isBlank()) ? Double.parseDouble(priceStr) : 0.0;
                Integer avail = null; try { if (availStr != null && !availStr.isBlank()) avail = Integer.parseInt(availStr); } catch (Exception ignored) {}
                Integer cat = null; try { if (catStr != null && !catStr.isBlank()) cat = Integer.parseInt(catStr); } catch (Exception ignored) {}
                com.silvercare.models.CareService svc = new com.silvercare.models.CareService();
                svc.setService_name(name); svc.setDescription(desc); svc.setPrice(price);
                if (cat != null) svc.setCategory_id(cat);
                if (avail != null) svc.setAvailability(avail);
                // file upload (optional)
                try {
                    Part part = request.getPart("imageFile");
                    if (part != null && part.getSubmittedFileName() != null && !part.getSubmittedFileName().isBlank()) {
                        svc.setImage_path("images/" + part.getSubmittedFileName());
                    }
                } catch (Exception ignored) {}
                if (serviceRepo != null) serviceRepo.save(svc);
                request.getSession().setAttribute("msg", "Service added");
            } catch (Exception e) {
                request.getSession().setAttribute("error", "Failed to add service");
            }
            response.sendRedirect(request.getContextPath() + "/admin/manage-services");
        
        } else if (path.startsWith("/admin/update-service")) {
            // /admin/update-service/{id}
            String full = request.getRequestURI().substring(request.getContextPath().length());
            String[] parts = full.split("/");
            Integer id = null;
            try { id = Integer.parseInt(parts[parts.length-1]); } catch (Exception ignored) {}
            if (id == null) { response.sendRedirect(request.getContextPath() + "/admin/manage-services"); return; }
            try {
                com.silvercare.models.CareService svc = serviceRepo.findById(id).orElse(null);
                if (svc != null) {
                    String name = request.getParameter("service_name");
                    String desc = request.getParameter("description");
                    String priceStr = request.getParameter("price");
                    String availStr = request.getParameter("availability");
                    String catStr = request.getParameter("category_id");
                    if (name != null) svc.setService_name(name);
                    if (desc != null) svc.setDescription(desc);
                    try { if (priceStr != null && !priceStr.isBlank()) svc.setPrice(Double.parseDouble(priceStr)); } catch (Exception ignored) {}
                    try { if (availStr != null && !availStr.isBlank()) svc.setAvailability(Integer.parseInt(availStr)); } catch (Exception ignored) {}
                    try { if (catStr != null && !catStr.isBlank()) svc.setCategory_id(Integer.parseInt(catStr)); } catch (Exception ignored) {}
                    try { Part part = request.getPart("imageFile"); if (part != null && part.getSubmittedFileName() != null && !part.getSubmittedFileName().isBlank()) svc.setImage_path("images/" + part.getSubmittedFileName()); } catch (Exception ignored) {}
                    serviceRepo.save(svc);
                    request.getSession().setAttribute("msg", "Service updated");
                }
            } catch (Exception e) { request.getSession().setAttribute("error","Failed to update service"); }
            response.sendRedirect(request.getContextPath() + "/admin/manage-services");
        
        } else if (path.startsWith("/admin/delete-service")) {
            // /admin/delete-service/{id} POST variant
            String full = request.getRequestURI().substring(request.getContextPath().length());
            String[] parts = full.split("/");
            Integer id = null; try { id = Integer.parseInt(parts[parts.length-1]); } catch (Exception ignored) {}
            if (id != null && serviceRepo != null) {
                try {
                    long refs = (bookingRepo != null) ? bookingRepo.countDetailsByServiceId(id) : 0L;
                    if (refs > 0) {
                        request.getSession().setAttribute("error", "Cannot delete service: it is referenced by existing bookings (" + refs + " references). Consider disabling the service instead.");
                    } else {
                        serviceRepo.deleteById(id);
                        request.getSession().setAttribute("msg", "Service deleted");
                    }
                } catch (Exception e) { request.getSession().setAttribute("error", "Failed to delete service"); }
            }
            response.sendRedirect(request.getContextPath() + "/admin/manage-services");
        } else if (path.equals("/admin/add-client")) {
            // Handle add client POST
            try {
                String name = request.getParameter("client_name");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String address = request.getParameter("address");
                com.silvercare.models.Customer client = new com.silvercare.models.Customer();
                client.setName(name);
                client.setEmail(email);
                client.setPhone(phone);
                client.setAddress(address);
                if (customerRepo != null) customerRepo.save(client);
                request.getSession().setAttribute("msg", "Client added");
            } catch (Exception e) {
                request.getSession().setAttribute("error", "Failed to add client");
            }
            response.sendRedirect(request.getContextPath() + "/admin/manage-clients");
        } else if (path.startsWith("/admin/update-client")) {
            // /admin/update-client/{id}
            String full = request.getRequestURI().substring(request.getContextPath().length());
            String[] parts = full.split("/");
            Integer id = null;
            try { id = Integer.parseInt(parts[parts.length-1]); } catch (Exception ignored) {}
            if (id == null) { response.sendRedirect(request.getContextPath() + "/admin/manage-clients"); return; }
            try {
                com.silvercare.models.Customer client = (customerRepo != null) ? customerRepo.findById(id).orElse(null) : null;
                if (client != null) {
                    String name = request.getParameter("client_name");
                    String email = request.getParameter("email");
                    String phone = request.getParameter("phone");
                    String address = request.getParameter("address");
                    if (name != null) client.setName(name);
                    if (email != null) client.setEmail(email);
                    if (phone != null) client.setPhone(phone);
                    if (address != null) client.setAddress(address);
                    customerRepo.save(client);
                    request.getSession().setAttribute("msg", "Client updated");
                }
            } catch (Exception e) { request.getSession().setAttribute("error","Failed to update client"); }
            response.sendRedirect(request.getContextPath() + "/admin/manage-clients");
        } else if (path.startsWith("/admin/delete-client")) {
            // /admin/delete-client/{id} POST variant
            String full = request.getRequestURI().substring(request.getContextPath().length());
            String[] parts = full.split("/");
            Integer id = null; try { id = Integer.parseInt(parts[parts.length-1]); } catch (Exception ignored) {}
            if (id != null && customerRepo != null) {
                try { customerRepo.deleteById(id); request.getSession().setAttribute("msg", "Client deleted"); } catch (Exception e) { request.getSession().setAttribute("error", "Failed to delete client"); }
            }
            response.sendRedirect(request.getContextPath() + "/admin/manage-clients");
        }
    }

    // Ensure repositories are initialized lazily if the Spring context wasn't present during init()
    private void ensureBeansInitialized() {
        if (this.serviceRepo != null && this.feedbackRepo != null && this.bookingRepo != null && this.categoryRepo != null) return;
        try {
            ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            if (this.serviceRepo == null) this.serviceRepo = ctx.getBean(ServiceRepository.class);
            if (this.adminRepo == null) this.adminRepo = ctx.getBean(AdminRepository.class);
            try { if (this.categoryRepo == null) this.categoryRepo = ctx.getBean(CategoryRepository.class); } catch (Exception ignored) {}
            try { if (this.customerRepo == null) this.customerRepo = ctx.getBean(CustomerRepository.class); } catch (Exception ignored) {}
            try { if (this.feedbackRepo == null) this.feedbackRepo = ctx.getBean(FeedbackRepository.class); } catch (Exception ignored) {}
            try { if (this.bookingRepo == null) this.bookingRepo = ctx.getBean(BookingRepository.class); } catch (Exception ignored) {}
        } catch (Exception e) {
            // Ignore - we'll operate with null repos but avoid NPEs in handlers
        }
    }
}
