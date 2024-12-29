package com.yellowrose.busbookingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DigitalBillActivity extends AppCompatActivity {
    private TextView billNumberText;
    private TextView dateText;
    private TextView passengerNameText;
    private TextView busNumberText;
    private TextView routeText;
    private TextView seatsText;
    private TextView amountText;
    private TextView statusText;
    private Button confirmButton;
    private String bookingId;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_bill);

        // Initialize views
        initializeViews();

        // Get booking ID from intent
        bookingId = getIntent().getStringExtra("bookingId");
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        if (bookingId == null) {
            Toast.makeText(this, "Error: Booking not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Digital Bill");

        // Show/hide confirm button based on user role
        confirmButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // Load bill details
        loadBillDetails();

        // Setup confirm button
        confirmButton.setOnClickListener(v -> confirmBill());
    }

    private void initializeViews() {
        billNumberText = findViewById(R.id.billNumberText);
        dateText = findViewById(R.id.dateText);
        passengerNameText = findViewById(R.id.passengerNameText);
        busNumberText = findViewById(R.id.busNumberText);
        routeText = findViewById(R.id.routeText);
        seatsText = findViewById(R.id.seatsText);
        amountText = findViewById(R.id.amountText);
        statusText = findViewById(R.id.statusText);
        confirmButton = findViewById(R.id.confirmButton);
    }

    private void loadBillDetails() {
        DatabaseReference bookingRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("bookings")
                .child(bookingId);

        bookingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(DigitalBillActivity.this,
                            "Booking not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Booking booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    updateBillUI(booking);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DigitalBillActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBillUI(Booking booking) {
        billNumberText.setText("Bill #" + booking.getId().substring(0, 8));
        dateText.setText("Date: " + booking.getBookingDate());
        busNumberText.setText("Bus: " + booking.getBusId());
//        routeText.setText("Route: " + booking.getRouteFrom() + " → " + booking.getRouteTo());
        seatsText.setText("Seats: " + String.join(", ", booking.getSelectedSeats()));
        amountText.setText("Amount: ₹" + booking.getTotalPrice());
        statusText.setText("Status: " + booking.getStatus().toUpperCase());

        // Update confirm button state
        confirmButton.setEnabled("pending".equals(booking.getStatus()));

        // Load passenger name
        loadPassengerName(booking.getUserId());
    }

    private void loadPassengerName(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    passengerNameText.setText("Passenger: " + name);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    private void confirmBill() {
        DatabaseReference bookingRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("bookings")
                .child(bookingId);

        bookingRef.child("status").setValue("confirmed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Bill confirmed successfully",
                            Toast.LENGTH_SHORT).show();
                    confirmButton.setEnabled(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}