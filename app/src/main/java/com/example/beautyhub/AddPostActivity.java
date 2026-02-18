package com.example.beautyhub;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
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
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddPostActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private TextView tvSelectedTags;
    private LinearLayout layoutGuideTags;
    private ImageView ivSelectedImage;
    private Uri selectedImageUri;
    private File photoFile;
    private boolean isGuide;
    
    private List<String> allPossibleTags = new ArrayList<>();
    private boolean[] checkedTags;
    private List<String> selectedTagsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        isGuide = sp.getBoolean("isGuide", false);

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

        if (isGuide) {
            layoutGuideTags.setVisibility(View.VISIBLE);
            prepareTagsList();
        }

        btnSelectTags.setOnClickListener(v -> showTagsDialog());
        btnBack.setOnClickListener(v -> finish());

        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
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

        ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
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
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        btnTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        btnSubmit.setOnClickListener(v -> validateAndUpload());
    }

    private void prepareTagsList() {
        allPossibleTags.addAll(Arrays.asList(getResources().getStringArray(R.array.eye_colors)));
        allPossibleTags.addAll(Arrays.asList(getResources().getStringArray(R.array.eye_shapes)));
        allPossibleTags.addAll(Arrays.asList(getResources().getStringArray(R.array.skin_tones)));
        allPossibleTags.addAll(Arrays.asList(getResources().getStringArray(R.array.face_shapes)));
        allPossibleTags.addAll(Arrays.asList(getResources().getStringArray(R.array.eyebrows_shapes)));
        allPossibleTags.addAll(Arrays.asList(getResources().getStringArray(R.array.lips_sizes)));
        allPossibleTags.addAll(Arrays.asList(getResources().getStringArray(R.array.hair_colors)));
        
        checkedTags = new boolean[allPossibleTags.size()];
    }

    private void showTagsDialog() {
        String[] tagsArray = allPossibleTags.toArray(new String[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Beauty Categories");
        builder.setMultiChoiceItems(tagsArray, checkedTags, (dialog, which, isChecked) -> {
            checkedTags[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedTagsList.clear();
            StringBuilder preview = new StringBuilder();
            for (int i = 0; i < checkedTags.length; i++) {
                if (checkedTags[i]) {
                    selectedTagsList.add(tagsArray[i]);
                    if (preview.length() > 0) preview.append(", ");
                    preview.append(tagsArray[i]);
                }
            }
            tvSelectedTags.setText(preview.length() > 0 ? preview.toString() : "No tags selected");
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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

        BeautyPost post = new BeautyPost(title, desc, uid, nickname, profileImageUrl, Timestamp.now(), imageUrl, tags, isTip);
        
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