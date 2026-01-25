package com.example.beautyhub;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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

public class ProductCollection extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView rvProducts;
    private MaterialButton btnAddProduct, btnBack;
    private TextView tvEmptyMessage;
    private List<Product> productList = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();
    private ProductAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private boolean isSearching = false;

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
        setupSearch();
        fetchUserCollection();
        fetchAllProductsForSearch();

        btnAddProduct.setOnClickListener(v -> saveSelectedProductsToUserCollection());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        searchView = findViewById(R.id.search_view);
        rvProducts = findViewById(R.id.rv_products);
        btnAddProduct = findViewById(R.id.btn_add_product);
        btnBack = findViewById(R.id.btn_goback);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(productList);
        adapter.setMyCollectionMode(true); // Start in collection view mode
        adapter.setOnProductRemoveListener(this::removeProductFromCollection);
        rvProducts.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    isSearching = false;
                    adapter.setMyCollectionMode(true);
                    adapter.setProducts(productList);
                    updateEmptyMessage();
                    btnAddProduct.setVisibility(View.GONE);
                } else {
                    isSearching = true;
                    adapter.setMyCollectionMode(false);
                    adapter.setProducts(allProducts);
                    adapter.filter(newText);
                    tvEmptyMessage.setVisibility(View.GONE);
                    btnAddProduct.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
    }

    private void fetchUserCollection() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("my_collection")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        productList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            productList.add(doc.toObject(Product.class));
                        }
                        if (!isSearching) {
                            adapter.setProducts(productList);
                            updateEmptyMessage();
                        }
                    }
                });
    }

    private void fetchAllProductsForSearch() {
        db.collection("all_products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                allProducts.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    allProducts.add(doc.toObject(Product.class));
                }
            }
        });
    }

    private void removeProductFromCollection(Product product) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("my_collection")
                .document(product.getId())
                .delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Removed " + product.getName(), Toast.LENGTH_SHORT).show());
    }

    private void saveSelectedProductsToUserCollection() {
        List<Product> selected = adapter.getSelectedProducts();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select products first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        for (Product product : selected) {
            db.collection("users").document(userId)
                    .collection("my_collection")
                    .document(product.getId())
                    .set(product);
        }
        
        Toast.makeText(this, "Added to your collection!", Toast.LENGTH_SHORT).show();
        searchView.setQuery("", true); // Clear search and return to collection view
    }

    private void updateEmptyMessage() {
        if (productList.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }
    }
}