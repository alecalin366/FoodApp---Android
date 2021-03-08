package com.example.food.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.food.R;
import com.example.food.Utils.FirebaseMethods;
import com.example.food.Utils.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private String email,password, username;
    private EditText mEmail, mPassword, mUsername;
    private ProgressBar mProgressBar;
    private AppCompatButton btnRegister;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private String append = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseMethods = new FirebaseMethods(RegisterActivity.this);
        Log.d(TAG, "onCreate: started");

        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                if(checkInputs(email, username, password)){
                    mProgressBar.setVisibility(View.VISIBLE);

                    firebaseMethods.registerNewEmail(email, password, username);
                }
            }
        });
    }

    private boolean checkInputs(String email, String username, String password){
        Log.d(TAG, "checkkInputs: checking inputs for null value");
        if(email.equals("") || username.equals("") || password.equals("")){
            Toast.makeText(RegisterActivity.this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initWidgets(){
        Log.d(TAG, "initWidgets: initializing widgets");
        mEmail = (EditText) findViewById(R.id.email_field);
        mPassword = (EditText) findViewById(R.id.password_field);
        mUsername = (EditText) findViewById(R.id.name_field);
        btnRegister = (AppCompatButton) findViewById(R.id.btnRegister);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.GONE);
    }

    private boolean isStringNull(String string){
        Log.d(TAG, "isStringNull: checking string if null");
        if(string.equals("")){
            return true;
        }
        else{
            return false;
        }
    }

     /*
    ----------------------------Firebase------------------------------------------
     */
//
//    /**
//     * Check is @param username already exists in the database
//     * @param username
//     */
    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: Checking if  " + username + " already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if (singleSnapshot.exists()){
                        Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());
                        append = myRef.push().getKey().substring(3,10);
                        Log.d(TAG, "onDataChange: username already exists.Appending random string to name: " + append);
                    }
                }
                String mUsername = "";
                mUsername = username + append;

                //add new user to the database
                firebaseMethods.addNewUser(email, mUsername, "", "", "" );

                Toast.makeText(RegisterActivity.this, "Signup succesful.Sending verification email", Toast.LENGTH_SHORT).show();

                mAuth.signOut();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    //User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed in" + ((FirebaseUser) user).getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            checkIfUsernameExists(username);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    finish();

                } else {
                    //User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}

