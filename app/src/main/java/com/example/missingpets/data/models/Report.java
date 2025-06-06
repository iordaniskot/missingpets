package com.example.missingpets.data.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Report implements Serializable {
    @SerializedName("_id")
    private String id;
    private Pet pet; // Can be populated Pet object from API
    private User reporter; // Can be populated User object from API
    private String status; // enum: 'lost', 'found', required
    private String description; // optional
    private List<String> photos; // array of CDN URLs
    private ReportLocation location; // GeoJSON Point format
    private Date createdAt;
    private Date updatedAt;

    // Inner class for GeoJSON Point location
    public static class ReportLocation implements Serializable {
        private String type; // must be 'Point'
        private double[] coordinates; // [longitude, latitude]

        public ReportLocation() {
            this.type = "Point";
        }

        public ReportLocation(double longitude, double latitude) {
            this.type = "Point";
            this.coordinates = new double[]{longitude, latitude};
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public double[] getCoordinates() { return coordinates; }
        public void setCoordinates(double[] coordinates) { this.coordinates = coordinates; }

        public double getLongitude() {
            return coordinates != null && coordinates.length >= 2 ? coordinates[0] : 0;
        }

        public double getLatitude() {
            return coordinates != null && coordinates.length >= 2 ? coordinates[1] : 0;
        }

        public void setLongitude(double longitude) {
            if (coordinates == null || coordinates.length < 2) {
                coordinates = new double[2];
            }
            coordinates[0] = longitude;
        }        public void setLatitude(double latitude) {
            if (coordinates == null || coordinates.length < 2) {
                coordinates = new double[2];
            }
            coordinates[1] = latitude;
        }
    }

    public Report() {
        // Default constructor for JSON parsing
    }

    public Report(String petId, String reporterId, String status, String description, 
                  List<String> photos, double longitude, double latitude) {
        // For creating reports with just IDs (when creating new reports)
        this.pet = new Pet();
        this.pet.setId(petId);
        this.reporter = new User();
        this.reporter.setId(reporterId);
        this.status = status;
        this.description = description;
        this.photos = photos;
        this.location = new ReportLocation(longitude, latitude);
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    
    // Overloaded setter for backwards compatibility - accepts String ID
    public void setPet(String petId) { 
        if (petId != null) {
            this.pet = new Pet();
            this.pet.setId(petId);
        } else {
            this.pet = null;
        }
    }
    
    // Helper method to get pet ID (for backwards compatibility)
    public String getPetId() { 
        return pet != null ? pet.getId() : null; 
    }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }
    
    // Overloaded setter for backwards compatibility - accepts String ID
    public void setReporter(String reporterId) { 
        if (reporterId != null) {
            this.reporter = new User();
            this.reporter.setId(reporterId);
        } else {
            this.reporter = null;
        }
    }
    
    // Helper method to get reporter ID (for backwards compatibility)
    public String getReporterId() { 
        return reporter != null ? reporter.getId() : null; 
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    public ReportLocation getLocation() { return location; }
    public void setLocation(ReportLocation location) { this.location = location; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isLost() {
        return "lost".equalsIgnoreCase(status);
    }

    public boolean isFound() {
        return "found".equalsIgnoreCase(status);
    }

    public String getDisplayImageUrl() {
        return (photos != null && !photos.isEmpty()) ? photos.get(0) : null;
    }    // Status enum constants
    public static final String STATUS_LOST = "lost";
    public static final String STATUS_FOUND = "found";

    // Validation helper
    public boolean isValid() {
        return pet != null && pet.getId() != null && !pet.getId().trim().isEmpty() &&
               reporter != null && reporter.getId() != null && !reporter.getId().trim().isEmpty() &&
               status != null && (STATUS_LOST.equals(status) || STATUS_FOUND.equals(status)) &&
               location != null && location.getCoordinates() != null && 
               location.getCoordinates().length == 2;
    }
}
