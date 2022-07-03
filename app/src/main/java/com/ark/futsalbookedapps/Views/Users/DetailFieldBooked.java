package com.ark.futsalbookedapps.Views.Users;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.databinding.ActivityDetailFieldBookedBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailFieldBooked extends AppCompatActivity {

    private ActivityDetailFieldBookedBinding binding;

    // intent data
    private String keyBooked, keyProviderField, dateBooked, timeBooked, playTime, urlProof, status;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailFieldBookedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        // Get String Extra
        keyBooked = getIntent().getStringExtra("keyBooked");
        keyProviderField = getIntent().getStringExtra("keyProviderField");
        dateBooked = getIntent().getStringExtra("dateBooked");
        timeBooked = getIntent().getStringExtra("timeBooked");
        playTime = getIntent().getStringExtra("playTime");
        urlProof = getIntent().getStringExtra("urlProof");
        status = getIntent().getStringExtra("status");

        listenerComponent();
        setDataProvider();
        pickImageSetup();
    }

    private void listenerComponent() {
        binding.dateBooked.setText(dateBooked);
        binding.timeBooked.setText(timeBooked);
        binding.timePlay.setText(playTime + " Jam");

        binding.backBtn.setOnClickListener(view -> finish());

        if (status.equals("400")){
            binding.uploadProof.setText("Lihat Bukti Pembayaran");
        }

        binding.uploadProof.setOnClickListener(view -> {
            if (!status.equals("400")){
                imagePick.launch("image/*");
            }else {
                // set view proof
                Dialog dialog;
                dialog = new Dialog(this);
                dialog.setContentView(R.layout.layout_view_proof);
                dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.background_center_dialog));

                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                dialog.setCancelable(false); //Optional
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

                // button customize
                Button close = dialog.findViewById(R.id.btn_okay);
                close.setText("Close");

                ImageView imageView = dialog.findViewById(R.id.imageView);
                Picasso.get().load(urlProof).into(imageView);

                // text customize
                close.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            }
        });
    }

    private void setDataProvider(){
        ReferenceDatabase.referenceProviderField.child(keyProviderField).get().addOnCompleteListener(task -> {
            ModelProviderField modelProviderField = task.getResult().getValue(ModelProviderField.class);
            if (modelProviderField != null){
                binding.nameProvider.setText(Functions.capitalizeWord(modelProviderField.getName()));
                binding.bankName.setText(modelProviderField.getNameBank());
                binding.numberAccount.setText(modelProviderField.getBankAccountNumber());
            }
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
                    Toast.makeText(DetailFieldBooked.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.progressCircular.setVisibility(View.GONE);
                })
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    ReferenceDatabase.referenceBooked.child(keyBooked).child("urlProof").setValue(String.valueOf(uri));
                    Toast.makeText(DetailFieldBooked.this, "Sukses upload bukti pembayaran", Toast.LENGTH_SHORT).show();
                    updateStatus();
                    binding.progressCircular.setVisibility(View.GONE);
                    binding.uploadProof.setVisibility(View.GONE);
                }).addOnFailureListener(e -> {
                    Toast.makeText(DetailFieldBooked.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.progressCircular.setVisibility(View.GONE);
                }));
    }

    private void updateStatus(){
        ReferenceDatabase.referenceBooked.child(keyBooked).child("status").setValue(400).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                startActivity(new Intent(DetailFieldBooked.this, Home.class));
                finish();
            }
        });
    }
}