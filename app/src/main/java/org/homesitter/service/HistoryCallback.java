package org.homesitter.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pubnub.api.PubnubError;

import org.homesitter.Messages;
import org.homesitter.model.Picture;

import java.util.Collections;
import java.util.List;

/**
 * Created by mtkachenko on 12/09/16.
 */
public class HistoryCallback extends BasePubnubCallback {

    HistoryCallback(@NonNull PubnubService pubnubService) {
        super(pubnubService);
    }

    @Override
    public void successCallback(String channel, Object message) {
        super.successCallback(channel, message);

        List<Picture> pictures;
        try {
            pictures = Messages.parseHistory(message);
        } catch (Messages.MalformedMessageException e) {
            pictures = Collections.emptyList();
            Log.e(PubnubService.TAG, "Cannot parse history", e);
            pubnubService.notifyStateChanged(e.getCause().getMessage());
        }

        if (!pictures.isEmpty()) {
            Picture lastPicture = pictures.get(pictures.size() - 1);
            pubnubService.onNewPicture(lastPicture);
        }
    }

    @Override
    public void errorCallback(String channel, PubnubError error) {
        super.errorCallback(channel, error);
        pubnubService.notifyStateChanged(error.getErrorString());
    }
}
