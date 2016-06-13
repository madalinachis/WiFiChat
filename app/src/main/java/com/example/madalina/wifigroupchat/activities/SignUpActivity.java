package com.example.madalina.wifigroupchat.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.madalina.wifigroupchat.R;
import com.example.madalina.wifigroupchat.fragments.SignUpFragment;

/**
 * Activity which displays a login screen to the user.
 */
public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.register_container, new SignUpFragment())
                    .commit();
        }
    }
}

