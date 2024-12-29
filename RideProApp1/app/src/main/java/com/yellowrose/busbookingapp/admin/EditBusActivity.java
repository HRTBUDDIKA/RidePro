package com.yellowrose.busbookingapp.admin;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yellowrose.busbookingapp.Bus;
import com.yellowrose.busbookingapp.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditBusActivity extends AppCompatActivity {
    private EditText busNumberInput, routeFromInput, routeToInput, departureDateInput,
            departureTimeInput, priceInput, totalSeatsInput;
    private Button updateBusButton;
    private ProgressBar progressBar;
    private DatabaseReference busRef;
    private String busId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bus); // Reuse add bus layout

        busId = getIntent().getStringExtra("busId");
        busRef = FirebaseDatabase.getInstance()
                .getReference().child("buses").child(busId);

        initializeViews();
        setupDateTimePickers();
        loadBusData();
        setupUpdateButton();
    }

    private void initializeViews() {
        busNumberInput = findViewById(R.id.busNumberInput);
        routeFromInput = findViewById(R.id.routeFromInput);
        routeToInput = findViewById(R.id.routeToInput);
        departureDateInput = findViewById(R.id.departureDateInput);
        departureTimeInput = findViewById(R.id.departureTimeInput);
        priceInput = findViewById(R.id.priceInput);
        totalSeatsInput = findViewById(R.id.totalSeatsInput);
        updateBusButton = findViewById(R.id.addBusButton); // Reuse add button
        updateBusButton.setText("Update Bus");
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupDateTimePickers() {
        departureDateInput.setOnClickListener(v -> showDatePicker());
        departureTimeInput.setOnClickListener(v -> showTimePicker());
    }

    private void loadBusData() {
        progressBar.setVisibility(View.VISIBLE);
        busRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Bus bus = snapshot.getValue(Bus.class);
                if (bus != null) {
                    busNumberInput.setText(bus.getBusNumber());
                    routeFromInput.setText(bus.getRouteFrom());
                    routeToInput.setText(bus.getRouteTo());
                    departureDateInput.setText(bus.getDepartureDate());
                    departureTimeInput.setText(bus.getDepartureTime());
                    priceInput.setText(String.valueOf(bus.getPrice()));
                    totalSeatsInput.setText(String.valueOf(bus.getTotalSeats()));
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditBusActivity.this,
                        "Error loading bus data", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String date = String.format(Locale.US, "%02d/%02d/%d", day, month + 1, year);
                    departureDateInput.setText(date);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(this,
                (view, hour, minute) -> {
                    String time = String.format(Locale.US, "%02d:%02d", hour, minute);
                    departureTimeInput.setText(time);
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true).show();
    }

    private void setupUpdateButton() {
        updateBusButton.setOnClickListener(v -> {
            if (validateInputs()) {
                updateBusInDatabase();
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

    private void updateBusInDatabase() {
        progressBar.setVisibility(View.VISIBLE);
        updateBusButton.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("busNumber", busNumberInput.getText().toString().trim());
        updates.put("routeFrom", routeFromInput.getText().toString().trim());
        updates.put("routeTo", routeToInput.getText().toString().trim());
        updates.put("departureDate", departureDateInput.getText().toString().trim());
        updates.put("departureTime", departureTimeInput.getText().toString().trim());
        updates.put("price", Double.parseDouble(priceInput.getText().toString().trim()));
        updates.put("totalSeats", Integer.parseInt(totalSeatsInput.getText().toString().trim()));

        busRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditBusActivity.this,
                            "Bus updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditBusActivity.this,
                            "Failed to update bus: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    updateBusButton.setEnabled(true);
                });
    }
}