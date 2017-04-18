package com.byteshaft.carpool;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.byteshaft.carpool.accounts.LoginActivity;
import com.byteshaft.carpool.fragments.ActivityFragment;
import com.byteshaft.carpool.fragments.History;
import com.byteshaft.carpool.fragments.Home;
import com.byteshaft.carpool.fragments.User;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (IntroScreen.getInstance() != null) {
            IntroScreen.getInstance().finish();
        }
        if (LoginActivity.getInstance() != null) {
            LoginActivity.getInstance().finish();
        }
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(new Home());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(new Home());
                    return true;
                case R.id.navigation_activity:
                    loadFragment(new ActivityFragment());
                    return true;
                case R.id.navigation_history:
                    loadFragment(new History());
                    return true;
                case R.id.navigation_user:
                    loadFragment(new User());
                    return true;
            }
            return false;
        }

    };


    public void loadFragment(Fragment fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.content, fragment);
        tx.commit();
    }

}
