package com.silvercare.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String body;

    private String image_path;

    private LocalDateTime start_date;
    private LocalDateTime end_date;

    private String target; // e.g., header, homepage, booking-page

    private Integer priority = 0;

    private Boolean is_published = false;

    // Getters & setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getImage_path() { return image_path; }
    public void setImage_path(String image_path) { this.image_path = image_path; }

    public LocalDateTime getStart_date() { return start_date; }
    public void setStart_date(LocalDateTime start_date) { this.start_date = start_date; }

    public LocalDateTime getEnd_date() { return end_date; }
    public void setEnd_date(LocalDateTime end_date) { this.end_date = end_date; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Boolean getIs_published() { return is_published; }
    public void setIs_published(Boolean is_published) { this.is_published = is_published; }
}
