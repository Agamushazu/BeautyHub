package com.example.beautyhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.beautyhub.utils.BeautyPost;
import com.example.beautyhub.utils.SupabaseStorageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AddPostActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private ImageView ivSelectedImage;
    private Uri selectedImageUri;
    private File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        etTitle = findViewById(R.id.et_post_title);
        etDescription = findViewById(R.id.et_post_description);
        ivSelectedImage = findViewById(R.id.iv_selected_image);
        MaterialButton btnSubmit = findViewById(R.id.btn_submit_post);
        MaterialButton btnPickImage = findViewById(R.id.btn_pick_image);
        MaterialButton btnTakePhoto = findViewById(R.id.btn_take_photo);
        MaterialButton btnBack = findViewById(R.id.btn_back);

        // כפתור חזרה
        btnBack.setOnClickListener(v -> finish());

        // בחירת תמונה מהגלריה
        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivSelectedImage.setImageURI(selectedImageUri);
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        photoFile = null; // איפוס קובץ מצלמה אם נבחר מהגלריה
                    }
                }
        );

        // צילום תמונה
        ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        ivSelectedImage.setImageBitmap(photo);
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        selectedImageUri = null; // איפוס URI גלריה
                        photoFile = saveBitmapToFile(photo);
                    }
                }
        );

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> validateAndUpload());
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            File tempFile = new File(getCacheDir(), "camera_image_" + System.currentTimeMillis() + ".jpg");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            byte[] bitmapData = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    private void validateAndUpload() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadFile(getFileFromUri(selectedImageUri), title, desc);
        } else if (photoFile != null) {
            uploadFile(photoFile, title, desc);
        } else {
            savePost(title, desc, "");
        }
    }

    private void uploadFile(File file, String title, String desc) {
        if (file == null) return;
        String fileName = "posts/" + System.currentTimeMillis() + ".jpg";
        SupabaseStorageHelper.uploadPicture(file, fileName, (success, url, error) -> {
            if (success) savePost(title, desc, url);
            else Toast.makeText(this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void savePost(String title, String desc, String imageUrl) {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String nickname = sp.getString("nickname", "User");
        String profileImageUrl = sp.getString("profileImageUrl", "");
        String uid = FirebaseAuth.getInstance().getUid();

        BeautyPost post = new BeautyPost(title, desc, uid, nickname, profileImageUrl, Timestamp.now(), imageUrl);
        
        FirebaseFirestore.getInstance().collection("posts").add(post)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Post Published!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private File getFileFromUri(Uri uri) {
        try {
            File tempFile = new File(getCacheDir(), "temp_image.jpg");
            InputStream is = getContentResolver().openInputStream(uri);
            FileOutputStream os = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) os.write(buffer, 0, bytesRead);
            os.close(); is.close();
            return tempFile;
        } catch (Exception e) { return null; }
    }
}