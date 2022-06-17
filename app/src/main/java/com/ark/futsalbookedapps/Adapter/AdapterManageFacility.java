package com.ark.futsalbookedapps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.Models.ModelFacility;
import com.ark.futsalbookedapps.R;
import java.util.ArrayList;
import java.util.List;

public class AdapterManageFacility extends RecyclerView.Adapter<AdapterManageFacility.FacilityVH> {

    private Context context;
    private List<ModelFacility> listFacility = new ArrayList<>();

    public AdapterManageFacility(Context context) {
        this.context = context;

    }

    public void setItem(List<ModelFacility> listFacility){
        this.listFacility = listFacility;
    }

    @NonNull
    @Override
    public FacilityVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_facility, parent, false);
        return new FacilityVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityVH holder, int position) {
        holder.textFacility.setText(listFacility.get(position).getNameFacility());
    }

    @Override
    public int getItemCount() {
        return listFacility.size();
    }

    public static class FacilityVH extends RecyclerView.ViewHolder {
        CardView cardFacility;
        TextView textFacility;
        public FacilityVH(@NonNull View itemView) {
            super(itemView);
            cardFacility = itemView.findViewById(R.id.card_facility_field);
            textFacility = itemView.findViewById(R.id.facility_name);
        }
    }
}
