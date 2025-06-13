package com.example.missingpets.data.api;

import com.example.missingpets.data.models.Pet;
import com.example.missingpets.data.models.PetDeserializer;
import com.example.missingpets.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static Retrofit retrofit;
    private static ApiService apiService;
    
    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(ApiService.class);
        }
        return apiService;
    }
    
    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // Create OkHttp client
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build();
            
            // Create Gson instance with custom deserializers
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .registerTypeAdapter(Pet.class, new PetDeserializer())
                    .create();
            
            // Create Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
