package com.example.missingpets.ui.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.missingpets.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class MapLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String EXTRA_LATITUDE = "latitude";
    private static final String EXTRA_LONGITUDE = "longitude";
    private static final String EXTRA_ADDRESS = "address";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;
    private String selectedAddress;
    private MaterialButton btnConfirmLocation;
    private FloatingActionButton fabMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location_picker);

        setupToolbar();
        initViews();
        setupMapFragment();
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Location");
        }
    }

    private void initViews() {
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
        fabMyLocation.setOnClickListener(v -> moveToCurrentLocation());
        
        // Initially disable confirm button
        btnConfirmLocation.setEnabled(false);
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set up map click listener
        mMap.setOnMapClickListener(latLng -> {
            selectLocation(latLng);
        });

        // Enable location if permission is granted
        if (checkLocationPermission()) {
            enableMyLocation();
            moveToCurrentLocation();
        } else {
            requestLocationPermission();
        }

        // Set default location (e.g., center of US)
        LatLng defaultLocation = new LatLng(39.8283, -98.5795);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 4));
    }

    private void selectLocation(LatLng latLng) {
        selectedLocation = latLng;
        
        // Clear previous markers
        mMap.clear();
        
        // Add marker at selected location
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Selected Location"));

        // Get address for the location
        getAddressFromLocation(latLng);
        
        // Enable confirm button
        btnConfirmLocation.setEnabled(true);
    }    private void getAddressFromLocation(LatLng latLng) {
        // Always use coordinates format instead of geocoded address
        selectedAddress = String.format(Locale.getDefault(), 
            "%.6f, %.6f", latLng.latitude, latLng.longitude);
    }

    private void confirmLocation() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_LATITUDE, selectedLocation.latitude);
            resultIntent.putExtra(EXTRA_LONGITUDE, selectedLocation.longitude);
            resultIntent.putExtra(EXTRA_ADDRESS, selectedAddress);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void enableMyLocation() {
        if (checkLocationPermission()) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                // Handle exception
            }
        }
    }

    private void moveToCurrentLocation() {
        if (checkLocationPermission()) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), 
                                        location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory
                                        .newLatLngZoom(currentLocation, 15));
                            }
                        });
            } catch (SecurityException e) {
                // Handle exception
            }
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                moveToCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required to show your current location", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Static methods for launching the activity
    public static double getLatitudeFromIntent(Intent intent) {
        return intent.getDoubleExtra(EXTRA_LATITUDE, 0.0);
    }

    public static double getLongitudeFromIntent(Intent intent) {
        return intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0);
    }

    public static String getAddressFromIntent(Intent intent) {
        return intent.getStringExtra(EXTRA_ADDRESS);
    }
}
