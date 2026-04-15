package com.example.beautyhub.utils;

import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.beautyhub.OtherUserProfileActivity;
import com.example.beautyhub.PostDetailActivity;
import com.example.beautyhub.R;
import com.google.android.material.card.MaterialCardView;
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

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private List<BeautyPost> posts;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private Comment replyingTo = null;

    public PostsAdapter(List<BeautyPost> posts) { this.posts = posts; }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        BeautyPost post = posts.get(position);
        holder.tvTitle.setText(post.getTitle());
        holder.tvDesc.setText(post.getDescription());
        holder.tvOwner.setText(post.getOwnerNickname());

        // לוגיקה חזקה לזיהוי פוסט מדריך:
        // אם השדה isTip הוא אמת, או אם פשוט יש תגיות בפוסט
        boolean representsGuidePost = post.isTip() || (post.getTags() != null && !post.getTags().isEmpty());

        if (representsGuidePost) {
            // 1. הצגת תגית המדריך מתחת לשם
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.tvRole.setText("OFFICIAL GUIDE");
            
            // 2. הוספת מסגרת ושינוי צבע רקע כדי להבדיל משאר הפוסטים
            holder.cardContainer.setStrokeWidth(6); // מסגרת עבה
            holder.cardContainer.setStrokeColor(Color.parseColor("#A64452")); // צבע ורוד כהה
            holder.cardContainer.setCardBackgroundColor(Color.parseColor("#FFF0F3")); // רקע ורדרד עדין
            
            // 3. הצגת תגיות כ-Hashtags מעל הכותרת
            List<String> tags = post.getTags();
            if (tags != null && !tags.isEmpty()) {
                holder.tvTags.setVisibility(View.VISIBLE);
                StringBuilder hashtags = new StringBuilder();
                for (String tag : tags) {
                    hashtags.append("#").append(tag.replace(" ", "")).append(" ");
                }
                holder.tvTags.setText(hashtags.toString().trim());
            } else {
                holder.tvTags.setVisibility(View.GONE);
            }
        } else {
            // עיצוב פוסט רגיל
            holder.tvRole.setVisibility(View.GONE);
            holder.tvTags.setVisibility(View.GONE);
            holder.cardContainer.setStrokeWidth(0);
            holder.cardContainer.setCardBackgroundColor(Color.WHITE);
        }

        if (post.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem")); 
            holder.tvDate.setText(sdf.format(post.getCreatedAt().toDate()));
        }

        // Profile Image
        String profPath = post.getOwnerProfileImageUrl();
        if (profPath != null && !profPath.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profPath)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_launcher_background);
        }

        // Post Image
        if (post.getPostImageUrl() != null && !post.getPostImageUrl().isEmpty()) {
            holder.ivPost.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getPostImageUrl())
                    .centerCrop()
                    .into(holder.ivPost);
        } else {
            holder.ivPost.setVisibility(View.GONE);
        }

        // Click listeners
        View.OnClickListener openProfileListener = v -> {
            Intent intent = new Intent(holder.itemView.getContext(), OtherUserProfileActivity.class);
            intent.putExtra("userId", post.getOwnerUid());
            holder.itemView.getContext().startActivity(intent);
        };
        holder.tvOwner.setOnClickListener(openProfileListener);
        holder.ivProfile.setOnClickListener(openProfileListener);

        View.OnClickListener openDetailListener = v -> {
            Intent intent = new Intent(holder.itemView.getContext(), PostDetailActivity.class);
            intent.putExtra("postId", post.getPostId());
            holder.itemView.getContext().startActivity(intent);
        };

        holder.itemView.setOnClickListener(openDetailListener);
        holder.tvTitle.setOnClickListener(openDetailListener);
        holder.tvDesc.setOnClickListener(openDetailListener);
        holder.ivPost.setOnClickListener(openDetailListener);
        holder.tvCommentsCount.setOnClickListener(openDetailListener);

        setupComments(holder, post);
    }

    private void setupComments(PostViewHolder holder, BeautyPost post) {
        List<Comment> commentsList = new ArrayList<>();
        CommentsAdapter commentsAdapter = new CommentsAdapter(commentsList, post.getOwnerUid(), post.getPostId(), parentComment -> {
            replyingTo = parentComment;
            holder.etComment.setHint("Replying to " + parentComment.getUserNickname() + "...");
            holder.etComment.requestFocus();
        });
        
        holder.rvComments.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvComments.setAdapter(commentsAdapter);

        if (post.getPostId() != null) {
            db.collection("posts").document(post.getPostId()).collection("comments")
                    .whereEqualTo("parentCommentId", null)
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .limit(3) 
                    .addSnapshotListener((value, error) -> {
                        if (error != null || value == null) return;
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Comment c = dc.getDocument().toObject(Comment.class);
                                c.setCommentId(dc.getDocument().getId());
                                commentsList.add(c);
                                commentsAdapter.notifyItemInserted(commentsList.size() - 1);
                            }
                        }
                        holder.tvCommentsCount.setText(commentsList.size() + " Comments (Click to see more)");
                        holder.rvComments.setVisibility(commentsList.isEmpty() ? View.GONE : View.VISIBLE);
                    });
        }

        holder.btnSendComment.setOnClickListener(v -> {
            String commentText = holder.etComment.getText().toString().trim();
            if (TextUtils.isEmpty(commentText)) return;

            if (auth.getCurrentUser() == null) {
                Toast.makeText(holder.itemView.getContext(), "Please login to comment", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = auth.getCurrentUser().getUid();
            
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                String nickname = documentSnapshot.getString("nickname");
                if (nickname == null) nickname = "User";

                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("userUid", uid);
                commentMap.put("userNickname", nickname);
                commentMap.put("text", commentText);
                commentMap.put("createdAt", Timestamp.now());
                commentMap.put("parentCommentId", replyingTo != null ? replyingTo.getCommentId() : null);

                db.collection("posts").document(post.getPostId()).collection("comments")
                        .add(commentMap)
                        .addOnSuccessListener(documentReference -> {
                            holder.etComment.setText("");
                            holder.etComment.setHint("Add a comment...");
                            replyingTo = null;
                        });
            });
        });
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvOwner, tvDate, tvRole, tvCommentsCount, tvTags;
        ImageView ivPost, ivProfile;
        RecyclerView rvComments;
        EditText etComment;
        ImageButton btnSendComment;
        MaterialCardView cardContainer;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvDesc = itemView.findViewById(R.id.tv_post_description);
            tvOwner = itemView.findViewById(R.id.tv_post_owner);
            tvDate = itemView.findViewById(R.id.tv_post_created_at);
            tvRole = itemView.findViewById(R.id.tv_post_role);
            tvTags = itemView.findViewById(R.id.tv_post_tags);
            ivPost = itemView.findViewById(R.id.iv_post_image);
            ivProfile = itemView.findViewById(R.id.iv_owner_profile);
            cardContainer = itemView.findViewById(R.id.post_card_container);
            
            tvCommentsCount = itemView.findViewById(R.id.tv_comments_count);
            rvComments = itemView.findViewById(R.id.rv_comments);
            etComment = itemView.findViewById(R.id.et_comment);
            btnSendComment = itemView.findViewById(R.id.btn_send_comment);
        }
    }
}