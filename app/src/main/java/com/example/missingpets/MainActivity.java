package com.example.missingpets;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.missingpets.ui.auth.LoginActivity;
import com.example.missingpets.ui.reports.MissingPetsActivity;
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
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
            Toast.makeText(this, "Interactive Map feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_logout) {
            logout();
            return true;
        } else if (itemId == R.id.action_profile) {
            // TODO: Navigate to profile screen
            Toast.makeText(this, "Profile feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_settings) {
            // TODO: Navigate to settings screen
            Toast.makeText(this, "Settings feature coming soon!", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_about) {
            // TODO: Show about dialog
            Toast.makeText(this, "Missing Pets v1.0 - Reuniting families, one pet at a time", Toast.LENGTH_LONG).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
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