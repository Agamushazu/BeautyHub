package com.example.beautyhub;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalInfoActivity extends AppCompatActivity {

    private Spinner spinnerSkin, spinnerEyes, spinnerEyeShape, spinnerFace, spinnerLips, spinnerHair;
    private MaterialButton btnSave;
    private FirebaseFirestore db;
    private String userId;

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
    }

    private void initViews() {
        spinnerSkin = findViewById(R.id.spinner_skin_tone);
        spinnerEyes = findViewById(R.id.spinner_eye_color);
        spinnerEyeShape = findViewById(R.id.spinner_eye_shape);
        spinnerFace = findViewById(R.id.spinner_face_shape);
        spinnerLips = findViewById(R.id.spinner_lips_size);
        spinnerHair = findViewById(R.id.spinner_hair_color);
        btnSave = findViewById(R.id.btn_save_info);
    }

    private void setupAllSpinners() {
        String[] skin = {"Fair", "Medium", "Olive", "Deep"};
        String[] eyes = {"Brown", "Blue", "Green", "Hazel", "Other"};
        String[] eyeShape = {"Almond", "Round", "Hooded", "Monolid"};
        String[] faceShape = {"Oval", "Round", "Square", "Heart"};
        String[] lips = {"Thin", "Natural", "Full"};
        String[] hair = {"Blonde", "Brown", "Black", "Grey", "Other"};

        loadSpinner(spinnerSkin, skin, "Select Skin Tone");
        loadSpinner(spinnerEyes, eyes, "Select Eye Color");
        loadSpinner(spinnerEyeShape, eyeShape, "Select Eye Shape");
        loadSpinner(spinnerFace, faceShape, "Select Face Shape");
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

    private void saveUserData() {
        if (userId == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("skinTone", getSelectedValue(spinnerSkin));
        userData.put("eyeColor", getSelectedValue(spinnerEyes));
        userData.put("eyeShape", getSelectedValue(spinnerEyeShape));
        userData.put("faceShape", getSelectedValue(spinnerFace));
        userData.put("lipsSize", getSelectedValue(spinnerLips));
        userData.put("hairColor", getSelectedValue(spinnerHair));

        db.collection("users").document(userId).update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // If update fails (e.g. document doesn't exist yet), use set with merge
                    db.collection("users").document(userId).set(userData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(err -> Toast.makeText(this, "Error saving info", Toast.LENGTH_SHORT).show());
                });
    }

    private String getSelectedValue(Spinner spinner) {
        if (spinner.getSelectedItemPosition() == 0) return null;
        return spinner.getSelectedItem().toString();
    }

    private void loadUserData() {
        if (userId == null) return;

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Check if there is any data to determine button text
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
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }
}