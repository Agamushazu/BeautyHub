package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        MaterialButton btnPersonalInfo = findViewById(R.id.btn_personal_info);
        MaterialButton btnSignOut = findViewById(R.id.btn_sign_out);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        btnPersonalInfo.setOnClickListener(v ->
                startActivity(new Intent(this, PersonalInfoActivity.class)));

        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) {
                startActivity(new Intent(this, FeedActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_tips) {
                startActivity(new Intent(this, TipsActivity.class));
                // לא עושים finish כדי שהמשתמש יוכל לחזור לפרופיל עם כפתור אחורה
                return true;
            }
            return true;
        });
    }
}