package br.ufg.antenado.antenado.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.ufg.antenado.antenado.Callback;
import br.ufg.antenado.antenado.MapController;
import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.model.MarkerAddress;
import br.ufg.antenado.antenado.model.Occurrence;
import br.ufg.antenado.antenado.util.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    public final static int ALERT_CREATED = 10;

    private GoogleMap mMap;
    HashMap<Marker, Occurrence> markerInformation;

    @Bind(R.id.main_toolbar) Toolbar toolbar;
    @Bind(R.id.maps_top_container) View topContainer;
    @Bind(R.id.maps_bottom_container) View bottomContainer;
    @Bind(R.id.create_alert) FloatingActionButton createAlert;
    @Bind(R.id.alert_title) TextView alertTitle;
    @Bind(R.id.alert_description) TextView alertdescription;
    @Bind(R.id.time_ago) TextView timeAgo;
    @Bind(R.id.distance) TextView distance;
    @Bind(R.id.alert_address) TextView address;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        markerInformation = new HashMap<>();
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Alertas");
            toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        }

        createView();
    }

    @Override
    public void onBackPressed() {
        System.out.println();
        if (topContainer.getVisibility() == View.VISIBLE && bottomContainer.getVisibility() == View.VISIBLE) {
            topContainer.setVisibility(View.INVISIBLE);
            bottomContainer.setVisibility(View.INVISIBLE);
            createAlert.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void createView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }

                        mMap.setMyLocationEnabled(true);
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> arrayList) {

                    }
                })
                .setDeniedMessage("Adicione permissão para pegar sua localização atual")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        MapController.listOccurrences(new Callback<List<Occurrence>>() {
            @Override
            public void onSuccess(List<Occurrence> occurrences) {
                for (int i = 0; i < occurrences.size(); i++) {
                    LatLng sydney = new LatLng(occurrences.get(i).getLatitude(), occurrences.get(i).getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(sydney).title(occurrences.get(i).getTitle()));
                    markerInformation.put(marker, occurrences.get(i));
                }
            }

            @Override
            public void onError(String errorMessage) {
                //show error to user
            }
        });

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Occurrence occurrence = markerInformation.get(marker);
        alertTitle.setText(occurrence.getTitle());
        alertdescription.setText(occurrence.getDescription());
        timeAgo.setText(occurrence.getTimeAgo());
        topContainer.setVisibility(View.VISIBLE);
        bottomContainer.setVisibility(View.VISIBLE);
        createAlert.setVisibility(View.GONE);
        MarkerAddress markerAddress = Utils.getAddress(this, new LatLng(occurrence.getLatitude(), occurrence.getLongitude()));
        String fullAddress = String.format(Locale.ENGLISH, "%s - %s - %s", markerAddress.getAddress(), markerAddress.getCity(), markerAddress.getState());
        String knowName = ", " + markerAddress.getKnownName();
        fullAddress.replace(fullAddress, knowName);
        address.setText(fullAddress);
        // Move the camera instantly to Sydney with a zoom of 15.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15));

        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(marker.getPosition())      // Sets the center of the map to Mountain View
                .zoom(100)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if(getMyLocation() != null) {
            float[] results = new float[1];
            Location.distanceBetween(getMyLocation().getLatitude(), getMyLocation().getLongitude(),
                    marker.getPosition().latitude, marker.getPosition().longitude, results);
            distance.setText(Utils.mToKm((long) results[0]));

        }

        return false;
    }

    private Location getMyLocation(){
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));

        return location;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        topContainer.setVisibility(View.INVISIBLE);
        bottomContainer.setVisibility(View.INVISIBLE);
        createAlert.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.create_alert)
    void onCreateAlertClick(){
        startActivityForResult(new Intent(this, AlertActivity.class), ALERT_CREATED);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        BitmapDescriptor markercolor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        if(resultCode == 10){
            if(requestCode == ALERT_CREATED){
                Occurrence occurrence = (Occurrence) data.getExtras().getSerializable("occurrence");
                if(occurrence.getSeverity().equals("Risco Médio")){
                    markercolor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                }
                if(occurrence.isMine()){
                    markercolor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                }
                LatLng location = new LatLng(occurrence.getLatitude(), occurrence.getLongitude());
                Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(occurrence.getTitle()).icon(markercolor));
                markerInformation.put(marker, occurrence);
            }
        }

    }

}
