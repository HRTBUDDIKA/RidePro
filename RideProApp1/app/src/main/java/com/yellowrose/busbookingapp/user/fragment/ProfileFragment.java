package com.yellowrose.busbookingapp.user.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yellowrose.busbookingapp.R;
import android.content.Intent;
//import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

//import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yellowrose.busbookingapp.User;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {
    private ImageView profileImage;
    private TextView userIdText, joinedDateText, bookingsCountText;
    private TextInputEditText nameInput, emailInput, phoneInput, addressInput;
    private MaterialButton updateButton, changePasswordButton;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private String userId;
    private View rootView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("users").child(userId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeViews();
//        setupToolbar();
        loadUserProfile();
        setupClickListeners();

        return rootView;
    }

    private void initializeViews() {
        profileImage = rootView.findViewById(R.id.profileImage);
        userIdText = rootView.findViewById(R.id.userIdText);
        joinedDateText = rootView.findViewById(R.id.joinedDateText);
        bookingsCountText = rootView.findViewById(R.id.bookingsCountText);
        nameInput = rootView.findViewById(R.id.nameInput);
        emailInput = rootView.findViewById(R.id.emailInput);
        phoneInput = rootView.findViewById(R.id.phoneInput);
        updateButton = rootView.findViewById(R.id.updateButton);
        changePasswordButton = rootView.findViewById(R.id.changePasswordButton);
    }

//    private void setupToolbar() {
//        Toolbar toolbar = rootView.findViewById(R.id.toolbar);
//        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        if (activity != null) {
//            activity.setSupportActionBar(toolbar);
//            if (activity.getSupportActionBar() != null) {
//                activity.getSupportActionBar().setTitle("My Profile");
//                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            }
//            toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
//        }
//    }

    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        nameInput.setText(user.getName());
                        emailInput.setText(user.getEmail());
                        phoneInput.setText(user.getPhone());
                        userIdText.setText("User ID: " + userId.substring(0, 8));
                        loadBookingsCount();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Error loading profile: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadBookingsCount() {
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance()
                .getReference().child("bookings");

        bookingsRef.orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        bookingsCountText.setText("Total Bookings: " + count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        bookingsCountText.setText("Total Bookings: 0");
                    }
                });
    }

    private void setupClickListeners() {
        profileImage.setOnClickListener(v -> selectImage());

        updateButton.setOnClickListener(v -> updateProfile());

        changePasswordButton.setOnClickListener(v -> {
            String email = mAuth.getCurrentUser().getEmail();
            if (email != null && getContext() != null) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                                "Password reset email sent", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> Toast.makeText(getContext(),
                                "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 100);
    }

    private void updateProfile() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Name and Phone are required", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        User updatedUser = new User();
        updatedUser.setName(name);
        updatedUser.setEmail(emailInput.getText().toString().trim());
        updatedUser.setPhone(phone);

        userRef.setValue(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
