package com.ark.futsalbookedapps.Views.ProviderField;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.ark.futsalbookedapps.Adapter.AdapterDashboard;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelGallery;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.Views.Users.Home;
import com.ark.futsalbookedapps.databinding.ActivityDashboardBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Dashboard extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private AdapterDashboard adapterDashboard;
    private List<ModelGallery> listGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
        setDataProviderField();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.recyclerFieldGrid.setLayoutManager(gridLayoutManager);
        binding.recyclerFieldGrid.setHasFixedSize(true);

        adapterDashboard = new AdapterDashboard(this);
        binding.recyclerFieldGrid.setAdapter(adapterDashboard);

        setDataGallery();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.cardAddGallery.setOnClickListener(view -> Functions.updateUI(this, GalleryImageAdd.class));
        binding.cardReviewUser.setOnClickListener(view -> Functions.updateUI(this, ReviewField.class));
        binding.cardProvider.setOnClickListener(view -> Functions.updateUI(this, UpdateDataProvider.class));
        binding.cardBooked.setOnClickListener(view -> Functions.updateUI(this, ManageBookedField.class));
    }

    private void setDataProviderField() {
        ReferenceDatabase.referenceProviderField.child(Data.uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProviderField modelProviderField = task.getResult().getValue(ModelProviderField.class);
                if (modelProviderField != null){
                    binding.fieldNameText.setText(modelProviderField.getName());
                    binding.ratingText.setText("Rating : "+modelProviderField.getRating());

                    Picasso.get().load(modelProviderField.getUrlPhotoField()).into(binding.imageField);
                }else {
                    Toast.makeText(Dashboard.this, "Provider field is null", Toast.LENGTH_SHORT).show();
                    Functions.updateUI(Dashboard.this, Home.class);
                    finish();
                }
            }else {
                Toast.makeText(Dashboard.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataGallery() {
        ReferenceDatabase.referenceGallery.child(Data.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listGallery = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelGallery modelGallery = ds.getValue(ModelGallery.class);
                    if (modelGallery != null){
                        modelGallery.setKeyGallery(ds.getKey());
                        listGallery.add(modelGallery);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listGallery.size() != 0){
                        adapterDashboard.setItem(listGallery);
                    }else {
                        Toast.makeText(Dashboard.this, "Not yet image gallery", Toast.LENGTH_SHORT).show();
                    }
                    adapterDashboard.notifyDataSetChanged();
                }, 200);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dashboard.this, "Error : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}