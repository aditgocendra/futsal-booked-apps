package com.ark.futsalbookedapps.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelAccount;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.Models.ModelNotification;
import com.ark.futsalbookedapps.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterManageBookedField extends RecyclerView.Adapter<AdapterManageBookedField.ManageBookedFieldVH> {

    private Context context;
    private List<ModelBooked> listBooked = new ArrayList<>();
    private BottomSheetDialog bottomSheetDialogAccount;

    // notification
    private String title = "Futsaloka";
    private String msg = "Haloo, sepertinya lapangan yang kamu booking telah berubah statusnya";

    public AdapterManageBookedField(Context context) {
        this.context = context;
    }

    public void setItem(List<ModelBooked> listBooked){
        this.listBooked = listBooked;
    }

    @NonNull
    @Override
    public ManageBookedFieldVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_manage_field_booked, parent, false);
        return new ManageBookedFieldVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageBookedFieldVH holder, int position) {
        ModelBooked modelBooked = listBooked.get(position);

        // set data booked
        holder.playDateText.setText(modelBooked.getDateBooked());
        holder.playtimeText.setText(modelBooked.getPlaytime()+" Hours");

        if (modelBooked.getStatus() == 0){
            holder.statusText.setText("Waiting paid");
        }else if (modelBooked.getStatus() == 101){
            holder.statusText.setText("Canceled");
            holder.cancelBtn.setEnabled(false);
            holder.confirmPaidBtn.setEnabled(false);
        }else if (modelBooked.getStatus() == 202){
            holder.statusText.setText("DP payment in full");
            holder.cancelBtn.setEnabled(false);
            holder.confirmPaidBtn.setEnabled(false);
        }else if (modelBooked.getStatus() == 303){
            holder.statusText.setText("Booked Finish");
            holder.cancelBtn.setEnabled(false);
            holder.confirmPaidBtn.setEnabled(false);
        }


        // set data field booked
//        setDataField(modelBooked.getKeyProviderField(), modelBooked.getKeyFieldBooked(), holder);

        // set data user booked
        bottomSheetDialogAccount = new BottomSheetDialog(context);
        setDataUserBooked(modelBooked.getKeyUserBooked(), holder);


        holder.contactUserBtn.setOnClickListener(view -> bottomSheetDialogAccount.show());

        holder.cancelBtn.setOnClickListener(view -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.layout_confirmation_option);
            dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_center_dialog));

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog.setCancelable(false); //Optional
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

            // button customize
            Button okay, cancel;
            okay = dialog.findViewById(R.id.btn_okay);
            cancel = dialog.findViewById(R.id.btn_cancel);
            okay.setText("Okay");
            cancel.setText("Cancel");

            // text customize
            TextView messageText, headerText;
            headerText = dialog.findViewById(R.id.textView);
            messageText = dialog.findViewById(R.id.textView2);
            headerText.setText("Cancel Booked Field");
            messageText.setText("Are you sure cancel this booked ?");

            dialog.show();

            cancel.setOnClickListener(v -> dialog.dismiss());

            okay.setOnClickListener(v -> {
                updateStatusBooked(modelBooked, 101, position);
                dialog.dismiss();
            });
        });

        holder.confirmPaidBtn.setOnClickListener(view -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.layout_confirmation_option);
            dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_center_dialog));

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog.setCancelable(false); //Optional
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

            // button customize
            Button okay, cancel;
            okay = dialog.findViewById(R.id.btn_okay);
            cancel = dialog.findViewById(R.id.btn_cancel);
            okay.setText("Okay");
            cancel.setText("Cancel");

            // text customize
            TextView messageText, headerText;
            headerText = dialog.findViewById(R.id.textView);
            messageText = dialog.findViewById(R.id.textView2);
            headerText.setText("Konfirmasi DP");
            messageText.setText("Apakah pelanggan telah membayar DP ?");

            dialog.show();

            cancel.setOnClickListener(v -> dialog.dismiss());

            okay.setOnClickListener(v -> {
                updateStatusBooked(modelBooked, 202, position);
                dialog.dismiss();
            });
        });
    }

    @Override
    public int getItemCount() {
        return listBooked.size();
    }

    public static class ManageBookedFieldVH extends RecyclerView.ViewHolder {
        TextView userFieldText, playDateText, playtimeText, statusText;
        ImageView imageField;
        Button contactUserBtn, confirmPaidBtn, cancelBtn;

        public ManageBookedFieldVH(@NonNull View itemView) {
            super(itemView);

            userFieldText = itemView.findViewById(R.id.username_text);
            playDateText = itemView.findViewById(R.id.playdate_text);
            playtimeText = itemView.findViewById(R.id.playtime_text);
            statusText = itemView.findViewById(R.id.status_text);
            imageField = itemView.findViewById(R.id.image_field);

            contactUserBtn = itemView.findViewById(R.id.contact_user_btn);
            confirmPaidBtn = itemView.findViewById(R.id.confirmation_paid);
            cancelBtn = itemView.findViewById(R.id.cancel_booked);
        }
    }

    private void setDataUserBooked(String keyUserBooked, ManageBookedFieldVH holder) {
        ReferenceDatabase.referenceAccount.child(keyUserBooked).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelAccount modelAccount = task.getResult().getValue(ModelAccount.class);
                if (modelAccount != null){
                    holder.userFieldText.setText(modelAccount.getUsername());
                    setBottomDialogAccount(modelAccount);
                }else {
                    Toast.makeText(context, "Account not found", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(context, "Error : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setBottomDialogAccount(ModelAccount modelAccount){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View viewBottomDialog = layoutInflater.inflate(R.layout.layout_dialog_contact_user, null, false);

        TextView usernameText, emailText, phoneNumberText;

        usernameText = viewBottomDialog.findViewById(R.id.username_text);
        emailText = viewBottomDialog.findViewById(R.id.email_text);
        phoneNumberText = viewBottomDialog.findViewById(R.id.phone_number_text);

        usernameText.setText(modelAccount.getUsername());
        emailText.setText(modelAccount.getEmail());
        phoneNumberText.setText(modelAccount.getNumber_phone());

        Button btnWA = viewBottomDialog.findViewById(R.id.btn_whatsapp);

        btnWA.setOnClickListener(view -> {
            String url = "https://api.whatsapp.com/send?phone=" + "+62" + modelAccount.getNumber_phone();

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
            bottomSheetDialogAccount.dismiss();
        });

        bottomSheetDialogAccount.setContentView(viewBottomDialog);

    }

//    private void setDataField(String keyProviderField, String keyFieldBooked, ManageBookedFieldVH holder) {
//        ReferenceDatabase.referenceField.child(keyProviderField).child(keyFieldBooked).get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()){
//                ModelField modelField = task.getResult().getValue(ModelField.class);
//                assert modelField != null;
//                holder.fieldText.setText(modelField.getTypeField());
//                Picasso.get().load(modelField.getUrlField()).into(holder.imageField);
//            }else {
//                Toast.makeText(context, "Error : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void updateStatusBooked(ModelBooked modelBooked, int status,  int position) {
        modelBooked.setStatus(status);
        ReferenceDatabase.referenceBooked.child(modelBooked.getKeyBooked()).setValue(modelBooked).addOnSuccessListener(unused -> {

            listBooked.get(position).setStatus(status);
            this.notifyItemChanged(position);

            ReferenceDatabase.referenceTokenNotification.child(modelBooked.getKeyUserBooked()).child("token").get()
                    .addOnCompleteListener(this::createNotificationStatus);

            saveNotification(title, msg, modelBooked.getKeyUserBooked());

        }).addOnFailureListener(e -> Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createNotificationStatus(Task<DataSnapshot> task){
        String tokenReceiver;
        if (task.isSuccessful()){
            tokenReceiver = task.getResult().getValue().toString();
        }else {
            Toast.makeText(context, "Notification Task Failed", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray token = new JSONArray();
            token.put(tokenReceiver);

            JSONObject data = new JSONObject();
            data.put("title", title);
            data.put("message", msg);

            JSONObject body = new JSONObject();
            body.put(Data.REMOTE_MSG_DATA, data);
            body.put(Data.REMOTE_MSG_REGISTRATION_IDS, token);

            Functions.sendNotification(body.toString(), context);

        }catch (Exception e){
            Toast.makeText(context, "Error Notification : "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNotification(String header, String msg, String keyReceiver){
        ModelNotification modelNotification = new ModelNotification(
                header,
                msg
        );
        ReferenceDatabase.referenceNotification.child(keyReceiver).push().setValue(modelNotification);
    }
}
