package com.silvercare.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // If request targets customer or admin protected area, ensure session indicates login
        if (path.startsWith(request.getContextPath() + "/customer") || path.startsWith(request.getContextPath() + "/admin")) {
            if (path.startsWith(request.getContextPath() + "/customer")) {
                if (session == null || session.getAttribute("customer_id") == null) {
                    // Save intended URL for post-login redirect
                    if (request.getMethod().equalsIgnoreCase("GET")) {
                        request.getSession(true).setAttribute("after_login_redirect", request.getRequestURI());
                    }
                    response.sendRedirect(request.getContextPath() + "/customer/login");
                    return false;
                }
            }

            if (path.startsWith(request.getContextPath() + "/admin")) {
                if (session == null || session.getAttribute("admin_username") == null) {
                    response.sendRedirect(request.getContextPath() + "/admin/login");
                    return false;
                }
            }
        }
        return true;
    }
}
