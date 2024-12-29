package com.yellowrose.busbookingapp.user;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yellowrose.busbookingapp.user.fragment.AllBus;
import com.yellowrose.busbookingapp.BookingDetailsActivity;
import com.yellowrose.busbookingapp.R;
import com.yellowrose.busbookingapp.main.LoginActivity;
import com.yellowrose.busbookingapp.user.fragment.HomeFragment;
import com.yellowrose.busbookingapp.user.fragment.ProfileFragment;

public class UserDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private static final String FIREBASE_URL = "https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        // Setup initial fragment if this is first creation
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        bottomNavigationView.setBackground(null);
        setupNavigationView();
    }

    private void setupNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // Use if-else instead of switch for resource IDs
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_bookings) {
                replaceFragment(new AllBus());
            } else if (id == R.id.nav_profile) {
                replaceFragment(new ProfileFragment());
            } else if (id == R.id.nav_logout) {
                handleLogout();
            }
            return true;
        });
    }

    private void handleLogout() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}