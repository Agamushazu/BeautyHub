package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beautyhub.utils.PostsAdapter;
import com.example.beautyhub.utils.BeautyPost;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private List<BeautyPost> allPosts = new ArrayList<>();
    private List<BeautyPost> filteredPosts = new ArrayList<>();
    private Button btnMyPosts;
    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;
    private FirebaseFirestore db;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        db = FirebaseFirestore.getInstance();

        initViews();
        initRecyclerView();
        setupClickListeners();
        registerToNewPosts();
    }

    private void initViews() {
        btnMyPosts = findViewById(R.id.btn_gotoposts);
        recyclerView = findViewById(R.id.recycler_posts);
        tabLayout = findViewById(R.id.feed_tabs);
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new PostsAdapter(filteredPosts);
        recyclerView.setAdapter(postsAdapter);
    }

    private void setupClickListeners() {
        btnMyPosts.setOnClickListener(v -> startActivity(new Intent(this, MyPostsActivity.class)));

        FloatingActionButton btnAddPost = findViewById(R.id.btn_add_post);
        btnAddPost.setOnClickListener(v -> startActivity(new Intent(this, AddPostActivity.class)));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterPosts(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_feed);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) { 
                startActivity(new Intent(this, ProfileActivity.class)); 
                finish();
                return true; 
            } else if (id == R.id.nav_tips) { 
                startActivity(new Intent(this, TipsActivity.class)); 
                finish();
                return true; 
            } else if (id == R.id.nav_build_look) {
                startActivity(new Intent(this, BuildLookActivity.class));
                finish();
                return true;
            }
            return id == R.id.nav_feed;
        });
    }

    private void registerToNewPosts() {
        db.collection("posts").orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        BeautyPost post = dc.getDocument().toObject(BeautyPost.class);
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            allPosts.add(dc.getNewIndex(), post);
                        }
                    }
                    filterPosts(tabLayout.getSelectedTabPosition());
                });
    }

    private void filterPosts(int tabIndex) {
        filteredPosts.clear();
        boolean showingGuides = (tabIndex == 1);

        for (BeautyPost post : allPosts) {
            // Post is a guide post if isTip is true OR it has tags
            boolean isGuidePost = post.isTip() || (post.getTags() != null && !post.getTags().isEmpty());
            
            if (showingGuides && isGuidePost) {
                filteredPosts.add(post);
            } else if (!showingGuides && !isGuidePost) {
                filteredPosts.add(post);
            }
        }
        postsAdapter.notifyDataSetChanged();
    }
}