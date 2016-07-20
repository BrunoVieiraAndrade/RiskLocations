package br.ufg.antenado.antenado.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.ufg.antenado.antenado.Callback;
import br.ufg.antenado.antenado.MapController;
import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.model.MarkerAddress;
import br.ufg.antenado.antenado.model.Occurrence;
import br.ufg.antenado.antenado.util.LatLngInterpolator;
import br.ufg.antenado.antenado.util.MapUtils;
import br.ufg.antenado.antenado.util.MarkerAnimation;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private HashMap<Marker, Occurrence> markerInformation;

    public final static int ALERT_CREATED = 10;
    public static final int LOCATION_PERMISSIONS_GRANTED = 11;

    @Bind(R.id.time_ago) TextView timeAgo;
    @Bind(R.id.distance) TextView distance;
    @Bind(R.id.main_toolbar) Toolbar toolbar;
    @Bind(R.id.alert_address) TextView address;
    @Bind(R.id.alert_title) TextView alertTitle;
    @Bind(R.id.maps_top_container) View topContainer;
    @Bind(R.id.maps_bottom_container) View bottomContainer;
    @Bind(R.id.alert_description) TextView alertDescription;
    @Bind(R.id.create_alert) FloatingActionButton createAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        createView();
    }

    @Override
    public void onBackPressed() {
        if (topContainer.getVisibility() == View.VISIBLE && bottomContainer.getVisibility() == View.VISIBLE) {
            topContainer.setVisibility(View.INVISIBLE);
            bottomContainer.setVisibility(View.INVISIBLE);
            createAlert.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.onBackPressed();
                break;
            case R.id.action_refresh:
                refreshMap();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createView() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission not conceded, ask for permission asynchronously
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSIONS_GRANTED);
        } else {
            //permission already granted
            mMap.setMyLocationEnabled(true);
            if(MapUtils.getMyLocation(MapsActivity.this) != null){
                MapUtils.zoomToLocation(mMap, new LatLng(MapUtils.getMyLocation(MapsActivity.this).getLatitude(), MapUtils.getMyLocation(MapsActivity.this).getLongitude()));
            }
        }

        refreshMap();
    }


    @Override
    public void onMapClick(LatLng latLng) {
        createAlert.setVisibility(View.VISIBLE);
        topContainer.setVisibility(View.INVISIBLE);
        bottomContainer.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.create_alert)
    void onCreateAlertClick() {
        startActivityForResult(new Intent(this, CreateAlertActivity.class), ALERT_CREATED);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        createAlert.setVisibility(View.INVISIBLE);
        Occurrence occurrence = markerInformation.get(marker);
        alertTitle.setText(occurrence.getTitle());
        alertDescription.setText(occurrence.getDescription());
        timeAgo.setText(occurrence.getTimeAgo());
        topContainer.setVisibility(View.VISIBLE);
        bottomContainer.setVisibility(View.VISIBLE);

        //Pegar a localização e uma tarefa pesada, então colocamos em outra thread
        MapUtils.getMarkerAddress(this, new LatLng(occurrence.getLatitude(), occurrence.getLongitude()), new MapUtils.MarkerAddressListener() {
            @Override
            public void onAddressRetrieved(MarkerAddress markerAddress) {
                String formattedAddress = String.format(Locale.ENGLISH, "%s - %s - %s", markerAddress.getAddress(), markerAddress.getCity(), markerAddress.getState());
                address.setText(formattedAddress);
            }
        });


        MapUtils.zoomToLocation(mMap, marker.getPosition());

        Location location = MapUtils.getMyLocation(this);

        if (location != null) {
            LatLng myPosition = new LatLng(location.getLatitude(), location.getLatitude());
            MapUtils.setDistanceBetweenLocations(distance, myPosition, marker.getPosition());
        }

        MarkerAnimation markerAnimation = new MarkerAnimation(marker, marker.getPosition(), new LatLngInterpolator.Spherical(), 100);
        markerAnimation.animate();

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ALERT_CREATED && data != null) {
            Occurrence occurrence = (Occurrence) data.getExtras().getSerializable("occurrence");
            LatLng location = new LatLng(occurrence.getLatitude(), occurrence.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(occurrence.getTitle())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_blue)));
            markerInformation.put(marker, occurrence);
        }

    }

    public void refreshMap() {
        //Busca as ocorrencias da API {/api/v1/occurrences}
        MapController.listOccurrences(new Callback<List<Occurrence>>() {
            @Override
            public void onSuccess(final List<Occurrence> occurrences) {
                markerInformation = new HashMap<>();


                for (int i = 0; i < 2; i++) {
                    LatLng latLng = new LatLng(occurrences.get(i).getLatitude(), occurrences.get(i).getLongitude());

                    Marker marker;

                    marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(occurrences.get(i).getTitle()));
//
//                            if (occurrences.get(i).isMine()) {
//
//                            } else if (occurrences.get(i).getSeverity().equals("Risco Médio")) {
//                                marker = mMap.addMarker(new MarkerOptions()
//                                        .position(latLng)
//                                        .title(occurrences.get(i).getTitle()));
//                            }else {
//                                marker = mMap.addMarker(new MarkerOptions()
//                                        .position(latLng)
//                                        .title(occurrences.get(i).getTitle()));
//                            }

                    markerInformation.put(marker, occurrences.get(i));
                }

            }

            @Override
            public void onError(String errorMessage) {
                Snackbar.make(findViewById(android.R.id.content), "Sem conexão com a internet.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_GRANTED: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            }
        }
    }

}
