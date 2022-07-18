package com.ark.futsalbookedapps.Views.Users;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ark.futsalbookedapps.Adapter.AdapterProviderField;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelAccount;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.databinding.FragmentHomeBinding;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.common.util.Strings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public FragmentHome() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestDataProvider();
        setDataAccountGlobal();
    }

    private FragmentHomeBinding binding;

    // init adapter
    private AdapterProviderField adapterProviderField;
    private List<ModelProviderField> listProviderField = new ArrayList<>();

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerFieldHome.setLayoutManager(layoutManager);
        binding.recyclerFieldHome.setItemAnimator(new DefaultItemAnimator());

        adapterProviderField = new AdapterProviderField(requireContext());
        binding.recyclerFieldHome.setAdapter(adapterProviderField);

        binding.bookedFieldBtn.setOnClickListener(view -> {
            if (!Data.uid.isEmpty()){
                Functions.updateUI(requireContext(), FieldBooked.class);
            }else {
                Toast.makeText(requireContext(), "Aplikasi sedang dipersiapkan, mohon tunggu sebentar", Toast.LENGTH_SHORT).show();
            }
        });
        binding.notificationView.setOnClickListener(view -> {
            if (!Data.uid.isEmpty()){
                Functions.updateUI(requireContext(), Notification.class);
            }else {
                Toast.makeText(requireContext(), "Aplikasi sedang dipersiapkan, mohon tunggu sebentar", Toast.LENGTH_SHORT).show();
            }
        });

        binding.allViewField.setOnClickListener(view -> {
            ((Home)getActivity()).changeFragment(new FragmentField());
        });

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

        List<SlideModel> imageList = new ArrayList<>();

        imageList.add(new SlideModel(R.drawable.ads_1, ScaleTypes.CENTER_CROP));
        imageList.add(new SlideModel(R.drawable.ads_2, ScaleTypes.CENTER_CROP));
        imageList.add(new SlideModel(R.drawable.ads_3, ScaleTypes.CENTER_CROP));

        binding.imageSlider.setImageList(imageList);

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
            query = ReferenceDatabase.referenceProviderField.orderByKey().limitToFirst(5);
        }else {
            query = ReferenceDatabase.referenceProviderField.orderByKey().startAfter(key).limitToFirst(5);
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
                    adapterProviderField.setItem(listProviderField);
                    if (listProviderField.size() != 0){
                        adapterProviderField.notifyDataSetChanged();
                        isLoadData = false;
                    }else {
                        Toast.makeText(getContext(), "Futsal field provider is empty", Toast.LENGTH_SHORT).show();
                    }
//                    binding.swipeRefresh.setRefreshing(false);
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                    binding.username.setText(Functions.capitalizeWord(modelAccount.getUsername()));
                }else {
                    Toast.makeText(requireContext(), "Account not found, please contact CS", Toast.LENGTH_SHORT).show();
                    System.exit(0);
                }
            }else {
                Toast.makeText(requireContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}