package com.example.missingpets.data.api;

import com.example.missingpets.data.models.AuthRequest;
import com.example.missingpets.data.models.AuthResponse;
import com.example.missingpets.data.models.Pet;
import com.example.missingpets.data.models.PetResponse;
import com.example.missingpets.data.models.Report;
import com.example.missingpets.data.models.ReportsResponse;
import com.example.missingpets.data.models.RegisterRequest;
import com.example.missingpets.data.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    
    // Authentication endpoints
    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);
    
    @POST("auth/signup")
    Call<AuthResponse> register(@Body RegisterRequest request);
    
    @GET("users/me")
    Call<User> getCurrentUser(@Header("Authorization") String token);
    
    // Pet endpoints
    @GET("pets")
    Call<List<Pet>> getAllPets(@Header("Authorization") String token);
    
    @GET("pets")
    Call<List<Pet>> getPetsByStatus(@Header("Authorization") String token, @Query("status") String status);
    
    @GET("pets/{id}")
    Call<Pet> getPetById(@Header("Authorization") String token, @Path("id") String petId);
      @POST("pets")
    Call<PetResponse> createPet(@Header("Authorization") String token, @Body Pet pet);
      @PUT("pets/{id}")
    Call<Pet> updatePet(@Header("Authorization") String token, @Path("id") String petId, @Body Pet pet);
    
    @GET("pets/search")
    Call<List<Pet>> searchPets(@Header("Authorization") String token, 
                               @Query("q") String query,
                               @Query("type") String type,
                               @Query("status") String status,
                               @Query("location") String location);    // Report endpoints
    @POST("reports")
    Call<Report> createReport(@Header("Authorization") String token, @Body Report report);
    
    @GET("reports")
    Call<ReportsResponse> getReports(@Header("Authorization") String token);
    
    @GET("reports")
    Call<ReportsResponse> getReportsByStatus(@Header("Authorization") String token, @Query("status") String status);
      @GET("reports/{id}")
    Call<Report> getReportById(@Header("Authorization") String token, @Path("id") String reportId);
    
    // Geospatial search for reports within radius
    @GET("reports")
    Call<ReportsResponse> getReportsNearLocation(@Header("Authorization") String token,
                                                @Query("lat") double latitude,
                                                @Query("lng") double longitude,
                                                @Query("radius") int radiusInMeters,
                                                @Query("status") String status);
}
