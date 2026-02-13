package com.silvercare.models;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "caregiver_bookings")
public class CaregiverBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String customerName;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Track caregiver check-in/check-out timestamps
    private LocalDateTime checkedInAt;
    private LocalDateTime checkedOutAt;

    // Snapshot fields: some deployments store caregiver snapshot data in caregiver_bookings
    // (name, qualifications, imagePath, availabilityStatus). Add mappings to support that.
    private String name;
    private String qualifications;
    private String imagePath;
    private String availabilityStatus;

    @ManyToOne
    @JoinColumn(name = "caregiver_id", nullable = true)
    private Caregiver caregiver;

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Caregiver getCaregiver() { return caregiver; }
    public void setCaregiver(Caregiver caregiver) { this.caregiver = caregiver; }

    // Snapshot getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }

    public LocalDateTime getCheckedOutAt() { return checkedOutAt; }
    public void setCheckedOutAt(LocalDateTime checkedOutAt) { this.checkedOutAt = checkedOutAt; }
}