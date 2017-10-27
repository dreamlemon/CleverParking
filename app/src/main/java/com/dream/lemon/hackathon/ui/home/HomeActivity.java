package com.dream.lemon.hackathon.ui.home;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.dream.lemon.hackathon.R;
import com.dream.lemon.hackathon.data.PlaceRecord;
import com.dream.lemon.hackathon.pojosJSON.Binding;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity implements HomeContract.View, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 96;
    private static final int DEFAULT_ZOOM = 15;

    private HomeContract.Presenter presenter;
    private RecyclerView.LayoutManager lManager;
    private DatabaseReference database;
    private Realm realm;
    private String selectedPlaceID;

    private LatLng selectedParking;

    private GeoDataClient geoDataClient;
    private PlaceDetectionClient placeDetectionClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Location lastKnownLocation;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    private List<LatLng> nearestResults;

    @BindView(R.id.btn_where_to)        Button whereToButtonView;
    @BindView(R.id.lyt_recent_search)   LinearLayout recentSearchLayoutView;
    @BindView(R.id.tempList)            RecyclerView recyclerView;
    @BindView(R.id.button_nearby)       LinearLayout nearbyButtonView;

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

        //Firebase
        database = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
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

                        nearestResults = getNearParkins(lastKnownLocation.getLatitude(),
                                lastKnownLocation.getLongitude());
                        int i = 0;
                        for (LatLng latLng : nearestResults) {
                            PlaceRecord placeRecord = new PlaceRecord();
                            placeRecord.setLog(latLng.longitude);
                            placeRecord.setLat(latLng.latitude);
                            setMarkerOnLocation(placeRecord, R.drawable.ic_marker_unused,"park"+i);
                            i++;
                        }
                        i = 0;
                    } else {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        } catch(SecurityException e)  {

        }
    }

    private void moveCameraToPosition(double latitude, double longitude) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitude, longitude), DEFAULT_ZOOM));
    }

    private MarkerOptions setMarkerOnLocation(PlaceRecord place, int icon, String title) {
        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(place.getLat(), place.getLog()))
                .icon(BitmapDescriptorFactory.fromResource(icon))
                .title(title);
        map.addMarker(marker);
        return marker;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            map.clear();

            Place place = PlaceAutocomplete.getPlace(this, data);
            whereToButtonView.setText(place.getAddress());
            whereToButtonView.setAllCaps(false);

            recentSearchLayoutView.setVisibility(View.GONE);

            realm.beginTransaction();
            PlaceRecord placeRecord = new PlaceRecord(place.getAddress().toString(), place.getName().toString(),
                    place.getLatLng().toString(), null);
            placeRecord.setLat(place.getLatLng().latitude);
            placeRecord.setLog(place.getLatLng().longitude);
            realm.copyToRealm(placeRecord);

            setMarkerOnLocation(placeRecord, R.drawable.ic_marker_user, "user");
            moveCameraToPosition(place.getLatLng().latitude, place.getLatLng().longitude);

            nearestResults = getNearParkins(place.getLatLng().latitude, place.getLatLng().longitude);
            int i = 0;
            for (LatLng latLng : nearestResults) {
                PlaceRecord placeRecordParkins = new PlaceRecord();
                placeRecordParkins.setLog(latLng.longitude);
                placeRecordParkins.setLat(latLng.latitude);
                setMarkerOnLocation(placeRecordParkins, R.drawable.ic_marker_unused,"park"+i);
                i++;
            }
            i = 0;
        }
    }

    private void configureList(List<PlaceRecord> items) {
        RecyclerView.Adapter adapter = new TempAdapter(items,
                new TempAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(PlaceRecord item) {
                        map.clear();

                        whereToButtonView.setText(item.getAddress());
                        recentSearchLayoutView.setVisibility(View.GONE);

                        setMarkerOnLocation(item, R.drawable.ic_marker_user,"user");
                        moveCameraToPosition(item.getLat(), item.getLog());

                        nearestResults = getNearParkins(item.getLat(), item.getLog());
                        int i = 0;
                        for (LatLng latLng : nearestResults) {
                            PlaceRecord placeRecordParkins = new PlaceRecord();
                            placeRecordParkins.setLog(latLng.longitude);
                            placeRecordParkins.setLat(latLng.latitude);
                            setMarkerOnLocation(placeRecordParkins, R.drawable.ic_marker_unused,"park"+i);
                            i++;
                        }
                        i = 0;
                    }
                });
        recyclerView.setAdapter(adapter);
    }

    private Marker selectedMark;

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!marker.getTitle().equalsIgnoreCase("user")) {
            if(selectedMark != null && !selectedMark.getTitle().equalsIgnoreCase(marker.getTitle())) {
                selectedMark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_unused));
            }
            database.child("usedLocations").child(marker.getId()).setValue(true);
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_selected));

            selectedParking = marker.getPosition();
            selectedMark = marker;
        }
        return false;
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

        map.clear();
    }

    @OnClick(R.id.btn_confirm)
    public void goToSelectedPosition() {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(nearestResults.get(0).latitude,
                        nearestResults.get(0).longitude), DEFAULT_ZOOM));
    }

    private ArrayList<LatLng> getNearParkins(double lat, double lon) {
        ArrayList<LatLng> latLongList = new ArrayList<>();
        for (Binding parking : WelcomeActivity.parkingJSONS.getBindings()) {
            LatLng latLng = new LatLng(Double.parseDouble(parking.getGeoLat().getValue()),
                    Double.parseDouble(parking.getGeoLong().getValue()));
            latLongList.add(latLng);
        }
        LatLng givenPosition = new LatLng(lat,lon);
        ArrayList<LatLng> selectedPositions = new ArrayList<>();
        selectedPositions.add(0, latLongList.get(0));
        /*
        for (int i=0; i<latLongList.size(); i++) {
            LatLng parking = latLongList.get(i);
            double distance = calculationByDistance(givenPosition, parking);
            double currentDistance = calculationByDistance(givenPosition, selectedPositions.get(0));
            if(distance < currentDistance) {
                selectedPositions.set(0, parking);
            }
        }*/


        selectedPositions.add(1, latLongList.get(1));
        selectedPositions.add(2, latLongList.get(2));
        selectedPositions.add(3, latLongList.get(3));
        for (LatLng parking : latLongList) {
            double newDistance = calculationByDistance(givenPosition, parking);
            int biggerIndex = getLongestDistanceIndex(givenPosition,selectedPositions);
            double biggerDistance = calculationByDistance(givenPosition, selectedPositions.get(biggerIndex));
            if(newDistance < biggerDistance) {
                selectedPositions.set(biggerIndex, parking);
            }
        }
        return selectedPositions;
    }

    public int getLongestDistanceIndex(LatLng givenPosition, ArrayList<LatLng> selectedPositions) {
        int biggerPositionIndex = 0;
        for(int i=0; i < selectedPositions.size(); i++) {
            double biggerPosDistance = calculationByDistance(givenPosition,selectedPositions.get(biggerPositionIndex));
            double selectedPosDistance = calculationByDistance(givenPosition,selectedPositions.get(i));
            if(biggerPosDistance < selectedPosDistance) {
                biggerPositionIndex = i;
            }
        }
        return biggerPositionIndex;
    }

    public double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return Radius * c;
    }
}
