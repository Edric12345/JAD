package com.silvercare.restController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silvercare.models.*;
import com.silvercare.repositories.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
public class StripeController {

    private final ServiceRepository serviceRepo;
    private final BookingRepository bookingRepo;
    private final CustomerRepository customerRepo;
    private final PaymentRepository paymentRepo;

    private final String stripeSecretKey;
    private final String stripePublishableKey;
    private final String stripeWebhookSecret;
    private final String successUrl;
    private final String cancelUrl;

    private final ObjectMapper mapper = new ObjectMapper();

    public StripeController(ServiceRepository serviceRepo,
                            BookingRepository bookingRepo,
                            CustomerRepository customerRepo,
                            PaymentRepository paymentRepo,
                            @Value("${stripe.apiKey}") String apiKey,
                            @Value("${stripe.publishableKey}") String publishableKey,
                            @Value("${stripe.webhookSecret}") String webhookSecret,
                            @Value("${app.checkout.successUrl}") String successUrl,
                            @Value("${app.checkout.cancelUrl}") String cancelUrl) {
        this.serviceRepo = serviceRepo;
        this.bookingRepo = bookingRepo;
        this.customerRepo = customerRepo;
        this.paymentRepo = paymentRepo;
        this.stripeSecretKey = apiKey;
        this.stripePublishableKey = publishableKey;
        this.stripeWebhookSecret = webhookSecret;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    @GetMapping("/config")
    public ResponseEntity<?> getConfig() {
        return ResponseEntity.ok(Map.of("publishableKey", stripePublishableKey));
    }

    // Accepts: { serviceIds: [1,2], customerEmail: "..." }
    @PostMapping("/create-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody Map<String,Object> body) {
        List<Integer> serviceIds = (List<Integer>) body.get("serviceIds");
        String email = (String) body.getOrDefault("customerEmail", "");
        if (serviceIds == null || serviceIds.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","no services"));

        List<CareService> services = new ArrayList<>();
        for (Integer id : serviceIds) serviceRepo.findById(id).ifPresent(services::add);

        long totalInCents = Math.round(services.stream().mapToDouble(CareService::getPrice).sum() * 100);

        RestTemplate rest = new RestTemplate();
        String url = "https://api.stripe.com/v1/checkout/sessions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = stripeSecretKey + ":";
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8)));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("payment_method_types[]", "card");
        params.add("mode", "payment");
        params.add("success_url", successUrl + "?session_id={CHECKOUT_SESSION_ID}");
        params.add("cancel_url", cancelUrl);

        // metadata: store customer email and service IDs so webhook can create bookings
        params.add("metadata[customerEmail]", email);
        params.add("metadata[serviceIds]", serviceIds.toString());

