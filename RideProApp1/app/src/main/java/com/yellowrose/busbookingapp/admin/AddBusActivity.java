package com.yellowrose.busbookingapp.admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yellowrose.busbookingapp.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class AddBusActivity extends AppCompatActivity {
    private EditText busNumberInput, routeFromInput, routeToInput, departureDateInput,
            departureTimeInput, priceInput, totalSeatsInput;
    private Button addBusButton;
    private DatabaseReference busRef;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bus);

        busRef = FirebaseDatabase.getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("buses");

        initializeViews();
        setupDateTimePickers();
        setupAddBusButton();
    }

    private void initializeViews() {
        busNumberInput = findViewById(R.id.busNumberInput);
        routeFromInput = findViewById(R.id.routeFromInput);
        routeToInput = findViewById(R.id.routeToInput);
        departureDateInput = findViewById(R.id.departureDateInput);
        departureTimeInput = findViewById(R.id.departureTimeInput);
        priceInput = findViewById(R.id.priceInput);
        totalSeatsInput = findViewById(R.id.totalSeatsInput);
        addBusButton = findViewById(R.id.addBusButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupDateTimePickers() {
        departureDateInput.setOnClickListener(v -> showDatePicker());
        departureTimeInput.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            String date = String.format(Locale.US, "%02d/%02d/%d", day, month + 1, year);
            departureDateInput.setText(date);
        };

        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hour, minute) -> {
            String time = String.format(Locale.US, "%02d:%02d", hour, minute);
            departureTimeInput.setText(time);
        };

        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this, timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), true).show();
    }

    private void setupAddBusButton() {
        addBusButton.setOnClickListener(v -> {
            if (validateInputs()) {
                addBusToDatabase();
            }
        });
    }

    private boolean validateInputs() {
        if (busNumberInput.getText().toString().trim().isEmpty() ||
                routeFromInput.getText().toString().trim().isEmpty() ||
                routeToInput.getText().toString().trim().isEmpty() ||
                departureDateInput.getText().toString().trim().isEmpty() ||
                departureTimeInput.getText().toString().trim().isEmpty() ||
                priceInput.getText().toString().trim().isEmpty() ||
                totalSeatsInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addBusToDatabase() {
        progressBar.setVisibility(View.VISIBLE);
        addBusButton.setEnabled(false);

        String busId = busRef.push().getKey();
        Map<String, Object> busData = new HashMap<>();
        busData.put("busNumber", busNumberInput.getText().toString().trim());
        busData.put("routeFrom", routeFromInput.getText().toString().trim());
        busData.put("routeTo", routeToInput.getText().toString().trim());
        busData.put("departureDate", departureDateInput.getText().toString().trim());
        busData.put("departureTime", departureTimeInput.getText().toString().trim());
        busData.put("price", Double.parseDouble(priceInput.getText().toString().trim()));
        busData.put("totalSeats", Integer.parseInt(totalSeatsInput.getText().toString().trim()));
        busData.put("availableSeats", Integer.parseInt(totalSeatsInput.getText().toString().trim()));
        busData.put("status", "Active");

        busRef.child(busId).setValue(busData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddBusActivity.this, "Bus added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddBusActivity.this, "Failed to add bus: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    addBusButton.setEnabled(true);
                });
    }
}