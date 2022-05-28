package com.ark.futsalbookedapps.Views.Auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Views.Users.Home;
import com.ark.futsalbookedapps.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.recoverPassRedirect.setOnClickListener(view -> Functions.updateUI(this, ForgotPassword.class));

        binding.signInBtn.setOnClickListener(view -> {
            String email = binding.emailSignIn.getText().toString();
            String pass = binding.passSignIn.getText().toString();

            if (email.isEmpty()){
                Toast.makeText(this, "Email cannot be null", Toast.LENGTH_SHORT).show();
            }else if (pass.isEmpty()){
                Toast.makeText(this, "Password cannot be null", Toast.LENGTH_SHORT).show();
            }else {
                binding.progressCircular.setVisibility(View.VISIBLE);
                binding.signInBtn.setEnabled(false);
                signIn(email, pass);
            }
        });
    }

    private void signIn(String email, String pass){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass).addOnSuccessListener(authResult -> {
            FirebaseUser firebaseUser = authResult.getUser();
            binding.progressCircular.setVisibility(View.INVISIBLE);

            if (firebaseUser != null){
                Data.uid = firebaseUser.getUid();
                Functions.updateUI(this, Home.class);
                finish();
            }else {
                Toast.makeText(this, "User account not found, please contact CS", Toast.LENGTH_SHORT).show();
            }
            binding.progressCircular.setVisibility(View.INVISIBLE);
            binding.signInBtn.setEnabled(true);

        }).addOnFailureListener(e -> {
            binding.progressCircular.setVisibility(View.INVISIBLE);
            binding.signInBtn.setEnabled(true);
            Toast.makeText(SignIn.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}