package com.example.beautyhub;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.beautyhub.utils.BeautyPost;
import com.example.beautyhub.utils.Comment;
import com.example.beautyhub.utils.CommentsAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class PostDetailActivity extends AppCompatActivity {

    private String postId;
    private String ownerUid;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    private TextView tvTitle, tvDesc, tvOwner, tvDate, tvRole, tvCommentsCount;
    private ImageView ivPost, ivProfile;
    private RecyclerView rvComments;
    private EditText etComment;
    private ImageButton btnSendComment;
    private Comment replyingTo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            finish();
            return;
        }

        initViews();
        loadPostData();
    }

    private void initViews() {
        View postView = findViewById(R.id.included_post);
        tvTitle = postView.findViewById(R.id.tv_post_title);
        tvDesc = postView.findViewById(R.id.tv_post_description);
        tvOwner = postView.findViewById(R.id.tv_post_owner);
        tvDate = postView.findViewById(R.id.tv_post_created_at);
        tvRole = postView.findViewById(R.id.tv_post_role);
        ivPost = postView.findViewById(R.id.iv_post_image);
        ivProfile = postView.findViewById(R.id.iv_owner_profile);
        tvCommentsCount = postView.findViewById(R.id.tv_comments_count);
        rvComments = postView.findViewById(R.id.rv_comments);
        etComment = postView.findViewById(R.id.et_comment);
        btnSendComment = postView.findViewById(R.id.btn_send_comment);
    }

    private void loadPostData() {
        db.collection("posts").document(postId).get().addOnSuccessListener(doc -> {
            BeautyPost post = doc.toObject(BeautyPost.class);
            if (post != null) {
                ownerUid = post.getOwnerUid();
                displayPost(post);
                setupComments();
            }
        });
    }

    private void displayPost(BeautyPost post) {
        tvTitle.setText(post.getTitle());
        tvDesc.setText(post.getDescription());
        tvOwner.setText("By: " + post.getOwnerNickname());

        if (post.isTip()) {
            tvRole.setVisibility(View.VISIBLE);
            tvRole.setText("Guide • Tip");
        }

        if (post.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
            tvDate.setText(sdf.format(post.getCreatedAt().toDate()));
        }

        if (post.getOwnerProfileImageUrl() != null && !post.getOwnerProfileImageUrl().isEmpty()) {
            Glide.with(this).load(post.getOwnerProfileImageUrl()).circleCrop().into(ivProfile);
        }

        if (post.getPostImageUrl() != null && !post.getPostImageUrl().isEmpty()) {
            ivPost.setVisibility(View.VISIBLE);
            Glide.with(this).load(post.getPostImageUrl()).into(ivPost);
        }
    }

    private void setupComments() {
        List<Comment> commentsList = new ArrayList<>();
        CommentsAdapter adapter = new CommentsAdapter(commentsList, ownerUid, postId, parentComment -> {
            replyingTo = parentComment;
            etComment.setHint("Replying to " + parentComment.getUserNickname() + "...");
            etComment.requestFocus();
        });

        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);

        db.collection("posts").document(postId).collection("comments")
                .whereEqualTo("parentCommentId", null)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Comment c = dc.getDocument().toObject(Comment.class);
                            c.setCommentId(dc.getDocument().getId());
                            commentsList.add(c);
                            adapter.notifyItemInserted(commentsList.size() - 1);
                        }
                    }
                    tvCommentsCount.setText(commentsList.size() + " Comments");
                    rvComments.setVisibility(View.VISIBLE);
                });

        btnSendComment.setOnClickListener(v -> {
            String text = etComment.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;

            String uid = auth.getUid();
            db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                String nickname = userDoc.getString("nickname");
                Map<String, Object> map = new HashMap<>();
                map.put("userUid", uid);
                map.put("userNickname", nickname != null ? nickname : "User");
                map.put("text", text);
                map.put("createdAt", Timestamp.now());
                map.put("parentCommentId", replyingTo != null ? replyingTo.getCommentId() : null);

                db.collection("posts").document(postId).collection("comments").add(map)
                        .addOnSuccessListener(ref -> {
                            etComment.setText("");
                            etComment.setHint("Add a comment...");
                            replyingTo = null;
                        });
            });
        });
    }
}