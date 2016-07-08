package br.ufg.antenado.antenado.ui.launcher;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import br.ufg.antenado.antenado.R;
import br.ufg.antenado.antenado.ui.MapsActivity;

public class SplashActivity extends AppCompatActivity {

    Handler handler;

    public void onAttachedToWindow() {
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        window.setFormat(PixelFormat.RGBA_8888);
        super.onAttachedToWindow();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }, 1000);
    }
}
