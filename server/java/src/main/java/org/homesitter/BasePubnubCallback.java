package org.homesitter;

import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;

/**
 * Created by mtkachenko on 11/09/16.
 */
public class BasePubnubCallback extends Callback {
    private final String action;

    public BasePubnubCallback(String action) {
        this.action = action;
    }

    @Override
    public void successCallback(String s, Object o) {
        Log.i(action + ".successCallback2");
    }

    @Override
    public void errorCallback(String s, PubnubError pubnubError) {
        Log.i(action + ".errorCallback");
    }
}
