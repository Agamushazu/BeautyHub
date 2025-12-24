package com.example.beautyhub;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beautyhub.utils.RegistrationManager;
import com.example.beautyhub.utils.UserImageSelector;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText ageEditText;
    private EditText nicknameEditText;
    private Spinner levelSpinner;
    private UserImageSelector userImageSelector;

    private static final String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView profilePictureImageView = findViewById(R.id.iv_profile_picture);
        userImageSelector = new UserImageSelector(this, profilePictureImageView);
        Button choosePictureButton = findViewById(R.id.btn_choose_picture);
        choosePictureButton.setOnClickListener(v -> userImageSelector.showImageSourceDialog());

        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        nicknameEditText = findViewById(R.id.et_nickname);
        ageEditText = findViewById(R.id.et_age);
        levelSpinner = findViewById(R.id.spinner_level);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.levels_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);

        Button registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(v -> registerButtonClick());

        Log.d(TAG, "onCreate: done");
    }

    private void registerButtonClick() {
        Log.d(TAG, "Register button clicked");

        int selectedLevel = Integer.parseInt(levelSpinner.getSelectedItem().toString());

        RegistrationManager registrationManager = new RegistrationManager(RegistrationActivity.this);
        registrationManager.startRegistration(
                emailEditText.getText().toString(),
                passwordEditText.getText().toString(),
                nicknameEditText.getText().toString(),
                Integer.parseInt(ageEditText.getText().toString()),
                selectedLevel,
                userImageSelector.createImageFile(),
                new RegistrationManager.OnResultCallback() {
                    @Override
                    public void onResult(boolean success, String message) {
                        if (success) {
                            Toast.makeText(RegistrationActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegistrationActivity.this, com.example.travelog.LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Registration failed: " + message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}