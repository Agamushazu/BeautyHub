package com.example.beautyhub;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class OtherUserProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePic;
    private TextView tvUsername, tvUserRole, tvGender, tvAge;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");

        if (userId == null) {
            finish();
            return;
        }

        ivProfilePic = findViewById(R.id.iv_user_profile_pic);
        tvUsername = findViewById(R.id.tv_username);
        tvUserRole = findViewById(R.id.tv_user_role);
        tvGender = findViewById(R.id.tv_user_gender);
        tvAge = findViewById(R.id.tv_user_age);
        MaterialButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        loadUserData();
    }

    private void loadUserData() {
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String nickname = doc.getString("nickname");
                String gender = doc.getString("gender");
                Long age = doc.getLong("age");
                Boolean isGuide = doc.getBoolean("isGuide");
                String profileUrl = doc.getString("profileImageUrl");

                tvUsername.setText(nickname != null ? nickname : "User");
                tvGender.setText("Gender: " + (gender != null ? gender : "Not specified"));
                tvAge.setText("Age: " + (age != null ? age : "-"));
                
                if (isGuide != null && isGuide) {
                    tvUserRole.setText("Guide");
                } else {
                    tvUserRole.setText("Student");
                }

                if (profileUrl != null && !profileUrl.isEmpty()) {
                    Glide.with(this).load(profileUrl).placeholder(R.drawable.ic_launcher_background).circleCrop().into(ivProfilePic);
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show());
    }
}