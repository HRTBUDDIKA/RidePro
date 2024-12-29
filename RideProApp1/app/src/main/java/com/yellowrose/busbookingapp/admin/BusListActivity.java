package com.yellowrose.busbookingapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yellowrose.busbookingapp.Bus;
import com.yellowrose.busbookingapp.BusAdapter;
import com.yellowrose.busbookingapp.R;

import java.util.ArrayList;
import java.util.List;

public class BusListActivity extends AppCompatActivity {
    private RecyclerView busRecyclerView;
    private BusAdapter busAdapter;
    private ProgressBar progressBar;
    private DatabaseReference busRef;
    private List<Bus> busList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_list);

        busRef = FirebaseDatabase.getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("buses");

        initializeViews();
        setupRecyclerView();
        loadBuses();
    }

    private void initializeViews() {
        busRecyclerView = findViewById(R.id.busRecyclerView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        busList = new ArrayList<>();
        busAdapter = new BusAdapter(busList);
        busRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        busRecyclerView.setAdapter(busAdapter);
    }

    private void loadBuses() {
        progressBar.setVisibility(View.VISIBLE);

        busRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                busList.clear();
                for (DataSnapshot busSnapshot : snapshot.getChildren()) {
                    Bus bus = busSnapshot.getValue(Bus.class);
                    if (bus != null) {
                        bus.setId(busSnapshot.getKey());
                        busList.add(bus);
                    }
                }
                busAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BusListActivity.this,
                        "Error loading buses: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}