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

        // --- FACE & COMPLEXION ---
        products.add(createProduct("Dior Backstage Glow Maximizer Palette", "Dior", "Face & Complexion", "Highlighter", "Dior's iconic highlighter and multi-purpose makeup palette – for endless radiance.Dior Backstage Glow Maximizer Palette illuminates the face, eyes and décolleté with a wider range of finishes: pearlescent, metallic, glittery and duotone.", "https://www.dior.com/dw/image/v2/BGXS_PRD/on/demandware.static/-/Sites-master_dior/default/dw9ab11228/Y0000104/Y0000104_E000000579_E01_ZHC.jpg?sw=1800"));
        products.add(createProduct("Shape Tape Concealer", "Tarte", "Face & Complexion", "Concealer", "Full coverage concealer with a natural matte finish.", "https://tartecosmetics.com/dw/image/v2/BBPW_PRD/on/demandware.static/-/Sites-master-catalog-tarte/default/dw14c6e8e7/836/836_Power_Main.jpg"));
        products.add(createProduct("Photo Finish Primer", "Smashbox", "Face & Complexion", "Primer", "A transparent face primer gel that smoothes skin and blurs flaws.", "https://www.smashbox.com/media/export/cms/products/640x600/sbx_sku_60165_640x600_0.jpg"));

        // --- EYES ---
        products.add(createProduct("Epic Ink Liner", "NYX Professional Makeup", "Eyes", "Eyeliner", "A waterproof liquid eyeliner with a slender and supple brush tip.", "https://www.nyxcosmetics.com/dw/image/v2/AANG_PRD/on/demandware.static/-/Sites-cp-nyxcosmetics-master-catalog/default/dw0695027a/ProductImages/2017/Eyes/Epic_Ink_Liner/800897085605_epicinkliner_black_main.jpg"));
        products.add(createProduct("Clear Brow Gel", "Anastasia Beverly Hills", "Eyes", "Eyebrow Gel", "A lightweight clear gel that sets brow color and holds brow hairs in place.", "https://www.anastasiabeverlyhills.com/dw/image/v2/BBXQ_PRD/on/demandware.static/-/Sites-abh-master-catalog/default/dw837486e0/images/products/ABH01-18111/ABH01-18111_v1.jpg"));
        products.add(createProduct("Brow Wiz", "Anastasia Beverly Hills", "Eyes", "Eyebrow Pencil", "An ultra-slim, retractable eyebrow pencil for detailing and creating hair-like strokes.", "https://www.anastasiabeverlyhills.com/dw/image/v2/BBXQ_PRD/on/demandware.static/-/Sites-abh-master-catalog/default/dw3253b925/images/products/ABH01-18101/ABH01-18101_v1.jpg"));
        products.add(createProduct("24/7 Glide-On Eye Pencil - Black", "Urban Decay", "Eyes", "Eye Pencil", "Award-winning waterproof eyeliner pencil in Perversion (Black).", "https://www.urbandecay.com/dw/image/v2/AABV_PRD/on/demandware.static/-/Sites-urbandecay-master-catalog/default/dwf2b9048a/product/247GlideOnEyePencil/3605970258251_247glideoneyepencil_perversion.jpg"));
        products.add(createProduct("24/7 Glide-On Eye Pencil - Brown", "Urban Decay", "Eyes", "Eye Pencil", "Award-winning waterproof eyeliner pencil in Whiskey (Brown).", "https://www.urbandecay.com/dw/image/v2/AABV_PRD/on/demandware.static/-/Sites-urbandecay-master-catalog/default/dwf2b9048a/product/247GlideOnEyePencil/3605970258329_247glideoneyepencil_whiskey.jpg"));

        // --- LIPS ---
        products.add(createProduct("Gloss Bomb Universal Lip Luminizer", "Fenty Beauty", "Lips", "Lip Gloss", "The ultimate gotta-have-it lip gloss with explosive shine.", "https://www.fentybeauty.com/dw/image/v2/BBSG_PRD/on/demandware.static/-/Sites-itemmaster_fenty/default/dwf5567c9c/all-images/22742/22742_primary.jpg"));
        products.add(createProduct("Matte Lipstick", "MAC", "Lips", "Lipstick", "A creamy rich formula with high colour pay-off in a no-shine matte finish.", "https://www.maccosmetics.com/media/export/cms/products/640x600/mac_sku_M2LP11_640x600_0.jpg"));
        products.add(createProduct("Lip Cheat Liner", "Charlotte Tilbury", "Lips", "Lip Liner", "A rich, velvety lip liner to reshape and resize the look of your lips.", "https://www.charlottetilbury.com/media/catalog/product/l/i/lip-cheat-pillow-talk-packshot.jpg"));

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
