package com.example.missingpets.data.models;

import java.io.Serializable;

public class Location implements Serializable {
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    public Location() {
        // Default constructor for JSON parsing
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(double latitude, double longitude, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    // Getters and Setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }    // Helper methods
    public String getFormattedAddress() {
        if (address != null && !address.isEmpty()) {
            return address;
        }
        
        StringBuilder formattedAddress = new StringBuilder();
        if (city != null) formattedAddress.append(city);
        if (state != null) {
            if (formattedAddress.length() > 0) formattedAddress.append(", ");
            formattedAddress.append(state);
        }
        if (country != null) {
            if (formattedAddress.length() > 0) formattedAddress.append(", ");
            formattedAddress.append(country);
        }
        
        return formattedAddress.length() > 0 ? formattedAddress.toString() : "Unknown location";
    }

    // Convert to GeoJSON format for API compatibility
    public Report.ReportLocation toReportLocation() {
        return new Report.ReportLocation(longitude, latitude);
    }

    // Create from GeoJSON format
    public static Location fromReportLocation(Report.ReportLocation reportLocation) {
        if (reportLocation == null || reportLocation.getCoordinates() == null) {
            return null;
        }
        return new Location(reportLocation.getLatitude(), reportLocation.getLongitude());
    }

    public double distanceTo(Location other) {
        if (other == null) return Double.MAX_VALUE;
        
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in kilometers
    }

    @Override
    public String toString() {
        return getFormattedAddress();
    }
}
