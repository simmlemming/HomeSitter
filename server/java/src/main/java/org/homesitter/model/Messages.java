package org.homesitter.model;

import org.homesitter.Keys;
import com.pubnub.api.PubnubException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by mtkachenko on 11/09/16.
 */
public class Messages {

    public static int getCameraIndexFromNewPictureRequest(Object message) {
        if (!(message instanceof JSONObject)) {
            return 0;
        }

        return ((JSONObject)message).optInt("camera_index", 0);
    }

    public static boolean isNewPictureRequest(Object message) {
        if (!(message instanceof JSONObject)) {
            return false;
        }

        String action = ((JSONObject) message).optString("action");
        return "new_picture_request".equals(action);
    }

    public static JSONObject newPicture(String fileName) throws PubnubException {
        JSONObject message = new JSONObject();
        try {
            message.put("action", "new_picture");
            message.put("link", "http://" + Keys.REMOTE_IP + "/p/" + fileName);
            message.put("time", System.currentTimeMillis());
            return message;
        } catch (JSONException e) {
            throw new PubnubException("Cannot create 'New picture' message");
        }
    }
}
