package com.silvercare.services;

import com.silvercare.models.*;
import com.silvercare.repositories.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private ServiceRepository serviceRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private CustomerCartRepository customerCartRepo;

    @Autowired
    private com.silvercare.repositories.CustomerRepository customerRepo;
    
    @Autowired
    private CaregiverRepository caregiverRepo;

    // self-injection to obtain proxy so @Transactional(propagation = REQUIRES_NEW) works on internal method calls
    @Autowired
    private BookingService self;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BookingResult createBookingsForCustomerPaid(int customerId, Customer customer, List<Integer> serviceIds, String paymentMethod) {
        BookingResult result = new BookingResult();
        if (serviceIds == null || serviceIds.isEmpty()) {
            logger.warn("No service IDs provided for paid booking for customer {}", customerId);
            return result;
        }

        for (Integer sid : serviceIds) {
            try {
                // call via proxy to apply REQUIRES_NEW for paid item
                BookingResult single = self.createSinglePaidBooking(customerId, customer, sid, paymentMethod);
                if (!single.getCreatedBookingIds().isEmpty()) {
                    result.getCreatedBookingIds().addAll(single.getCreatedBookingIds());
                    result.getSucceededServiceIds().addAll(single.getSucceededServiceIds());
                }
            } catch (Exception ex) {
                logger.error("Failed to create booking/payment for customer {} service {}: {}", customerId, sid, ex.getMessage(), ex);
                // continue with next item
            }
        }

        // Controller will manage cart persistence; do not delete persisted cart here.

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookingResult createSinglePaidBooking(int customerId,
            Customer customer,
            Integer serviceId,
            String paymentMethod) {

        BookingResult r = new BookingResult();
        CareService service = serviceRepo.findById(serviceId).orElse(null);
        if (service == null) {
            logger.warn("Service id {} not found, skipping", serviceId);
            return r;
        }
        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setStatus(com.silvercare.models.BookingStatus.PAID);
        booking.setBooking_date(LocalDateTime.now());

        BookingDetails detail = new BookingDetails();
        detail.setService(service);
        detail.setBooking(booking);
        detail.setPriceAtBooking(service.getPrice());
        detail.setSubtotal(service.getPrice());

        booking.setDetails(List.of(detail));
        Booking saved = bookingRepo.save(booking);
        r.addCreatedBookingId(saved.getId());
        r.addSucceededServiceId(serviceId);

        // Prepare payment data
        BigDecimal excl = BigDecimal.valueOf(service.getPrice());
        BigDecimal gst = excl.multiply(BigDecimal.valueOf(0.09)).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal total = excl.add(gst).setScale(2, BigDecimal.ROUND_HALF_UP);

        // Save payment in the same REQUIRES_NEW transaction so the booking and its payment are atomic for this item.
        Payment payment = new Payment();
        payment.setBookingId(saved.getId());
        payment.setCustomer(customer);
        payment.setAmountExclGst(excl);
        payment.setGstAmount(gst);
        payment.setTotalAmount(total);

        String pm = "PayNow";
        if (paymentMethod != null) {
            String v = paymentMethod.toLowerCase();
            if (v.contains("card") || v.contains("credit") || v.equals("online")) pm = "Credit Card";
            else if (v.contains("paypal")) pm = "PayPal";
            else if (v.contains("paynow")) pm = "PayNow";
            else if (v.contains("cash")) pm = "Cash";
            else pm = "PayNow";
        }
        payment.setPaymentMethod(pm);
        payment.setPaymentStatus("Paid");

        payment.setAmount(service.getPrice());
        payment.setMethod(paymentMethod != null ? paymentMethod : "online");
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionRef(UUID.randomUUID().toString());

        // If this save fails, let the exception propagate so this REQUIRES_NEW transaction rolls back (booking & payment not persisted).
        paymentRepo.save(payment);

        return r;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createPaymentForBooking(int bookingId, int customerId, BigDecimal excl, BigDecimal gst, BigDecimal total, String paymentMethod, double legacyAmount) {
        try {
            Payment payment = new Payment();
            payment.setBookingId(bookingId);
            // set lightweight booking relation null (we only set bookingId column)
            payment.setCustomer(customerRepo.findById(customerId).orElse(null));
            payment.setAmountExclGst(excl);
            payment.setGstAmount(gst);
            payment.setTotalAmount(total);

            String pm = "PayNow";
            if (paymentMethod != null) {
                String v = paymentMethod.toLowerCase();
                if (v.contains("card") || v.contains("credit") || v.equals("online")) pm = "Credit Card";
                else if (v.contains("paypal")) pm = "PayPal";
                else if (v.contains("paynow")) pm = "PayNow";
                else if (v.contains("cash")) pm = "Cash";
                else pm = "PayNow";
            }
            payment.setPaymentMethod(pm);
            payment.setPaymentStatus("Paid");

            payment.setAmount(legacyAmount);
            payment.setMethod(paymentMethod != null ? paymentMethod : "online");
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionRef(UUID.randomUUID().toString());

            paymentRepo.save(payment);
            return true;
        } catch (Exception ex) {
            logger.error("createPaymentForBooking failed for bookingId={} customerId={}: {}", bookingId, customerId, ex.getMessage(), ex);
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteBookingById(int bookingId) {
        bookingRepo.deleteById(bookingId);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BookingResult createBookingsForCustomerPending(int customerId, Customer customer, List<Integer> serviceIds) {
        BookingResult result = new BookingResult();
        if (serviceIds == null || serviceIds.isEmpty()) {
            logger.warn("No service IDs provided for pending booking for customer {}", customerId);
            return result;
        }

        for (Integer sid : serviceIds) {
            try {
                // delegate to a per-item REQUIRES_NEW method via proxy to isolate failures
                BookingResult single = self.createSinglePendingBooking(customerId, customer, sid);
                if (!single.getCreatedBookingIds().isEmpty()) {
                    result.getCreatedBookingIds().addAll(single.getCreatedBookingIds());
                    result.getSucceededServiceIds().addAll(single.getSucceededServiceIds());
                }
            } catch (Exception ex) {
                logger.error("Failed to create pending booking for customer {} service {}: {}", customerId, sid, ex.getMessage(), ex);
            }
        }

        // Controller will manage cart persistence; do not delete persisted cart here.

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookingResult createSinglePendingBooking(int customerId, Customer customer, Integer serviceId) {
        BookingResult r = new BookingResult();
        CareService service = serviceRepo.findById(serviceId).orElse(null);
        if (service == null) {
            logger.warn("Service id {} not found, skipping", serviceId);
            return r;
        }

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setStatus(com.silvercare.models.BookingStatus.PENDING);
        booking.setBooking_date(LocalDateTime.now());

        BookingDetails detail = new BookingDetails();
        detail.setService(service);
        detail.setBooking(booking);
        detail.setPriceAtBooking(service.getPrice());
        detail.setSubtotal(service.getPrice());

        booking.setDetails(List.of(detail));
        Booking saved = bookingRepo.save(booking);
        r.addCreatedBookingId(saved.getId());
        r.addSucceededServiceId(serviceId);
        return r;
    }
}