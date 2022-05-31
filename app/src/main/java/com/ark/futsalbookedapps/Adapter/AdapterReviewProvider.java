package com.ark.futsalbookedapps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.Models.ModelAccount;
import com.ark.futsalbookedapps.Models.ModelReviewProvider;
import com.ark.futsalbookedapps.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterReviewProvider extends RecyclerView.Adapter<AdapterReviewProvider.ReviewProviderVH> {

    private Context context;
    private List<ModelReviewProvider> listReviewProvider = new ArrayList<>();

    public AdapterReviewProvider(Context context) {
        this.context = context;
    }

    public void setItem(List<ModelReviewProvider> listReviewProvider){
        this.listReviewProvider = listReviewProvider;
    }

    @NonNull
    @Override
    public ReviewProviderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_review_provider, parent, false);
        return new ReviewProviderVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewProviderVH holder, int position) {
        ModelReviewProvider modelReviewProvider = listReviewProvider.get(position);

        holder.commentText.setText(modelReviewProvider.getComment());
        holder.rateStarText.setText(String.valueOf(modelReviewProvider.getRating()));

        // set user data
        setUserData(modelReviewProvider.getKeyUser(), holder);
    }

    @Override
    public int getItemCount() {
        return listReviewProvider.size();
    }

    public static class ReviewProviderVH extends RecyclerView.ViewHolder {
        TextView usernameText, commentText, rateStarText;
        public ReviewProviderVH(@NonNull View itemView) {
            super(itemView);
            usernameText  = itemView.findViewById(R.id.username_text);
            commentText = itemView.findViewById(R.id.comment_user);
            rateStarText = itemView.findViewById(R.id.rate_star_review);
        }
    }

    private void setUserData(String keyUser,ReviewProviderVH holder){
        ReferenceDatabase.referenceAccount.child(keyUser).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelAccount modelAccount = task.getResult().getValue(ModelAccount.class);
                if (modelAccount != null){
                    holder.usernameText.setText(modelAccount.getUsername());
                }
            }else {
                Toast.makeText(context, "Error : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
