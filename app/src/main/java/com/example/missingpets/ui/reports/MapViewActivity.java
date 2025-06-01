package com.example.missingpets.ui.reports;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.missingpets.R;
import com.example.missingpets.data.models.ReportWithPet;

import java.util.ArrayList;
import java.util.List;

public class MapViewActivity extends AppCompatActivity {

    public static final String EXTRA_REPORTS = "extra_reports";
    
    private List<ReportWithPet> reports = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        setupToolbar();
        getReportsFromIntent();
        setupMapView();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Missing Pets Map");
        }
    }

    private void getReportsFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_REPORTS)) {
            // In a real implementation, you would pass reports via Intent
            // For now, we'll show a placeholder
        }
    }

    private void setupMapView() {
        // TODO: Implement actual map functionality
        // This would typically involve:
        // 1. Setting up Google Maps or similar mapping library
        // 2. Adding markers for each missing pet report
        // 3. Clustering nearby markers
        // 4. Adding info windows with pet details
        // 5. Implementing location-based filtering
        
        Toast.makeText(this, 
            "Map view will show missing pets locations. " +
            "This requires Google Maps integration which is not implemented yet.", 
            Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
