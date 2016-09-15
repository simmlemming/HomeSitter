package org.homesitter.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by mtkachenko on 09/09/16.
 */
public class Picture implements Serializable {
    @NonNull
    public final String link;
    public final long timeMs;
    public final int cameraIndex;

    public static Picture fromMessage(JSONObject message) throws JSONException {
        String link = message.getString("link");
        long time = message.getLong("time");
        int cameraIndex = message.optInt("camera_index", 0);
        return new Picture(link, time, cameraIndex);
    }

    public Picture(@NonNull String link, long timeMs, int cameraIndex) {
        this.link = link;
        this.timeMs = timeMs;
        this.cameraIndex = cameraIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Picture)) {
            return false;
        }

        Picture other = (Picture) o;
        return this.link.equals(other.link)
                && this.timeMs == other.timeMs
                && this.cameraIndex == other.cameraIndex;
    }

    @Override
    public String toString() {
        return new SimpleDateFormat("MMM d',' HH:mm:ss").format(timeMs) + " [" + cameraIndex + "]";
    }

    @Override
    public int hashCode() {
        return link.hashCode() + Long.valueOf(timeMs).intValue() + 13 * cameraIndex;
    }
}
