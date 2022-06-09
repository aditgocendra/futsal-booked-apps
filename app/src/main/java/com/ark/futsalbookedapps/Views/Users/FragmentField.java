package com.ark.futsalbookedapps.Views.Users;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.ark.futsalbookedapps.Adapter.AdapterProviderField;
import com.ark.futsalbookedapps.Adapter.AdapterSlider;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.Models.ModelSlider;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.Views.ProviderField.ProviderFieldRegister;
import com.ark.futsalbookedapps.databinding.FragmentFieldBinding;
import com.ark.futsalbookedapps.databinding.FragmentHomeBinding;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentField#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentField extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public FragmentField() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentField.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentField newInstance(String param1, String param2) {
        FragmentField fragment = new FragmentField();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    // init location
    private LocationRequest locationRequest;
    private double latitude, longitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestDataProvider();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

    }

    private FragmentFieldBinding binding;

    // init adapter
    private AdapterProviderField adapterProviderField;
    private List<ModelProviderField> listProviderField;

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    // bottom sheet filter and sort
    private BottomSheetDialog sheetDialogFilter, sheetDialogSort;

    // filter set
    // 0 == no filter
    // 1 == rated
    // 2 == price
    private int filter = 0;

    // sort set
    // 0 == ascending
    // 1 == descending
    private int sort = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFieldBinding.inflate(inflater, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerField.setLayoutManager(layoutManager);
        binding.recyclerField.setItemAnimator(new DefaultItemAnimator());

        adapterProviderField = new AdapterProviderField(getContext());
        binding.recyclerField.setAdapter(adapterProviderField);

        binding.recyclerField.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list field provider
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerField.getLayoutManager();
                assert linearLayoutManager != null;
                int totalCategory = linearLayoutManager.getItemCount();
                Log.d("Total Provider", String.valueOf(totalCategory));
                // check scroll on bottom
                if (!binding.recyclerField.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
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

        binding.cardGetLocation.setOnClickListener(view -> getLocationUser());

        // search
        binding.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0){
                    key = null;
                    requestDataProvider();

                }else {
                    String keySearch = binding.searchEdt.getText().toString();

                    listProviderField.clear();
                    key = null;
                    isLoadData = true;
                    adapterProviderField.notifyDataSetChanged();
                    searchProviderField(keySearch.toLowerCase());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.searchEdt.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Functions.hideKeyboard(requireActivity());
                handled = true;
            }
            return handled;
        });

        sheetDialogFilter = new BottomSheetDialog(requireContext());
        setBottomDialogFilter();
        binding.filterBtn.setOnClickListener(view -> {
            sheetDialogFilter.show();
        });

        sheetDialogSort = new BottomSheetDialog(requireContext());
        setBottomDialogSort();
        binding.sortBtn.setOnClickListener(view -> {
            sheetDialogSort.show();
        });


        return binding.getRoot();
    }

    private void requestDataProvider(){
        ReferenceDatabase.referenceProviderField.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countData = task.getResult().getChildrenCount();
                isLoadData = true;
                setDataProviderField();
            }else {
                Toast.makeText(getContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataProviderField(){
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            // no filter
            query = ReferenceDatabase.referenceProviderField.orderByKey().limitToFirst(10);

            // rated filter
            if (filter == 1){
                query = ReferenceDatabase.referenceProviderField.orderByChild("rating").limitToFirst(10);
            }

            // price filter
            if (filter == 2){
                query = ReferenceDatabase.referenceProviderField.orderByChild("priceField").limitToFirst(10);
            }

        }else {

            // no filter
            query = ReferenceDatabase.referenceProviderField.orderByKey().startAfter(key).limitToFirst(10);

            // rated filter
            if (filter == 1){
                query = ReferenceDatabase.referenceProviderField.orderByChild("rating").startAfter(key).limitToFirst(10);
            }

            // price filter
            if (filter == 2){
                query = ReferenceDatabase.referenceProviderField.orderByChild("priceField").startAfter(key).limitToFirst(10);
            }
        }

        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listProviderField = new ArrayList<>();
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
                    adapterProviderField.setItem(listProviderField);
                    if (listProviderField.size() != 0){
                        sortList();
                        adapterProviderField.notifyDataSetChanged();
                    }else {
                        Toast.makeText(getContext(), "Futsal field provider is empty", Toast.LENGTH_SHORT).show();
                    }
                }, 200);
                isLoadData = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchProviderField(String keywordSearch){
        if (!isLoadData){
            return;
        }

        Query query;
        query = ReferenceDatabase.referenceProviderField.orderByChild("name").startAt(keywordSearch).endAt(keywordSearch+"\uf8ff");
        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listProviderField = new ArrayList<>();
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
                    adapterProviderField.setItem(listProviderField);

                    if (listProviderField.size() != 0){
                        adapterProviderField.notifyDataSetChanged();
                    }
//                    binding.swipeRefresh.setRefreshing(false);
                }, 200);

                isLoadData = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLocationUser(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Functions.isGPSEnabled(requireContext())) {
                    LocationServices.getFusedLocationProviderClient(requireContext()).requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(requireContext()).removeLocationUpdates(this);
                            if (locationResult.getLocations().size() > 0){
                                int index = locationResult.getLocations().size() - 1;

                                latitude = locationResult.getLocations().get(index).getLatitude();
                                longitude = locationResult.getLocations().get(index).getLongitude();

                                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                                try {

                                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude,1);
                                    binding.locationUser.setText(addressList.get(0).getAddressLine(0));

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Toast.makeText(requireContext(), "Find location failed", Toast.LENGTH_SHORT).show();
                            }
//                            binding.progressCircular.setVisibility(View.GONE);
//                            binding.setLocation.setEnabled(true);
                        }
                    }, Looper.getMainLooper());

                } else {
                    Functions.turnOnGPS(locationRequest, getActivity());
//                    binding.progressCircular.setVisibility(View.GONE);
//                    binding.setLocation.setEnabled(true);
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1005);
//                binding.progressCircular.setVisibility(View.GONE);
//                binding.setLocation.setEnabled(true);
            }
        }
    }

    private void setBottomDialogFilter(){
        LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
        View viewBottomSheet = layoutInflater.inflate(R.layout.layout_filter_field, null);

        MaterialButton ratedFilter, priceRate, resetNoFilter;
        ratedFilter = viewBottomSheet.findViewById(R.id.rated_filter);
        priceRate = viewBottomSheet.findViewById(R.id.price_rate);
        resetNoFilter = viewBottomSheet.findViewById(R.id.reset_no_filter);

        ratedFilter.setOnClickListener(view -> {
            ratedFilter.setTextColor(getResources().getColor(R.color.white));
            ViewCompat.setBackgroundTintList(ratedFilter, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

            priceRate.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(priceRate, ColorStateList.valueOf(getResources().getColor(R.color.transparent)));

            resetNoFilter.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(resetNoFilter, ColorStateList.valueOf(getResources().getColor(R.color.transparent)));
            filter = 1;

            key = null;
            isLoadData = true;
            listProviderField.clear();
            adapterProviderField.notifyDataSetChanged();
            setDataProviderField();

            sheetDialogFilter.dismiss();
        });

        priceRate.setOnClickListener(view -> {
            priceRate.setTextColor(getResources().getColor(R.color.white));
            ViewCompat.setBackgroundTintList(priceRate, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

            ratedFilter.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(ratedFilter, ColorStateList.valueOf(getResources().getColor(R.color.transparent)));

            resetNoFilter.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(resetNoFilter, ColorStateList.valueOf(getResources().getColor(R.color.transparent)));

            filter = 2;

            key = null;
            isLoadData = true;
            listProviderField.clear();
            adapterProviderField.notifyDataSetChanged();
            setDataProviderField();

            sheetDialogFilter.dismiss();
        });

        resetNoFilter.setOnClickListener(view -> {
            resetNoFilter.setTextColor(getResources().getColor(R.color.white));
            ViewCompat.setBackgroundTintList(resetNoFilter, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

            priceRate.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(priceRate, ColorStateList.valueOf(getResources().getColor(R.color.transparent)));

            ratedFilter.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(ratedFilter, ColorStateList.valueOf(getResources().getColor(R.color.transparent)));

            filter = 0;

            key = null;
            isLoadData = true;
            listProviderField.clear();
            adapterProviderField.notifyDataSetChanged();
            setDataProviderField();

            sheetDialogFilter.dismiss();
        });

        sheetDialogFilter.setContentView(viewBottomSheet);
    }

    private void setBottomDialogSort(){
        LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
        View viewBottomSheet = layoutInflater.inflate(R.layout.layout_sort_data, null);

        MaterialButton ascendingBtn, descendingBtn;
        ascendingBtn = viewBottomSheet.findViewById(R.id.ascending_btn);
        descendingBtn = viewBottomSheet.findViewById(R.id.descending_btn);

        ascendingBtn.setTextColor(getResources().getColor(R.color.white));
        ViewCompat.setBackgroundTintList(ascendingBtn, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

        ascendingBtn.setOnClickListener(view -> {
            ascendingBtn.setTextColor(getResources().getColor(R.color.white));
            ViewCompat.setBackgroundTintList(ascendingBtn, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

            descendingBtn.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(descendingBtn, ColorStateList.valueOf(getResources().getColor(R.color.transparent)));

            sort = 0;
            sortList();
            adapterProviderField.notifyDataSetChanged();
            sheetDialogSort.dismiss();
        });

        descendingBtn.setOnClickListener(view -> {
            ascendingBtn.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(ascendingBtn, ColorStateList.valueOf(getResources().getColor(R.color.transparent)));

            descendingBtn.setTextColor(getResources().getColor(R.color.white));
            ViewCompat.setBackgroundTintList(descendingBtn, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

            sort = 1;
            sortList();
            adapterProviderField.notifyDataSetChanged();
            sheetDialogSort.dismiss();
        });

        sheetDialogSort.setContentView(viewBottomSheet);
    }

    private void sortList(){
        // no filter
        if (filter == 0){
            if (sort == 1){
                // descending
                Collections.sort(listProviderField, (modelProviderField1, modelProviderField2) -> {
                    String name1 = modelProviderField1.getName();
                    String name2 = modelProviderField2.getName();
                    return name2.compareToIgnoreCase(name1);
                });
            }else {
                // ascending
                Collections.sort(listProviderField, (modelProviderField1, modelProviderField2) -> {
                    String name1 = modelProviderField1.getName();
                    String name2 = modelProviderField2.getName();
                    return name1.compareToIgnoreCase(name2);
                });
            }
        }

        // rating sort
        if (filter == 1){
            if (sort == 1){
                // descending
                Collections.sort(listProviderField, (modelProviderField1, modelProviderField2) -> {
                    double filter1 = modelProviderField1.getRating();
                    double filter2 = modelProviderField2.getRating();
                    return Double.compare(filter2, filter1);
                });
            }else {
                // ascending
                Collections.sort(listProviderField, (modelProviderField1, modelProviderField2) -> {
                    double filter1 = modelProviderField1.getRating();
                    double filter2 = modelProviderField2.getRating();
                    return Double.compare(filter1, filter2);
                });
            }
        }

        // price sort
        if (filter == 2){
            if (sort == 1){
                // descending
                Collections.sort(listProviderField, (modelProviderField1, modelProviderField2) -> {
                    int filter1 = modelProviderField1.getPriceField();
                    int filter2 = modelProviderField2.getPriceField();
                    return Integer.compare(filter2, filter1);
                });
            }else {
                // ascending
                Collections.sort(listProviderField, (modelProviderField1, modelProviderField2) -> {
                    int filter1 = modelProviderField1.getPriceField();
                    int filter2 = modelProviderField2.getPriceField();
                    return Integer.compare(filter1, filter2);
                });
            }
        }
    }
}