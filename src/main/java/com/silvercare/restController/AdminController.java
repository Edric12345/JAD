package com.silvercare.restController;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import com.silvercare.repositories.*; 
import com.silvercare.models.*; // Import all models
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Component
// @RequestMapping("/admin") // Disabled to allow servlets to be primary for view routes
public class AdminController {

    @Autowired
    private ServiceRepository serviceRepo;
    
    @Autowired
    private CustomerRepository customerRepo;
    
    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    // --- LOGIN & LOGOUT ---

    // @GetMapping("/login") // Disabled: servlet handles /admin/login
    public String showLoginPage() {
        return "admin/admin_login";
    }

    // @PostMapping("/login") // Disabled
    public String processLogin(@RequestParam String username, 
                               @RequestParam String password, 
                               HttpSession session, 
                               Model model) {
        return adminRepo.findByUsernameAndPassword(username, password)
            .map(admin -> {
                session.setAttribute("admin_username", admin.getUsername());
                return "redirect:/admin/dashboard";
            })
            .orElseGet(() -> {
                model.addAttribute("message", "Invalid admin login.");
                return "admin/admin_login";
            });
    }

    // @GetMapping("/logout") // Disabled
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // --- DASHBOARD ---

    // @GetMapping("/dashboard") // Disabled
    public String showDashboard(HttpSession session, Model model) {
        String adminUser = (String) session.getAttribute("admin_username");
        if (adminUser == null) return "redirect:/admin/login";

        model.addAttribute("adminUser", adminUser);
        return "admin/admin_dashboard";
    }

    // --- SERVICE MANAGEMENT ---

    // @GetMapping("/manage-services")
    public String manageServices(@RequestParam(required = false) String filter,
                                 @RequestParam(required = false) Integer threshold,
                                 Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        List<CareService> servicesList;

        // Prepare optional metric maps
        java.util.Map<Integer, Double> ratingsMap = new java.util.HashMap<>();
        java.util.Map<Integer, Long> demandMap = new java.util.HashMap<>();

        if (filter != null) {
            switch (filter) {
                case "topRated":
                    List<Object[]> topRated = feedbackRepo.findTopRatedServices();
                    servicesList = new java.util.ArrayList<>();
                    for (Object[] row : topRated) {
                        if (row == null || row.length < 2) continue;
                        CareService s = (CareService) row[0];
                        Double avg = row[1] == null ? null : ((Number) row[1]).doubleValue();
                        servicesList.add(s);
                        if (avg != null) ratingsMap.put(s.getId(), avg);
                    }
                    model.addAttribute("metricLabel", "Avg Rating");
                    break;
                case "lowRated":
                    List<Object[]> lowRated = feedbackRepo.findLowestRatedServices();
                    servicesList = new java.util.ArrayList<>();
                    for (Object[] row : lowRated) {
                        if (row == null || row.length < 2) continue;
                        CareService s = (CareService) row[0];
                        Double avg = row[1] == null ? null : ((Number) row[1]).doubleValue();
                        servicesList.add(s);
                        if (avg != null) ratingsMap.put(s.getId(), avg);
                    }
                    model.addAttribute("metricLabel", "Avg Rating");
                    break;
                case "highDemand":
                    List<Object[]> highDemand = bookingRepo.findHighDemandServices();
                    servicesList = new java.util.ArrayList<>();
                    for (Object[] row : highDemand) {
                        if (row == null || row.length < 2) continue;
                        CareService s = (CareService) row[0];
                        Long cnt = row[1] == null ? 0L : ((Number) row[1]).longValue();
                        servicesList.add(s);
                        demandMap.put(s.getId(), cnt);
                    }
                    model.addAttribute("metricLabel", "Bookings");
                    break;
                case "lowAvailability":
                    int thr = (threshold == null) ? 3 : threshold;
                    servicesList = serviceRepo.findLowAvailability(thr);
                    model.addAttribute("metricLabel", "Slots");
                    break;
                default:
                    servicesList = serviceRepo.findAll();
                    model.addAttribute("metricLabel", "");
            }
        } else {
            // default: show all services
            servicesList = serviceRepo.findAll();
            model.addAttribute("metricLabel", "");
        }

        model.addAttribute("services", servicesList);
        model.addAttribute("ratingsMap", ratingsMap);
        model.addAttribute("demandMap", demandMap);
        model.addAttribute("selectedFilter", filter);
        model.addAttribute("threshold", threshold == null ? 3 : threshold);
        return "admin/manage_services";
    }

