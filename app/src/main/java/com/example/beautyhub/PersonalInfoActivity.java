package com.example.beautyhub;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class PersonalInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // אתחול האדפטרים עבור כל הרשימות בתוך ה-Card
        setupAllSpinners();

        MaterialButton btnSave = findViewById(R.id.btn_save_info);
        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupAllSpinners() {
        // רשימות נתונים
        String[] skin = {"Fair", "Medium", "Olive", "Deep"};
        String[] eyes = {"Brown", "Blue", "Green", "Hazel"};
        String[] eyeShape = {"Almond", "Round", "Hooded", "Monolid"};
        String[] faceShape = {"Oval", "Round", "Square", "Heart"};
        String[] lips = {"Thin", "Full", "Natural"};
        String[] hair = {"Blonde", "Brown", "Black", "Red", "Grey"};

        // חיבור ה-IDs ל-Java וטעינת הנתונים
        loadSpinner(findViewById(R.id.spinner_skin_tone), skin);
        loadSpinner(findViewById(R.id.spinner_eye_color), eyes);
        loadSpinner(findViewById(R.id.spinner_eye_shape), eyeShape);
        loadSpinner(findViewById(R.id.spinner_face_shape), faceShape);
        loadSpinner(findViewById(R.id.spinner_lips_size), lips);
        loadSpinner(findViewById(R.id.spinner_hair_color), hair);
    }

    private void loadSpinner(Spinner spinner, String[] data) {
        if (spinner == null) return;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}