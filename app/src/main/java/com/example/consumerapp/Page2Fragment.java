package com.example.consumerapp;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Page2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Page2Fragment extends Fragment {
    private LineChart lineChart;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String SUPABASE_URL = "https://qedemugeyctrrpxkcjpr.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlZGVtdWdleWN0cnJweGtjanByIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTg0MDM5NjYsImV4cCI6MjAzMzk3OTk2Nn0.tgr8odentTg1nG_7XmAUZG6RknKXfFRoOnAcGH1Tp34";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Page2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Page2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Page2Fragment newInstance(String param1, String param2) {
        Page2Fragment fragment = new Page2Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            System.out.println("DISPLAY NAME from Page2Fragment: " + mParam1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_page2, container, false);

        // Initialize LineChart
        lineChart = rootView.findViewById(R.id.lineChart);

        // Fetch usage history and update chart
        fetchUsageHistory(mParam1);

        return rootView;
    }

    public void updateLineChart(ArrayList<Entry> entries, ArrayList<String> months) {
        // Set up the XAxis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
        lineChart.setScaleEnabled(false);
        lineChart.getXAxis().setAxisMinimum(0);
        lineChart.getXAxis().setGranularity(1f);

        // Create a LineDataSet from the entries
        LineDataSet dataSet = new LineDataSet(entries, "Past Bills");
        dataSet.setValueTextSize(15f);
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.RED);

        // Create LineData and set it to the chart
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // Refresh the chart
    }

    public void fetchUsageHistory(String accountNo) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            // Build the URL for fetching the latest 5 rows from Supabase
            HttpUrl url = HttpUrl.parse(SUPABASE_URL + "/rest/v1/meter_readings")
                    .newBuilder()
                    .addQueryParameter("select", "created_at, reading_this_month, reading_last_month")
                    .addQueryParameter("account_no", "eq." + accountNo)
                    .addQueryParameter("order", "created_at.asc") // Oldest first
                    .addQueryParameter("limit", "5") // Only fetch 5 rows
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

                    // Date formatter to extract the month name
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM", Locale.getDefault());

                    ArrayList<Entry> entries = new ArrayList<>();
                    ArrayList<String> months = new ArrayList<>();

                    // Iterate through the results and prepare the data
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject row = jsonArray.getJSONObject(i);

                        // Extract the necessary fields
                        String createdAt = row.optString("created_at");
                        double readingThisMonth = row.optDouble("reading_this_month", 0.0);
                        double readingLastMonth = row.optDouble("reading_last_month", 0.0);

                        // Calculate consumption (this month - last month)
                        double consumption = readingThisMonth - readingLastMonth;

                        // Format the date to show only the month name (e.g., "January")
                        String monthName = "";
                        try {
                            Date date = inputFormat.parse(createdAt);
                            monthName = outputFormat.format(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            monthName = "Unknown"; // Fallback if parsing fails
                        }

                        // Add the consumption data to the entries list
                        entries.add(new Entry(i, (float) consumption)); // Use the index as X position
                        Log.e("TAG", monthName);
                        months.add(monthName);
                    }

                    // Pass data to UI thread to update the chart
                    new Handler(Looper.getMainLooper()).post(() -> updateLineChart(entries, months));

                } else {
                    Log.e("UsageHistory", "Error: " + response.message());
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
                Log.e("UsageHistory", "Error fetching data", e);
            }
        }).start();
    }

}
