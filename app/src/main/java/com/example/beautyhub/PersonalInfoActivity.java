package com.example.beautyhub;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PersonalInfoActivity extends AppCompatActivity {

    private Spinner spinnerSkin, spinnerEyes, spinnerEyeShape, spinnerFace, spinnerLips, spinnerHair, spinnerEyebrows;
    private MaterialButton btnSave, btnBack, btnUploadPhoto;
    private ProgressBar analysisProgress;
    private FirebaseFirestore db;
    private String userId;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    analyzeImage(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        initViews();
        setupAllSpinners();
        loadUserData();

        btnSave.setOnClickListener(v -> saveUserData());
        btnBack.setOnClickListener(v -> finish());
        btnUploadPhoto.setOnClickListener(v -> mGetContent.launch("image/*"));
    }

    private void initViews() {
        spinnerSkin = findViewById(R.id.spinner_skin_tone);
        spinnerEyes = findViewById(R.id.spinner_eye_color);
        spinnerEyeShape = findViewById(R.id.spinner_eye_shape);
        spinnerFace = findViewById(R.id.spinner_face_shape);
        spinnerEyebrows = findViewById(R.id.spinner_eyebrows_shape);
        spinnerLips = findViewById(R.id.spinner_lips_size);
        spinnerHair = findViewById(R.id.spinner_hair_color);
        btnSave = findViewById(R.id.btn_save_info);
        btnBack = findViewById(R.id.btn_goback);
        btnUploadPhoto = findViewById(R.id.btn_upload_photo);
        analysisProgress = findViewById(R.id.analysis_progress);
    }

    private void setupAllSpinners() {
        String[] skin = {"Fair", "Medium", "Olive", "Deep"};
        String[] eyes = {"Brown", "Blue", "Green", "Hazel", "Other"};
        String[] eyeShape = {"Almond", "Round", "Hooded", "Monolid"};
        String[] faceShape = {"Oval", "Round", "Square", "Heart"};
        String[] eyebrowShape = {"Straight", "Arched", "Rounded", "S-Shaped", "Upward"};
        String[] lips = {"Thin", "Natural", "Full"};
        String[] hair = {"Blonde", "Brown", "Black", "Grey", "Other"};

        loadSpinner(spinnerSkin, skin, "Select Skin Tone");
        loadSpinner(spinnerEyes, eyes, "Select Eye Color");
        loadSpinner(spinnerEyeShape, eyeShape, "Select Eye Shape");
        loadSpinner(spinnerFace, faceShape, "Select Face Shape");
        loadSpinner(spinnerEyebrows, eyebrowShape, "Select Eyebrows Shape");
        loadSpinner(spinnerLips, lips, "Select Lips Size");
        loadSpinner(spinnerHair, hair, "Select Hair Color");
    }

    private void loadSpinner(Spinner spinner, String[] data, String prompt) {
        if (spinner == null) return;
        List<String> list = new ArrayList<>();
        list.add(prompt);
        list.addAll(Arrays.asList(data));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void analyzeImage(Uri imageUri) {
        analysisProgress.setVisibility(View.VISIBLE);
        btnUploadPhoto.setEnabled(false);
        Toast.makeText(this, "Analyzing features...", Toast.LENGTH_SHORT).show();

        // Simulate AI analysis delay
        new Handler().postDelayed(() -> {
            analysisProgress.setVisibility(View.GONE);
            btnUploadPhoto.setEnabled(true);

            // Mocked results - In a real app, you would use a vision API here
            String[] skin = {"Fair", "Medium", "Olive", "Deep"};
            String[] eyes = {"Brown", "Blue", "Green", "Hazel"};
            String[] eyeShape = {"Almond", "Round", "Hooded"};
            String[] faceShape = {"Oval", "Round", "Heart"};
            String[] eyebrowShape = {"Arched", "Straight", "Rounded"};

            Random r = new Random();
            setSpinnerSelection(spinnerSkin, skin[r.nextInt(skin.length)]);
            setSpinnerSelection(spinnerEyes, eyes[r.nextInt(eyes.length)]);
            setSpinnerSelection(spinnerEyeShape, eyeShape[r.nextInt(eyeShape.length)]);
            setSpinnerSelection(spinnerFace, faceShape[r.nextInt(faceShape.length)]);
            setSpinnerSelection(spinnerEyebrows, eyebrowShape[r.nextInt(eyebrowShape.length)]);
            
            Toast.makeText(this, "Analysis complete! Check the fields below.", Toast.LENGTH_LONG).show();
        }, 2500);
    }

    private void saveUserData() {
        if (userId == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("skinTone", getSelectedValue(spinnerSkin));
        userData.put("eyeColor", getSelectedValue(spinnerEyes));
        userData.put("eyeShape", getSelectedValue(spinnerEyeShape));
        userData.put("faceShape", getSelectedValue(spinnerFace));
        userData.put("eyebrowsShape", getSelectedValue(spinnerEyebrows));
        userData.put("lipsSize", getSelectedValue(spinnerLips));
        userData.put("hairColor", getSelectedValue(spinnerHair));

        db.collection("users").document(userId).update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    db.collection("users").document(userId).set(userData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(err -> Toast.makeText(this, "Error saving info", Toast.LENGTH_SHORT).show());
                });
    }

    private String getSelectedValue(Spinner spinner) {
        if (spinner == null || spinner.getSelectedItemPosition() <= 0) return null;
        return spinner.getSelectedItem().toString();
    }

    private void loadUserData() {
        if (userId == null) return;

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                boolean hasData = false;
                
                if (documentSnapshot.contains("skinTone")) {
                    setSpinnerSelection(spinnerSkin, documentSnapshot.getString("skinTone"));
                    hasData = true;
                }
                if (documentSnapshot.contains("eyeColor")) {
                    setSpinnerSelection(spinnerEyes, documentSnapshot.getString("eyeColor"));
                    hasData = true;
                }
                if (documentSnapshot.contains("eyeShape")) {
                    setSpinnerSelection(spinnerEyeShape, documentSnapshot.getString("eyeShape"));
                    hasData = true;
                }
                if (documentSnapshot.contains("faceShape")) {
                    setSpinnerSelection(spinnerFace, documentSnapshot.getString("faceShape"));
                    hasData = true;
                }
                if (documentSnapshot.contains("eyebrowsShape")) {
                    setSpinnerSelection(spinnerEyebrows, documentSnapshot.getString("eyebrowsShape"));
                    hasData = true;
                }
                if (documentSnapshot.contains("lipsSize")) {
                    setSpinnerSelection(spinnerLips, documentSnapshot.getString("lipsSize"));
                    hasData = true;
                }
                if (documentSnapshot.contains("hairColor")) {
                    setSpinnerSelection(spinnerHair, documentSnapshot.getString("hairColor"));
                    hasData = true;
                }

                if (hasData) {
                    btnSave.setText("UPDATE INFORMATION");
                }
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || spinner == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }
}
