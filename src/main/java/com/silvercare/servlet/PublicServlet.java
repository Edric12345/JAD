package com.silvercare.servlet;

import com.silvercare.models.CareService;
import com.silvercare.models.ServiceCategory;
import com.silvercare.repositories.CategoryRepository;
import com.silvercare.repositories.ServiceRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.List;

// We map multiple patterns to one Servlet
@WebServlet(urlPatterns = {"/services/*", "/welcome"})
public class PublicServlet extends HttpServlet {

    private CategoryRepository categoryRepo;
    private ServiceRepository serviceRepo;

    @Override
    public void init() throws ServletException {
        ApplicationContext context = WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
        this.categoryRepo = context.getBean(CategoryRepository.class);
        this.serviceRepo = context.getBean(ServiceRepository.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String fullPath = (pathInfo == null) ? path : path + pathInfo; // e.g. /services or /services/5

        // Normalize trailing slash
        if (fullPath.endsWith("/") && fullPath.length() > 1) fullPath = fullPath.substring(0, fullPath.length()-1);

        if (fullPath.equals("/welcome")) {
            // Equivalent to return "index"
            request.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(request, response);
            return;
        } 
        else if (fullPath.equals("/services") || fullPath.equals("/services/categories")) {
            // Show list of service categories
            request.setAttribute("categories", categoryRepo.findAll());
            request.getRequestDispatcher("/WEB-INF/jsp/public/service_category.jsp").forward(request, response);
            return;
        }
        else if (path.startsWith("/services")) {
            // service details by category id (e.g., /services/5)
            handleServiceDetails(request, response);
            return;
        }

        // Fallback
        response.sendRedirect(request.getContextPath() + "/");
    }

    private void handleServiceDetails(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // request.getPathInfo() gets the part after /services (e.g., "/5")
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            // Parse the categoryId from the URL (removes the leading slash)
            int categoryId = Integer.parseInt(pathInfo.substring(1));

            ServiceCategory category = categoryRepo.findById(categoryId).orElse(null);
            
            if (category == null) {
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }

            List<CareService> services = serviceRepo.findByCategoryId(categoryId);

            // Equivalent to model.addAttribute
            request.setAttribute("category", category);
            request.setAttribute("services", services);

            // Forward to the JSP
            request.getRequestDispatcher("/WEB-INF/jsp/public/service_details.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            // If the ID isn't a number, go back home
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}