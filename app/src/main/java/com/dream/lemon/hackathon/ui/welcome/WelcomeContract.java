package com.dream.lemon.hackathon.ui.welcome;

import com.dream.lemon.hackathon.arch.BasePresenter;
import com.dream.lemon.hackathon.arch.BaseView;

public interface WelcomeContract {

    interface View extends BaseView<Presenter> {
        void presentNextModule();
    }

    interface Presenter extends BasePresenter {
        void continueButtonTapped();
    }
}
