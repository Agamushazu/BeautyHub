package com.example.beautyhub.utils;

import com.google.firebase.Timestamp;
import java.util.List;

public class BeautyPost {
    private String postId;
    private String title;
    private String description;
    private String ownerUid;
    private String ownerNickname;
    private String ownerProfileImageUrl;
    private Timestamp createdAt;
    private String postImageUrl;
    private List<String> tags; // Updated to list of strings for multiple categories
    private boolean isTip;     // Explicit flag if it's a guide tip

    public BeautyPost() {}

    public BeautyPost(String postId, String title, String description, String ownerUid, String ownerNickname, String ownerProfileImageUrl, Timestamp createdAt, String postImageUrl) {
        this(postId, title, description, ownerUid, ownerNickname, ownerProfileImageUrl, createdAt, postImageUrl, null, false);
    }

    public BeautyPost(String postId, String title, String description, String ownerUid, String ownerNickname, String ownerProfileImageUrl, Timestamp createdAt, String postImageUrl, List<String> tags, boolean isTip) {
        this.postId = postId;
        this.title = title;
        this.description = description;
        this.ownerUid = ownerUid;
        this.ownerNickname = ownerNickname;
        this.ownerProfileImageUrl = ownerProfileImageUrl;
        this.createdAt = createdAt;
        this.postImageUrl = postImageUrl;
        this.tags = tags;
        this.isTip = isTip;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOwnerUid() { return ownerUid; }
    public String getOwnerNickname() { return ownerNickname; }
    public String getOwnerProfileImageUrl() { return ownerProfileImageUrl; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getPostImageUrl() { return postImageUrl; }
    public List<String> getTags() { return tags; }
    public boolean isTip() { return isTip; }
}