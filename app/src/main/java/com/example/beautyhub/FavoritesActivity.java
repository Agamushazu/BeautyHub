package com.example.beautyhub;

import android.os.Bundle;
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
    private TextView tvEmptyMessage;
    private MaterialButton btnBack;
    private List<Product> favoritesList = new ArrayList<>();
    private ProductAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_collection); // Reusing the layout since it fits perfectly

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupRecyclerView();
        fetchFavorites();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvFavorites = findViewById(R.id.rv_products);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        btnBack = findViewById(R.id.btn_goback);
        
        // Hide things we don't need in Favorites page
        findViewById(R.id.search_view).setVisibility(View.GONE);
        findViewById(R.id.btn_add_product).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.tv_title)).setText("My Favorite Products");
        tvEmptyMessage.setText("You haven't favorited any products yet!");
    }

    private void setupRecyclerView() {
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(favoritesList);
        // We don't need selection or remove button here, just the heart
        adapter.setMyCollectionMode(false); 
        rvFavorites.setAdapter(adapter);
    }

    private void fetchFavorites() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("favorites")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        favoritesList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            favoritesList.add(doc.toObject(Product.class));
                        }
                        adapter.setProducts(favoritesList);
                        updateEmptyUI();
                    }
                });
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