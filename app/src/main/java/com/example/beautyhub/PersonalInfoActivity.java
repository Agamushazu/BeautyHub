package com.example.beautyhub;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PersonalInfoActivity extends AppCompatActivity {

    private Spinner spinnerEye, spinnerSkin, spinnerFace, spinnerLips, spinnerHair;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Initialize UI components
        initSpinners();

        MaterialButton btnSave = findViewById(R.id.btn_save_info);
        btnSave.setOnClickListener(v -> saveToFirestore());
    }

    private void initSpinners() {
        spinnerEye = findViewById(R.id.spinner_eye_color);
        spinnerSkin = findViewById(R.id.spinner_skin_tone);
        spinnerFace = findViewById(R.id.spinner_face_shape);
        spinnerLips = findViewById(R.id.spinner_lips_size);
        spinnerHair = findViewById(R.id.spinner_hair_color);

        // English Options as requested
        String[] eyeColors = {"Select Eye Color", "Dark Brown", "Light Brown", "Green", "Blue", "Grey", "Black", "Gold", "Violet"};
        String[] skinTones = {"Select Skin Tone", "Fair", "Pale", "Olive", "Light Tan", "Tan", "Dark Tan", "Deep"};
        String[] faceShapes = {"Select Face Shape", "Round", "Oval", "Square", "Oblong", "Triangle", "Heart"};
        String[] lipsSizes = {"Select Lips Size", "Thin", "Average", "Full", "Very Full"};
        String[] hairColors = {"Select Hair Color", "Black", "Dark Brown", "Light Brown", "Red", "Dark Blonde", "Light Blonde", "Grey", "White/Silver", "Fashion Colors"};

        setupAdapter(spinnerEye, eyeColors);
        setupAdapter(spinnerSkin, skinTones);
        setupAdapter(spinnerFace, faceShapes);
        setupAdapter(spinnerLips, lipsSizes);
        setupAdapter(spinnerHair, hairColors);
    }

    private void setupAdapter(Spinner spinner, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void saveToFirestore() {
        if (userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("eyeColor", spinnerEye.getSelectedItem().toString());
        data.put("skinTone", spinnerSkin.getSelectedItem().toString());
        data.put("faceShape", spinnerFace.getSelectedItem().toString());
        data.put("lipsSize", spinnerLips.getSelectedItem().toString());
        data.put("hairColor", spinnerHair.getSelectedItem().toString());

        db.collection("users").document(userId)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}