package com.ark.futsalbookedapps.Views.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.ark.futsalbookedapps.Adapter.AdapterNotification;
import com.ark.futsalbookedapps.Adapter.AdapterProviderField;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelNotification;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.databinding.ActivityNotificationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Notification extends AppCompatActivity {

    private ActivityNotificationBinding binding;

    private AdapterNotification adapterNotification;
    private List<ModelNotification> listNotification;

    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerNotification.setLayoutManager(layoutManager);
        binding.recyclerNotification.setItemAnimator(new DefaultItemAnimator());

        adapterNotification = new AdapterNotification(this);
        binding.recyclerNotification.setAdapter(adapterNotification);

        listenerComponent();
        requestDataNotification();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.recyclerNotification.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list field provider
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerNotification.getLayoutManager();
                assert linearLayoutManager != null;
                int totalCategory = linearLayoutManager.getItemCount();
                Log.d("Total Provider", String.valueOf(totalCategory));
                // check scroll on bottom
                if (!binding.recyclerNotification.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    // check data item if total item < total data in database == load more data
                    if (totalCategory < countData){
                        // load more data
                        if (!isLoadData){
                            isLoadData = true;
                            setDataNotification();
                        }
                    }
                }
            }
        });
    }

    private void requestDataNotification(){
        ReferenceDatabase.referenceNotification.child(Data.uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countData = task.getResult().getChildrenCount();
                isLoadData = true;
                setDataNotification();
            }else {
                Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataNotification(){
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            query = ReferenceDatabase.referenceNotification.child(Data.uid).orderByKey().limitToFirst(10);
        }else {
            query = ReferenceDatabase.referenceNotification.child(Data.uid).orderByKey().startAfter(key).limitToFirst(10);
        }

        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listNotification = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelNotification modelNotification = ds.getValue(ModelNotification.class);
                    if (modelNotification != null){
                        modelNotification.setKeyNotification(ds.getKey());
                        key = ds.getKey();
                        listNotification.add(modelNotification);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterNotification.setItem(listNotification);
                    if (listNotification.size() != 0){
                        adapterNotification.notifyDataSetChanged();
                        isLoadData = false;
                    }
//                    binding.swipeRefresh.setRefreshing(false);
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Notification.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}