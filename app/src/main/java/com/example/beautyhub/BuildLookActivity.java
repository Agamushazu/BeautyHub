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
            String userTraits = "";
            String[] fields = {"skinTone", "eyeColor", "eyeShape", "hairColor", "eyebrowsShape", "lipsSize", "faceShape"};
            for (String field : fields) {
                String val = doc.getString(field);
                if (val != null)
                    userTraits += field + ": " + val + ", ";
            }
            
            Log.d(TAG, "User Traits for build look: " + userTraits);
            
            getAiTips(userTraits);

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

    public void getAiTips(String userAppearance)
    {
        String userQuery = etStyleQuery.getText().toString();

        String prompt = getAiTipsPrompt(userQuery, userAppearance);
        Log.d(TAG, "getAiTips: prompt: " + prompt);

        GeminiManager gemini = GeminiManager.getInstance();

        gemini.sendText(prompt, this,
                new GeminiManager.GeminiCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d(TAG, "onSuccess: " + result);
                        String newText = "";

                            String[] fields = result.split("#");
                            if (fields.length == 4) {

                                if (!fields[0].equals("NA"))
                                    newText += "\n" + "General: " + fields[0];
                                if (!fields[1].equals("NA"))
                                    newText += "\n" + "Face:   " + fields[1];
                                if (!fields[2].equals("NA"))
                                    newText += "\n" + "Eye: " + fields[2];
                                if (!fields[2].equals("NA"))
                                    newText += "\n" + "Lip: " + fields[2];


                                etStyleQuery.setText(newText);
                            }
                            else
                            {
                                Log.d(TAG, "onSuccess: Bad Format. fields size: " + fields.length);
                                Toast.makeText(BuildLookActivity.this, "Error: Bad Format", Toast.LENGTH_LONG).show();

                            }

                    }

                    @Override
                    public void onError(Throwable error) {
                        Log.d("TAG", "onError: " + error.getMessage());

                        Toast.makeText(BuildLookActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }

                });

    }

    private String getAiTipsPrompt(String userStyle, String userAppearance)
    {
        String prompt = "You are an expert Professional Makeup Artist and Beauty Consultant. Your task is to provide personalized makeup recommendations based on a user's physical characteristics and their desired style.\n" +
                "\n" +
                "### INPUT DATA:\n" +
                "- **Desired Style: %s ** \n" +
                "- **User Appearance: %s **\n" +
                "\n" +
                "### INSTRUCTIONS:\n" +
                "1. Analyze how the desired style fits the user's specific features.\n" +
                "2. Provide practical, professional, and aesthetically pleasing tips.\n" +
                "3. You must provide exactly 4 sections in your response.\n" +
                "4. **CRITICAL:** Separate each section ONLY with the '#' character. Do not use the '#' character anywhere else in the text.\n" +
                "5. Do not include any introductory or concluding remarks outside of the four sections.\n" +
                "\n" +
                "### RESPONSE STRUCTURE:\n" +
                "Section 1: A general overview of the look and why it suits the user.\n" +
                "#\n" +
                "Section 2: Detailed face makeup tips (foundation, contour, blush, highlight).\n" +
                "#\n" +
                "Section 3: Detailed eye makeup tips (eyeshadow, eyeliner, mascara, brows).\n" +
                "#\n" +
                "Section 4: Detailed lip makeup tips (liner, lipstick, gloss, finish).\n" +
                "\n" +
                "### OUTPUT EXAMPLE (Reference only):\n" +
                "General description of the look... # Tips for the face... # Tips for the eyes... # Tips for the lips...";

        return prompt.formatted(userStyle, userAppearance);
    }

}
