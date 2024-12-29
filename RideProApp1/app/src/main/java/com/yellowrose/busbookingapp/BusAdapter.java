package com.yellowrose.busbookingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yellowrose.busbookingapp.admin.EditBusActivity;

import java.util.List;

public class BusAdapter extends RecyclerView.Adapter<BusAdapter.BusViewHolder> {
    private final List<Bus> busList;
    private final DatabaseReference busRef;

    public BusAdapter(List<Bus> busList) {
        this.busList = busList;
        this.busRef = FirebaseDatabase.getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("buses");
    }

    @NonNull
    @Override
    public BusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bus, parent, false);
        return new BusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BusViewHolder holder, int position) {
        Bus bus = busList.get(position);
        holder.busNumberText.setText(bus.getBusNumber());
        holder.routeText.setText(String.format("%s to %s", bus.getRouteFrom(), bus.getRouteTo()));
        holder.departureDateText.setText(bus.getDepartureDate());
        holder.departureTimeText.setText(bus.getDepartureTime());
        holder.seatsText.setText(String.format("Available Seats: %d/%d",
                bus.getAvailableSeats(), bus.getTotalSeats()));
        holder.priceText.setText(String.format("Price: $%.2f", bus.getPrice()));

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditBusActivity.class);
            intent.putExtra("busId", bus.getId());
            v.getContext().startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Bus")
                    .setMessage("Are you sure you want to delete this bus?")
                    .setPositiveButton("Delete", (dialog, which) ->
                            busRef.child(bus.getId()).removeValue())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return busList.size();
    }

    static class BusViewHolder extends RecyclerView.ViewHolder {
        TextView busNumberText, routeText, departureDateText,
                departureTimeText, seatsText, priceText;
        ImageButton editButton, deleteButton;

        public BusViewHolder(@NonNull View itemView) {
            super(itemView);
            busNumberText = itemView.findViewById(R.id.busNumberText);
            routeText = itemView.findViewById(R.id.routeText);
            departureDateText = itemView.findViewById(R.id.departureDateText);
            departureTimeText = itemView.findViewById(R.id.departureTimeText);
            seatsText = itemView.findViewById(R.id.seatsText);
            priceText = itemView.findViewById(R.id.priceText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}