package com.silvercare.models;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_carts")
public class CustomerCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "customer_id", unique = true)
    private Customer customer;

    @Lob
    private String cart_data; // comma-separated service IDs, e.g. "1,2,3"

    public int getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getCart_data() { return cart_data; }
    public void setCart_data(String cart_data) { this.cart_data = cart_data; }
}
