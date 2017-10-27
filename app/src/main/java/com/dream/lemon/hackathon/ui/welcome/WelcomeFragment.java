package com.dream.lemon.hackathon.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

        return null;
    }

    @Override
    public void presentNextModule() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        getActivity().startActivity(intent);
    }
}
