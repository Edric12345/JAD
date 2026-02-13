package com.silvercare.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/b2b", "/b2b/"})
public class B2BPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Forward to the JSP page located at /b2b/index.jsp
        req.getRequestDispatcher("/b2b/index.jsp").forward(req, resp);
    }
}
