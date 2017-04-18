package com.byteshaft.carpool.accounts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.byteshaft.carpool.R;
import com.byteshaft.carpool.utils.AppGlobals;

/**
 * Created by s9iper1 on 4/16/17.
 */

public class AccountTypeActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatButton driverButton;
    private AppCompatButton userButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_type);
        driverButton = (AppCompatButton) findViewById(R.id.driver);
        userButton = (AppCompatButton) findViewById(R.id.user);
        driverButton.setOnClickListener(this);
        userButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.driver:
                AppGlobals.sUserType = AppGlobals.DRIVER;
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                break;
            case R.id.user:
                AppGlobals.sUserType = AppGlobals.USER;
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                break;
        }
    }
}
