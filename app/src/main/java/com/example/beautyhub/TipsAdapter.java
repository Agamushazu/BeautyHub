package com.example.beautyhub;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.TipViewHolder> {

    private List<Tip> tipsList;
    private List<Tip> filteredList;

    public TipsAdapter(List<Tip> tipsList) {
        this.tipsList = tipsList;
        this.filteredList = new ArrayList<>(tipsList);
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        Tip tip = filteredList.get(position);
        holder.tvTitle.setText(tip.getTitle());
        holder.tvDescription.setText(tip.getDescription());
        
        // Use a default icon or handle image from tip if available
        holder.imgTip.setImageResource(R.drawable.ic_nav_tips); // Using tips icon as placeholder

        // Handle click to open video URL
        holder.itemView.setOnClickListener(v -> {
            String videoUrl = tip.getVideoUrl();
            if (videoUrl != null && !videoUrl.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Could not open video link", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(v.getContext(), "No video available for this tip", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateList(List<Tip> newList) {
        this.tipsList = new ArrayList<>(newList);
        this.filteredList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(tipsList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Tip item : tipsList) {
                if ((item.getTitle() != null && item.getTitle().toLowerCase().contains(filterPattern)) || 
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(filterPattern))) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTip;
        TextView tvTitle, tvDescription;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTip = itemView.findViewById(R.id.img_tip);
            tvTitle = itemView.findViewById(R.id.tv_tip_title);
            tvDescription = itemView.findViewById(R.id.tv_tip_description);
        }
    }
}
