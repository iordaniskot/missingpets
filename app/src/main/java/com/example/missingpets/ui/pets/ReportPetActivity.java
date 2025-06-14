package com.example.missingpets.ui.pets;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.missingpets.R;
import com.example.missingpets.data.api.ApiClient;
import com.example.missingpets.data.models.Location;
import com.example.missingpets.data.models.Pet;
import com.example.missingpets.data.models.PetResponse;
import com.example.missingpets.data.models.Report;
import com.example.missingpets.ui.maps.MapLocationPickerActivity;
import com.example.missingpets.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportPetActivity extends AppCompatActivity {
    private static final int MAP_LOCATION_REQUEST_CODE = 1001;
    
    private TextInputEditText etPetName, etPetBreed, etPetColor, etDescription;
    private TextInputEditText etPetHeight, etPetWeight;
    private TextInputEditText etLocation, etLastSeenDate, etLastSeenTime;
    private TextInputLayout tilLocation;
    private MaterialButton btnAddPhoto, btnSubmitReport;
    
    private PreferenceManager preferenceManager;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    
    // Location data
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedAddress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_pet);
        
        preferenceManager = PreferenceManager.getInstance(this);
        selectedDateTime = Calendar.getInstance();
        
        initViews();
        setupToolbar();
        setupDateTimePickers();
        setupClickListeners();
        prefillUserInfo();
    }
      private void initViews() {
        etPetName = findViewById(R.id.etPetName);
        etPetBreed = findViewById(R.id.etPetBreed);
        etPetColor = findViewById(R.id.etPetColor);
        etPetHeight = findViewById(R.id.etPetHeight);
        etPetWeight = findViewById(R.id.etPetWeight);        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        tilLocation = findViewById(R.id.tilLocation);
        etLastSeenDate = findViewById(R.id.etLastSeenDate);
        etLastSeenTime = findViewById(R.id.etLastSeenTime);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnSubmitReport = findViewById(R.id.btnSubmitReport);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupDateTimePickers() {
        etLastSeenDate.setOnClickListener(v -> showDatePicker());
        etLastSeenTime.setOnClickListener(v -> showTimePicker());
        
        // Set current date/time as default
        etLastSeenDate.setText(dateFormat.format(new Date()));
        etLastSeenTime.setText(timeFormat.format(new Date()));
    }    private void setupClickListeners() {
        btnAddPhoto.setOnClickListener(v -> {
            Toast.makeText(this, "Photo upload feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnSubmitReport.setOnClickListener(v -> submitReport());
        
        // Location picker click listeners
        etLocation.setOnClickListener(v -> openLocationPicker());
        tilLocation.setEndIconOnClickListener(v -> openLocationPicker());
    }
      private void prefillUserInfo() {
        // User contact information is already stored from registration
        // No need to collect it again in the report form
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                etLastSeenDate.setText(dateFormat.format(selectedDateTime.getTime()));
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                etLastSeenTime.setText(timeFormat.format(selectedDateTime.getTime()));
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            false
        );
        timePickerDialog.show();
    }    private void submitReport() {
        if (!validateForm()) {
            Log.w("ReportPetActivity", "Form validation failed");
            return;
        }
        
        Log.d("ReportPetActivity", "Form validation passed, creating Pet object");
        
        // Create Pet object with only non-empty fields
        Pet pet = new Pet();
        
        // Only set fields that are not empty
        String name = etPetName.getText().toString().trim();
        String breed = etPetBreed.getText().toString().trim();
        String color = etPetColor.getText().toString().trim();
        
        if (!name.isEmpty()) {
            pet.setName(name);
        }
        if (!breed.isEmpty()) {
            pet.setBreed(breed);
        }
        if (!color.isEmpty()) {
            pet.setColor(color.toLowerCase());
        }
        
        // Always set the creator
        pet.setCreatedBy(preferenceManager.getUserId());        // Debug user information
        Log.d("ReportPetActivity", "User ID: " + preferenceManager.getUserId());
        Log.d("ReportPetActivity", "Auth Token available: " + (preferenceManager.getAuthToken() != null));
        
        // Additional debug - check if user is logged in
        Log.d("ReportPetActivity", "User is logged in: " + preferenceManager.isLoggedIn());
        if (preferenceManager.getAuthToken() != null) {
            Log.d("ReportPetActivity", "Raw token length: " + preferenceManager.getAuthToken().length());
            Log.d("ReportPetActivity", "Token starts with 'Bearer': " + preferenceManager.getAuthToken().startsWith("Bearer"));
        }
        
        // Check if user ID is null - this is a critical issue
        if (preferenceManager.getUserId() == null) {
            Log.e("ReportPetActivity", "USER ID IS NULL! User might not be properly logged in.");
            Toast.makeText(this, "Authentication error. Please log out and log back in.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Parse height and weight from form fields
        String heightStr = etPetHeight.getText().toString().trim();
        String weightStr = etPetWeight.getText().toString().trim();
        
        try {
            if (!heightStr.isEmpty()) {
                pet.setHeight(Double.parseDouble(heightStr));
            }
        } catch (NumberFormatException e) {
            etPetHeight.setError("Please enter a valid height");
            return;
        }
          try {
            if (!weightStr.isEmpty()) {
                pet.setWeight(Double.parseDouble(weightStr));
            }
        } catch (NumberFormatException e) {
            etPetWeight.setError("Please enter a valid weight");
            return;
        }
        
        // Note: Photos will be handled separately when image upload is implemented
        // pet.setPhotos(photoUrls);
          // Submit Pet first, then create Report
        String authToken = "Bearer " + preferenceManager.getAuthToken();
        
        // Debug logging before API call
        Log.d("ReportPetActivity", "Submitting Pet - Name: " + pet.getName() + 
              ", Breed: " + pet.getBreed() + 
              ", Color: " + pet.getColor() + 
              ", Height: " + pet.getHeight() + 
              ", Weight: " + pet.getWeight() + 
              ", CreatedBy: " + pet.getCreatedBy());        Log.d("ReportPetActivity", "Auth token: " + (authToken != null ? "Present (length=" + authToken.length() + ")" : "NULL"));
        Log.d("ReportPetActivity", "API Base URL: " + com.example.missingpets.utils.Constants.BASE_URL);
        Log.d("ReportPetActivity", "Making API call to create pet...");
          Call<PetResponse> call = ApiClient.getApiService().createPet(authToken, pet);
          call.enqueue(new Callback<PetResponse>() {
            @Override
            public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                if (response.isSuccessful() && response.body() != null) {                    PetResponse petResponse = response.body();
                    Pet createdPet = petResponse.getPet();
                    if (createdPet != null) {
                        Log.d("ReportPetActivity", "Pet created successfully with ID: " + createdPet.getId());
                        Log.d("ReportPetActivity", "Pet name: " + createdPet.getName());
                        Log.d("ReportPetActivity", "Pet owner: " + (createdPet.getOwner() != null ? createdPet.getOwner().getName() : "null"));
                        Log.d("ReportPetActivity", "Pet owner ID: " + createdPet.getOwnerId());
                        // Now create the Report with the Pet ID
                        createReport(createdPet.getId(), authToken);
                    } else {
                        Log.e("ReportPetActivity", "Pet creation response missing pet data");
                        Toast.makeText(ReportPetActivity.this, "Pet creation failed - missing pet data", Toast.LENGTH_LONG).show();
                    }
                } else {// Enhanced error logging
                    String errorMessage = "Failed to create pet record.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("ReportPetActivity", "Pet creation failed - Status: " + response.code() + 
                                  ", Error body: " + errorBody);
                              // Parse validation errors more intelligently
                            if (response.code() == 422 && errorBody.contains("Validation failed")) {
                                // Extract specific validation error messages
                                if (errorBody.contains("cannot be empty") || errorBody.contains("is required")) {
                                    errorMessage = "At least one pet detail is required. Please fill in name, breed, color, height, weight, or description.";
                                } else {
                                    errorMessage = "Please check the form fields and try again.";
                                }
                            } else if (errorBody.contains("message")) {
                                errorMessage += " Server says: " + errorBody;
                            } else {
                                errorMessage += " Status code: " + response.code();
                            }
                        } else {
                            errorMessage += " Status code: " + response.code();
                            Log.e("ReportPetActivity", "Pet creation failed - Status: " + response.code() + 
                                  ", No error body available");
                        }
                    } catch (Exception e) {
                        Log.e("ReportPetActivity", "Error parsing error response", e);
                        errorMessage += " Unable to parse error details.";
                    }
                    
                    // Log the Pet object being sent for debugging
                    Log.d("ReportPetActivity", "Pet object sent: " + 
                          "Name=" + pet.getName() + 
                          ", Breed=" + pet.getBreed() + 
                          ", Color=" + pet.getColor() + 
                          ", Height=" + pet.getHeight() + 
                          ", Weight=" + pet.getWeight() + 
                          ", CreatedBy=" + pet.getCreatedBy());
                    
                    Toast.makeText(ReportPetActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }            @Override
            public void onFailure(Call<PetResponse> call, Throwable t) {
                Log.e("ReportPetActivity", "Network error creating pet", t);
                Toast.makeText(ReportPetActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void openLocationPicker() {
        Intent intent = new Intent(this, MapLocationPickerActivity.class);
        startActivityForResult(intent, MAP_LOCATION_REQUEST_CODE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
          if (requestCode == MAP_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedLatitude = MapLocationPickerActivity.getLatitudeFromIntent(data);
            selectedLongitude = MapLocationPickerActivity.getLongitudeFromIntent(data);
            selectedAddress = MapLocationPickerActivity.getAddressFromIntent(data);
            
            // Update the location field with latitude and longitude coordinates
            String coordinates = String.format(Locale.getDefault(), "%.6f, %.6f", selectedLatitude, selectedLongitude);
            etLocation.setText(coordinates);
        }
    }    private void createReport(String petId, String authToken) {
        // Debug the inputs to this method
        Log.d("ReportPetActivity", "createReport called with petId: " + petId + ", authToken available: " + (authToken != null));
        Log.d("ReportPetActivity", "Current user ID for report: " + preferenceManager.getUserId());
        
        // Create request using HashMap to avoid object serialization issues
        Map<String, Object> request = new HashMap<>();
        request.put("pet", petId);
        request.put("reporter", preferenceManager.getUserId());
        request.put("status", "lost"); // This is a "missing pet" report
        
        // Only set description if it's not empty
        String description = etDescription.getText().toString().trim();
        if (!description.isEmpty()) {
            request.put("description", description);
        }
          // Create location in GeoJSON format using selected coordinates
        Map<String, Object> location = new HashMap<>();
        location.put("type", "Point");
        location.put("coordinates", new double[]{selectedLongitude, selectedLatitude});
        request.put("location", location);
        
        // Note: Photos will be handled separately when image upload is implemented
        // request.put("photos", photoUrls);
        
        // Submit Report to API
        Call<Report> reportCall = ApiClient.getApiService().createReport(authToken, request);
        reportCall.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(Call<Report> call, Response<Report> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ReportPetActivity.this, "Pet report submitted successfully!", Toast.LENGTH_LONG).show();
                    finish();                } else {
                    // Enhanced error logging for report creation
                    String errorMessage = "Failed to submit report.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("ReportPetActivity", "Report creation failed - Status: " + response.code() + 
                                  ", Error body: " + errorBody);
                            
                            if (errorBody.contains("message")) {
                                errorMessage += " Server says: " + errorBody;
                            } else {
                                errorMessage += " Status code: " + response.code();
                            }
                        } else {
                            errorMessage += " Status code: " + response.code();
                            Log.e("ReportPetActivity", "Report creation failed - Status: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e("ReportPetActivity", "Error parsing report error response", e);
                        errorMessage += " Unable to parse error details.";
                    }
                    
                    // Log the Report request object being sent for debugging
                    Log.d("ReportPetActivity", "Report request object sent: " + 
                          "Pet=" + request.get("pet") + 
                          ", Reporter=" + request.get("reporter") + 
                          ", Status=" + request.get("status") + 
                          ", Location=[" + selectedLongitude + "," + selectedLatitude + "]");
                    
                    Toast.makeText(ReportPetActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
              @Override
            public void onFailure(Call<Report> call, Throwable t) {
                Log.e("ReportPetActivity", "Network error creating report", t);
                Toast.makeText(ReportPetActivity.this, "Network error while creating report: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }    private boolean validateForm() {
        // Clear previous error messages
        etPetName.setError(null);
        etPetBreed.setError(null);
        etLocation.setError(null);
        
        boolean isValid = true;
        
        // Check if at least one pet detail field is filled
        String name = etPetName.getText().toString().trim();
        String breed = etPetBreed.getText().toString().trim();
        String color = etPetColor.getText().toString().trim();
        String heightStr = etPetHeight.getText().toString().trim();
        String weightStr = etPetWeight.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        boolean hasAtLeastOneField = !name.isEmpty() || !breed.isEmpty() || !color.isEmpty() || 
                                   !heightStr.isEmpty() || !weightStr.isEmpty() || !description.isEmpty();
        
        if (!hasAtLeastOneField) {
            Toast.makeText(this, "Please fill in at least one pet detail (name, breed, color, height, weight, or description)", Toast.LENGTH_LONG).show();
            isValid = false;
        }
        
        // Location is still required for creating a report
        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Last known location is required");
            isValid = false;
        }
        
        return isValid;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        // Show confirmation dialog if form has data
        super.onBackPressed();
    }
}
