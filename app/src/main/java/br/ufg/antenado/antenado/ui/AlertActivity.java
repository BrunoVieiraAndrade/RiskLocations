package br.ufg.antenado.antenado.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import br.ufg.antenado.antenado.Callback;
import br.ufg.antenado.antenado.MapController;
import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.model.Occurrence;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AlertActivity extends AppCompatActivity {

    @Bind(R.id.alert_toolbar) Toolbar toolbar;
    @Bind(R.id.alertTitle) EditText alertTitle;
    @Bind(R.id.alertDescription) EditText alertDescription;
    @Bind(R.id.alert_severity) Spinner spinner;
    @Bind(R.id.enviar_button) Button button;

    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!= null) {
            getSupportActionBar().setTitle(getString(R.string.criar_alerta));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        adapter = ArrayAdapter
                .createFromResource(this, R.array.occurrences_severities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @OnClick(R.id.enviar_button)
    void onAlertCreateClick(){
        final Occurrence localOccurrence = new Occurrence();
        localOccurrence.setTitle(alertTitle.getText().toString());
        localOccurrence.setDescription(alertDescription.getText().toString());
        localOccurrence.setTimeAgo("1 min");
        localOccurrence.setLatitude(-19.7059516);
        localOccurrence.setLongitude(-50.241514);
        localOccurrence.setMine(true);
        localOccurrence.setSeverity((String) spinner.getSelectedItem());
        MapController.createAlert(localOccurrence, new Callback<Occurrence>() {
            @Override
            public void onSuccess(Occurrence occurrence) {
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
