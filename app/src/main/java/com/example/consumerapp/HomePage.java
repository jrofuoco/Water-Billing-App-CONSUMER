package com.example.consumerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomePage extends AppCompatActivity {

    private TextView priceTextView; // TextView to show the payable amount
    private String connectionId; // Holds the connection ID passed from MainActivity
    private Handler handler; // Handler for scheduling tasks
    private Runnable fetchPayableTask; // Task to fetch payable amount
    private static final long POLLING_INTERVAL = 15000; // Polling interval in milliseconds (15 seconds)

    private TextView date, current_date, previous_Reading, current_Reading, total, consumption;
    private Button button, unpaidBiils;
    private ImageButton announcement;
    private TextView accountno, meter_Number;
    private ImageButton payments, settings;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page2);

        consumption = findViewById(R.id.consumption);
        date = findViewById(R.id.date);
        current_date = findViewById(R.id.current_date);
        previous_Reading = findViewById(R.id.previous_Reading);
        current_Reading = findViewById(R.id.current_Reading);
        total = findViewById(R.id.total);

        announcement = findViewById(R.id.announcement);
        announcement.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, Notification.class);
            startActivity(intent);
        });

        priceTextView = findViewById(R.id.priceTextView);
        accountno = findViewById(R.id.accountno); // Ensure this ID matches your XML layout


        // Get the Intent data
        Intent intent = getIntent();
        String displayName = intent.getStringExtra("display_name");
        String token = intent.getStringExtra("access_token");

        viewPager = findViewById(R.id.view_pager);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), displayName));

        SupabaseConnector.fetchLatestReading(displayName, this);
        // Set the display name in the TextView
        if (displayName != null) {
            accountno.setText(displayName);
        } else {
            accountno.setText("Unknown User");
        }

        unpaidBiils = findViewById(R.id.unpaidBiils);
        unpaidBiils.setOnClickListener(v -> {
            Intent intent1 = new Intent(HomePage.this, Unpaid_Bills.class);
            intent1.putExtra("accountno1", displayName);
            System.out.println(displayName);
            startActivity(intent1);  // This doesn't finish the current activity, it stays in the stack
        });

        payments = findViewById(R.id.payments);
        payments.setOnClickListener(v -> {
            Intent intent1 = new Intent(HomePage.this, Payments.class);
            intent1.putExtra("account", displayName);
            startActivity(intent1);  // This doesn't finish the current activity, it stays in the stack
        });

        settings = findViewById(R.id.settings);
        settings.setOnClickListener(v -> {
            Intent intent1 = new Intent(HomePage.this, Settings.class);
            intent1.putExtra("token", token);
            startActivity(intent1);  // This doesn't finish the current activity, it stays in the stack
        });

        // Display a welcome toast
        Toast.makeText(this, "Welcome, " + displayName + "!", Toast.LENGTH_SHORT).show();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Start fetching unpaid amounts asynchronously
        startFetchingUnpaidAmount(displayName);

        // Start periodic fetching of latest readings
        startFetchingReadingsPeriodically(displayName);

        TextView meterNumberTextView = findViewById(R.id.meter_Number);
        SupabaseConnector.fetchmeteraccount(displayName, meterNumberTextView, this);
    }

    private void startFetchingReadingsPeriodically(final String displayName) {
        // Initialize Handler
        handler = new Handler(Looper.getMainLooper());

        // Define the Runnable task
        fetchPayableTask = new Runnable() {
            @Override
            public void run() {
                // Call SupabaseConnector.fetchLatestReading every 15 seconds
                SupabaseConnector.fetchLatestReading(displayName, HomePage.this);

                // Schedule the next fetch after the polling interval (15 seconds)
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        };

        // Start the first fetch immediately
        handler.post(fetchPayableTask);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && fetchPayableTask != null) {
            // Stop polling when the activity is destroyed
            handler.removeCallbacks(fetchPayableTask);
        }
    }

    public void updateReadingsAndAmount(String readingThisMonthDate, String readingLastMonthDate, double readingThisMonth, double readingLastMonth, double amountPayable) {
        // Update the UI with the fetched data
        date.setText(String.valueOf(readingThisMonthDate) + " - " + String.valueOf(readingLastMonthDate));
        current_date.setText(String.valueOf(readingLastMonthDate));
        previous_Reading.setText(String.valueOf(readingLastMonth));
        current_Reading.setText(String.valueOf(readingThisMonth));
        consumption.setText(String.valueOf(readingThisMonth - readingLastMonth));
        total.setText(String.valueOf(amountPayable));
        priceTextView.setText(String.valueOf(amountPayable));
    }

    private void startFetchingUnpaidAmount(final String accountNo) {
        // Using Executor to fetch data asynchronously
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Fetch and sum the unpaid amounts
                double totalAmount = SupabaseConnector.fetchAndSumUnpaidAmounts(accountNo);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        priceTextView.setText(String.valueOf(totalAmount));
                    }
                });
            }
        });
    }
}
