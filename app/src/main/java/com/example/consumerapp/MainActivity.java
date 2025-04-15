package com.example.consumerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginBtn;
    private TextView clickHereBtn;

    private TextView forgotBtn;

    private static final String SUPABASE_URL = "https://qedemugeyctrrpxkcjpr.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlZGVtdWdleWN0cnJweGtjanByIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTg0MDM5NjYsImV4cCI6MjAzMzk3OTk2Nn0.tgr8odentTg1nG_7XmAUZG6RknKXfFRoOnAcGH1Tp34";

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        clickHereBtn = findViewById(R.id.clickHereBtn);

        clickHereBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });

        forgotBtn = findViewById(R.id.forgotBtn);
        forgotBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPassword.class);
            startActivity(intent);
        });

        loginBtn = findViewById(R.id.loginBtn);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        loginBtn.setOnClickListener(v -> {
            String usernameInput1 = usernameInput.getText().toString();
            String passwordInput1 = passwordInput.getText().toString();

            if (!usernameInput1.isEmpty()) {
                new Thread(() -> {
                    try {
                        SignInResult result = SupabaseConnector.signIn(usernameInput1, passwordInput1);

                        if (result != null) {
                            String accessToken = result.getAccessToken();
                            String displayName = result.getDisplayName();

                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Sign-in successful!", Toast.LENGTH_SHORT).show();
                                Intent intent1 = new Intent(MainActivity.this, HomePage.class);
                                intent1.putExtra("display_name", displayName);
                                intent1.putExtra("access_token", accessToken);
                                startActivity(intent1);
                            });
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error signing in", Toast.LENGTH_SHORT).show());
                        }
                    } catch (IOException e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error signing in", Toast.LENGTH_SHORT).show());
                        e.printStackTrace();
                    }
                }).start();
            } else {
                Toast.makeText(MainActivity.this, "Please enter an account number", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static class SignInResult {
        private String accessToken;
        private String displayName;

        public SignInResult(String accessToken, String displayName) {
            this.accessToken = accessToken;
            this.displayName = displayName;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private void sendResetPasswordEmail(String email) {
        RequestBody body = RequestBody.create("{\"email\":\"" + email + "\"}", okhttp3.MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/recover")
                .header("apikey", SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to send recovery email", Toast.LENGTH_SHORT).show();
                    Log.e("Error", "Network failure: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Recovery email sent successfully", Toast.LENGTH_SHORT).show());
                } else {
                    String errorMessage = response.body().string(); // Log the response body for debugging
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Failed to send recovery email", Toast.LENGTH_SHORT).show();
                        Log.e("Error", "Response failed: " + errorMessage);
                    });
                }
            }

        });
    }
}