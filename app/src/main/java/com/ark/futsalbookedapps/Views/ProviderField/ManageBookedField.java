package com.ark.futsalbookedapps.Views.ProviderField;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ark.futsalbookedapps.Adapter.AdapterManageBookedField;
import com.ark.futsalbookedapps.Adapter.AdapterMonthSelection;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.databinding.ActivityManageBookedFieldBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ManageBookedField extends AppCompatActivity {

    private ActivityManageBookedFieldBinding binding;


    // init adapter
    private AdapterMonthSelection adapterMonthSelection;
    private AdapterManageBookedField adapterManageBookedField;
    private List<ModelBooked> listBooked;

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    // Bottom Sheet Dialog
    private BottomSheetDialog bottomSheetDialog, bottomSheetDialogReport;

    // filter data
    int status = 500;

    // report PDF
    private List<ModelBooked> listReportPdf;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageBookedFieldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerManageFieldBooked.setLayoutManager(layoutManager);
        binding.recyclerManageFieldBooked.setItemAnimator(new DefaultItemAnimator());

        adapterManageBookedField = new AdapterManageBookedField(this);
        binding.recyclerManageFieldBooked.setAdapter(adapterManageBookedField);

        listenerComponent();
        requestDataBooked();

    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        // listen user scroll recyclerview
        binding.recyclerManageFieldBooked.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list manage field booked
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerManageFieldBooked.getLayoutManager();
                assert linearLayoutManager != null;
                int totalBooked = linearLayoutManager.getItemCount();
                Log.d("Total Manage Booked", String.valueOf(totalBooked + " " + countData));
                // check scroll on bottom
                if (!binding.recyclerManageFieldBooked.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    // check data item if total item < total data in database == load more data
                    if (totalBooked < countData){
                        // load more data
                        if (!isLoadData && status == 500){
                            isLoadData = true;
                            setDataBooked();
                        }
                    }
                }
            }
        });

        bottomSheetDialogReport = new BottomSheetDialog(this);
        setBottomSheetDialogReport();
        binding.generatePdf.setOnClickListener(view -> {
                bottomSheetDialogReport.show();
        });

        bottomSheetDialog = new BottomSheetDialog(this);
        setBottomDialogFilter();
        binding.filterBtn.setOnClickListener(view -> {
            bottomSheetDialog.show();
        });


    }

    private void requestDataBooked(){
        ReferenceDatabase.referenceBooked.orderByChild("keyProviderField").equalTo(Data.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countData = snapshot.getChildrenCount();
                isLoadData = true;
                setDataBooked();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageBookedField.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataBooked() {
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            query = ReferenceDatabase.referenceBooked.orderByChild("keyProviderField").equalTo(Data.uid).limitToFirst(10);
        }else {
            query = ReferenceDatabase.referenceBooked.orderByChild("keyProviderField").equalTo(Data.uid).startAfter(key).limitToFirst(10);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressCircular.setVisibility(View.VISIBLE);
                listBooked = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelBooked modelBooked = ds.getValue(ModelBooked.class);
                    if (modelBooked != null){
                        // filter condition
                        if (status == 500){
                            modelBooked.setKeyBooked(ds.getKey());
                            listBooked.add(modelBooked);
                        }else {
                            if (modelBooked.getStatus() == status){
                                modelBooked.setKeyBooked(ds.getKey());
                                listBooked.add(modelBooked);
                            }
                        }
                        key = ds.getKey();
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterManageBookedField.setItem(listBooked);
                    if (listBooked.size() != 0){
                        adapterManageBookedField.notifyDataSetChanged();
                    }else {
                        Toast.makeText(ManageBookedField.this, "Not Yet Booked", Toast.LENGTH_SHORT).show();
                    }

                    isLoadData = false;
                    binding.progressCircular.setVisibility(View.GONE);
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageBookedField.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setBottomDialogFilter(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_filter_booked, null, false);

        Button waitingPaid, cancelled, dpPay, bookedFinish;
        waitingPaid = viewBottomDialog.findViewById(R.id.waiting_paid_btn);
        cancelled = viewBottomDialog.findViewById(R.id.cancelled_btn);
        dpPay = viewBottomDialog.findViewById(R.id.dp_pay_btn);
        bookedFinish = viewBottomDialog.findViewById(R.id.booked_finish_btn);

        waitingPaid.setOnClickListener(view -> {
            waitingPaid.setEnabled(false);
            cancelled.setEnabled(true);
            dpPay.setEnabled(true);
            bookedFinish.setEnabled(true);
            filterData(0);
            bottomSheetDialog.dismiss();
        });

        cancelled.setOnClickListener(view -> {
            cancelled.setEnabled(false);
            waitingPaid.setEnabled(true);
            dpPay.setEnabled(true);
            bookedFinish.setEnabled(true);
            filterData(101);
            bottomSheetDialog.dismiss();
        });

        dpPay.setOnClickListener(view -> {
            dpPay.setEnabled(false);
            waitingPaid.setEnabled(true);
            cancelled.setEnabled(true);
            bookedFinish.setEnabled(true);
            filterData(202);
            bottomSheetDialog.dismiss();
        });

        bookedFinish.setOnClickListener(view -> {
            bookedFinish.setEnabled(false);
            waitingPaid.setEnabled(true);
            cancelled.setEnabled(true);
            dpPay.setEnabled(true);
            filterData(303);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);

    }

    private void filterData(int codeStatus){
        status = codeStatus;
        key = null;
        listBooked.clear();
        isLoadData = true;
        setDataBooked();
    }

    private int lastSelectTimePos = 0;
    private String monthSelected;
    private String yearSelected;

    private void setBottomSheetDialogReport(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_dialog_generate_pdf, null, false);
        RecyclerView recyclerGenerate = viewBottomDialog.findViewById(R.id.month_selection_recycler);
        AutoCompleteTextView autoCompleteYear = viewBottomDialog.findViewById(R.id.auto_complete_year);

        // set adapter unit auto complete
        String[] year = {"2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, R.layout.layout_option_item, year);
        autoCompleteYear.setAdapter(yearAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerGenerate.setLayoutManager(gridLayoutManager);
        recyclerGenerate.setHasFixedSize(true);

        adapterMonthSelection = new AdapterMonthSelection(this, Arrays.asList(Data.months));
        recyclerGenerate.setAdapter(adapterMonthSelection);

        adapterMonthSelection.RecyclerMonthPick((textSelectionNew, pos) -> {
            if (lastSelectTimePos != pos){
                View itemView = Objects.requireNonNull(recyclerGenerate.findViewHolderForLayoutPosition(lastSelectTimePos)).itemView;
                TextView textTimeSelectionLast = itemView.findViewById(R.id.month_time);
                ViewCompat.setBackgroundTintList(textTimeSelectionLast, ColorStateList.valueOf(getResources().getColor(R.color.gray_white)));
                textTimeSelectionLast.setTextColor(getResources().getColor(R.color.primary));
            }
            ViewCompat.setBackgroundTintList(textSelectionNew, ColorStateList.valueOf(getResources().getColor(R.color.primary)));
            textSelectionNew.setTextColor(getResources().getColor(R.color.gray_white));
            monthSelected = textSelectionNew.getText().toString().substring(0,3);
            lastSelectTimePos = pos;

        });

        Button btnGenerate = viewBottomDialog.findViewById(R.id.btn_generate);
        btnGenerate.setOnClickListener(view -> {
            yearSelected = autoCompleteYear.getText().toString();

            if (yearSelected.isEmpty()){
                Toast.makeText(this, "Anda belum memilih tahun", Toast.LENGTH_SHORT).show();
            }else if (monthSelected.isEmpty()){
                Toast.makeText(this, "Anda belum memilih bulan", Toast.LENGTH_SHORT).show();
            }else {
                getReportData();
                bottomSheetDialog.dismiss();
            }

        });

        bottomSheetDialogReport.setContentView(viewBottomDialog);
    }

    private void getReportData(){
        ReferenceDatabase.referenceBooked.orderByChild("keyProviderField").equalTo(Data.uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listReportPdf = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelBooked modelBooked = ds.getValue(ModelBooked.class);
                    if (modelBooked != null){
                        String getYear = modelBooked.getDateBooked().substring(modelBooked.getDateBooked().length() - 4);
                        String getMonth = modelBooked.getDateBooked().substring(0, 3);
                        if (yearSelected.equals(getYear) && monthSelected.equals(getMonth)){
                            listReportPdf.add(modelBooked);
                        }
                    }
                }
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listReportPdf.size() != 0){
                        sortList();
                        generateReport();
                    }else {
                        Toast.makeText(ManageBookedField.this, "Tidak ada satupun booking pada tahun dan bulan tersebut", Toast.LENGTH_SHORT).show();
                    }

                },500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageBookedField.this, "Error : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateReport(){
        int width = 1200;

        PdfDocument pdfDocument = new PdfDocument();
        Paint titlePaint = new Paint();
        Paint dataPaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, 2000, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(32);
        canvas.drawText("Laporan Booking Lapangan Futsal", width / 2, 120, titlePaint);

        dataPaint.setColor(Color.BLACK);
        dataPaint.setTextSize(30f);

        dataPaint.setStyle(Paint.Style.STROKE);
        dataPaint.setStrokeWidth(2);
        canvas.drawRect(20, 180, width - 20, 260, dataPaint);

        dataPaint.setTextAlign(Paint.Align.LEFT);
        dataPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("No", 35, 230, dataPaint);
        canvas.drawText("Tanggal Booking", 115, 230, dataPaint);
        canvas.drawText("Waktu Main",445, 230, dataPaint);
        canvas.drawText("Lama Permainan", 665, 230, dataPaint);
        canvas.drawText("Status", 930, 230, dataPaint);

        canvas.drawLine(100, 190, 100, 240, dataPaint);
        canvas.drawLine(430, 190, 430, 240, dataPaint);
        canvas.drawLine(650, 190, 650, 240, dataPaint);
        canvas.drawLine(915, 190, 915, 240, dataPaint);

        int yStart = 320;
        for (int i = 0; i < listReportPdf.size(); i++){
                canvas.drawText(String.valueOf(i+1), 35, yStart, dataPaint);
                canvas.drawText(listReportPdf.get(i).getDateBooked(), 115, yStart, dataPaint);
                canvas.drawText(listReportPdf.get(i).getTimeBooked(), 445, yStart, dataPaint);
                canvas.drawText(listReportPdf.get(i).getPlaytime() + " Jam", 730, yStart, dataPaint);

                String descStatus = "";
                if (listReportPdf.get(i).getStatus() == 0){
                    descStatus ="Waiting paid";
                }else if (listReportPdf.get(i).getStatus() == 101){
                    descStatus = "Canceled";
                }else if (listReportPdf.get(i).getStatus() == 202){
                    descStatus = "Dp Payment in Full";
                }else if (listReportPdf.get(i).getStatus() == 303){
                    descStatus = "Booked finish";
                }

                canvas.drawText(descStatus, 930, yStart, dataPaint);
                yStart += 80;
            }

        pdfDocument.finishPage(page);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/Laporan-Booking-Lapangan-Futsal.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "Laporan PDF berhasil dibuat", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortList() {
        Collections.sort(listReportPdf, (modelBooked1, modelBooked2) -> {
            String date1 = modelBooked1.getDateBooked().substring(4, 6);
            String date2 = modelBooked2.getDateBooked().substring(4, 6);
            return date1.compareToIgnoreCase(date2);
        });

    }


}