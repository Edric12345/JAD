package com.silvercare.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "caregiver_id")
    private Caregiver caregiver;


    // Requirement: Use session/cookie to manage customer booking
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Requirement: Capture booking date and real-time status
    private LocalDateTime booking_date;
    private BookingStatus status;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BookingDetails> details;

    // Optional notes for customer or admin (stored with booking)
    private String notes;

    // Setters - Required for your Controller logic
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setBooking_date(LocalDateTime booking_date) { this.booking_date = booking_date; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public void setDetails(List<BookingDetails> details) { this.details = details; }
    public void setNotes(String notes) { this.notes = notes; }

    // Getters
    public int getId() { return id; }
    public Customer getCustomer() { return customer; }
    public LocalDateTime getBooking_date() { return booking_date; }
    public BookingStatus getStatus() { return status; }
    public List<BookingDetails> getDetails() { return details; }
    public String getNotes() { return notes; }
    // Helper for JSPs: formatted booking date (dd MMM yyyy HH:mm)
    public String getFormattedBookingDate() {
        if (this.booking_date == null) return "";
        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            return this.booking_date.format(fmt);
        } catch (Exception ex) {
            return this.booking_date.toString();
        }
    }
    
    public Caregiver getCaregiver() {
        return caregiver;
    }

    public void setCaregiver(Caregiver caregiver) {
        this.caregiver = caregiver;
    }


    public void addDetail(BookingDetails detail) {
        if (this.details == null) {
            this.details = new java.util.ArrayList<>();
        }
        this.details.add(detail);
        detail.setBooking(this); // Maintains the bidirectional link

    }
}