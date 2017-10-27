package com.dream.lemon.hackathon;

import android.app.Application;

import io.realm.Realm;

public class OpenApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}