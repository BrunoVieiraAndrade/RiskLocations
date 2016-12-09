package br.ufg.antenado.antenado.ui;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import br.ufg.antenado.antenado.Callback;
import br.ufg.antenado.antenado.MapController;
import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.model.Occurrence;
import br.ufg.antenado.antenado.util.MapUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateAlertActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Bind(R.id.alert_toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.alertTitle)
    protected EditText alertTitle;
    @Bind(R.id.alertDescription)
    protected EditText alertDescription;
    @Bind(R.id.alert_severity)
    protected Spinner spinner;

    private ArrayAdapter<CharSequence> adapter;

    private GoogleMap mMap;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alert);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!= null) {
            getSupportActionBar().setTitle(getString(R.string.criar_alerta));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        latitude = getIntent().getExtras().getDouble("latitude");
        longitude = getIntent().getExtras().getDouble("longitude");

        adapter = ArrayAdapter
                .createFromResource(this, R.array.occurrences_severities, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_alert);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.onBackPressed();
                break;
            case R.id.action_done:
                onDoneClick();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onDoneClick(){

        if(alertTitle.getText().toString().equals("") || alertDescription.getText().toString().equals("")){
            Snackbar.make(findViewById(android.R.id.content), "Preencha todos os campos", Snackbar.LENGTH_LONG).show();
        }else {
            final Occurrence localOccurrence = new Occurrence();
            localOccurrence.setTitle(alertTitle.getText().toString());
            localOccurrence.setDescription(alertDescription.getText().toString());
            localOccurrence.setTimeAgo("1 min");

            Location myLocation = MapUtils.getMyLocation(this);
            if(myLocation != null){
                localOccurrence.setLatitude(myLocation.getLatitude());
                localOccurrence.setLongitude(myLocation.getLongitude());
            }

            localOccurrence.setMine(true);
            localOccurrence.setSeverity((String) spinner.getSelectedItem());
            MapController.createAlert(localOccurrence, new Callback<Occurrence>() {
                @Override
                public void onSuccess(Occurrence occurrence) {
                    if(MapUtils.getMyLocation(CreateAlertActivity.this) == null){
                        localOccurrence.setLatitude(occurrence.getLatitude());
                        localOccurrence.setLongitude(occurrence.getLongitude());
                    }
                    setResult(MapsActivity.ALERT_CREATED, new Intent().putExtra("occurrence", localOccurrence));
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.connection_problem), Snackbar.LENGTH_LONG);
                }
            });
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker));

        mMap.addMarker(markerOptions);

        MapUtils.zoomToLocation(mMap, new LatLng(latitude,longitude), MapUtils.ZOOM_FACTOR);
    }
}
