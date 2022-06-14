package com.ark.futsalbookedapps.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.Functions;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelBooked;
import com.ark.futsalbookedapps.Models.ModelNotification;
import com.ark.futsalbookedapps.Models.ModelProviderField;
import com.ark.futsalbookedapps.Models.ModelReviewProvider;
import com.ark.futsalbookedapps.R;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterFieldBooked extends RecyclerView.Adapter<AdapterFieldBooked.FieldBookedVH> {

    private final Context context;
    private List<ModelBooked> listBooked = new ArrayList<>();
    private Dialog dialogBankAccount;
    private BottomSheetDialog bottomSheetDialog;
    private RecyclerBooked listenerRecycler;

    // notification
    private String title = "Futsaloka";
    private String msg = "Haloo penyedia lapangan, telah ada pengguna yang memberikan review kepadamu";

    public AdapterFieldBooked(Context context) {
        this.context = context;
    }

    public void setItem(List<ModelBooked> listBooked){
        this.listBooked = listBooked;
    }

    public interface RecyclerBooked{
        void recyclerBooked(Button proofBtn, int pos);
    }

    public void RecyclerBooked(RecyclerBooked listenerRecycler){
        this.listenerRecycler = listenerRecycler;
    }

    @NonNull
    @Override
    public AdapterFieldBooked.FieldBookedVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_booked, parent, false);
        return new FieldBookedVH(view, listenerRecycler);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFieldBooked.FieldBookedVH holder, int position) {
        ModelBooked modelBooked = listBooked.get(position);

        if (modelBooked.getUrlProof().equals("-")){
            holder.proofBtn.setVisibility(View.VISIBLE);
        }

        // set data provider field
        setProviderField(modelBooked.getKeyProviderField(), holder);


        // set data booked
        holder.playtimeText.setText(modelBooked.getPlaytime()+" Jam");
        holder.datePlayText.setText(modelBooked.getDateBooked());
        holder.timePlayText.setText(modelBooked.getTimeBooked());

        if (modelBooked.getStatus() == 0){
            holder.statusText.setText("Status : Waiting paid");
        }else if (modelBooked.getStatus() == 101){
            holder.statusText.setText("Status : Canceled");
            holder.bankAccNumber.setEnabled(false);
            holder.ratingBtn.setVisibility(View.GONE);
            holder.previewLocBtn.setVisibility(View.GONE);
            holder.contactProvider.setVisibility(View.GONE);
        }else if (modelBooked.getStatus() == 202){
            holder.statusText.setText("Status : DP payment in full");
            holder.bankAccNumber.setEnabled(false);
            holder.ratingBtn.setVisibility(View.VISIBLE);
        }else if (modelBooked.getStatus() == 303){
            holder.statusText.setText("Status : Booked Finish");
            holder.bankAccNumber.setEnabled(false);
            holder.ratingBtn.setVisibility(View.GONE);
            holder.previewLocBtn.setVisibility(View.GONE);
            holder.contactProvider.setVisibility(View.GONE);
        }

        holder.bankAccNumber.setOnClickListener(view -> dialogBankAccount.show());

        // rating & review
        holder.ratingBtn.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(context);
            setBottomSheetDialog(modelBooked, position);
            bottomSheetDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return listBooked.size();
    }

    public static class FieldBookedVH extends RecyclerView.ViewHolder {
        TextView providerFieldText, playtimeText, minimumDpText, bankAccNumber, statusText, datePlayText, timePlayText;
        ImageView imageField;
        Button previewLocBtn, contactProvider, ratingBtn, proofBtn;
        public FieldBookedVH(@NonNull View itemView, RecyclerBooked listenerRecycler) {
            super(itemView);

            providerFieldText = itemView.findViewById(R.id.provider_field_text);
            playtimeText = itemView.findViewById(R.id.playtime_text);
            minimumDpText = itemView.findViewById(R.id.minimum_dp_text);
            bankAccNumber = itemView.findViewById(R.id.bank_acc_number);
            statusText = itemView.findViewById(R.id.status_dp_text);
            imageField = itemView.findViewById(R.id.image_field);
            datePlayText = itemView.findViewById(R.id.date_play_text);
            timePlayText = itemView.findViewById(R.id.time_play_text);
            contactProvider = itemView.findViewById(R.id.contact_provider);
            previewLocBtn = itemView.findViewById(R.id.preview_location_btn);
            ratingBtn = itemView.findViewById(R.id.rating_btn);
            proofBtn = itemView.findViewById(R.id.proof_btn);

            proofBtn.setOnClickListener(view -> {
                if (listenerRecycler != null && getLayoutPosition() != RecyclerView.NO_POSITION){
                    listenerRecycler.recyclerBooked(proofBtn, getLayoutPosition());

                }
            });
        }
    }

    private void setProviderField(String keyProviderField, FieldBookedVH holder) {
        ReferenceDatabase.referenceProviderField.child(keyProviderField).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProviderField modelProviderField = task.getResult().getValue(ModelProviderField.class);
                assert modelProviderField != null;
                holder.providerFieldText.setText(Functions.capitalizeWord(modelProviderField.getName()));
                Picasso.get().load(modelProviderField.getUrlPhotoField()).into(holder.imageField);

                // contact provider
                holder.contactProvider.setOnClickListener(view -> {
                    String url = "https://api.whatsapp.com/send?phone=" + "+62" + modelProviderField.getNumberPhone();

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    context.startActivity(intent);
                });

                // preview location
                holder.previewLocBtn.setOnClickListener(view ->
                        Functions.previewStreetMaps(modelProviderField.getLatitude(), modelProviderField.getLongitude(), context));

                // set bank account with dialog popup
                dialogBankAccount = new Dialog(context);
                dialogBankAccount.setContentView(R.layout.layout_view_bank_account);
                dialogBankAccount.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.background_center_dialog));

                dialogBankAccount.getWindow().setLayout(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                dialogBankAccount.setCancelable(false); //Optional
                dialogBankAccount.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

                // button customize
                Button close;
                close = dialogBankAccount.findViewById(R.id.btn_okay);
                close.setText("Close");


                // text customize
                TextView messageText = dialogBankAccount.findViewById(R.id.textView2);
                messageText.setText(modelProviderField.getBankAccountNumber());
                close.setOnClickListener(v -> dialogBankAccount.dismiss());

            }else {
                Toast.makeText(context, "Error : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setBottomSheetDialog(ModelBooked modelBooked, int pos){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View viewBottomDialog = layoutInflater.inflate(R.layout.layout_dialog_review_field, null);

        Button reviewBtn = viewBottomDialog.findViewById(R.id.review_btn);
        RatingBar ratingBar = viewBottomDialog.findViewById(R.id.rating_bar_field);
        TextView commentReview = viewBottomDialog.findViewById(R.id.comment_review);

        reviewBtn.setOnClickListener(view -> {

            double rating = ratingBar.getRating();
            String comment = commentReview.getText().toString();

            if (rating <= 0){
                Toast.makeText(context, "Rating masih kosong", Toast.LENGTH_SHORT).show();
            }else if (comment.isEmpty()){
                Toast.makeText(context, "Komentar tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }else {
                createReview(modelBooked, rating, comment, pos);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void createReview(ModelBooked modelBooked, double rating, String comment, int pos){

        ModelReviewProvider modelReviewProvider = new ModelReviewProvider(
                rating,
                comment,
                modelBooked.getKeyUserBooked(),
                modelBooked.getKeyBooked()
        );

        ReferenceDatabase.referenceReview.child(modelBooked.getKeyProviderField()).push().setValue(modelReviewProvider)
                .addOnSuccessListener(unused -> {

                    // create and send notification
                    ReferenceDatabase.referenceTokenNotification.child(modelBooked.getKeyProviderField()).child("token").get()
                            .addOnCompleteListener(this::createNotification);

                    saveNotification(title, msg, modelBooked.getKeyProviderField());

                    // change status booked
                    updateStatusBooked(modelBooked, 303, pos);

                }).addOnFailureListener(e -> Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateStatusBooked(ModelBooked modelBooked, int status,  int position) {
        modelBooked.setStatus(status);
        ReferenceDatabase.referenceBooked.child(modelBooked.getKeyBooked()).setValue(modelBooked).addOnSuccessListener(unused -> {
            listBooked.get(position).setStatus(status);
            this.notifyItemChanged(position);
            countRatingProviderField(modelBooked.getKeyProviderField());
        }).addOnFailureListener(e -> Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    double totalRating = 0;
    long totalReview;
    private void countRatingProviderField(String keyProviderField){
        ReferenceDatabase.referenceReview.child(keyProviderField).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalReview = snapshot.getChildrenCount();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelReviewProvider modelReviewProvider = ds.getValue(ModelReviewProvider.class);
                    if (modelReviewProvider != null){
                        totalRating += modelReviewProvider.getRating();
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> updateRatingProvider(keyProviderField), 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRatingProvider(String keyProviderField){
        ReferenceDatabase.referenceProviderField.child(keyProviderField).child("rating").setValue(totalRating / totalReview)
                .addOnFailureListener(e -> Toast.makeText(context, "Error Update Rating Provider : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveNotification(String header, String msg, String keyReceiver){
        ModelNotification modelNotification = new ModelNotification(
                header,
                msg
        );
        ReferenceDatabase.referenceNotification.child(keyReceiver).push().setValue(modelNotification);
    }

    private void createNotification(Task<DataSnapshot> task){
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

}
