package com.example.missingpets.ui.reports;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.missingpets.R;
import com.example.missingpets.data.api.ApiClient;
import com.example.missingpets.data.models.Report;
import com.example.missingpets.data.models.ReportWithPet;
import com.example.missingpets.data.models.ReportsResponse;
import com.example.missingpets.utils.PreferenceManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapViewActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final double DEFAULT_RADIUS_KM = 5.0;
    private static final int DEFAULT_ZOOM_LEVEL = 13;
    
    public static final String EXTRA_REPORTS = "extra_reports";
    
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PreferenceManager preferenceManager;
    
    // UI Components
    private ProgressBar progressBar;
    private TextInputEditText etRadius;
    private MaterialButton btnSearch;
    private FloatingActionButton fabMyLocation;
      // Data
    private List<ReportWithPet> reports = new ArrayList<>();
    private Map<Marker, ReportWithPet> markerReportMap = new HashMap<>();
    private LatLng currentUserLocation;
    private Circle radiusCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        preferenceManager = PreferenceManager.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupToolbar();
        initViews();
        setupMapFragment();
        getReportsFromIntent();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Missing Pets Map");
        }
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        etRadius = findViewById(R.id.etRadius);
        btnSearch = findViewById(R.id.btnSearch);
        fabMyLocation = findViewById(R.id.fabMyLocation);

        btnSearch.setOnClickListener(v -> searchNearbyReports());
        fabMyLocation.setOnClickListener(v -> moveToCurrentLocation());
    }

    private void setupMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void getReportsFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_REPORTS)) {
            // For future implementation when reports are passed via Intent
            Log.d(TAG, "Reports received via Intent");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Map is ready");

        // Set up map UI settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Set up marker click listener
        mMap.setOnMarkerClickListener(this::onMarkerClick);

        // Set up info window click listener
        mMap.setOnInfoWindowClickListener(this::onInfoWindowClick);

        // Enable location if permission is granted
        if (checkLocationPermission()) {
            enableMyLocation();
            getCurrentLocationAndSearch();
        } else {
            requestLocationPermission();
        }

        // Set default location (center of US)
        LatLng defaultLocation = new LatLng(39.8283, -98.5795);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 4));
    }

    private boolean onMarkerClick(Marker marker) {
        ReportWithPet report = markerReportMap.get(marker);
        if (report != null) {
            // Show custom info window
            marker.showInfoWindow();
            return true;
        }
        return false;
    }

    private void onInfoWindowClick(Marker marker) {
        ReportWithPet report = markerReportMap.get(marker);
        if (report != null) {
            // Open report details
            Intent intent = new Intent(this, ReportDetailActivity.class);
            intent.putExtra(ReportDetailActivity.EXTRA_REPORT, report);
            startActivity(intent);
        }
    }

    private void getCurrentLocationAndSearch() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, DEFAULT_ZOOM_LEVEL));
                            drawRadiusCircle(); // Draw the radius circle first
                            searchNearbyReports();
                        } else {
                            Log.w(TAG, "Current location is null");
                            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get current location", e);
                        Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when getting location", e);
        }
    }

    private void moveToCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        getCurrentLocationAndSearch();
    }    private void searchNearbyReports() {
        if (currentUserLocation == null) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the radius circle with current input
        drawRadiusCircle();

        double radiusKm;
        try {
            String radiusText = etRadius.getText().toString().trim();
            radiusKm = radiusText.isEmpty() ? DEFAULT_RADIUS_KM : Double.parseDouble(radiusText);
        } catch (NumberFormatException e) {
            radiusKm = DEFAULT_RADIUS_KM;
            etRadius.setText(String.valueOf(DEFAULT_RADIUS_KM));
        }

        // Convert km to meters for API
        int radiusMeters = (int) (radiusKm * 1000);

        progressBar.setVisibility(View.VISIBLE);
        
        String authToken = "Bearer " + preferenceManager.getAuthToken();
        
        Call<ReportsResponse> call = ApiClient.getApiService().getReportsNearLocation(
                authToken,
                currentUserLocation.latitude,
                currentUserLocation.longitude,
                radiusMeters,
                "lost" // Only show missing pets
        );

        call.enqueue(new Callback<ReportsResponse>() {
            @Override
            public void onResponse(Call<ReportsResponse> call, Response<ReportsResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Report> apiReports = response.body().getData();
                    if (apiReports != null) {
                        convertAndDisplayReports(apiReports);
                    } else {
                        Log.w(TAG, "No reports data in response");
                        Toast.makeText(MapViewActivity.this, "No missing pets found in this area", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load nearby reports - Status: " + response.code());
                    Toast.makeText(MapViewActivity.this, "Failed to load nearby reports", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReportsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error loading nearby reports", t);
                Toast.makeText(MapViewActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertAndDisplayReports(List<Report> apiReports) {
        reports.clear();
        for (Report report : apiReports) {
            ReportWithPet reportWithPet = ReportWithPet.fromReport(report);
            reports.add(reportWithPet);
        }
        
        Log.d(TAG, "Displaying " + reports.size() + " missing pet reports on map");
        displayReportsOnMap();
    }

    private void displayReportsOnMap() {
        if (mMap == null) {
            Log.w(TAG, "Map is not ready");
            return;
        }

        // Clear existing markers and circle
        mMap.clear();
        markerReportMap.clear();
        radiusCircle = null;

        // Draw radius circle if we have a current location
        drawRadiusCircle();

        // Add markers for each report
        for (ReportWithPet report : reports) {
            if (report.getLocation() != null && report.getLocation().getCoordinates() != null) {
                LatLng position = new LatLng(
                        report.getLocation().getLatitude(),
                        report.getLocation().getLongitude()
                );

                String title = getMarkerTitle(report);
                String snippet = getMarkerSnippet(report);
                
                float markerColor = getMarkerColor(report);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor));

                Marker marker = mMap.addMarker(markerOptions);
                if (marker != null) {
                    markerReportMap.put(marker, report);
                }
            }
        }

        Toast.makeText(this, "Found " + reports.size() + " missing pets nearby", Toast.LENGTH_SHORT).show();
    }

    private void drawRadiusCircle() {
        if (currentUserLocation == null || mMap == null) {
            return;
        }

        // Get current radius from input field
        double radiusKm;
        try {
            String radiusText = etRadius.getText().toString().trim();
            radiusKm = radiusText.isEmpty() ? DEFAULT_RADIUS_KM : Double.parseDouble(radiusText);
        } catch (NumberFormatException e) {
            radiusKm = DEFAULT_RADIUS_KM;
        }

        // Convert km to meters for circle
        double radiusMeters = radiusKm * 1000;

        // Create circle options with low opacity
        CircleOptions circleOptions = new CircleOptions()
                .center(currentUserLocation)
                .radius(radiusMeters)
                .strokeColor(0x660000FF) // Blue with low opacity (40% alpha)
                .fillColor(0x220000FF)   // Blue with very low opacity (13% alpha)
                .strokeWidth(2);

        // Add circle to map
        radiusCircle = mMap.addCircle(circleOptions);
    }

    private String getMarkerTitle(ReportWithPet report) {
        if (report.getPet() != null && report.getPet().getName() != null) {
            return report.getPet().getName();
        }
        return "Missing Pet";
    }

    private String getMarkerSnippet(ReportWithPet report) {
        StringBuilder snippet = new StringBuilder();
        
        if (report.getPet() != null) {
            if (report.getPet().getBreed() != null) {
                snippet.append(report.getPet().getBreed());
            }
            if (report.getPet().getColor() != null) {
                if (snippet.length() > 0) snippet.append(" • ");
                snippet.append(report.getPet().getColor());
            }
        }
        
        if (snippet.length() == 0) {
            snippet.append("Tap for details");
        } else {
            snippet.append(" • Tap for details");
        }
        
        return snippet.toString();
    }

    private float getMarkerColor(ReportWithPet report) {
        // Different colors for different statuses
        if ("lost".equals(report.getStatus())) {
            return BitmapDescriptorFactory.HUE_RED;
        } else if ("found".equals(report.getStatus())) {
            return BitmapDescriptorFactory.HUE_GREEN;
        } else {
            return BitmapDescriptorFactory.HUE_ORANGE;
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
        if (checkLocationPermission() && mMap != null) {
            try {
                mMap.setMyLocationEnabled(true);
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception enabling my location", e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                getCurrentLocationAndSearch();
            } else {
                Toast.makeText(this, "Location permission is required to show nearby missing pets", 
                        Toast.LENGTH_LONG).show();
            }
        }
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
