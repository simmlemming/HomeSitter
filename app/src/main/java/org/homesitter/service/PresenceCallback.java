package org.homesitter.service;

import android.support.annotation.NonNull;

import org.homesitter.Messages;

/**
 * Created by mtkachenko on 02/09/16.
 */
class PresenceCallback extends BasePubnubCallback {

    PresenceCallback(@NonNull PubnubService pubnubService) {
        super(pubnubService);
    }

    @Override
    public void successCallback(String channel, Object message) {
        super.successCallback(channel, message);

        boolean homeConnected = Messages.isHomeConnected(message, pubnubService.getHomeUuid());
        if (homeConnected) {
            pubnubService.setHomeConnected(true);
            return;
        }

        boolean homeDisconnected = Messages.isHomeDisconnected(message, pubnubService.getHomeUuid());
        if (homeDisconnected) {
            pubnubService.setHomeConnected(false);
        }
    }
}
