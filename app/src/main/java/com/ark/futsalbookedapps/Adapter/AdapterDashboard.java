package com.ark.futsalbookedapps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ark.futsalbookedapps.Models.ModelField;
import com.ark.futsalbookedapps.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterDashboard extends RecyclerView.Adapter<AdapterDashboard.DashboardVH> {

    private Context context;
    private List<ModelField> listField;

    public AdapterDashboard(Context context, List<ModelField> listField) {
        this.context = context;
        this.listField = listField;
    }

    @NonNull
    @Override
    public DashboardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_grid_field, parent, false);
        return new DashboardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardVH holder, int position) {
        ModelField modelField = listField.get(position);
        Picasso.get().load(modelField.getUrlField()).into(holder.imageField);
        holder.typeField.setText(modelField.getTypeField());
    }

    @Override
    public int getItemCount() {
        return listField.size();
    }

    public static class DashboardVH extends RecyclerView.ViewHolder {
        ImageView imageField;
        TextView typeField;
        public DashboardVH(@NonNull View itemView) {
            super(itemView);
            imageField = itemView.findViewById(R.id.image_field);
            typeField = itemView.findViewById(R.id.type_field);
        }
    }
}
