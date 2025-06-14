package com.example.missingpets;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpets.ui.auth.LoginActivity;
import com.example.missingpets.ui.reports.MissingPetsActivity;
import com.example.missingpets.ui.reports.MapViewActivity;
import com.example.missingpets.ui.pets.ReportPetActivity;
import com.example.missingpets.utils.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        preferenceManager = PreferenceManager.getInstance(this);
        
        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        
        // Check if user is logged in
        if (!preferenceManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        
        setupUI();
    }
    
    private void setupUI() {
        // Display user information for testing
        TextView welcomeText = findViewById(R.id.tvWelcome);
        String userName = preferenceManager.getUserName();
        String userEmail = preferenceManager.getUserEmail();
        
        if (userName != null) {
            welcomeText.setText("Welcome, " + userName + "!");
        } else if (userEmail != null) {
            welcomeText.setText("Welcome, " + userEmail + "!");
        } else {
            welcomeText.setText("Welcome to Missing Pets!");
        }
        
        // Set up card click listeners
        setupCardClickListeners();
        
        // Set up logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }
    
    private void setupCardClickListeners() {
        findViewById(R.id.cardReportMissing).setOnClickListener(v -> {
            // Navigate to Report Pet Activity
            Intent intent = new Intent(this, ReportPetActivity.class);
            startActivity(intent);
        });
          findViewById(R.id.cardSearchFound).setOnClickListener(v -> {
            // Navigate to Missing Pets Activity
            Intent intent = new Intent(this, MissingPetsActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.cardViewMap).setOnClickListener(v -> {
            // Navigate to Map View Activity
            Intent intent = new Intent(this, MapViewActivity.class);
            startActivity(intent);
        });
    }
    
    private void logout() {
        preferenceManager.clearUserData();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}