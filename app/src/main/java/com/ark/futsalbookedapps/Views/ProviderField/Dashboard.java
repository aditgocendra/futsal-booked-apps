package com.ark.futsalbookedapps.Views.ProviderField;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import com.ark.futsalbookedapps.Adapter.AdapterDashboard;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelField;
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

    private List<ModelField> listField;
    private AdapterDashboard adapterDashboard;

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

        setDataField();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.cardAddField.setOnClickListener(view -> Functions.updateUI(this, FieldAdd.class));

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

    private void setDataField() {
        ReferenceDatabase.referenceField.child(Data.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listField = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelField modelField = ds.getValue(ModelField.class);
                    if (modelField != null){
                        modelField.setKeyField(ds.getKey());
                        listField.add(modelField);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listField.size() != 0){
                        adapterDashboard.setItem(listField);
                    }else {
                        Toast.makeText(Dashboard.this, "Not yet field", Toast.LENGTH_SHORT).show();
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