package br.ufg.antenado.antenado.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import br.ufg.antenado.antenado.Callback;
import br.ufg.antenado.antenado.MapController;
import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.model.Ocurrence;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    @Bind(R.id.main_toolbar) Toolbar toolbar;
    @Bind(R.id.maps_top_container) View topContainer;
    @Bind(R.id.maps_bottom_container) View bottomContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        if(getSupportActionBar()!= null) {
            getSupportActionBar().setTitle("Alertas");
            toolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        }

        createView();
    }

    private void createView(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        MapController.listOccurrences(new Callback<List<Ocurrence>>() {
            @Override
            public void onSuccess(List<Ocurrence> occurrences) {
                for (int i = 0; i < occurrences.size(); i++) {
                    LatLng sydney = new LatLng(occurrences.get(i).getLatitude(), occurrences.get(i).getLongitude());
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
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
        topContainer.setVisibility(View.VISIBLE);
        bottomContainer.setVisibility(View.VISIBLE);

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
}
