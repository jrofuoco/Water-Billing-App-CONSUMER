package com.example.consumerapp;


import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Notification extends AppCompatActivity {

    private static TextView a;
    private static TextView b;
    private static TextView c;
    private static TextView d;
    private static TextView e;
    private static TextView f;
    private static TextView g;

    private static int countl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        SupabaseConnector.fetchAnnouncements();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        a = findViewById(R.id.a);
        b = findViewById(R.id.b);
        c = findViewById(R.id.c);
        d = findViewById(R.id.d);
        e = findViewById(R.id.e);
        f = findViewById(R.id.f);
        g = findViewById(R.id.g);


    }

    @Override
    protected void onResume() {
        super.onResume();
        countl = 0; // Reset the counter whenever the activity is resumed
    }

    public static void Populate(String title, String content) {
        countl = countl + 1;

        // Create a styled SpannableString for the title
        SpannableString styledTitle = new SpannableString(title);
        styledTitle.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledTitle.setSpan(new ForegroundColorSpan(Color.BLUE), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledTitle.setSpan(new RelativeSizeSpan(1.2f), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 1.2x size of normal text

        // Append the content to the title
        SpannableStringBuilder combinedText = new SpannableStringBuilder();
        combinedText.append(styledTitle).append("\n").append(content);

        if (countl == 1) {
            a.setText(combinedText);
        } else if (countl == 2) {
            b.setText(combinedText);
        } else if (countl == 3) {
            c.setText(combinedText);;
        } else if (countl == 4) {
            d.setText(combinedText);
        } else if (countl == 5) {
            e.setText(combinedText);
        } else if (countl == 6) {
            f.setText(combinedText);
        } else if (countl == 5) {
            g.setText(combinedText);
        } else {
            // Optionally handle the case when countl exceeds 5
            Log.d("Reading", "Exceeded the maximum number of TextViews.");
        }
    }
}