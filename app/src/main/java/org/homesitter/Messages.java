package org.homesitter;

import android.util.Log;

import org.homesitter.service.PubnubService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mtkachenko on 02/09/16.
 */
public class Messages {

    public static boolean isNewPicture(Object message) {
        if (!(message instanceof JSONObject)) {
            return false;
        }

        JSONObject update = (JSONObject) message;
        try {
            String action = update.getString("action");
            return "new_picture".equals(action);
        } catch (JSONException e) {
            Log.e(PubnubService.TAG, "Malformed new picture message", e);
            return false;
        }
    }

    public static boolean isHomeConnected(Object message, String homeUuid) {
        if (!(message instanceof JSONObject)) {
            return false;
        }

        JSONObject update = (JSONObject) message;
        try {
            String uuid = update.getString("uuid");
            String action = update.getString("action");
            return homeUuid.equals(uuid) && "join".equals(action);
        } catch (JSONException e) {
            Log.e(PubnubService.TAG, "Malformed presence message", e);
            return false;
        }
    }

    public static boolean isHomeDisconnected(Object message, String homeUuid) {
        if (!(message instanceof JSONObject)) {
            return false;
        }

        JSONObject update = (JSONObject) message;
        try {
            String uuid = update.getString("uuid");
            String action = update.getString("action");
            return homeUuid.equals(uuid) && ("leave".equals(action) || "timeout".equals(action));
        } catch (JSONException e) {
            Log.e(PubnubService.TAG, "Malformed presence message", e);
            return false;
        }
    }

    public static boolean isHomeHere(Object message, String homeUuid) {
        if (!(message instanceof JSONObject)) {
            return false;
        }

        JSONObject update = (JSONObject) message;

        JSONArray uuids = null;
        try {
            uuids = update.getJSONArray("uuids");
            for (int i = 0; i < uuids.length(); i++) {
                if (homeUuid.equals(uuids.getString(i))) {
                    return true;
                }
            }
        } catch (JSONException e) {
            Log.e(PubnubService.TAG, "Malformed 'Here now' message", e);
            return false;
        }

        return false;
    }

    public static JSONObject newPictureRequest() {
        JSONObject request = new JSONObject();
        try {
            request.put("action", "new_picture_request");
        } catch (JSONException e) {
            Log.e(PubnubService.TAG, "Cannot create new picture request", e);
        }

        return request;
    }
}
