package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beautyhub.utils.RegistrationManager;

public class RegistrationActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, nicknameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // אתחול שדות (מבוסס על התמונה)
        nicknameEditText = findViewById(R.id.et_nickname); // שדה Name
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);

        Button registerButton = findViewById(R.id.btn_register); // כפתור Sign up בורדו
        Button backToLoginButton = findViewById(R.id.btn_back_to_login); // כפתור Log in ורוד

        // לחיצה על Sign up
        registerButton.setOnClickListener(v -> registerButtonClick());

        // לחיצה על Log in (מעבר חזרה למסך הקודם)
        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerButtonClick() {
        String email = emailEditText.getText().toString().trim();
        String pass = passwordEditText.getText().toString().trim();
        String name = nicknameEditText.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // כאן אתה קורא ל-RegistrationManager שלך
        // שים לב: הורדתי את ה-age וה-level כדי להתאים למסך החדש בתמונה
        RegistrationManager registrationManager = new RegistrationManager(this);
        registrationManager.startRegistration(
                email, pass, name, 0, 0, null, // ערכי ברירת מחדל למה שלא בתמונה
                (success, message) -> {
                    if (success) {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                });
    }
}