package com.silvercare.models;

import jakarta.persistence.*;

@Entity
@Table(name = "booking_details")
public class BookingDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private CareService service; // This links to your Service name

    private int quantity;
    private double subtotal;
    @Column(name = "price_at_booking")
    private double priceAtBooking;
    
 // Add these to your existing BookingDetails.java
    public void setBooking(Booking booking) { 
        this.booking = booking; 
    }

    public void setService(CareService service) { 
        this.service = service; 
    }

    public void setPriceAtBooking(double price) { 
        this.priceAtBooking = price; 
    }
    
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    // Getters
    public CareService getService() { return service; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return subtotal; }
    public double getPriceAtBooking() { return priceAtBooking; }
}