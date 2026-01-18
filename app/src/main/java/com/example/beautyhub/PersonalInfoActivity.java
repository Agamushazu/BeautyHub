package com.example.beautyhub;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersonalInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        setupAllSpinners();

        MaterialButton btnSave = findViewById(R.id.btn_save_info);
        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupAllSpinners() {
        // Data arrays
        String[] skin = {"Fair", "Medium", "Olive", "Deep"};
        String[] eyes = {"Brown", "Blue", "Green", "Hazel"};
        String[] eyeShape = {"Almond", "Round", "Hooded", "Monolid"};
        String[] faceShape = {"Oval", "Round", "Square", "Heart"};
        String[] lips = {"Thin", "Full", "Natural"};
        String[] hair = {"Blonde", "Brown", "Black", "Red", "Grey"};

        // Loading spinners with "Select..." prompt
        loadSpinner(findViewById(R.id.spinner_skin_tone), skin, "Select Skin Tone");
        loadSpinner(findViewById(R.id.spinner_eye_color), eyes, "Select Eye Color");
        loadSpinner(findViewById(R.id.spinner_eye_shape), eyeShape, "Select Eye Shape");
        loadSpinner(findViewById(R.id.spinner_face_shape), faceShape, "Select Face Shape");
        loadSpinner(findViewById(R.id.spinner_lips_size), lips, "Select Lips Size");
        loadSpinner(findViewById(R.id.spinner_hair_color), hair, "Select Hair Color");
    }

    private void loadSpinner(Spinner spinner, String[] data, String prompt) {
        if (spinner == null) return;
        
        List<String> list = new ArrayList<>();
        list.add(prompt);
        list.addAll(Arrays.asList(data));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}