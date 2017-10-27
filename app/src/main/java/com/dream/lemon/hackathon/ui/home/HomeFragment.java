package com.dream.lemon.hackathon.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class HomeFragment extends Fragment implements HomeContract.View, OnMapReadyCallback {

    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 96;
    private static final int DEFAULT_ZOOM = 15;

    private HomeContract.Presenter presenter;

    Realm realm;
    List items;
    GeoDataClient geoDataClient;
    PlaceDetectionClient placeDetectionClient;
    FusedLocationProviderClient fusedLocationProviderClient;
    SupportMapFragment mapFragment;
    GoogleMap map;
    private Location lastKnownLocation;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    @BindView(R.id.btn_where_to) Button whereToButtonView;
    @BindView(R.id.lyt_recent_search) LinearLayout recentSearchLayoutView;
    @BindView(R.id.tempList) RecyclerView recyclerView;
    @BindView(R.id.button_nearby) Button nearbyButtonView;

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

        realm = Realm.getDefaultInstance();

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager lManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lManager);

        RealmResults<PlaceRecord> realmResults = realm.where(PlaceRecord.class).findAll();
        List<PlaceRecord> items = realm.copyFromRealm(realmResults);
        if (items.size() > 6) {
            items = items.subList(0, 6);
        }
        configureList(items);

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
        realm.commitTransaction();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        GeoDataClient geoDataClient = Places.getGeoDataClient(getActivity(), null);
        PlaceDetectionClient placeDetectionClient = Places.getPlaceDetectionClient(getActivity(), null);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        updateLocationUI();
        getDeviceLocation();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);

        } catch (SecurityException e)  {
        }
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

        whereToButtonView.setText(getString(R.string.where_to));
        whereToButtonView.setAllCaps(false);

        RealmResults<PlaceRecord> realmResults = realm.where(PlaceRecord.class).findAll();
        List<PlaceRecord> items = realm.copyFromRealm(realmResults);
        if (items.size() > 6) {
            items = items.subList(0, 6);
        }
        configureList(items);
    }
}
