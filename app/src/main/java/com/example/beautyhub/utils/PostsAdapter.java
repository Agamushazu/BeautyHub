package com.example.beautyhub.utils;


import android.util.Log;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private static final String TAG = "PostsAdapter";

    private List<TravelPost> posts;


    public PostsAdapter(List<TravelPost> postsList) {
        this.posts = (postsList != null) ? postsList : new ArrayList<>();
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post, parent, false);
        return new PostViewHolder(view);
    }

    // ⭐ onBindViewHolder מעודכן עם לוגיקת טעינת תמונת פרופיל
    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        TravelPost post = posts.get(position);

        holder.titleTextView.setText(post.getTitle());
        holder.descriptionTextView.setText(post.getDescription());
        holder.ownerNicknameTextView.setText("By: " + post.getOwnerNickname());

        if (post.getCreatedAt() != null) {
            holder.timestampTextView.setText(timestampToString(post.getCreatedAt()));
        } else {
            holder.timestampTextView.setText("Date N/A");
        }

        // ❌ שורות טעינת תמונה שרירותית נמחקו (או הוחלפו)
        // holder.postImageView.setImageResource(R.drawable.ic_launcher_background);

        // ⭐ לוגיקת טעינת תמונת הפרופיל באמצעות Supabase ו-Glide
        String profilePicturePath = "images/profile-pics/" + post.getOwnerUid() + ".jpg";
        String profilePictureUrl = SupabaseStorageHelper.getFileSupabaseUrl(profilePicturePath);

        Glide.with(holder.itemView)
                .load(profilePictureUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.postImageView); // 👈 שימו לב ששם המשתנה שבו השתמשנו הוא postImageView
        // אם התכוונת ל-imageViewOwnerPic, אנא עדכן את PostViewHolder.
    }

    @Override
    public int getItemCount() {
        int count = posts != null ? posts.size() : 0;
        Log.d(TAG, "getItemCount: Number of items in posts list is " + count);
        return count;
    }

    // ... (timestampToString)

    private String timestampToString(Timestamp timestamp) {
        Date messageDate = timestamp.toDate();
        boolean isToday = android.text.format.DateUtils.isToday(messageDate.getTime());
        SimpleDateFormat fmt;
        if (isToday) {
            fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        } else {
            fmt = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        }
        return fmt.format(messageDate);
    }

    // ... (setPosts)
    public void setPosts(List<TravelPost> newPostList) {
        this.posts = (newPostList != null) ? newPostList : new ArrayList<>();
        notifyDataSetChanged();
    }


    static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        TextView descriptionTextView;
        TextView ownerNicknameTextView;
        TextView timestampTextView;

        ImageView postImageView;


        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.tv_post_title);
            descriptionTextView = itemView.findViewById(R.id.tv_post_description);
            ownerNicknameTextView = itemView.findViewById(R.id.tv_post_owner);
            timestampTextView = itemView.findViewById(R.id.tv_post_created_at);
            postImageView = itemView.findViewById(R.id.iv_post_image);
        }
    }
}