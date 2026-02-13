package com.silvercare.models;

import jakarta.persistence.*;

@Entity
@Table(name = "service")
public class CareService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String service_name;
    private String description;
    private double price;

    // --- Many-to-one relationship ---
    @ManyToOne
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private ServiceCategory category;

    // --- Raw category_id column for direct setting ---
    @Column(name = "category_id") // make sure this is mapped explicitly
    private int category_id;

    private String image_path;
    private Integer availability;

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getService_name() { return service_name; }
    public void setService_name(String service_name) { this.service_name = service_name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public ServiceCategory getCategory() { return category; }
    public void setCategory(ServiceCategory category) { this.category = category; }

    public int getCategory_id() { return category_id; }
    public void setCategory_id(int category_id) { this.category_id = category_id; }

    public String getImage_path() { return image_path; }
    public void setImage_path(String image_path) { this.image_path = image_path; }

    public Integer getAvailability() { return availability; }
    public void setAvailability(Integer availability) { this.availability = availability; }
}
