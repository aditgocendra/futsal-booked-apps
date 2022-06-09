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
    private RecyclerTimePick listenerRecycler;

    public interface RecyclerTimePick{
        void recyclerTimePick(TextView textSelection, int pos);
    }

    public void RecyclerTimePick(RecyclerTimePick listenerRecycler){
        this.listenerRecycler = listenerRecycler;
    }

    public AdapterTimePick(Context context, List<String> listTime) {
        this.context = context;
        this.listTime = listTime;

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
        holder.textTimeSelection.setText(time);

        Log.d("test", String.valueOf(position));

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
