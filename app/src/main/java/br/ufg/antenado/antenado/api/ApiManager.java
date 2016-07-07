package br.ufg.antenado.antenado.api;

import com.google.gson.Gson;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by diogojayme on 7/4/16.
 */
public class ApiManager {

    private static Retrofit retrofit;
    private static ApiManager instance;

    public ApiManager(){
        Gson gson = new Gson();
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static void initialize(){
        if(instance == null){
            instance = new ApiManager();
        }
    }

    public static <T> T create(final Class<T> tClass){
        if(retrofit == null)
            throw new NullPointerException("Retrofit is null");

        if(tClass == null)
            throw new NullPointerException("Invalid call to class");

        return retrofit.create(tClass);
    }

}
