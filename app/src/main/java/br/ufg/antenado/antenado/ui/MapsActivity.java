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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import br.ufg.antenado.antenado.util.MapUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    public final static int ALERT_CREATED = 10;

    private GoogleMap mMap;
    private HashMap<Marker, Occurrence> markerInformation;

    @Bind(R.id.time_ago)
    TextView timeAgo;
    @Bind(R.id.distance)
    TextView distance;
    @Bind(R.id.main_toolbar)
    Toolbar toolbar;
    @Bind(R.id.alert_address)
    TextView address;
    @Bind(R.id.alert_title)
    TextView alertTitle;
    @Bind(R.id.maps_top_container)
    View topContainer;
    @Bind(R.id.progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.maps_bottom_container)
    View bottomContainer;
    @Bind(R.id.alert_description)
    TextView alertDescription;
    @Bind(R.id.create_alert)
    FloatingActionButton createAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
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
        progressBar.setVisibility(View.INVISIBLE);

        //Pede permissão para pegar a localização atual
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
                .setDeniedMessage("Adicione a permissão de Localização em Configurações.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();

        if (MapUtils.getMyLocation(this) != null) {
            MapUtils.zoomToLocation(mMap, new LatLng(MapUtils.getMyLocation(this).getLatitude(), MapUtils.getMyLocation(this).getLongitude()));
        }

        refreshMap();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Occurrence occurrence = markerInformation.get(marker);
        alertTitle.setText(occurrence.getTitle());
        alertDescription.setText(occurrence.getDescription());
        timeAgo.setText(occurrence.getTimeAgo());
        topContainer.setVisibility(View.VISIBLE);
        bottomContainer.setVisibility(View.VISIBLE);
        createAlert.setVisibility(View.GONE);

        //Pegar a localização e uma tarefa pesada, então colocamos em outra thread
        MapUtils.getMarkerAddress(this, new LatLng(occurrence.getLatitude(), occurrence.getLongitude()), new MapUtils.MarkerAddressListener() {
            @Override
            public void onAddressRetrieved(MarkerAddress markerAddress) {
                String formattedAddress = String.format(Locale.ENGLISH, "%s - %s - %s", markerAddress.getAddress(), markerAddress.getCity(), markerAddress.getState());
                address.setText(formattedAddress);
            }
        });

        Location myLocation = MapUtils.getMyLocation(this);

        if (myLocation != null) {
            MapUtils.setDistanceBetweenLocations(distance,
                    new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), marker.getPosition());
        }


        return false;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ALERT_CREATED && data!= null) {
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
        progressBar.setVisibility(View.VISIBLE);
        MapController.listOccurrences(new Callback<List<Occurrence>>() {
            @Override
            public void onSuccess(List<Occurrence> occurrences) {
                markerInformation = new HashMap<>();

                for (int i = 0; i < occurrences.size(); i++) {
                    LatLng latLng = new LatLng(occurrences.get(i).getLatitude(), occurrences.get(i).getLongitude());

                    Marker marker;

                    if (occurrences.get(i).isMine()) {
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(occurrences.get(i).getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker_blue)));
                    } else if (occurrences.get(i).getSeverity().equals("Risco Médio")) {
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(occurrences.get(i).getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
                    } else {
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(occurrences.get(i).getTitle())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker)));
                    }

                    markerInformation.put(marker, occurrences.get(i));
                }
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String errorMessage) {
                Snackbar.make(findViewById(android.R.id.content), "Sem conexão com a internet.", Snackbar.LENGTH_LONG).show();
            }
        });
    }


}
