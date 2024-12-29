package com.yellowrose.busbookingapp.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.yellowrose.busbookingapp.BookingActivity;
import com.yellowrose.busbookingapp.Bus;
import com.yellowrose.busbookingapp.R;

import java.util.List;

public class BusSearchListActivity extends AppCompatActivity {

private RecyclerView busRecyclerView;
    private BusAdapter busAdapter;
    private List<Bus> busList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_search_list);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize RecyclerView
        busRecyclerView = findViewById(R.id.busRecyclerView);
        busRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get buses from intent
        busList = (List<Bus>) getIntent().getSerializableExtra("buses");

        if (busList == null || busList.isEmpty()) {
            findViewById(R.id.noBusesFound).setVisibility(View.VISIBLE);
            return;
        }

        // Setup adapter
        busAdapter = new BusAdapter(busList, bus -> {
            Intent intent = new Intent(this, BookingActivity.class);
            intent.putExtra("bus", bus);
            startActivity(intent);
        });
        busRecyclerView.setAdapter(busAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Bus Adapter
    private static class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusViewHolder> {
        private List<Bus> busList;
        private OnBusClickListener listener;

        public BusAdapter(List<Bus> busList, OnBusClickListener listener) {
            this.busList = busList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public BusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_item_bus, parent, false);
            return new BusViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BusViewHolder holder, int position) {
            Bus bus = busList.get(position);
            holder.bind(bus, listener);
        }

        @Override
        public int getItemCount() {
            return busList.size();
        }

        static class BusViewHolder extends RecyclerView.ViewHolder {
            TextView busNumberText, routeText, timeText, priceText, seatsText;
            MaterialButton bookButton;

            BusViewHolder(@NonNull View itemView) {
                super(itemView);
                busNumberText = itemView.findViewById(R.id.busNumberText);
                routeText = itemView.findViewById(R.id.routeText);
                timeText = itemView.findViewById(R.id.timeText);
                priceText = itemView.findViewById(R.id.priceText);
                seatsText = itemView.findViewById(R.id.seatsText);
                bookButton = itemView.findViewById(R.id.bookButton);
            }

            void bind(Bus bus, OnBusClickListener listener) {
                busNumberText.setText(bus.getBusNumber());
                routeText.setText(bus.getRouteFrom() + " → " + bus.getRouteTo());
                timeText.setText(bus.getDepartureTime() + ", " + bus.getDepartureDate());
                priceText.setText("₹" + bus.getPrice());
                seatsText.setText(bus.getAvailableSeats() + " seats available");
                bookButton.setOnClickListener(v -> listener.onBusClick(bus));
            }
        }

        interface OnBusClickListener {
            void onBusClick(Bus bus);
        }
    }
}