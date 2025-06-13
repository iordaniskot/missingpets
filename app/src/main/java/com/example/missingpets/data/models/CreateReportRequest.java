package com.example.missingpets.data.models;

import java.io.Serializable;
import java.util.List;

/**
 * Request model for creating new reports.
 * Uses String IDs for pet and reporter instead of full objects.
 */
public class CreateReportRequest implements Serializable {
    private String pet; // Pet ID as string
    private String reporter; // Reporter ID as string
    private String status; // enum: 'lost', 'found', required
    private String description; // optional
    private List<String> photos; // array of CDN URLs
    private Report.ReportLocation location; // GeoJSON Point format

    public CreateReportRequest() {
        // Default constructor for JSON parsing
    }

    public CreateReportRequest(String petId, String reporterId, String status, String description, 
                              List<String> photos, double longitude, double latitude) {
        this.pet = petId;
        this.reporter = reporterId;
        this.status = status;
        this.description = description;
        this.photos = photos;
        this.location = new Report.ReportLocation(longitude, latitude);
    }

    // Getters and Setters
    public String getPet() { return pet; }
    public void setPet(String pet) { this.pet = pet; }

    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    public Report.ReportLocation getLocation() { return location; }
    public void setLocation(Report.ReportLocation location) { this.location = location; }

    // Constants for status values
    public static final String STATUS_LOST = "lost";
    public static final String STATUS_FOUND = "found";
}
