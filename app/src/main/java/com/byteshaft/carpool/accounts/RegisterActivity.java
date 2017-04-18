package com.byteshaft.carpool.accounts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.byteshaft.carpool.IntroScreen;
import com.byteshaft.carpool.MainActivity;
import com.byteshaft.carpool.R;
import com.byteshaft.carpool.gettersetter.UserDetails;
import com.byteshaft.carpool.utils.AppGlobals;
import com.byteshaft.carpool.utils.Helpers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, username;
    private AppCompatButton btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (IntroScreen.getInstance() != null) {
            IntroScreen.getInstance().finish();
        }
        ref = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/");
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        btnSignUp = (AppCompatButton) findViewById(R.id.sign_up_button);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        username = (EditText) findViewById(R.id.username);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String user = username.getText().toString();


                if (TextUtils.isEmpty(user)) {
                    Helpers.showSnackBar(findViewById(android.R.id.content), "Enter username!");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Helpers.showSnackBar(findViewById(android.R.id.content), "Enter email address!");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Helpers.showSnackBar(findViewById(android.R.id.content), "Enter password!");
                    return;
                }

                if (password.length() < 6) {
                    Helpers.showSnackBar(findViewById(android.R.id.content), "Password too short, enter minimum 6 characters!");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Helpers.alertDialog(RegisterActivity.this, "Failed", task.getException().getMessage());
                                } else {
                                    FirebaseUser user = task.getResult().getUser();
                                    if (user != null) {
                                        // User is signed in
                                        UserDetails userDetails = new UserDetails();
                                        userDetails.setUserName(username.getText().toString());
                                        userDetails.setUserType(AppGlobals.sUserType);
                                        ref.child("users").child(user.getUid()).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.i("TAG", String.valueOf(task.isSuccessful()));
                                                if (task.isSuccessful()) {
                                                    AppGlobals.saveStringToSP(AppGlobals.KEY_USER_TYPE, AppGlobals.sUserType);
                                                    AppGlobals.saveStringToSP(AppGlobals.KEY_USERNAME, username.getText().toString());
                                                }
                                            }
                                        });
                                    }
                                    AppGlobals.login(true);
                                    Helpers.showSnackBar(findViewById(android.R.id.content), "Account Created Successfully");
                                    new android.os.Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        }
                                    }, 1000);
                                }
                            }
                        });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

