package com.silvercare.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

@WebServlet("/errorHandler")
public class GlobalErrorServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(GlobalErrorServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        handleError(request, response);
    }

    private void handleError(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Retrieve error details from the request
        Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        String message = (throwable != null) ? throwable.getMessage() : "Unknown Error";

        logger.error("Unhandled exception for request {}: {}", requestUri, message, throwable);

        // 2. Logic to decide where to redirect (Replaces your URI checks)
        if (requestUri != null) {
            if (requestUri.contains("/customer/checkout")) {
                // Use session to pass "Flash" attributes since RedirectAttributes don't exist in Servlets
                try {
                    if (!response.isCommitted()) {
                        request.getSession().setAttribute("error", "Checkout error: " + message);
                        response.sendRedirect(request.getContextPath() + "/customer/checkout");
                        return;
                    } else {
                        logger.warn("Response already committed while handling error for {}", requestUri);
                        return;
                    }
                } catch (IllegalStateException ise) {
                    logger.warn("Cannot redirect after response committed: {}", ise.getMessage());
                    return;
                }
            } 
            if (requestUri.contains("/customer")) {
                try {
                    if (!response.isCommitted()) {
                        request.getSession().setAttribute("error", "An error occurred: " + message);
                        response.sendRedirect(request.getContextPath() + "/customer/profile");
                        return;
                    } else {
                        logger.warn("Response already committed while handling error for {}", requestUri);
                        return;
                    }
                } catch (IllegalStateException ise) {
                    logger.warn("Cannot redirect after response committed: {}", ise.getMessage());
                    return;
                }
            }
        }

        // 3. Fallback to error page - only forward if response not committed
        request.setAttribute("errorMessage", message);
        if (!response.isCommitted()) {
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        } else {
            logger.warn("Response already committed; cannot forward to error JSP for request {}", requestUri);
        }
    }
}