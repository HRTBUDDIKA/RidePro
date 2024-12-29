package com.yellowrose.busbookingapp.main;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yellowrose.busbookingapp.R;
import com.yellowrose.busbookingapp.user.UserDashboardActivity;
import com.yellowrose.busbookingapp.admin.AdminDashboardActivity;

public class MainActivity extends AppCompatActivity {

        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mAuth = FirebaseAuth.getInstance();

            new Handler().postDelayed(() -> {
                // Check if user is logged in
                if (mAuth.getCurrentUser() != null) {
                    checkUserRole();
                } else {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }, 2000);
        }

        private void checkUserRole() {
            String userId = mAuth.getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase
                    .getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference()
                    .child("users").child(userId);

            userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);
                    if ("ADMIN".equals(role)) {
                        startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, UserDashboardActivity.class));
                    }
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }