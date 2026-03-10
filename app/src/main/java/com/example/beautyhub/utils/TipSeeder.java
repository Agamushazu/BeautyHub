package com.example.beautyhub.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TipSeeder {
    private static final String TAG = "TipSeeder";

    public static void seedTips(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // נבדוק קודם אם הטיפים האלה כבר קיימים כדי לא ליצור כפילויות
        db.collection("tips").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                QuerySnapshot snapshot = task.getResult();
                List<String> existingTitles = new ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot) {
                    if (doc.contains("title")) {
                        existingTitles.add(doc.getString("title"));
                    }
                }

                List<Map<String, Object>> tipsToUpload = new ArrayList<>();
                
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Makeup for Brown Eyes", "Best shades and techniques to make brown eyes pop.", "Eye Color", "Brown", "https://vt.tiktok.com/ZSuhBDK1D/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Makeup for Blue Eyes", "Tutorial on colors that complement blue eyes perfectly.", "Eye Color", "Blue", "https://vt.tiktok.com/ZSuhB93ax/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Eyeliner for Hooded Eyes", "Mastering the winged liner for hooded eyelids.", "Eye Shape", "Hooded", "https://vt.tiktok.com/ZSuhBt6GD/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Eyeliner for Almond Eyes", "How to accentuate almond-shaped eyes with liner.", "Eye Shape", "Almond", "https://vt.tiktok.com/ZSuhByWnV/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Monolid Eyeliner Guide", "Precision eyeliner techniques for monolid eyes.", "Eye Shape", "Monolid", "https://vt.tiktok.com/ZSuhBmjwc/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Eyeliner for Round Eyes", "Defining round eyes with the right liner placement.", "Eye Shape", "Round", "https://vt.tiktok.com/ZSuhByppj/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Complexion for Round Face", "Base and highlight tips specifically for round faces.", "Face Shape", "Round", "https://vt.tiktok.com/ZSuhBby32/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Contour for Round Face", "Sculpting techniques to define a round face shape.", "Face Shape", "Round", "https://vt.tiktok.com/ZSuhSNm3b/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Contour for Heart Face", "Where to place contour on a heart-shaped face.", "Face Shape", "Heart", "https://vt.tiktok.com/ZSuhS2x8U/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Contour for Square Face", "Soften the jawline with these contour tips for square faces.", "Face Shape", "Square", "https://vt.tiktok.com/ZSuhS2NGX/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Contour for Oval Face", "Enhancing the natural balance of an oval face.", "Face Shape", "Oval", "https://vt.tiktok.com/ZSuhBEMh4/"));
                addIfMissing(tipsToUpload, existingTitles, createTipMap("Eyebrow Style Tutorial", "Learn how to draw arched or straight eyebrows.", "Eyebrows", "Arched/Straight", "https://vt.tiktok.com/ZSuhBcFjj/"));

                if (tipsToUpload.isEmpty()) {
                    Toast.makeText(context, "All TikTok tips are already in the database!", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Map<String, Object> tip : tipsToUpload) {
                    db.collection("tips").add(tip);
                }
                Toast.makeText(context, "Added " + tipsToUpload.size() + " new tips!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void addIfMissing(List<Map<String, Object>> list, List<String> existing, Map<String, Object> newTip) {
        if (!existing.contains((String)newTip.get("title"))) {
            list.add(newTip);
        }
    }

    private static Map<String, Object> createTipMap(String title, String desc, String category, String value, String url) {
        Map<String, Object> tip = new HashMap<>();
        tip.put("title", title);
        tip.put("description", desc);
        tip.put("Category", category);
        tip.put("CategoryValue", value);
        tip.put("videoUrl", url);
        return tip;
    }
}
