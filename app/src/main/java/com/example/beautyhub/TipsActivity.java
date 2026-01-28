package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TipsActivity extends AppCompatActivity {

    private RecyclerView recyclerAll, recyclerRecommended;
    private TipsAdapter adapterAll, adapterRecommended;
    private List<Tip> allTips;
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
        setupData();
        setupRecyclerViews();
        setupSearchView();
        setupBottomNavigation();
        
        loadUserPreferencesAndFilter();
    }

    private void setupData() {
        allTips = new ArrayList<>();
        allTips.add(new Tip("Smoky Eye for Brown Eyes", "Highlight your brown eyes with deep earthy tones.", android.R.drawable.ic_menu_gallery, Arrays.asList("Brown", "Round")));
        allTips.add(new Tip("Fair Skin Glow", "Best blush and highlight techniques for fair complexions.", android.R.drawable.ic_menu_gallery, Arrays.asList("Fair")));
        allTips.add(new Tip("Blue Eye Magic", "Contrasting shades to make blue eyes pop.", android.R.drawable.ic_menu_gallery, Arrays.asList("Blue")));
        allTips.add(new Tip("Hooded Eyes Wing", "Master the 'bat-wing' eyeliner for hooded lids.", android.R.drawable.ic_menu_gallery, Arrays.asList("Hooded")));
        allTips.add(new Tip("Olive Skin Bronzing", "Get that sun-kissed look for olive skin tones.", android.R.drawable.ic_menu_gallery, Arrays.asList("Olive")));
        allTips.add(new Tip("Bold Red Lips", "Classic look for all face shapes.", android.R.drawable.ic_menu_gallery, Arrays.asList("Full", "Natural")));
        allTips.add(new Tip("Brown Hair Harmony", "Makeup colors that complement brunette hair.", android.R.drawable.ic_menu_gallery, Arrays.asList("Brown")));
    }

    private void setupRecyclerViews() {
        // All Tips
        recyclerAll = findViewById(R.id.recycler_tips);
        recyclerAll.setLayoutManager(new LinearLayoutManager(this));
        adapterAll = new TipsAdapter(allTips);
        recyclerAll.setAdapter(adapterAll);

        // Recommended Tips
        recyclerRecommended = findViewById(R.id.recycler_recommended_tips);
        recyclerRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterRecommended = new TipsAdapter(new ArrayList<>());
        recyclerRecommended.setAdapter(adapterRecommended);
    }

    private void loadUserPreferencesAndFilter() {
        if (userId == null) return;

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String skin = doc.getString("skinTone");
                String eyes = doc.getString("eyeColor");
                String eyeShape = doc.getString("eyeShape");
                String hair = doc.getString("hairColor");

                List<Tip> recommended = new ArrayList<>();
                for (Tip tip : allTips) {
                    if (tip.matches(skin, eyes, eyeShape, hair)) {
                        recommended.add(tip);
                    }
                }

                if (!recommended.isEmpty()) {
                    adapterRecommended.updateList(recommended);
                    tvRecommendedTitle.setVisibility(View.VISIBLE);
                    recyclerRecommended.setVisibility(View.VISIBLE);
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
            if (id == R.id.nav_feed) { startActivity(new Intent(this, FeedActivity.class)); finish(); return true; }
            if (id == R.id.nav_profile) { startActivity(new Intent(this, ProfileActivity.class)); finish(); return true; }
            return id == R.id.nav_tips;
        });
    }
}