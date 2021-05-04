package com.example.food.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food.R;
import com.example.food.Recipe.Recipe;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecipeRecyclerViewAdapter extends FirestoreRecyclerAdapter<Recipe, RecipeRecyclerViewAdapter.RecipeViewHolder>{

    private Context mContext;
    public RecipeRecyclerViewAdapter(Context mContext, @NonNull FirestoreRecyclerOptions<Recipe> options) {
        super(options);
        this.mContext = mContext;;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_cardview, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position, @NonNull Recipe model) {
           holder.titlu_recipe.setText(model.getName());

        Glide.with(holder.recipe_photo.getContext())
                .load(model.getPhoto())
                .into(holder.recipe_photo);

        holder.recipe_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, model.getPhoto(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    public class RecipeViewHolder extends RecyclerView.ViewHolder{
        private ImageView recipe_photo;
        private TextView titlu_recipe;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipe_photo = itemView.findViewById(R.id.recipe_photo);
            titlu_recipe = itemView.findViewById(R.id.titlu_recipe);
        }
    }

}
