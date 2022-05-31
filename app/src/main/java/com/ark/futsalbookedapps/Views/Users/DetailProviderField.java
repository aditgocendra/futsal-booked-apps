package com.ark.futsalbookedapps.Views.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.ark.futsalbookedapps.Adapter.AdapterDetailProviderField;
import com.ark.futsalbookedapps.Adapter.AdapterReviewProvider;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.Models.ModelField;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.Models.ModelReviewProvider;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.databinding.ActivityDetailProviderFieldBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

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
    
    // init adapter detail provider
    private AdapterDetailProviderField adapterDetailProviderField;
    private List<ModelField> listField;

    // init adapter user review
    private AdapterReviewProvider adapterReviewProvider;
    private List<ModelReviewProvider> listReviewProvider;

    // param pagination data review provider
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

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

        // recycler detail provider
        RecyclerView.LayoutManager layoutManagerProduct = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerDetailField.setHasFixedSize(true);
        binding.recyclerDetailField.setLayoutManager(layoutManagerProduct);

        if (keyProviderField.equals(Data.uid)){
            binding.bookedBtn.setEnabled(false);
        }

        // recycler review user
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerReviewProviderField.setLayoutManager(layoutManager);
        binding.recyclerReviewProviderField.setItemAnimator(new DefaultItemAnimator());

        adapterReviewProvider = new AdapterReviewProvider(this);
        binding.recyclerReviewProviderField.setAdapter(adapterReviewProvider);

        requestReviewProvider();

        listenerComponent();
        setDataProviderField();
        setDataField();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.previewLocation.setOnClickListener(view -> Functions.previewStreetMaps(latitude, longitude, this));
        binding.cardWhatsapp.setOnClickListener(view -> {
            String url = "https://api.whatsapp.com/send?phone=" + "+62"+ phoneNumberProvider;

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        bottomSheetDialog = new BottomSheetDialog(this);
        binding.bookedBtn.setOnClickListener(view -> bottomSheetDialog.show());

        binding.recyclerReviewProviderField.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list review provider
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerReviewProviderField.getLayoutManager();
                assert linearLayoutManager != null;
                int totalCategory = linearLayoutManager.getItemCount();
                Log.d("Total Review Provider", String.valueOf(totalCategory));
                // check scroll on bottom
                if (!binding.recyclerReviewProviderField.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    // check data item if total item < total data in database == load more data
                    if (totalCategory < countData){
                        // load more data
                        if (!isLoadData){
                            isLoadData = true;
                            setDataProviderField();
                        }
                    }
                }
            }
        });
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

        // calendar constraint
        CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();
        calendarConstraintBuilder.setValidator(DateValidatorPointForward.now());
        builder.setCalendarConstraints(calendarConstraintBuilder.build());

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
                    binding.ratingText.setText("Rating : "+modelProviderField.getRating());

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
                        binding.totalField.setText("Total Field : "+listField.size());
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

            // create and sending notification
            ReferenceDatabase.referenceTokenNotification.child(keyProviderField).child("token").get().addOnCompleteListener(this::createNotification);

            finish();
        }).addOnFailureListener(e -> Toast.makeText(DetailProviderField.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createNotification(Task<DataSnapshot> task){
        String tokenReceiver;
        if (task.isSuccessful()){
            tokenReceiver = task.getResult().getValue().toString();
        }else {
            Toast.makeText(this, "Notification Task Failed", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray token = new JSONArray();
            token.put(tokenReceiver);

            JSONObject data = new JSONObject();
            data.put("title", "Field Booked");
            data.put("message", "Hi there are customers who have booked your place, let's take a look");

            JSONObject body = new JSONObject();
            body.put(Data.REMOTE_MSG_DATA, data);
            body.put(Data.REMOTE_MSG_REGISTRATION_IDS, token);

            Functions.sendNotification(body.toString(), DetailProviderField.this);

        }catch (Exception e){
            Toast.makeText(this, "Error Notification : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestReviewProvider(){
        ReferenceDatabase.referenceReview.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countData = task.getResult().getChildrenCount();
                isLoadData = true;
                setDataReview();
            }else {
                Toast.makeText(DetailProviderField.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataReview(){
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            query = ReferenceDatabase.referenceReview.child(keyProviderField).orderByKey().limitToFirst(10);
        }else {
            query = ReferenceDatabase.referenceReview.child(keyProviderField).orderByKey().startAfter(key).limitToFirst(10);
        }

        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listReviewProvider = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelReviewProvider modelReviewProvider = ds.getValue(ModelReviewProvider.class);
                    if (modelReviewProvider != null){
                        modelReviewProvider.setKeyReview(ds.getKey());
                        key = ds.getKey();
                        listReviewProvider.add(modelReviewProvider);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterReviewProvider.setItem(listReviewProvider);
                    if (listReviewProvider.size() != 0){
                        adapterReviewProvider.notifyDataSetChanged();
                        isLoadData = false;
                    }else {
                        Toast.makeText(DetailProviderField.this, "Not yet review", Toast.LENGTH_SHORT).show();
                    }
                }, 200);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailProviderField.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}