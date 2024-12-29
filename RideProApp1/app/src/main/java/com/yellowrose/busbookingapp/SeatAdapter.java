package com.yellowrose.busbookingapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {
    private List<Seat> seatList;
    private OnSeatSelectedListener listener;

    public interface OnSeatSelectedListener {
        void onSeatSelected(Seat seat, int position);
    }

    public SeatAdapter(List<Seat> seatList, OnSeatSelectedListener listener) {
        this.seatList = seatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Seat seat = seatList.get(position);
        holder.bind(seat, position);
    }

    @Override
    public int getItemCount() {
        return seatList.size();
    }

    class SeatViewHolder extends RecyclerView.ViewHolder {
        TextView seatNumber;
        CardView seatCard;

        SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            seatNumber = itemView.findViewById(R.id.seatNumber);
            seatCard = itemView.findViewById(R.id.seatCard);
        }

        void bind(Seat seat, int position) {
            seatNumber.setText(seat.getNumber());

            // Set background color based on seat status
            int backgroundColor;
            switch (seat.getStatus()) {
                case "booked":
                    backgroundColor = Color.GRAY;
                    break;
                case "selected":
                    backgroundColor = Color.GREEN;
                    break;
                default:
                    backgroundColor = Color.WHITE;
            }
            seatCard.setCardBackgroundColor(backgroundColor);
            itemView.setOnClickListener(v -> listener.onSeatSelected(seat, position));
        }
    }
}