package com.dream.lemon.hackathon.ui.Map;

import com.dream.lemon.hackathon.ui.welcome.WelcomeContract;

/**
 * Created by ninja on 23/10/2017.
 */

public class MapPresenter implements MapContract.Presenter {

    private final MapContract.View view;

    public MapPresenter(MapContract.View view) {
        if (view != null) {
            this.view = view;
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public void start() {
        view.setPresenter(this);
    }
}
