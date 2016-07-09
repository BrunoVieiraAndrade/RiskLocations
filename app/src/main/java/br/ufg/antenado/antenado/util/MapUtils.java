package br.ufg.antenado.antenado.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import br.ufg.antenado.antenado.model.MarkerAddress;

/**
 * Created by diogojayme on 7/7/16.
 */
public class MapUtils {

    //Listener para notificar a activity quando o endereço for carregado
    public static interface MarkerAddressListener{
        void onAddressRetrieved(MarkerAddress address);
    }

    /*
     * Pega o endereço a partir da Latitude e Longitude
     *
     */
    public static void getMarkerAddress(final Context context, final LatLng latLng, final MarkerAddressListener listener){
        final Handler handler = new Handler();
        final Handler mainHandler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                    final MarkerAddress markerAddress = new MarkerAddress();
                    markerAddress.setAddress(address);
                    markerAddress.setCity(city);
                    markerAddress.setState(state);
                    markerAddress.setCountry(country);
                    markerAddress.setPostalCode(postalCode);
                    markerAddress.setKnownName(knownName);

                    //Callback in the main thread
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onAddressRetrieved(markerAddress);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
     * Pega a localização atual
     *
     */
    public static Location getMyLocation(Context context){
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        //verifica se a permissão foi concedida
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        return locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
    }

    /*
     * Seta a distância entre um ponto em outro, nesse caso o ponto
     *
     */
    public static void setDistanceBetweenLocations(TextView textView, LatLng myLocation, LatLng targetLocation){
        float[] results = new float[1];
        Location.distanceBetween(myLocation.latitude, myLocation.longitude,
                targetLocation.latitude, targetLocation.longitude, results);

        textView.setText(MapUtils.mToKm((long) results[0]));
    }

    /*
     * Converte a distância em metros para km
     *
     */
    public static String mToKm(long count){
        if (count < 1000) return count + " m";
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format(Locale.ENGLISH, "%.1f %s", count / Math.pow(1000, exp), "km");
    }

}
