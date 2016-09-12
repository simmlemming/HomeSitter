package org.homesitter.service;

import android.support.annotation.NonNull;

import com.pubnub.api.PubnubError;

import org.homesitter.Messages;

/**
 * Created by mtkachenko on 02/09/16.
 */
class HomeConnectedCallback extends BasePubnubCallback {
    HomeConnectedCallback(@NonNull PubnubService pubnubService) {
        super(pubnubService);
    }

    @Override
    public void successCallback(String channel, Object message) {
        super.successCallback(channel, message);
        boolean isHomeConnected = Messages.isHomeHere(message, pubnubService.getHomeUuid());
        pubnubService.setHomeConnected(isHomeConnected);
    }

    @Override
    public void errorCallback(String channel, PubnubError error) {
        super.errorCallback(channel, error);
        pubnubService.setHomeConnected(false);
    }
}
