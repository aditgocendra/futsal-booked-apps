package com.ark.futsalbookedapps.Views.ProviderField;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.Views.Users.Home;
import com.ark.futsalbookedapps.databinding.ActivityUpdateDataProviderBinding;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UpdateDataProvider extends AppCompatActivity {

    private ActivityUpdateDataProviderBinding binding;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    // init location
    private LocationRequest locationRequest;
    private double latitude, longitude;

    // init attr data
    private String fieldName, phoneNumber, accNumber, location, oldUrl, openTime, closeTime;
    private int priceField;

    private int hourOpen, minuteOpen, hourClose, minuteClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateDataProviderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        listenerComponent();
        pickImageSetup();

        setDataProviderField();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.uploadImage.setOnClickListener(view -> imagePick.launch("image/*"));

        binding.setLocationEdit.setOnClickListener(view -> {
            binding.setLocationEdit.setEnabled(false);
            binding.progressCircular.setVisibility(View.VISIBLE);
            getLocationUser();
        });

        binding.editBtn.setOnClickListener(view -> {
            fieldName = Objects.requireNonNull(binding.nameFieldEdit.getText()).toString();
            phoneNumber = Objects.requireNonNull(binding.numberPhoneEdit.getText()).toString();
            accNumber = Objects.requireNonNull(binding.bankAccountNumberEdit.getText()).toString();
            location = Objects.requireNonNull(binding.locationEdit.getText()).toString();
            priceField = Integer.parseInt(Objects.requireNonNull(binding.priceField.getText()).toString());
            openTime = binding.timeOpenResult.getText().toString();
            closeTime = binding.timeCloseResult.getText().toString();

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
            }else {
                if (!onImageChange){
                    changeProviderField(oldUrl);
                }else {
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    binding.editBtn.setEnabled(false);
                    changeFieldWithImage();
                }
            }
        });

        // pick time open
        binding.cardPickTimeOpen.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog;
            Calendar calendar = Calendar.getInstance();
            timePickerDialog = new TimePickerDialog(UpdateDataProvider.this, (timePicker, hourDay, minuteOfDay) -> {

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
            timePickerDialog = new TimePickerDialog(UpdateDataProvider.this, (timePicker, hourDay, minuteOfDay) -> {

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
                    binding.imageFieldEdit.setImageURI(result);
                    onImageChange = true;
                }
        );
    }

    private void getLocationUser(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(UpdateDataProvider.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Functions.isGPSEnabled(UpdateDataProvider.this)) {
                    LocationServices.getFusedLocationProviderClient(UpdateDataProvider.this).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(UpdateDataProvider.this).removeLocationUpdates(this);
                            if (locationResult.getLocations().size() > 0){
                                int index = locationResult.getLocations().size() - 1;

                                latitude = locationResult.getLocations().get(index).getLatitude();
                                longitude = locationResult.getLocations().get(index).getLongitude();

                                Geocoder geocoder = new Geocoder(UpdateDataProvider.this, Locale.getDefault());
                                try {

                                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude,1);
                                    binding.locationEdit.setText(addressList.get(0).getAddressLine(0));

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Toast.makeText(UpdateDataProvider.this, "Find location failed", Toast.LENGTH_SHORT).show();
                            }
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.setLocationEdit.setEnabled(true);
                        }
                    }, Looper.getMainLooper());

                } else {
                    Functions.turnOnGPS(locationRequest, UpdateDataProvider.this);
                    binding.progressCircular.setVisibility(View.GONE);
                    binding.setLocationEdit.setEnabled(true);
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1005);
                binding.progressCircular.setVisibility(View.GONE);
                binding.setLocationEdit.setEnabled(true);
            }
        }
    }

    private void setDataProviderField() {
        ReferenceDatabase.referenceProviderField.child(Data.uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProviderField modelProviderField = task.getResult().getValue(ModelProviderField.class);
                if (modelProviderField != null){
                    binding.nameFieldEdit.setText(modelProviderField.getName());
                    Picasso.get().load(modelProviderField.getUrlPhotoField()).into(binding.imageFieldEdit);
                    binding.numberPhoneEdit.setText(modelProviderField.getNumberPhone());
                    binding.bankAccountNumberEdit.setText(modelProviderField.getBankAccountNumber());
                    binding.locationEdit.setText(modelProviderField.getLocation());
                    binding.timeOpenResult.setText(modelProviderField.getOpenTime());
                    binding.timeCloseResult.setText(modelProviderField.getCloseTime());
                    binding.priceField.setText(String.valueOf(modelProviderField.getPriceField()));
                    oldUrl = modelProviderField.getUrlPhotoField();
                    latitude = modelProviderField.getLatitude();
                    longitude = modelProviderField.getLongitude();
                }else {
                    Toast.makeText(UpdateDataProvider.this, "Provider field is null", Toast.LENGTH_SHORT).show();
                    Functions.updateUI(UpdateDataProvider.this, Home.class);
                    finish();
                }
            }else {
                Toast.makeText(UpdateDataProvider.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeFieldWithImage() {
        FirebaseStorage referenceStorage = FirebaseStorage.getInstance();
        String name_photo = referenceStorage.getReferenceFromUrl(oldUrl).getName();
        StorageReference deleteRef = referenceStorage.getReference("field_provider/"+name_photo);

        deleteRef.delete().addOnSuccessListener(unused -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            String fileName = dateFormat.format(now);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference("field_provider/"+fileName);

            Bitmap bitmap = ((BitmapDrawable) binding.imageFieldEdit.getDrawable()).getBitmap();
            ByteArrayOutputStream baOs = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
            byte[] data = baOs.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data);

            uploadTask
                    .addOnFailureListener(e -> {
                        binding.progressCircular.setVisibility(View.GONE);
                        binding.editBtn.setEnabled(true);
                        Toast.makeText(UpdateDataProvider.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> changeProviderField(String.valueOf(uri)))
                            .addOnFailureListener(e -> {
                                binding.progressCircular.setVisibility(View.GONE);
                                binding.editBtn.setEnabled(true);
                                Toast.makeText(UpdateDataProvider.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }));

        }).addOnFailureListener(e -> {
            binding.progressCircular.setVisibility(View.GONE);
            binding.editBtn.setEnabled(true);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void changeProviderField(String uri) {
        ModelProviderField modelProviderField = new ModelProviderField(
                fieldName,
                uri,
                phoneNumber,
                accNumber,
                location,
                latitude,
                longitude,
                0.0,
                openTime,
                closeTime,
                priceField
        );

        ReferenceDatabase.referenceProviderField.child(Data.uid).setValue(modelProviderField).addOnSuccessListener(unused -> {
            binding.progressCircular.setVisibility(View.GONE);
            binding.editBtn.setEnabled(true);
            Toast.makeText(UpdateDataProvider.this, "Update Data Success", Toast.LENGTH_SHORT).show();
            Functions.updateUI(UpdateDataProvider.this, Dashboard.class);
        }).addOnFailureListener(e -> {
            binding.progressCircular.setVisibility(View.GONE);
            binding.editBtn.setEnabled(true);
            Toast.makeText(UpdateDataProvider.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}