package com.dream.lemon.hackathon.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.dream.lemon.hackathon.R;
import com.dream.lemon.hackathon.utils.ActivityUtils;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.root_container);

        HomeFragment fragment = HomeFragment.newInstance();
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment,
                R.id.contentFrame);

        new HomePresenter(fragment).start();
    }
}
