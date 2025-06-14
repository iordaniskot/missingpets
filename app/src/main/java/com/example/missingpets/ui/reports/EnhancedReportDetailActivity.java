package com.example.missingpets.ui.reports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.missingpets.R;
import com.example.missingpets.data.models.ReportWithPet;
import com.example.missingpets.ui.pets.PetDetailActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EnhancedReportDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_REPORT = "extra_report";
    
    private ImageView ivReportImage;
    private TextView tvReportTitle;
    private Chip chipStatus;
    private TextView tvDescription;
    private TextView tvPetDetails;
    private TextView tvLocation;
    private TextView tvReportDate;
    private TextView tvReporterInfo;
    private FloatingActionButton fabCall;
    private FloatingActionButton fabMap;
    private MaterialButton btnFoundIt;
    
    private ReportWithPet report;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_report_detail);
        
        // Get report data from intent
        report = (ReportWithPet) getIntent().getSerializableExtra(EXTRA_REPORT);
        if (report == null) {
            Toast.makeText(this, "Error: Report data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupToolbar();
        initViews();
        displayReportData();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Report Details");
        }
    }
      private void initViews() {
        ivReportImage = findViewById(R.id.ivReportImage);
        tvReportTitle = findViewById(R.id.tvReportTitle);
        chipStatus = findViewById(R.id.chipStatus);
        tvDescription = findViewById(R.id.tvDescription);
        tvPetDetails = findViewById(R.id.tvPetDetails);
        tvLocation = findViewById(R.id.tvLocation);
        tvReportDate = findViewById(R.id.tvReportDate);
        tvReporterInfo = findViewById(R.id.tvReporterInfo);
        fabCall = findViewById(R.id.fabCall);
        fabMap = findViewById(R.id.fabMap);
        btnFoundIt = findViewById(R.id.btnFoundIt);
        
        // Initially hide call FAB, will be shown if phone number is available
        fabCall.setVisibility(View.GONE);
        
        // Set up Found It button click listener
        btnFoundIt.setOnClickListener(v -> onFoundItClicked());
    }
    
    private void displayReportData() {
        // Set report title using enhanced display title
        tvReportTitle.setText(report.getDisplayTitle());
        
        // Set status chip
        if ("lost".equals(report.getStatus())) {
            chipStatus.setText("MISSING");
            chipStatus.setChipBackgroundColorResource(R.color.error);
            chipStatus.setTextColor(getColor(R.color.white));
        } else if ("found".equals(report.getStatus())) {
            chipStatus.setText("FOUND");
            chipStatus.setChipBackgroundColorResource(R.color.success);
            chipStatus.setTextColor(getColor(R.color.white));
        } else {
            chipStatus.setText("UNKNOWN");
            chipStatus.setChipBackgroundColorResource(R.color.text_secondary);
            chipStatus.setTextColor(getColor(R.color.white));
        }
        
        // Set description
        if (report.getDescription() != null && !report.getDescription().isEmpty()) {
            tvDescription.setText(report.getDescription());
        } else {
            tvDescription.setText("No description provided");
        }
          // Set pet details
        if (report.getPet() != null) {
            StringBuilder petDetails = new StringBuilder();
            petDetails.append("Pet: ").append(report.getPet().getDisplayName()).append("\n");
            
            if (report.getPet().getBreed() != null && !report.getPet().getBreed().isEmpty()) {
                petDetails.append("Breed: ").append(report.getPet().getBreed()).append("\n");
            }
            
            if (report.getPet().getColor() != null && !report.getPet().getColor().isEmpty()) {
                petDetails.append("Color: ").append(capitalizeFirst(report.getPet().getColor())).append("\n");
            }
            
            if (report.getPet().getWeight() != null && report.getPet().getWeight() > 0) {
                petDetails.append("Weight: ").append(report.getPet().getWeight()).append(" kg\n");
            }
            
            if (report.getPet().getHeight() != null && report.getPet().getHeight() > 0) {
                petDetails.append("Height: ").append(report.getPet().getHeight()).append(" cm\n");
            }
            
            tvPetDetails.setText(petDetails.toString().trim());
        } else {
            tvPetDetails.setText("Pet details not available");
        }
        
        // Set location
        tvLocation.setText(report.getLocationString());
        
        // Set report date
        if (report.getCreatedAt() != null) {
            tvReportDate.setText("Reported on: " + dateFormat.format(report.getCreatedAt()));
        } else {
            tvReportDate.setText("Report date unknown");
        }
        
        // Set reporter info with contact details
        StringBuilder reporterInfo = new StringBuilder();
        reporterInfo.append("Reporter: ").append(report.getReporterName()).append("\n");
        
        if (report.getReporter() != null) {
            if (report.getReporter().getEmail() != null && !report.getReporter().getEmail().isEmpty()) {
                reporterInfo.append("Email: ").append(report.getReporter().getEmail()).append("\n");
            }
            
            if (report.getReporter().getPhone() != null && !report.getReporter().getPhone().isEmpty()) {
                reporterInfo.append("Phone: ").append(report.getReporter().getPhone());
            }
        }
        
        tvReporterInfo.setText(reporterInfo.toString().trim());
        
        // Set pet image
        setPetImage();
        
        // Set up FAB click listeners
        setupFabListeners();
    }
    
    private void setPetImage() {
        if (report.getPet() != null && report.getPet().getBreed() != null) {
            // Use breed-based image if pet information is available
            String breed = report.getPet().getBreed().toLowerCase();
            if (breed.contains("dog") || breed.contains("retriever") || 
                breed.contains("terrier") || breed.contains("bulldog") ||
                breed.contains("poodle") || breed.contains("shepherd")) {
                ivReportImage.setImageResource(R.drawable.ic_pets_dog);
            } else if (breed.contains("cat") || breed.contains("persian") || 
                       breed.contains("siamese") || breed.contains("maine") ||
                       breed.contains("bengal") || breed.contains("ragdoll")) {
                ivReportImage.setImageResource(R.drawable.ic_pets_cat);
            } else {
                ivReportImage.setImageResource(R.drawable.ic_pets_other);
            }
        } else {
            // Fallback to status-based image
            if ("lost".equals(report.getStatus())) {
                ivReportImage.setImageResource(R.drawable.ic_pets_missing);
            } else if ("found".equals(report.getStatus())) {
                ivReportImage.setImageResource(R.drawable.ic_pets_found);
            } else {
                ivReportImage.setImageResource(R.drawable.ic_pets_other);
            }
        }
    }
    
    private void setupFabListeners() {
        // Debug logging for reporter information
        Log.d("EnhancedReportDetail", "Setting up FAB listeners");
        Log.d("EnhancedReportDetail", "Reporter: " + (report.getReporter() != null ? "Available" : "NULL"));
        if (report.getReporter() != null) {
            Log.d("EnhancedReportDetail", "Phone: " + report.getReporter().getPhone());
        }
        
        // Contact FAB - check if phone number is available
        if (report.getReporter() != null && 
            report.getReporter().getPhone() != null && 
            !report.getReporter().getPhone().isEmpty()) {
            
            Log.d("EnhancedReportDetail", "Phone number available, showing call FAB");
            
            fabCall.setOnClickListener(v -> {
                String phoneNumber = report.getReporter().getPhone();
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + phoneNumber));
                    startActivity(callIntent);
                } catch (Exception e) {
                    Log.e("EnhancedReportDetail", "Error opening phone app", e);
                    Toast.makeText(this, "Unable to open phone app", Toast.LENGTH_SHORT).show();
                }
            });
            
            // Show the call button since phone number is available
            fabCall.setVisibility(View.VISIBLE);
        } else {
            Log.d("EnhancedReportDetail", "No phone number available, hiding call FAB");
            // Hide the call button if no phone number is available
            fabCall.setVisibility(View.GONE);
        }
        
        // Map FAB - open location in maps app
        fabMap.setOnClickListener(v -> {
            if (report.getLocation() != null) {
                double lat = report.getLocation().getLatitude();
                double lng = report.getLocation().getLongitude();
                
                // Create URI for maps app
                Uri gmmIntentUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(Pet Report Location)");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    // Fallback to browser if Maps app not available
                    String url = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }
      private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    
    private void onFoundItClicked() {
        // Show confirmation dialog or direct action
        Toast.makeText(this, "Great! Thanks for letting us know you found " + 
            (report.getPet() != null ? report.getPet().getDisplayName() : "the pet") + "!", 
            Toast.LENGTH_LONG).show();
        
        // TODO: In a real app, you would:
        // 1. Update the report status to "resolved" via API
        // 2. Send notification to the pet owner
        // 3. Mark the report as closed
        // 4. Optionally redirect to a success page
        
        Log.d("EnhancedReportDetail", "Found It button clicked for report: " + report.getId());
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
