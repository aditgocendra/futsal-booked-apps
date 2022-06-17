package com.ark.futsalbookedapps.Views.ProviderField;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ark.futsalbookedapps.Adapter.AdapterManageFacility;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelFacility;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.databinding.ActivityFacilityBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Facility extends AppCompatActivity {

    private ActivityFacilityBinding binding;

    // adapter
    private AdapterManageFacility adapterManageFacility;
    private List<ModelFacility> listFacility;

    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFacilityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        // layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        // set layout recycler variant product
        binding.recyclerFacility.setLayoutManager(layoutManager);
        binding.recyclerFacility.setItemAnimator(new DefaultItemAnimator());

        // set adapter recyclerview
        adapterManageFacility = new AdapterManageFacility(this);
        binding.recyclerFacility.setAdapter(adapterManageFacility);

        listenerComponent();
        setDataFacility();

    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.floatAdd.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(this);
            setBottomSheetDialog();
            bottomSheetDialog.show();
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerFacility);

    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            //Create the Dialog here
            Dialog dialog = new Dialog(Facility.this);
            dialog.setContentView(R.layout.layout_confirmation_option);
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_center_dialog));

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog.setCancelable(false); //Optional
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

            // button customize
            Button okay, cancel;
            okay = dialog.findViewById(R.id.btn_okay);
            cancel = dialog.findViewById(R.id.btn_cancel);
            okay.setText("Okay");
            cancel.setText("Cancel");

            // text customize
            TextView messageText, headerText;
            headerText = dialog.findViewById(R.id.textView);
            messageText = dialog.findViewById(R.id.textView2);
            headerText.setText("Delete Data");
            messageText.setText("Are you sure delete this data ?");

            int totalVariant = listFacility.size();

            if (totalVariant < 2){
                messageText.setText("Are you sure to delete all data?");
            }

            dialog.show();

            okay.setOnClickListener(v -> {
                // new data category
                deleteFacility(pos);
                dialog.dismiss();
            });

            cancel.setOnClickListener(v -> {
                adapterManageFacility.notifyItemChanged(pos);
                dialog.dismiss();
            });
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // set swipe flags horizontal
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(0, swipeFlags);
        }
    };

    private void deleteFacility(int pos) {
        ModelFacility modelFacility = listFacility.get(pos);
        ReferenceDatabase.referenceFacility.child(Data.uid).child(modelFacility.getKeyFacility()).removeValue()
                .addOnFailureListener(e -> Toast.makeText(Facility.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> {
                    listFacility.remove(pos);
                    adapterManageFacility.notifyItemRemoved(pos);
                });
    }


    private void setBottomSheetDialog(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_dialog_add_faciity, null, false);
        TextInputEditText nameFacility = viewBottomDialog.findViewById(R.id.name_facility);
        Button btnSave = viewBottomDialog.findViewById(R.id.btn_save_facility);

        btnSave.setOnClickListener(view -> {
            String name = Objects.requireNonNull(nameFacility.getText()).toString();
            if (name.isEmpty()){
                Toast.makeText(this, "Nama fasilitas tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }else {
                saveFacility(name);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void saveFacility(String name) {
        ModelFacility modelFacility = new ModelFacility(name);
        String key = ReferenceDatabase.referenceFacility.child(Data.uid).push().getKey();
        assert key != null;
        ReferenceDatabase.referenceFacility.child(Data.uid).child(key).setValue(modelFacility)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(Facility.this, "Success", Toast.LENGTH_SHORT).show();

                    if (listFacility.size() == 0){
                        adapterManageFacility.setItem(listFacility);
                    }

                    modelFacility.setKeyFacility(key);
                    listFacility.add(modelFacility);
                    adapterManageFacility.notifyItemInserted(listFacility.size());
                })
                .addOnFailureListener(e -> Toast.makeText(Facility.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setDataFacility() {
        ReferenceDatabase.referenceFacility.child(Data.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listFacility = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelFacility modelFacility = ds.getValue(ModelFacility.class);
                    if (modelFacility != null){
                        modelFacility.setKeyFacility(ds.getKey());
                        listFacility.add(modelFacility);
                    }
                }
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listFacility.size() != 0){
                        adapterManageFacility.setItem(listFacility);
                        adapterManageFacility.notifyItemRangeChanged(0, listFacility.size());
                    }else {
                        Toast.makeText(Facility.this, "Belum ada satupun failitas yang ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                },100);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Facility.this, "Error : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}