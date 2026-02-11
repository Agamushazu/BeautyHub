package com.example.beautyhub.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.beautyhub.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private List<Product> filteredList;
    private Set<Product> selectedProducts = new HashSet<>();
    private Set<String> favoriteIds = new HashSet<>();
    private Set<String> collectionIds = new HashSet<>(); // New: Tracks products already in collection
    private OnProductRemoveListener removeListener;
    private boolean isMyCollectionMode = false;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId = FirebaseAuth.getInstance().getUid();

    public interface OnProductRemoveListener {
        void onRemove(Product product);
    }

    public ProductAdapter(List<Product> products) {
        this.products = products;
        this.filteredList = new ArrayList<>(products);
        fetchFavorites();
        fetchCollectionIds(); // Listen to my_collection
    }

    private void fetchFavorites() {
        if (userId == null) return;
        db.collection("users").document(userId).collection("favorites")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        favoriteIds.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            favoriteIds.add(doc.getId());
                        }
                        notifyDataSetChanged();
                    }
                });
    }

    private void fetchCollectionIds() {
        if (userId == null) return;
        db.collection("users").document(userId).collection("my_collection")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        collectionIds.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            collectionIds.add(doc.getId());
                        }
                        notifyDataSetChanged();
                    }
                });
    }

    public void setOnProductRemoveListener(OnProductRemoveListener listener) {
        this.removeListener = listener;
    }

    public void setMyCollectionMode(boolean isMyCollectionMode) {
        this.isMyCollectionMode = isMyCollectionMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvBrand.setText(product.getBrand());
        
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.ivProduct);

        // Favorite logic
        boolean isFav = favoriteIds.contains(product.getId());
        holder.btnFavorite.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        holder.btnFavorite.setOnClickListener(v -> toggleFavorite(product, isFav));

        if (isMyCollectionMode) {
            holder.checkBox.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.VISIBLE);
            holder.btnRemove.setOnClickListener(v -> {
                if (removeListener != null) removeListener.onRemove(product);
            });
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.btnRemove.setVisibility(View.GONE);
            
            holder.checkBox.setOnCheckedChangeListener(null);
            
            // Logic: Checkbox is checked if it's already in collection OR currently selected
            boolean alreadyInCollection = collectionIds.contains(product.getId());
            holder.checkBox.setChecked(alreadyInCollection || selectedProducts.contains(product));
            
            // If it's already in collection, we can disable the checkbox or just let it stay checked
            holder.checkBox.setEnabled(!alreadyInCollection); 

            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedProducts.add(product);
                else selectedProducts.remove(product);
            });
        }
    }

    private void toggleFavorite(Product product, boolean isCurrentlyFav) {
        if (userId == null) return;
        if (isCurrentlyFav) {
            db.collection("users").document(userId).collection("favorites").document(product.getId()).delete();
        } else {
            db.collection("users").document(userId).collection("favorites").document(product.getId()).set(product);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(products);
        } else {
            for (Product p : products) {
                if (p.getName().toLowerCase().contains(query.toLowerCase()) || 
                    p.getBrand().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<Product> getSelectedProducts() {
        return new ArrayList<>(selectedProducts);
    }

    public void setProducts(List<Product> newProducts) {
        this.products = newProducts;
        this.filteredList = new ArrayList<>(newProducts);
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvBrand;
        CheckBox checkBox;
        ImageButton btnRemove, btnFavorite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvBrand = itemView.findViewById(R.id.tv_product_brand);
            checkBox = itemView.findViewById(R.id.cb_selected);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}
