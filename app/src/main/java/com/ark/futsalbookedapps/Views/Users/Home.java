package com.ark.futsalbookedapps.Views.Users;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.Toast;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelAccount;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.databinding.ActivityHomeBinding;
import com.ark.futsalbookedapps.databinding.FragmentHomeBinding;
import com.google.android.gms.common.util.Strings;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.Objects;

public class Home extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();

        setToken();

    }

    private void listenerComponent() {
    //binding.cardBookedField.setOnClickListener(view -> Functions.updateUI(this, FieldBooked.class));
        // listen user scroll recyclerview

        changeFragment(new FragmentHome());
        binding.bottomNavbar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.home:
                    changeFragment(new FragmentHome());
                    break;
                case R.id.field:
                    changeFragment(new FragmentField());
                    break;
                case R.id.account:
                    if (!Strings.isEmptyOrWhitespace(Data.username)){
                        changeFragment(new FragmentAccount());
                    }else {
                        Toast.makeText(this, "Aplikasi sedang dipersiapkan, mohon tunggu sebentar", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return true;
        });

    }

    public void changeFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setCustomAnimations(com.google.android.material.R.anim.na, com.google.android.material.R.anim.fragment_fade_exit);
        fragmentTransaction.replace(R.id.frame_layout_fragment, fragment);
        fragmentTransaction.commit();
    }



    private void setToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        ReferenceDatabase.referenceTokenNotification.child(Data.uid).child("token").setValue(token);
    }
}