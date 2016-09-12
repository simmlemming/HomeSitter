package org.homesitter.service;

import android.util.Log;

import com.pubnub.api.PubnubError;

import org.homesitter.HomeSitter;
import org.homesitter.Messages;
import org.homesitter.model.Picture;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mtkachenko on 02/09/16.
 */
class SubscribeCallback extends BasePubnubCallback {

    SubscribeCallback(PubnubService pubnubService) {
        super(pubnubService);
    }

    @Override
    public void successCallback(String channel, Object message, String timetoken) {
        super.successCallback(channel, message, timetoken);

        if (Messages.isNewPicture(message)) {
            Picture picture = null;
            try {
                picture = Picture.fromMessage((JSONObject) message);
                pubnubService.onNewPicture(picture);
            } catch (JSONException e) {
                Log.e(HomeSitter.TAG, "Cannot parse picture new message", e);
            }
        }
    }

    @Override
    public void connectCallback(String channel, Object message) {
        pubnubService.setConnected(true);
    }

    @Override
    public void errorCallback(String channel, PubnubError error) {
        pubnubService.setConnected(false);
    }

    @Override
    public void disconnectCallback(String channel, Object message) {
        pubnubService.setConnected(false);
    }

    @Override
    public void reconnectCallback(String channel, Object message) {
        pubnubService.setConnected(false);
    }
}
