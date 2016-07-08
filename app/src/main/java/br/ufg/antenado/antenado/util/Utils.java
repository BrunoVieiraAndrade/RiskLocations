package br.ufg.antenado.antenado.util;

import android.content.Context;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import br.ufg.antenado.antenado.model.MarkerAddress;

/**
 * Created by diogojayme on 7/7/16.
 */
public class Utils {

    public static MarkerAddress getAddress(Context context, LatLng latLng){
        Geocoder geocoder;
        List<android.location.Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            MarkerAddress markerAddress = new MarkerAddress();
            markerAddress.setAddress(address);
            markerAddress.setCity(city);
            markerAddress.setState(state);
            markerAddress.setCountry(country);
            markerAddress.setPostalCode(postalCode);
            markerAddress.setKnownName(knownName);

            return markerAddress;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String mToKm(long count){
        if (count < 1000) return count + " m";
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format(Locale.ENGLISH, "%.1f %s", count / Math.pow(1000, exp), "km");

    }

}
