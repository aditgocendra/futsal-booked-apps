package com.ark.futsalbookedapps.Adapter;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.Models.ModelFacility;
import com.ark.futsalbookedapps.Models.ModelGallery;
import com.ark.futsalbookedapps.Models.ModelNotification;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.Models.ModelSlider;
import com.ark.futsalbookedapps.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdapterProviderField extends RecyclerView.Adapter<AdapterProviderField.HomeVH> {

    private Context context;
    private List<ModelProviderField> listProviderFieldList = new ArrayList<>();
    private BottomSheetDialog bottomSheetDialog;

    private int hour, minute;
    private String playtime;

    // validation time user pick
    private boolean timeIsPick = false;
    private int availablePlaytime = 4;

    // component bottom sheet dialog order
    private TextView textDate, textTime,
            textDurationOne, textDurationTwo, textDurationThree, textDurationFour,
            textPriceOne, textPriceTwo, textPriceThree, textPriceFour, textTotalPrice;

    // notification
    private String title = "Lapangan Di Booking";
    private String msg = "Satu pelanggan telah memboking lapangan anda.";


    public AdapterProviderField(Context context) {
        this.context = context;
    }

    public void setItem(List<ModelProviderField> listProviderFieldList){
        this.listProviderFieldList = listProviderFieldList;
    }

    @NonNull
    @Override
    public HomeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_futsal_field, parent, false);
        return new HomeVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeVH holder, int position) {
        ModelProviderField modelProviderField = listProviderFieldList.get(position);
        holder.fieldNameText.setText(Functions.capitalizeWord(modelProviderField.getName()));
        holder.priceField.setText(Functions.currencyRp(String.valueOf(modelProviderField.getPriceField())) + " / Jam");
        holder.locationProvider.setText(modelProviderField.getLocation());
        Picasso.get().load(modelProviderField.getUrlPhotoField()).into(holder.imageProviderField);

        if (modelProviderField.getRating() <= 0){
            holder.ratingText.setText("Belum ada rating");
        }else {
            holder.ratingText.setText(String.valueOf(modelProviderField.getRating()));
        }

        holder.cardProviderField.setOnClickListener(view -> {
//            Intent intent = new Intent(context, DetailProviderField.class);
//            intent.putExtra("keyProviderField", modelProviderField.getKeyUserProviderField());
//            context.startActivity(intent);
              bottomSheetDialog = new BottomSheetDialog(context);
              setDialogDetailField(modelProviderField);
              bottomSheetDialog.show();

        });

        holder.ratingBar.setRating((float) modelProviderField.getRating());

    }

    @Override
    public int getItemCount() {
        return listProviderFieldList.size();
    }

    public static class HomeVH extends RecyclerView.ViewHolder {
        ImageView imageProviderField;
        TextView fieldNameText, ratingText, locationProvider, priceField;
        LinearLayout cardProviderField;
        RatingBar ratingBar;
        public HomeVH(@NonNull View itemView) {
            super(itemView);
            imageProviderField = itemView.findViewById(R.id.image_provider_field);
            fieldNameText = itemView.findViewById(R.id.field_name_text);
            ratingText = itemView.findViewById(R.id.rating_text);
            locationProvider = itemView.findViewById(R.id.location_provider);
            priceField = itemView.findViewById(R.id.price_field);
            cardProviderField = itemView.findViewById(R.id.card_provider_field);
            ratingBar = itemView.findViewById(R.id.rating_bar_field);

        }
    }

    private void setDialogDetailField(ModelProviderField modelProviderField){
        LayoutInflater li = LayoutInflater.from(context);
        View viewBottomDialog = li.inflate(R.layout.layout_dialog_detail_provider, null);

        ViewPager2 viewPager2 = viewBottomDialog.findViewById(R.id.view_pager_image);
        ImageView backBtn = viewBottomDialog.findViewById(R.id.back_btn);
        CardView cardWhatsapp = viewBottomDialog.findViewById(R.id.card_whatsapp);
        Button btnOrder = viewBottomDialog.findViewById(R.id.order_btn);

        TextView textProviderField = viewBottomDialog.findViewById(R.id.provider_field);
        TextView textLocationProvider = viewBottomDialog.findViewById(R.id.location_provider);
        TextView textRating = viewBottomDialog.findViewById(R.id.rating_text);
        TextView textTimeOpen = viewBottomDialog.findViewById(R.id.open_time_provider);

        RatingBar ratingBar = viewBottomDialog.findViewById(R.id.rating_bar_field);
        ratingBar.setRating((float) modelProviderField.getRating());

        textProviderField.setText(Functions.capitalizeWord(modelProviderField.getName()));
        textLocationProvider.setText(modelProviderField.getLocation());
        textRating.setText(String.valueOf(modelProviderField.getRating()));
        textTimeOpen.setText(modelProviderField.getOpenTime() + "-" + modelProviderField.getCloseTime() + " WIB");

        RecyclerView recyclerFacility = viewBottomDialog.findViewById(R.id.recycler_facility);
        TextView textNullFacility = viewBottomDialog.findViewById(R.id.null_facility);

        RecyclerView.LayoutManager layoutManagerProduct = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerFacility.setHasFixedSize(true);
        recyclerFacility.setLayoutManager(layoutManagerProduct);

        List<ModelFacility> facilityList = new ArrayList<>();
        ReferenceDatabase.referenceFacility.child(modelProviderField.getKeyUserProviderField()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                   ModelFacility modelFacility = ds.getValue(ModelFacility.class);
                   if (modelFacility != null){
                        facilityList.add(modelFacility);
                   }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (facilityList.isEmpty()){
                        textNullFacility.setVisibility(View.VISIBLE);
                    }else {
                        AdapterFacilityData adapterFacilityData = new AdapterFacilityData(context, facilityList);
                        recyclerFacility.setAdapter(adapterFacilityData);
                    }
                },100);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error Facility : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



        List<ModelGallery> listGallery = new ArrayList<>();
        ReferenceDatabase.referenceGallery.child(modelProviderField.getKeyUserProviderField()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelGallery modelGallery = ds.getValue(ModelGallery.class);
                    if (modelGallery != null){
                        listGallery.add(modelGallery);
                    }
                }
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listGallery.size() != 0){
                        viewPager2.setAdapter(new AdapterSlider(listGallery, viewPager2));
                    }else {
                        Toast.makeText(context, "Tidak ada detail gambar penyedia lapangan", Toast.LENGTH_SHORT).show();
                    }
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });

        // Handler slide
        Handler handlerSlide = new Handler();
        Runnable sliderRunnable = () -> viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);

        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                handlerSlide.removeCallbacks(sliderRunnable);
                handlerSlide.postDelayed(sliderRunnable, 3000);
            }
        });

        cardWhatsapp.setOnClickListener(view -> {
            String url = "https://api.whatsapp.com/send?phone=" + "+62"+ modelProviderField.getNumberPhone();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        });

        btnOrder.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();

            bottomSheetDialog = new BottomSheetDialog(context);
            setDialogOrder(modelProviderField);
            bottomSheetDialog.show();
        });


        backBtn.setOnClickListener(view -> bottomSheetDialog.dismiss());
        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private int lastSelectTimePos = 0;
    private String timeSelectedPlay;
    private AdapterTimePick adapterTimePick;
    private void setDialogOrder(ModelProviderField modelProviderField){
        LayoutInflater li = LayoutInflater.from(context);
        View viewBottomDialog = li.inflate(R.layout.layout_dialog_order, null);

        RecyclerView recyclerTimePicker = viewBottomDialog.findViewById(R.id.recycler_time_pick);

        LinearLayout layoutPickerTime = viewBottomDialog.findViewById(R.id.layout_time_picker);
        LinearLayout layoutPickDuration = viewBottomDialog.findViewById(R.id.layout_pick_duration);
        Button btnNext = viewBottomDialog.findViewById(R.id.btn_next);

        CardView cardPickDate, cardPickTime;
        cardPickDate = viewBottomDialog.findViewById(R.id.card_date_pick);
//        cardPickTime = viewBottomDialog.findViewById(R.id.card_pick_time);

        textDurationOne = viewBottomDialog.findViewById(R.id.duration_one);
        textDurationTwo = viewBottomDialog.findViewById(R.id.duration_two);
        textDurationThree = viewBottomDialog.findViewById(R.id.duration_three);
        textDurationFour = viewBottomDialog.findViewById(R.id.duration_four);

        // set price duration
        textPriceOne = viewBottomDialog.findViewById(R.id.text_price_one);
        textPriceTwo = viewBottomDialog.findViewById(R.id.text_price_two);
        textPriceThree = viewBottomDialog.findViewById(R.id.text_price_three);
        textPriceFour = viewBottomDialog.findViewById(R.id.text_price_four);

        textPriceOne.setText(Functions.currencyRp(String.valueOf(modelProviderField.getPriceField())));
        textPriceTwo.setText(Functions.currencyRp(String.valueOf(modelProviderField.getPriceField() * 2)));
        textPriceThree.setText(Functions.currencyRp(String.valueOf(modelProviderField.getPriceField() * 3)));
        textPriceFour.setText(Functions.currencyRp(String.valueOf(modelProviderField.getPriceField() * 4)));

        textDate = viewBottomDialog.findViewById(R.id.text_date);
//        textTime = viewBottomDialog.findViewById(R.id.text_time);
        textTotalPrice = viewBottomDialog.findViewById(R.id.text_total_price);

        ImageView backBtn = viewBottomDialog.findViewById(R.id.back_btn);
        backBtn.setOnClickListener(view -> bottomSheetDialog.dismiss());


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
        cardPickDate.setOnClickListener(view -> materialDatePicker.show(((AppCompatActivity) context).getSupportFragmentManager(), "DATE PICKER"));
        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            textDate.setText(materialDatePicker.getHeaderText());
            layoutPickerTime.setVisibility(View.VISIBLE);
        });

        // picker time manual
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
        recyclerTimePicker.setLayoutManager(gridLayoutManager);
        recyclerTimePicker.setHasFixedSize(true);

        adapterTimePick = new AdapterTimePick(context, Arrays.asList(Data.time24hours));
        recyclerTimePicker.setAdapter(adapterTimePick);

        adapterTimePick.RecyclerTimePick((textSelectionNew, pos) -> {
            if (lastSelectTimePos != pos){
                View itemView = Objects.requireNonNull(recyclerTimePicker.findViewHolderForLayoutPosition(lastSelectTimePos)).itemView;
                TextView textTimeSelectionLast = itemView.findViewById(R.id.text_time);
                ViewCompat.setBackgroundTintList(textTimeSelectionLast, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textTimeSelectionLast.setTextColor(context.getResources().getColor(R.color.primary));
            }
            ViewCompat.setBackgroundTintList(textSelectionNew, ColorStateList.valueOf(context.getResources().getColor(R.color.primary)));
            textSelectionNew.setTextColor(context.getResources().getColor(R.color.gray_white));
            timeSelectedPlay = textSelectionNew.getText().toString();
            lastSelectTimePos = pos;

            availablePlaytime = 0;
            validationPickTime(textDate.getText().toString(), timeSelectedPlay, layoutPickDuration);
        });



        // pick time booked
//        cardPickTime.setOnClickListener(view -> {
//            TimePickerDialog timePickerDialog;
//            Calendar calendar = Calendar.getInstance();
//            timePickerDialog = new TimePickerDialog(context, (timePicker, hourDay, minuteOfDay) -> {
//
//                hour = hourDay;
//                minute = minuteOfDay;
//
//                calendar.set(0,0,0, hour, minute);
//                textTime.setText(DateFormat.format("hh:mm aa", calendar));
//                availablePlaytime = 0;
//                validationPickTime(textDate.getText().toString(), textTime.getText().toString(), layoutPickDuration);
//
//            }, 24, 0, true);
//
//            timePickerDialog.show();
//        });

        // duration choice
        textDurationOne.setOnClickListener(view -> {
            textTotalPrice.setText(textPriceOne.getText().toString());
            // set color

            ViewCompat.setBackgroundTintList(textDurationOne, ColorStateList.valueOf(context.getResources().getColor(R.color.primary)));
            textDurationOne.setTextColor(Color.WHITE);

            if (textDurationTwo.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationTwo, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationTwo.setTextColor(context.getResources().getColor(R.color.primary));
            }

            if (textDurationThree.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationThree, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationThree.setTextColor(context.getResources().getColor(R.color.primary));
            }

            if (textDurationFour.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationFour, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationFour.setTextColor(context.getResources().getColor(R.color.primary));
            }

            btnNext.setEnabled(true);
            playtime = "1";
        });

        textDurationTwo.setOnClickListener(view -> {
            textTotalPrice.setText(textPriceTwo.getText().toString());
            // set color
            ViewCompat.setBackgroundTintList(textDurationTwo, ColorStateList.valueOf(context.getResources().getColor(R.color.primary)));
            textDurationTwo.setTextColor(Color.WHITE);

            if (textDurationOne.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationOne, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationOne.setTextColor(context.getResources().getColor(R.color.primary));
            }

            if (textDurationThree.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationThree, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationThree.setTextColor(context.getResources().getColor(R.color.primary));
            }

            if (textDurationFour.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationFour, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationFour.setTextColor(context.getResources().getColor(R.color.primary));
            }

            btnNext.setEnabled(true);
            playtime = "2";
        });

        textDurationThree.setOnClickListener(view -> {
            textTotalPrice.setText(textPriceThree.getText().toString());
            // set color
            ViewCompat.setBackgroundTintList(textDurationThree, ColorStateList.valueOf(context.getResources().getColor(R.color.primary)));
            textDurationThree.setTextColor(Color.WHITE);

            if (textDurationOne.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationOne, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationOne.setTextColor(context.getResources().getColor(R.color.primary));
            }

            if (textDurationTwo.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationTwo, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationTwo.setTextColor(context.getResources().getColor(R.color.primary));
            }

            if (textDurationFour.isEnabled()){
                ViewCompat.setBackgroundTintList(textDurationFour, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
                textDurationFour.setTextColor(context.getResources().getColor(R.color.primary));
            }

            btnNext.setEnabled(true);
            playtime = "3";
        });

        textDurationFour.setOnClickListener(view -> {
            textTotalPrice.setText(textPriceFour.getText().toString());
            // set color
            ViewCompat.setBackgroundTintList(textDurationFour, ColorStateList.valueOf(context.getResources().getColor(R.color.primary)));
            textDurationFour.setTextColor(Color.WHITE);

            ViewCompat.setBackgroundTintList(textDurationOne, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
            textDurationOne.setTextColor(context.getResources().getColor(R.color.primary));

            ViewCompat.setBackgroundTintList(textDurationTwo, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
            textDurationTwo.setTextColor(context.getResources().getColor(R.color.primary));

            ViewCompat.setBackgroundTintList(textDurationThree, ColorStateList.valueOf(context.getResources().getColor(R.color.gray_white)));
            textDurationThree.setTextColor(context.getResources().getColor(R.color.primary));

            btnNext.setEnabled(true);
            playtime = "4";
        });

        btnNext.setOnClickListener(view -> {
            String dateBooked = textDate.getText().toString();

            ModelBooked modelBooked = new ModelBooked(
                    Data.uid,
                    modelProviderField.getKeyUserProviderField(),
                    playtime,
                    dateBooked,
                    timeSelectedPlay,
                    "-",
                    0
            );

            bookedField(modelBooked, modelProviderField.getKeyUserProviderField());
            bottomSheetDialog.dismiss();

        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void bookedField(ModelBooked modelBooked, String keyProviderField){
        ReferenceDatabase.referenceBooked.push().setValue(modelBooked).addOnSuccessListener(unused -> {
            Toast.makeText(context, "Booked Success", Toast.LENGTH_SHORT).show();

            // create and sending notification
            ReferenceDatabase.referenceTokenNotification.child(keyProviderField).child("token").get().addOnCompleteListener(this::createNotification);
            saveNotification(title, msg, keyProviderField);

        }).addOnFailureListener(e -> Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createNotification(Task<DataSnapshot> task){
        String tokenReceiver;
        if (task.isSuccessful()){
            tokenReceiver = task.getResult().getValue().toString();
        }else {
            Toast.makeText(context, "Notification Task Failed", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray token = new JSONArray();
            token.put(tokenReceiver);

            JSONObject data = new JSONObject();
            data.put("title", title);
            data.put("message", msg);

            JSONObject body = new JSONObject();
            body.put(Data.REMOTE_MSG_DATA, data);
            body.put(Data.REMOTE_MSG_REGISTRATION_IDS, token);

            Functions.sendNotification(body.toString(), context);

        }catch (Exception e){
            Toast.makeText(context, "Error Notification : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNotification(String header, String msg, String keyReceiver){
        ModelNotification modelNotification = new ModelNotification(
                header,
                msg
        );
        ReferenceDatabase.referenceNotification.child(keyReceiver).push().setValue(modelNotification);
    }

    private void validationPickTime(String dateBooked, String timeBooked, LinearLayout layoutPickDuration){
//        hourBookedNew = Integer.parseInt(timeBooked.substring(0, 2)) + 4;
//        Toast.makeText(context, String.valueOf(hourBookedNew), Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, timeBooked.substring(3, 5), Toast.LENGTH_SHORT).show(); MINUTE
        ReferenceDatabase.referenceBooked.orderByChild("dateBooked").equalTo(dateBooked).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    ModelBooked modelBooked = dataSnapshot.getValue(ModelBooked.class);
                    if (modelBooked != null){

                        // check AM or PM time
                        if (timeBooked.substring(6, 7).equals(modelBooked.getTimeBooked().substring(6, 7))){
                            int timeBookedModel = Integer.parseInt(modelBooked.getTimeBooked().substring(0,2));
                            int timeBookedNow = Integer.parseInt(timeBooked.substring(0,2));

                            if (timeBookedNow + 1 == timeBookedModel){
                                availablePlaytime = 1;
                            }

                            if (timeBookedNow + 2 == timeBookedModel){
                                availablePlaytime = 2;
                            }

                            if (timeBookedNow + 3 == timeBookedModel){
                                availablePlaytime = 3;
                            }

                            if (timeBookedNow - 1 == timeBookedModel - 1){
                                timeIsPick = true;
                                break;
                            }

                            if (timeBookedNow - 2 == timeBookedModel - 1){
                                timeIsPick = true;
                                break;
                            }

                            if (timeBookedNow - 3 == timeBookedModel - 1){
                                timeIsPick = true;
                                break;
                            }
                        }
                        // check time booked is picking
                        if (modelBooked.getTimeBooked().equals(timeBooked)){
                            // break if the time pick same the time booked
                            timeIsPick = true;
                            break;
                        }
                    }
                }

                if (timeIsPick){
                    Toast.makeText(context, "Waktu tersebut telah di booking, silahkan pilih waktu lainnya", Toast.LENGTH_SHORT).show();
                    layoutPickDuration.setVisibility(View.GONE);
                }else {
                    layoutPickDuration.setVisibility(View.VISIBLE);

                    if (availablePlaytime == 1){
                        // disable
                        textDurationFour.setEnabled(false);
                        textDurationFour.setTextColor(context.getResources().getColor(R.color.gray_dop));

                        textDurationThree.setEnabled(false);
                        textDurationThree.setTextColor(context.getResources().getColor(R.color.gray_dop));

                        textDurationTwo.setTextColor(context.getResources().getColor(R.color.gray_dop));
                        textDurationTwo.setEnabled(false);

                        // enable
                        textDurationOne.setEnabled(true);
                        textDurationOne.setTextColor(context.getResources().getColor(R.color.primary_dark));
                    }

                    if (availablePlaytime == 2){
                        // disable
                        textDurationFour.setEnabled(false);
                        textDurationFour.setTextColor(context.getResources().getColor(R.color.gray_dop));

                        textDurationThree.setEnabled(false);
                        textDurationThree.setTextColor(context.getResources().getColor(R.color.gray_dop));

                        // enable
                        textDurationTwo.setEnabled(true);
                        textDurationTwo.setTextColor(context.getResources().getColor(R.color.primary_dark));

                        textDurationOne.setEnabled(true);
                        textDurationOne.setTextColor(context.getResources().getColor(R.color.primary_dark));
                    }

                    if (availablePlaytime == 3){
                        // disable
                        textDurationFour.setEnabled(false);
                        textDurationFour.setTextColor(context.getResources().getColor(R.color.gray_dop));

                        // enable
                        textDurationThree.setEnabled(true);
                        textDurationThree.setTextColor(context.getResources().getColor(R.color.primary_dark));

                        textDurationTwo.setEnabled(true);
                        textDurationTwo.setTextColor(context.getResources().getColor(R.color.primary_dark));

                        textDurationOne.setEnabled(true);
                        textDurationOne.setTextColor(context.getResources().getColor(R.color.primary_dark));
                    }

                    if (availablePlaytime == 4 || availablePlaytime == 0){
                        // enable
                        textDurationFour.setEnabled(true);
                        textDurationFour.setTextColor(context.getResources().getColor(R.color.primary_dark));

                        textDurationThree.setEnabled(true);
                        textDurationThree.setTextColor(context.getResources().getColor(R.color.primary_dark));

                        textDurationTwo.setTextColor(context.getResources().getColor(R.color.primary_dark));
                        textDurationTwo.setEnabled(true);

                        textDurationOne.setEnabled(true);
                        textDurationOne.setTextColor(context.getResources().getColor(R.color.primary_dark));
                    }
                }
                timeIsPick = false;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "DB Error : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
