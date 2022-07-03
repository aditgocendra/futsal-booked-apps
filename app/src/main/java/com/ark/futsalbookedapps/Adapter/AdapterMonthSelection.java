package com.ark.futsalbookedapps.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.R;
import java.util.List;

public class AdapterMonthSelection extends RecyclerView.Adapter<AdapterMonthSelection.MonthSelectionVH> {

    private Context context;
    private List<String> months;
    private RecyclerMonthPick listenerRecycler;

    public interface RecyclerMonthPick{
        void recyclerMonthPick(TextView textSelection, int pos);
    }

    public void RecyclerMonthPick(RecyclerMonthPick listenerRecycler){
        this.listenerRecycler = listenerRecycler;
    }

    public AdapterMonthSelection(Context context, List<String> months) {
        this.context = context;
        this.months = months;
    }

    @NonNull
    @Override
    public MonthSelectionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_month_pick, parent, false);
        return new MonthSelectionVH(view, listenerRecycler);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthSelectionVH holder, int position) {
        String month = months.get(position);
        holder.textMonthSelection.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        holder.textMonthSelection.setText(month);
    }

    @Override
    public int getItemCount() {
        return months.size();
    }

    public static class MonthSelectionVH extends RecyclerView.ViewHolder {
        TextView textMonthSelection;
        public MonthSelectionVH(@NonNull View itemView , RecyclerMonthPick listenerRecycler) {
            super(itemView);
            textMonthSelection = itemView.findViewById(R.id.month_time);

            itemView.setOnClickListener(view -> {
                if (listenerRecycler != null && getLayoutPosition() != RecyclerView.NO_POSITION){
                    listenerRecycler.recyclerMonthPick(textMonthSelection, getLayoutPosition());

                }
            });
        }
    }
}
