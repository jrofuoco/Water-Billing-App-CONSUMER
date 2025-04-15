package com.example.consumerapp;

import static com.example.consumerapp.DatabaseConnector.performPostRequest;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupabaseConnector {

    private static final String SUPABASE_URL = "https://qedemugeyctrrpxkcjpr.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlZGVtdWdleWN0cnJweGtjanByIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTg0MDM5NjYsImV4cCI6MjAzMzk3OTk2Nn0.tgr8odentTg1nG_7XmAUZG6RknKXfFRoOnAcGH1Tp34";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public static void checkAccount(String accountNo, String firstName, String lastName, int meterNo, String email, String password) {
        OkHttpClient client = new OkHttpClient();

        String tableName = "meter_connection";

        // Construct the URL for the Supabase table
        HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/" + tableName)
                .newBuilder()
                .addQueryParameter("select", "accountno_field,first_name,last_name,meter_no")
                .addQueryParameter("accountno_field", "eq." + accountNo) // Filter by account number
                .build();

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .build();

        // Execute the request
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    System.out.println("Fetched Data: " + responseData);

                    // Parse JSON response
                    JSONArray jsonArray = new JSONArray(responseData);
                    if (jsonArray.length() > 0) {
                        JSONObject row = jsonArray.getJSONObject(0);
                        String fetchedFirstName = row.getString("first_name");
                        String fetchedLastName = row.getString("last_name");
                        String fetchedMeterNo = row.getString("meter_no");

                        // Check if data matches
                        boolean isMatch = fetchedFirstName.trim().equalsIgnoreCase(firstName.trim()) &&
                                fetchedLastName.trim().equalsIgnoreCase(lastName.trim()) &&
                                Integer.parseInt(fetchedMeterNo) == meterNo;

                        System.out.println("Match Found: " + isMatch);

                        if (isMatch) {
                            // Perform sign up if match is found
                            try {
                                Response signUpResponse = signUp(email, password, accountNo);
                                if (signUpResponse.isSuccessful()) {
                                    System.out.println("Sign up successful: " + signUpResponse.body().string());
                                    insert(accountNo, email);
                                } else {
                                    System.err.println("Sign up failed: " + signUpResponse.code() + " - " + signUpResponse.message());
                                    String responseBody = signUpResponse.body().string();
                                    System.err.println("Error details: " + responseBody);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("No matching data found.");
                    }
                } else {
                    System.err.println("Request failed: " + response.code() + " - " + response.message());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static Response signUp(String email, String password, String displayName) throws IOException {
        String endpoint = "/auth/v1/signup";
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"data\":{\"display_name\":\"" + displayName + "\"}}";
        return performPostRequest(endpoint, json);
    }

    public static Response updatePassword(String newPassword, String accessToken) throws IOException {
        String endpoint = "/auth/v1/user";
        String json = "{\"password\":\"" + newPassword + "\"}";

        // Make sure to pass the Authorization token (access token) in the header for user authentication.
        return performPutRequest(endpoint, json, accessToken);
    }

    public static Response performPutRequest(String endpoint, String json, String accessToken) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Build the URL to the endpoint
        HttpUrl url = HttpUrl.parse(SUPABASE_URL + endpoint);

        // Create the request body
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

        // Create the request with Authorization header and body
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .header("Authorization", "Bearer " + accessToken)  // Add the user access token in header
                .build();

        return client.newCall(request).execute();
    }



    public static Response insert(String accountNo, String email) throws IOException {
        String endpoint = "/rest/v1/meter_connection";
        String json = "{\"email\":\"" + email + "\"}";

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);

        HttpUrl url = HttpUrl.parse(SUPABASE_URL + endpoint)
                .newBuilder()
                .addQueryParameter("accountno_field", "eq." + accountNo) // Filter by account number
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .method("PATCH", body) // Use PATCH to update existing record
                .build();

        return client.newCall(request).execute();
    }

    public static MainActivity.SignInResult signIn(String email, String password) throws IOException {
        String endpoint = "/auth/v1/token?grant_type=password";
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";

        Response response = performPostRequest(endpoint, json);

        if (response.isSuccessful()) {
            try {
                String responseData = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseData);

                String accessToken = jsonResponse.optString("access_token");
                if (accessToken == null || accessToken.isEmpty()) {
                    System.err.println("No access token in response.");
                    return null;
                }

                JSONObject userData = jsonResponse.getJSONObject("user");
                JSONObject userMetadata = userData.getJSONObject("user_metadata");
                String displayName = userMetadata.getString("display_name");

                return new MainActivity.SignInResult(accessToken, displayName);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error parsing response: " + e.getMessage());
            }
        } else {
            System.err.println("Sign-in failed: " + response.code() + " - " + response.message());
        }

        return null;
    }


    public static double fetchAndSumUnpaidAmounts(String accountNo) {
        OkHttpClient client = new OkHttpClient();
        double totalAmount = 0.0;

        // Construct the URL to fetch unpaid amounts for a given account number
        HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                .newBuilder()
                .addQueryParameter("select", "amount_payable")  // Fetch only the amount_payable column
                .addQueryParameter("account_no", "eq." + accountNo)  // Filter by account_no
                .addQueryParameter("status", "eq.Unpaid")  // Filter by status "Unpaid"
                .build();

        // Build the request with appropriate headers
        Request request = new Request.Builder()
                .url(url)
                .header("apikey", SUPABASE_KEY)  // API key for Supabase
                .header("Authorization", "Bearer " + SUPABASE_KEY)  // Bearer token for authentication
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseData = response.body().string();
                System.out.println("Response Data: " + responseData);  // Log the entire response for debugging

                JSONArray jsonArray = new JSONArray(responseData);

                // Verify the number of rows returned
                System.out.println("Number of records: " + jsonArray.length());  // Log the number of rows

                // Iterate through the JSON array and sum up the unpaid amounts
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    double amount = row.optDouble("amount_payable", 0.0);
                    totalAmount += amount;

                    // Log each amount for debugging
                    System.out.println("Amount for row " + i + ": " + amount);
                }
            } else {
                System.err.println("Request failed: " + response.code() + " - " + response.message());
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        System.out.println("Total unpaid amount: " + totalAmount);  // Log the final total amount
        return totalAmount;  // Return the sum of unpaid amounts
    }

    public static void fetchmeteraccount(final String accountNo, final TextView meterNumberTextView, final Activity activity) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_connection")
                    .newBuilder()
                    .addQueryParameter("select", "meter_no")
                    .addQueryParameter("accountno_field", "eq." + accountNo)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseData);

                    if (jsonArray.length() > 0) {
                        JSONObject row = jsonArray.getJSONObject(0);
                        final String meterNo = row.getString("meter_no");

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                meterNumberTextView.setText(meterNo);
                            }
                        });
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                meterNumberTextView.setText("No meter number found");
                            }
                        });
                    }
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            meterNumberTextView.setText("Failed to fetch meter number");
                        }
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void fetchLatestReading(final String accountno, final HomePage homePage) {
        OkHttpClient client = new OkHttpClient();

        // Construct the URL for the Supabase table
        HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                .newBuilder()
                .addQueryParameter("select", "reading_this_month_date, reading_last_month_date, reading_this_month, reading_last_month, amount_payable")
                .addQueryParameter("account_no", "eq." + accountno)  // Filter by account_no
                .addQueryParameter("status", "eq.Unpaid")           // Filter for status = "Unpaid"
                .addQueryParameter("order", "created_at.desc")      // Sort by created_at in descending order
                .addQueryParameter("limit", "1")                   // Limit to the most recent record
                .build();

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .build();

        // Execute the request in a background thread
        new Thread(() -> {
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseData);

                    if (jsonArray.length() > 0) {
                        JSONObject row = jsonArray.getJSONObject(0);

                        // Extract data from JSON response
                        String readingThisMonthDate = row.getString("reading_this_month_date");
                        String readingLastMonthDate = row.getString("reading_last_month_date");
                        double readingThisMonth = row.optDouble("reading_this_month", 0.0);
                        double readingLastMonth = row.optDouble("reading_last_month", 0.0);
                        double amountPayable = row.optDouble("amount_payable", 0.0);

                        homePage.runOnUiThread(() -> {
                            homePage.updateReadingsAndAmount(
                                    readingThisMonthDate,
                                    readingLastMonthDate,
                                    readingThisMonth,
                                    readingLastMonth,
                                    amountPayable
                            );
                        });
                    } else {
                        // Handle case where no unpaid readings are found
                        homePage.runOnUiThread(() ->
                                Toast.makeText(homePage, "No unpaid readings found.", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    // Log the error response
                    System.err.println("Response Code: " + response.code());
                    System.err.println("Response Message: " + response.message());
                    if (response.body() != null) {
                        System.err.println("Response Body: " + response.body().string());
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void fetchmeteraccount(String accountNo) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            // Build the URL for fetching the data from Supabase
            HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                    .newBuilder()
                    .addQueryParameter("select", "reading_last_month_date, amount_payable")
                    .addQueryParameter("account_no", "eq." + accountNo)
                    .addQueryParameter("status", "eq.Unpaid")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONArray jsonArray = new JSONArray(responseData);

                    // Iterate through the results and log the required fields
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject row = jsonArray.getJSONObject(i);

                        // Extract and log the necessary fields
                        String readingLastMonthDate = row.optString("reading_last_month_date");
                        double amountPayable = row.optDouble("amount_payable", 0.0);

                        Log.d("UnpaidBills", "Reading Date: " + readingLastMonthDate + ", Amount Payable: " + amountPayable);
                        //Unpaid_Bills.values(readingLastMonthDate, amountPayable);

                    }
                } else {
                    Log.e("UnpaidBills", "Error: " + response.message());
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
                Log.e("UnpaidBills", "Error fetching data", e);
            }
        }).start();
    }

    private static final String ANNOUNCEMENTS_TABLE = "announcement";

    public static void fetchAnnouncements() {
        OkHttpClient client = new OkHttpClient();

        // Construct the URL for the API request
        String url = SUPABASE_URL + "/rest/v1/" + ANNOUNCEMENTS_TABLE + "?status=eq.Active";

        // Create the request
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .header("apikey", SUPABASE_KEY)
                .build();

        // Execute the request on a background thread
        new Thread(() -> {
            try {
                // Execute the request and get the response
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    // Process the response (e.g., parse JSON data)
                    String responseData = response.body().string();
                    // Parse the response to get the announcement details
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Announcement>>() {}.getType();
                    List<Announcement> announcements = gson.fromJson(responseData, listType);

                    // Log or use the fetched data
                    for (Announcement announcement : announcements) {
                        // You can print the announcements to Logcat or process them as needed
                        Log.d("FetchedAnnouncement", "Title: " + announcement.getTitle() + ", Content: " + announcement.getContent());
                        Notification.Populate(announcement.getTitle(), announcement.getContent());
                    }
                } else {
                    // Handle the error response
                    Log.e("SupabaseConnector", "Error fetching announcements: " + response.message());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

}