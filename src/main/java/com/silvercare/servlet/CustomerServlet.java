package com.silvercare.servlet;

import com.silvercare.models.*;
import com.silvercare.repositories.*;
import com.silvercare.services.BookingService;
import com.silvercare.services.BookingResult;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {
        "/customer/login", "/customer/register", "/customer/logout",
        "/customer/profile", "/customer/update-profile",
        "/customer/booking", "/customer/add-to-cart/*", "/customer/checkout",
        "/customer/confirm-booking", "/customer/checkout/pay",
        "/customer/my-bookings", "/customer/booking-history", "/customer/cancel-booking/*", "/customer/booking/pay/*",
        "/customer/feedback", "/customer/cart-debug",
        "/customer/book-service/*", "/customer/receipt/*"
    })

public class CustomerServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CustomerServlet.class);

    private CategoryRepository categoryRepo;
    private ServiceRepository serviceRepo;
    private BookingRepository bookingRepo;
    private CustomerRepository customerRepo;
    private CustomerCartRepository customerCartRepo;
    private FeedbackRepository feedbackRepo;
    private BookingService bookingService;
    private PaymentRepository paymentRepo; // <-- added
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {
        try {
            ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            this.categoryRepo = context.getBean(CategoryRepository.class);
            this.serviceRepo = context.getBean(ServiceRepository.class);
            this.bookingRepo = context.getBean(BookingRepository.class);
            this.customerRepo = context.getBean(CustomerRepository.class);
            this.customerCartRepo = context.getBean(CustomerCartRepository.class);
            this.feedbackRepo = context.getBean(FeedbackRepository.class);
            this.bookingService = context.getBean(BookingService.class);
            try { this.paymentRepo = context.getBean(PaymentRepository.class); } catch (Exception ignored) {}
        } catch (Exception e) {
            // Log and continue; we'll try lazy initialization later in request handling
            logger.warn("Spring ApplicationContext not available during servlet init: {}", e.getMessage());
        }
    }

    private void ensureBeansInitialized() {
        if (this.customerRepo != null && this.serviceRepo != null) return; // already initialized
        try {
            ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            if (this.categoryRepo == null) this.categoryRepo = context.getBean(CategoryRepository.class);
            if (this.serviceRepo == null) this.serviceRepo = context.getBean(ServiceRepository.class);
            if (this.bookingRepo == null) this.bookingRepo = context.getBean(BookingRepository.class);
            if (this.customerRepo == null) this.customerRepo = context.getBean(CustomerRepository.class);
            if (this.customerCartRepo == null) this.customerCartRepo = context.getBean(CustomerCartRepository.class);
            if (this.feedbackRepo == null) this.feedbackRepo = context.getBean(FeedbackRepository.class);
            if (this.bookingService == null) this.bookingService = context.getBean(BookingService.class);
            if (this.paymentRepo == null) {
                try { this.paymentRepo = context.getBean(PaymentRepository.class); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            logger.error("Failed to initialize Spring beans from servlet context: {}", e.getMessage());
        }
    }

        private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
    ensureBeansInitialized();
    if (customerRepo == null) {
        request.setAttribute("error", "Service temporarily unavailable. Please try again later.");
        try {
            request.getRequestDispatcher("/WEB-INF/jsp/public/register.jsp").forward(request, response);
        } catch (ServletException se) {
            logger.error("Failed to forward to register page: {}", se.getMessage(), se);
            response.sendRedirect(request.getContextPath() + "/errorHandler");
        }
        return;
    }

    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    String phone = request.getParameter("phone");
    String address = request.getParameter("address");

    if (name == null || email == null || password == null || name.isBlank() || email.isBlank() || password.isBlank()) {
        request.setAttribute("error", "Name, email, and password are required");
        request.setAttribute("savedName", name);
        request.setAttribute("savedEmail", email);
        try {
            request.getRequestDispatcher("/WEB-INF/jsp/public/register.jsp").forward(request, response);
        } catch (ServletException se) {
            logger.error("Failed to forward to register page: {}", se.getMessage(), se);
            response.sendRedirect(request.getContextPath() + "/customer/register");
        }
        return;
    }

    try {
        // Check if email already exists
        if (customerRepo.findByEmail(email).isPresent()) {
            request.setAttribute("error", "Email already registered");
            request.setAttribute("savedName", name);
            request.setAttribute("savedEmail", email);
            request.getRequestDispatcher("/WEB-INF/jsp/public/register.jsp").forward(request, response);
            return;
        }

        // Create new customer
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPassword(password); // optionally hash this if your repo expects plain vs hashed
        customer.setPhone(phone);
        customer.setAddress(address);
        customer.setCreated_at(LocalDateTime.now());

        customerRepo.save(customer);

        // Auto-login after registration
        HttpSession session = request.getSession();
        session.setAttribute("customer_id", customer.getId());
        session.setAttribute("customer_name", customer.getName());

        session.setAttribute("msg", "Registration successful! Welcome, " + customer.getName());
        response.sendRedirect(request.getContextPath() + "/customer/profile");
    } catch (Exception e) {
        logger.error("Error during registration", e);
        request.setAttribute("error", "Failed to register: " + e.getMessage());
        try {
            request.getRequestDispatcher("/WEB-INF/jsp/public/register.jsp").forward(request, response);
        } catch (ServletException se) {
            logger.error("Failed to forward to register page after exception: {}", se.getMessage(), se);
            response.sendRedirect(request.getContextPath() + "/customer/register");
        }
    }
}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ensureBeansInitialized();
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String path = (pathInfo == null) ? servletPath : servletPath + pathInfo; // e.g. /customer + /login => /customer/login

        // Normalize trailing slash
        if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length()-1);

        logger.debug("CustomerServlet doGet resolved path={}", path);

        // New: handle /customer/book-service/{id}
        if (path.startsWith("/customer/book-service/")) {
            handleShowBookService(request, response);
            return;
        }

        // New: handle /customer/receipt/{id}
        if (path.startsWith("/customer/receipt/")) {
            handleShowReceipt(request, response);
            return;
        }

        // New: handle /customer/cancel-booking/{id} via GET as users may click links
        if (path.startsWith("/customer/cancel-booking/")) {
            handleCancelBooking(request, response);
            return;
        }

        // New: handle /customer/booking-history
        if (path.equals("/customer/booking-history")) {
            handleShowBookingHistory(request, response);
            return;
        }

        switch (path) {
            case "/customer/login":
                request.getRequestDispatcher("/WEB-INF/jsp/public/login.jsp").forward(request, response);
                break;
            case "/customer/register":
                request.getRequestDispatcher("/WEB-INF/jsp/public/register.jsp").forward(request, response);
                break;
            case "/customer/logout":
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/customer/login");
                break;
            case "/customer/profile":
                handleShowProfile(request, response);
                break;
            case "/customer/checkout":
                handleShowCheckout(request, response);
                break;
            case "/customer/my-bookings":
                handleShowMyBookings(request, response);
                break;
            case "/customer/feedback":
                handleShowFeedback(request, response);
                break;
            case "/customer/booking":
                // Show booking page; redirect to services/categories for browsing
                response.sendRedirect(request.getContextPath() + "/services");
                break;
            case "/customer/services":
            case "/customer/services/categories":
                // Redirect old /customer/services paths to the public /services endpoints
                response.sendRedirect(request.getContextPath() + "/services");
                break;
            default:
                // handle deeper /customer/services/{id}
                if (path.startsWith("/customer/services/")) {
                    String rest = path.substring("/customer".length()); // "/services/..."
                    response.sendRedirect(request.getContextPath() + rest);
                    break;
                }
                response.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ensureBeansInitialized();
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String path = (pathInfo == null) ? servletPath : servletPath + pathInfo;
        if (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length()-1);
        
        logger.debug("CustomerServlet doPost resolved path={}", path);

        if (path.equals("/customer/login")) {
            handleLogin(request, response);
        }else if (path.equals("/customer/register")) {
            handleRegister(request, response);
        }else if (path.startsWith("/customer/add-to-cart")) {
            handleAddToCart(request, response);
        } else if (path.equals("/customer/update-profile")) {
            handleUpdateProfile(request, response);
        } else if (path.equals("/customer/checkout/pay")) {
            handleCheckoutPay(request, response);
        } else if (path.equals("/customer/confirm-booking")) {
            handleConfirmBooking(request, response);
        } else if (path.equals("/customer/feedback")) {
            handleSubmitFeedback(request, response);
        } else if (path.equals("/customer/feedback/edit")) {
            handleEditFeedback(request, response);
        } else if (path.equals("/customer/feedback/delete")) {
            handleDeleteFeedback(request, response);
        } else if (path.startsWith("/customer/cancel-booking")) {
            handleCancelBooking(request, response);
        } else if (path.startsWith("/customer/booking/pay")) {
            handlePaySingleBooking(request, response);
        } else if (path.startsWith("/customer/book-service")) {
            handleProcessBookService(request, response);
        }else {
            response.sendError(404);
        }
    }

    // --- LOGIC METHODS (Converted from Spring methods) ---

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (customerRepo == null) {
            // Try to init one more time
            ensureBeansInitialized();
            if (customerRepo == null) {
                // Can't proceed; show friendly error page via forward so login.jsp can read request attribute
                logger.error("CustomerRepository not available - cannot process login");
                request.setAttribute("error", "Service temporarily unavailable. Please try again later.");
                try {
                    request.getRequestDispatcher("/WEB-INF/jsp/public/login.jsp").forward(request, response);
                } catch (ServletException se) {
                    logger.error("Failed to forward to login page: {}", se.getMessage(), se);
                    response.sendRedirect(request.getContextPath() + "/errorHandler");
                }
                return;
            }
        }
            
        

        String email = request.getParameter("email");
        String pass = request.getParameter("password");
        HttpSession session = request.getSession();

        try {
            Optional<Customer> opt = customerRepo.findByEmailAndPassword(email, pass);
            if (opt.isPresent()) {
                Customer customer = opt.get();
                session.setAttribute("customer_id", customer.getId());
                session.setAttribute("customer_name", customer.getName());

                // Restore cart from persisted CustomerCart for the user
                restoreCartForSessionIfNeeded(session, customer.getId());

                // If we saved an after-login redirect, go there; otherwise go to profile
                String after = (String) session.getAttribute("after_login_redirect");
                if (after != null && !after.isBlank()) {
                    session.removeAttribute("after_login_redirect");
                    // Normalize: if saved 'after' already includes contextPath use it; if it's an absolute path start with '/', prepend contextPath when needed
                    try {
                        if (after.startsWith(request.getContextPath())) {
                            response.sendRedirect(after);
                        } else if (after.startsWith("/")) {
                            response.sendRedirect(request.getContextPath() + after);
                        } else if (after.startsWith("http://") || after.startsWith("https://")) {
                            response.sendRedirect(after);
                        } else {
                            response.sendRedirect(request.getContextPath() + "/" + after);
                        }
                    } catch (Exception ex) {
                        // Fallback
                        response.sendRedirect(request.getContextPath() + "/customer/profile");
                    }
                } else {
                    response.sendRedirect(request.getContextPath() + "/customer/profile");
                }
                return;
            } else {
                // Forward back to login with request-scoped error (so JSP shows it immediately)
                request.setAttribute("error", "Invalid email or password");
                request.setAttribute("savedEmail", email);
                try {
                    request.getRequestDispatcher("/WEB-INF/jsp/public/login.jsp").forward(request, response);
                } catch (ServletException se) {
                    logger.error("Failed to forward to login page after invalid credentials: {}", se.getMessage(), se);
                    response.sendRedirect(request.getContextPath() + "/customer/login");
                }
            }
        } catch (Exception e) {
            logger.error("Error during login for email {}: {}", email, e.getMessage(), e);
            request.setAttribute("error", "An internal error occurred. Please try again later.");
            try {
                request.getRequestDispatcher("/WEB-INF/jsp/public/login.jsp").forward(request, response);
            } catch (ServletException se) {
                logger.error("Failed to forward to login page after exception: {}", se.getMessage(), se);
                response.sendRedirect(request.getContextPath() + "/errorHandler");
            }
        }
    }

    private void handleShowCheckout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<Integer> cart = (List<Integer>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/booking");
            return;
        }

        double subtotal = 0;
        List<CareService> selectedServices = new ArrayList<>();
        for (Integer id : cart) {
            serviceRepo.findById(id).ifPresent(s -> {
                selectedServices.add(s);
            });
        }
        subtotal = selectedServices.stream().mapToDouble(CareService::getPrice).sum();

        double gstRate = 0.09; // 9% GST
        double gstAmount = subtotal * gstRate;
        double total = subtotal + gstAmount;

        request.setAttribute("selectedServices", selectedServices);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("gstAmount", gstAmount);
        request.setAttribute("gstRate", gstRate);
        request.setAttribute("total", total); // 2-decimals formatting is handled in JSP
        request.getRequestDispatcher("/WEB-INF/jsp/customer/checkout.jsp").forward(request, response);
    }

    // --- New: Confirm booking (creates pending bookings from session cart) ---
    private void handleConfirmBooking(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Integer cid = (Integer) session.getAttribute("customer_id");
        List<Integer> cart = (List<Integer>) session.getAttribute("cart");

        if (cid == null) {
            session.setAttribute("after_login_redirect", "/customer/checkout");
            response.sendRedirect(request.getContextPath() + "/customer/login");
            return;
        }
        if (cart == null || cart.isEmpty()) {
            session.setAttribute("cartEmpty", true);
            response.sendRedirect(request.getContextPath() + "/customer/booking");
            return;
        }

        try {
            Customer customer = customerRepo.findById(cid).orElseThrow(() -> new RuntimeException("Customer not found"));
            logger.info("Confirming booking for customer {} with cart items {}", cid, cart);

            com.silvercare.services.BookingResult result = bookingService.createBookingsForCustomerPending(cid, customer, cart);

            if (result == null || result.getCreatedBookingIds().isEmpty()) {
                logger.warn("No pending bookings created for customer {}", cid);
                session.setAttribute("error", "No bookings were created. Please try again or contact support.");
                response.sendRedirect(request.getContextPath() + "/customer/checkout");
                return;
            }

            // Remove succeeded service IDs from session cart and persist remaining
            List<Integer> succeeded = result.getSucceededServiceIds();
            List<Integer> remaining = new ArrayList<>(cart);
            remaining.removeAll(succeeded);
            if (remaining.isEmpty()) {
                session.removeAttribute("cart");
                try { customerCartRepo.deleteByCustomer_Id(cid); } catch (Exception ignored) {}
            } else {
                session.setAttribute("cart", remaining);
                String data = String.join(",", remaining.stream().map(Object::toString).toArray(String[]::new));
                com.silvercare.models.CustomerCart cc = customerCartRepo.findByCustomer_Id(cid).orElseGet(() -> {
                    com.silvercare.models.CustomerCart n = new com.silvercare.models.CustomerCart();
                    n.setCustomer(customerRepo.findById(cid).orElseThrow());
                    return n;
                });
                cc.setCart_data(data);
                customerCartRepo.save(cc);
            }

            session.setAttribute("msg", "Booking confirmed. Booking IDs: " + result.getCreatedBookingIds());
            response.sendRedirect(request.getContextPath() + "/customer/booking-history");
         } catch (Exception ex) {
             logger.error("Error confirming booking for customer {}: {}", cid, ex.getMessage(), ex);
             session.setAttribute("error", "Failed to confirm booking: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/customer/checkout");
         }
     }
        
    
         // Add these cases to your doPost or doGet switch/if-else blocks as appropriate

        private void handleCancelBooking(HttpServletRequest request, HttpServletResponse response) throws IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }

            // Extract ID from /customer/cancel-booking/{id}
            String pathInfo = request.getPathInfo();
            if (pathInfo != null && pathInfo.length() > 1) {
                try {
                    int bookingId = Integer.parseInt(pathInfo.substring(1));
                    int updatedRows = bookingRepo.cancelBooking(bookingId, cid);
                    
                    if (updatedRows > 0) {
                        request.getSession().setAttribute("msg", "Booking #" + bookingId + " cancelled.");
                    } else {
                        request.getSession().setAttribute("error", "Failed to cancel booking.");
                    }
                } catch (NumberFormatException nfe) {
                    logger.warn("Invalid booking id for cancel: {}", pathInfo);
                }
            }
            response.sendRedirect(request.getContextPath() + "/customer/booking-history");
        }

        private void handlePaySingleBooking(HttpServletRequest request, HttpServletResponse response) throws IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            String pathInfo = request.getPathInfo(); // /customer/booking/pay/{id}
            
            if (pathInfo == null || cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/booking-history");
                return;
            }

            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Booking booking = bookingRepo.findById(id).orElse(null);

                if (booking != null && booking.getCustomer().getId()==(cid) && 
                    booking.getStatus() == BookingStatus.PENDING) {

                    double total = booking.getDetails().stream().mapToDouble(BookingDetails::getSubtotal).sum();
                    
                    // Consistency with your existing Service logic
                    java.math.BigDecimal excl = java.math.BigDecimal.valueOf(total).setScale(2, java.math.RoundingMode.HALF_UP);
                    java.math.BigDecimal gst = excl.multiply(java.math.BigDecimal.valueOf(0.09)).setScale(2, java.math.RoundingMode.HALF_UP);
                    java.math.BigDecimal tot = excl.add(gst).setScale(2, java.math.RoundingMode.HALF_UP);

                    boolean ok = bookingService.createPaymentForBooking(booking.getId(), cid, excl, gst, tot, "online", total);
                    
                    if (ok) {
                        booking.setStatus(BookingStatus.PAID);
                        bookingRepo.save(booking);
                        request.getSession().setAttribute("msg", "Payment successful!");
                    }
                }
            } catch (Exception e) {
                logger.error("Payment error", e);
                request.getSession().setAttribute("error", "Payment failed: " + e.getMessage());

            }
            response.sendRedirect(request.getContextPath() + "/customer/booking-history");
        }

        // This replaces @ResponseBody for the Debug/API endpoints
        private void handleCartDebug(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("application/json");
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            List<Integer> sessionCart = (List<Integer>) request.getSession().getAttribute("cart");
            
            Map<String, Object> debugData = new HashMap<>();
            debugData.put("customer_id", cid);
            debugData.put("sessionCart", sessionCart != null ? sessionCart : List.of());
            
            if (cid != null && customerCartRepo != null) {
                customerCartRepo.findByCustomer_Id(cid).ifPresent(cc -> 
                    debugData.put("persistedCart", cc.getCart_data()));
            }

            response.getWriter().write(objectMapper.writeValueAsString(debugData));
        }

        // --- NEW: Additional handlers for missing customer routes ---

        private void handleShowBookingHistory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            List<Booking> bookings = bookingRepo.findByCustomer_Id(cid);
            if (bookings == null) bookings = new ArrayList<>();
            bookings.sort((a,b) -> {
                if (a.getBooking_date() == null && b.getBooking_date() == null) return 0;
                if (a.getBooking_date() == null) return 1;
                if (b.getBooking_date() == null) return -1;
                return b.getBooking_date().compareTo(a.getBooking_date());
            });
            request.setAttribute("bookings", bookings);
            request.getRequestDispatcher("/WEB-INF/jsp/customer/booking_history.jsp").forward(request, response);
        }

        private void handleShowBookService(HttpServletRequest request, HttpServletResponse response, boolean checkLogin) throws ServletException, IOException {
            // Extract last non-empty path segment from the request URI
            String uri = request.getRequestURI().substring(request.getContextPath().length()); // e.g. /customer/book-service/5
            String[] parts = uri.split("/");
            String last = null;
            for (int i = parts.length - 1; i >= 0; i--) {
                if (parts[i] != null && !parts[i].isBlank()) { last = parts[i]; break; }
            }
            if (last == null) { response.sendError(404); return; }
            int id;
            try { id = Integer.parseInt(last); } catch (NumberFormatException nfe) {
                response.sendRedirect(request.getContextPath() + "/services");
                return;
            }

            HttpSession session = request.getSession();
            if (checkLogin && session.getAttribute("customer_id") == null) {
                // save intended URL and redirect to login
                session.setAttribute("after_login_redirect", request.getRequestURI());
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }

            CareService service = (serviceRepo != null) ? serviceRepo.findById(id).orElse(null) : null;
            if (service == null) {
                response.sendRedirect(request.getContextPath() + "/services");
                return;
            }

            request.setAttribute("service", service);
            request.getRequestDispatcher("/WEB-INF/jsp/customer/booking.jsp").forward(request, response);
        }

        // original method replaced with new signature wrapper
        private void handleShowBookService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            handleShowBookService(request, response, true);
        }

        private void handleProcessBookService(HttpServletRequest request, HttpServletResponse response) throws IOException {
            // Robustly extract the id from the request URI (handles cases where pathInfo may vary)
            String uri = request.getRequestURI().substring(request.getContextPath().length());
            String[] parts = uri.split("/");
            String last = null;
            for (int i = parts.length - 1; i >= 0; i--) {
                if (parts[i] != null && !parts[i].isBlank()) { last = parts[i]; break; }
            }
            if (last == null) { response.sendError(404); return; }

            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                request.getSession().setAttribute("after_login_redirect", request.getRequestURI());
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            try {
                int id = Integer.parseInt(last);

                Customer customer = customerRepo.findById(cid).orElseThrow();
                CareService service = serviceRepo.findById(id).orElseThrow();

                // Read optional preferred date and validate it's not in the past
                String dateParam = request.getParameter("date");
                java.time.LocalDate preferred = null;
                if (dateParam != null && !dateParam.isBlank()) {
                    try {
                        preferred = java.time.LocalDate.parse(dateParam);
                        if (preferred.isBefore(java.time.LocalDate.now())) {
                            request.getSession().setAttribute("error", "Preferred date cannot be in the past.");
                            response.sendRedirect(request.getContextPath() + "/customer/book-service/" + id);
                            return;
                        }
                    } catch (Exception pe) {
                        request.getSession().setAttribute("error", "Invalid date format.");
                        response.sendRedirect(request.getContextPath() + "/customer/book-service/" + id);
                        return;
                    }
                }

                Booking booking = new Booking();
                booking.setCustomer(customer);
                booking.setStatus(BookingStatus.PENDING);
                booking.setBooking_date(LocalDateTime.now());

                BookingDetails detail = new BookingDetails();
                detail.setService(service);
                detail.setBooking(booking);
                detail.setPriceAtBooking(service.getPrice());
                detail.setSubtotal(service.getPrice());

                booking.setDetails(List.of(detail));

                if (preferred != null) {
                    booking.setNotes("preferred_date:" + preferred.toString());
                }

                bookingRepo.save(booking);

                request.getSession().setAttribute("msg", "Booking confirmed successfully!");
            } catch (Exception e) {
                logger.error("Failed to process single service booking", e);
                request.getSession().setAttribute("error", "Unable to confirm booking: " + e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/customer/booking-history");
        }

        // Update login path to restore cart from persisted CustomerCart for the user
        private void restoreCartForSessionIfNeeded(HttpSession session, Integer cid) {
            if (cid == null || customerCartRepo == null) return;
            List<Integer> cart = (List<Integer>) session.getAttribute("cart");
            if (cart != null && !cart.isEmpty()) return; // already present
            customerCartRepo.findByCustomer_Id(cid).ifPresent(cc -> {
                String data = cc.getCart_data();
                if (data != null && !data.isBlank()) {
                    List<Integer> restored = new ArrayList<>();
                    for (String s : data.split(",")) {
                        try { restored.add(Integer.parseInt(s.trim())); } catch (Exception ignored) {}
                    }
                    if (!restored.isEmpty()) session.setAttribute("cart", restored);
                }
            });
        }
        
        // Replace handleShowMyBookings to accept filters and return bookings in reverse chron order
        private void handleShowMyBookings(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }

            // Optional filters: status, from (yyyy-MM-dd), to (yyyy-MM-dd)
            String statusFilter = request.getParameter("status");
            String fromStr = request.getParameter("from");
            String toStr = request.getParameter("to");

            List<Booking> bookings = bookingRepo.findByCustomer_Id(cid);

            // Apply date range filter if provided
            java.time.LocalDate fromDate = null, toDate = null;
            try { if (fromStr != null && !fromStr.isBlank()) fromDate = java.time.LocalDate.parse(fromStr); } catch (Exception ignored) {}
            try { if (toStr != null && !toStr.isBlank()) toDate = java.time.LocalDate.parse(toStr); } catch (Exception ignored) {}

            final java.time.LocalDate fd = fromDate; final java.time.LocalDate td = toDate;
            List<Booking> filtered = new ArrayList<>();
            for (Booking b : bookings) {
                boolean keep = true;
                if (statusFilter != null && !statusFilter.isBlank()) {
                    keep = b.getStatus() != null && b.getStatus().name().equalsIgnoreCase(statusFilter);
                }
                if (keep && fd != null) {
                    keep = b.getBooking_date() != null && !b.getBooking_date().toLocalDate().isBefore(fd);
                }
                if (keep && td != null) {
                    keep = b.getBooking_date() != null && !b.getBooking_date().toLocalDate().isAfter(td);
                }
                if (keep) filtered.add(b);
            }

            // Sort reverse by booking_date (most recent first)
            filtered.sort((a,b) -> {
                if (a.getBooking_date() == null && b.getBooking_date() == null) return 0;
                if (a.getBooking_date() == null) return 1;
                if (b.getBooking_date() == null) return -1;
                return b.getBooking_date().compareTo(a.getBooking_date());
            });

            request.setAttribute("bookings", filtered);
            // keep filter params for the view to render the filter UI
            request.setAttribute("filter_status", statusFilter);
            request.setAttribute("filter_from", fromStr);
            request.setAttribute("filter_to", toStr);
            request.getRequestDispatcher("/WEB-INF/jsp/customer/my_bookings.jsp").forward(request, response);
        }

        private void handleShowProfile(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            Customer customer = customerRepo.findById(cid).orElse(null);
            if (customer == null) {
                request.getSession().setAttribute("error", "Customer record not found");
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            // Ensure session has canonical customer attributes used by header.jsp
            HttpSession session = request.getSession();
            try {
                if (session.getAttribute("customer_id") == null) session.setAttribute("customer_id", customer.getId());
                // Keep session customer_name in sync with DB so header shows correct name
                Object existingName = session.getAttribute("customer_name");
                if (existingName == null || !existingName.equals(customer.getName())) {
                    session.setAttribute("customer_name", customer.getName());
                }
            } catch (Exception ignored) {}

            request.setAttribute("customer", customer);
            // The project uses manage_profile.jsp for the editable profile page
            request.getRequestDispatcher("/WEB-INF/jsp/customer/manage_profile.jsp").forward(request, response);
        }

        private void handleEditFeedback(HttpServletRequest request, HttpServletResponse response) throws IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            try {
                int feedbackId = Integer.parseInt(request.getParameter("feedback_id"));
                int rating = Integer.parseInt(request.getParameter("rating"));
                String comments = request.getParameter("comments");
                Feedback fb = feedbackRepo.findById(feedbackId).orElse(null);
                if (fb == null) {
                    request.getSession().setAttribute("errorFb", "Feedback not found");
                    response.sendRedirect(request.getContextPath() + "/customer/feedback");
                    return;
                }
                if (fb.getCustomer() == null || fb.getCustomer().getId() != cid) {
                    request.getSession().setAttribute("errorFb", "You are not authorized to edit this feedback");
                    response.sendRedirect(request.getContextPath() + "/customer/feedback");
                    return;
                }
                fb.setRating(rating);
                fb.setComments(comments);
                feedbackRepo.save(fb);
                request.getSession().setAttribute("msgFb", "Feedback updated");
            } catch (Exception e) {
                logger.error("Error editing feedback", e);
                request.getSession().setAttribute("errorFb", "Failed to update feedback");
            }
            response.sendRedirect(request.getContextPath() + "/customer/feedback");
        }

        private void handleDeleteFeedback(HttpServletRequest request, HttpServletResponse response) throws IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            try {
                int feedbackId = Integer.parseInt(request.getParameter("feedback_id"));
                Feedback fb = feedbackRepo.findById(feedbackId).orElse(null);
                if (fb == null) {
                    request.getSession().setAttribute("errorFb", "Feedback not found");
                    response.sendRedirect(request.getContextPath() + "/customer/feedback");
                    return;
                }
                if (fb.getCustomer() == null || fb.getCustomer().getId() != cid) {
                    request.getSession().setAttribute("errorFb", "You are not authorized to delete this feedback");
                    response.sendRedirect(request.getContextPath() + "/customer/feedback");
                    return;
                }
                feedbackRepo.deleteById(feedbackId);
                request.getSession().setAttribute("msgFb", "Feedback deleted");
            } catch (Exception e) {
                logger.error("Error deleting feedback", e);
                request.getSession().setAttribute("errorFb", "Failed to delete feedback");
            }
            response.sendRedirect(request.getContextPath() + "/customer/feedback");
        }

        // Provide feedback page attributes and forward to public feedback JSP
        private void handleShowFeedback(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            List<Feedback> all = (feedbackRepo != null) ? feedbackRepo.findAllFeedbackSorted() : Collections.emptyList();
            request.setAttribute("feedbacks", all);
            request.setAttribute("services", (serviceRepo != null) ? serviceRepo.findAll() : Collections.emptyList());
            request.setAttribute("currentCustomerId", cid);
            request.getRequestDispatcher("/WEB-INF/jsp/public/feedback.jsp").forward(request, response);
        }

        // Add item to session cart (and persist for logged-in customers)
        private void handleAddToCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
            HttpSession session = request.getSession();
            String pathInfo = request.getPathInfo(); // /add-to-cart/{id}
            String idParam = request.getParameter("id");
            Integer svcId = null;
            try {
                if (pathInfo != null && pathInfo.length() > 1) svcId = Integer.parseInt(pathInfo.substring(1));
                else if (idParam != null) svcId = Integer.parseInt(idParam);
            } catch (NumberFormatException ignored) {}

            if (svcId == null) {
                response.sendRedirect(request.getContextPath() + "/services");
                return;
            }

            List<Integer> cart = (List<Integer>) session.getAttribute("cart");
            if (cart == null) cart = new ArrayList<>();
            cart.add(svcId);
            session.setAttribute("cart", cart);

            Integer cid = (Integer) session.getAttribute("customer_id");
            if (cid != null && customerCartRepo != null) {
                String data = String.join(",", cart.stream().map(Object::toString).toArray(String[]::new));
                com.silvercare.models.CustomerCart cc = customerCartRepo.findByCustomer_Id(cid).orElseGet(() -> {
                    com.silvercare.models.CustomerCart n = new com.silvercare.models.CustomerCart();
                    n.setCustomer(customerRepo.findById(cid).orElse(null));
                    return n;
                });
                cc.setCart_data(data);
                customerCartRepo.save(cc);
            }

            session.setAttribute("msg", "Added to cart");
            response.sendRedirect(request.getContextPath() + "/customer/checkout");
        }

        // Update customer profile
        private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            try {
                Customer c = customerRepo.findById(cid).orElseThrow();
                String name = request.getParameter("name");
                String phone = request.getParameter("phone");
                String address = request.getParameter("address");
                String prefs = request.getParameter("carePreferences");
                String notes = request.getParameter("medicalNotes");
                String emergency = request.getParameter("emergencyContact");
                if (name != null) c.setName(name);
                if (phone != null) c.setPhone(phone);
                if (address != null) c.setAddress(address);
                if (prefs != null) c.setCarePreferences(prefs);
                if (notes != null) c.setMedicalNotes(notes);
                if (emergency != null) c.setEmergencyContact(emergency);
                customerRepo.save(c);
                // Update session-scoped customer display name so header and other pages reflect the change immediately
                try { request.getSession().setAttribute("customer_name", c.getName()); } catch (Exception ignored) {}
                request.getSession().setAttribute("msg", "Profile updated");
            } catch (Exception e) {
                logger.error("Failed to update profile", e);
                request.getSession().setAttribute("error", "Failed to update profile");
            }
            response.sendRedirect(request.getContextPath() + "/customer/profile");
        }

        // Handle checkout + immediate payment for cart
        private void handleCheckoutPay(HttpServletRequest request, HttpServletResponse response) throws IOException {
            HttpSession session = request.getSession();
            Integer cid = (Integer) session.getAttribute("customer_id");
            List<Integer> cart = (List<Integer>) session.getAttribute("cart");
            if (cid == null) {
                session.setAttribute("after_login_redirect", "/customer/checkout");
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            if (cart == null || cart.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/customer/booking");
                return;
            }
            try {
                Customer customer = customerRepo.findById(cid).orElseThrow();
                String paymentMethod = Optional.ofNullable(request.getParameter("payment_method")).orElse("online");
                BookingResult result = bookingService.createBookingsForCustomerPaid(cid.intValue(), customer, cart, paymentMethod);
                if (result != null && !result.getCreatedBookingIds().isEmpty()) {
                    session.removeAttribute("cart");
                    try { customerCartRepo.deleteByCustomer_Id(cid); } catch (Exception ignored) {}
                    session.setAttribute("msg", "Checkout and payment successful");
                    response.sendRedirect(request.getContextPath() + "/customer/booking-history");
                    return;
                }
                session.setAttribute("error", "Payment failed or no bookings created");
            } catch (Exception e) {
                logger.error("Checkout payment failed", e);
                session.setAttribute("error", "Checkout/payment failed: " + e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/customer/checkout");
        }

        // Submit feedback from logged-in user
        private void handleSubmitFeedback(HttpServletRequest request, HttpServletResponse response) throws IOException {
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) {
                response.sendRedirect(request.getContextPath() + "/customer/login");
                return;
            }
            try {
                int serviceId = Integer.parseInt(request.getParameter("service_id"));
                int rating = Integer.parseInt(request.getParameter("rating"));
                String comments = request.getParameter("comments");
                Feedback fb = new Feedback();
                fb.setCustomer(customerRepo.findById(cid).orElseThrow());
                fb.setService(serviceRepo.findById(serviceId).orElse(null));
                fb.setRating(rating);
                fb.setComments(comments);
                fb.setCreated_at(LocalDateTime.now());
                feedbackRepo.save(fb);
                request.getSession().setAttribute("msgFb", "Feedback submitted");
            } catch (Exception e) {
                logger.error("Failed to submit feedback", e);
                request.getSession().setAttribute("errorFb", "Failed to submit feedback");
            }
            response.sendRedirect(request.getContextPath() + "/customer/feedback");
        }

        // New: show receipt page for a booking
        private void handleShowReceipt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String pathInfo = request.getPathInfo(); // /receipt/{id}
            if (pathInfo == null || pathInfo.length() <= 1) { response.sendError(404); return; }
            Integer cid = (Integer) request.getSession().getAttribute("customer_id");
            if (cid == null) { request.getSession().setAttribute("after_login_redirect", request.getRequestURI()); response.sendRedirect(request.getContextPath() + "/customer/login"); return; }
            try {
                // robust parse of last URI segment
                String uri = request.getRequestURI().substring(request.getContextPath().length());
                String[] segs = uri.split("/");
                int id = Integer.parseInt(segs[segs.length-1]);
                 Booking booking = bookingRepo.findById(id).orElse(null);
                 if (booking == null || booking.getCustomer() == null || booking.getCustomer().getId() != cid) {
                     request.getSession().setAttribute("error", "Receipt not found or you don't have access.");
                    response.sendRedirect(request.getContextPath() + "/customer/booking-history");
                     return;
                 }
                 Payment payment = (paymentRepo != null) ? paymentRepo.findByBooking_Id(id).orElse(null) : null;
                 request.setAttribute("booking", booking);
                 request.setAttribute("payment", payment);
                 request.getRequestDispatcher("/WEB-INF/jsp/customer/receipt.jsp").forward(request, response);
             } catch (NumberFormatException nfe) {
                 response.sendError(404);
             }
         }

    }
