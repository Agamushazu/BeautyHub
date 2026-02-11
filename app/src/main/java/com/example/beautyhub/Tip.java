package com.example.beautyhub;

import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.List;

public class Tip {
    private String title;
    private String description;
    private String category;
    private String categoryValue;
    private String videoUrl;
    private int imageResId;
    private List<String> tags = new ArrayList<>();
    private List<String> requiredProducts = new ArrayList<>();

    // Required empty constructor for Firebase
    public Tip() {}

    @PropertyName("title")
    public String getTitle() { return title; }
    @PropertyName("title")
    public void setTitle(String title) { this.title = title; }

    @PropertyName("description")
    public String getDescription() { return description; }
    @PropertyName("description")
    public void setDescription(String description) { this.description = description; }

    @PropertyName("Category")
    public String getCategory() { return category; }
    @PropertyName("Category")
    public void setCategory(String category) { this.category = category; }

    @PropertyName("CategoryValue")
    public String getCategoryValue() { return categoryValue; }
    @PropertyName("CategoryValue")
    public void setCategoryValue(String categoryValue) { this.categoryValue = categoryValue; }

    @PropertyName("videoUrl")
    public String getVideoUrl() { return videoUrl; }
    @PropertyName("videoUrl")
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getRequiredProducts() { return requiredProducts; }
    public void setRequiredProducts(List<String> requiredProducts) { this.requiredProducts = requiredProducts; }

    public boolean matches(String... userTraits) {
        if (categoryValue != null && userTraits != null) {
            for (String trait : userTraits) {
                if (trait != null && categoryValue.equalsIgnoreCase(trait)) return true;
            }
        }
        if (tags != null && userTraits != null) {
            for (String trait : userTraits) {
                if (trait != null && tags.contains(trait)) return true;
            }
        }
        return false;
    }
}