    // Backwards-compatible mappings for direct JSP URLs (underscored filenames)
    // @GetMapping("/manage_services.jsp")
    public String legacyManageServices(Model model, HttpSession session) {
        // Delegate to the canonical handler so the model is populated
        return manageServices(null, null, model, session);
    }

    // @GetMapping("/add-service")
    public String showAddServiceForm(Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/add_service";
    }

    // @PostMapping("/add-service")
    public String processAddService(@ModelAttribute CareService service,
                                    @RequestParam(value = "availability", required = false) Integer availability,
                                    @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                    HttpSession session, RedirectAttributes ra) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uploads = "src/main/resources/static/images/services";
                Path dir = Paths.get(uploads);
                if (!Files.exists(dir)) Files.createDirectories(dir);
                String filename = Instant.now().getEpochSecond() + "_" + imageFile.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
                Path target = dir.resolve(filename);
                Files.copy(imageFile.getInputStream(), target);
                // store web path
                service.setImage_path("/images/services/" + filename);
            } catch (Exception ex) {
                ra.addFlashAttribute("error", "Failed to save image: " + ex.getMessage());
                return "redirect:/admin/add-service";
            }
        }

        if (availability != null) service.setAvailability(availability);
        serviceRepo.save(service);
        ra.addFlashAttribute("msg", "Service added");
        return "redirect:/admin/manage-services";
    }

    // --- CLIENT MANAGEMENT ---

    // @GetMapping("/manage-clients")
    public String manageClients(HttpSession session, Model model) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        
        List<Customer> clients = customerRepo.findAll();
        model.addAttribute("clients", clients);
        return "admin/manage_clients";
    }

    // @GetMapping("/manage_clients.jsp")
    public String legacyManageClients(HttpSession session, Model model) {
        return manageClients(session, model);
    }
    
    // @GetMapping("/delete-service/{id}")
    public String deleteService(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        // 1. Access Control: Check if admin is logged in
        if (session.getAttribute("admin_username") == null) {
            return "redirect:/admin/login";
        }

        // 2. Logic: Delete the service using the Repository
        try {
            serviceRepo.deleteById(id);
            ra.addFlashAttribute("msg", "Service deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not delete service. It might be linked to existing bookings.");
        }

        // 3. Redirect back to the management list
        return "redirect:/admin/manage-services";
    }
    
    // 1. Show the Edit Form with existing data
    // @GetMapping("/edit-service/{id}")
    public String showEditForm(@PathVariable int id, Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        // Fetch the service or throw an error if not found
        CareService service = serviceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service Id:" + id));

        model.addAttribute("service", service);
        model.addAttribute("categories", categoryRepo.findAll()); // For the dropdown
        return "admin/edit_service";
    }

    // 2. Process the Update
    // @PostMapping("/update-service/{id}")
    public String updateService(@PathVariable int id,
                                @ModelAttribute CareService service,
                                @RequestParam(value = "availability", required = false) Integer availability,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                HttpSession session,
                                RedirectAttributes ra) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        // preserve the ID
        service.setId(id);

        // If a new image was uploaded, save it and overwrite the path
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uploads = "src/main/resources/static/images/services";
                Path dir = Paths.get(uploads);
                if (!Files.exists(dir)) Files.createDirectories(dir);
                String filename = Instant.now().getEpochSecond() + "_" + imageFile.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
                Path target = dir.resolve(filename);
                Files.copy(imageFile.getInputStream(), target);
                service.setImage_path("/images/services/" + filename);
            } catch (Exception ex) {
                ra.addFlashAttribute("error", "Failed to save image: " + ex.getMessage());
                return "redirect:/admin/edit-service/" + id;
            }
        } else {
            // Keep existing image path if not provided: load current and apply
            Optional<CareService> existing = serviceRepo.findById(id);
            existing.ifPresent(e -> service.setImage_path(e.getImage_path()));
        }

        if (availability != null) service.setAvailability(availability);
        serviceRepo.save(service);
        ra.addFlashAttribute("msg", "Service updated");
        return "redirect:/admin/manage-services";
    }
    
    @Autowired
    private FeedbackRepository feedbackRepo;

    // @GetMapping("/feedback")
    public String viewFeedback(Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        // Replaces the PreparedStatement and ResultSet logic
        List<Feedback> feedbackList = feedbackRepo.findAllFeedbackSorted();
        model.addAttribute("feedbacks", feedbackList);
        
        return "admin/manage_feedback";
    }

    // @GetMapping("/manage_feedback.jsp")
    public String legacyManageFeedback(Model model, HttpSession session) {
        return viewFeedback(model, session);
    }

    @Autowired
    private BookingRepository bookingRepo;

    // @GetMapping("/reports/services")
    public String serviceReports(Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        // Top and lowest rated services
        List<Object[]> topRated = feedbackRepo.findTopRatedServices();
        List<Object[]> lowRated = feedbackRepo.findLowestRatedServices();

        // High demand services (service entity, count)
        List<Object[]> highDemand = bookingRepo.findHighDemandServices();

        // Low availability services (threshold 3)
        List<CareService> lowAvail = serviceRepo.findLowAvailability(3);

        model.addAttribute("topRated", topRated);
        model.addAttribute("lowRated", lowRated);
        model.addAttribute("highDemand", highDemand);
        model.addAttribute("lowAvail", lowAvail);
        return "admin/report_services";
    }
  
    // @GetMapping("/edit-client/{id}")
    public String showEditClient(@PathVariable int id, HttpSession session, Model model) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        Customer c = customerRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid client id:" + id));
        model.addAttribute("client", c);
        return "admin/edit_client";
    }

    // @PostMapping("/update-client/{id}")
    public String updateClient(@PathVariable int id,
                               @RequestParam String name,
                               @RequestParam String email,
                               @RequestParam(required = false) String phone,
                               @RequestParam(required = false) String address,
                               @RequestParam(required = false) String carePreferences,
                               @RequestParam(required = false) String medicalNotes,
                               @RequestParam(required = false) String emergencyContact,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        Customer c = customerRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid client id:" + id));
        c.setName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setAddress(address);
        c.setCarePreferences(carePreferences);
        c.setMedicalNotes(medicalNotes);
        c.setEmergencyContact(emergencyContact);
        customerRepo.save(c);
        ra.addFlashAttribute("msg", "Client updated");
        return "redirect:/admin/manage-clients";
    }

    // @GetMapping("/delete-client/{id}")
    public String deleteClient(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        try { customerRepo.deleteById(id); ra.addFlashAttribute("msg","Client deleted"); }
        catch (Exception ex) { ra.addFlashAttribute("error","Failed to delete client: " + ex.getMessage()); }
        return "redirect:/admin/manage-clients";
    }
    
    @Autowired
    private PaymentRepository paymentRepo;

      
    @GetMapping("/manage-clients/report") // 2. UNCOMMENT THIS
    public String manageClients(
            @RequestParam(name = "area", required = false, defaultValue = "") String area,
            @RequestParam(name = "careNeeds", required = false, defaultValue = "") String careNeeds,
            HttpSession session, 
            Model model) {
        
        // Security Check
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        List<Customer> clients;

        // 3. Logic: If both are empty, show all. Otherwise, use the Repository search.
        if (area.isBlank() && careNeeds.isBlank()) {
            clients = customerRepo.findAll();
        } else {
            // This calls the Database using the custom method we added to the Repository
            clients = customerRepo.findByAddressContainingIgnoreCaseAndCarePreferencesContainingIgnoreCase(area, careNeeds);
        }

        model.addAttribute("clients", clients);
        model.addAttribute("area", area);
        model.addAttribute("careNeeds", careNeeds);
        
        return "admin/manage_clients"; 
    }
    
    // @GetMapping("/reports/sales")
    public String salesReports(@RequestParam(required = false) String from,
                               @RequestParam(required = false) String to,
                               Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        java.time.LocalDateTime fromDt = null, toDt = null;
        try {
            if (from != null && !from.isBlank()) fromDt = java.time.LocalDate.parse(from).atStartOfDay();
            if (to != null && !to.isBlank()) toDt = java.time.LocalDate.parse(to).atTime(23,59,59);
        } catch (Exception ex) {
            // ignore parse errors; fallback to null
        }

        List<Payment> payments;
        if (fromDt != null && toDt != null) payments = paymentRepo.findByPaidAtBetween(fromDt, toDt);
        else payments = paymentRepo.findAll();

        // Top clients by spend
        List<Object[]> topClients = paymentRepo.findTopClientsBySpend();

        model.addAttribute("payments", payments);
        model.addAttribute("topClients", topClients);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "admin/report_sales";
    }
    
    // Service search for admin
    // @GetMapping("/manage-services/search")
    public String searchServices(@RequestParam(required = false) String q, Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        List<CareService> results;
        if (q == null || q.isBlank()) results = serviceRepo.findAll();
        else results = serviceRepo.searchByName(q.trim());
        model.addAttribute("services", results);
        model.addAttribute("searchQuery", q);
        return "admin/manage_services";
    }

    // Client reporting: list clients by area or care needs
    // @GetMapping("/manage-clients/report")
    public String clientReport(@RequestParam(required = false) String area,
                               @RequestParam(required = false) String careNeeds,
                               HttpSession session, Model model) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        List<Customer> all = customerRepo.findAll();
        List<Customer> filtered = all.stream().filter(c -> {
            boolean ok = true;
            if (area != null && !area.isBlank()) ok &= (c.getAddress() != null && c.getAddress().contains(area));
            if (careNeeds != null && !careNeeds.isBlank()) ok &= (c.getCarePreferences() != null && c.getCarePreferences().toLowerCase().contains(careNeeds.toLowerCase()));
            return ok;
        }).toList();

        model.addAttribute("clients", filtered);
        model.addAttribute("area", area);
        model.addAttribute("careNeeds", careNeeds);
        return "admin/manage_clients";
    }

    // Admin reporting: list clients by service
    // @GetMapping("/reports/clients-by-service")
    public String clientsByService(@RequestParam(required = false) Integer serviceId, Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        List<Customer> clients = List.of();
        if (serviceId != null) {
            clients = bookingRepo.findCustomersByServiceId(serviceId);
        }
        model.addAttribute("clients", clients);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("services", serviceRepo.findAll());
        return "admin/manage_clients"; // reuse the clients table view to show results
    }

    // Generic handler for other legacy direct JSP links like /admin/whatever.jsp
    // @GetMapping("/{jspName:.+\\.jsp}")
    public String catchAllLegacyAdminJsps(@PathVariable String jspName, Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        switch (jspName) {
            case "manage_services.jsp":
                return manageServices(null, null, model, session);
            case "manage_clients.jsp":
                return manageClients(session, model);
            case "manage_feedback.jsp":
                    return viewFeedback(model, session);
            default:
                // Fallback: try to forward to a same-named view under admin/ without .jsp
                String view;
                if (jspName != null && jspName.endsWith(".jsp")) {
                    view = "admin/" + jspName.substring(0, jspName.length() - 4);
                } else {
                    view = "admin/" + jspName;
                }
                return view;
        }
    }

    // @GetMapping("/add-client")
    public String showAddClientForm(Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        model.addAttribute("client", new Customer());
        return "admin/add_client";
    }

    // @PostMapping("/add-client")
    public String processAddClient(@ModelAttribute Customer client, HttpSession session, RedirectAttributes ra) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";
        customerRepo.save(client);
        ra.addFlashAttribute("msg", "Client added");
        return "redirect:/admin/manage-clients";
    }

    // @GetMapping("/manage-billings")
    public String manageBillings(@RequestParam(required = false) String from,
                                 @RequestParam(required = false) String to,
                                 @RequestParam(required = false) String month,
                                 @RequestParam(required = false) Integer serviceId,
                                 Model model, HttpSession session) {
        if (session.getAttribute("admin_username") == null) return "redirect:/admin/login";

        LocalDateTime fromDt = null, toDt = null;
        try {
            if (month != null && !month.isBlank()) {
                // month in YYYY-MM format
                LocalDate m = LocalDate.parse(month + "-01");
                fromDt = m.atStartOfDay();
                toDt = m.with(TemporalAdjusters.lastDayOfMonth()).atTime(23,59,59);
            } else {
                if (from != null && !from.isBlank()) fromDt = LocalDate.parse(from).atStartOfDay();
                if (to != null && !to.isBlank()) toDt = LocalDate.parse(to).atTime(23,59,59);
            }
        } catch (Exception ex) {
            // ignore parse errors
        }

        List<Booking> bookings;
        if (fromDt != null && toDt != null) bookings = bookingRepo.findByBookingDateBetween(fromDt, toDt);
        else bookings = bookingRepo.findAll();

        // Payments in period
        List<Payment> payments;
        if (fromDt != null && toDt != null) payments = paymentRepo.findByPaidAtBetween(fromDt, toDt);
        else payments = paymentRepo.findAll();

        // Top clients by spend
        List<Object[]> topClients = paymentRepo.findTopClientsBySpend();

        // Clients who booked a specific service
        List<Customer> clientsByService = List.of();
        if (serviceId != null) {
            clientsByService = bookingRepo.findCustomersByServiceId(serviceId);
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("payments", payments);
        model.addAttribute("topClients", topClients);
        model.addAttribute("clientsByService", clientsByService);
        model.addAttribute("services", serviceRepo.findAll());
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("month", month);
        model.addAttribute("serviceId", serviceId);

        return "admin/manage_billings";
    }
}