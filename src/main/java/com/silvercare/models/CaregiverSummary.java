package com.silvercare.models;

public class CaregiverSummary {
    private Integer id;
    private String name;
    private String qualifications;
    private String imagePath;
    private String availabilityStatus;

    public CaregiverSummary() {}

    public CaregiverSummary(Integer id, String name, String qualifications, String imagePath, String availabilityStatus) {
        this.id = id;
        this.name = name;
        this.qualifications = qualifications;
        this.imagePath = imagePath;
        this.availabilityStatus = availabilityStatus;
    }

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
}