        params.add("line_items[0][price_data][currency]", "sgd");
        params.add("line_items[0][price_data][product_data][name]", "SilverCare Services");
        params.add("line_items[0][price_data][unit_amount]", String.valueOf(totalInCents));
        params.add("line_items[0][quantity]", "1");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> resp = rest.postForEntity(url, request, Map.class);
        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null && resp.getBody().get("id") != null) {
            return ResponseEntity.ok(Map.of("id", resp.getBody().get("id")));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","stripe failed"));
    }

    // Stripe webhook: verifies signature and processes checkout.session.completed
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestHeader(value = "Stripe-Signature", required = false) String sigHeader,
                                               @RequestBody String payload) throws Exception {
        // Verify signature using HMAC-SHA256 per Stripe docs
        if (stripeWebhookSecret == null || stripeWebhookSecret.startsWith("whsec_replace")) {
            // Webhook secret not set - reject in production. For local testing you can bypass (but not recommended)
            System.out.println("Warning: Stripe webhook secret not set; skipping verification.");
        } else {
            if (!verifyStripeSignature(sigHeader, payload, stripeWebhookSecret)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            }
        }

        Map<String, Object> event = mapper.readValue(payload, Map.class);
        String type = (String) event.get("type");
        System.out.println("Stripe event type: " + type);

        if ("checkout.session.completed".equals(type)) {
            Map<String,Object> data = (Map<String,Object>) event.get("data");
            Map<String,Object> obj = (Map<String,Object>) data.get("object");
            Map<String,String> metadata = (Map<String,String>) obj.get("metadata");
            String customerEmail = metadata != null ? metadata.get("customerEmail") : null;
            String serviceIdsStr = metadata != null ? metadata.get("serviceIds") : null;

            List<Integer> serviceIds = new ArrayList<>();
            if (serviceIdsStr != null) {
                // serviceIds were stored as [1, 2]
                String cleaned = serviceIdsStr.replaceAll("[\\[\\]\\s]",""
                );
                if (!cleaned.isBlank()) {
                    for (String s : cleaned.split(",")) {
                        try { serviceIds.add(Integer.parseInt(s)); } catch (Exception ignored) {}
                    }
                }
            }

            if (customerEmail == null || customerEmail.isBlank()) {
                System.out.println("No customerEmail in metadata - skipping booking creation");
            } else {
                Customer customer = customerRepo.findByEmail(customerEmail).orElse(null);
                if (customer == null) {
                    customer = new Customer();
                    customer.setEmail(customerEmail);
                    customer.setName("Stripe Customer");
                    customerRepo.save(customer);
                }

                // Create bookings and payment record (one per service)
                for (Integer sid : serviceIds) {
                    CareService service = serviceRepo.findById(sid).orElse(null);
                    if (service == null) continue;

                    Booking booking = new Booking();
                    booking.setCustomer(customer);
                    booking.setStatus(com.silvercare.models.BookingStatus.PAID);
                    booking.setBooking_date(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());

                    BookingDetails detail = new BookingDetails();
                    detail.setService(service);
                    detail.setBooking(booking);
                    detail.setPriceAtBooking(service.getPrice());
                    detail.setSubtotal(service.getPrice());

                    booking.setDetails(List.of(detail));
                    bookingRepo.save(booking);

                    Payment payment = new Payment();
                    payment.setCustomer(customer);
                    payment.setMethod("Stripe");
                    payment.setAmount(service.getPrice());
                    payment.setPaidAt(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                    payment.setTransactionRef((String) obj.get("payment_intent"));
                    paymentRepo.save(payment);
                }
            }
        }

        return ResponseEntity.ok("received");
    }

    // Add: helper endpoint to simulate webhook processing locally (no signature required)
    @PostMapping("/simulate-session")
    public ResponseEntity<?> simulateSession(@RequestBody Map<String,Object> payload) {
        String customerEmail = (String) payload.get("customerEmail");
        Object svcObj = payload.get("serviceIds");
        List<Integer> serviceIds = new ArrayList<>();
        if (svcObj instanceof List) {
            for (Object o : (List<?>) svcObj) {
                if (o instanceof Number) serviceIds.add(((Number) o).intValue());
                else if (o instanceof String) try { serviceIds.add(Integer.parseInt((String)o)); } catch(Exception ignored){}
            }
        }
        if (customerEmail == null || customerEmail.isBlank() || serviceIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error","missing customerEmail or serviceIds"));
        }

        Customer customer = customerRepo.findByEmail(customerEmail).orElse(null);
        if (customer == null) {
            customer = new Customer();
            customer.setEmail(customerEmail);
            customer.setName("Simulated Customer");
            customerRepo.save(customer);
        }

        List<Integer> createdBookingIds = new ArrayList<>();
        for (Integer sid : serviceIds) {
            CareService service = serviceRepo.findById(sid).orElse(null);
            if (service == null) continue;
            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setStatus(com.silvercare.models.BookingStatus.PAID);
            booking.setBooking_date(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());

            BookingDetails detail = new BookingDetails();
            detail.setService(service);
            detail.setBooking(booking);
            detail.setPriceAtBooking(service.getPrice());
            detail.setSubtotal(service.getPrice());

            booking.setDetails(List.of(detail));
            Booking saved = bookingRepo.save(booking);
            createdBookingIds.add(saved.getId());

            Payment payment = new Payment();
            payment.setCustomer(customer);
            payment.setMethod("Simulated");
            payment.setAmount(service.getPrice());
            payment.setPaidAt(Instant.now().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            payment.setTransactionRef("SIM-" + UUID.randomUUID().toString());
            paymentRepo.save(payment);
        }

        return ResponseEntity.ok(Map.of("createdBookingIds", createdBookingIds));
    }

    private boolean verifyStripeSignature(String sigHeader, String payload, String secret) throws Exception {
        if (sigHeader == null) return false;

        // stripe sends header like: t=timestamp,v1=signature
        String[] parts = sigHeader.split(",");
        String timestamp = null;
        String v1 = null;
        for (String p : parts) {
            String[] kv = p.split("=");
            if (kv.length == 2) {
                if (kv[0].equals("t")) timestamp = kv[1];
                if (kv[0].equals("v1")) v1 = kv[1];
            }
        }
        if (timestamp == null || v1 == null) return false;

        String signedPayload = timestamp + "." + payload;
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] mac_data = sha256_HMAC.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
        String computed = bytesToHex(mac_data);

        // Compare signatures (Stripe can send multiple v1 signatures in header; we just compare the single v1 extracted)
        return constantTimeEquals(computed, v1);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    // Constant-time compare to avoid timing attacks
    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) result |= a.charAt(i) ^ b.charAt(i);
        return result == 0;
    }
}
