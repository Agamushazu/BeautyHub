package com.example.beautyhub;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.beautyhub.utils.TravelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import java.util.Date;

public class AddPostActivity extends AppCompatActivity {

    private static final String TAG = "AddPostActivity";

    private EditText etTitle;
    private EditText etDescription;
    private Button btnSubmitPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_post);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etTitle = findViewById(R.id.et_post_title);
        etDescription = findViewById(R.id.et_post_description);
        btnSubmitPost = findViewById(R.id.btn_submit_post);

        btnSubmitPost.setOnClickListener(v -> {
            if (validateForm()) {
                sendPost();
            }
        });
    }

    private boolean validateForm() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter both title and content.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private TravelPost createTravelPost() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        String ownerUid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() :
                "unknown_uid";

        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String ownerNickname = sharedPreferences.getString("nickname", "N/A");

        Timestamp createdAt = new Timestamp(new Date());

        return new TravelPost(title, description, ownerUid, ownerNickname, createdAt);
    }

    private void sendPost() {
        Log.d(TAG, "sendPost: start");
        TravelPost post = createTravelPost();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    Toast.makeText(AddPostActivity.this, "Post saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(AddPostActivity.this, "Error saving post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        Log.d(TAG, "sendPost: done");
    }
}