package com.example.missingpets.data.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ReportWithPet implements Serializable {
    @SerializedName("_id")
    private String id;
    private Pet pet; // Populated Pet object instead of just ID
    private User reporter; // Populated User object instead of just ID
    private String status; // enum: 'lost', 'found'
    private String description;
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
        }

        public void setLatitude(double latitude) {
            if (coordinates == null || coordinates.length < 2) {
                coordinates = new double[2];
            }
            coordinates[1] = latitude;
        }
    }

    public ReportWithPet() {
        // Default constructor for JSON parsing
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

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
    public String getDisplayTitle() {
        if (pet != null) {
            return pet.getDisplayName() + " - " + capitalizeFirst(status);
        }
        return "Pet Report #" + (id != null ? id.substring(0, Math.min(8, id.length())) : "Unknown");
    }

    public String getLocationString() {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            return String.format(java.util.Locale.getDefault(), "%.4f, %.4f", lat, lng);
        }
        return "Location unknown";
    }

    public String getReporterName() {
        if (reporter != null && reporter.getName() != null) {
            return reporter.getName();
        }
        return "Unknown Reporter";
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    // Convert from regular Report to ReportWithPet
    public static ReportWithPet fromReport(Report report) {
        ReportWithPet reportWithPet = new ReportWithPet();
        reportWithPet.setId(report.getId());
        reportWithPet.setStatus(report.getStatus());
        reportWithPet.setDescription(report.getDescription());
        reportWithPet.setPhotos(report.getPhotos());
        reportWithPet.setCreatedAt(report.getCreatedAt());
        reportWithPet.setUpdatedAt(report.getUpdatedAt());
        
        // Copy pet object - this was missing!
        if (report.getPet() != null) {
            reportWithPet.setPet(report.getPet());
        }
        
        // Copy reporter object - this was also missing!
        if (report.getReporter() != null) {
            reportWithPet.setReporter(report.getReporter());
        }
        
        // Convert location
        if (report.getLocation() != null) {
            ReportLocation newLocation = new ReportLocation();
            newLocation.setType(report.getLocation().getType());
            newLocation.setCoordinates(report.getLocation().getCoordinates());
            reportWithPet.setLocation(newLocation);
        }
        
        return reportWithPet;
    }
}
