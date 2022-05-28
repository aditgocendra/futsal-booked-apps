package com.ark.futsalbookedapps.Views.Users;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelAccount;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.Views.Auth.Authentication;
import com.ark.futsalbookedapps.Views.ProviderField.Dashboard;
import com.ark.futsalbookedapps.Views.ProviderField.ProviderFieldRegister;
import com.ark.futsalbookedapps.databinding.ActivityAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Objects;

public class Account extends AppCompatActivity {

    private ActivityAccountBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
    }

    private void listenerComponent() {
        // Set data account
        binding.emailEditTi.setText(Data.email);
        binding.usernameEditTi.setText(Data.username);

        if (!Data.numberPhone.equals("-")){
            binding.phoneNumberEditTi.setText(Data.numberPhone);
        }

        binding.logoutBtn.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Functions.updateUI(this, Authentication.class);
        });

        binding.backBtn.setOnClickListener(view -> finish());

        binding.futsalFieldBtn.setOnClickListener(view -> checkUserProvider());

        binding.editDataProfileBtn.setOnClickListener(view -> {
            String email = Objects.requireNonNull(binding.emailEditTi.getText()).toString();
            String username = Objects.requireNonNull(binding.usernameEditTi.getText()).toString();
            String numberPhone = Objects.requireNonNull(binding.phoneNumberEditTi.getText()).toString();

            if (email.isEmpty()){
                Toast.makeText(this, "Email cannot be null", Toast.LENGTH_SHORT).show();
            }else if (username.isEmpty()){
                Toast.makeText(this, "Username cannot be null", Toast.LENGTH_SHORT).show();
            }else if (numberPhone.isEmpty()){
                Toast.makeText(this, "Phone number cannot be null", Toast.LENGTH_SHORT).show();
            }else {
                binding.progressCircular.setVisibility(View.VISIBLE);
                binding.editDataProfileBtn.setEnabled(false);
                saveChangeDataAccount(username, email, numberPhone);
            }
        });
    }

    private void saveChangeDataAccount(String email, String username, String numberPhone){
        ModelAccount modelAccount = new ModelAccount(
                username,
                email,
                Data.role,
                numberPhone
        );

        ReferenceDatabase.referenceAccount.child(Data.uid).setValue(modelAccount)
                .addOnSuccessListener(unused -> {
                    binding.progressCircular.setVisibility(View.INVISIBLE);
                    binding.editDataProfileBtn.setEnabled(true);
                    Toast.makeText(Account.this, "Success update account data", Toast.LENGTH_SHORT).show();
                    Data.username = username;
                    Data.numberPhone = numberPhone;

                })
                .addOnFailureListener(e -> {
                    binding.progressCircular.setVisibility(View.INVISIBLE);
                    binding.editDataProfileBtn.setEnabled(true);
                    Toast.makeText(Account.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserProvider(){
        ReferenceDatabase.referenceProviderField.child(Data.uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProviderField modelProviderField = task.getResult().getValue(ModelProviderField.class);

                if (modelProviderField != null){
                    Functions.updateUI(this, Dashboard.class);
                }else {
                    Dialog dialog = new Dialog(Account.this);
                    dialog.setContentView(R.layout.layout_confirmation_option);
                    dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.background_center_dialog));

                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    dialog.setCancelable(false); //Optional
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

                    Button yes = dialog.findViewById(R.id.btn_okay);
                    Button no = dialog.findViewById(R.id.btn_cancel);

                    dialog.show();

                    no.setOnClickListener(v -> dialog.dismiss());

                    yes.setOnClickListener(v -> {
                        Functions.updateUI(this, ProviderFieldRegister.class);
                        dialog.dismiss();
                    });
                }
            }else {
                Toast.makeText(Account.this, "Error : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}