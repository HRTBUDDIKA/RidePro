package com.yellowrose.busbookingapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRPaymentDialog extends DialogFragment {
    private static final String ARG_BOOKING_ID = "booking_id";
    private static final String ARG_AMOUNT = "amount";
    private String bookingId;
    private double amount;
    private PaymentCallback paymentCallback;

    public interface PaymentCallback {
        void onPaymentSuccess(String transactionId);
        void onPaymentFailed(String error);
    }

    public static QRPaymentDialog newInstance(String bookingId, double amount) {
        QRPaymentDialog fragment = new QRPaymentDialog();
        Bundle args = new Bundle();
        args.putString(ARG_BOOKING_ID, bookingId);
        args.putDouble(ARG_AMOUNT, amount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingId = getArguments().getString(ARG_BOOKING_ID);
            amount = getArguments().getDouble(ARG_AMOUNT);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_qr_payment, null);

        ImageView qrImageView = view.findViewById(R.id.qrImageView);
        TextView amountText = view.findViewById(R.id.amountText);
        Button verifyButton = view.findViewById(R.id.verifyButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        // Set amount
        amountText.setText(String.format("Amount to Pay: ₹%.2f", amount));

        // Generate QR code
        generateQRCode(qrImageView);

        // Setup buttons
        verifyButton.setOnClickListener(v -> verifyPayment());
        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view)
                .setTitle("Scan QR to Pay");

        return builder.create();
    }

    private void generateQRCode(ImageView qrImageView) {
        try {
            // Create payment data
            String paymentData = String.format("booking_id=%s&amount=%.2f", bookingId, amount);

            // Generate QR code
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(paymentData, BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            qrImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void verifyPayment() {
        // Create a payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setBookingId(bookingId);
        transaction.setAmount(amount);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setStatus("processing");

        // Save to Firebase
        DatabaseReference paymentRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("payments");

        String transactionId = paymentRef.push().getKey();
        transaction.setTransactionId(transactionId);



//        paymentRef.child(transactionId).setValue(transaction)
//                .addOnSuccessListener(aVoid -> {
//                    // Start payment verification
//                    startPaymentVerification(transactionId);
//                })
//                .addOnFailureListener(e -> {
//                    if (paymentCallback != null) {
//                        paymentCallback.onPaymentFailed(e.getMessage());
//                    }
//                    dismiss();
//                });
    }

    private void startPaymentVerification(String transactionId) {
        // Simulate payment verification (In real app, this would check payment gateway)
        DatabaseReference transactionRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("payments")
                .child(transactionId);

        transactionRef.child("status").setValue("completed")
                .addOnSuccessListener(aVoid -> {
                    if (paymentCallback != null) {
                        paymentCallback.onPaymentSuccess(transactionId);
                    }
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    if (paymentCallback != null) {
                        paymentCallback.onPaymentFailed(e.getMessage());
                    }
                    dismiss();
                });
    }

    public void setPaymentCallback(PaymentCallback callback) {
        this.paymentCallback = callback;
    }
}