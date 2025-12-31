package com.example.beautyhub;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private ShapeableImageView profilePic;

    // Launcher לבחירת תמונה מהגלריה
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    profilePic.setImageURI(selectedImageUri);
                }
            }
    );

    // Launcher לצילום תמונה חדשה מהמצלמה
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    profilePic.setImageBitmap(imageBitmap);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // אתחול רכיבים
        profilePic = findViewById(R.id.iv_user_profile_pic);
        MaterialButton btnSignOut = findViewById(R.id.btn_sign_out);
        MaterialButton btnPersonalInfo = findViewById(R.id.btn_personal_info);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // כפתור התנתקות
        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // ניווט תחתון
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_feed) {
                startActivity(new Intent(ProfileActivity.this, FeedActivity.class));
                finish();
                return true;
            }
            return item.getItemId() == R.id.nav_profile;
        });

        // לחיצה על תמונת הפרופיל - פתיחת בחירה בין מצלמה לגלריה
        profilePic.setOnClickListener(v -> showImageSourceDialog());

        // מעבר למסך מידע אישי
        btnPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PersonalInfoActivity.class);
            startActivity(intent);
        });
    }

    /**
     * מציג דיאלוג למשתמש לבחירת מקור התמונה
     */
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // אפשרות 1: מצלמה
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    cameraLauncher.launch(takePictureIntent);
                } else {
                    Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
                }
            } else {
                // אפשרות 2: גלריה
                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(pickIntent);
            }
        });
        builder.show();
    }
}