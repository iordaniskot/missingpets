package com.example.missingpets.ui.reports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.missingpets.R;
import com.example.missingpets.data.models.Report;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ReportDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_REPORT = "extra_report";
    
    private ImageView ivReportImage;
    private TextView tvReportTitle;
    private Chip chipStatus;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvReportDate;
    private TextView tvReporterId;
    private FloatingActionButton fabCall;
    private FloatingActionButton fabMap;
    
    private Report report;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        
        // Get report data from intent
        report = (Report) getIntent().getSerializableExtra(EXTRA_REPORT);
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
        tvLocation = findViewById(R.id.tvLocation);
        tvReportDate = findViewById(R.id.tvReportDate);
        tvReporterId = findViewById(R.id.tvReporterId);
        fabCall = findViewById(R.id.fabCall);
        fabMap = findViewById(R.id.fabMap);
    }
    
    private void displayReportData() {
        // Set report title
        String title = "Pet Report";
        if (report.getId() != null) {
            title += " #" + report.getId().substring(0, Math.min(8, report.getId().length()));
        }
        tvReportTitle.setText(title);
        
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
        
        // Set location
        if (report.getLocation() != null) {
            double lat = report.getLocation().getLatitude();
            double lng = report.getLocation().getLongitude();
            tvLocation.setText(String.format(Locale.getDefault(), 
                "Latitude: %.6f\nLongitude: %.6f", lat, lng));
        } else {
            tvLocation.setText("Location not available");
        }
        
        // Set report date
        if (report.getCreatedAt() != null) {
            tvReportDate.setText("Reported on: " + dateFormat.format(report.getCreatedAt()));
        } else {
            tvReportDate.setText("Report date unknown");
        }
        
        // Set reporter ID (in real app, you'd fetch user details)
        if (report.getReporter() != null) {
            tvReporterId.setText("Reporter ID: " + report.getReporter());
        } else {
            tvReporterId.setText("Reporter unknown");
        }
        
        // Set placeholder image based on status
        setPlaceholderImage();
        
        // Set up FAB click listeners
        setupFabListeners();
    }
    
    private void setPlaceholderImage() {
        if ("lost".equals(report.getStatus())) {
            ivReportImage.setImageResource(R.drawable.ic_pets_missing);
        } else if ("found".equals(report.getStatus())) {
            ivReportImage.setImageResource(R.drawable.ic_pets_found);
        } else {
            ivReportImage.setImageResource(R.drawable.ic_pets_other);
        }
    }
    
    private void setupFabListeners() {
        // Contact FAB - placeholder for now
        fabCall.setOnClickListener(v -> {
            Toast.makeText(this, "Contact feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
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
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
