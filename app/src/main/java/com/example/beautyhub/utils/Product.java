package com.example.beautyhub.utils;

public class Product {
    private String id;
    private String name;
    private String brand;
    private String imageUrl;

    public Product() {} // Required for Firebase

    public Product(String id, String name, String brand, String imageUrl) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getImageUrl() { return imageUrl; }
}