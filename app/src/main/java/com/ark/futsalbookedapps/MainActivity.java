package com.ark.futsalbookedapps;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Views.Auth.Authentication;
import com.ark.futsalbookedapps.Views.Users.Home;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Functions.checkWindowSetFlag(this);

        routes();
    }

    private void routes() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (firebaseUser != null){
                Functions.updateUI(MainActivity.this, Home.class);
                finish();
                Data.uid = firebaseUser.getUid();
            }else {
                Functions.updateUI(MainActivity.this, Authentication.class);
            }

        }, 1000);
    }
}