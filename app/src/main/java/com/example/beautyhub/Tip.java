package com.example.beautyhub;

import java.util.ArrayList;
import java.util.List;

public class Tip {
    private String title;
    private String description;
    private int imageResId;
    private List<String> tags; // e.g., "Brown", "Blue", "Fair", "Oily"

    public Tip(String title, String description, int imageResId) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
        this.tags = new ArrayList<>();
    }

    public Tip(String title, String description, int imageResId, List<String> tags) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
        this.tags = tags;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
    public List<String> getTags() { return tags; }

    public boolean matches(String... userTraits) {
        if (tags == null || userTraits == null) return false;
        for (String trait : userTraits) {
            if (trait != null && tags.contains(trait)) {
                return true;
            }
        }
        return false;
    }
}