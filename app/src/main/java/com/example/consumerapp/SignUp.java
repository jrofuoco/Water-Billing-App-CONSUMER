package com.example.consumerapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUp extends AppCompatActivity {

    private ImageButton backBtn2;
    private Button registerBtn;
    private EditText firstname, lastName, connectionno, meterno, email, password, confirmpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        backBtn2 = findViewById(R.id.backBtn2);
        registerBtn = findViewById(R.id.registerBtn);

        firstname = findViewById(R.id.firstname); // Add this ID in your XML
        lastName = findViewById(R.id.lastName); // Add this ID in your XML
        connectionno = findViewById(R.id.connectionno); // Add this ID in your XML
        meterno = findViewById(R.id.meterno); // Add this ID in your XML
        email = findViewById(R.id.email); // Add this ID in your XML
        password = findViewById(R.id.password); // Add this ID in your XML
        confirmpassword = findViewById(R.id.confirmpassword); // Add this ID in your XML

        // Back button click listener
        backBtn2.setOnClickListener(v -> onBackPressed());

        // Register button click listener
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ensure the fields are not null
                if (validateInputs()) {
                    String firstname1 = firstname.getText().toString();
                    String lastName1 = lastName.getText().toString();
                    String connectionno1 = connectionno.getText().toString();
                    String meterNoInput = meterno.getText().toString();
                    String emai1 = email.getText().toString();
                    String password1 = password.getText().toString();
                    String confirmpassword1 = confirmpassword.getText().toString();

                    // Parse meter number safely
                    int meterno1 = 0;
                    try {
                        meterno1 = Integer.parseInt(meterNoInput);
                    } catch (NumberFormatException e) {
                        meterno.setError("Invalid meter number");
                        return;
                    }

                    // Call SupabaseConnector
                    SupabaseConnector connector = new SupabaseConnector();
                    SupabaseConnector.checkAccount(connectionno1, firstname1, lastName1, meterno1, emai1, password1);
                }
            }
        });
    }

    // Validate inputs
    private boolean validateInputs() {
        if (firstname.getText().toString().isEmpty()) {
            firstname.setError("First name is required");
            return false;
        }
        if (lastName.getText().toString().isEmpty()) {
            lastName.setError("Last name is required");
            return false;
        }
        if (connectionno.getText().toString().isEmpty()) {
            connectionno.setError("Connection number is required");
            return false;
        }
        if (meterno.getText().toString().isEmpty()) {
            meterno.setError("Meter number is required");
            return false;
        }
        return true;
    }
}
