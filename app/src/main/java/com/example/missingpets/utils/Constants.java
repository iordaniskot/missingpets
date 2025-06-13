package com.example.missingpets.utils;

public class Constants {    
    // Production URLs
    // public static final String BASE_URL = "https://missingpets.rhodesholidaysvillas.com/";
    // public static final String SOCKET_URL = "https://missingpets.rhodesholidaysvillas.com/";
    
    // Development URLs
    // For Android Emulator (use this when testing on emulator)
    public static final String BASE_URL = "http://10.0.2.2:3000/";
    public static final String SOCKET_URL = "http://10.0.2.2:3000/";
    
    // For Physical Device (uncomment these lines and comment the emulator lines above when testing on a real device)
    // public static final String BASE_URL = "http://192.168.2.27:3000/";
    // public static final String SOCKET_URL = "http://192.168.2.27:3000/";
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
