package br.ufg.antenado.antenado.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import br.ufg.antenado.antenado.Callback;
import br.ufg.antenado.antenado.MapController;
import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.model.Occurrence;
import br.ufg.antenado.antenado.util.MapUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateAlertActivity extends AppCompatActivity {

    @Bind(R.id.alert_toolbar) Toolbar toolbar;
    @Bind(R.id.alertTitle) EditText alertTitle;
    @Bind(R.id.alertDescription) EditText alertDescription;
    @Bind(R.id.alert_severity) Spinner spinner;

    ArrayAdapter<CharSequence> adapter;

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
        adapter = ArrayAdapter
                .createFromResource(this, R.array.occurrences_severities, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
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

    public void onDoneClick(){

        if(alertTitle.getText().toString().equals("") || alertDescription.getText().toString().equals("")){
            Snackbar.make(findViewById(android.R.id.content), "Preencha todos os campos", Snackbar.LENGTH_LONG).show();
        }else {
            final Occurrence localOccurrence = new Occurrence();
            localOccurrence.setTitle(alertTitle.getText().toString());
            localOccurrence.setDescription(alertDescription.getText().toString());
            localOccurrence.setTimeAgo("1 min");
            if(MapUtils.getMyLocation(this) != null){
                localOccurrence.setLatitude(MapUtils.getMyLocation(this).getLatitude());
                localOccurrence.setLongitude(MapUtils.getMyLocation(this).getLongitude());
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

}
