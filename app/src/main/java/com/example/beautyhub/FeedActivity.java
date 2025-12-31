package com.example.beautyhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautyhub.utils.PostsAdapter;
import com.example.beautyhub.utils.TravelPost;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private String nickname;
    private int level;
    private List<TravelPost> posts;
    private TextView tvTitle;
    private Button btnLogout;
    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);

        // הגדרת ריווחים למערכת
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // חיבור רכיבים
        tvTitle = findViewById(R.id.tv_title);
        btnLogout = findViewById(R.id.btn_logout);
        FloatingActionButton btnAddPost = findViewById(R.id.btn_add_post);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // לוגיקת Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(FeedActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // ניווט תחתון
        bottomNav.setSelectedItemId(R.id.nav_feed);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(FeedActivity.this, ProfileActivity.class));
                return true;
            }
            return item.getItemId() == R.id.nav_feed;
        });

        btnAddPost.setOnClickListener(v -> startActivity(new Intent(this, AddPostActivity.class)));

        // טעינת נתונים
        readUserData();
        tvTitle.setText(nickname + " (lvl. " + level + ")");
        posts = new ArrayList<>();
        initRecyclerView();
        registerToNewPosts();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new PostsAdapter(posts);
        recyclerView.setAdapter(postsAdapter);
    }

    private void readUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        nickname = sharedPreferences.getString("nickname", "N/A");
        level = sharedPreferences.getInt("level", 0);
    }

    private void registerToNewPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            TravelPost post = dc.getDocument().toObject(TravelPost.class);
                            posts.add(0, post);
                        }
                    }
                    postsAdapter.notifyDataSetChanged();
                });
    }
}