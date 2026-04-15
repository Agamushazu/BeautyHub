package com.example.beautyhub.utils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;
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
    private List<String> tags;
    private boolean isTip;

    public BeautyPost() {}

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
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOwnerUid() { return ownerUid; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }
    public String getOwnerNickname() { return ownerNickname; }
    public void setOwnerNickname(String ownerNickname) { this.ownerNickname = ownerNickname; }
    public String getOwnerProfileImageUrl() { return ownerProfileImageUrl; }
    public void setOwnerProfileImageUrl(String ownerProfileImageUrl) { this.ownerProfileImageUrl = ownerProfileImageUrl; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public String getPostImageUrl() { return postImageUrl; }
    public void setPostImageUrl(String postImageUrl) { this.postImageUrl = postImageUrl; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @PropertyName("isTip")
    public boolean isTip() { return isTip; }

    @PropertyName("isTip")
    public void setIsTip(boolean isTip) { this.isTip = isTip; }

    // Fallback for Firestore field named "tip"
    public void setTip(boolean tip) { this.isTip = tip; }
}