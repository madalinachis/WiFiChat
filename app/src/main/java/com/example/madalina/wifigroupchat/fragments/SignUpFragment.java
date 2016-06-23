package com.example.madalina.wifigroupchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.ButterKnife;

import com.example.madalina.wifigroupchat.R;
import com.example.madalina.wifigroupchat.activities.LoginActivity;
import com.example.madalina.wifigroupchat.dependencies.Injector;
import com.example.madalina.wifigroupchat.model.User;
import com.example.madalina.wifigroupchat.network.ErrorHandler;
import com.example.madalina.wifigroupchat.network.UserApis;
import com.example.madalina.wifigroupchat.utils.GetGpsLocation;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Madalina on 5/6/2016.
 */
public class SignUpFragment extends BaseFragment {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText passwordAgainEditText;
    private EditText nameEditText;
    private Spinner hobbySpinner;

    UserApis userApis;
    GetGpsLocation gpsLocation;
    double latitude;
    double longitude;
    String hobby;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);
        ButterKnife.bind(this, view);
        Injector.init();
        userApis = Injector.getApi(UserApis.class);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usernameEditText = (EditText) view.findViewById(R.id.username_edit_text);
        nameEditText = (EditText) view.findViewById(R.id.name_edit_text);
        hobbySpinner = (Spinner) view.findViewById(R.id.hobby_edit_text);
        passwordEditText = (EditText) view.findViewById(R.id.password_edit_text);
        passwordAgainEditText = (EditText) view.findViewById(R.id.password_again_edit_text);
        Button mActionButton = (Button) view.findViewById(R.id.action_button);
        initializeReasonsNotDoneSpinner();
        mActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                signup();
            }
        });
    }

    private void signup() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String passwordAgain = passwordAgainEditText.getText().toString().trim();

        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));
        if (username.length() == 0) {
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_username));
        }
        if (password.length() == 0) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_password));
        }
        if (!password.equals(passwordAgain)) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_mismatched_passwords));
        }
        validationErrorMessage.append(getString(R.string.error_end));

        if (validationError) {
            Toast.makeText(getActivity(), validationErrorMessage.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();

        gpsLocation = new GetGpsLocation(getActivity());
        if (gpsLocation.canGetLocation()) {
            latitude = gpsLocation.getLatitude();
            longitude = gpsLocation.getLongitude();
        } else {
            gpsLocation.showSettingsAlert();
        }

        User user = User.create()
                .hobby(hobby)
                .latitude(latitude)
                .longitude(longitude)
                .mac(macAddress)
                .name(nameEditText.getText().toString())
                .username(username)
                .password(password);

        runCall(userApis.register(user)).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    ErrorHandler.showError(getActivity(), response);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                ErrorHandler.showError(getActivity(), t);
            }
        });
    }

    private void initializeReasonsNotDoneSpinner() {
        ArrayAdapter<CharSequence> reasonNotDoneAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.hobbies, android.R.layout.simple_spinner_item);
        reasonNotDoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hobbySpinner.setAdapter(reasonNotDoneAdapter);
        hobbySpinner.setOnItemSelectedListener(new ReasonNotDoneItemSelector());
    }

    private class ReasonNotDoneItemSelector implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            hobby = hobbySpinner.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

}
