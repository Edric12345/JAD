package com.silvercare.restController;

import com.silvercare.models.*;
import com.silvercare.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/external")
@CrossOrigin(origins = "*") // allow partners to call this API in dev; tighten in production
public class PartnerBookingController {

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private BookingRepository bookingRepo;

    // Create bookings for a partner on behalf of a client
    // Expected JSON: { "customer": {"name":"...","email":"...","phone":"...","address":"..."}, "serviceIds": [1,2], "prepaid": true }
    @PostMapping("/bookings")
    public ResponseEntity<?> createBookingsForPartner(@RequestBody Map<String, Object> payload) {
        // Defensive parsing
        Object customerObj = payload.get("customer");
        Object serviceIdsObj = payload.get("serviceIds");
        Object prepaidObj = payload.get("prepaid");

        if (!(customerObj instanceof Map) || serviceIdsObj == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload: missing customer or serviceIds");
        }

        Map<?, ?> customerMap = (Map<?, ?>) customerObj;

        // Extract and validate service IDs list (accept numbers or strings)
        List<Integer> serviceIds = new ArrayList<>();
        if (serviceIdsObj instanceof List) {
            for (Object o : (List<?>) serviceIdsObj) {
                if (o instanceof Number) {
                    serviceIds.add(((Number) o).intValue());
                } else if (o instanceof String) {
                    try { serviceIds.add(Integer.parseInt((String) o)); } catch (NumberFormatException ignored) {}
                }
            }
        }

        boolean prepaid = false;
        if (prepaidObj != null) {
            if (prepaidObj instanceof Boolean) prepaid = (Boolean) prepaidObj;
            else prepaid = Boolean.parseBoolean(prepaidObj.toString());
        }

        if (serviceIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload: serviceIds must be a non-empty array of ids");
        }

        String email = (String) customerMap.get("email");
        if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer email required");

        Customer customer = customerRepo.findByEmail(email).orElse(null);
        if (customer == null) {
            // Create a minimal customer record with safe conversions
            Object nameObj = customerMap.get("name");
            Object phoneObj = customerMap.get("phone");
            Object addressObj = customerMap.get("address");

            String name = nameObj != null ? nameObj.toString() : "Partner Client";
            String phone = phoneObj != null ? phoneObj.toString() : "";
            String address = addressObj != null ? addressObj.toString() : "";

            customer = new Customer();
            customer.setName(name);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setAddress(address);
            customer.setPassword(""); // empty password for partner-created accounts
            customerRepo.save(customer);
        }

        List<Integer> createdBookingIds = new ArrayList<>();

        for (Integer sid : serviceIds) {
            CareService service = serviceRepo.findById(sid).orElse(null);
            if (service == null) continue;

            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setStatus(prepaid ? com.silvercare.models.BookingStatus.PAID : com.silvercare.models.BookingStatus.PENDING);
            booking.setBooking_date(LocalDateTime.now());

            BookingDetails detail = new BookingDetails();
            detail.setService(service);
            detail.setBooking(booking);
            detail.setPriceAtBooking(service.getPrice());
            detail.setSubtotal(service.getPrice());

            booking.setDetails(List.of(detail));
            Booking saved = bookingRepo.save(booking);
            createdBookingIds.add(saved.getId());
        }

        return ResponseEntity.ok(Map.of("createdBookingIds", createdBookingIds));
    }
}