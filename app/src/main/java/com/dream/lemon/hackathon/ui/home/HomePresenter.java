package com.dream.lemon.hackathon.ui.home;

/**
 * Created by ninja on 23/10/2017.
 */

public class HomePresenter implements HomeContract.Presenter {

    private final HomeContract.View view;

    public HomePresenter(HomeContract.View view) {
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
