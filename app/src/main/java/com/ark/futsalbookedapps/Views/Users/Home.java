package com.ark.futsalbookedapps.Views.Users;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelAccount;
import com.ark.futsalbookedapps.databinding.ActivityHomeBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
        setDataAccountGlobal();
    }

    private void listenerComponent() {
        binding.account.setOnClickListener(view -> {
            if (!Data.uid.isEmpty()){
                Functions.updateUI(this, Account.class);
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
}