package com.example.beautyhub.utils;

import com.google.firebase.Timestamp;

public class BeautyPost {
    private String title;
    private String description;
    private String ownerUid;
    private String ownerNickname;
    private String ownerProfileImageUrl;
    private Timestamp createdAt;
    private String postImageUrl;

    public BeautyPost() {}

    public BeautyPost(String title, String description, String ownerUid, String ownerNickname, String ownerProfileImageUrl, Timestamp createdAt, String postImageUrl) {
        this.title = title;
        this.description = description;
        this.ownerUid = ownerUid;
        this.ownerNickname = ownerNickname;
        this.ownerProfileImageUrl = ownerProfileImageUrl;
        this.createdAt = createdAt;
        this.postImageUrl = postImageUrl;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOwnerUid() { return ownerUid; }
    public String getOwnerNickname() { return ownerNickname; }
    public String getOwnerProfileImageUrl() { return ownerProfileImageUrl; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getPostImageUrl() { return postImageUrl; }
}