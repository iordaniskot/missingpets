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
import com.example.missingpets.data.models.AuthResponse;
import com.example.missingpets.data.models.RegisterRequest;
import com.example.missingpets.databinding.ActivityRegisterBinding;
import com.example.missingpets.utils.PreferenceManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    
    private ActivityRegisterBinding binding;
    private PreferenceManager preferenceManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        preferenceManager = PreferenceManager.getInstance(this);
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRegister.setOnClickListener(v -> performRegister());
        binding.tvSignIn.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }
    
    private void performRegister() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        
        // Validate inputs
        if (!isValidInput(name, email, phone, password, confirmPassword)) {
            return;
        }
        
        // Check terms acceptance
        if (!binding.cbTerms.isChecked()) {
            Toast.makeText(this, getString(R.string.error_terms_not_accepted), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Create request
        RegisterRequest request = new RegisterRequest(name, email, phone, password);
        
        // Make API call
        ApiClient.getApiService().register(request).enqueue(new Callback<AuthResponse>() {
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
                    
                    Toast.makeText(RegisterActivity.this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    showError(getString(R.string.error_registration_failed));
                }
            }
            
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                showError(getString(R.string.error_network));
            }
        });
    }
    
    private boolean isValidInput(String name, String email, String phone, String password, String confirmPassword) {
        // Clear previous errors
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
        
        boolean isValid = true;
        
        // Validate name
        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError(getString(R.string.error_empty_name));
            isValid = false;
        }
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }
        
        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            binding.tilPhone.setError(getString(R.string.error_empty_phone));
            isValid = false;
        }
        
        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_too_short));
            isValid = false;
        }
        
        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_passwords_dont_match));
            isValid = false;
        }
        
        return isValid;
    }
    
    private void setLoading(boolean isLoading) {
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnBack.setEnabled(!isLoading);
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
