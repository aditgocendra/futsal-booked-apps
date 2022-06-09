package com.ark.futsalbookedapps.Views.ProviderField;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelGallery;
import com.ark.futsalbookedapps.databinding.ActivityUpdateGalleryBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class UpdateGallery extends AppCompatActivity {

    private ActivityUpdateGalleryBinding binding;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    private String oldUrl;

    // init intent extra
    private String keyGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        keyGallery = getIntent().getStringExtra("keyGallery");

        listenerComponent();
        pickImageSetup();

        setDataGalleryImage();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.uploadImage.setOnClickListener(view -> imagePick.launch("image/*"));

        binding.updateImageGalleryBtn.setOnClickListener(view -> {
            if (!onImageChange){
                Toast.makeText(this, "Belum ada perubahan gambar", Toast.LENGTH_SHORT).show();
            }else {
                binding.updateImageGalleryBtn.setEnabled(false);
                binding.progressCircular.setVisibility(View.VISIBLE);
                changeFieldWithImage();
            }
        });
    }

    private void pickImageSetup() {
        imagePick = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    binding.imageField.setImageURI(result);
                    onImageChange = true;
                }
        );
    }

    private void setDataGalleryImage(){
        ReferenceDatabase.referenceGallery.child(Data.uid).child(keyGallery).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelGallery modelGallery = task.getResult().getValue(ModelGallery.class);
                if (modelGallery != null){
                    Picasso.get().load(modelGallery.getImageGalleryUrl()).into(binding.imageField);
                    oldUrl = modelGallery.getImageGalleryUrl();
                }
            }else {
                Toast.makeText(UpdateGallery.this, "Error : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeFieldWithImage() {
        FirebaseStorage referenceStorage = FirebaseStorage.getInstance();
        String name_photo = referenceStorage.getReferenceFromUrl(oldUrl).getName();
        StorageReference deleteRef = referenceStorage.getReference("field/"+name_photo);

        deleteRef.delete()
                .addOnFailureListener(e -> Toast.makeText(UpdateGallery.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            String fileName = dateFormat.format(now);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference("field/"+fileName);

            Bitmap bitmap = ((BitmapDrawable) binding.imageField.getDrawable()).getBitmap();
            ByteArrayOutputStream baOs = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
            byte[] data = baOs.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data);

            uploadTask.addOnFailureListener(e -> Toast.makeText(UpdateGallery.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> changeImageGallery(String.valueOf(uri)))
                            .addOnFailureListener(e -> {
                                binding.progressCircular.setVisibility(View.GONE);
                                binding.updateImageGalleryBtn.setEnabled(true);
                                Toast.makeText(UpdateGallery.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        });
    }

    private void changeImageGallery(String uri){
        ModelGallery modelGallery = new ModelGallery(uri);

        ReferenceDatabase.referenceGallery.child(Data.uid).child(keyGallery).setValue(modelGallery).addOnSuccessListener(unused -> {
            Toast.makeText(UpdateGallery.this, "Success update image gallery", Toast.LENGTH_SHORT).show();
            binding.updateImageGalleryBtn.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            finish();
        }).addOnFailureListener(e -> {
            binding.updateImageGalleryBtn.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            Toast.makeText(UpdateGallery.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}