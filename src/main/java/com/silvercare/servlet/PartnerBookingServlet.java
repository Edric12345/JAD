package com.silvercare.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silvercare.restController.PartnerBookingController;

@WebServlet(urlPatterns = {"/partner/bookings"})
public class PartnerBookingServlet extends HttpServlet {

    private PartnerBookingController partnerController;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        this.partnerController = ctx.getBean(PartnerBookingController.class);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read JSON body
        String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        Map<String, Object> payload = mapper.readValue(body, Map.class);

        // Delegate to the existing PartnerBookingController
        var result = partnerController.createBookingsForPartner(payload);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(mapper.writeValueAsString(result.getBody()));
        resp.setStatus(result.getStatusCodeValue());
    }
}