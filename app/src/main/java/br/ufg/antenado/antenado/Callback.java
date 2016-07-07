package br.ufg.antenado.antenado;

/**
 * Created by diogojayme on 7/4/16.
 */
public interface Callback<T> {
    void onSuccess(T t);
    void onError(String errorMessage);
}
