package com.example.beautyhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautyhub.utils.PostsAdapter;
import com.example.beautyhub.utils.TravelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private static final String TAG = "MyPostsActivity";

    private String nickname;
    private int age;
    private int level;

    private List<TravelPost> posts;

    private TextView tvTitle;
    private Button btnBack;

    // תכונות ל-RecyclerView
    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_posts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitle = findViewById(R.id.tv_title);
        btnBack = findViewById(R.id.btn_goback);

        // לוגיקת Logout
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyPostsActivity.this, FeedActivity.class);
                startActivity(intent);
                finish();
            }
        });

        readUserData();
        tvTitle.setText(nickname + " (lvl. " + level + ")");

        posts = new ArrayList<>();

        // זימון אתחול RecyclerView
        initRecyclerView();
        loadPosts();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new PostsAdapter(posts);
        recyclerView.setAdapter(postsAdapter);
    }

    // ⭐ הפעולה החדשה לטעינת פוסטים מ-Firestore
    private void loadPosts() {
        Log.d(TAG, "loadPosts: start");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .whereEqualTo("ownerUid", FirebaseAuth.getInstance().getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    posts.clear();
                    Log.d(TAG, "loadPosts succeeded: " + queryDocumentSnapshots.size() + " documents");
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // המרת המסמך לאובייקט TravelPost
                        TravelPost post = doc.toObject(TravelPost.class);
                        posts.add(post);
                    }
                    // עדכון ה-RecyclerView לאחר טעינת הנתונים
                    postsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load posts: " + e.getMessage()));
    }

    private void readUserData(){
        Log.d(TAG, "readUserData: start");
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        nickname = sharedPreferences.getString("nickname", "N/A");
        age = sharedPreferences.getInt("age", 0);
        level = sharedPreferences.getInt("level", 0);
    }
}