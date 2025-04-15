package com.example.consumerapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DatabaseConnector {

    private static final String SUPABASE_URL = "https://qedemugeyctrrpxkcjpr.supabase.co";  // Fetch from BuildConfig
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlZGVtdWdleWN0cnJweGtjanByIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTg0MDM5NjYsImV4cCI6MjAzMzk3OTk2Nn0.tgr8odentTg1nG_7XmAUZG6RknKXfFRoOnAcGH1Tp34";  // Fetch from BuildConfig

    // Define the media type for JSON
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Create OkHttpClient with custom timeout settings
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    // Method to create a new OkHttpClient instance
    public static OkHttpClient getClient() {
        return client;
    }

    // Method to perform a GET request
    public static Response performGetRequest(String endpoint) throws IOException {
        String url = SUPABASE_URL + endpoint;
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        return client.newCall(request).execute();
    }

    // Method to perform a POST request
    public static Response performPostRequest(String endpoint, String json) throws IOException {
        String url = SUPABASE_URL + endpoint;
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        return client.newCall(request).execute();
    }

    // Method to perform a PATCH request
    public static Response performPatchRequest(String endpoint, String json) throws IOException {
        String url = SUPABASE_URL + endpoint;
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", API_KEY)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .patch(body)
                .build();

        return client.newCall(request).execute();
    }

    public static void confirmAccount(String connectionNo, int meterNo, String firstName, String lastName, OnAccountConfirmedListener listener) {
        new Thread(() -> {
            String fetchEndpoint = "/rest/v1/meter_connection?select=*";
            try {
                Response fetchResponse = performGetRequest(fetchEndpoint);
                if (!fetchResponse.isSuccessful()) {
                    listener.onError("Error confirming account");
                    return;
                }

                String responseData = fetchResponse.body().string();
                JSONArray jsonArray = new JSONArray(responseData);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String accountNo = jsonObject.getString("accountno_field");
                    int meterNumber = jsonObject.getInt("meter_no");
                    String first_name = jsonObject.getString("first_name");
                    String last_name = jsonObject.getString("last_name");

                    if (accountNo.equals(connectionNo) && meterNumber == meterNo && first_name.equals(firstName) && last_name.equals(lastName)) {
                        listener.onAccountConfirmed();
                        return;
                    }
                }

                listener.onError("Account not found");
            } catch (IOException | JSONException e) {
                listener.onError("Failed to confirm account: " + e.getMessage());
            }
        }).start();
    }

    public interface OnAccountConfirmedListener {
        void onAccountConfirmed();
        void onError(String errorMessage);
    }

}