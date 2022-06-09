package com.ark.futsalbookedapps.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelGallery;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.Views.ProviderField.UpdateGallery;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;


public class AdapterDashboard extends RecyclerView.Adapter<AdapterDashboard.DashboardVH> {

    private final Context context;
    private List<ModelGallery> listGallery = new ArrayList<>();

    public AdapterDashboard(Context context) {
        this.context = context;
    }

    public void setItem(List<ModelGallery> listGallery){
        this.listGallery = listGallery;
    }


    @NonNull
    @Override
    public DashboardVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_grid_field, parent, false);
        return new DashboardVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardVH holder, int position) {
        ModelGallery modelGallery = listGallery.get(position);
        Picasso.get().load(modelGallery.getImageGalleryUrl()).into(holder.imageField);


        holder.cardEdit.setOnClickListener(view -> {
            Intent intent = new Intent(context, UpdateGallery.class);
            intent.putExtra("keyGallery", modelGallery.getKeyGallery());
            context.startActivity(intent);
        });

        holder.cardDelete.setOnClickListener(view -> {
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
            headerText.setText("Delete Data");
            messageText.setText("Are you sure delete this data ?");

            dialog.show();

            cancel.setOnClickListener(v -> dialog.dismiss());

            okay.setOnClickListener(v -> {
                deleteImageField(modelGallery.getKeyGallery(), modelGallery.getImageGalleryUrl(), position);
                dialog.dismiss();
            });
        });
    }

    @Override
    public int getItemCount() {
        return listGallery.size();
    }

    public static class DashboardVH extends RecyclerView.ViewHolder {
        ImageView imageField;
        TextView typeField;
        CardView cardEdit, cardDelete;
        public DashboardVH(@NonNull View itemView) {
            super(itemView);
            imageField = itemView.findViewById(R.id.image_field);
            typeField = itemView.findViewById(R.id.type_field);
            cardEdit = itemView.findViewById(R.id.card_edit);
            cardDelete = itemView.findViewById(R.id.card_delete);
        }
    }

    private void deleteImageField(String keyField, String urlImage, int pos){
        // init storage firebase
        FirebaseStorage referenceStorage = FirebaseStorage.getInstance();

        String name_photo = referenceStorage.getReferenceFromUrl(urlImage).getName();
        StorageReference deleteRef = referenceStorage.getReference("field/"+name_photo);

        deleteRef.delete()
                .addOnFailureListener(e -> Toast.makeText(context, "Delete data failed", Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(unused -> deleteGalleryData(keyField, pos));
    }

    private void deleteGalleryData(String keyField, int pos){
        ReferenceDatabase.referenceGallery.child(Data.uid).child(keyField).removeValue()
                .addOnSuccessListener(unused -> {
                    listGallery.remove(pos);
                    this.notifyItemRemoved(pos);
                    Toast.makeText(context, "Success delete data", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
