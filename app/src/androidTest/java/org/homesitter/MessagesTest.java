package org.homesitter;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by mtkachenko on 09/09/16.
 */

@RunWith(AndroidJUnit4.class)
public class MessagesTest {

    @Test
    public void isHomeDisconnected() throws JSONException {
        String message = "{\"action\":\"leave\",\"timestamp\":1473432805,\"uuid\":\"home\",\"occupancy\":1}";

        boolean homeConnected = Messages.isHomeDisconnected(new JSONObject(message), "home");
        Assert.assertTrue(homeConnected);
    }

    @Test
    public void isHomeConnected() throws JSONException {
        String message = "{\"action\":\"join\",\"timestamp\":1473433957,\"uuid\":\"home\",\"occupancy\":2}";

        boolean homeConnected = Messages.isHomeConnected(new JSONObject(message), "home");
        Assert.assertTrue(homeConnected);
    }
}
