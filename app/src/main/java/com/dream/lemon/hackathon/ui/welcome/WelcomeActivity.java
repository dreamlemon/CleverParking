package com.dream.lemon.hackathon.ui.welcome;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.dream.lemon.hackathon.R;
import com.dream.lemon.hackathon.pojosJSON.ParkingJSON;
import com.dream.lemon.hackathon.utils.ActivityUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

public class WelcomeActivity extends AppCompatActivity {

    public static ParkingJSON parkingJSONS;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.root_container);

        WelcomeFragment fragment = WelcomeFragment.newInstance();
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment,
                R.id.contentFrame);

        new WelcomePresenter(fragment).start();

        Gson gson = new Gson();
        parkingJSONS = gson.fromJson(loadJSONFromAsset(), ParkingJSON.class);
    }

    public String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getAssets().open("jsonOpen.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            return null;
        }
        return json;
    }
}
