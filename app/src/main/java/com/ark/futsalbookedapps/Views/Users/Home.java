package com.ark.futsalbookedapps.Views.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.ark.futsalbookedapps.Adapter.AdapterHome;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelAccount;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.databinding.ActivityHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Home extends AppCompatActivity {

    private ActivityHomeBinding binding;

    // init adapter
    private AdapterHome adapterHome;
    private List<ModelProviderField> listProviderField = new ArrayList<>();

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
        setDateTime();
        setDataAccountGlobal();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerFieldHome.setLayoutManager(layoutManager);
        binding.recyclerFieldHome.setItemAnimator(new DefaultItemAnimator());

        adapterHome = new AdapterHome(this);
        binding.recyclerFieldHome.setAdapter(adapterHome);


        requestDataProvider();
    }

    private void listenerComponent() {
        binding.account.setOnClickListener(view -> {
            if (!Data.uid.isEmpty()){
                Functions.updateUI(this, Account.class);
            }
        });

//        binding.swipeRefresh.setOnRefreshListener(() -> {
//            key = null;
//            listProviderField = new ArrayList<>();
//            requestDataProvider();
//        });

        // listen user scroll recyclerview
        binding.recyclerFieldHome.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list field provider
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerFieldHome.getLayoutManager();
                assert linearLayoutManager != null;
                int totalCategory = linearLayoutManager.getItemCount();
                Log.d("Total Provider", String.valueOf(totalCategory));
                // check scroll on bottom
                if (!binding.recyclerFieldHome.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
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

    private void setDateTime() {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        String month_name = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);

        binding.dateText.setText(String.valueOf(cal.get(Calendar.DATE)));
        String monthYear = month_name+" "+cal.get(Calendar.YEAR);
        binding.monthAndYear.setText(monthYear);

        Log.d("date", String.valueOf(cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)));
    }

    private void requestDataProvider(){
        ReferenceDatabase.referenceProviderField.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countData = task.getResult().getChildrenCount();
                isLoadData = true;
                setDataProviderField();
            }else {
                Toast.makeText(Home.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataAccountGlobal() {
        ReferenceDatabase.referenceAccount.child(Data.uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelAccount modelAccount = task.getResult().getValue(ModelAccount.class);
                if (modelAccount != null){
                    // Set global data account user
                    Data.username = modelAccount.getUsername();
                    Data.email = modelAccount.getEmail();
                    Data.numberPhone = modelAccount.getNumber_phone();
                    Data.role = modelAccount.getRole();
                }else {
                    Toast.makeText(Home.this, "Account not found, please contact CS", Toast.LENGTH_SHORT).show();
                    System.exit(0);
                }
            }else {
                Toast.makeText(Home.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataProviderField(){
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            query = ReferenceDatabase.referenceProviderField.orderByKey().limitToFirst(10);
        }else {
            query = ReferenceDatabase.referenceProviderField.orderByKey().startAfter(key).limitToFirst(10);
        }

        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelProviderField modelProviderField = ds.getValue(ModelProviderField.class);
                    if (modelProviderField != null){
                        modelProviderField.setKeyUserProviderField(ds.getKey());
                        key = ds.getKey();
                        listProviderField.add(modelProviderField);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterHome.setItem(listProviderField);
                    if (listProviderField.size() != 0){
                        adapterHome.notifyDataSetChanged();
                        isLoadData = false;
                    }else {
                        Toast.makeText(Home.this, "Futsal field provider is empty", Toast.LENGTH_SHORT).show();
                    }
//                    binding.swipeRefresh.setRefreshing(false);
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Home.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}