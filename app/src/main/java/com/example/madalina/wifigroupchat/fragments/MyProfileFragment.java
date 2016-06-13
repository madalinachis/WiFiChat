package com.example.madalina.wifigroupchat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import com.example.madalina.wifigroupchat.R;
import com.example.madalina.wifigroupchat.dependencies.Injector;
import com.example.madalina.wifigroupchat.network.UserApis;

/**
 * Created by Madalina on 5/12/2016.
 */
public class MyProfileFragment extends BaseFragment {

    UserApis userApis;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_myprofile, container, false);
        ButterKnife.bind(this, view);
        Injector.init();
        userApis = Injector.getApi(UserApis.class);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
