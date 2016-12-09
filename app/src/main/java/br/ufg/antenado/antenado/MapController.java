package br.ufg.antenado.antenado;

import java.util.List;

import br.ufg.antenado.antenado.api.ApiManager;
import br.ufg.antenado.antenado.api.services.OcurrencesService;
import br.ufg.antenado.antenado.model.Occurrence;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by diogojayme on 7/4/16.
 */
public class MapController {

    private MapController(){

    }

    public static void listOccurrences(final Callback<List<Occurrence>> callback){
        Call<List<Occurrence>> call =  ApiManager.create(OcurrencesService.class).listOccurrences();
        call.enqueue(new retrofit2.Callback<List<Occurrence>>() {
            @Override
            public void onResponse(Call<List<Occurrence>> call, Response<List<Occurrence>> response) {
                if(response.isSuccessful()){
                    callback.onSuccess(response.body());
                }else{
                    callback.onError(response.message());
                    //Something went wrong
                }
            }

            @Override
            public void onFailure(Call<List<Occurrence>> call, Throwable t) {
                //Something went wrong
                callback.onError(t.getMessage());
            }
        });

    }

    public static void createAlert(Occurrence occurrence, final Callback<Occurrence> callback){
        Call<Occurrence> call =  ApiManager.create(OcurrencesService.class).createAlert(occurrence);
        call.enqueue(new retrofit2.Callback<Occurrence>() {
            @Override
            public void onResponse(Call<Occurrence> call, Response<Occurrence> response) {
                if(response.isSuccessful()){
                    callback.onSuccess(response.body());
                }else{
                    callback.onError(response.message());
                    //Something went wrong
                }
            }

            @Override
            public void onFailure(Call<Occurrence> call, Throwable t) {
                //Something went wrong
                callback.onError(t.getMessage());
            }
        });

    }

}
