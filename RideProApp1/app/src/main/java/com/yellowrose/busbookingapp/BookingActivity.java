package com.yellowrose.busbookingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {
    private RecyclerView seatRecyclerView;
    private SeatAdapter seatAdapter;
    private Bus selectedBus;
    private List<Seat> seatList;
    private List<String> selectedSeats;
    private TextView totalPriceText, selectedSeatsText;
    private MaterialButton confirmBookingButton;
    private double totalPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select Seats");

        // Initialize views
        seatRecyclerView = findViewById(R.id.seatRecyclerView);
        totalPriceText = findViewById(R.id.totalPriceText);
        selectedSeatsText = findViewById(R.id.selectedSeatsText);
        confirmBookingButton = findViewById(R.id.confirmBookingButton);

        // Get bus data from intent
        selectedBus = (Bus) getIntent().getSerializableExtra("bus");
        if (selectedBus == null) {
            Toast.makeText(this, "Error loading bus details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize seat list and selected seats
        selectedSeats = new ArrayList<>();
        initializeSeatList();

        // Setup RecyclerView with GridLayout (5 columns for 2-3 seat arrangement)
        seatRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        seatAdapter = new SeatAdapter(seatList, this::onSeatSelected);
        seatRecyclerView.setAdapter(seatAdapter);

        // Setup confirmation button
        confirmBookingButton.setOnClickListener(v -> confirmBooking());

        // Display bus details
        displayBusDetails();
    }

    private void initializeSeatList() {
        seatList = new ArrayList<>();
        // Create 50 seats (1-50)
        for (int i = 1; i <= 50; i++) {
            Seat seat = new Seat();
            seat.setNumber(String.valueOf(i));
            seat.setStatus("available"); // You might want to check database for booked seats
            seatList.add(seat);
        }
    }

    private void onSeatSelected(Seat seat, int position) {
        if (seat.getStatus().equals("booked")) {
            Toast.makeText(this, "Seat already booked", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedSeats.contains(seat.getNumber())) {
            selectedSeats.remove(seat.getNumber());
            seat.setStatus("available");
        } else {
            selectedSeats.add(seat.getNumber());
            seat.setStatus("selected");
        }

        seatAdapter.notifyItemChanged(position);
        updateBookingDetails();
    }

    private void updateBookingDetails() {
        totalPrice = selectedSeats.size() * selectedBus.getPrice();
        totalPriceText.setText("Total Price: ₹" + totalPrice);
        selectedSeatsText.setText("Selected Seats: " + TextUtils.join(", ", selectedSeats));
        confirmBookingButton.setEnabled(!selectedSeats.isEmpty());
    }

    private void displayBusDetails() {
        TextView busNumberText = findViewById(R.id.busNumberText);
        TextView routeText = findViewById(R.id.routeText);
        TextView timeText = findViewById(R.id.timeText);
        TextView priceText = findViewById(R.id.priceText);

        busNumberText.setText("Bus: " + selectedBus.getBusNumber());
        routeText.setText(selectedBus.getRouteFrom() + " → " + selectedBus.getRouteTo());
        timeText.setText(selectedBus.getDepartureTime() + ", " + selectedBus.getDepartureDate());
        priceText.setText("Price per seat: ₹" + selectedBus.getPrice());
    }


    private void confirmBooking() {

        // Create booking object
        Booking booking = new Booking();
        booking.setBusId(selectedBus.getId());
        booking.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        booking.setSelectedSeats(selectedSeats);
        booking.setTotalPrice(totalPrice);
        booking.setBookingDate(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        booking.setStatus("pending");

        // Save to Firebase
        DatabaseReference bookingRef = FirebaseDatabase.getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference()
                .child("bookings");

        String bookingId = bookingRef.push().getKey();
        booking.setId(bookingId);

        bookingRef.child(bookingId).setValue(booking)
                .addOnSuccessListener(aVoid -> {
                    // Show QR Payment Dialog
                    showPaymentDialog(bookingId, totalPrice);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Booking failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showPaymentDialog(String bookingId, double amount) {
        QRPaymentDialog dialog = QRPaymentDialog.newInstance(bookingId, amount);
        dialog.setPaymentCallback(new QRPaymentDialog.PaymentCallback() {
            @Override
            public void onPaymentSuccess(String transactionId) {
                // Update booking status
                updateBookingStatus(bookingId, "confirmed", transactionId);
                // Update bus seats
                updateBusSeats();

                //Show the digital Bill
                Intent intent = new Intent(BookingActivity.this, DigitalBillActivity.class);
                intent.putExtra("bookingId", bookingId);
                intent.putExtra("isAdmin", false);
                startActivity(intent);
                finish();
            }

            @Override
            public void onPaymentFailed(String error) {
                Toast.makeText(BookingActivity.this,
                        "Payment failed: " + error,
                        Toast.LENGTH_SHORT).show();
                // Update booking status to failed
                updateBookingStatus(bookingId, "failed", null);
            }
        });
        dialog.show(getSupportFragmentManager(), "QRPayment");
    }

    private void updateBookingStatus(String bookingId, String status, String transactionId) {
        DatabaseReference bookingRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("bookings")
                .child(bookingId);

        bookingRef.child("status").setValue(status);
        if (transactionId != null) {
            bookingRef.child("transactionId").setValue(transactionId);
        }
    }

    private void updateBusSeats() {
        DatabaseReference busRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("buses")
                .child(selectedBus.getId());

        int newAvailableSeats = selectedBus.getAvailableSeats() - selectedSeats.size();
        busRef.child("availableSeats").setValue(newAvailableSeats);
    }
}