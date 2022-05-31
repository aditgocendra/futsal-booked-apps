package com.ark.futsalbookedapps.Views.ProviderField;

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
import com.ark.futsalbookedapps.Adapter.AdapterManageBookedField;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.Views.Users.FieldBooked;
import com.ark.futsalbookedapps.databinding.ActivityManageBookedFieldBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageBookedField extends AppCompatActivity {

    private ActivityManageBookedFieldBinding binding;

    // init adapter
    private AdapterManageBookedField adapterManageBookedField;
    private List<ModelBooked> listBooked = new ArrayList<>();

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageBookedFieldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerManageFieldBooked.setLayoutManager(layoutManager);
        binding.recyclerManageFieldBooked.setItemAnimator(new DefaultItemAnimator());

        adapterManageBookedField = new AdapterManageBookedField(this);
        binding.recyclerManageFieldBooked.setAdapter(adapterManageBookedField);

        listenerComponent();
        requestDataBooked();

    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        // listen user scroll recyclerview
        binding.recyclerManageFieldBooked.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list manage field booked
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerManageFieldBooked.getLayoutManager();
                assert linearLayoutManager != null;
                int totalCategory = linearLayoutManager.getItemCount();
                Log.d("Total Manage Booked", String.valueOf(totalCategory));
                // check scroll on bottom
                if (!binding.recyclerManageFieldBooked.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    // check data item if total item < total data in database == load more data
                    if (totalCategory < countData){
                        // load more data
                        if (!isLoadData){
                            isLoadData = true;
                            setDataBooked();
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
                setDataBooked();
            }else {
                Toast.makeText(ManageBookedField.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataBooked() {
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            query = ReferenceDatabase.referenceBooked.orderByChild("keyProviderField").equalTo(Data.uid).limitToFirst(10);
        }else {
            query = ReferenceDatabase.referenceBooked.orderByChild("keyProviderField").equalTo(Data.uid).startAfter(key).limitToFirst(10);
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
                    adapterManageBookedField.setItem(listBooked);
                    if (listBooked.size() != 0){
                        adapterManageBookedField.notifyDataSetChanged();
                        isLoadData = false;
                    }else {
                        Toast.makeText(ManageBookedField.this, "Not Yet Booked", Toast.LENGTH_SHORT).show();
                    }
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageBookedField.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}