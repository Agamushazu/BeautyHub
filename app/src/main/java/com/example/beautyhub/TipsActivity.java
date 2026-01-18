package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class TipsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TipsAdapter adapter;
    private List<Tip> tipsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        setupRecyclerView();
        setupSearchView();
        setupBottomNavigation();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_tips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tipsList = new ArrayList<>();
        tipsList.add(new Tip("Smoky Eye Tutorial", "Perfect for evening events. Learn how to blend dark shades for a dramatic look.", android.R.drawable.ic_menu_gallery));
        tipsList.add(new Tip("Natural Daily Look", "Simple and fresh makeup for your everyday routine.", android.R.drawable.ic_menu_gallery));
        tipsList.add(new Tip("Red Lip Guide", "How to apply red lipstick perfectly without smudging.", android.R.drawable.ic_menu_gallery));
        tipsList.add(new Tip("Winged Eyeliner", "Master the cat-eye technique with this step-by-step guide.", android.R.drawable.ic_menu_gallery));
        tipsList.add(new Tip("Contouring Basics", "Define your features like a pro using highlight and contour.", android.R.drawable.ic_menu_gallery));

        adapter = new TipsAdapter(tipsList);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.search_view_tips);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_tips);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_feed) {
                startActivity(new Intent(this, FeedActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return id == R.id.nav_tips;
        });
    }
}