package com.ark.futsalbookedapps.Views.Auth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.databinding.ActivityAuthenticationBinding;

public class Authentication extends AppCompatActivity {

    private ActivityAuthenticationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
    }

    private void listenerComponent() {
        binding.signInBtn.setOnClickListener(view -> Functions.updateUI(this, SignIn.class));
        binding.signUpBtn.setOnClickListener(view -> Functions.updateUI(this, SignUp.class));
    }
}