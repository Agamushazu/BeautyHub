package com.example.beautyhub.utils;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentId;
    private String userUid;
    private String userNickname;
    private String text;
    private Timestamp createdAt;
    private String parentCommentId; // ID of the comment this is replying to, null if it's a top-level comment

    public Comment() {}

    public Comment(String commentId, String userUid, String userNickname, String text, Timestamp createdAt) {
        this(commentId, userUid, userNickname, text, createdAt, null);
    }

    public Comment(String commentId, String userUid, String userNickname, String text, Timestamp createdAt, String parentCommentId) {
        this.commentId = commentId;
        this.userUid = userUid;
        this.userNickname = userNickname;
        this.text = text;
        this.createdAt = createdAt;
        this.parentCommentId = parentCommentId;
    }

    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public String getUserUid() { return userUid; }
    public String getUserNickname() { return userNickname; }
    public String getText() { return text; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getParentCommentId() { return parentCommentId; }
}