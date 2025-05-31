package com.example.missingpets.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.security.GeneralSecurityException;
import java.io.IOException;

public class PreferenceManager {
    private static PreferenceManager instance;
    private SharedPreferences sharedPreferences;
    
    private PreferenceManager(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    Constants.PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular SharedPreferences if encryption fails
            sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        }
    }
    
    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void saveAuthToken(String token) {
        sharedPreferences.edit()
                .putString(Constants.KEY_AUTH_TOKEN, token)
                .apply();
    }
    
    public String getAuthToken() {
        return sharedPreferences.getString(Constants.KEY_AUTH_TOKEN, null);
    }
      public void saveUserInfo(String userId, String email, String name, String phone) {
        sharedPreferences.edit()
                .putString(Constants.KEY_USER_ID, userId)
                .putString(Constants.KEY_USER_EMAIL, email)
                .putString(Constants.KEY_USER_NAME, name)
                .putString(Constants.KEY_USER_PHONE, phone)
                .apply();
    }
    
    public String getUserId() {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null);
    }
    
    public String getUserEmail() {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, null);
    }
      public String getUserName() {
        return sharedPreferences.getString(Constants.KEY_USER_NAME, null);
    }
    
    public String getUserPhone() {
        return sharedPreferences.getString(Constants.KEY_USER_PHONE, null);
    }
    
    public boolean isLoggedIn() {
        return getAuthToken() != null;
    }
      public void clearUserData() {
        sharedPreferences.edit()
                .remove(Constants.KEY_AUTH_TOKEN)
                .remove(Constants.KEY_USER_ID)
                .remove(Constants.KEY_USER_EMAIL)
                .remove(Constants.KEY_USER_NAME)
                .remove(Constants.KEY_USER_PHONE)
                .apply();
    }
}
