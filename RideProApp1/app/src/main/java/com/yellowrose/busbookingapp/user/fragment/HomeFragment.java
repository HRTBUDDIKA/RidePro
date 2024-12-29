package com.yellowrose.busbookingapp.user.fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yellowrose.busbookingapp.Bus;
import com.yellowrose.busbookingapp.R;
import com.yellowrose.busbookingapp.user.BusSearchListActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HomeFragment extends Fragment {
    private AutoCompleteTextView fromInput, toInput;
    private TextInputEditText dateInput;
    private Set<String> locations;
    private FirebaseAuth mAuth;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        locations = new HashSet<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize views
        fromInput = rootView.findViewById(R.id.fromInput);
        toInput = rootView.findViewById(R.id.toInput);
        dateInput = rootView.findViewById(R.id.dateInput);
        MaterialButton searchButton = rootView.findViewById(R.id.searchButton);

        // Initialize components
        loadLocationsFromDatabase();
        setupDatePicker();
        searchButton.setOnClickListener(v -> searchBuses());

        return rootView;
    }

    private void loadLocationsFromDatabase() {
        DatabaseReference busRef = FirebaseDatabase
                .getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference()
                .child("buses");

        busRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locations.clear();

                for (DataSnapshot busSnapshot : snapshot.getChildren()) {
                    Bus bus = busSnapshot.getValue(Bus.class);
                    if (bus != null) {
                        locations.add(bus.getRouteFrom().trim());
                        locations.add(bus.getRouteTo().trim());
                    }
                }

                setupAutoComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Error loading locations: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupAutoComplete() {
        if (getContext() == null) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(locations)
        );

        fromInput.setAdapter(adapter);
        toInput.setAdapter(adapter);

        // Set threshold and other properties
        fromInput.setThreshold(1);
        toInput.setThreshold(1);

        // Optional: Enable text filtering
        adapter.setNotifyOnChange(true);
        adapter.getFilter().filter(null);
    }

    private void setupDatePicker() {
        dateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        dateInput.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
    }

    private void searchBuses() {
        String from = Objects.requireNonNull(fromInput.getText()).toString().trim();
        String to = Objects.requireNonNull(toInput.getText()).toString().trim();
        String date = Objects.requireNonNull(dateInput.getText()).toString().trim();

        if (from.isEmpty() || to.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!locations.contains(from) || !locations.contains(to)) {
            Toast.makeText(getContext(), "Please select valid locations from the suggestions", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference busRef = FirebaseDatabase
                .getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference()
                .child("buses");

        busRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Bus> matchingBuses = new ArrayList<>();

                for (DataSnapshot busSnapshot : snapshot.getChildren()) {
                    Bus bus = busSnapshot.getValue(Bus.class);
                    if (bus != null &&
                            bus.getRouteFrom().equalsIgnoreCase(from) &&
                            bus.getRouteTo().equalsIgnoreCase(to) &&
                            bus.getDepartureDate().equals(date) &&
                            bus.getAvailableSeats() > 0) {
                        matchingBuses.add(bus);
                    }
                }

                if (matchingBuses.isEmpty()) {
                    Toast.makeText(getContext(), "No buses found for selected route and date", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getContext(), BusSearchListActivity.class);
                intent.putExtra("buses", (Serializable) matchingBuses);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Error searching buses: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}