package br.ufg.antenado.antenado;

import java.util.List;

import br.ufg.antenado.antenado.api.ApiManager;
import br.ufg.antenado.antenado.api.services.OcurrencesService;
import br.ufg.antenado.antenado.model.Ocurrence;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by diogojayme on 7/4/16.
 */
public class MapController {

    public static void listOccurrences(final Callback<List<Ocurrence>> callback){
        Call<List<Ocurrence>> call =  ApiManager.create(OcurrencesService.class).listOccurrences();
        call.enqueue(new retrofit2.Callback<List<Ocurrence>>() {
            @Override
            public void onResponse(Call<List<Ocurrence>> call, Response<List<Ocurrence>> response) {
                if(response.isSuccessful()){
                    callback.onSuccess(response.body());
                }else{
                    callback.onError(response.message());
                    //Something went wrong
                }
            }

            @Override
            public void onFailure(Call<List<Ocurrence>> call, Throwable t) {
                //Something went wrong
                callback.onError(t.getMessage());
            }
        });

    }

}
