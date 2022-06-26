package com.ark.futsalbookedapps.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.R;

import java.io.Console;
import java.util.List;

public class AdapterTimePick extends RecyclerView.Adapter<AdapterTimePick.TimePickVH> {

    private Context context;
    private List<String> listTime;
    private String openTime;
    private String closeTime;
    private RecyclerTimePick listenerRecycler;

    public interface RecyclerTimePick{
        void recyclerTimePick(TextView textSelection, int pos);
    }

    public void RecyclerTimePick(RecyclerTimePick listenerRecycler){
        this.listenerRecycler = listenerRecycler;
    }

    public AdapterTimePick(Context context, List<String> listTime, String openTime, String closeTime) {
        this.context = context;
        this.listTime = listTime;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    @NonNull
    @Override
    public TimePickVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_time_pick, parent, false);
        return new TimePickVH(view, listenerRecycler);
    }

    @Override
    public void onBindViewHolder(@NonNull TimePickVH holder, int position) {
        String time = listTime.get(position);

        // convert time to integer
        int timeOpen = Integer.parseInt(openTime.substring(0, 2));
        int timeClose = Integer.parseInt(closeTime.substring(0, 2));
        int hoursTime = Integer.parseInt(time.substring(0,2));

        // Disable time before open time (AM)
        if (time.substring(6,7).equals(openTime.substring(6,7))){
            if (hoursTime < timeOpen){
                holder.textTimeSelection.setEnabled(false);
                holder.textTimeSelection.setTextColor(context.getResources().getColor(R.color.gray_dop));
            }
        }

        // Disable time after close time (PM)
        if (time.substring(6,7).equals(closeTime.substring(6,7))){
            if (hoursTime >= timeClose){
                holder.textTimeSelection.setEnabled(false);
                holder.textTimeSelection.setTextColor(context.getResources().getColor(R.color.gray_dop));
            }
        }

        holder.textTimeSelection.setText(time);

    }

    @Override
    public int getItemCount() {
        return listTime.size();
    }

    public static class TimePickVH extends RecyclerView.ViewHolder {
        TextView textTimeSelection;
        public TimePickVH(@NonNull View itemView, RecyclerTimePick listenerRecycler) {
            super(itemView);
            textTimeSelection = itemView.findViewById(R.id.text_time);

            itemView.setOnClickListener(view -> {
                if (listenerRecycler != null && getLayoutPosition() != RecyclerView.NO_POSITION){
                    listenerRecycler.recyclerTimePick(textTimeSelection, getLayoutPosition());

                }
            });
        }



    }
}
