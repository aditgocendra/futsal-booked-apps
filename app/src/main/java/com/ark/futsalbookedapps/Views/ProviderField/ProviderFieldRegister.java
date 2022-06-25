package com.ark.futsalbookedapps.Views.ProviderField;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.databinding.ActivityProviderFieldRegisterBinding;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProviderFieldRegister extends AppCompatActivity {

    private ActivityProviderFieldRegisterBinding binding;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    // init location
    private LocationRequest locationRequest;
    private double latitude, longitude;

    // init attr data
    private String fieldName, phoneNumber, bankName, accNumber, location, openTime, closeTime, description;
    private int priceField;

    private int hourOpen, minuteOpen, hourClose, minuteClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProviderFieldRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        
        listenerComponent();
        pickImageSetup();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.uploadImage.setOnClickListener(view -> imagePick.launch("image/*"));

        binding.setLocation.setOnClickListener(view -> {
            binding.setLocation.setEnabled(false);
            binding.progressCircular.setVisibility(View.VISIBLE);
            getLocationUser();
        });

        binding.saveRegisterBtn.setOnClickListener(view -> {
            fieldName = Objects.requireNonNull(binding.nameFieldAdd.getText()).toString();
            phoneNumber = Objects.requireNonNull(binding.numberPhone.getText()).toString();
            bankName = Objects.requireNonNull(binding.bankName.getText()).toString();
            accNumber = Objects.requireNonNull(binding.bankAccountNumber.getText()).toString();
            location = Objects.requireNonNull(binding.locationRegister.getText()).toString();
            priceField = Integer.parseInt(Objects.requireNonNull(binding.priceField.getText()).toString());
            openTime = binding.timeOpenResult.getText().toString();
            closeTime = binding.timeCloseResult.getText().toString();
            description = Objects.requireNonNull(binding.descriptionEdt.getText()).toString();

            if (fieldName.isEmpty()){
                Toast.makeText(this, "Field name cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (phoneNumber.isEmpty()){
                Toast.makeText(this, "Phone number field cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (accNumber.isEmpty()){
                Toast.makeText(this, "Bank account number field cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (location.isEmpty()){
                Toast.makeText(this, "Location field cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (openTime.equals("Time Open") || openTime.equals("Time Close")){
                Toast.makeText(this, "Open or close time cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (priceField == 0){
                Toast.makeText(this, "Price field cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (bankName.isEmpty()){
                Toast.makeText(this, "Bank name field cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (description.isEmpty()){
                Toast.makeText(this, "Description field cannot be empty", Toast.LENGTH_SHORT).show();
            }
            else {
                if (!onImageChange){
                    Toast.makeText(this, "Please upload the image field", Toast.LENGTH_SHORT).show();
                }else {
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    binding.saveRegisterBtn.setEnabled(false);
                    saveImageField();
                }
            }
        });

        // pick time open
        binding.cardPickTimeOpen.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog;
            Calendar calendar = Calendar.getInstance();
            timePickerDialog = new TimePickerDialog(ProviderFieldRegister.this, (timePicker, hourDay, minuteOfDay) -> {

                hourOpen = hourDay;
                minuteOpen = minuteOfDay;

                calendar.set(0,0,0, hourOpen, minuteOpen);
                binding.timeOpenResult.setText(DateFormat.format("hh:mm aa", calendar));
            }, 24, 0, true);

            timePickerDialog.show();
        });

        // pick time close
        binding.cardPickTimeClose.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog;
            Calendar calendar = Calendar.getInstance();
            timePickerDialog = new TimePickerDialog(ProviderFieldRegister.this, (timePicker, hourDay, minuteOfDay) -> {

                hourClose = hourDay;
                minuteClose = minuteOfDay;

                calendar.set(0,0,0, hourClose, minuteClose);
                binding.timeCloseResult.setText(DateFormat.format("hh:mm aa", calendar));
            }, 24, 0, true);

            timePickerDialog.show();
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

    private void getLocationUser(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(ProviderFieldRegister.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Functions.isGPSEnabled(ProviderFieldRegister.this)) {
                    LocationServices.getFusedLocationProviderClient(ProviderFieldRegister.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(ProviderFieldRegister.this).removeLocationUpdates(this);
                            if (locationResult.getLocations().size() > 0){
                                int index = locationResult.getLocations().size() - 1;

                                latitude = locationResult.getLocations().get(index).getLatitude();
                                longitude = locationResult.getLocations().get(index).getLongitude();

                                Geocoder geocoder = new Geocoder(ProviderFieldRegister.this, Locale.getDefault());
                                try {

                                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude,1);
                                    binding.locationRegister.setText(addressList.get(0).getAddressLine(0));

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Toast.makeText(ProviderFieldRegister.this, "Find location failed", Toast.LENGTH_SHORT).show();
                            }
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.setLocation.setEnabled(true);
                        }
                    }, Looper.getMainLooper());

                } else {
                    Functions.turnOnGPS(locationRequest, ProviderFieldRegister.this);
                    binding.progressCircular.setVisibility(View.GONE);
                    binding.setLocation.setEnabled(true);
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1005);
                binding.progressCircular.setVisibility(View.GONE);
                binding.setLocation.setEnabled(true);
            }
        }
    }

    private void saveImageField() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        String fileName = dateFormat.format(now);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("field_provider/"+fileName);

        Bitmap bitmap = ((BitmapDrawable) binding.imageField.getDrawable()).getBitmap();
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
        byte[] data = baOs.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnFailureListener(e -> Toast.makeText(ProviderFieldRegister.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveProviderField(String.valueOf(uri)))
                        .addOnFailureListener(e -> {
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.saveRegisterBtn.setEnabled(true);
                            Toast.makeText(ProviderFieldRegister.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
    }

    private void saveProviderField(String uri) {
        ModelProviderField modelProviderField = new ModelProviderField(
          fieldName.toLowerCase(),
          uri,
          phoneNumber,
          bankName,
          accNumber,
          location,
          latitude,
          longitude,
                0.0,
                openTime,
                closeTime,
                description,
                priceField
        );

        ReferenceDatabase.referenceProviderField.child(Data.uid).setValue(modelProviderField).addOnSuccessListener(unused -> {
            binding.progressCircular.setVisibility(View.GONE);
            binding.saveRegisterBtn.setEnabled(true);
            Toast.makeText(ProviderFieldRegister.this, "Registration Success", Toast.LENGTH_SHORT).show();
            Functions.updateUI(ProviderFieldRegister.this, Dashboard.class);
        }).addOnFailureListener(e -> {
            binding.progressCircular.setVisibility(View.GONE);
            binding.saveRegisterBtn.setEnabled(true);
            Toast.makeText(ProviderFieldRegister.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}