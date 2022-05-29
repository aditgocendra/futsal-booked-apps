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
import com.ark.futsalbookedapps.databinding.ActivityUpdateFieldBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class UpdateField extends AppCompatActivity {

    private ActivityUpdateFieldBinding binding;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    private String typeField, minDP, oldUrl;

    // init intent extra
    private String keyField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateFieldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        keyField = getIntent().getStringExtra("keyField");

        listenerComponent();
        pickImageSetup();

        setDataField();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.uploadImage.setOnClickListener(view -> imagePick.launch("image/*"));

        binding.updateFieldBtn.setOnClickListener(view -> {
            typeField = Objects.requireNonNull(binding.typeFieldEdit.getText()).toString();
            minDP = Objects.requireNonNull(binding.minDownPay.getText()).toString();

            if (typeField.isEmpty()){
                Toast.makeText(this, "Type field cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (minDP.isEmpty()){
                Toast.makeText(this, "Minimum down payment cannot be empty", Toast.LENGTH_SHORT).show();
            }else {
                if (!onImageChange){
                    changeField(oldUrl);
                }else {
                    binding.updateFieldBtn.setEnabled(false);
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    changeFieldWithImage();
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

    private void setDataField(){
        ReferenceDatabase.referenceField.child(Data.uid).child(keyField).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelField modelField = task.getResult().getValue(ModelField.class);
                if (modelField != null){
                    Picasso.get().load(modelField.getUrlField()).into(binding.imageField);
                    binding.typeFieldEdit.setText(modelField.getTypeField());
                    binding.minDownPay.setText(modelField.getMinDP());
                    oldUrl = modelField.getUrlField();
                }
            }else {
                Toast.makeText(UpdateField.this, "Error : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeFieldWithImage() {
        FirebaseStorage referenceStorage = FirebaseStorage.getInstance();
        String name_photo = referenceStorage.getReferenceFromUrl(oldUrl).getName();
        StorageReference deleteRef = referenceStorage.getReference("field/"+name_photo);

        deleteRef.delete()
                .addOnFailureListener(e -> Toast.makeText(UpdateField.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show())
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

            uploadTask.addOnFailureListener(e -> Toast.makeText(UpdateField.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> changeField(String.valueOf(uri)))
                            .addOnFailureListener(e -> {
                                binding.progressCircular.setVisibility(View.GONE);
                                binding.updateFieldBtn.setEnabled(true);
                                Toast.makeText(UpdateField.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }));
        });



    }

    private void changeField(String uri){
        ModelField modelField = new ModelField(
                typeField,
                uri,
                minDP
        );

        ReferenceDatabase.referenceField.child(Data.uid).child(keyField).setValue(modelField).addOnSuccessListener(unused -> {
            Toast.makeText(UpdateField.this, "Success update field", Toast.LENGTH_SHORT).show();
            binding.updateFieldBtn.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            finish();
        }).addOnFailureListener(e -> {
            binding.updateFieldBtn.setEnabled(true);
            binding.progressCircular.setVisibility(View.GONE);
            Toast.makeText(UpdateField.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}