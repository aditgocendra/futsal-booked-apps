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
import com.ark.futsalbookedapps.databinding.ActivityGalleryImageAddBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GalleryImageAdd extends AppCompatActivity {

    private ActivityGalleryImageAddBinding binding;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGalleryImageAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
        pickImageSetup();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.uploadImage.setOnClickListener(view -> imagePick.launch("image/*"));

        binding.addGalleryBtn.setOnClickListener(view -> {
            if (!onImageChange){
                Toast.makeText(this, "Image field required", Toast.LENGTH_SHORT).show();
            }else {
                binding.addGalleryBtn.setEnabled(false);
                binding.progressCircular.setVisibility(View.VISIBLE);
                saveImageGallery();
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

    private void saveImageGallery() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        String fileName = dateFormat.format(now);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("field/"+fileName);

        Bitmap bitmap = ((BitmapDrawable) binding.imageField.getDrawable()).getBitmap();
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
        byte[] data = baOs.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnFailureListener(e -> Toast.makeText(GalleryImageAdd.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveImageGallery(String.valueOf(uri)))
                        .addOnFailureListener(e -> {
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.addGalleryBtn.setEnabled(true);
                            Toast.makeText(GalleryImageAdd.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
    }

    private void saveImageGallery(String uri){
        ModelGallery modelGallery = new ModelGallery(uri);

        ReferenceDatabase.referenceGallery.child(Data.uid).push().setValue(modelGallery).addOnSuccessListener(unused -> {
            Toast.makeText(GalleryImageAdd.this, "Success add new image gallery", Toast.LENGTH_SHORT).show();
            binding.addGalleryBtn.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            finish();
        }).addOnFailureListener(e -> {
            binding.addGalleryBtn.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            Toast.makeText(GalleryImageAdd.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}