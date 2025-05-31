package com.example.missingpets.ui.pets;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.missingpets.R;
import com.example.missingpets.data.api.ApiClient;
import com.example.missingpets.data.models.Location;
import com.example.missingpets.data.models.Pet;
import com.example.missingpets.data.models.Report;
import com.example.missingpets.utils.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportPetActivity extends AppCompatActivity {
    private TextInputEditText etPetName, etPetBreed, etPetColor, etDescription;
    private TextInputEditText etPetHeight, etPetWeight;
    private TextInputEditText etLocation, etLastSeenDate, etLastSeenTime;
    private TextInputEditText etPhoneNumber, etContactInfo;
    private AutoCompleteTextView acPetType;
    private MaterialButton btnAddPhoto, btnSubmitReport;
    
    private PreferenceManager preferenceManager;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_pet);
        
        preferenceManager = PreferenceManager.getInstance(this);
        selectedDateTime = Calendar.getInstance();
        
        initViews();
        setupToolbar();
        setupDropdowns();
        setupDateTimePickers();
        setupClickListeners();
        prefillUserInfo();
    }
      private void initViews() {
        etPetName = findViewById(R.id.etPetName);
        etPetBreed = findViewById(R.id.etPetBreed);
        etPetColor = findViewById(R.id.etPetColor);
        etPetHeight = findViewById(R.id.etPetHeight);
        etPetWeight = findViewById(R.id.etPetWeight);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etLastSeenDate = findViewById(R.id.etLastSeenDate);
        etLastSeenTime = findViewById(R.id.etLastSeenTime);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etContactInfo = findViewById(R.id.etContactInfo);
        acPetType = findViewById(R.id.acPetType);
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
      private void setupDropdowns() {
        // Pet Type dropdown
        String[] petTypes = {"Dog", "Cat", "Bird", "Rabbit", "Hamster", "Guinea Pig", "Fish", "Reptile", "Other"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, petTypes);
        acPetType.setAdapter(typeAdapter);
        
        // Note: Size dropdown removed - now using height and weight fields
    }
    
    private void setupDateTimePickers() {
        etLastSeenDate.setOnClickListener(v -> showDatePicker());
        etLastSeenTime.setOnClickListener(v -> showTimePicker());
        
        // Set current date/time as default
        etLastSeenDate.setText(dateFormat.format(new Date()));
        etLastSeenTime.setText(timeFormat.format(new Date()));
    }
      private void setupClickListeners() {
        btnAddPhoto.setOnClickListener(v -> {
            Toast.makeText(this, "Photo upload feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnSubmitReport.setOnClickListener(v -> submitReport());
    }
    
    private void prefillUserInfo() {
        // Pre-fill phone number if available
        String userPhone = preferenceManager.getUserPhone();
        if (userPhone != null && !userPhone.isEmpty()) {
            etPhoneNumber.setText(userPhone);
        }
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
            return;
        }
        
        // Create Pet object with API-compliant fields only
        Pet pet = new Pet();
        pet.setName(etPetName.getText().toString().trim());
        pet.setBreed(etPetBreed.getText().toString().trim());
        pet.setColor(etPetColor.getText().toString().trim().toLowerCase());
        pet.setCreatedBy(preferenceManager.getUserId());
        
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
        Call<Pet> call = ApiClient.getApiService().createPet(authToken, pet);
          call.enqueue(new Callback<Pet>() {
            @Override
            public void onResponse(Call<Pet> call, Response<Pet> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pet createdPet = response.body();
                    // Now create the Report with the Pet ID
                    createReport(createdPet.getId(), authToken);
                } else {
                    Toast.makeText(ReportPetActivity.this, "Failed to create pet record. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }            @Override
            public void onFailure(Call<Pet> call, Throwable t) {
                Toast.makeText(ReportPetActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createReport(String petId, String authToken) {
        // Create Report object with status and location information
        Report report = new Report();
        report.setPet(petId);
        report.setReporter(preferenceManager.getUserId());
        report.setStatus(Report.STATUS_LOST); // This is a "missing pet" report
        report.setDescription(etDescription.getText().toString().trim());
        
        // Create location in GeoJSON format
        // TODO: Get actual coordinates from location picker or geocoding
        // For now, using placeholder coordinates
        Report.ReportLocation reportLocation = new Report.ReportLocation(0.0, 0.0);
        report.setLocation(reportLocation);
        
        // Note: Photos will be handled separately when image upload is implemented
        // report.setPhotos(photoUrls);
        
        // Submit Report to API
        Call<Report> reportCall = ApiClient.getApiService().createReport(authToken, report);
        reportCall.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(Call<Report> call, Response<Report> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ReportPetActivity.this, "Pet report submitted successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ReportPetActivity.this, "Failed to submit report. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Report> call, Throwable t) {
                Toast.makeText(ReportPetActivity.this, "Network error while creating report: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }    private boolean validateForm() {
        boolean isValid = true;
        
        if (etPetName.getText().toString().trim().isEmpty()) {
            etPetName.setError("Pet name is required");
            isValid = false;
        }
        
        // Note: Pet type validation removed since Pet model no longer has 'type' field
        // Type information is now captured in the form but not stored in Pet model
        
        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Last known location is required");
            isValid = false;
        }
        
        if (etPhoneNumber.getText().toString().trim().isEmpty()) {
            etPhoneNumber.setError("Phone number is required");
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
