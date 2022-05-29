package com.ark.futsalbookedapps.Views.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ark.futsalbookedapps.Adapter.AdapterDetailProviderField;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.Models.ModelField;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.databinding.ActivityDetailProviderFieldBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DetailProviderField extends AppCompatActivity {

    private ActivityDetailProviderFieldBinding binding;

    private String phoneNumberProvider;
    private double latitude, longitude;

    // init intent extra
    private String keyProviderField;
    
    // init adapter
    private AdapterDetailProviderField adapterDetailProviderField;
    private List<ModelField> listField;

    // init bottom sheet dialog
    private BottomSheetDialog bottomSheetDialog;
    private String fieldKeySelected;

    // time
    private int hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailProviderFieldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        keyProviderField = getIntent().getStringExtra("keyProviderField");

        RecyclerView.LayoutManager layoutManagerProduct = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerDetailField.setHasFixedSize(true);
        binding.recyclerDetailField.setLayoutManager(layoutManagerProduct);

        listenerComponent();
        setDataProviderField();
        setDataField();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.previewLocation.setOnClickListener(view -> Functions.previewStreetMaps(latitude, longitude, this));
        binding.cardWhatsapp.setOnClickListener(view -> {
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumberProvider;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        bottomSheetDialog = new BottomSheetDialog(this);
        binding.bookedBtn.setOnClickListener(view -> bottomSheetDialog.show());
    }

    private void setBottomDialogBooked(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_dialog_booked_field, null, false);
        AutoCompleteTextView autoCompleteField = viewBottomDialog.findViewById(R.id.auto_complete_field);
        TextInputEditText playTime = viewBottomDialog.findViewById(R.id.play_time);

        TextView textPriceDP = viewBottomDialog.findViewById(R.id.price_dp);

        CardView cardPickDate = viewBottomDialog.findViewById(R.id.card_pick_date);
        TextView textDate = viewBottomDialog.findViewById(R.id.text_date_tv);

        CardView cardPickTime = viewBottomDialog.findViewById(R.id.card_pick_time);
        TextView textTime = viewBottomDialog.findViewById(R.id.text_time_tv);

        Button bookedBtn = viewBottomDialog.findViewById(R.id.booked_fix_btn);

        // Layout
        LinearLayout layoutDP = viewBottomDialog.findViewById(R.id.layout_dp);
        LinearLayout layoutDate = viewBottomDialog.findViewById(R.id.layout_date);
        LinearLayout layoutTime = viewBottomDialog.findViewById(R.id.layout_time);

        ArrayAdapter<ModelField> fieldArrayAdapter = new ArrayAdapter<>(this, R.layout.layout_option_item);
        fieldArrayAdapter.addAll(listField);
        autoCompleteField.setAdapter(fieldArrayAdapter);

        autoCompleteField.setOnItemClickListener((adapterView, view, i, l) -> {
            ModelField modelField = (ModelField) adapterView.getItemAtPosition(i);
            fieldKeySelected = modelField.getKeyField();

            if (layoutDP.getVisibility() == View.GONE){
                layoutDP.setVisibility(View.VISIBLE);
                layoutDate.setVisibility(View.VISIBLE);
                layoutTime.setVisibility(View.VISIBLE);
            }

            textPriceDP.setText(Functions.currencyRp(modelField.getMinDP()));
        });

        // pick date booked
        Locale.setDefault(Locale.ENGLISH);
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select Date Booked");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        MaterialDatePicker<Long> materialDatePicker = builder.build();

        cardPickDate.setOnClickListener(view -> materialDatePicker.show(getSupportFragmentManager(), "DATE PICKER"));
        materialDatePicker.addOnPositiveButtonClickListener(selection -> textDate.setText(materialDatePicker.getHeaderText()));

        // pick time booked
        cardPickTime.setOnClickListener(view -> {
            TimePickerDialog timePickerDialog;
            Calendar calendar = Calendar.getInstance();
            timePickerDialog = new TimePickerDialog(DetailProviderField.this, (timePicker, hourDay, minuteOfDay) -> {

                hour = hourDay;
                minute = minuteOfDay;

                calendar.set(0,0,0, hour, minute);
                textTime.setText(DateFormat.format("hh:mm aa", calendar));
            }, 24, 0, true);
            timePickerDialog.show();
        });

        bookedBtn.setOnClickListener(view -> {
            String playtime = Objects.requireNonNull(playTime.getText()).toString();
            String dateBooked = textDate.getText().toString();
            String timeBooked = textTime.getText().toString();

            if (playtime.isEmpty()){
                Toast.makeText(this, "Playtime cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (fieldKeySelected.isEmpty()){
                Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (dateBooked.equals("-")){
                Toast.makeText(this, "Please select date booked", Toast.LENGTH_SHORT).show();
            }else if (timeBooked.equals("-")){
                Toast.makeText(this, "Please select time booked", Toast.LENGTH_SHORT).show();
            }else {
                ModelBooked modelBooked = new ModelBooked(
                        Data.uid,
                        keyProviderField,
                        fieldKeySelected,
                        playtime,
                        dateBooked,
                        timeBooked,
                        0
                );
                bookedField(modelBooked);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void setDataProviderField(){
        ReferenceDatabase.referenceProviderField.child(keyProviderField).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProviderField modelProviderField = task.getResult().getValue(ModelProviderField.class);
                if (modelProviderField != null){
                    Picasso.get().load(modelProviderField.getUrlPhotoField()).into(binding.imageField);
                    binding.fieldNameText.setText(modelProviderField.getName());
                    binding.locationField.setText(modelProviderField.getLocation());
                    binding.numberPhone.setText(modelProviderField.getNumberPhone());
                    
                    latitude = modelProviderField.getLatitude();
                    longitude = modelProviderField.getLongitude();
                    phoneNumberProvider = modelProviderField.getNumberPhone();
                }else {
                    Toast.makeText(this, "Data Provider is null", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }else {
                Toast.makeText(DetailProviderField.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setDataField(){
        ReferenceDatabase.referenceField.child(keyProviderField).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        adapterDetailProviderField = new AdapterDetailProviderField(DetailProviderField.this, listField);
                        binding.recyclerDetailField.setAdapter(adapterDetailProviderField);
                        setBottomDialogBooked();
                    }else {
                        Toast.makeText(DetailProviderField.this, "The field provider has not set the field for rent", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, 200);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailProviderField.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bookedField(ModelBooked modelBooked){
        ReferenceDatabase.referenceBooked.push().setValue(modelBooked).addOnSuccessListener(unused -> {
            Toast.makeText(DetailProviderField.this, "Booked Success", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(DetailProviderField.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }


}