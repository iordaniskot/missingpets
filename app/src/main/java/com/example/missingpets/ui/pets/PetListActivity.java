package com.example.missingpets.ui.pets;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.missingpets.R;
import com.example.missingpets.data.api.ApiClient;
import com.example.missingpets.data.models.Pet;
import com.example.missingpets.utils.PreferenceManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private PetListAdapter adapter;
    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private PreferenceManager preferenceManager;
    
    private static final String TAB_ALL = "all";
    private static final String TAB_MISSING = "missing";
    private static final String TAB_FOUND = "found";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);
        
        preferenceManager = PreferenceManager.getInstance(this);
        
        setupToolbar();
        setupViews();
        setupTabLayout();
        loadPets(TAB_ALL);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pet Listings");
        }
    }
    
    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerViewPets);
        progressBar = findViewById(R.id.progressBar);
        tabLayout = findViewById(R.id.tabLayout);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PetListAdapter(new ArrayList<>(), this::onPetClick);
        recyclerView.setAdapter(adapter);
    }
    
    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("All Pets").setTag(TAB_ALL));
        tabLayout.addTab(tabLayout.newTab().setText("Missing").setTag(TAB_MISSING));
        tabLayout.addTab(tabLayout.newTab().setText("Found").setTag(TAB_FOUND));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabTag = (String) tab.getTag();
                loadPets(tabTag);
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void loadPets(String status) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        
        String authToken = "Bearer " + preferenceManager.getAuthToken();
        Call<List<Pet>> call;
        
        if (TAB_ALL.equals(status)) {
            call = ApiClient.getApiService().getAllPets(authToken);
        } else {
            call = ApiClient.getApiService().getPetsByStatus(authToken, status);
        }
        
        call.enqueue(new Callback<List<Pet>>() {
            @Override
            public void onResponse(Call<List<Pet>> call, Response<List<Pet>> response) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Pet> pets = response.body();
                    adapter.updatePets(pets);
                    
                    if (pets.isEmpty()) {
                        Toast.makeText(PetListActivity.this, "No pets found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PetListActivity.this, "Failed to load pets", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<Pet>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(PetListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
      private void onPetClick(Pet pet) {
        Intent intent = new Intent(this, PetDetailActivity.class);
        intent.putExtra(PetDetailActivity.EXTRA_PET, pet);
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
