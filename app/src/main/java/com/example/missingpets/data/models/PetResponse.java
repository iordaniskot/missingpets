package com.example.missingpets.data.models;

public class PetResponse {
    private String message;
    private Pet pet;

    // Constructors
    public PetResponse() {}

    public PetResponse(String message, Pet pet) {
        this.message = message;
        this.pet = pet;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }
}
