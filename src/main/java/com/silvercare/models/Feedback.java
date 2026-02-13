package com.silvercare.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private CareService service;

    private int rating;
    private String comments;
    private LocalDateTime created_at;

    // --- Add these exact Setters to fix "no set.." errors ---
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setService(CareService service) { this.service = service; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComments(String comments) { this.comments = comments; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }

    // --- Getters ---
    public int getId() { return id; }
    public Customer getCustomer() { return customer; }
    public CareService getService() { return service; }
    public int getRating() { return rating; }
    public String getComments() { return comments; }
    public LocalDateTime getCreated_at() { return created_at; }
}