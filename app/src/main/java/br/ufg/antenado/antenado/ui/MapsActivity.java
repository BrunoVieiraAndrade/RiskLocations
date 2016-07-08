package br.ufg.antenado.antenado.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.util.HashMap;
import java.util.List;

import br.ufg.antenado.antenado.Callback;
import br.ufg.antenado.antenado.MapController;
import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.model.Occurrence;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        markerInformation = new HashMap<>();
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!= null) {
            getSupportActionBar().setTitle("Alertas");
            toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        }

        createView();
    }

    @Override
    public void onBackPressed() {
        System.out.println();
        if(topContainer.getVisibility()== View.VISIBLE &&bottomContainer.getVisibility() == View.VISIBLE){
            topContainer.setVisibility(View.GONE);
            bottomContainer.setVisibility(View.GONE);
            createAlert.setVisibility(View.VISIBLE);
        }else {
            super.onBackPressed();

        }
    }

    private void createView(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
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

        return false;
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
