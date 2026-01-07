package com.example.beautyhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beautyhub.utils.PostsAdapter;
import com.example.beautyhub.utils.BeautyPost;
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
    private List<BeautyPost> posts;
    private TextView tvTitle;
    private Button btnLogout, btnMyPosts;
    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        posts = new ArrayList<>();

        initViews();
        readUserData();

        // הצגת השם בלבד
        tvTitle.setText(nickname);

        initRecyclerView();
        setupClickListeners();
        registerToNewPosts();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        btnLogout = findViewById(R.id.btn_logout);
        btnMyPosts = findViewById(R.id.btn_gotoposts);
        recyclerView = findViewById(R.id.recycler_posts);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new PostsAdapter(posts);
        recyclerView.setAdapter(postsAdapter);
    }

    private void setupClickListeners() {
        // התנתקות מהאפליקציה
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(FeedActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // מעבר למסך הפוסטים האישיים (My Posts)
        btnMyPosts.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, MyPostsActivity.class);
            startActivity(intent);
        });

        // כפתור צף להוספת פוסט חדש
        FloatingActionButton btnAddPost = findViewById(R.id.btn_add_post);
        btnAddPost.setOnClickListener(v -> startActivity(new Intent(this, AddPostActivity.class)));

        // ניווט בתפריט התחתון
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_feed);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                // שינוי יעד ל-MyPostsActivity כדי לראות את הפוסטים האישיים
                startActivity(new Intent(FeedActivity.this, MyPostsActivity.class));
                return true;
            }
            return item.getItemId() == R.id.nav_feed;
        });
    }

    private void readUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        nickname = sharedPreferences.getString("nickname", "User");
    }

    private void registerToNewPosts() {
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            BeautyPost post = dc.getDocument().toObject(BeautyPost.class);
                            switch (dc.getType()) {
                                case ADDED:
                                    posts.add(dc.getNewIndex(), post);
                                    postsAdapter.notifyItemInserted(dc.getNewIndex());
                                    break;
                                case MODIFIED:
                                    posts.set(dc.getOldIndex(), post);
                                    postsAdapter.notifyItemChanged(dc.getOldIndex());
                                    break;
                                case REMOVED:
                                    posts.remove(dc.getOldIndex());
                                    postsAdapter.notifyItemRemoved(dc.getOldIndex());
                                    break;
                            }
                        }
                    }
                });
    }
}