package com.example.missingpets.ui.reports;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.missingpets.R;
import com.example.missingpets.data.api.ApiClient;
import com.example.missingpets.data.models.Pet;
import com.example.missingpets.data.models.Report;
import com.example.missingpets.data.models.ReportWithPet;
import com.example.missingpets.data.models.ReportsResponse;
import com.example.missingpets.data.models.User;
import com.example.missingpets.utils.PreferenceManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MissingPetsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EnhancedReportListAdapter adapter;
    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter;
    private FloatingActionButton fabMapView;
    private View emptyStateView;
    private PreferenceManager preferenceManager;
      private List<ReportWithPet> allReports = new ArrayList<>();
    private List<ReportWithPet> filteredReports = new ArrayList<>();
    private String currentStatus = TAB_ALL;
    private String currentSearchQuery = "";
    private FilterBottomSheetDialogFragment.FilterOptions currentFilterOptions = 
            new FilterBottomSheetDialogFragment.FilterOptions();
    
    private static final String TAB_ALL = "all";
    private static final String TAB_MISSING = "lost";
    private static final String TAB_FOUND = "found";
      @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MissingPetsActivity", "onCreate() called");
        
        setContentView(R.layout.activity_missing_pets);
        
        preferenceManager = PreferenceManager.getInstance(this);
        Log.d("MissingPetsActivity", "PreferenceManager initialized");
        
        // Log user authentication status
        Log.d("MissingPetsActivity", "User logged in: " + preferenceManager.isLoggedIn());
        Log.d("MissingPetsActivity", "User ID: " + preferenceManager.getUserId());
        Log.d("MissingPetsActivity", "Auth token available: " + (preferenceManager.getAuthToken() != null));
        
        setupToolbar();
        setupViews();
        setupTabLayout();
        
        Log.d("MissingPetsActivity", "UI setup complete, loading initial reports");
        loadReports(TAB_ALL);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Missing Pets");
        }
    }
      private void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewReports);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);
        etSearch = findViewById(R.id.etSearch);
        btnFilter = findViewById(R.id.btnFilter);
        fabMapView = findViewById(R.id.fabMapView);
        emptyStateView = findViewById(R.id.emptyStateView);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EnhancedReportListAdapter(new ArrayList<>(), this::onReportClick);
        recyclerView.setAdapter(adapter);
        
        setupSearchFunctionality();
        setupFilterFunctionality();
        setupMapViewFunctionality();
    }
    
    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("All Reports").setTag(TAB_ALL));
        tabLayout.addTab(tabLayout.newTab().setText("Missing").setTag(TAB_MISSING));
        tabLayout.addTab(tabLayout.newTab().setText("Found").setTag(TAB_FOUND));
          tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabTag = (String) tab.getTag();
                currentStatus = tabTag;
                applyFilters();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupSearchFunctionality() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                applyFilters();
            }
        });
    }
      private void setupFilterFunctionality() {
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheetDialogFragment filterDialog = 
                    FilterBottomSheetDialogFragment.newInstance(currentFilterOptions);
            filterDialog.setFilterListener(this::onFiltersApplied);
            filterDialog.show(getSupportFragmentManager(), "filter_dialog");
        });
    }    private void setupMapViewFunctionality() {
        fabMapView.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapViewActivity.class);
            // Pass filtered reports to map view
            ArrayList<ReportWithPet> reportsToPass = new ArrayList<>(filteredReports);
            intent.putExtra(MapViewActivity.EXTRA_REPORTS, reportsToPass);
            startActivity(intent);
        });
    }private void loadReports(String status) {
        Log.d("MissingPetsActivity", "loadReports() called with status: " + status);
        
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.GONE);
        
        // Validate authentication
        String rawToken = preferenceManager.getAuthToken();
        if (rawToken == null || rawToken.trim().isEmpty()) {
            Log.e("MissingPetsActivity", "No auth token available - user not properly logged in");
            progressBar.setVisibility(View.GONE);
            showEmptyState();
            Toast.makeText(this, "Authentication error. Please log out and log back in.", Toast.LENGTH_LONG).show();
            return;
        }
        
        String authToken = "Bearer " + rawToken;
        Log.d("MissingPetsActivity", "Auth token available - length: " + authToken.length());
        Log.d("MissingPetsActivity", "User ID: " + preferenceManager.getUserId());
        Log.d("MissingPetsActivity", "User is logged in: " + preferenceManager.isLoggedIn());
        
        Call<ReportsResponse> call = ApiClient.getApiService().getReports(authToken);        Log.d("MissingPetsActivity", "Making API call to fetch reports...");
        Log.d("MissingPetsActivity", "API Base URL: " + com.example.missingpets.utils.Constants.BASE_URL);
        
        call.enqueue(new Callback<ReportsResponse>() {
            @Override
            public void onResponse(Call<ReportsResponse> call, Response<ReportsResponse> response) {
                Log.d("MissingPetsActivity", "API response received - Code: " + response.code());
                Log.d("MissingPetsActivity", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ReportsResponse reportsResponse = response.body();
                    List<Report> reports = reportsResponse.getData();
                    
                    if (reports != null) {
                        Log.d("MissingPetsActivity", "Successfully received " + reports.size() + " reports");
                        
                        // Log pagination info
                        if (reportsResponse.getPagination() != null) {
                            ReportsResponse.Pagination pagination = reportsResponse.getPagination();
                            Log.d("MissingPetsActivity", "Pagination - Current page: " + pagination.getCurrentPage() + 
                                  ", Total pages: " + pagination.getTotalPages() + 
                                  ", Total items: " + pagination.getTotalItems());
                        }
                          // Log details about the first few reports for debugging
                        for (int i = 0; i < Math.min(3, reports.size()); i++) {
                            Report report = reports.get(i);
                            Log.d("MissingPetsActivity", "Report " + i + " - ID: " + report.getId() + 
                                  ", Status: " + report.getStatus() + 
                                  ", Pet: " + (report.getPet() != null ? report.getPet().getName() + " (" + report.getPetId() + ")" : "null") + 
                                  ", Reporter: " + (report.getReporter() != null ? report.getReporter().getName() + " (" + report.getReporterId() + ")" : "null"));
                        }
                        
                        // Convert reports to enhanced reports and populate with pet data
                        populateReportsWithPetData(reports, authToken);
                    } else {
                        Log.e("MissingPetsActivity", "Reports data is null in response");
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MissingPetsActivity.this, "No reports data received", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Enhanced error logging for failed responses
                    Log.e("MissingPetsActivity", "Failed to load reports - Status code: " + response.code());
                    String errorMessage = "Failed to load reports. Code: " + response.code();
                    
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e("MissingPetsActivity", "Error response body: " + errorBody);
                            
                            if (response.code() == 401) {
                                errorMessage = "Authentication failed. Please log out and log back in.";
                                Log.e("MissingPetsActivity", "Authentication error - token may be expired");
                            } else if (response.code() == 403) {
                                errorMessage = "Access forbidden. Check your account permissions.";
                                Log.e("MissingPetsActivity", "Authorization error - user may not have permission");
                            } else if (response.code() == 500) {
                                errorMessage = "Server error. Please try again later.";
                                Log.e("MissingPetsActivity", "Server error - backend may be down");
                            }
                        } else {
                            Log.e("MissingPetsActivity", "No error body available");
                        }
                    } catch (Exception e) {
                        Log.e("MissingPetsActivity", "Error parsing error response", e);
                    }
                    
                    progressBar.setVisibility(View.GONE);
                    showEmptyState();
                    Toast.makeText(MissingPetsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
              @Override
            public void onFailure(Call<ReportsResponse> call, Throwable t) {
                Log.e("MissingPetsActivity", "Network error loading reports", t);
                Log.e("MissingPetsActivity", "Error type: " + t.getClass().getSimpleName());
                Log.e("MissingPetsActivity", "Error message: " + t.getMessage());
                
                progressBar.setVisibility(View.GONE);
                showEmptyState();
                
                String errorMessage = "Network error: " + t.getMessage();
                if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "No internet connection. Please check your network.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Request timed out. Please try again.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "Cannot connect to server. Please try again later.";
                }
                
                Toast.makeText(MissingPetsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }    private void populateReportsWithPetData(List<Report> reports, String authToken) {
        Log.d("MissingPetsActivity", "populateReportsWithPetData() called with " + reports.size() + " reports");
        
        List<ReportWithPet> enhancedReports = new ArrayList<>();
        
        if (reports.isEmpty()) {
            Log.d("MissingPetsActivity", "No reports to process - showing empty state");
            allReports = enhancedReports;
            progressBar.setVisibility(View.GONE);
            applyFilters();
            return;
        }
        
        try {
            // For now, create enhanced reports without populated pet data
            // In a real app, you would make additional API calls to fetch pet details
            for (int i = 0; i < reports.size(); i++) {
                Report report = reports.get(i);
                Log.d("MissingPetsActivity", "Processing report " + i + " - ID: " + report.getId());
                
                try {
                    ReportWithPet enhancedReport = ReportWithPet.fromReport(report);
                    
                    // Log report details for debugging
                    Log.d("MissingPetsActivity", "Enhanced report created - Status: " + enhancedReport.getStatus() + 
                          ", Location: " + enhancedReport.getLocationString() + 
                          ", Created: " + enhancedReport.getCreatedAt());
                    
                    // TODO: Fetch and populate pet data using report.getPet() ID
                    // For now, we'll show reports without full pet details
                    enhancedReports.add(enhancedReport);
                    
                } catch (Exception e) {
                    Log.e("MissingPetsActivity", "Error processing report " + i + " (ID: " + report.getId() + ")", e);
                    // Continue with other reports even if one fails
                }
            }
            
            Log.d("MissingPetsActivity", "Successfully processed " + enhancedReports.size() + " out of " + reports.size() + " reports");
            
        } catch (Exception e) {
            Log.e("MissingPetsActivity", "Critical error in populateReportsWithPetData", e);
            // Show error to user and set empty state
            Toast.makeText(this, "Error processing reports data", Toast.LENGTH_SHORT).show();
        }
        
        allReports = enhancedReports;
        progressBar.setVisibility(View.GONE);
        
        Log.d("MissingPetsActivity", "Calling applyFilters() with " + allReports.size() + " total reports");
        applyFilters();
    }
      private void applyFilters() {
        Log.d("MissingPetsActivity", "applyFilters() called");
        Log.d("MissingPetsActivity", "Current status filter: " + currentStatus);
        Log.d("MissingPetsActivity", "Current search query: '" + currentSearchQuery + "'");
        Log.d("MissingPetsActivity", "Total reports to filter: " + allReports.size());
        
        filteredReports.clear();
        
        try {
            for (int i = 0; i < allReports.size(); i++) {
                ReportWithPet report = allReports.get(i);
                
                // Apply status filter
                boolean statusMatch = TAB_ALL.equals(currentStatus) || 
                                    currentStatus.equals(report.getStatus());
                
                // Apply search filter
                boolean searchMatch = currentSearchQuery.isEmpty() || 
                                    matchesSearchQuery(report, currentSearchQuery);
                
                // Apply advanced filters
                boolean advancedMatch = matchesAdvancedFilters(report, currentFilterOptions);
                
                if (statusMatch && searchMatch && advancedMatch) {
                    filteredReports.add(report);
                }
                
                // Log first few reports for debugging
                if (i < 3) {
                    Log.d("MissingPetsActivity", "Report " + i + " - Status: " + report.getStatus() + 
                          ", StatusMatch: " + statusMatch + 
                          ", SearchMatch: " + searchMatch + 
                          ", AdvancedMatch: " + advancedMatch + 
                          ", Final: " + (statusMatch && searchMatch && advancedMatch));
                }
            }
            
            Log.d("MissingPetsActivity", "Filtering complete - " + filteredReports.size() + " reports match filters");
            
        } catch (Exception e) {
            Log.e("MissingPetsActivity", "Error during filtering", e);
            // Continue with whatever reports we have processed
        }
        
        updateUI();
    }
    
    private boolean matchesSearchQuery(ReportWithPet report, String query) {
        String lowercaseQuery = query.toLowerCase();
        
        // Search in description
        if (report.getDescription() != null && 
            report.getDescription().toLowerCase().contains(lowercaseQuery)) {
            return true;
        }
        
        // Search in pet details if available
        if (report.getPet() != null) {
            Pet pet = report.getPet();
            
            if (pet.getName() != null && 
                pet.getName().toLowerCase().contains(lowercaseQuery)) {
                return true;
            }
            
            if (pet.getBreed() != null && 
                pet.getBreed().toLowerCase().contains(lowercaseQuery)) {
                return true;
            }
            
            if (pet.getColor() != null && 
                pet.getColor().toLowerCase().contains(lowercaseQuery)) {
                return true;
            }
        }
        
        // Search in location
        if (report.getLocationString() != null && 
            report.getLocationString().toLowerCase().contains(lowercaseQuery)) {
            return true;
        }
          return false;
    }
    
    private boolean matchesAdvancedFilters(ReportWithPet report, FilterBottomSheetDialogFragment.FilterOptions filters) {
        // Check breed filter
        if (!filters.selectedBreed.isEmpty() && report.getPet() != null) {
            String breed = report.getPet().getBreed();
            if (breed == null || !breed.toLowerCase().contains(filters.selectedBreed.toLowerCase())) {
                return false;
            }
        }
        
        // Check color filter
        if (!filters.selectedColor.isEmpty() && report.getPet() != null) {
            String color = report.getPet().getColor();
            if (color == null || !color.toLowerCase().contains(filters.selectedColor.toLowerCase())) {
                return false;
            }
        }
        
        // Check time filter
        if (report.getCreatedAt() != null) {
            long reportTime = report.getCreatedAt().getTime();
            long currentTime = System.currentTimeMillis();
            long daysDiff = (currentTime - reportTime) / (1000 * 60 * 60 * 24);
            
            if (daysDiff > filters.daysAgo) {
                return false;
            }
        }
        
        // Note: Location radius filtering would require user's current location
        // For now, we'll skip this filter as it requires location permissions
        
        return true;
    }
    
    private void onFiltersApplied(FilterBottomSheetDialogFragment.FilterOptions filterOptions) {
        currentFilterOptions = filterOptions;
        applyFilters();
        
        // Show toast with filter summary
        StringBuilder summary = new StringBuilder("Filters applied");
        if (!filterOptions.selectedBreed.isEmpty()) {
            summary.append(" | Breed: ").append(filterOptions.selectedBreed);
        }
        if (!filterOptions.selectedColor.isEmpty()) {
            summary.append(" | Color: ").append(filterOptions.selectedColor);
        }
        summary.append(" | Within ").append(filterOptions.daysAgo).append(" days");
        
        Toast.makeText(this, summary.toString(), Toast.LENGTH_SHORT).show();
    }
      private void updateUI() {
        Log.d("MissingPetsActivity", "updateUI() called with " + filteredReports.size() + " filtered reports");
        
        if (filteredReports.isEmpty()) {
            Log.d("MissingPetsActivity", "No filtered reports - showing empty state");
            showEmptyState();
        } else {
            Log.d("MissingPetsActivity", "Showing " + filteredReports.size() + " reports in RecyclerView");
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
            
            try {
                adapter.updateReports(filteredReports);
                Log.d("MissingPetsActivity", "Adapter updated successfully");
            } catch (Exception e) {
                Log.e("MissingPetsActivity", "Error updating adapter", e);
                showEmptyState();
                Toast.makeText(this, "Error displaying reports", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
    }
      private void onReportClick(ReportWithPet report) {
        Intent intent = new Intent(this, EnhancedReportDetailActivity.class);
        intent.putExtra(EnhancedReportDetailActivity.EXTRA_REPORT, report);
        startActivity(intent);
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
