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
import com.ark.futsalbookedapps.Adapter.AdapterReviewProvider;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelReviewProvider;
import com.ark.futsalbookedapps.databinding.ActivityReviewFieldBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReviewField extends AppCompatActivity {

    private ActivityReviewFieldBinding binding;

    // init adapter user review
    private AdapterReviewProvider adapterReviewProvider;
    private List<ModelReviewProvider> listReviewProvider;

    // param pagination data review provider
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewFieldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        // recycler review user
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerManageFieldBooked.setLayoutManager(layoutManager);
        binding.recyclerManageFieldBooked.setItemAnimator(new DefaultItemAnimator());

        adapterReviewProvider = new AdapterReviewProvider(this);
        binding.recyclerManageFieldBooked.setAdapter(adapterReviewProvider);

        listenerComponent();
        requestReviewProvider();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.recyclerManageFieldBooked.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list review provider
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerManageFieldBooked.getLayoutManager();
                assert linearLayoutManager != null;
                int totalCategory = linearLayoutManager.getItemCount();
                Log.d("Total Review Provider", String.valueOf(totalCategory));
                // check scroll on bottom
                if (!binding.recyclerManageFieldBooked.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    // check data item if total item < total data in database == load more data
                    if (totalCategory < countData){
                        // load more data
                        if (!isLoadData){
                            isLoadData = true;
                            setDataReview();
                        }
                    }
                }
            }
        });

    }

    private void requestReviewProvider(){
        ReferenceDatabase.referenceReview.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countData = task.getResult().getChildrenCount();
                isLoadData = true;
                setDataReview();
            }else {
                Toast.makeText(ReviewField.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataReview(){
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            query = ReferenceDatabase.referenceReview.child(Data.uid).orderByKey().limitToFirst(10);
        }else {
            query = ReferenceDatabase.referenceReview.child(Data.uid).orderByKey().startAfter(key).limitToFirst(10);
        }

        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listReviewProvider = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelReviewProvider modelReviewProvider = ds.getValue(ModelReviewProvider.class);
                    if (modelReviewProvider != null){
                        modelReviewProvider.setKeyReview(ds.getKey());
                        key = ds.getKey();
                        listReviewProvider.add(modelReviewProvider);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterReviewProvider.setItem(listReviewProvider);
                    if (listReviewProvider.size() != 0){
                        adapterReviewProvider.notifyDataSetChanged();
                        isLoadData = false;
                    }else {
                        Toast.makeText(ReviewField.this, "Not yet review", Toast.LENGTH_SHORT).show();
                    }
                }, 200);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReviewField.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}