package com.example.beautyhub.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.beautyhub.R;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private List<BeautyPost> posts;

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
        holder.tvOwner.setText("By: " + post.getOwnerNickname());

        // הצגת תג "Guide" אם הפוסט הוא טיפ
        if (post.isTip()) {
            holder.tvRole.setVisibility(View.VISIBLE);
            holder.tvRole.setText("Guide • Tip");
        } else {
            holder.tvRole.setVisibility(View.GONE);
        }

        if (post.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem")); 
            holder.tvDate.setText(sdf.format(post.getCreatedAt().toDate()));
        }

        // תמונת פרופיל
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

        // תמונת פוסט
        if (post.getPostImageUrl() != null && !post.getPostImageUrl().isEmpty()) {
            holder.ivPost.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getPostImageUrl())
                    .centerCrop()
                    .into(holder.ivPost);
        } else {
            holder.ivPost.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvOwner, tvDate, tvRole;
        ImageView ivPost, ivProfile;
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvDesc = itemView.findViewById(R.id.tv_post_description);
            tvOwner = itemView.findViewById(R.id.tv_post_owner);
            tvDate = itemView.findViewById(R.id.tv_post_created_at);
            tvRole = itemView.findViewById(R.id.tv_post_role);
            ivPost = itemView.findViewById(R.id.iv_post_image);
            ivProfile = itemView.findViewById(R.id.iv_owner_profile);
        }
    }
}