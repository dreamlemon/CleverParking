package com.dream.lemon.hackathon.ui.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dream.lemon.hackathon.R;
import com.dream.lemon.hackathon.data.PlaceRecord;
import com.dream.lemon.hackathon.pojosJSON.Binding;
import com.dream.lemon.hackathon.pojosJSON.ParkingJSON;
import com.dream.lemon.hackathon.ui.adapter.TempAdapter;
import com.dream.lemon.hackathon.ui.welcome.WelcomeActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.geometry.Point;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity implements HomeContract.View, OnMapReadyCallback {

    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 96;
    private static final int DEFAULT_ZOOM = 15;

    private HomeContract.Presenter presenter;
    private RecyclerView.LayoutManager lManager;

    private Realm realm;

    private GeoDataClient geoDataClient;
    private PlaceDetectionClient placeDetectionClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Location lastKnownLocation;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    @BindView(R.id.btn_where_to)        Button whereToButtonView;
    @BindView(R.id.lyt_recent_search)   LinearLayout recentSearchLayoutView;
    @BindView(R.id.tempList)            RecyclerView recyclerView;
    @BindView(R.id.button_nearby)       Button nearbyButtonView;

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
        Realm.init(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);
        ButterKnife.bind(this);

        // Configure instance of Realm DB
        realm = Realm.getDefaultInstance();

        // Map configuration
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geoDataClient = Places.getGeoDataClient(this, null);

        placeDetectionClient = Places.getPlaceDetectionClient(this, null);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Configure recycler view
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager lManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lManager);

        // Configure recent places searched
        final RealmResults<PlaceRecord> realmResults = realm.where(PlaceRecord.class).findAll();
        List<PlaceRecord> items = realm.copyFromRealm(realmResults);
        configureList(items);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://opendata.caceres.es/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
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

    private void getDeviceLocation() {
        try {
            Task locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        lastKnownLocation = (Location) task.getResult();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        } catch(SecurityException e)  {

        }
    }

    private void setMarkerOnLocation(Location location) {
        map.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("Park"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            whereToButtonView.setText(place.getAddress());
            whereToButtonView.setAllCaps(false);

            recentSearchLayoutView.setVisibility(View.GONE);

            realm.beginTransaction();
            PlaceRecord placeRecord = new PlaceRecord(place.getAddress().toString(), place.getName().toString(),
                    place.getLatLng().toString(), null);
            realm.copyToRealm(placeRecord);
        }
    }

    private void configureList(List<PlaceRecord> items) {
        RecyclerView.Adapter adapter = new TempAdapter(items,
                new TempAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(PlaceRecord item) {
                        whereToButtonView.setText(item.getAddress());
                        recentSearchLayoutView.setVisibility(View.GONE);
                    }
                });
        recyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.btn_where_to)
    public void onClickButtonWhere() {
        try {
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.button_nearby)
    public void onClickButtonNearby() {
        updateLocationUI();
        getDeviceLocation();

        recentSearchLayoutView.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_back)
    public void onClickButtonBack() {
        recentSearchLayoutView.setVisibility(View.VISIBLE);

        whereToButtonView.setText(R.string.where_to);
        whereToButtonView.setAllCaps(false);

        RealmResults<PlaceRecord> realmResults = realm.where(PlaceRecord.class).findAll();
        List<PlaceRecord> items = realm.copyFromRealm(realmResults);
        configureList(items);
    }

    private void getNearParkins(double lat, double lon) {
        ArrayList<LatLng> latLongList = new ArrayList<>();
        for (Binding parking : WelcomeActivity.parkingJSONS.getBindings()) {
            LatLng latLng = new LatLng(Double.parseDouble(parking.getGeoLat().getValue()),
                    Double.parseDouble(parking.getGeoLong().getValue()));
            latLongList.add(latLng);
        }


    }
}
