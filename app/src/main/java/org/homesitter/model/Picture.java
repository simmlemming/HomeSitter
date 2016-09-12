package org.homesitter.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by mtkachenko on 09/09/16.
 */
public class Picture implements Serializable {
    @NonNull
    public final String link;
    public final long timeMs;

    public static Picture fromMessage(JSONObject message) throws JSONException {
        String link = message.getString("link");
        long time = message.getLong("time");
        return new Picture(link, time);
    }

    public Picture(@NonNull String link, long timeMs) {
        this.link = link;
        this.timeMs = timeMs;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Picture)) {
            return false;
        }

        Picture other = (Picture) o;
        return this.link.equals(other.link)
                && this.timeMs == other.timeMs;
    }

    @Override
    public int hashCode() {
        return link.hashCode() + Long.valueOf(timeMs).intValue();
    }
}
