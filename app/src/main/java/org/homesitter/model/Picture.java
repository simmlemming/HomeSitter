package org.homesitter.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mtkachenko on 09/09/16.
 */
public class Picture {
    public final String link;
    public final long timeMs;

    public static Picture fromMessage(JSONObject message) throws JSONException {
        String link = message.getString("link");
        long time = message.getLong("time");
        return new Picture(link, time);
    }

    public Picture(String link, long timeMs) {
        this.link = link;
        this.timeMs = timeMs;
    }
}
