package com.ark.futsalbookedapps.Views.Users;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ark.futsalbookedapps.Adapter.AdapterFieldBooked;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.databinding.ActivityFieldBookedBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FieldBooked extends AppCompatActivity {

    private ActivityFieldBookedBinding binding;

    // init adapter
    private AdapterFieldBooked adapterFieldBooked;
    private List<ModelBooked> listBooked;

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    // Bottom Sheet Dialog
    private BottomSheetDialog bottomSheetDialog;

    // filter data
    int status = 500;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private String keyBookedFieldSelect;
    private Button btnProofSelected;

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
        pickImageSetup();
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

        bottomSheetDialog = new BottomSheetDialog(this);
        setBottomDialogFilter();
        binding.filterBtn.setOnClickListener(view -> {
            bottomSheetDialog.show();
        });

        adapterFieldBooked.RecyclerBooked((proofBtn, pos) -> {
            btnProofSelected = proofBtn;
            keyBookedFieldSelect = listBooked.get(pos).getKeyBooked();
            imagePick.launch("image/*");

        });

    }

    private void pickImageSetup() {
        imagePick = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                this::updateProof
        );
    }

    private void updateProof(Uri uriProof){
        if (uriProof == null){
            return;
        }
        binding.progressCircular.setVisibility(View.VISIBLE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        String fileName = dateFormat.format(now);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("payment_proof/"+fileName);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriProof);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
        byte[] data = baOs.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnFailureListener(e -> {
                    Toast.makeText(FieldBooked.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.progressCircular.setVisibility(View.GONE);
                })
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    ReferenceDatabase.referenceBooked.child(keyBookedFieldSelect).child("urlProof").setValue(String.valueOf(uri));
                    Toast.makeText(FieldBooked.this, "Sukses upload bukti pembayaran", Toast.LENGTH_SHORT).show();
                    binding.progressCircular.setVisibility(View.GONE);
                    btnProofSelected.setVisibility(View.GONE);
                }).addOnFailureListener(e -> {
                    Toast.makeText(FieldBooked.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.progressCircular.setVisibility(View.GONE);
                }));
    }

    private void requestDataBooked(){
        ReferenceDatabase.referenceBooked.orderByChild("keyUserBooked").equalTo(Data.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countData = snapshot.getChildrenCount();
                isLoadData = true;
                setDataFieldBooked();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FieldBooked.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
                binding.progressCircular.setVisibility(View.VISIBLE);
                listBooked = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelBooked modelBooked = ds.getValue(ModelBooked.class);
                    if (modelBooked != null){

                        // filter condition
                        if (status == 500){
                            modelBooked.setKeyBooked(ds.getKey());
                            listBooked.add(modelBooked);
                            key = ds.getKey();
                        }else {
                            if (modelBooked.getStatus() == status){
                                modelBooked.setKeyBooked(ds.getKey());
                                listBooked.add(modelBooked);
                                key = ds.getKey();
                            }
                        }
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterFieldBooked.setItem(listBooked);
                    if (listBooked.size() != 0){
                        adapterFieldBooked.notifyDataSetChanged();
                    }else {
                        Toast.makeText(FieldBooked.this, "Booked field provider is empty", Toast.LENGTH_SHORT).show();
                    }

                    isLoadData = false;
                    binding.progressCircular.setVisibility(View.GONE);
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FieldBooked.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setBottomDialogFilter(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_filter_booked, null, false);

        Button waitingPaid, cancelled, dpPay, bookedFinish;
        waitingPaid = viewBottomDialog.findViewById(R.id.waiting_paid_btn);
        cancelled = viewBottomDialog.findViewById(R.id.cancelled_btn);
        dpPay = viewBottomDialog.findViewById(R.id.dp_pay_btn);
        bookedFinish = viewBottomDialog.findViewById(R.id.booked_finish_btn);

        waitingPaid.setOnClickListener(view -> {
            waitingPaid.setEnabled(false);
            cancelled.setEnabled(true);
            dpPay.setEnabled(true);
            bookedFinish.setEnabled(true);

            filterData(0);
            bottomSheetDialog.dismiss();
        });

        cancelled.setOnClickListener(view -> {
            cancelled.setEnabled(false);
            waitingPaid.setEnabled(true);
            dpPay.setEnabled(true);
            bookedFinish.setEnabled(true);

            filterData(101);
            bottomSheetDialog.dismiss();
        });

        dpPay.setOnClickListener(view -> {
            dpPay.setEnabled(false);
            waitingPaid.setEnabled(true);
            cancelled.setEnabled(true);
            bookedFinish.setEnabled(true);

            filterData(202);
            bottomSheetDialog.dismiss();
        });

        bookedFinish.setOnClickListener(view -> {
            bookedFinish.setEnabled(false);
            waitingPaid.setEnabled(true);
            cancelled.setEnabled(true);
            dpPay.setEnabled(true);

            filterData(303);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);

    }

    private void filterData(int codeStatus){
        status = codeStatus;
        key = null;
        listBooked.clear();
        adapterFieldBooked.notifyDataSetChanged();
        isLoadData = true;
        setDataFieldBooked();

    }
}