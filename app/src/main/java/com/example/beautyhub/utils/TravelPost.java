package com.example.beautyhub.utils;

import com.google.firebase.Timestamp;

public class TravelPost {
    private String title;
    private String description;
    private String ownerUid;
    private String ownerNickname;
    private Timestamp createdAt;

    // בנאי ריק (חובה ל-Firebase)
    public TravelPost() {
    }

    // בנאי מלא
    public TravelPost(String title, String description, String ownerUid, String ownerNickname, Timestamp createdAt) {
        this.title = title;
        this.description = description;
        this.ownerUid = ownerUid;
        this.ownerNickname = ownerNickname;
        this.createdAt = createdAt;
    }

    // גטרים (חובה ל-Firebase)
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOwnerUid() { return ownerUid; }
    public String getOwnerNickname() { return ownerNickname; }
    public Timestamp getCreatedAt() { return createdAt; }

    // סטרים (אופציונלי)
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }
    public void setOwnerNickname(String ownerNickname) { this.ownerNickname = ownerNickname; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
