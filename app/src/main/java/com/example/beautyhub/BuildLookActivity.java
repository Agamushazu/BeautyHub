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

import com.example.beautyhub.utils.GeminiManager;
import com.example.beautyhub.utils.Product;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
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
    private TextView tvNoResults, tvResultsTitle, tvAiResponseText;
    private MaterialCardView cardAiResults;
    private TipsAdapter adapter;
    private List<Tip> allTips = new ArrayList<>();
    private List<Product> userProducts = new ArrayList<>();
    private Map<String, String> userTraitsMap = new HashMap<>();
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
        loadUserTraits();
    }

    private void initViews() {
        etStyleQuery = findViewById(R.id.et_style_query);
        rvResults = findViewById(R.id.rv_look_results);
        tvNoResults = findViewById(R.id.tv_no_results);
        tvResultsTitle = findViewById(R.id.tv_results_title);
        tvAiResponseText = findViewById(R.id.tv_ai_response_text);
        cardAiResults = findViewById(R.id.card_ai_results);

        findViewById(R.id.btn_generate_look).setOnClickListener(v -> generateLook());
    }

    private void loadTipsFromFirestore() {
        db.collection("tips").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allTips.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
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
        });
    }

    private void loadUserTraits() {
        if (userId == null) return;
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String[] fields = {"skinTone", "eyeColor", "eyeShape", "hairColor", "eyebrowsShape", "lipsSize", "faceShape"};
                for (String field : fields) {
                    String val = doc.getString(field);
                    if (val != null) userTraitsMap.put(field, val);
                }
            }
        });
    }

    private void setupRecyclerView() {
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TipsAdapter(new ArrayList<>());
        rvResults.setAdapter(adapter);
    }

    private void loadUserProducts() {
        if (userId == null) return;
        db.collection("users").document(userId).addSnapshotListener((snapshot, e) -> {
            if (e != null) return;
            if (snapshot != null && snapshot.exists()) {
                List<String> productIds = (List<String>) snapshot.get("my_collection_ids");
                if (productIds != null && !productIds.isEmpty()) {
                    db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
                        userProducts.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (productIds.contains(doc.getId())) {
                                Product p = doc.toObject(Product.class);
                                if (p.getId() == null) p.setId(doc.getId());
                                userProducts.add(p);
                            }
                        }
                    });
                } else {
                    userProducts.clear();
                }
            }
        });
    }

    private void generateLook() {
        String query = etStyleQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a style", Toast.LENGTH_SHORT).show();
            return;
        }

        tvNoResults.setText("Generating your personalized look... please wait.");
        tvNoResults.setVisibility(View.VISIBLE);
        cardAiResults.setVisibility(View.GONE);
        rvResults.setVisibility(View.GONE);
        tvResultsTitle.setVisibility(View.GONE);

        // Filter tips based on user traits
        List<Tip> recommendedTips = new ArrayList<>();
        for (Tip tip : allTips) {
            if (tip.matches(userTraitsMap)) {
                recommendedTips.add(tip);
            }
        }

        if (!recommendedTips.isEmpty()) {
            adapter.updateList(recommendedTips);
            rvResults.setVisibility(View.VISIBLE);
            tvResultsTitle.setVisibility(View.VISIBLE);
            tvResultsTitle.setText("Recommended Video Guides for You:");
        }

        // Proceed with AI generation
        StringBuilder traitsStr = new StringBuilder();
        for (Map.Entry<String, String> entry : userTraitsMap.entrySet()) {
            traitsStr.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
        }

        StringBuilder productsStr = new StringBuilder();
        if (!userProducts.isEmpty()) {
            for (Product p : userProducts) {
                productsStr.append(p.getName()).append(" (").append(p.getBrand()).append("), ");
            }
        } else {
            productsStr.append("No products in collection.");
        }

        getAiTips(traitsStr.toString(), productsStr.toString());
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

    public void getAiTips(String userAppearance, String userProducts) {
        String userQuery = etStyleQuery.getText().toString();
        String prompt = getAiTipsPrompt(userQuery, userAppearance, userProducts);

        GeminiManager gemini = GeminiManager.getInstance();
        gemini.sendText(prompt, this, new GeminiManager.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                // Improved parsing: filter out empty sections caused by leading/trailing separators
                String[] rawSections = result.split("#");
                List<String> cleanSections = new ArrayList<>();
                for (String s : rawSections) {
                    if (!s.trim().isEmpty()) {
                        cleanSections.add(s.trim());
                    }
                }

                if (cleanSections.size() >= 4) {
                    StringBuilder formattedResult = new StringBuilder();
                    formattedResult.append("🌟 OVERVIEW\n").append(cleanSections.get(0)).append("\n\n");
                    formattedResult.append("✨ FACE\n").append(cleanSections.get(1)).append("\n\n");
                    formattedResult.append("👁️ EYES\n").append(cleanSections.get(2)).append("\n\n");
                    formattedResult.append("💄 LIPS\n").append(cleanSections.get(3));

                    tvAiResponseText.setText(formattedResult.toString());
                    cardAiResults.setVisibility(View.VISIBLE);
                    tvNoResults.setVisibility(View.GONE);
                } else {
                    tvNoResults.setText("AI response was incomplete. Please try again.");
                }
            }

            @Override
            public void onError(Throwable error) {
                tvNoResults.setText("Error generating tips.");
            }
        });
    }

    private String getAiTipsPrompt(String userStyle, String userAppearance, String userProducts) {
        return "You are an expert Professional Makeup Artist. Provide personalized makeup recommendations based on:\n" +
                "Desired Style: "+ userStyle + "\n" +
                "User Appearance: " + userAppearance + "\n" +
                "Available Products: " + userProducts + "\n\n" +
                "Instructions:\n" +
                "Provide exactly 4 sections in this EXACT order: Overview, then Face tips, then Eye tips, then Lip tips.\n" +
                "Separate sections ONLY with the '#' character.\n" +
                "DO NOT start the response with '#'.\n" +
                "DO NOT include section titles like 'Section 1' or 'Face:' inside the text, I will add them myself.\n" +
                "Example format: Overview text here... # Face tips here... # Eye tips here... # Lip tips here...";
    }
}
