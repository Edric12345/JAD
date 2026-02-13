package com.silvercare.models;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String address;

    // New: optional health / care preferences fields for profile
    private String carePreferences;     // e.g., "No baths after 10pm", "Diet: diabetic"
    private String medicalNotes;        // free-text medical notes or conditions
    private String emergencyContact;    // emergency contact phone/name

    // --- GETTERS AND SETTERS (These fix the "undefined" errors) ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    // --- New getters/setters for health/care preferences ---
    public String getCarePreferences() { return carePreferences; }
    public void setCarePreferences(String carePreferences) { this.carePreferences = carePreferences; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
}