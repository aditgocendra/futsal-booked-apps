package com.ark.futsalbookedapps.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.ark.futsalbookedapps.Models.ModelGallery;
import com.ark.futsalbookedapps.Models.ModelSlider;
import com.ark.futsalbookedapps.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterSlider extends RecyclerView.Adapter<AdapterSlider.SliderViewHolder>{

    private List<ModelGallery> listGallery;
    private ViewPager2 viewPager2;

    public AdapterSlider(List<ModelGallery> listGallery, ViewPager2 viewPager2) {
        this.listGallery = listGallery;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.slide_item_container, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        ModelGallery modelGallery = listGallery.get(position);
        Picasso.get().load(modelGallery.getImageGalleryUrl()).into(holder.imageSlider);
    }

    @Override
    public int getItemCount() {
        return listGallery.size();
    }

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageSlider;
        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageSlider = itemView.findViewById(R.id.image_slider);
        }


    }
}
