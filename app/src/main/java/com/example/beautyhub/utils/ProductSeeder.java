package com.example.beautyhub.utils;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ProductSeeder {
    private static final String TAG = "ProductSeeder";

    public static void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Product> products = new ArrayList<>();

        // Eyes
        products.add(createProduct("Sky High Mascara", "Maybelline", "Eyes", "Mascara", "Sky high lash impact from every angle!", "https://www.maybelline.com/~/media/mny/us/eye-makeup/mascara/lash-sensational-sky-high-washable-mascara/maybelline-mascara-lash-sensational-sky-high-washable-very-black-041554590518-c.jpg"));
        products.add(createProduct("Epic Ink Liner", "NYX Professional Makeup", "Eyes", "Eyeliner", "A waterproof liquid eyeliner with a slender and supple brush tip.", "https://www.nyxcosmetics.com/dw/image/v2/AANG_PRD/on/demandware.static/-/Sites-cp-nyxcosmetics-master-catalog/default/dw0695027a/ProductImages/2017/Eyes/Epic_Ink_Liner/800897085605_epicinkliner_black_main.jpg"));
        products.add(createProduct("Modern Renaissance Palette", "Anastasia Beverly Hills", "Eyes", "Eyeshadow", "An essential eyeshadow palette featuring 14 full-pigment shades.", "https://www.anastasiabeverlyhills.com/dw/image/v2/BBXQ_PRD/on/demandware.static/-/Sites-abh-master-catalog/default/dw6c6b3e9a/images/products/ABH01-18170/ABH01-18170_v1.jpg"));

        // Face & Complexion
        products.add(createProduct("Double Wear Foundation", "Estee Lauder", "Face & Complexion", "Foundation", "24-hour staying power. Flawless, natural, matte finish.", "https://www.esteelauder.com/media/export/cms/products/640x600/el_sku_1G5Y01_640x600_0.jpg"));
        products.add(createProduct("Shape Tape Concealer", "Tarte", "Face & Complexion", "Concealer", "Full coverage concealer with a natural matte finish.", "https://tartecosmetics.com/dw/image/v2/BBPW_PRD/on/demandware.static/-/Sites-master-catalog-tarte/default/dw14c6e8e7/836/836_Power_Main.jpg"));
        products.add(createProduct("Soft Pinch Liquid Blush", "Rare Beauty", "Face & Complexion", "Blush", "A weightless, long-lasting liquid blush that blends and builds beautifully.", "https://www.rarebeauty.com/cdn/shop/products/Liquid-Blush-Belief-RENDER_1.jpg"));
        products.add(createProduct("All Nighter Setting Spray", "Urban Decay", "Face & Complexion", "Setting Spray", "Award-winning makeup setting spray that keeps makeup looking fresh for up to 16 hours.", "https://www.urbandecay.com/dw/image/v2/AABV_PRD/on/demandware.static/-/Sites-urbandecay-master-catalog/default/dw9e6d8a3a/product/AllNighter/3605971440051_allnighter_settingspray_118ml.jpg"));

        // Lips
        products.add(createProduct("Matte Lipstick - Velvet Teddy", "MAC", "Lips", "Lipstick", "A creamy rich formula with high colour pay-off in a no-shine matte finish.", "https://www.maccosmetics.com/media/export/cms/products/640x600/mac_sku_M2LP11_640x600_0.jpg"));

        for (Product p : products) {
            db.collection("products")
                .add(p)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Product added successfully: " + p.getName());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding product: " + p.getName(), e);
                });
        }
    }

    private static Product createProduct(String name, String brand, String category, String subCategory, String description, String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setBrand(brand);
        p.setCategory(category);
        p.setSubCategory(subCategory);
        p.setDescription(description);
        p.setImageUrl(imageUrl);
        return p;
    }
}
