package com.yellowrose.busbookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class BookingDetailsActivity extends AppCompatActivity {
    private TextView statusText, busNumberText, routeText, dateTimeText;
    private TextView seatsText, amountText, paymentIdText;
    private Button viewBillButton;
    private String bookingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        initViews();
        bookingId = getIntent().getStringExtra("bookingId");

        if (bookingId == null) {
            Toast.makeText(this, "Booking not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBookingDetails();
        viewBillButton.setOnClickListener(v -> showDigitalBill());
    }

    private void initViews() {
        statusText = findViewById(R.id.statusText);
        busNumberText = findViewById(R.id.busNumberText);
        routeText = findViewById(R.id.routeText);
        dateTimeText = findViewById(R.id.dateTimeText);
        seatsText = findViewById(R.id.seatsText);
        amountText = findViewById(R.id.amountText);
        paymentIdText = findViewById(R.id.paymentIdText);
        viewBillButton = findViewById(R.id.viewBillButton);
    }

    private void loadBookingDetails() {
        DatabaseReference bookingRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("bookings")
                .child(bookingId);

        bookingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(BookingDetailsActivity.this,
                            "Booking not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Booking booking = snapshot.getValue(Booking.class);
                if (booking != null) {
                    updateUI(booking);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(BookingDetailsActivity.this,
                        "Error loading details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Booking booking) {
        statusText.setText("Status: " + booking.getStatus().toUpperCase());
        busNumberText.setText("Bus: " + booking.getBusId());
//        routeText.setText(booking.getRouteFrom() + " → " + booking.getRouteTo());
//        dateTimeText.setText(booking.getDepartureDate() + ", " + booking.getDepartureTime());
        seatsText.setText("Seats: " + String.join(", ", booking.getSelectedSeats()));
        amountText.setText("Amount: ₹" + booking.getTotalPrice());

//        if (booking.getTransactionId() != null) {
//            paymentIdText.setText("Payment ID: " + booking.getTransactionId());
//        }

        viewBillButton.setEnabled(booking.getStatus().equals("confirmed") ||
                booking.getStatus().equals("pending"));
    }

    private void showDigitalBill() {
        Intent intent = new Intent(this, DigitalBillActivity.class);
        intent.putExtra("bookingId", bookingId);
        intent.putExtra("isAdmin", false);
        startActivity(intent);
    }
}