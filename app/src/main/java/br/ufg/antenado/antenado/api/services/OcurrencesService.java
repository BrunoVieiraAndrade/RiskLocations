package br.ufg.antenado.antenado.api.services;

import java.util.List;

import br.ufg.antenado.antenado.model.Occurrence;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by diogojayme on 7/5/16.
 */
public interface OcurrencesService {

    @GET("/api/v1/occurrences")
    Call<List<Occurrence>> listOccurrences();

    @POST("/api/v1/occurrences/create")
    Call<Occurrence> createAlert(@Body Occurrence occurrence);
}
