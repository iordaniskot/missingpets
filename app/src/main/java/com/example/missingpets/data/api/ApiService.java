package com.example.missingpets.data.api;

import com.example.missingpets.data.models.AuthRequest;
import com.example.missingpets.data.models.AuthResponse;
import com.example.missingpets.data.models.RegisterRequest;
import com.example.missingpets.data.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    
    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);
    
    @POST("auth/signup")
    Call<AuthResponse> register(@Body RegisterRequest request);
    
    @GET("users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);
}
