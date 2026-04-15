package com.example.beautyhub;

import com.google.firebase.firestore.PropertyName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tip {
    private String title;
    private String description;
    private String category;
    private String categoryValue;
    private String videoUrl;
    private List<String> requiredProducts = new ArrayList<>();

    public Tip() {}

    public Tip(String title, String description, String videoUrl) {
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
    }

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

    @PropertyName("requiredProducts")
    public List<String> getRequiredProducts() { return requiredProducts; }
    @PropertyName("requiredProducts")
    public void setRequiredProducts(List<String> requiredProducts) { this.requiredProducts = requiredProducts; }

    public boolean matches(Map<String, String> userTraitsMap) {
        if (category == null || categoryValue == null || userTraitsMap == null) return false;

        String userField = "";
        switch (category) {
            case "Eye Color": userField = "eyeColor"; break;
            case "Eye Shape": userField = "eyeShape"; break;
            case "Face Shape": userField = "faceShape"; break;
            case "Eyebrows": userField = "eyebrowsShape"; break;
            case "Skin Tone": userField = "skinTone"; break;
        }

        if (userTraitsMap.containsKey(userField)) {
            String userVal = userTraitsMap.get(userField);
            if (userVal != null) {
                return categoryValue.equalsIgnoreCase(userVal.trim());
            }
        }
        return false;
    }
}
