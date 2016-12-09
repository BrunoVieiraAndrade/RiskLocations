package br.ufg.antenado.antenado.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import br.ufg.antenado.antenado.Callback;
import br.ufg.antenado.antenado.MapController;
import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.model.MarkerAddress;
import br.ufg.antenado.antenado.model.Occurrence;
import br.ufg.antenado.antenado.util.MapUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static br.ufg.antenado.antenado.R.id.map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener {

    final double INITIAL_LAT_POS = -16.7059516;
    final double INITIAL_LNG_POS = -49.241514;
    final int ARRAY_SIZE = 2;
    final int OPTION19 = 19;
    final int OPTION15 = 15;
    final int OPTION16 = 16;

    private Circle circle;
    private GoogleMap mMap;
    private LatLng centerLocation;
    private boolean moving = false;
    private static final int RADIUS = 3000;
    private List<Marker> markers = new ArrayList<>();
    private HashMap<Marker, Occurrence> markerInformation;
    private LatLng startPosition = new LatLng(INITIAL_LAT_POS, INITIAL_LNG_POS);

    public final static int ALERT_CREATED = 10;
    public static final int LOCATION_PERMISSIONS_GRANTED = 11;

    @Bind(R.id.time_ago) TextView timeAgo;
    @Bind(R.id.distance) TextView distance;
    @Bind(R.id.main_toolbar) Toolbar toolbar;
    @Bind(R.id.alert_address) TextView address;
    @Bind(R.id.alert_title) TextView alertTitle;
    @Bind(R.id.maps_top_container) View topContainer;
    @Bind(R.id.general_container) View generalContainer;
    @Bind(R.id.maps_bottom_container) View bottomContainer;
    @Bind(R.id.alert_description) TextView alertDescription;
    @Bind(R.id.create_alert) FloatingActionButton createAlert;
    @Bind(R.id.fixed_marker_address) TextView fixedMarkerAddress;
    @Bind(R.id.fixed_marker_container) View fixedMarkerContainer;

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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnCameraChangeListener(this);

        circle = mMap.addCircle(new CircleOptions()
                .center(startPosition)
                .radius(RADIUS)
                .strokeColor(Color.TRANSPARENT));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //permission not conceded, ask for permission asynchronously
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSIONS_GRANTED);
        } else {
            //permission already granted
            mMap.setMyLocationEnabled(true);

            Location location = MapUtils.getMyLocation(MapsActivity.this);

            if(location != null){
                centerLocation = new LatLng(location.getLatitude(), location.getLongitude());
                MapUtils.zoomToLocation(mMap, new LatLng(location.getLatitude(),location.getLongitude()), OPTION19);

                MapUtils.getMarkerAddress(this, new LatLng(location.getLatitude(), location.getLongitude()), new MapUtils.MarkerAddressListener() {
                    @Override
                    public void onAddressRetrieved(MarkerAddress address) {
                        fixedMarkerAddress.setText(address.getAddress());
                    }

                    @Override
                    public void onAddressFailed(String message) {
                        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }else {
                MapUtils.zoomToLocation(mMap, startPosition, OPTION15);
            }

        }

        refreshMap();
    }


    @Override
    public void onMapClick(LatLng latLng) {
        moving = false;
        fixedMarkerContainer.setVisibility(View.VISIBLE);
        createAlert.setVisibility(View.VISIBLE);
        topContainer.setVisibility(View.INVISIBLE);
        bottomContainer.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.create_alert)
    void onCreateAlertClick() {
        startActivityForResult(new Intent(this, CreateAlertActivity.class)
                .putExtra("latitude", centerLocation.latitude)
                .putExtra("longitude", centerLocation.longitude),
                ALERT_CREATED);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        moving = true;
        fixedMarkerContainer.setVisibility(View.INVISIBLE);
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

            @Override
            public void onAddressFailed(String message) {
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
            }
        });

        MapUtils.zoomToLocation(mMap, marker.getPosition(), OPTION16);
        Location location = MapUtils.getMyLocation(this);

        if (location != null) {
            LatLng myPosition = new LatLng(location.getLatitude(), location.getLatitude());
            MapUtils.setDistanceBetweenLocations(distance, myPosition, marker.getPosition());
        }

        return true;
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
                for (int i = 0; i < occurrences.size(); i++) {
                    LatLng latLng = new LatLng(occurrences.get(i).getLatitude(), occurrences.get(i).getLongitude());

                    Marker marker;

                    if (occurrences.get(i).isMine()) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .title(occurrences.get(i).getTitle())
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_blue));

                        marker = mMap.addMarker(markerOptions);
                    } else if (occurrences.get(i).getSeverity().equals("Risco Médio")) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .title(occurrences.get(i).getTitle())
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_yellow));

                        marker = mMap.addMarker(markerOptions);
                    }else {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .title(occurrences.get(i).getTitle())
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker));

                        marker = mMap.addMarker(markerOptions);
                    }

                    markerInformation.put(marker, occurrences.get(i));
                    markers.add(marker);
                }

                setMarkersVisibility();

            }

            @Override
            public void onError(String errorMessage) {
                Snackbar.make(findViewById(android.R.id.content), "Sem conexão com a internet.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_GRANTED: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        Location location = MapUtils.getMyLocation(this);

                        if(location != null){
                            MapUtils.zoomToLocation(mMap, new LatLng(location.getLatitude(),location.getLongitude()), OPTION19);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onCameraChange(final CameraPosition cameraPosition) {
        if(!moving) {
            circle = mMap.addCircle(new CircleOptions()
                    .center(cameraPosition.target)
                    .radius(RADIUS)
                    .strokeColor(Color.TRANSPARENT));
            centerLocation = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);

            MapUtils.getMarkerAddress(this, cameraPosition.target, new MapUtils.MarkerAddressListener() {
                @Override
                public void onAddressRetrieved(final MarkerAddress address) {
                    fixedMarkerAddress.setText(address.getAddress());
                }

                @Override
                public void onAddressFailed(String message) {
                    Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
                }
            });
            setMarkersVisibility();
        }
    }

    public void setMarkersVisibility(){
        for (Marker marker: markers) {

            float[] distance = new float[ARRAY_SIZE];

            Location.distanceBetween( marker.getPosition().latitude, marker.getPosition().longitude,
                    circle.getCenter().latitude, circle.getCenter().longitude, distance);

            if( distance[0] > circle.getRadius()  ){
                marker.setVisible(false);
            } else {
                marker.setVisible(true);
            }
        }
    }

}
