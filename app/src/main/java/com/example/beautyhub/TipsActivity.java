package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beautyhub.utils.BeautyPost;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class TipsActivity extends AppCompatActivity {

    private RecyclerView recyclerAll, recyclerRecommended;
    private TipsAdapter adapterAll, adapterRecommended;
    private List<Tip> allTipsList;
    private TextView tvRecommendedTitle;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        tvRecommendedTitle = findViewById(R.id.tv_recommended_title);
        allTipsList = new ArrayList<>();
        
        setupRecyclerViews();
        setupSearchView();
        setupBottomNavigation();
        
        loadTipsFromFirestore();
    }

    private void setupRecyclerViews() {
        recyclerAll = findViewById(R.id.recycler_tips);
        recyclerAll.setLayoutManager(new LinearLayoutManager(this));
        adapterAll = new TipsAdapter(allTipsList);
        recyclerAll.setAdapter(adapterAll);

        recyclerRecommended = findViewById(R.id.recycler_recommended_tips);
        recyclerRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterRecommended = new TipsAdapter(new ArrayList<>());
        recyclerRecommended.setAdapter(adapterRecommended);
    }

    private void loadTipsFromFirestore() {
        db.collection("tips").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allTipsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Tip tip = document.toObject(Tip.class);
                    allTipsList.add(tip);
                }
                adapterAll.updateList(allTipsList);
                loadUserPreferencesAndFilter();
            } else {
                Toast.makeText(this, "Error loading tips", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserPreferencesAndFilter() {
        if (userId == null) return;

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                List<String> userTraits = new ArrayList<>();
                if (doc.contains("skinTone")) userTraits.add(doc.getString("skinTone"));
                if (doc.contains("eyeColor")) userTraits.add(doc.getString("eyeColor"));
                if (doc.contains("eyeShape")) userTraits.add(doc.getString("eyeShape"));
                if (doc.contains("hairColor")) userTraits.add(doc.getString("hairColor"));
                if (doc.contains("eyebrowsShape")) userTraits.add(doc.getString("eyebrowsShape"));
                if (doc.contains("lipsSize")) userTraits.add(doc.getString("lipsSize"));
                if (doc.contains("faceShape")) userTraits.add(doc.getString("faceShape"));

                final List<Tip> recommendedList = new ArrayList<>();

                for (Tip tip : allTipsList) {
                    if (tip.matches(userTraits.toArray(new String[0]))) {
                        recommendedList.add(tip);
                    }
                }

                db.collection("posts")
                    .whereEqualTo("tip", true) // Corrected from "isTip" to "tip"
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot postDoc : queryDocumentSnapshots) {
                            BeautyPost post = postDoc.toObject(BeautyPost.class);
                            
                            if (post.getTags() != null) {
                                boolean isMatch = false;
                                for (String tag : post.getTags()) {
                                    for (String trait : userTraits) {
                                        if (trait != null && trait.equalsIgnoreCase(tag)) {
                                            isMatch = true;
                                            break;
                                        }
                                    }
                                    if (isMatch) break;
                                }

                                if (isMatch) {
                                    Tip tipFromPost = new Tip();
                                    tipFromPost.setTitle(post.getTitle() + " (by " + post.getOwnerNickname() + ")");
                                    tipFromPost.setDescription(post.getDescription());
                                    tipFromPost.setVideoUrl(post.getPostImageUrl());
                                    recommendedList.add(tipFromPost);
                                }
                            }
                        }

                        if (!recommendedList.isEmpty()) {
                            adapterRecommended.updateList(recommendedList);
                            tvRecommendedTitle.setVisibility(View.VISIBLE);
                            recyclerRecommended.setVisibility(View.VISIBLE);
                        } else {
                            tvRecommendedTitle.setVisibility(View.GONE);
                            recyclerRecommended.setVisibility(View.GONE);
                        }
                    });
            }
        });
    }

    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.search_view_tips);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapterAll.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterAll.filter(newText);
                return false;
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_tips);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) { 
                startActivity(new Intent(this, FeedActivity.class)); 
                finish();
                return true; 
            } else if (id == R.id.nav_profile) { 
                startActivity(new Intent(this, ProfileActivity.class)); 
                finish();
                return true; 
            } else if (id == R.id.nav_build_look) {
                startActivity(new Intent(this, BuildLookActivity.class));
                finish();
                return true;
            }
            return id == R.id.nav_tips;
        });
    }
}