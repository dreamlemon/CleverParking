package com.dream.lemon.hackathon.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dream.lemon.hackathon.R;
import com.dream.lemon.hackathon.ui.adapter.TempAdapter;
import com.dream.lemon.hackathon.data.PlaceRecord;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class HomeFragment extends Fragment implements HomeContract.View, OnMapReadyCallback {

    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 96;

    private HomeContract.Presenter presenter;
    private RecyclerView.LayoutManager lManager;

    Realm realm;

    @BindView(R.id.btn_where_to) Button whereToButtonView;
    @BindView(R.id.lyt_recent_search) LinearLayout recentSearchLayoutView;
    @BindView(R.id.tempList) RecyclerView recyclerView;

    @BindView(R.id.btn_where_to)
    Button whereToButtonView;
    @BindView(R.id.button_nearby)
    Button nearbyButtonView;
    @BindView(R.id.lyt_recent_search)
    LinearLayout recentSearchLayoutView;
    @BindView(R.id.tempList)
    RecyclerView recycler;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void setPresenter(HomeContract.Presenter presenter) {
        if (presenter != null) {
            this.presenter = presenter;
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Realm.init(getActivity());
        presenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, rootView);
        //Recycler
        items = new ArrayList();

        realm = Realm.getDefaultInstance();

        whereToButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager lManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lManager);

        try {
            RealmResults<PlaceRecord> realmResults = realm.where(PlaceRecord.class).findAll();
            List<PlaceRecord> items = realm.copyFromRealm(realmResults);
            RecyclerView.Adapter adapter = new TempAdapter(items,
                    new TempAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(PlaceRecord item) {

                }
            });
            recyclerView.setAdapter(adapter);
        } catch (SQLiteException e) {

        }

        return rootView;
    }

    public void onPlaceSelected(Place place) {
        whereToButtonView.setText(place.getAddress());
        whereToButtonView.setAllCaps(false);

        recentSearchLayoutView.setVisibility(View.GONE);

        realm.beginTransaction();
        PlaceRecord placeRecord = new PlaceRecord(place.getAddress().toString(), place.getName().toString(),
                place.getLatLng().toString(), null);
        realm.copyToRealm(placeRecord);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
    }

    @OnClick(R.id.btn_where_to)
    public void onClickButtonWhere() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(getActivity());
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.button_nearby)
    public void onClickButtonNearby() {
        recentSearchLayoutView.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_back)
    public void onClickButtonBack() {
        recentSearchLayoutView.setVisibility(View.VISIBLE);
    }

}
