package com.example.beautyhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
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

    private List<BeautyPost> posts;
    private Button btnLogout, btnMyPosts;
    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed); // ודאי שה-XML מעודכן עם ID main או מחקי את ViewCompat

        db = FirebaseFirestore.getInstance();
        posts = new ArrayList<>();

        initViews();
        initRecyclerView();
        setupClickListeners();
        registerToNewPosts();
    }

    private void initViews() {
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
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnMyPosts.setOnClickListener(v -> startActivity(new Intent(this, MyPostsActivity.class)));

        FloatingActionButton btnAddPost = findViewById(R.id.btn_add_post);
        btnAddPost.setOnClickListener(v -> startActivity(new Intent(this, AddPostActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_feed);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return item.getItemId() == R.id.nav_feed;
        });
    }

    private void registerToNewPosts() {
        db.collection("posts").orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        BeautyPost post = dc.getDocument().toObject(BeautyPost.class);
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            posts.add(dc.getNewIndex(), post);
                            postsAdapter.notifyItemInserted(dc.getNewIndex());
                        }
                    }
                });
    }
}