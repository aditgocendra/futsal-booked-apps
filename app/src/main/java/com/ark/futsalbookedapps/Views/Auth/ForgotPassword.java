package com.ark.futsalbookedapps.Views.Auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.forgotPassBtn.setOnClickListener(view -> {
            String email = binding.emailForgotPass.getText().toString();

            if (email.isEmpty()){
                Toast.makeText(this, "Email cannot be null", Toast.LENGTH_SHORT).show();
            }else {
                binding.progressCircular.setVisibility(View.VISIBLE);
                binding.forgotPassBtn.setEnabled(false);
                forgotPassword(email);
            }
        });
    }

    private void forgotPassword(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> Toast.makeText(ForgotPassword.this, "We have sent assistance via your email, please verify and you will be able to use your account again", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(ForgotPassword.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());

        binding.progressCircular.setVisibility(View.INVISIBLE);
        binding.forgotPassBtn.setEnabled(true);
    }
}