package com.silvercare.restController;

import com.silvercare.models.*;
import com.silvercare.repositories.*;
import com.silvercare.services.BookingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.UnexpectedRollbackException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
// @RequestMapping("/customer") // Disabled: `com.silvercare.servlet.CustomerServlet` handles /customer/* routes
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CategoryRepository categoryRepo;
    
    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private BookingRepository bookingRepo;
    
    @Autowired
    private CustomerRepository customerRepo;
    
    @Autowired
    private FeedbackRepository feedbackRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private CustomerCartRepository customerCartRepo;

    @Autowired
    private BookingService bookingService;

    

 // --- LOGIN ---
 // Move these outside the /customer mapping or put them in a PublicController
    // @GetMapping("/login")
    public String showLoginPage() {
        return "public/login"; 
    }

    // @PostMapping("/login")
    public String handleLogin(@RequestParam String email, 
                              @RequestParam String password, 
                              HttpSession session, 
                              RedirectAttributes ra) {
        
        return customerRepo.findByEmailAndPassword(email, password)
            .map(customer -> {
                session.setAttribute("customer_id", customer.getId());
                session.setAttribute("customer_name", customer.getName());

                // Restore cart from DB if any
                // Merge persisted cart with any guest session cart (guest cart should not be lost on login)
                List<Integer> sessionCart = (List<Integer>) session.getAttribute("cart");
                List<Integer> merged = new ArrayList<>();

                // Load persisted cart
                customerCartRepo.findByCustomer_Id(customer.getId()).ifPresent(cc -> {
                    String data = cc.getCart_data();
                    if (data != null && !data.isBlank()) {
                        for (String s : data.split(",")) {
                            try {
                                String t = s.trim();
                                if (!t.isEmpty()) {
                                    int v = Integer.parseInt(t);
                                    if (!merged.contains(v)) merged.add(v);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                });

                // Merge in-session (guest) cart items, preserving existing order and avoiding duplicates
                if (sessionCart != null) {
                    for (Integer v : sessionCart) {
                        if (!merged.contains(v)) merged.add(v);
                    }
                }

                // Save merged cart back to session and persist to DB
                if (!merged.isEmpty()) {
                    session.setAttribute("cart", merged);
                    com.silvercare.models.CustomerCart cc = customerCartRepo.findByCustomer_Id(customer.getId()).orElseGet(() -> {
                        com.silvercare.models.CustomerCart n = new com.silvercare.models.CustomerCart();
                        n.setCustomer(customer);
                        return n;
                    });
                    String data = String.join(",", merged.stream().map(Object::toString).toArray(String[]::new));
                    cc.setCart_data(data);
                    customerCartRepo.save(cc);
                }

                // If a redirect URL was saved by a protected endpoint, use it
                String after = (String) session.getAttribute("after_login_redirect");
                if (after != null && !after.isBlank()) {
                    session.removeAttribute("after_login_redirect");
                    return "redirect:" + after;
                }

                return "redirect:/customer/profile"; // Redirects into the protected area
            })
            .orElseGet(() -> {
                ra.addFlashAttribute("error", "Invalid email or password");
                return "redirect:/customer/login";
            });
    }

    // --- REGISTRATION ---
    // @GetMapping("/register")
    public String showRegisterPage() {
        return "public/register";
    }

    // @PostMapping("/register")
    public String handleRegistration(@ModelAttribute Customer customer, RedirectAttributes ra) {
        // Save the new customer object directly
        customerRepo.save(customer);
        ra.addFlashAttribute("msg", "Registration successful! Please login.");
        return "redirect:/customer/login";
    }

    // --- LOGOUT ---
    // @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/customer/login";
    }
    
    
    // --- SERVICE & CATEGORY BROWSING ---

    // @GetMapping("/services/categories")
    public String showCategories(Model model) {
        List<ServiceCategory> categories = categoryRepo.findAll();
        model.addAttribute("categories", categories);
        return "public/service_category";
    }

    // New: Logic for service_details.jsp
    // @GetMapping("/services/{categoryId}")
    public String showServiceDetails(@PathVariable int categoryId, Model model) {
        ServiceCategory category = categoryRepo.findById(categoryId).orElse(null);
        if (category == null) return "redirect:/customer/services/categories";

        List<CareService> services = serviceRepo.findByCategoryId(categoryId);
        model.addAttribute("category", category);
        model.addAttribute("services", services);
        return "public/service_details";
    }

    // --- CART & BOOKING ---
    
    // @PostMapping("/confirm-booking")
    public String confirmBooking(HttpSession session, RedirectAttributes ra) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        List<Integer> cart = (List<Integer>) session.getAttribute("cart");

        if (cid == null) {
            session.setAttribute("after_login_redirect", "/customer/checkout");
            return "redirect:/customer/login";
        }
        if (cart == null || cart.isEmpty()) {
            ra.addFlashAttribute("cartEmpty", true);
            return "redirect:/customer/services/categories";
        }

        	

        try {
            Customer customer = customerRepo.findById(cid).orElseThrow(() -> new RuntimeException("Customer not found"));

            logger.info("Confirming booking for customer {} with cart items {}", cid, cart);

            // delegate to service to create pending bookings
            com.silvercare.services.BookingResult result = bookingService.createBookingsForCustomerPending(cid, customer, cart);

            if (result == null || result.getCreatedBookingIds().isEmpty()) {
                logger.warn("No pending bookings created for customer {}", cid);
                ra.addFlashAttribute("error", "No bookings were created. Please try again or contact support.");
                return "redirect:/customer/checkout";
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

            ra.addFlashAttribute("msg", "Booking confirmed. Booking IDs: " + result.getCreatedBookingIds());
            return "redirect:/customer/my-bookings";
        } catch (Exception ex) {
            logger.error("Error confirming booking for customer {}: {}", cid, ex.getMessage(), ex);
            ra.addFlashAttribute("error", "Failed to confirm booking: " + ex.getMessage());
            return "redirect:/customer/checkout";
        }
    }
    
    // @PostMapping("/add-to-cart/{id}")
    public String addToCart(@PathVariable int id, HttpSession session) {
        List<Integer> cart = (List<Integer>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        
        cart.add(id);
        session.setAttribute("cart", cart);

        // Persist to DB if user logged in
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid != null) {
            String data = String.join(",", cart.stream().map(Object::toString).toArray(String[]::new));
            com.silvercare.models.CustomerCart cc = customerCartRepo.findByCustomer_Id(cid).orElseGet(() -> {
                com.silvercare.models.CustomerCart n = new com.silvercare.models.CustomerCart();
                n.setCustomer(customerRepo.findById(cid).orElseThrow());
                return n;
            });
            cc.setCart_data(data);
            customerCartRepo.save(cc);
        }
        return "redirect:/customer/booking";
    }
    
    // @GetMapping("/checkout")
    public String showCheckout(HttpSession session, Model model) {
        List<Integer> cart = (List<Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) return "redirect:/customer/booking";

        double subtotal = 0;
        List<CareService> selectedServices = new ArrayList<>();

        for (Integer id : cart) {
            CareService s = serviceRepo.findById(id).orElse(null);
            if (s != null) {
                selectedServices.add(s);
                subtotal += s.getPrice();
            }
        }

        double gstRate = 0.09; // 9% GST for 2026
        double gstAmount = subtotal * gstRate;
        double total = subtotal + gstAmount;

        model.addAttribute("selectedServices", selectedServices);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("gstAmount", gstAmount);
        model.addAttribute("total", total);

        return "customer/checkout"; // Points to checkout.jsp
    }
    
    
    // @GetMapping("/booking")
    public String showBookingPage(HttpSession session, Model model) {
        if (session.getAttribute("customer_id") == null) return "redirect:/customer/login";

        List<CareService> services = serviceRepo.findAll();
        model.addAttribute("services", services);
        return "customer/booking";
    }

    // @GetMapping("/my-bookings")
    public String showMyBookings(HttpSession session, Model model) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        List<Booking> myBookings = bookingRepo.findByCustomer_Id(cid);
        // Filter out cancelled bookings
        List<Booking> visibleBookings = myBookings.stream()
                .filter(b -> b.getStatus() == null || b.getStatus() != com.silvercare.models.BookingStatus.CANCELLED)
                .collect(Collectors.toList());
        model.addAttribute("bookings", visibleBookings);
        return "customer/my_bookings";
    }

    // @GetMapping("/booking-history")
    public String showBookingHistory(HttpSession session, Model model) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        List<Booking> bookings = bookingRepo.findByCustomer_Id(cid);
        model.addAttribute("bookings", bookings);
        return "customer/booking_history";
    }

    // @GetMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        int updatedRows = bookingRepo.cancelBooking(id, cid);
        if (updatedRows > 0) {
            ra.addFlashAttribute("msg", "Booking #" + id + " has been cancelled.");
        } else {
            ra.addFlashAttribute("error", "Failed to cancel booking.");
        }
        return "redirect:/customer/my-bookings";
    }

    // --- FEEDBACK ---

    // @GetMapping("/feedback")
    public String showFeedbackForm(HttpSession session, Model model) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) {
            session.setAttribute("after_login_redirect", "/customer/feedback");
            return "redirect:/customer/login";
        }
        model.addAttribute("services", serviceRepo.findAll());
        // Add all feedbacks to the page (most recent first)
        List<Feedback> feedbacks = feedbackRepo.findAllFeedbackSorted();
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("currentCustomerId", cid);
        return "public/feedback"; // changed to public folder where feedback.jsp exists
    }

    // @PostMapping("/feedback")
    public String submitFeedback(@RequestParam int service_id, @RequestParam int rating,
                                 @RequestParam String comments, HttpSession session, RedirectAttributes ra) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        Customer customer = customerRepo.findById(cid).orElseThrow();
        CareService service = serviceRepo.findById(service_id).orElseThrow();

        Feedback feedback = new Feedback();
        feedback.setCustomer(customer);
        feedback.setService(service);
        feedback.setRating(rating);
        feedback.setComments(comments);
        feedback.setCreated_at(LocalDateTime.now());

        feedbackRepo.save(feedback);

        ra.addFlashAttribute("msgFb", "Thank you for your feedback!");
        return "redirect:/customer/feedback"; // PRG: redirect so page reloads with updated feedback list
    }

    // Edit feedback (only owner can edit)
    // @PostMapping("/feedback/edit")
    public String editFeedback(@RequestParam int feedback_id,
                               @RequestParam int rating,
                               @RequestParam String comments,
                               HttpSession session,
                               RedirectAttributes ra) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        Feedback fb = feedbackRepo.findById(feedback_id).orElse(null);
        if (fb == null) {
            ra.addFlashAttribute("errorFb", "Feedback not found");
            return "redirect:/customer/feedback";
        }
        if (fb.getCustomer() == null || fb.getCustomer().getId() != cid) {
            ra.addFlashAttribute("errorFb", "You are not authorized to edit this feedback");
            return "redirect:/customer/feedback";
        }

        fb.setRating(rating);
        fb.setComments(comments);
        // keep created_at as original
        feedbackRepo.save(fb);
        ra.addFlashAttribute("msgFb", "Feedback updated");
        return "redirect:/customer/feedback";
    }

    // Delete feedback (only owner can delete)
    // @PostMapping("/feedback/delete")
    public String deleteFeedback(@RequestParam int feedback_id, HttpSession session, RedirectAttributes ra) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        Feedback fb = feedbackRepo.findById(feedback_id).orElse(null);
        if (fb == null) {
            ra.addFlashAttribute("errorFb", "Feedback not found");
            return "redirect:/customer/feedback";
        }
        if (fb.getCustomer() == null || fb.getCustomer().getId() != cid) {
            ra.addFlashAttribute("errorFb", "You are not authorized to delete this feedback");
            return "redirect:/customer/feedback";
        }

        feedbackRepo.deleteById(feedback_id);
        ra.addFlashAttribute("msgFb", "Feedback deleted");
        return "redirect:/customer/feedback";
    }

    // --- PROFILE MANAGEMENT ---

    // @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        Customer customer = customerRepo.findById(cid).orElse(null);
        model.addAttribute("customer", customer);
        return "customer/manage_profile";
    }

    // @PostMapping("/update-profile")
    public String handleProfileUpdate(@RequestParam String name, @RequestParam String email,
                                      @RequestParam String phone, @RequestParam String address,
                                      @RequestParam(required = false) String carePreferences,
                                      @RequestParam(required = false) String medicalNotes,
                                      @RequestParam(required = false) String emergencyContact,
                                      HttpSession session, RedirectAttributes ra) {

        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        Customer customer = customerRepo.findById(cid).orElseThrow();
        // Basic validation and normalization
        customer.setName(name != null ? name.trim() : customer.getName());
        customer.setEmail(email != null ? email.trim() : customer.getEmail());
        customer.setPhone(phone != null ? phone.trim() : customer.getPhone());
        customer.setAddress(address != null ? address.trim() : customer.getAddress());

        // New fields: persist care preferences, medical notes and emergency contact
        customer.setCarePreferences(carePreferences != null ? carePreferences.trim() : customer.getCarePreferences());
        customer.setMedicalNotes(medicalNotes != null ? medicalNotes.trim() : customer.getMedicalNotes());
        customer.setEmergencyContact(emergencyContact != null ? emergencyContact.trim() : customer.getEmergencyContact());

        customerRepo.save(customer);
        ra.addFlashAttribute("msg", "Profile updated successfully!");
        return "redirect:/customer/profile";
    }

    // @GetMapping("/book-service/{id}")
    public String showBookService(@PathVariable int id, HttpSession session, Model model) {
        // ensure user is logged in
        if (session.getAttribute("customer_id") == null) {
            // save intended URL and redirect to login
            session.setAttribute("after_login_redirect", "/customer/book-service/" + id);
            return "redirect:/customer/login";
        }

        CareService service = serviceRepo.findById(id).orElse(null);
        if (service == null) return "redirect:/customer/services/categories";

        model.addAttribute("service", service);
        return "customer/booking"; // renders customer/booking.jsp
    }

    // @PostMapping("/book-service/{id}")
    public String processBookService(@PathVariable int id,
                                     @RequestParam(required = false) String date,
                                     HttpSession session,
                                     RedirectAttributes ra) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) {
            session.setAttribute("after_login_redirect", "/customer/book-service/" + id);
            return "redirect:/customer/login";
        }

        Customer customer = customerRepo.findById(cid).orElseThrow();
        CareService service = serviceRepo.findById(id).orElseThrow();

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setStatus(com.silvercare.models.BookingStatus.PENDING);
        booking.setBooking_date(LocalDateTime.now());

        BookingDetails detail = new BookingDetails();
        detail.setService(service);
        detail.setBooking(booking);
        detail.setPriceAtBooking(service.getPrice());
        // If you want to store the requested date, add parsing here and set it on BookingDetails

        booking.setDetails(List.of(detail));
        bookingRepo.save(booking);

        ra.addFlashAttribute("msg", "Booking confirmed successfully!");
        return "redirect:/customer/my-bookings";
    }

    // @PostMapping("/checkout/pay")
    public String processCheckoutPayment(@RequestParam(required = false, defaultValue = "online") String paymentMethod,
                                         HttpSession session,
                                         RedirectAttributes ra) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) {
            session.setAttribute("after_login_redirect", "/customer/checkout");
            return "redirect:/customer/login";
        }

        List<Integer> cart = (List<Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            ra.addFlashAttribute("error", "Your cart is empty.");
            return "redirect:/customer/booking";
        }

        try {
            Customer customer = customerRepo.findById(cid).orElseThrow(() -> new RuntimeException("Customer not found"));

            logger.info("Starting checkout for customer {} with cart items {} and paymentMethod={}", cid, cart, paymentMethod);

            // Simulate payment processing here (assume success)
            boolean paymentSuccess = true;
            if (!paymentSuccess) {
                ra.addFlashAttribute("error", "Payment failed. Please try again.");
                return "redirect:/customer/checkout";
            }

            com.silvercare.services.BookingResult result;
            try {
                // delegate to service to create bookings and payments (service may delete persisted cart)
                result = bookingService.createBookingsForCustomerPaid(cid, customer, cart, paymentMethod);
            } catch (UnexpectedRollbackException ure) {
                logger.error("UnexpectedRollbackException during createBookingsForCustomerPaid for customer {}: {}", cid, ure.getMessage(), ure);
                ra.addFlashAttribute("error", "Payment processing failed due to a transaction error. Please try again or contact support.");
                return "redirect:/customer/checkout";
            } catch (Throwable t) {
                logger.error("Unhandled exception during createBookingsForCustomerPaid for customer {}: {}", cid, t.getMessage(), t);
                ra.addFlashAttribute("error", "Payment processing failed: " + t.getMessage());
                return "redirect:/customer/checkout";
            }

             if (result == null || result.getCreatedBookingIds().isEmpty()) {
                 logger.warn("No bookings created for customer {} during checkout", cid);
                 ra.addFlashAttribute("error", "Payment succeeded but no bookings were created. Please contact support.");
                 return "redirect:/customer/checkout";
             }

            // Remove succeeded service IDs from session cart and persist remaining ones
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

            ra.addFlashAttribute("msg", "Payment successful. Created bookings: " + result.getCreatedBookingIds());
            return "redirect:/customer/my-bookings";
        } catch (Exception ex) {
            logger.error("Error during checkout/pay for customer {}: {}", cid, ex.getMessage(), ex);
            ra.addFlashAttribute("error", "Payment processing failed: " + ex.getMessage());
            return "redirect:/customer/checkout";
        }
    }

    // @PostMapping("/booking/pay/{id}")
    public String paySingleBooking(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        Booking booking = bookingRepo.findById(id).orElse(null);
        if (booking == null || booking.getCustomer() == null || booking.getCustomer().getId() != cid) {
            ra.addFlashAttribute("error", "Booking not found or you do not have permission to pay for it.");
            return "redirect:/customer/booking-history";
        }

        if (booking.getStatus() != null && booking.getStatus() != com.silvercare.models.BookingStatus.PENDING) {
            ra.addFlashAttribute("error", "Only pending bookings can be paid.");
            return "redirect:/customer/booking-history";
        }

        // For now, simulate payment success and create Payment linked to booking
        try {
            double total = 0;
            if (booking.getDetails() != null) {
                for (BookingDetails d : booking.getDetails()) {
                    total += d.getSubtotal();
                }
            }

            // compute GST and amounts consistent with payments schema
            java.math.BigDecimal excl = java.math.BigDecimal.valueOf(total).setScale(2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal gst = excl.multiply(java.math.BigDecimal.valueOf(0.09)).setScale(2, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal tot = excl.add(gst).setScale(2, java.math.RoundingMode.HALF_UP);

            boolean ok = bookingService.createPaymentForBooking(booking.getId(), booking.getCustomer().getId(), excl, gst, tot, "online", total);
            if (!ok) {
                ra.addFlashAttribute("error", "Payment recording failed. Please contact support.");
                return "redirect:/customer/booking-history";
            }

             booking.setStatus(com.silvercare.models.BookingStatus.PAID);
             bookingRepo.save(booking);

             ra.addFlashAttribute("msg", "Payment recorded and booking marked as paid.");
             return "redirect:/customer/booking-history";
         } catch (Exception ex) {
             logger.error("Error paying booking {}: {}", id, ex.getMessage(), ex);
             ra.addFlashAttribute("error", "Failed to record payment: " + ex.getMessage());
             return "redirect:/customer/booking-history";
         }
    }

    // @GetMapping("/cart-debug")
    // @ResponseBody
    public ResponseEntity<?> cartDebug(HttpSession session) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        List<Integer> sessionCart = (List<Integer>) session.getAttribute("cart");
        String persisted = null;
        if (cid != null) {
            persisted = customerCartRepo.findByCustomer_Id(cid).map(com.silvercare.models.CustomerCart::getCart_data).orElse(null);
        }
        return ResponseEntity.ok(Map.of(
                "customer_id", cid,
                "sessionCart", sessionCart == null ? List.of() : sessionCart,
                "persistedCart", persisted
        ));
    }

    // @PostMapping("/cart/save")
    // @ResponseBody
    public ResponseEntity<?> saveSessionCart(HttpSession session) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "not logged in"));
        List<Integer> cart = (List<Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","session cart empty"));

        com.silvercare.models.CustomerCart cc = customerCartRepo.findByCustomer_Id(cid).orElseGet(() -> {
            com.silvercare.models.CustomerCart n = new com.silvercare.models.CustomerCart();
            n.setCustomer(customerRepo.findById(cid).orElseThrow());
            return n;
        });
        String data = String.join(",", cart.stream().map(Object::toString).toArray(String[]::new));
        cc.setCart_data(data);
        customerCartRepo.save(cc);
        return ResponseEntity.ok(Map.of("saved", true, "data", data));
    }

    // @GetMapping("/debug/bookings")
    // @ResponseBody
    public ResponseEntity<?> debugBookings(HttpSession session) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","not logged in"));

        List<Booking> bookings = bookingRepo.findByCustomer_Id(cid);
        List<Map<String,Object>> out = new ArrayList<>();
        for (Booking b : bookings) {
            Map<String,Object> bm = Map.of(
                    "id", b.getId(),
                    "status", b.getStatus() == null ? null : b.getStatus().name(),
                    "booking_date", b.getFormattedBookingDate(),
                    "details", b.getDetails() == null ? List.of() : b.getDetails().stream().map(d -> Map.of(
                            "serviceId", d.getService() == null ? null : d.getService().getId(),
                            "serviceName", d.getService() == null ? null : d.getService().getService_name(),
                            "priceAtBooking", d.getPriceAtBooking(),
                            "subtotal", d.getSubtotal()
                    )).toList()
            );
            out.add(bm);
        }

        List<Payment> payments = paymentRepo.findAll(); // small dataset in dev; filter by customer
        List<Payment> custPayments = new ArrayList<>();
        for (Payment p : payments) if (p.getCustomer() != null && p.getCustomer().getId() == cid) custPayments.add(p);

        return ResponseEntity.ok(Map.of("bookings", out, "payments", custPayments));
    }

    // @GetMapping("/receipt/{id}")
    public String viewReceipt(@PathVariable int id, HttpSession session, Model model) {
        Integer cid = (Integer) session.getAttribute("customer_id");
        if (cid == null) return "redirect:/customer/login";

        Booking booking = bookingRepo.findById(id).orElse(null);
        if (booking == null || booking.getCustomer() == null || booking.getCustomer().getId() != cid) {
            model.addAttribute("error", "Receipt not found or you don't have access.");
            return "customer/booking_history";
        }

        // Find associated payment if any
        Payment payment = paymentRepo.findByBooking_Id(id).orElse(null);

        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        return "customer/receipt";
    }
}
