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

import com.ark.futsalbookedapps.Adapter.AdapterFieldBooked;
import com.ark.futsalbookedapps.Adapter.AdapterHome;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.databinding.ActivityFieldBookedBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FieldBooked extends AppCompatActivity {

    private ActivityFieldBookedBinding binding;

    // init adapter
    private AdapterFieldBooked adapterFieldBooked;
    private List<ModelBooked> listBooked = new ArrayList<>();

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFieldBookedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerFieldBooked.setLayoutManager(layoutManager);
        binding.recyclerFieldBooked.setItemAnimator(new DefaultItemAnimator());

        adapterFieldBooked = new AdapterFieldBooked(this);
        binding.recyclerFieldBooked.setAdapter(adapterFieldBooked);

        listenerComponent();
        requestDataBooked();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());


        // listen user scroll recyclerview
        binding.recyclerFieldBooked.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list field booked
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerFieldBooked.getLayoutManager();
                assert linearLayoutManager != null;
                int totalCategory = linearLayoutManager.getItemCount();
                Log.d("Total Booked", String.valueOf(totalCategory));
                // check scroll on bottom
                if (!binding.recyclerFieldBooked.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    // check data item if total item < total data in database == load more data
                    if (totalCategory < countData){
                        // load more data
                        if (!isLoadData){
                            isLoadData = true;
                            setDataFieldBooked();
                        }
                    }
                }
            }
        });

    }

    private void requestDataBooked(){
        ReferenceDatabase.referenceBooked.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countData = task.getResult().getChildrenCount();
                isLoadData = true;
                setDataFieldBooked();
            }else {
                Toast.makeText(FieldBooked.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataFieldBooked() {
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            query = ReferenceDatabase.referenceBooked.orderByChild("keyUserBooked").equalTo(Data.uid).limitToFirst(10);
        }else {
            query = ReferenceDatabase.referenceBooked.orderByChild("keyUserBooked").equalTo(Data.uid).startAfter(key).limitToFirst(10);
        }

        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelBooked modelBooked = ds.getValue(ModelBooked.class);
                    if (modelBooked != null){
                        modelBooked.setKeyBooked(ds.getKey());
                        key = ds.getKey();
                        listBooked.add(modelBooked);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterFieldBooked.setItem(listBooked);
                    if (listBooked.size() != 0){
                        adapterFieldBooked.notifyDataSetChanged();
                        isLoadData = false;
                    }else {
                        Toast.makeText(FieldBooked.this, "Boooked field provider is empty", Toast.LENGTH_SHORT).show();
                    }
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FieldBooked.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}