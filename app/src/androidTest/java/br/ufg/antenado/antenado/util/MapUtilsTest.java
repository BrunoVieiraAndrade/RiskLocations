package br.ufg.antenado.antenado.util;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by bruno.andrade on 09/12/2016.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class MapUtilsTest {

    @Test
    public void getMyLocationTest() {

        Assert.assertEquals(4, 4);
    }

    @Test
    public void testConvertDistanceSmallerThan1KTest() {

        String distanceConverted = MapUtils.convertDistance(999l);
        Assert.assertEquals("999m", distanceConverted);
    }

}