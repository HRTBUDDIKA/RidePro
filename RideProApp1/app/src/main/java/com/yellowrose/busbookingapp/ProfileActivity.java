package com.yellowrose.busbookingapp;

import android.content.Intent;
//import android.net.Uri;
import android.os.Bundle;
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
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ImageView profileImage;
    private TextView userIdText, joinedDateText, bookingsCountText;
    private TextInputEditText nameInput, emailInput, phoneInput, addressInput;
    private MaterialButton updateButton, changePasswordButton;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
//    private StorageReference storageRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference().child("users").child(userId);
//        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Initialize views
        initializeViews();
        setupToolbar();
        loadUserProfile();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        profileImage = findViewById(R.id.profileImage);
        userIdText = findViewById(R.id.userIdText);
        joinedDateText = findViewById(R.id.joinedDateText);
        bookingsCountText = findViewById(R.id.bookingsCountText);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        updateButton = findViewById(R.id.updateButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true) ;
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // Set user details
                        nameInput.setText(user.getName());
                        emailInput.setText(user.getEmail());
                        phoneInput.setText(user.getPhone());
                        userIdText.setText("User ID: " + userId.substring(0, 8));

//                        // Load profile image if exists
//                        if (user.getProfileImage() != null) {
//                            Glide.with(ProfileActivity.this)
//                                    .load(user.getProfileImage())
//                                    .placeholder(R.drawable.default_profile)
//                                    .into(profileImage);
//                        }

                        // Load bookings count
                        loadBookingsCount();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this,
                        "Error loading profile: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
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
            if (email != null) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this,
                                "Password reset email sent", Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
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
            Toast.makeText(this, "Name and Phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = new User();
        updatedUser.setName(name);
        updatedUser.setEmail(emailInput.getText().toString().trim());
        updatedUser.setPhone(phone);

        userRef.setValue(updatedUser)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this,
                        "Profile Updated Successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
                        "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
//            Uri imageUri = data.getData();
//            uploadProfileImage(imageUri);
//        }
//    }

//    private void uploadProfileImage(Uri imageUri) {
//        StorageReference fileRef = storageRef.child(userId + ".jpg");
//        fileRef.putFile(imageUri)
//                .addOnSuccessListener(taskSnapshot -> {
//                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                        userRef.child("profileImage").setValue(uri.toString());
//                        Toast.makeText(ProfileActivity.this,
//                                "Profile picture updated", Toast.LENGTH_SHORT).show();
//                    });
//                })
//                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
//                        "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
}