package com.example.missingpets.data.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Custom Gson deserializer for Pet objects to handle createdBy field
 * that can be either a string ID or a User object
 */
public class PetDeserializer implements JsonDeserializer<Pet> {
    
    @Override
    public Pet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Pet pet = new Pet();
        
        // Deserialize simple fields
        if (jsonObject.has("_id") && !jsonObject.get("_id").isJsonNull()) {
            pet.setId(jsonObject.get("_id").getAsString());
        }
        
        if (jsonObject.has("name") && !jsonObject.get("name").isJsonNull()) {
            pet.setName(jsonObject.get("name").getAsString());
        }
        
        if (jsonObject.has("breed") && !jsonObject.get("breed").isJsonNull()) {
            pet.setBreed(jsonObject.get("breed").getAsString());
        }
        
        if (jsonObject.has("color") && !jsonObject.get("color").isJsonNull()) {
            pet.setColor(jsonObject.get("color").getAsString());
        }
        
        if (jsonObject.has("height") && !jsonObject.get("height").isJsonNull()) {
            pet.setHeight(jsonObject.get("height").getAsDouble());
        }
        
        if (jsonObject.has("weight") && !jsonObject.get("weight").isJsonNull()) {
            pet.setWeight(jsonObject.get("weight").getAsDouble());
        }
        
        if (jsonObject.has("isOwnedByCreator") && !jsonObject.get("isOwnedByCreator").isJsonNull()) {
            pet.setOwnedByCreator(jsonObject.get("isOwnedByCreator").getAsBoolean());
        }
        
        // Handle photos array
        if (jsonObject.has("photos") && !jsonObject.get("photos").isJsonNull()) {
            List<String> photos = new ArrayList<>();
            jsonObject.getAsJsonArray("photos").forEach(element -> 
                photos.add(element.getAsString()));
            pet.setPhotos(photos);
        }
        
        // Handle dates
        if (jsonObject.has("createdAt") && !jsonObject.get("createdAt").isJsonNull()) {
            pet.setCreatedAt(context.deserialize(jsonObject.get("createdAt"), Date.class));
        }
        
        if (jsonObject.has("updatedAt") && !jsonObject.get("updatedAt").isJsonNull()) {
            pet.setUpdatedAt(context.deserialize(jsonObject.get("updatedAt"), Date.class));
        }
        
        // Handle owner (User object)
        if (jsonObject.has("owner") && !jsonObject.get("owner").isJsonNull()) {
            pet.setOwner(context.deserialize(jsonObject.get("owner"), User.class));
        }
        
        // Handle createdBy - can be either string ID or User object
        if (jsonObject.has("createdBy") && !jsonObject.get("createdBy").isJsonNull()) {
            JsonElement createdByElement = jsonObject.get("createdBy");
            
            if (createdByElement.isJsonPrimitive()) {
                // It's a string ID
                String createdById = createdByElement.getAsString();
                User creator = new User();
                creator.setId(createdById);
                pet.setCreatedByUser(creator);
            } else if (createdByElement.isJsonObject()) {
                // It's a full User object
                User creator = context.deserialize(createdByElement, User.class);
                pet.setCreatedByUser(creator);
            }
        }
        
        return pet;
    }
}
