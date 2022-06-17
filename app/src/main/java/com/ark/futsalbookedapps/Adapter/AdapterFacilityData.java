package com.ark.futsalbookedapps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.Models.ModelFacility;
import com.ark.futsalbookedapps.R;
import java.util.List;

public class AdapterFacilityData extends RecyclerView.Adapter<AdapterFacilityData.FacilityDataVH> {

    private Context context;
    private List<ModelFacility> listFacility;

    public AdapterFacilityData(Context context, List<ModelFacility> listFacility) {
        this.context = context;
        this.listFacility = listFacility;
    }

    @NonNull
    @Override
    public FacilityDataVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_facility_text, parent, false);
        return new FacilityDataVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityDataVH holder, int position) {
        holder.textFacility.setText(listFacility.get(0).getNameFacility());
    }

    @Override
    public int getItemCount() {
        return listFacility.size();
    }

    public static class FacilityDataVH extends RecyclerView.ViewHolder {
        TextView textFacility;
        public FacilityDataVH(@NonNull View itemView) {
            super(itemView);
            textFacility = itemView.findViewById(R.id.text_facility);
        }
    }
}
