package com.ark.futsalbookedapps.Views.Auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Models.ModelAccount;
import com.ark.futsalbookedapps.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private String username, email, pass, re_pass;

    private final DatabaseReference referenceAccount = FirebaseDatabase.getInstance().getReference().child("account");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.signInRedirect.setOnClickListener(view -> Functions.updateUI(this, SignIn.class));

        binding.signUpBtn.setOnClickListener(view -> {
            username = binding.usernameSignUp.getText().toString();
            email = binding.emailSignUp.getText().toString();
            pass = binding.passSignUp.getText().toString();
            re_pass = binding.rePassSignUp.getText().toString();

            if (username.isEmpty()){
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (email.isEmpty()){
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (pass.isEmpty()){
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            }else if (re_pass.isEmpty()){
                Toast.makeText(this, "Conformation password cannot be empty", Toast.LENGTH_SHORT).show();
            }else {
                if (!pass.equals(re_pass)){
                    Toast.makeText(this, "Password and confirmation password not same", Toast.LENGTH_SHORT).show();
                }else {
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    binding.signUpBtn.setEnabled(false);
                    createUser(email, pass);
                }
            }
        });
    }

    private void createUser(String email, String pass){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener(authResult -> {
            String uid = Objects.requireNonNull(authResult.getUser()).getUid();
            saveUserAccount(uid);
        }).addOnFailureListener(e -> {
            binding.progressCircular.setVisibility(View.INVISIBLE);
            binding.signUpBtn.setEnabled(true);
            Toast.makeText(SignUp.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserAccount(String uid){
        ModelAccount modelAccount = new ModelAccount(
                username,
                email,
                1,
                "-"
        );

        referenceAccount.child(uid).setValue(modelAccount).addOnSuccessListener(unused -> {
            binding.progressCircular.setVisibility(View.INVISIBLE);
            binding.signUpBtn.setEnabled(true);
            Functions.updateUI(SignUp.this, SignIn.class);
            Toast.makeText(SignUp.this, "Success create account, please login", Toast.LENGTH_SHORT).show();

        }).addOnFailureListener(e -> {
            binding.progressCircular.setVisibility(View.INVISIBLE);
            binding.signUpBtn.setEnabled(true);
            Toast.makeText(SignUp.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        });


    }
}