package com.silvercare.models;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "caregivers")
public class Caregiver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String qualifications;

    private String imagePath;

    private String availabilityStatus;

    // Bidirectional mappings to Booking and CaregiverBooking (optional but helpful)
    @OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "caregiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CaregiverBooking> caregiverBookings = new ArrayList<>();

    // Default constructor required by JPA
    public Caregiver() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQualifications() { return qualifications; }
    public void setQualifications(String qualifications) { this.qualifications = qualifications; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public List<Booking> getBookings() { return bookings; }
    public void setBookings(List<Booking> bookings) { this.bookings = bookings; }

    public List<CaregiverBooking> getCaregiverBookings() { return caregiverBookings; }
    public void setCaregiverBookings(List<CaregiverBooking> caregiverBookings) { this.caregiverBookings = caregiverBookings; }

    // Helper to maintain bidirectional relationship
    public void addBooking(Booking booking) {
        if (booking == null) return;
        this.bookings.add(booking);
        booking.setCaregiver(this);
    }

    public void addCaregiverBooking(CaregiverBooking cb) {
        if (cb == null) return;
        this.caregiverBookings.add(cb);
        cb.setCaregiver(this);
    }
}