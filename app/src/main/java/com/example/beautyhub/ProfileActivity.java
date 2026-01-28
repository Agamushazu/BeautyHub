package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.beautyhub.utils.SupabaseStorageHelper;
import com.example.beautyhub.utils.UserImageSelector;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    private UserImageSelector imageSelector;
    private ImageView ivUserProfilePic;
    private MaterialButton btnSetProfilePic;
    private FirebaseFirestore db;
    private String userId;
    private File pendingImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();
        ivUserProfilePic = findViewById(R.id.iv_user_profile_pic);
        btnSetProfilePic = findViewById(R.id.btn_set_profile_pic);

        loadUserProfilePicture();

        imageSelector = new UserImageSelector(this, ivUserProfilePic);
        ivUserProfilePic.setOnClickListener(v -> imageSelector.showImageSourceDialog());

        imageSelector.setOnImageSelectedListener(() -> {
            File file = imageSelector.createImageFile();
            if (file != null) {
                pendingImageFile = file;
                btnSetProfilePic.setVisibility(View.VISIBLE);
            }
        });

        btnSetProfilePic.setOnClickListener(v -> {
            if (pendingImageFile != null) {
                uploadProfilePicture(pendingImageFile);
            }
        });

        setupButtons();
        setupBottomNav();
    }

    private void uploadProfilePicture(File file) {
        btnSetProfilePic.setEnabled(false);
        btnSetProfilePic.setText("Uploading...");
        
        String fileName = "profiles/" + userId + ".jpg";
        SupabaseStorageHelper.uploadPicture(file, fileName, (success, url, error) -> {
            if (success) {
                db.collection("users").document(userId).update("profileImageUrl", url)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            getSharedPreferences("userInfo", MODE_PRIVATE).edit()
                                    .putString("profileImageUrl", url).apply();
                            
                            btnSetProfilePic.setVisibility(View.GONE);
                            btnSetProfilePic.setEnabled(true);
                            btnSetProfilePic.setText("Set As Profile Picture");
                            pendingImageFile = null;
                        });
            } else {
                Toast.makeText(this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                btnSetProfilePic.setEnabled(true);
                btnSetProfilePic.setText("Set As Profile Picture");
            }
        });
    }

    private void loadUserProfilePicture() {
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.contains("profileImageUrl")) {
                String url = doc.getString("profileImageUrl");
                Glide.with(this).load(url).placeholder(android.R.drawable.ic_menu_camera).into(ivUserProfilePic);
                getSharedPreferences("userInfo", MODE_PRIVATE).edit()
                        .putString("profileImageUrl", url).apply();
            }
        });
    }

    private void setupButtons() {
        findViewById(R.id.btn_personal_info).setOnClickListener(v -> startActivity(new Intent(this, PersonalInfoActivity.class)));
        findViewById(R.id.btn_collection).setOnClickListener(v -> startActivity(new Intent(this, ProductCollection.class)));
        findViewById(R.id.btn_favorites).setOnClickListener(v -> startActivity(new Intent(this, FavoritesActivity.class)));
        findViewById(R.id.btn_sign_out).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) { startActivity(new Intent(this, FeedActivity.class)); finish(); return true; }
            if (id == R.id.nav_build_look) { startActivity(new Intent(this, BuildLookActivity.class)); finish(); return true; }
            if (id == R.id.nav_tips) { startActivity(new Intent(this, TipsActivity.class)); finish(); return true; }
            return id == R.id.nav_profile;
        });
    }
}