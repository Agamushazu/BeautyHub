package com.example.beautyhub.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.beautyhub.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private String postOwnerUid;
    private String postId;
    private OnReplyClickListener replyClickListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnReplyClickListener {
        void onReplyClick(Comment parentComment);
    }

    public CommentsAdapter(List<Comment> comments, String postOwnerUid, String postId, OnReplyClickListener listener) {
        this.comments = comments;
        this.postOwnerUid = postOwnerUid;
        this.postId = postId;
        this.replyClickListener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.tvUser.setText(comment.getUserNickname());
        holder.tvText.setText(comment.getText());

        if (comment.getUserUid() != null && comment.getUserUid().equals(postOwnerUid)) {
            holder.tvCreatorTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvCreatorTag.setVisibility(View.GONE);
        }

        if (comment.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
            holder.tvDate.setText(sdf.format(comment.getCreatedAt().toDate()));
        }

        holder.tvReply.setOnClickListener(v -> {
            if (replyClickListener != null) {
                replyClickListener.onReplyClick(comment);
            }
        });

        // Handle nested replies only for top-level comments to avoid infinite nesting depth visually
        if (comment.getParentCommentId() == null) {
            loadReplies(holder, comment);
        } else {
            holder.rvReplies.setVisibility(View.GONE);
            holder.tvReply.setVisibility(View.GONE); // Optional: disable nested replies to replies for simplicity
        }
    }

    private void loadReplies(CommentViewHolder holder, Comment parentComment) {
        List<Comment> repliesList = new ArrayList<>();
        // Note: Using parentCommentId=null for this specific adapter instance, 
        // but we pass null for listener to disable further nesting if desired
        CommentsAdapter repliesAdapter = new CommentsAdapter(repliesList, postOwnerUid, postId, null);
        holder.rvReplies.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.rvReplies.setAdapter(repliesAdapter);

        db.collection("posts").document(postId).collection("comments")
                .whereEqualTo("parentCommentId", parentComment.getCommentId())
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Comment reply = dc.getDocument().toObject(Comment.class);
                            reply.setCommentId(dc.getDocument().getId());
                            repliesList.add(reply);
                            repliesAdapter.notifyItemInserted(repliesList.size() - 1);
                        }
                    }
                    holder.rvReplies.setVisibility(repliesList.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvText, tvDate, tvCreatorTag, tvReply;
        RecyclerView rvReplies;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tv_comment_user);
            tvText = itemView.findViewById(R.id.tv_comment_text);
            tvDate = itemView.findViewById(R.id.tv_comment_date);
            tvCreatorTag = itemView.findViewById(R.id.tv_creator_tag);
            tvReply = itemView.findViewById(R.id.tv_reply);
            rvReplies = itemView.findViewById(R.id.rv_replies);
        }
    }
}