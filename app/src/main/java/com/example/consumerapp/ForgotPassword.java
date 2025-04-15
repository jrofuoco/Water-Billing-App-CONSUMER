package com.example.consumerapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ForgotPassword extends AppCompatActivity {
    private static final String SUPABASE_URL = "https://qedemugeyctrrpxkcjpr.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlZGVtdWdleWN0cnJweGtjanByIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTg0MDM5NjYsImV4cCI6MjAzMzk3OTk2Nn0.tgr8odentTg1nG_7XmAUZG6RknKXfFRoOnAcGH1Tp34";
    private static final String RESET_PASSWORD_URL = "https://qedemugeyctrrpxkcjpr.supabase.co/auth/v1/password/reset";

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize OkHttpClient
        client = new OkHttpClient();

        EditText email = findViewById(R.id.email);
        Button resetPassword = findViewById(R.id.resetPassword);

        // Handle reset password button click
        resetPassword.setOnClickListener(v -> {
            String emailAddress = email.getText().toString().trim();

            if (emailAddress.isEmpty()) {
                Toast.makeText(ForgotPassword.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call Supabase reset password function
            resetPassword(emailAddress);
        });
    }

    private void resetPassword(String emailAddress) {
        // Prepare the JSON body for the request
        String json = "{\"email\":\"" + emailAddress + "\"}";

        // Create a request body
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        // Create the HTTP request
        Request request = new Request.Builder()
                .url(RESET_PASSWORD_URL)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)  // Add your Supabase API key
                .build();

        // Make the request asynchronously
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();

                // Log the response to help with debugging
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ForgotPassword.this, "Password reset email sent", Toast.LENGTH_LONG).show();
                    });
                } else {
                    // Log the response body for debugging
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    Log.e("ForgotPassword", "Error: " + response.message() + " - " + responseBody);

                    runOnUiThread(() -> {
                        Toast.makeText(ForgotPassword.this, "Error: " + response.message(), Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ForgotPassword.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
