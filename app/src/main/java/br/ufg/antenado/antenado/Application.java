package br.ufg.antenado.antenado;

import br.ufg.antenado.antenado.api.ApiManager;

/**
 * Created by diogojayme on 7/5/16.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApiManager.initialize();
    }
}
