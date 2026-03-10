package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beautyhub.utils.BeautyPost;
import com.example.beautyhub.utils.TipSeeder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TipsActivity extends AppCompatActivity {

    private static final String TAG = "TIPS_DEBUG";
    private RecyclerView recyclerAll, recyclerRecommended;
    private TipsAdapter adapterAll, adapterRecommended;
    private List<Tip> allTipsList = new ArrayList<>();
    private Map<String, String> userTraitsMap = new HashMap<>();
    private TextView tvRecommendedTitle;
    private FirebaseFirestore db;
    private String userId;
    
    private ListenerRegistration tipsListener, userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        tvRecommendedTitle = findViewById(R.id.tv_recommended_title);
        
        setupRecyclerViews();
        setupSearchView();
        setupBottomNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (tipsListener != null) tipsListener.remove();
        if (userListener != null) userListener.remove();
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

    private void startListening() {
        tipsListener = db.collection("tips").addSnapshotListener((value, error) -> {
            if (error != null) return;
            if (value != null) {
                allTipsList.clear();
                for (QueryDocumentSnapshot document : value) {
                    allTipsList.add(mapToTip(document.getData()));
                }
                if (allTipsList.isEmpty()) TipSeeder.seedTips(this);
                adapterAll.updateList(allTipsList);
                updateRecommendations();
            }
        });

        if (userId != null) {
            userListener = db.collection("users").document(userId).addSnapshotListener((doc, error) -> {
                if (error != null || doc == null || !doc.exists()) return;
                
                userTraitsMap.clear();
                String[] fields = {"skinTone", "eyeColor", "eyeShape", "hairColor", "eyebrowsShape", "lipsSize", "faceShape"};
                for (String field : fields) {
                    String val = doc.getString(field);
                    if (val != null) userTraitsMap.put(field, val);
                }
                updateRecommendations();
            });
        }
    }

    private Tip mapToTip(Map<String, Object> data) {
        Tip tip = new Tip();
        tip.setTitle((String) data.get("title"));
        tip.setDescription((String) data.get("description"));
        tip.setVideoUrl((String) data.get("videoUrl"));
        tip.setCategory(data.containsKey("Category") ? (String) data.get("Category") : (String) data.get("category"));
        tip.setCategoryValue(data.containsKey("CategoryValue") ? (String) data.get("CategoryValue") : (String) data.get("categoryValue"));
        return tip;
    }

    private void updateRecommendations() {
        if (allTipsList.isEmpty() || userTraitsMap.isEmpty()) {
            tvRecommendedTitle.setVisibility(View.GONE);
            recyclerRecommended.setVisibility(View.GONE);
            return;
        }

        final List<Tip> recommendedList = new ArrayList<>();
        for (Tip tip : allTipsList) {
            if (tip.matches(userTraitsMap)) {
                recommendedList.add(tip);
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
    }

    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.search_view_tips);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
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
            if (id == R.id.nav_build_look) { startActivity(new Intent(this, BuildLookActivity.class)); finish(); return true; }
            return id == R.id.nav_tips;
        });
    }
}
