package com.example.travelog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelog.utils.PostsAdapter;
import com.example.travelog.utils.TravelPost;
import com.google.firebase.auth.FirebaseAuth;

// ⭐ ייבואי Firestore נדרשים
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.ArrayList;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "FeedActivity";

    private String nickname;
    private int age;
    private int level;

    private List<TravelPost> posts;

    private TextView tvTitle;
    private Button btnLogout;
    private Button btnAddPost;
    private Button btnGoToPost;


    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitle = findViewById(R.id.tv_title);
        btnLogout = findViewById(R.id.btn_logout);
        btnAddPost = findViewById(R.id.btn_add_post);
        btnGoToPost = findViewById(R.id.btn_gotoposts);

        btnGoToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, MyPostsActivity.class);
                startActivity(intent);
                Toast.makeText(FeedActivity.this, "Navigating to My Post screen...", Toast.LENGTH_SHORT).show();
            }
        });



        btnAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, AddPostActivity.class);
                startActivity(intent);
                Toast.makeText(FeedActivity.this, "Navigating to Add Post screen...", Toast.LENGTH_SHORT).show();
            }
        });

        // לוגיקת Logout
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(FeedActivity.this, com.example.travelog.LoginActivity.class);
                startActivity(intent);
                Toast.makeText(FeedActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        readUserData();
        tvTitle.setText(nickname + " (lvl. " + level + ")");

        posts = new ArrayList<>();

        // זימון אתחול RecyclerView
        initRecyclerView();
        registerToNewPosts(); /////
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postsAdapter = new PostsAdapter(posts);
        recyclerView.setAdapter(postsAdapter);
    }



    private void readUserData(){
        Log.d(TAG, "readUserData: start");
        SharedPreferences sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);
        nickname = sharedPreferences.getString("nickname", "N/A");
        age = sharedPreferences.getInt("age", 0);
        level = sharedPreferences.getInt("level", 0);
    }

    private void registerToNewPosts() {
        Log.d(TAG, "registerToNewPosts: start");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(TAG, "New post: " + dc.getDocument().getData());
                                    TravelPost post = dc.getDocument().toObject(TravelPost.class);
                                    posts.add(0,post);
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified post: " + dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed post: " + dc.getDocument().getData());
                                    break;
                            }
                        }
                        postsAdapter.notifyDataSetChanged();
                    }
                });
    }

}