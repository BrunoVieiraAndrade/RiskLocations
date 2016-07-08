package br.ufg.antenado.antenado.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import br.ufg.antenado.antenado.R;
import butterknife.Bind;
import butterknife.ButterKnife;

public class AlertActivity extends AppCompatActivity {

    @Bind(R.id.alertTitle) EditText alertTitle;
    @Bind(R.id.alertDescription) EditText alertDescription;
    @Bind(R.id.alert_severity) Spinner spinner;
    @Bind(R.id.enviar_button) Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        ButterKnife.bind(this);
        getActionBar().setTitle(getString(R.string.criar_alerta));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.occurrences_severities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
