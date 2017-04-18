package com.byteshaft.carpool.accounts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by s9iper1 on 4/16/17.
 */

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnLogin;
    private static LoginActivity sInstance;
    private TextView resetPassword;
    private DatabaseReference ref;

    public static LoginActivity getInstance() {
        return sInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (IntroScreen.getInstance() != null) {
            IntroScreen.getInstance().finish();
        }
        sInstance = this;
        auth = FirebaseAuth.getInstance();
        // set the view now
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnLogin = (Button) findViewById(R.id.btn_login);
        resetPassword = (TextView) findViewById(R.id.reset_password);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
            }
        });

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    progressBar.setVisibility(View.GONE);
                                    // there was an error
                                    if (password.length() < 6) {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    } else {

                                        Helpers.alertDialog(LoginActivity.this, "Failed", task.getException().getMessage());
                                    }
                                } else {
                                    ref = FirebaseDatabase.getInstance().
                                            getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/")
                                            .child("users").child(task.getResult().getUser().getUid());
                                    Helpers.showSnackBar(findViewById(android.R.id.content), "User loggedIn");
                                    AppGlobals.login(true);
                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot) {
                                            progressBar.setVisibility(View.GONE);
                                            ref.removeEventListener(this);
                                            //Getting the data from snapshot
                                            UserDetails detail = snapshot.getValue(UserDetails.class);
                                            Log.i("TAG", detail.getUserType());
                                            AppGlobals.saveStringToSP(AppGlobals.KEY_USERNAME, detail.getUserName());
                                            AppGlobals.saveStringToSP(AppGlobals.KEY_NAME, detail.getName());
                                            AppGlobals.saveStringToSP(AppGlobals.KEY_USER_TYPE, detail.getUserType());
                                            AppGlobals.saveStringToSP(AppGlobals.KEY_PHONE_NUMBER, detail.getPhoneNumber());
                                            AppGlobals.saveStringToSP(AppGlobals.KEY_ENCODED_IMAGE, detail.getPhoto());
                                            AppGlobals.saveStringToSP(AppGlobals.KEY_LOCAL_IMAGE_URI,
                                                    detail.getLocalImageUri());

                                            new android.os.Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(intent);
                                                }
                                            }, 500);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.e("TAG", databaseError.getMessage());
                                            progressBar.setVisibility(View.GONE);

                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }
}
