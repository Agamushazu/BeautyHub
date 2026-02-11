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
        // All Tips
        recyclerAll = findViewById(R.id.recycler_tips);
        recyclerAll.setLayoutManager(new LinearLayoutManager(this));
        adapterAll = new TipsAdapter(allTipsList);
        recyclerAll.setAdapter(adapterAll);

        // Recommended Tips
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
                // UPDATE: Use updateList to ensure the adapter's internal filtered list is updated
                adapterAll.updateList(allTipsList);
                
                // Once tips are loaded, filter recommendations based on user info
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
                String skin = doc.getString("skinTone");
                String eyes = doc.getString("eyeColor");
                String eyeShape = doc.getString("eyeShape");
                String hair = doc.getString("hairColor");
                String eyebrows = doc.getString("eyebrowsShape");

                List<Tip> recommended = new ArrayList<>();
                for (Tip tip : allTipsList) {
                    // Match based on user traits
                    if (tip.matches(skin, eyes, eyeShape, hair, eyebrows)) {
                        recommended.add(tip);
                    }
                }

                if (!recommended.isEmpty()) {
                    adapterRecommended.updateList(recommended);
                    tvRecommendedTitle.setVisibility(View.VISIBLE);
                    recyclerRecommended.setVisibility(View.VISIBLE);
                } else {
                    tvRecommendedTitle.setVisibility(View.GONE);
                    recyclerRecommended.setVisibility(View.GONE);
                }
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
