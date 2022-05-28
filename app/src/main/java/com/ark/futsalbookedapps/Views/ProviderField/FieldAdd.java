package com.ark.futsalbookedapps.Views.ProviderField;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelField;
import com.ark.futsalbookedapps.databinding.ActivityFieldAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FieldAdd extends AppCompatActivity {

    private ActivityFieldAddBinding binding;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    private String typeField, minDP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFieldAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
        pickImageSetup();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.uploadImage.setOnClickListener(view -> imagePick.launch("image/*"));

        binding.addFieldBtn.setOnClickListener(view -> {
            typeField = Objects.requireNonNull(binding.typeFieldAdd.getText()).toString();
            minDP = Objects.requireNonNull(binding.minDownPay.getText()).toString();

            if (typeField.isEmpty()){
                Toast.makeText(this, "Type field cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (minDP.isEmpty()){
                Toast.makeText(this, "Minimum down payment cannot be empty", Toast.LENGTH_SHORT).show();
            }else {
                if (!onImageChange){
                    Toast.makeText(this, "Image field required", Toast.LENGTH_SHORT).show();
                }else {
                    binding.addFieldBtn.setEnabled(false);
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    saveImageField();
                }
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

    private void saveImageField() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        String fileName = dateFormat.format(now);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("field/"+fileName);

        Bitmap bitmap = ((BitmapDrawable) binding.imageField.getDrawable()).getBitmap();
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
        byte[] data = baOs.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnFailureListener(e -> Toast.makeText(FieldAdd.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveField(String.valueOf(uri)))
                        .addOnFailureListener(e -> {
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.addFieldBtn.setEnabled(true);
                            Toast.makeText(FieldAdd.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
    }

    private void saveField(String uri){
        ModelField modelField = new ModelField(
                typeField,
                uri,
                minDP
        );

        ReferenceDatabase.referenceField.child(Data.uid).push().setValue(modelField).addOnSuccessListener(unused -> {
            Toast.makeText(FieldAdd.this, "Success add new field", Toast.LENGTH_SHORT).show();
            binding.addFieldBtn.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            finish();
        }).addOnFailureListener(e -> {
            binding.addFieldBtn.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            Toast.makeText(FieldAdd.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });


    }
}