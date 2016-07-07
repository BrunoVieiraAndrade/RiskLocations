package br.ufg.antenado.antenado.api.services;

import java.util.List;

import br.ufg.antenado.antenado.model.Ocurrence;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by diogojayme on 7/5/16.
 */
public interface OcurrencesService {

    @GET("/api/v1/occurrences/")
    Call<List<Ocurrence>> listOccurrences();
}
