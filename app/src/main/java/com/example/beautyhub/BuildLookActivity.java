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
import java.util.Arrays;
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
        setupData();
        setupRecyclerView();
        setupBottomNavigation();
        loadUserProducts();
    }

    private void initViews() {
        etStyleQuery = findViewById(R.id.et_style_query);
        rvResults = findViewById(R.id.rv_look_results);
        tvNoResults = findViewById(R.id.tv_no_results);
        tvResultsTitle = findViewById(R.id.tv_results_title);
        userProducts = new ArrayList<>();

        findViewById(R.id.btn_generate_look).setOnClickListener(v -> generateLook());
    }

    private void setupData() {
        allTips = new ArrayList<>();
        // Tips with Traits AND Required Products
        allTips.add(new Tip("Evening Glam for Brown Eyes", "Use your dark eyeshadows and bold liner.", 
                android.R.drawable.ic_menu_gallery, 
                Arrays.asList("Brown", "Evening", "Glam"), 
                Arrays.asList("Eyeshadow", "Eyeliner")));

        allTips.add(new Tip("Natural Day Look", "Light foundation and nude lips for a fresh feel.", 
                android.R.drawable.ic_menu_gallery, 
                Arrays.asList("Natural", "Fair", "Medium"), 
                Arrays.asList("Foundation", "Lipstick")));

        allTips.add(new Tip("Party Glow", "Highlighter is key! Best for olive skin tones.", 
                android.R.drawable.ic_menu_gallery, 
                Arrays.asList("Party", "Olive", "Glow"), 
                Arrays.asList("Highlighter", "Bronzer")));

        allTips.add(new Tip("Red Carpet Lips", "Deep red lipstick guide for full lips.", 
                android.R.drawable.ic_menu_gallery, 
                Arrays.asList("Full", "Evening", "Red"), 
                Arrays.asList("Lipstick", "Lip liner")));

        allTips.add(new Tip("Office Professional", "Subtle tones for a clean work look.", 
                android.R.drawable.ic_menu_gallery, 
                Arrays.asList("Natural", "Work", "Professional"), 
                Arrays.asList("Mascara", "Concealer")));
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
            
            List<Tip> filtered = new ArrayList<>();
            for (Tip tip : allTips) {
                // 1. Match Style Query
                boolean matchesStyle = tip.getTitle().toLowerCase().contains(query) || 
                                       tip.getDescription().toLowerCase().contains(query) ||
                                       tip.getTags().stream().anyMatch(t -> t.toLowerCase().contains(query));
                
                // 2. Match Personal Traits (Skin/Eyes)
                boolean matchesTraits = tip.matches(skin, eyes);

                // 3. Match Products in Collection
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
                // Check if product name or brand contains the requirement (e.g. "Lipstick")
                if (p.getName().toLowerCase().contains(req.toLowerCase()) || 
                    p.getBrand().toLowerCase().contains(req.toLowerCase())) {
                    found = true;
                    break;
                }
            }
            if (!found) return false; // Missing at least one required product type
        }
        return true;
    }

    private void displayResults(List<Tip> results) {
        if (results.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            tvResultsTitle.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.VISIBLE);
            tvNoResults.setText("No looks found that match both your style and the products you own. \n\nTip: Make sure you've added your makeup items to 'My Products'!");
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