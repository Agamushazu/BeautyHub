package com.example.beautyhub;

import android.os.Bundle;
import android.util.Log;
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
import com.example.beautyhub.utils.ProductSeeder;
import com.example.beautyhub.utils.TipSeeder;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ProductCollection extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView rvMyCollection, rvAllProducts;
    private MaterialButton btnAddProduct, btnBack;
    private TextView tvEmptyMyCollection;
    
    private ProductAdapter adapterMyCollection, adapterAllProducts;
    private List<Product> myProductsList = new ArrayList<>();
    private List<Product> allProductsCatalog = new ArrayList<>();
    
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
        setupRecyclerViews();
        setupSearch();
        fetchUserCollection();
        fetchAllCatalogProducts();

        // RUN THESE ONCE TO UPLOAD DATA, THEN COMMENT THEM OUT
        // ProductSeeder.seedDatabase();
        
        // Pass 'this' as context to see errors on screen
        TipSeeder.seedTips(this);

        btnAddProduct.setOnClickListener(v -> saveSelectedToCollection());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        searchView = findViewById(R.id.search_view);
        rvMyCollection = findViewById(R.id.rv_my_collection);
        rvAllProducts = findViewById(R.id.rv_all_products);
        btnAddProduct = findViewById(R.id.btn_add_product);
        btnBack = findViewById(R.id.btn_goback);
        tvEmptyMyCollection = findViewById(R.id.tv_empty_my_collection);
    }

    private void setupRecyclerViews() {
        rvMyCollection.setLayoutManager(new LinearLayoutManager(this));
        adapterMyCollection = new ProductAdapter(myProductsList);
        adapterMyCollection.setMyCollectionMode(true);
        adapterMyCollection.setOnProductRemoveListener(this::removeProductFromCollection);
        rvMyCollection.setAdapter(adapterMyCollection);

        rvAllProducts.setLayoutManager(new LinearLayoutManager(this));
        adapterAllProducts = new ProductAdapter(allProductsCatalog);
        adapterAllProducts.setMyCollectionMode(false);
        rvAllProducts.setAdapter(adapterAllProducts);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapterAllProducts.filter(newText);
                return true;
            }
        });
    }

    private void fetchUserCollection() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("my_collection")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ProductCollection", "Listen to user collection failed", error);
                        return;
                    }
                    if (value != null) {
                        myProductsList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Product p = doc.toObject(Product.class);
                            if (p.getId() == null) p.setId(doc.getId());
                            myProductsList.add(p);
                        }
                        adapterMyCollection.setProducts(myProductsList);
                        tvEmptyMyCollection.setVisibility(myProductsList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    private void fetchAllCatalogProducts() {
        db.collection("products").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("ProductCollection", "Listen to products failed", error);
                return;
            }
            if (value != null) {
                allProductsCatalog.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Product p = doc.toObject(Product.class);
                    if (p.getId() == null) p.setId(doc.getId());
                    allProductsCatalog.add(p);
                }
                adapterAllProducts.setProducts(allProductsCatalog);
            }
        });
    }

    private void saveSelectedToCollection() {
        List<Product> selected = adapterAllProducts.getSelectedProducts();
        if (selected.isEmpty()) {
            Toast.makeText(this, "Please select products from 'All Products'", Toast.LENGTH_SHORT).show();
            return;
        }

        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        for (Product product : selected) {
            db.collection("users").document(userId)
                    .collection("my_collection")
                    .document(product.getId())
                    .set(product);
        }
        Toast.makeText(this, "Added to your collection!", Toast.LENGTH_SHORT).show();
    }

    private void removeProductFromCollection(Product product) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("my_collection")
                .document(product.getId())
                .delete();
    }
}
