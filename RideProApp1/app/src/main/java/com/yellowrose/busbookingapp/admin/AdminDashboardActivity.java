package com.yellowrose.busbookingapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yellowrose.busbookingapp.BookingActivity;
import com.yellowrose.busbookingapp.main.LoginActivity;
import com.yellowrose.busbookingapp.R;

public class AdminDashboardActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private TextView totalBusesText, totalBookingsText, totalUsersText;
    private CardView addBusCard, viewBusesCard, viewBookingsCard, viewUsersCard;
    private TextView navHeaderName, navHeaderEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verify admin access before showing dashboard
        verifyAdminAccess();

        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance("https://bus-booking-app-63514-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        initializeViews();
        setupToolbar();
        setupNavigationView();
        setupClickListeners();
        loadStatistics();
        loadAdminInfo();
    }

    private void verifyAdminAccess() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(currentUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    redirectToLogin();
                    return;
                }

                String role = snapshot.child("role").getValue(String.class);
                if (!"ADMIN".equals(role)) {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Access denied. Admin privileges required.",
                            Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Error verifying admin access",
                        Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        // Initialize navigation header views

        totalBusesText = findViewById(R.id.totalBusesText);
        totalBookingsText = findViewById(R.id.totalBookingsText);
        totalUsersText = findViewById(R.id.totalUsersText);

        addBusCard = findViewById(R.id.addBusCard);
        viewBusesCard = findViewById(R.id.viewBusesCard);
        viewBookingsCard = findViewById(R.id.viewBookingsCard);
        viewUsersCard = findViewById(R.id.viewUsersCard);
    }

    private void loadAdminInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = databaseRef.child("users").child(user.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
//                        navHeaderName.setText(name);
//                        navHeaderEmail.setText(email);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Error loading admin information",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigationView() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                // Already on dashboard
            } else if (id == R.id.nav_add_bus) {
                startActivity(new Intent(this, AddBusActivity.class));
            } else if (id == R.id.nav_view_buses) {
                startActivity(new Intent(this, BusListActivity.class));
            } else if (id == R.id.nav_bookings) {
                startActivity(new Intent(this, BookingActivity.class));
            } else if (id == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupClickListeners() {
        addBusCard.setOnClickListener(v ->
                startActivity(new Intent(this, AddBusActivity.class)));

        viewBusesCard.setOnClickListener(v ->
                startActivity(new Intent(this, BusListActivity.class)));

        viewBookingsCard.setOnClickListener(v ->
                startActivity(new Intent(this, BookingActivity.class)));

        viewUsersCard.setOnClickListener(v ->
                startActivity(new Intent(this, UsersListActivity.class)));
    }

    private void loadStatistics() {
        // Load total buses
        databaseRef.child("buses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalBusesText.setText("Total Buses: " + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Error loading bus statistics",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Load total bookings
        databaseRef.child("bookings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalBookingsText.setText("Total Bookings: " + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Error loading booking statistics",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Load total users
        databaseRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalUsersText.setText("Total Users: " + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminDashboardActivity.this,
                        "Error loading user statistics",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}