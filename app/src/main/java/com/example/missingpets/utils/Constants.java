package com.example.missingpets.utils;

public class Constants {    
    public static final String BASE_URL = "https://missingpets.rhodesholidaysvillas.com/";
    public static final String SOCKET_URL = "https://missingpets.rhodesholidaysvillas.com/";
    public static final long TIMEOUT_SECONDS = 30L;
    public static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
      // SharedPreferences keys
    public static final String PREF_NAME = "MissingPetsPrefs";
    public static final String KEY_AUTH_TOKEN = "auth_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_PHONE = "user_phone";
    
    // Intent extras
    public static final String EXTRA_USER = "extra_user";
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}
