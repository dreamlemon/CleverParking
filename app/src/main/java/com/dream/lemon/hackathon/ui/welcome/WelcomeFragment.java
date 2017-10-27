package com.dream.lemon.hackathon.ui.welcome;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dream.lemon.hackathon.R;
import com.dream.lemon.hackathon.ui.home.HomeActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeFragment extends Fragment implements WelcomeContract.View {

    private WelcomeContract.Presenter presenter;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    boolean locationPermissionGranted;
    @BindView(R.id.btn_continue) Button continueButtonView;

    public static WelcomeFragment newInstance() {
        return new WelcomeFragment();
    }

    @Override
    public void setPresenter(WelcomeContract.Presenter presenter) {
        if (presenter != null) {
            this.presenter = presenter;
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
        ButterKnife.bind(this, rootView);

        continueButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.continueButtonTapped();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLocationPermission();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        //updateLocationUI();
    }

    @Override
    public void presentNextModule() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        getActivity().startActivity(intent);
    }
}
