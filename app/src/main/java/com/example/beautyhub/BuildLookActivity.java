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

    private void setupRecyclerView() {
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TipsAdapter(new ArrayList<>());
        rvResults.setAdapter(adapter);
    }

    private void loadUserProducts() {
        if (userId == null) return;
        // Updated to listen to 'my_collection_ids' array in the user document
        db.collection("users").document(userId).addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.e(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                List<String> productIds = (List<String>) snapshot.get("my_collection_ids");
                if (productIds != null && !productIds.isEmpty()) {
                    // Fetch full product details from the 'products' collection
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

        if (userId == null) return;

        tvNoResults.setText("Generating your personalized look... please wait.");
        tvNoResults.setVisibility(View.VISIBLE);
        cardAiResults.setVisibility(View.GONE);

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            StringBuilder userTraits = new StringBuilder();
            String[] fields = {"skinTone", "eyeColor", "eyeShape", "hairColor", "eyebrowsShape", "lipsSize", "faceShape"};
            for (String field : fields) {
                String val = doc.getString(field);
                if (val != null) userTraits.append(field).append(": ").append(val).append(", ");
            }

            // Build products string for the AI in requested format
            StringBuilder productsStr = new StringBuilder();
            if (userProducts != null && !userProducts.isEmpty()) {
                for (Product p : userProducts) {
                    productsStr.append("Name: ").append(p.getName())
                            .append(", Brand: ").append(p.getBrand())
                            .append(", Category: ").append(p.getCategory())
                            .append(", Description: ").append(p.getDescription() != null ? p.getDescription() : "N/A")
                            .append("\n");
                }
            } else {
                productsStr.append("No products in collection.");
            }

            getAiTips(userTraits.toString(), productsStr.toString());
        });
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

        Log.d(TAG, "getAiTips: prompt: " + prompt);
        GeminiManager gemini = GeminiManager.getInstance();
        gemini.sendText(prompt, this, new GeminiManager.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                String[] sections = result.split("#");
                
                if (sections.length >= 4) {
                    StringBuilder formattedResult = new StringBuilder();
                    formattedResult.append("🌟 OVERVIEW\n").append(sections[0].trim()).append("\n\n");
                    formattedResult.append("✨ FACE\n").append(sections[1].trim()).append("\n\n");
                    formattedResult.append("👁️ EYES\n").append(sections[2].trim()).append("\n\n");
                    formattedResult.append("💄 LIPS\n").append(sections[3].trim());

                    tvAiResponseText.setText(formattedResult.toString());
                    cardAiResults.setVisibility(View.VISIBLE);
                    tvNoResults.setVisibility(View.GONE);
                } else {
                    Toast.makeText(BuildLookActivity.this, "Error: AI response format was unexpected", Toast.LENGTH_LONG).show();
                    tvNoResults.setText("Try again with a different style.");
                }
            }

            @Override
            public void onError(Throwable error) {
                tvNoResults.setText("Error generating tips.");
                Toast.makeText(BuildLookActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getAiTipsPrompt(String userStyle, String userAppearance, String userProducts) {
        return "You are an expert Professional Makeup Artist and Beauty Consultant. Provide personalized makeup recommendations based on:\n" +
                "\n" +
                "Desired Style: "+ userStyle + "\n" +
                "\n" +
                "User Appearance: " + userAppearance + "\n" +
                "\n" +
                "Available Products: " + userProducts + "\n" +
                "\n" +
                "Instructions:\n" +
                "\n" +
                "Exactly 4 sections.\n" +
                "\n" +
                "Separate sections ONLY with '#'.\n" +
                "\n" +
                "Section 1: Overview, Section 2: Face, Section 3: Eyes, Section 4: Lips.\n" +
                "\n" +
                "Product Integration: You MUST prioritize the products listed in the 'Available Products' section. For each step, explicitly name the specific Brand and Product Name from the user's list.\n" +
                "\n" +
                "If a necessary product for the look is missing from their list, suggest a creative way to use an existing product (e.g., using a lipstick as a blush) or provide a general technique.\n" +
                "\n" +
                "The advice should feel tailored to the specific textures and shades of the products they own.\n" +
                "\n" +
                "Response format: Overview text (referencing the overall look using their brands)... # Face tips (mentioning specific face products from their list)... # Eye tips (mentioning specific eye products from their list)... # Lip tips (mentioning specific lip products from their list)";
    }
}
