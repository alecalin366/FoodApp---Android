package com.example.food.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.food.Likes.LikesFragment;
import com.example.food.Login.LoginActivity;
import com.example.food.Onboarding.WelcomeActivity;
import com.example.food.Profile.ProfileFragment;
import com.example.food.R;
import com.example.food.Recipe.RecipeFragment;
import com.example.food.Search.SearchFragment;
import com.example.food.ShopActivity.ShopFragment;
import com.example.food.Utils.UniversalImageLoader;
import com.example.food.Workout.WorkoutFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 0; //pentru a stii mai jos care dintre item sa le highligh-uiasca cand dam click pe ele
    private ChipNavigationBar chipNavigationBar;
    private Fragment fragment = null;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chipNavigationBar = (ChipNavigationBar) findViewById(R.id.navBar);

        SharedPreferences prefs = getSharedPreferences("Food", MODE_PRIVATE);

        boolean firstInstall  = prefs.getBoolean("FirstInstall", true);
        if(!firstInstall){
            setupFirebaseAuth();
            initImageLoader();
            setupNavBar();
        }
        else {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }



    public void setupNavBar(){
        chipNavigationBar = findViewById(R.id.navBar);

        chipNavigationBar.setItemSelected(R.id.ic_home, true);
        HomeFragment frag = new HomeFragment();
        FragmentTransaction transaction = MainActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, frag);
        transaction.commit();


        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                switch (i) {
                    case R.id.ic_home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.ic_recipe:
                        fragment = new RecipeFragment();
                        break;
                    case R.id.ic_circle:
                        fragment = new ShopFragment();
                        break;
                    case R.id.ic_alert:
                        fragment = new LikesFragment();
                        break;
                    case R.id.ic_workout:
                        fragment = new WorkoutFragment();
                        break;
                }

                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                }
            }
        });
    }

    /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * checks to see if the @param 'user' is logged in
     * @param user
     */
    private void checkCurrentUser(FirebaseUser user){

        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if(user == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in
                checkCurrentUser(user);

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
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}