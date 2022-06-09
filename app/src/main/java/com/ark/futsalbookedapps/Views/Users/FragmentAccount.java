package com.ark.futsalbookedapps.Views.Users;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
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
import com.ark.futsalbookedapps.databinding.FragmentAccountBinding;
import com.ark.futsalbookedapps.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentAccount#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentAccount extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentAccount() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentAccount.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentAccount newInstance(String param1, String param2) {
        FragmentAccount fragment = new FragmentAccount();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private FragmentAccountBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        listenerComponent();

        return binding.getRoot();


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
            Functions.updateUI(getContext(), Authentication.class);
        });

        binding.futsalFieldBtn.setOnClickListener(view -> checkUserProvider());

        binding.editDataProfileBtn.setOnClickListener(view -> {
            String email = Objects.requireNonNull(binding.emailEditTi.getText()).toString();
            String username = Objects.requireNonNull(binding.usernameEditTi.getText()).toString();
            String numberPhone = Objects.requireNonNull(binding.phoneNumberEditTi.getText()).toString();

            if (email.isEmpty()){
                Toast.makeText(getContext(), "Email cannot be null", Toast.LENGTH_SHORT).show();
            }else if (username.isEmpty()){
                Toast.makeText(getContext(), "Username cannot be null", Toast.LENGTH_SHORT).show();
            }else if (numberPhone.isEmpty()){
                Toast.makeText(getContext(), "Phone number cannot be null", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Success update account data", Toast.LENGTH_SHORT).show();
                    Data.username = username;
                    Data.numberPhone = numberPhone;

                })
                .addOnFailureListener(e -> {
                    binding.progressCircular.setVisibility(View.INVISIBLE);
                    binding.editDataProfileBtn.setEnabled(true);
                    Toast.makeText(getContext(), "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkUserProvider(){
        ReferenceDatabase.referenceProviderField.child(Data.uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProviderField modelProviderField = task.getResult().getValue(ModelProviderField.class);

                if (modelProviderField != null){
                    Functions.updateUI(getContext(), Dashboard.class);
                }else {
                    Dialog dialog = new Dialog(getContext());
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
                        Functions.updateUI(getContext(), ProviderFieldRegister.class);
                        dialog.dismiss();
                    });
                }
            }else {
                Toast.makeText(getContext(), "Error : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}