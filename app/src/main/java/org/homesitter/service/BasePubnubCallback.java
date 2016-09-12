package org.homesitter.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;

/**
 * Created by mtkachenko on 02/09/16.
 */
class BasePubnubCallback extends Callback {
    @NonNull
    protected final PubnubService pubnubService;

    BasePubnubCallback(@NonNull PubnubService pubnubService) {
        this.pubnubService = pubnubService;
    }

    @Override
    public void successCallback(String channel, Object message, String timetoken) {
        Log.i(PubnubService.TAG, getClass().getSimpleName() + ".successCallback() called with: " + "channel = [" + channel + "], message = [" + message + "], timetoken = [" + timetoken + "]");
    }

    @Override
    public void successCallback(String channel, Object message) {
        Log.d(PubnubService.TAG, getClass().getSimpleName() + ".successCallback() called with: " + "channel = [" + channel + "], message = [" + message + "]");
    }

    @Override
    public void errorCallback(String channel, PubnubError error) {
        Log.d(PubnubService.TAG, getClass().getSimpleName() + ".errorCallback() called with: " + "channel = [" + channel + "], error = [" + error + "]");
    }

    @Override
    public void connectCallback(String channel, Object message) {
        Log.d(PubnubService.TAG, getClass().getSimpleName() + ".connectCallback() called with: " + "channel = [" + channel + "], message = [" + message + "]");
    }

    @Override
    public void disconnectCallback(String channel, Object message) {
        Log.d(PubnubService.TAG, getClass().getSimpleName() + ".disconnectCallback() called with: " + "channel = [" + channel + "], message = [" + message + "]");
    }
}
