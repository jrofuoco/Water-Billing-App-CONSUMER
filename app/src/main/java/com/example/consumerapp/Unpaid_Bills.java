package com.example.consumerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Unpaid_Bills extends AppCompatActivity {
    private static final String SUPABASE_URL = "https://qedemugeyctrrpxkcjpr.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlZGVtdWdleWN0cnJweGtjanByIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTg0MDM5NjYsImV4cCI6MjAzMzk3OTk2Nn0.tgr8odentTg1nG_7XmAUZG6RknKXfFRoOnAcGH1Tp34";

    private TextView accountno, date;

    private int countl;

    public static String readingLastMonthDate;
    public static double amountPayable;

    private static ArrayList <Bills> billsList;
    private RecyclerView recyclerView;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_unpaid_bills);

        date = findViewById(R.id.date);

        recyclerView = findViewById(R.id.recy);
        billsList = new ArrayList<>();

        // Fetch unpaid rows for account B-1
        fetchmeteraccount("B01");
        setAdapter();
        // Find the LinearLayout from the XML

        // Call the method to add 5 buttons dynamically
        Intent intent = getIntent();
        String displayName = intent.getStringExtra("accountno1");

        accountno = findViewById(R.id.accountno);
        accountno.setText(String.valueOf(displayName));

        //SupabaseConnector.fetchallunpaidrows("B-1", linear);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetct(String.valueOf(accountno));
    }

    private void setAdapter() {
        recyclerAdapter adapter = new recyclerAdapter(billsList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    public void onBindViewHolder(@NonNull recyclerAdapter.MyViewHolder holder, int position) {
        // Get the current bill object
        Bills bill = billsList.get(position);

        // Set the reading date to textView5
        holder.billTxt.setText(bill.getReadingLastMonthDate());

        // Set the amount payable to textView6
        holder.amountTxt.setText(String.format("%.2f", bill.getAmountPayable()));
    }

    private static void setBillsInfo(JSONArray jsonArray) {

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject row = jsonArray.getJSONObject(i);
                String readingDate = row.optString("reading_this_month_date", "N/A");
                double amount = row.optDouble("amount_payable", 0.0);

                // Add the data to the bills list
                billsList.add(new Bills(readingDate, amount));

            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void fetchmeteraccount(String accountNo) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                    .newBuilder()
                    .addQueryParameter("select", "reading_this_month_date, amount_payable")
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

                    // Get the latest date
                    String latestDate = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject row = jsonArray.getJSONObject(i);
                        String readingDate = row.optString("reading_this_month_date", null);
                        if (latestDate == null || (readingDate != null && readingDate.compareTo(latestDate) > 0)) {
                            latestDate = readingDate;
                        }
                    }

                    // Now, fetch the data again but filter out the latest date
                    if (latestDate != null) {
                        // Use the latestDate to exclude it in the next query
                        HttpUrl updatedUrl = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                                .newBuilder()
                                .addQueryParameter("select", "reading_this_month_date, amount_payable")
                                .addQueryParameter("account_no", "eq." + accountNo)
                                .addQueryParameter("status", "eq.Unpaid")
                                .addQueryParameter("reading_this_month_date", "lt." + latestDate) // Filter for dates before the latest one
                                .build();

                        Request updatedRequest = new Request.Builder()
                                .url(updatedUrl)
                                .header("apikey", SUPABASE_KEY)
                                .header("Authorization", "Bearer " + SUPABASE_KEY)
                                .build();

                        try (Response updatedResponse = client.newCall(updatedRequest).execute()) {
                            if (updatedResponse.isSuccessful() && updatedResponse.body() != null) {
                                String updatedResponseData = updatedResponse.body().string();
                                JSONArray updatedJsonArray = new JSONArray(updatedResponseData);

                                synchronized (Unpaid_Bills.class) {
                                    setBillsInfo(updatedJsonArray);
                                }

                                // Notify the adapter on the UI thread
                                runOnUiThread(this::setAdapter);
                            } else {
                                Log.e("UnpaidBills", "Error: " + updatedResponse.message());
                            }
                        } catch (IOException | org.json.JSONException e) {
                            e.printStackTrace();
                            Log.e("UnpaidBills", "Error fetching updated data", e);
                        }
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


    public void fetct(String accountNo) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            // Calculate the date one month ago
            LocalDate oneMonthAgo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                oneMonthAgo = LocalDate.now().minusMonths(1);
            }
            String oneMonthAgoStr = null; // Format it as YYYY-MM-DD
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                oneMonthAgoStr = oneMonthAgo.format(DateTimeFormatter.ISO_DATE);
            }

            // Construct the URL with additional filter for overdue bills (older than one month)
            HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                    .newBuilder()
                    .addQueryParameter("select", "reading_last_month_date, amount_payable")
                    .addQueryParameter("account_no", "eq." + accountNo)
                    .addQueryParameter("status", "eq.Unpaid")
                    .addQueryParameter("reading_last_month_date", "lt." + oneMonthAgoStr) // Filter for overdue records
                    .addQueryParameter("order", "reading_last_month_date.asc") // Order by reading_last_month_date in ascending order
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

                    String oldestDate = null;
                    String latestDate = null;

                    if (jsonArray.length() > 0) {
                        oldestDate = jsonArray.getJSONObject(0).optString("reading_last_month_date", null);
                        latestDate = jsonArray.getJSONObject(jsonArray.length() - 1).optString("reading_last_month_date", null);
                    }

                    synchronized (Unpaid_Bills.class) {
                        setBillsInfo(jsonArray);
                        System.out.println("Oldest Date: " + oldestDate + ", Latest Date: " + latestDate);
                    }

                    // Notify the adapter on the UI thread
                    runOnUiThread(this::setAdapter);
                } else {
                    Log.e("UnpaidBills", "Error: " + response.message());
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
                Log.e("UnpaidBills", "Error fetching data", e);
            }
        }).start();
    }

    public void fetchBillingPeriodRange(String accountno) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            // Build the URL to fetch rows with the given account_no and status "Unpaid"
            HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                    .newBuilder()
                    .addQueryParameter("select", "reading_last_month_date") // Only fetch reading_last_month_date
                    .addQueryParameter("account_no", "eq." + accountno)
                    .addQueryParameter("status", "eq.Unpaid") // Filter for "Unpaid" status// Filter for the provided account_no
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
                        // Initialize variables for the oldest and latest dates
                        String oldestDate = null;
                        String latestDate = null;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject row = jsonArray.getJSONObject(i);

                            // Extract the reading_last_month_date from the current row
                            String lastMonthDate = row.optString("reading_last_month_date", null);

                            // Update oldestDate and latestDate
                            if (lastMonthDate != null) {
                                if (oldestDate == null || lastMonthDate.compareTo(oldestDate) < 0) {
                                    oldestDate = lastMonthDate;
                                }
                                if (latestDate == null || lastMonthDate.compareTo(latestDate) > 0) {
                                    latestDate = lastMonthDate;
                                }
                            }
                        }

                        // Log or use the results
                        if (oldestDate != null && latestDate != null) {
                            String range = oldestDate + " - " + latestDate;
                            Log.d("BillingPeriodRange", "Range: " + range);

                            // Example: Update the UI on the main thread
                            runOnUiThread(() -> {
                                date.setText(range); // Assuming 'date' is a TextView to display the range
                            });
                        }
                    } else {
                        Log.d("BillingPeriodRange", "No unpaid billing records found for account_no " + accountno);
                    }
                } else {
                    Log.e("BillingPeriodRange", "Error: " + response.message());
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
                Log.e("BillingPeriodRange", "Error fetching data", e);
            }
        }).start();
    }


}