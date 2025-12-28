package com.example.beautyhub;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // טיפול ב-padding של המערכת (סטטוס בר)
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        auth = FirebaseAuth.getInstance();

        // בדיקה אם המשתמש כבר מחובר
        if (auth.getCurrentUser() != null) {
            startFeedActivity(false);
            return;
        }

        // אתחול רכיבים
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        Button loginButton = findViewById(R.id.btn_login);
        Button registerButton = findViewById(R.id.link_register);

        // מעבר להרשמה
        registerButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        // ביצוע התחברות
        loginButton.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        getUserDataFromFirestore();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Failed";
                        Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getUserDataFromFirestore() {
        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            String nickname = doc.getString("nickname");
                            int age = doc.contains("age") ? doc.getLong("age").intValue() : 0;
                            int level = doc.contains("level") ? doc.getLong("level").intValue() : 0;

                            saveUserDataLocally(nickname, age, level);
                            startFeedActivity(true);
                        } else {
                            startFeedActivity(true); // עובר בכל זאת אם אין דאטה
                        }
                    } else {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDataLocally(String nickname, int age, int level) {
        SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
        pref.edit().putString("nickname", nickname).putInt("age", age).putInt("level", level).apply();
    }

    private void startFeedActivity(boolean sendToast) {
        if (sendToast) {
            Toast.makeText(LoginActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(LoginActivity.this, FeedActivity.class);
        startActivity(intent);
        finish();
    }
}