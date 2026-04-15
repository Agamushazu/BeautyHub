package com.example.beautyhub;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.beautyhub.utils.BeautyPost;
import com.example.beautyhub.utils.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TabLayout tabLayout;
    private FirebaseFirestore db;
    private AdminAdapter adapter;
    private List<Object> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.rv_admin_list);
        tabLayout = findViewById(R.id.admin_tabs);
        MaterialButton btnBack = findViewById(R.id.btn_admin_back);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminAdapter();
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                loadData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnBack.setOnClickListener(v -> finish());
        loadData();
    }

    private void loadData() {
        itemList.clear();
        int position = tabLayout.getSelectedTabPosition();
        String collection;
        if (position == 0) {
            collection = "users";
        } else if (position == 1) {
            collection = "posts";
        } else {
            collection = "products";
        }

        db.collection(collection).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                if (position == 0) {
                    Map<String, Object> userData = doc.getData();
                    if (userData != null) {
                        Map<String, Object> userMap = new HashMap<>(userData);
                        userMap.put("uid", doc.getId());
                        itemList.add(userMap);
                    }
                } else if (position == 1) {
                    BeautyPost post = doc.toObject(BeautyPost.class);
                    if (post != null) {
                        post.setPostId(doc.getId());
                        itemList.add(post);
                    }
                } else {
                    Product product = doc.toObject(Product.class);
                    if (product != null) {
                        if (product.getId() == null) product.setId(doc.getId());
                        itemList.add(product);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(AdminActivity.this, "Error loading data", Toast.LENGTH_SHORT).show());
    }

    private class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // שינוי ל-layout שכולל ImageView
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Object item = itemList.get(position);
            
            // הסתרת אלמנטים שלא רלוונטיים לתצוגת Admin פשוטה בתוך item_product
            holder.itemView.findViewById(R.id.cb_selected).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.btn_favorite).setVisibility(View.GONE);
            View btnRemove = holder.itemView.findViewById(R.id.btn_remove);
            btnRemove.setVisibility(View.VISIBLE);

            if (item instanceof Map) {
                Map<String, Object> user = (Map<String, Object>) item;
                holder.text1.setText(String.valueOf(user.get("nickname")));
                holder.text2.setText("Email: " + user.get("email"));
                holder.ivImage.setImageResource(android.R.drawable.ic_menu_report_image);
                btnRemove.setOnClickListener(v -> deleteUser(user));
                holder.itemView.setOnClickListener(v -> showUserOptions(user));
            } else if (item instanceof BeautyPost) {
                BeautyPost post = (BeautyPost) item;
                holder.text1.setText(post.getTitle());
                holder.text2.setText("By: " + post.getOwnerNickname());
                // Fixed: use getPostImageUrl() instead of getImageUrl()
                Glide.with(holder.itemView.getContext()).load(post.getPostImageUrl()).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivImage);
                btnRemove.setOnClickListener(v -> showPostOptions(post));
                holder.itemView.setOnClickListener(v -> showPostOptions(post));
            } else if (item instanceof Product) {
                Product product = (Product) item;
                holder.text1.setText(product.getName());
                holder.text2.setText(product.getBrand());
                Glide.with(holder.itemView.getContext()).load(product.getImageUrl()).placeholder(android.R.drawable.ic_menu_gallery).into(holder.ivImage);
                btnRemove.setOnClickListener(v -> showProductOptions(product));
                holder.itemView.setOnClickListener(v -> showProductOptions(product));
            }
        }

        @Override public int getItemCount() { return itemList.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ImageView ivImage;
            ViewHolder(View v) {
                super(v);
                text1 = v.findViewById(R.id.tv_product_name);
                text2 = v.findViewById(R.id.tv_product_brand);
                ivImage = v.findViewById(R.id.iv_product);
            }
        }
    }

    private void showUserOptions(Map<String, Object> user) {
        String[] options = {"Edit Details", "Delete User"};
        new AlertDialog.Builder(this)
                .setTitle("Manage User: " + user.get("nickname"))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) editUserDetails(user);
                    else deleteUser(user);
                }).show();
    }

    private void editUserDetails(Map<String, Object> user) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        EditText etName = new EditText(this);
        etName.setHint("Nickname");
        etName.setText(String.valueOf(user.get("nickname")));
        layout.addView(etName);

        EditText etAge = new EditText(this);
        etAge.setHint("Age");
        etAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        etAge.setText(String.valueOf(user.get("age") != null ? user.get("age") : ""));
        layout.addView(etAge);

        new AlertDialog.Builder(this)
                .setTitle("Edit User")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("nickname", etName.getText().toString());
                    try {
                        updates.put("age", Integer.parseInt(etAge.getText().toString()));
                    } catch (Exception ignored) {}
                    
                    db.collection("users").document(String.valueOf(user.get("uid"))).update(updates)
                            .addOnSuccessListener(aVoid -> loadData());
                }).show();
    }

    private void deleteUser(Map<String, Object> user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("users").document(String.valueOf(user.get("uid"))).delete()
                            .addOnSuccessListener(aVoid -> loadData());
                })
                .setNegativeButton("No", null).show();
    }

    private void showPostOptions(BeautyPost post) {
        new AlertDialog.Builder(this)
                .setTitle("Manage Post")
                .setMessage("Do you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("posts").document(post.getPostId()).delete()
                            .addOnSuccessListener(aVoid -> loadData());
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showProductOptions(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Manage Product")
                .setMessage("Are you sure you want to delete " + product.getName() + " from the database?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("products").document(product.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AdminActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                                loadData();
                            })
                            .addOnFailureListener(e -> Toast.makeText(AdminActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }
}