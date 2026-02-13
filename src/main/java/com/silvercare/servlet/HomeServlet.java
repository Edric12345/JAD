package com.silvercare.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// This maps to the root "/" URL, just like your @GetMapping("/")
@WebServlet(urlPatterns = {"", "/"})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // In a Servlet, "return index" becomes a Forward
        // This keeps the URL as "/" but shows the content of index.jsp
        request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);
    
    }
}