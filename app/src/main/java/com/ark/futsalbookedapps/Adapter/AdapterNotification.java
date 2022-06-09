package com.ark.futsalbookedapps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ark.futsalbookedapps.Models.ModelNotification;
import com.ark.futsalbookedapps.R;

import java.util.ArrayList;
import java.util.List;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.NotificationVH> {

    private Context context;
    private List<ModelNotification> listNotification = new ArrayList<>();

    public AdapterNotification(Context context) {
        this.context = context;
    }

    public void setItem(List<ModelNotification> listNotification){
        this.listNotification = listNotification;
    }

    @NonNull
    @Override
    public NotificationVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_notification, parent, false);
        return new NotificationVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationVH holder, int position) {
        ModelNotification modelNotification = listNotification.get(position);
        holder.header.setText(modelNotification.getHeader());
        holder.msg.setText(modelNotification.getMsg());
    }

    @Override
    public int getItemCount() {
        return listNotification.size();
    }

    public static class NotificationVH extends RecyclerView.ViewHolder {
        TextView header, msg;
        public NotificationVH(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header_notification);
            msg = itemView.findViewById(R.id.message_notification);
        }
    }
}
