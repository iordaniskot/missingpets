package com.example.missingpets.data.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Pet implements Serializable {
    @SerializedName("_id")
    private String id;
    private User owner; // User object, optional
    private User createdBy; // Can be User object when populated or String when just ID
    private String name; // optional
    private String breed; // optional
    private Double height; // optional, in cm
    private Double weight; // optional, in kg
    private String color; // optional, lowercase
    private List<String> photos; // array of CDN URLs
    private boolean isOwnedByCreator; // default: false
    private Date createdAt;
    private Date updatedAt;public Pet() {
        // Default constructor for JSON parsing
        this.isOwnedByCreator = false; // default value
    }

    public Pet(String createdById, String name, String breed, Double height, Double weight, 
               String color, List<String> photos) {
        // Create a User object with just the ID when constructing with String
        this.createdBy = new User();
        this.createdBy.setId(createdById);
        this.name = name;
        this.breed = breed;
        this.height = height;
        this.weight = weight;
        this.color = color != null ? color.toLowerCase() : null;
        this.photos = photos;
        this.isOwnedByCreator = false;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public User getCreatedByUser() { return createdBy; }
    public void setCreatedByUser(User createdBy) { this.createdBy = createdBy; }
    
    // Backwards compatibility methods for String ID
    public String getCreatedBy() { 
        return createdBy != null ? createdBy.getId() : null; 
    }
    
    public void setCreatedBy(String createdById) { 
        if (createdById != null) {
            this.createdBy = new User();
            this.createdBy.setId(createdById);
        } else {
            this.createdBy = null;
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color != null ? color.toLowerCase() : null; }

    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos; }

    public boolean isOwnedByCreator() { return isOwnedByCreator; }
    public void setOwnedByCreator(boolean ownedByCreator) { isOwnedByCreator = ownedByCreator; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }    // Helper methods
    public String getOwnerId() {
        return (owner != null) ? owner.getId() : null;
    }
    
    public String getDisplayImageUrl() {
        return (photos != null && !photos.isEmpty()) ? photos.get(0) : null;
    }

    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        if (breed != null && !breed.trim().isEmpty()) {
            return breed.trim();
        }
        return "Unknown Pet";
    }

    // Validation helper - at least one of name, breed, or photos must be provided
    public boolean isValid() {
        return (name != null && !name.trim().isEmpty()) ||
               (breed != null && !breed.trim().isEmpty()) ||
               (photos != null && !photos.isEmpty());
    }
}
