package com.byteshaft.carpool.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.byteshaft.carpool.IntroScreen;
import com.byteshaft.carpool.R;

/**
 * Created by s9iper1 on 4/16/17.
 */

public class CreateAccountActivity extends AppCompatActivity {

    private AppCompatButton createAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (IntroScreen.getInstance() != null) {
            IntroScreen.getInstance().finish();
        }
        setContentView(R.layout.activity_create_account);
        createAccount = (AppCompatButton) findViewById(R.id.create_an_account);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AccountTypeActivity.class));
            }
        });
    }
}
