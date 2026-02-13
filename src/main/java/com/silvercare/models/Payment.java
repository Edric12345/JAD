package com.silvercare.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "booking_id")
    private Integer bookingId; // kept for compatibility; actual relation below

    @Column(name = "amount_excl_gst", nullable = false)
    private BigDecimal amountExclGst;

    @Column(name = "gst_amount")
    private BigDecimal gstAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // legacy / existing fields
    @Column(nullable = false)
    private double amount;

    private String method;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "transaction_ref")
    private String transactionRef;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;

    // Getters and setters
    public int getId() { return id; }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public BigDecimal getAmountExclGst() { return amountExclGst; }
    public void setAmountExclGst(BigDecimal amountExclGst) { this.amountExclGst = amountExclGst; }

    public BigDecimal getGstAmount() { return gstAmount; }
    public void setGstAmount(BigDecimal gstAmount) { this.gstAmount = gstAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    // Backward-compatible legacy accessors (snake_case) so pre-existing code continues to compile
    public LocalDateTime getPaid_at() { return getPaidAt(); }
    public void setPaid_at(LocalDateTime paid_at) { setPaidAt(paid_at); }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; if (booking!=null) this.bookingId = booking.getId(); }
}