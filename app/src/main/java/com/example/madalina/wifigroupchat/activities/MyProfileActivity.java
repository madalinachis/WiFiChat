package com.example.madalina.wifigroupchat.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.madalina.wifigroupchat.R;
import com.example.madalina.wifigroupchat.fragments.MyProfileFragment;

/**
 * Created by Madalina on 5/12/2016.
 */
public class MyProfileActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.myprofile_container, new MyProfileFragment())
                    .commit();
        }
    }
}
