package com.ark.futsalbookedapps.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.Views.Users.DetailProviderField;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class AdapterHome extends RecyclerView.Adapter<AdapterHome.HomeVH> {

    private Context context;
    private List<ModelProviderField> listProviderFieldList = new ArrayList<>();

    public AdapterHome(Context context) {
        this.context = context;
    }

    public void setItem(List<ModelProviderField> listProviderFieldList){
        this.listProviderFieldList = listProviderFieldList;
    }

    @NonNull
    @Override
    public HomeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_futsal_field, parent, false);
        return new HomeVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeVH holder, int position) {
        ModelProviderField modelProviderField = listProviderFieldList.get(position);
        holder.fieldNameText.setText(modelProviderField.getName());
        Picasso.get().load(modelProviderField.getUrlPhotoField()).into(holder.imageProviderField);

        if (modelProviderField.getRating() <= 0){
            holder.ratingText.setText("Belum ada rating");
        }else {
            holder.ratingText.setText(String.valueOf(modelProviderField.getRating()));
        }

        holder.cardProviderField.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailProviderField.class);
            intent.putExtra("keyProviderField", modelProviderField.getKeyUserProviderField());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return listProviderFieldList.size();
    }

    public static class HomeVH extends RecyclerView.ViewHolder {
        ImageView imageProviderField;
        TextView fieldNameText, ratingText;
        CardView cardProviderField;
        public HomeVH(@NonNull View itemView) {
            super(itemView);
            imageProviderField = itemView.findViewById(R.id.image_provider_field);
            fieldNameText = itemView.findViewById(R.id.field_name_text);
            ratingText = itemView.findViewById(R.id.rating_text);
            cardProviderField = itemView.findViewById(R.id.card_provider_field);
        }
    }
}
