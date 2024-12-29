package com.yellowrose.busbookingapp.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yellowrose.busbookingapp.R;
import com.yellowrose.busbookingapp.User;
import com.yellowrose.busbookingapp.user.UserDashboardActivity;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText nameInput, phoneInput, emailInput, passwordInput;
    private MaterialButton registerButton;
    private TextView loginLink;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);

        // Initialize progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        // Set click listeners
        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password should be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        progressDialog.show();

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save additional user data to Firebase Database
                        User user = new User(
                                mAuth.getCurrentUser().getUid(),
                                name,
                                email,
                                phone,
                                "USER" // Default role
                        );

                        saveUserToDatabase(user);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(User user) {
        DatabaseReference userRef = FirebaseDatabase
                .getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference()
                .child("users").child(user.getUserId());

        userRef.setValue(user).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(RegisterActivity.this,
                        "Registration successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, UserDashboardActivity.class));
                finish();
            } else {
                Toast.makeText(RegisterActivity.this,
                        "Failed to save user data: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}