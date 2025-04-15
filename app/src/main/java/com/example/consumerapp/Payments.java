package com.example.consumerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Payments extends AppCompatActivity {
    private ArrayList<paids> paidsList; // Holds the fetched data
    private paymentsAdapter adapter;
    private RecyclerView recyclerView;

    private static final String SUPABASE_URL = "https://qedemugeyctrrpxkcjpr.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlZGVtdWdleWN0cnJweGtjanByIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTg0MDM5NjYsImV4cCI6MjAzMzk3OTk2Nn0.tgr8odentTg1nG_7XmAUZG6RknKXfFRoOnAcGH1Tp34";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payments);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView and paidsList
        paidsList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerViewPayme);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new paymentsAdapter(paidsList);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        String account = intent.getStringExtra("account");

        // Fetch data for a given account number
        String accountNo = account; // Replace with actual account number
        fetchPayments(accountNo);
    }

    private void fetchPayments(String accountNo) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            // Modify the URL to include the condition for the "status" column
            HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                    .newBuilder()
                    .addQueryParameter("select", "reading_last_month_date, amount_payable")
                    .addQueryParameter("account_no", "eq." + accountNo)  // Filter by account_no
                    .addQueryParameter("status", "eq.Paid")  // Filter by status = "Paid"
                    .build();

            Log.d("Payments", "Request URL: " + url.toString()); // Log the URL

            Request request = new Request.Builder()
                    .url(url)
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    Log.d("Payments", "Response: " + responseData);
                    JSONArray jsonArray = new JSONArray(responseData);
                    parsePaymentsData(jsonArray);
                } else {
                    Log.e("Payments", "Error: " + response.message());
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
                Log.e("Payments", "Error fetching payments", e);
            }
        }).start();
    }


    private void parsePaymentsData(JSONArray jsonArray) {
        runOnUiThread(() -> {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    String date = row.optString("reading_last_month_date", "N/A");
                    String amount = String.valueOf(row.optDouble("amount_payable", 0.0));
                    paidsList.add(new paids(date, amount));
                    Log.d("Payments", "Adding payment - Date: " + date + ", Amount: " + amount); // Log added data
                }
                // Notify adapter about data changes
                adapter.notifyDataSetChanged();
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        });
    }
}
