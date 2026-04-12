package com.example.beautyhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startFeedActivity(false);
            return;
        }

        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        Button loginButton = findViewById(R.id.btn_login);
        TextView registerLink = findViewById(R.id.link_register);

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });

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
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
                            saveUserDataLocally(doc.getString("nickname"),
                                    doc.contains("age") ? doc.getLong("age").intValue() : 0,
                                    doc.contains("level") ? doc.getLong("level").intValue() : 0,
                                    doc.contains("isGuide") && doc.getBoolean("isGuide"),
                                    doc.getString("profileImageUrl"));
                        }
                        startFeedActivity(true);
                    }
                });
    }

    private void saveUserDataLocally(String nickname, int age, int level, boolean isGuide, String profileImageUrl) {
        getSharedPreferences("userInfo", MODE_PRIVATE).edit()
                .putString("nickname", nickname)
                .putInt("age", age)
                .putInt("level", level)
                .putBoolean("isGuide", isGuide)
                .putString("profileImageUrl", profileImageUrl)
                .apply();
    }

    private void startFeedActivity(boolean sendToast) {
        if (sendToast) Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, FeedActivity.class));
        finish();
    }
}