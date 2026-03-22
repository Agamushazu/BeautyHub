package com.example.beautyhub;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.beautyhub.utils.BeautyPost;
import com.example.beautyhub.utils.SupabaseStorageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private TextView tvSelectedTags;
    private LinearLayout layoutGuideTags;
    private ImageView ivSelectedImage;
    private Uri selectedImageUri;
    private File photoFile;
    private boolean isGuide = false;
    
    private List<String> selectedTagsList = new ArrayList<>();
    private Map<String, Boolean> tagStatusMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        etTitle = findViewById(R.id.et_post_title);
        etDescription = findViewById(R.id.et_post_description);
        tvSelectedTags = findViewById(R.id.tv_selected_tags);
        layoutGuideTags = findViewById(R.id.layout_guide_tags);
        ivSelectedImage = findViewById(R.id.iv_selected_image);
        MaterialButton btnSubmit = findViewById(R.id.btn_submit_post);
        MaterialButton btnPickImage = findViewById(R.id.btn_pick_image);
        MaterialButton btnTakePhoto = findViewById(R.id.btn_take_photo);
        MaterialButton btnBack = findViewById(R.id.btn_back);
        MaterialButton btnSelectTags = findViewById(R.id.btn_select_tags);

        checkUserRole();

        btnSelectTags.setOnClickListener(v -> showTagsDialog());
        btnBack.setOnClickListener(v -> finish());

        ActivityResultLauncher<android.content.Intent> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivSelectedImage.setImageURI(selectedImageUri);
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        photoFile = null;
                    }
                }
        );

        ActivityResultLauncher<android.content.Intent> cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        ivSelectedImage.setImageBitmap(photo);
                        ivSelectedImage.setVisibility(View.VISIBLE);
                        selectedImageUri = null;
                        photoFile = saveBitmapToFile(photo);
                    }
                }
        );

        btnPickImage.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnTakePhoto.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> validateAndUpload());
    }

    private void checkUserRole() {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        isGuide = sp.getBoolean("isGuide", false);
        
        if (isGuide) {
            layoutGuideTags.setVisibility(View.VISIBLE);
        } else {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("isGuide")) {
                            isGuide = documentSnapshot.getBoolean("isGuide");
                            if (isGuide) layoutGuideTags.setVisibility(View.VISIBLE);
                        }
                    });
            }
        }
    }

    private void showTagsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_tags, null);
        LinearLayout container = dialogView.findViewById(R.id.container_tags);

        // Define categories and their corresponding string arrays
        addCategoryToDialog(container, "Eye Color", R.array.eye_colors);
        addCategoryToDialog(container, "Eye Shape", R.array.eye_shapes);
        addCategoryToDialog(container, "Skin Tone", R.array.skin_tones);
        addCategoryToDialog(container, "Face Shape", R.array.face_shapes);
        addCategoryToDialog(container, "Eyebrows Shape", R.array.eyebrows_shapes);
        addCategoryToDialog(container, "Lips Size", R.array.lips_sizes);
        addCategoryToDialog(container, "Hair Color", R.array.hair_colors);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> updateSelectedTagsPreview())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addCategoryToDialog(LinearLayout container, String title, int arrayResId) {
        // Add Category Header
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(16);
        header.setPadding(0, 20, 0, 10);
        header.setTextColor(getResources().getColor(R.color.nav_item_color_state)); // Use an existing color or #A64452
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        container.addView(header);

        // Add Checkboxes for each item in the array
        String[] items = getResources().getStringArray(arrayResId);
        for (String item : items) {
            CheckBox cb = new CheckBox(this);
            cb.setText(item);
            cb.setChecked(tagStatusMap.getOrDefault(item, false));
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> tagStatusMap.put(item, isChecked));
            container.addView(cb);
        }
    }

    private void updateSelectedTagsPreview() {
        selectedTagsList.clear();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : tagStatusMap.entrySet()) {
            if (entry.getValue()) {
                selectedTagsList.add(entry.getKey());
                if (sb.length() > 0) sb.append(", ");
                sb.append(entry.getKey());
            }
        }
        tvSelectedTags.setText(sb.length() > 0 ? sb.toString() : "No tags selected");
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
        } catch (Exception e) { return null; }
    }

    private void validateAndUpload() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        boolean isTip = isGuide && !selectedTagsList.isEmpty();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadFile(getFileFromUri(selectedImageUri), title, desc, selectedTagsList, isTip);
        } else if (photoFile != null) {
            uploadFile(photoFile, title, desc, selectedTagsList, isTip);
        } else {
            savePost(title, desc, "", selectedTagsList, isTip);
        }
    }

    private void uploadFile(File file, String title, String desc, List<String> tags, boolean isTip) {
        if (file == null) return;
        String fileName = "posts/" + System.currentTimeMillis() + ".jpg";
        SupabaseStorageHelper.uploadPicture(file, fileName, (success, url, error) -> {
            if (success) savePost(title, desc, url, tags, isTip);
            else Toast.makeText(this, "Upload failed: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void savePost(String title, String desc, String imageUrl, List<String> tags, boolean isTip) {
        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String nickname = sp.getString("nickname", "User");
        String profileImageUrl = sp.getString("profileImageUrl", "");
        String uid = FirebaseAuth.getInstance().getUid();

        // Create a new document reference to get a unique ID
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("posts").document();
        String postId = docRef.getId();

        BeautyPost post = new BeautyPost(postId, title, desc, uid, nickname, profileImageUrl, Timestamp.now(), imageUrl, tags, isTip);
        
        docRef.set(post)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Post Published!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to publish post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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