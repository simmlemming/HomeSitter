package org.homesitter.service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pubnub.api.PubnubError;

import org.homesitter.Messages;
import org.homesitter.model.Picture;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by mtkachenko on 12/09/16.
 */
public abstract class HistoryCallback extends BasePubnubCallback {
    private final int requestedCameraIndex;

    HistoryCallback(@NonNull PubnubService pubnubService, int cameraIndex) {
        super(pubnubService);
        this.requestedCameraIndex = cameraIndex;
    }

    @Override
    public void successCallback(String channel, Object message) {
        super.successCallback(channel, message);

        List<Picture> pictures;
        try {
            pictures = Messages.parseHistory(message);
            Iterator<Picture> iterator = pictures.iterator();
            while (iterator.hasNext()) {
                Picture picture = iterator.next();
                if (requestedCameraIndex != picture.cameraIndex) {
                    iterator.remove();
                }
            }
        } catch (Messages.MalformedMessageException e) {
            pictures = Collections.emptyList();
            Log.e(PubnubService.TAG, "Cannot parse history", e);
            onError(e.getCause().getMessage());
        }

        Picture lastPicture = null;
        if (!pictures.isEmpty()) {
            lastPicture = pictures.get(pictures.size() - 1);
        }

        onSuccess(lastPicture);
    }

    @Override
    public void errorCallback(String channel, PubnubError error) {
        super.errorCallback(channel, error);
        onError(error.getErrorString());
    }

    protected abstract void onSuccess(Picture picture);
    protected abstract void onError(String userFriendlyMessage);
}
