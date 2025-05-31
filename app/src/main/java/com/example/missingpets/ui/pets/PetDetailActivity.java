package com.example.missingpets.ui.pets;

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
import com.example.missingpets.data.models.Pet;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PetDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_PET = "extra_pet";
    
    private ImageView ivPetImage;
    private TextView tvPetName;
    private Chip chipStatus;
    private TextView tvPetType;
    private TextView tvPetBreed;
    private TextView tvPetColor;
    private TextView tvPetSize;
    private TextView tvLocation;
    private TextView tvLastSeen;
    private TextView tvDescription;
    private TextView tvOwnerName;
    private TextView tvContactInfo;
    private FloatingActionButton fabCall;
    private FloatingActionButton fabMessage;
    
    private Pet pet;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_detail);
        
        // Get pet data from intent
        pet = (Pet) getIntent().getSerializableExtra(EXTRA_PET);
        if (pet == null) {
            Toast.makeText(this, "Error loading pet details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupToolbar();
        setupClickListeners();
        populateData();
    }
    
    private void initViews() {
        ivPetImage = findViewById(R.id.ivPetImage);
        tvPetName = findViewById(R.id.tvPetName);
        chipStatus = findViewById(R.id.chipStatus);
        tvPetType = findViewById(R.id.tvPetType);
        tvPetBreed = findViewById(R.id.tvPetBreed);
        tvPetColor = findViewById(R.id.tvPetColor);
        tvPetSize = findViewById(R.id.tvPetSize);
        tvLocation = findViewById(R.id.tvLocation);
        tvLastSeen = findViewById(R.id.tvLastSeen);
        tvDescription = findViewById(R.id.tvDescription);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvContactInfo = findViewById(R.id.tvContactInfo);
        fabCall = findViewById(R.id.fabCall);
        fabMessage = findViewById(R.id.fabMessage);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(pet.getName());
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
      private void setupClickListeners() {
        // fabCall is hidden since we don't have owner contact info in Pet model
        fabCall.setVisibility(View.GONE);
        
        // fabMessage opens in-app messaging instead
        fabMessage.setOnClickListener(v -> openInAppMessaging());
    }
      private void populateData() {
        // Set pet name - use displayName helper method
        tvPetName.setText(pet.getDisplayName());
        
        // Hide status chip since Pet model no longer contains status
        // Status is now in Report model
        chipStatus.setVisibility(View.GONE);
        
        // Set pet details using available fields
        // Note: Pet model no longer has 'type' field, so we'll derive it from breed or hide it
        tvPetType.setVisibility(View.GONE); // Hide type field as it's not in API
        tvPetBreed.setText(pet.getBreed() != null ? pet.getBreed() : "Mixed breed");
        tvPetColor.setText(pet.getColor() != null ? capitalizeFirst(pet.getColor()) : "Unknown");
        
        // Show height and weight instead of size (new API fields)
        String sizeInfo = buildSizeInfo();
        tvPetSize.setText(sizeInfo);
        
        // Hide location since it's not in Pet model (now in Report model)
        tvLocation.setVisibility(View.GONE);
        
        // Show creation date instead of last seen
        if (pet.getCreatedAt() != null) {
            tvLastSeen.setText("Posted: " + dateFormat.format(pet.getCreatedAt()));
        } else {
            tvLastSeen.setText("Date unknown");
        }
        
        // Hide description since Pet model no longer has description
        tvDescription.setVisibility(View.GONE);
        findViewById(R.id.cardDescription).setVisibility(View.GONE);
        
        // Hide owner information since Pet model no longer contains owner contact details
        // Owner information would need to be fetched separately via API
        tvOwnerName.setText("Contact owner through app");
        tvContactInfo.setText("Use the app's messaging system to contact the owner.");
        
        // Set pet image
        setPetImage();
        
        // Hide contact buttons since we don't have direct contact info in Pet model
        fabCall.setVisibility(View.GONE);
        // Keep message button but make it open in-app messaging
        fabMessage.setOnClickListener(v -> openInAppMessaging());
    }
      private String buildSizeInfo() {
        StringBuilder sizeInfo = new StringBuilder();
        
        if (pet.getHeight() != null) {
            sizeInfo.append("Height: ").append(pet.getHeight()).append(" cm");
        }
        
        if (pet.getWeight() != null) {
            if (sizeInfo.length() > 0) sizeInfo.append(", ");
            sizeInfo.append("Weight: ").append(pet.getWeight()).append(" kg");
        }
        
        return sizeInfo.length() > 0 ? sizeInfo.toString() : "Size unknown";
    }
    
    private void setPetImage() {
        // Load actual pet image if available
        String imageUrl = pet.getDisplayImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // TODO: Load image with Glide or similar library
            // Glide.with(this).load(imageUrl).into(ivPetImage);
        }
        
        // Set placeholder based on breed (since type is not available)
        String breed = pet.getBreed();
        if (breed != null) {
            String lowerBreed = breed.toLowerCase();
            if (lowerBreed.contains("dog") || lowerBreed.contains("retriever") || 
                lowerBreed.contains("terrier") || lowerBreed.contains("bulldog")) {
                ivPetImage.setImageResource(R.drawable.ic_pets_dog);
            } else if (lowerBreed.contains("cat") || lowerBreed.contains("persian") || 
                       lowerBreed.contains("siamese") || lowerBreed.contains("maine")) {
                ivPetImage.setImageResource(R.drawable.ic_pets_cat);
            } else {
                ivPetImage.setImageResource(R.drawable.ic_pets_other);
            }
        } else {
            ivPetImage.setImageResource(R.drawable.ic_pets_other);
        }
    }
    
    private void openInAppMessaging() {
        // TODO: Implement in-app messaging system
        // This would typically open a chat/messaging activity
        Toast.makeText(this, "In-app messaging feature coming soon", Toast.LENGTH_SHORT).show();
    }
      private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}
