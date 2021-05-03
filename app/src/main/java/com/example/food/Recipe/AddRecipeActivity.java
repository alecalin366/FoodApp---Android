package com.example.food.Recipe;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;

import com.example.food.Interfaces.ICompleteListener;
import com.example.food.R;
import com.example.food.SelectPhotos.SelectPhotoActivity;
import com.example.food.Utils.FirebaseMethods;
import com.example.food.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {
    private static final String TAG = "NextActivity";
    private ImageView _save, backArrow, photo;
    private TextView category;
    private EditText mRecipeName, mPreparationTime, mServingSize, mDescription, mCalorii, mProteine, mCarbo, mGrasimi;

    boolean[] selectedCategory;
    ArrayList<Integer> categoryList = new ArrayList<>();
    String[] categoryArray = {"Desert", "Vegan", "Sosuri"};


    LinearLayout layoutList;
    Button buttonAddIngredient;
    List<String> measurementsList = new ArrayList<>();
    ArrayList<Ingredients> ingredientsList = new ArrayList<>();

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    private String mAppend = "file:/";


    @Override
     protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_recipe_layout);
         
        mFirebaseMethods = new FirebaseMethods(this);
         
        FindViews();
        initializeSelectedCategory();
        SetupAddIngredientButton();
        SetupSave();
        SetupChoosePhoto();
        setupBackButton();
        setImage();
        setupFirebaseAuth();
    }

    private void FindViews()
    {
        category = findViewById(R.id.category);
        _save = findViewById(R.id.saveChanges);
        backArrow = findViewById(R.id.backArrow);
        photo = findViewById(R.id.profile_photo);
        mRecipeName = findViewById(R.id.nume_reteta);
        mPreparationTime = findViewById(R.id.timp_preparare);
        mServingSize = findViewById(R.id.nr_portii);
        mDescription = findViewById(R.id.descriere);
        mCalorii = findViewById(R.id.cantitate_calorii);
        mProteine = findViewById(R.id.cantitate_proteine);
        mCarbo = findViewById(R.id.cantitate_carbo);
        mGrasimi = findViewById(R.id.cantitate_grasimi);
        layoutList = findViewById(R.id.layout_list);
        buttonAddIngredient = findViewById(R.id.add_ingredient_button);
        //TODO ale find all objects
    }

    private void SetupAddIngredientButton(){
        buttonAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addView();
            }
        });

        setupSpinner();
    }

    private void setupSpinner(){
        measurementsList.add("gr");
        measurementsList.add("l");
        measurementsList.add("kg");
        measurementsList.add("buc");
    }

    private void addView(){
        View ingredientView = getLayoutInflater().inflate(R.layout.add_row_ingredients, null, false);

        AppCompatSpinner spinnerMeasurements = (AppCompatSpinner) ingredientView.findViewById(R.id.spinner_measurements);
        ImageView removeIngr = (ImageView) ingredientView.findViewById(R.id.image_remove);

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, measurementsList);
        spinnerMeasurements.setAdapter(arrayAdapter);

        removeIngr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(ingredientView);
            }
        });

        layoutList.addView(ingredientView);
    }

    private void removeView(View view){
        layoutList.removeView(view);
    }

    //============================CATEGORY===============================//

    private void initializeSelectedCategory(){
        selectedCategory = new boolean[categoryArray.length];

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAlertDialog();
            }
        });
    }

    private void initAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipeActivity.this);
        builder.setTitle("Alege o categorie");
        builder.setCancelable(false);

        builder.setMultiChoiceItems(categoryArray, selectedCategory, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked){
                    //When checkbox selected, add position in cateogryList
                    categoryList.add(which);
                    Collections.sort(categoryList);
                } else {
                    //When checkbox unselected, remove position in cateogryList
                    categoryList.remove(which);
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuilder stringBuilder = new StringBuilder();
                for( int j=0; j < categoryList.size(); j++){
                    stringBuilder.append(categoryArray[categoryList.get(j)]);
                    if(j != categoryList.size()-1){
                        stringBuilder.append(", ");
                    }
                }
                category.setText(stringBuilder.toString());
            }
        });

        builder.setNegativeButton("Anuleaza", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Sterge tot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for( int j=0; j < selectedCategory.length; j++){
                    selectedCategory[j] = false;
                    categoryList.clear();
                    category.setText("");
                }
            }
        });

        builder.show();

    }
 
     private void SetupSave()
     {
         _save.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 SaveRecipe();
                 finish();
             }
         });
     }

     private void setupBackButton(){
         backArrow.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 finish();
             }
         });
     }

    private void SetupChoosePhoto()
    {
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddRecipeActivity.this, SelectPhotoActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setImage(){
        Intent intent = getIntent();
        ImageView image = (ImageView) findViewById(R.id.profile_photo);
        UniversalImageLoader.setImage(intent.getStringExtra(getString(R.string.selected_image)), image, null, mAppend);
    }

    private boolean checkIngredients() {
        ingredientsList.clear();
        boolean result = true;

        for(int i=0;i<layoutList.getChildCount();i++){

            View ingredientView = layoutList.getChildAt(i);

            EditText numeIngredient = (EditText) ingredientView.findViewById(R.id.nume_ingredient);
            EditText cantitateIngredient = (EditText) ingredientView.findViewById(R.id.cantitate_ingredient);
            AppCompatSpinner spinnerMeasurements = (AppCompatSpinner) ingredientView.findViewById(R.id.spinner_measurements);

            Ingredients ingredients = new Ingredients();

            if(!numeIngredient.getText().toString().equals("")){
                ingredients.setName_ingredient(numeIngredient.getText().toString());
            }else {
                result = false;
                break;
            }

            if(!cantitateIngredient.getText().toString().equals("")){
                ingredients.setQuantity(cantitateIngredient.getText().toString());
            }else {
                result = false;
                break;
            }

            if(spinnerMeasurements.getSelectedItemPosition()!=0){
                ingredients.setMeasurements(measurementsList.get(spinnerMeasurements.getSelectedItemPosition()));
            }else {
                result = false;
                break;
            }

            ingredientsList.add(ingredients);

        }

        if(ingredientsList.size()==0){
            result = false;
            Toast.makeText(this, "Add Ingredients First!", Toast.LENGTH_SHORT).show();
        }else if(!result){
            Toast.makeText(this, "Enter All Details Correctly!", Toast.LENGTH_SHORT).show();
        }


        return result;
    }

     private void SaveRecipe()
     {
         final String recipeName = mRecipeName.getText().toString();
         final String description = mDescription.getText().toString();
         final String preparationTime = mPreparationTime.getText().toString();
         final String servingSize = mServingSize.getText().toString();
         final String calorii = mCalorii.getText().toString();
         final String proteine = mProteine.getText().toString();
         final String carbo = mCarbo.getText().toString();
         final String grasimi = mGrasimi.getText().toString();
         final String category = categoryList.toString();
         final String ingredients = ingredientsList.toString();

         if(!checkIngredients() || ingredients == null || ingredients.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga ingrediente", Toast.LENGTH_SHORT).show();
         }

         if(recipeName == null || recipeName.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga titlu", Toast.LENGTH_SHORT).show();
             return;
         }

         if(description == null || description.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga descriere", Toast.LENGTH_SHORT).show();
             return;
         }

         if(preparationTime == null || preparationTime.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga preparationTime", Toast.LENGTH_SHORT).show();
             return;
         }

         if(servingSize == null || servingSize.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga servingSize", Toast.LENGTH_SHORT).show();
             return;
         }

         if(calorii == null || calorii.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga calorii", Toast.LENGTH_SHORT).show();
             return;
         }

         if(proteine == null || proteine.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga proteine", Toast.LENGTH_SHORT).show();
             return;
         }

         if(carbo == null || carbo.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga carbo", Toast.LENGTH_SHORT).show();
             return;
         }

         if(grasimi == null || grasimi.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga grasimi", Toast.LENGTH_SHORT).show();
             return;
         }

         if(category == null || category.isEmpty()){
             Toast.makeText(getApplicationContext(), "adauga category", Toast.LENGTH_SHORT).show();
             return;
         }
         Macronutrient macro = new Macronutrient(calorii, proteine, carbo, grasimi);
         Recipe testRecipe = new Recipe(recipeName, category, description, preparationTime, servingSize, macro, ingredients);
         mFirebaseMethods.AddRecipe(testRecipe, new ICompleteListener() {
             @Override
             public void OnComplete(boolean isSuccessfulCompleted) {
                 if(isSuccessfulCompleted)
                 {
                     Toast.makeText(getApplicationContext(),"Reteta a fost adaugata cu succes",Toast.LENGTH_LONG).show();
                 }
                 else {
                     Toast.makeText(getApplicationContext(),"Reteta nu a fost adaugata cu succes",Toast.LENGTH_LONG).show();
                 }
             }
         });
     }

      /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();


                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
