package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.List;

public class BuildLookActivity extends AppCompatActivity {

    private TextInputEditText etStyleQuery;
    private RecyclerView rvResults;
    private TextView tvNoResults, tvResultsTitle;
    private TipsAdapter adapter;
    private List<Tip> allTips;
    private List<Product> userProducts;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_look);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        initViews();
        loadTipsFromFirestore(); // טעינת הטיפים מה-Firestore במקום setupData
        setupRecyclerView();
        setupBottomNavigation();
        loadUserProducts();
    }

    private void initViews() {
        etStyleQuery = findViewById(R.id.et_style_query);
        rvResults = findViewById(R.id.rv_look_results);
        tvNoResults = findViewById(R.id.tv_no_results);
        tvResultsTitle = findViewById(R.id.tv_results_title);
        allTips = new ArrayList<>();
        userProducts = new ArrayList<>();

        findViewById(R.id.btn_generate_look).setOnClickListener(v -> generateLook());
    }

    private void loadTipsFromFirestore() {
        db.collection("tips").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allTips.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Tip tip = doc.toObject(Tip.class);
                allTips.add(tip);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to load tips from database", Toast.LENGTH_SHORT).show());
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
            String skin = doc.getString("skinTone");
            String eyes = doc.getString("eyeColor");
            String face = doc.getString("faceShape");
            String brows = doc.getString("eyebrowsShape");
            
            List<Tip> filtered = new ArrayList<>();
            for (Tip tip : allTips) {
                // 1. התאמה לשאילתת החיפוש (כותרת או תיאור מה-Firestore)
                boolean matchesStyle = (tip.getTitle() != null && tip.getTitle().toLowerCase().contains(query)) || 
                                       (tip.getDescription() != null && tip.getDescription().toLowerCase().contains(query));
                
                // 2. התאמה למאפיינים האישיים (צבע עיניים, עור וכו')
                boolean matchesTraits = tip.matches(skin, eyes, face, brows);

                // 3. בדיקה אם יש למשתמש את המוצרים הנדרשים (אם הוגדרו ב-Tip)
                boolean hasRequiredProducts = checkUserHasProducts(tip.getRequiredProducts());

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
            tvNoResults.setText("No looks found that match your style, features, and owned products.");
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
