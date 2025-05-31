package com.example.missingpets.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.missingpets.MainActivity;
import com.example.missingpets.R;
import com.example.missingpets.data.api.ApiClient;
import com.example.missingpets.data.models.AuthRequest;
import com.example.missingpets.data.models.AuthResponse;
import com.example.missingpets.databinding.ActivityLoginBinding;
import com.example.missingpets.utils.PreferenceManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private ActivityLoginBinding binding;
    private PreferenceManager preferenceManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        preferenceManager = PreferenceManager.getInstance(this);
        
        // Check if user is already logged in
        if (preferenceManager.isLoggedIn()) {
            navigateToMain();
            return;
        }
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> performLogin());
        binding.btnSignUp.setOnClickListener(v -> navigateToRegister());
        binding.tvForgotPassword.setOnClickListener(v -> {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void performLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        
        // Validate inputs
        if (!isValidInput(email, password)) {
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Create request
        AuthRequest request = new AuthRequest(email, password);
        
        // Make API call
        ApiClient.getApiService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                      // Save user data
                    preferenceManager.saveAuthToken(authResponse.getToken());
                    if (authResponse.getUser() != null) {
                        preferenceManager.saveUserInfo(
                                authResponse.getUser().getId(),
                                authResponse.getUser().getEmail(),
                                authResponse.getUser().getName(),
                                authResponse.getUser().getPhone()
                        );
                    }
                    
                    Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    showError(getString(R.string.error_login_failed));
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.error_network));
            }
        });
    }
    
    private boolean isValidInput(String email, String password) {
        // Clear previous errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        
        boolean isValid = true;
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }
        
        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        }
        
        return isValid;
    }
    
    private void setLoading(boolean isLoading) {
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnSignUp.setEnabled(!isLoading);
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
