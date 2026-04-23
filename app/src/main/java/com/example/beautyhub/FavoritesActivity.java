package com.example.beautyhub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beautyhub.utils.Product;
import com.example.beautyhub.utils.ProductAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private TextView tvEmptyMessage, tvTitleMyCollection, tvTitleAllProducts;
    private MaterialButton btnBack;
    
    private List<Product> favoritesList = new ArrayList<>();
    private List<Product> allProductsCatalog = new ArrayList<>();
    private ProductAdapter adapter;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_collection); 

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        fetchAllCatalogProducts(); // First fetch catalog, then favorites

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvFavorites = findViewById(R.id.rv_all_products); 
        tvEmptyMessage = findViewById(R.id.tv_empty_my_collection);
        btnBack = findViewById(R.id.btn_goback);
        
        tvTitleMyCollection = findViewById(R.id.tv_title_my_collection);
        tvTitleAllProducts = findViewById(R.id.tv_title_all_products);
        
        // Hide elements not needed in Favorites
        findViewById(R.id.search_view).setVisibility(View.GONE);
        findViewById(R.id.btn_add_product).setVisibility(View.GONE);
        findViewById(R.id.rv_my_collection).setVisibility(View.GONE);
        
        // Update headers to show ONLY "My Favorites"
        tvTitleMyCollection.setVisibility(View.VISIBLE);
        tvTitleMyCollection.setText("My Favorites");
        tvTitleAllProducts.setVisibility(View.GONE);
        
        tvEmptyMessage.setText("You haven't favorited any products yet!");
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(favoritesList);
        adapter.setMyCollectionMode(false); 
        rvFavorites.setAdapter(adapter);
    }

    private void fetchAllCatalogProducts() {
        db.collection("products").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("FavoritesActivity", "Listen to products failed", error);
                return;
            }
            if (value != null) {
                allProductsCatalog.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Product p = doc.toObject(Product.class);
                    if (p.getId() == null) p.setId(doc.getId());
                    allProductsCatalog.add(p);
                }
                fetchFavoriteIds(); // Fetch favorites after catalog is ready
            }
        });
    }

    private void fetchFavoriteIds() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        
        db.collection("users").document(userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("FavoritesActivity", "Listen to user doc failed", error);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        List<String> favIds = (List<String>) snapshot.get("favorite_ids");
                        updateFavoritesUI(favIds != null ? favIds : new ArrayList<>());
                    } else {
                        updateFavoritesUI(new ArrayList<>());
                    }
                });
    }

    private void updateFavoritesUI(List<String> ids) {
        favoritesList.clear();
        for (Product p : allProductsCatalog) {
            if (ids.contains(p.getId())) {
                favoritesList.add(p);
            }
        }
        adapter.setProducts(favoritesList);
        updateEmptyUI();
    }

    private void updateEmptyUI() {
        if (favoritesList.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }
}
