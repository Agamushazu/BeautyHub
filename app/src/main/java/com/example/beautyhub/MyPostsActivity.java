package com.example.beautyhub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beautyhub.utils.PostsAdapter;
import com.example.beautyhub.utils.BeautyPost;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    private List<BeautyPost> posts;
    private PostsAdapter postsAdapter;
    private RecyclerView recyclerView;
    private TextView tvTitle;
    private MaterialButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        tvTitle = findViewById(R.id.tv_title);
        btnBack = findViewById(R.id.btn_goback);
        recyclerView = findViewById(R.id.recycler_posts);

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        String nickname = sp.getString("nickname", "User");
        tvTitle.setText(nickname + "'s Posts");

        // כאן אנחנו סוגרים את המסך וחוזרים אוטומטית ל-Feed
        btnBack.setOnClickListener(v -> finish());

        posts = new ArrayList<>();
        postsAdapter = new PostsAdapter(posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postsAdapter);

        loadMyPosts();
    }

    private void loadMyPosts() {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null) return;

        FirebaseFirestore.getInstance().collection("posts")
                .whereEqualTo("ownerUid", currentUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("MyPosts", "Error. Link in Logcat?", e);
                        return;
                    }
                    if (snapshots != null) {
                        posts.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            posts.add(doc.toObject(BeautyPost.class));
                        }
                        postsAdapter.notifyDataSetChanged();
                    }
                });
    }
}