package com.example.madalina.wifigroupchat.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.madalina.wifigroupchat.fragments.LoginFragment;
import com.example.madalina.wifigroupchat.R;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.login_container, new LoginFragment())
                    .commit();
        }
    }
}
