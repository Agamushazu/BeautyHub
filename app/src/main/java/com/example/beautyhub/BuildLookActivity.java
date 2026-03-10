package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beautyhub.utils.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildLookActivity extends AppCompatActivity {

    private static final String TAG = "BUILD_LOOK_DEBUG";
    private TextInputEditText etStyleQuery;
    private RecyclerView rvResults;
    private TextView tvNoResults, tvResultsTitle;
    private TipsAdapter adapter;
    private List<Tip> allTips = new ArrayList<>();
    private List<Product> userProducts = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_look);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        initViews();
        loadTipsFromFirestore();
        setupRecyclerView();
        setupBottomNavigation();
        loadUserProducts();
    }

    private void initViews() {
        etStyleQuery = findViewById(R.id.et_style_query);
        rvResults = findViewById(R.id.rv_look_results);
        tvNoResults = findViewById(R.id.tv_no_results);
        tvResultsTitle = findViewById(R.id.tv_results_title);

        findViewById(R.id.btn_generate_look).setOnClickListener(v -> generateLook());
    }

    private void loadTipsFromFirestore() {
        db.collection("tips").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allTips.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                // Manual mapping to be safe with field names
                Map<String, Object> data = doc.getData();
                Tip tip = new Tip();
                tip.setTitle((String) data.get("title"));
                tip.setDescription((String) data.get("description"));
                tip.setVideoUrl((String) data.get("videoUrl"));
                
                String cat = data.containsKey("Category") ? (String) data.get("Category") : (String) data.get("category");
                String val = data.containsKey("CategoryValue") ? (String) data.get("CategoryValue") : (String) data.get("categoryValue");
                
                tip.setCategory(cat);
                tip.setCategoryValue(val);
                
                if (data.containsKey("requiredProducts")) {
                    tip.setRequiredProducts((List<String>) data.get("requiredProducts"));
                }
                
                allTips.add(tip);
            }
            Log.d(TAG, "Loaded " + allTips.size() + " tips for Build Look");
        });
    }

    private void setupRecyclerView() {
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TipsAdapter(new ArrayList<>());
        rvResults.setAdapter(adapter);
    }

    private void loadUserProducts() {
        if (userId == null) return;
        db.collection("users").document(userId).collection("my_collection").get().addOnSuccessListener(queryDocumentSnapshots -> {
            userProducts.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                userProducts.add(doc.toObject(Product.class));
            }
        });
    }

    private void generateLook() {
        String query = etStyleQuery.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a style", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null) return;

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            Map<String, String> userTraits = new HashMap<>();
            String[] fields = {"skinTone", "eyeColor", "eyeShape", "hairColor", "eyebrowsShape", "lipsSize", "faceShape"};
            for (String field : fields) {
                String val = doc.getString(field);
                if (val != null) userTraits.put(field, val);
            }
            
            Log.d(TAG, "User Traits for build look: " + userTraits);
            
            List<Tip> filtered = new ArrayList<>();
            for (Tip tip : allTips) {
                boolean matchesStyle = (tip.getTitle() != null && tip.getTitle().toLowerCase().contains(query)) || 
                                       (tip.getDescription() != null && tip.getDescription().toLowerCase().contains(query));
                
                boolean matchesTraits = tip.matches(userTraits);
                boolean hasRequiredProducts = checkUserHasProducts(tip.getRequiredProducts());

                Log.d(TAG, "Checking Tip: " + tip.getTitle() + " | Category: " + tip.getCategory() + " | Matches Style: " + matchesStyle + " | Matches Traits: " + matchesTraits);

                if (matchesStyle && matchesTraits && hasRequiredProducts) {
                    filtered.add(tip);
                }
            }

            displayResults(filtered);
        });
    }

    private boolean checkUserHasProducts(List<String> required) {
        if (required == null || required.isEmpty()) return true;
        
        for (String req : required) {
            boolean found = false;
            for (Product p : userProducts) {
                if (p.getName().toLowerCase().contains(req.toLowerCase()) || 
                    p.getBrand().toLowerCase().contains(req.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private void displayResults(List<Tip> results) {
        if (results.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            tvResultsTitle.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
        } else {
            adapter.updateList(results);
            rvResults.setVisibility(View.VISIBLE);
            tvResultsTitle.setVisibility(View.VISIBLE);
            tvNoResults.setVisibility(View.GONE);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_build_look);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) { startActivity(new Intent(this, FeedActivity.class)); finish(); return true; }
            if (id == R.id.nav_profile) { startActivity(new Intent(this, ProfileActivity.class)); finish(); return true; }
            if (id == R.id.nav_tips) { startActivity(new Intent(this, TipsActivity.class)); finish(); return true; }
            return id == R.id.nav_build_look;
        });
    }
}
