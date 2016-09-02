package org.homesitter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mtkachenko on 24/11/15.
 */
public class PubnubService extends Service {
    private static final String TAG = HomeSitter.TAG;

    private Pubnub pubnub;
    private String homeUuid, mainChannelId;

    private boolean isConnected = false;
    private boolean isHomeConnected = false;

    public static class State {
        public final boolean isConnected;
        public final boolean isHomeConnected;

        public State(boolean isConnected, boolean isHomeConnected) {
            this.isConnected = isConnected;
            this.isHomeConnected = isHomeConnected;
        }
    }

    private State getState() {
        return new State(isConnected, isHomeConnected);
    }

    public static Intent intent(Context context) {
        return new Intent(context, PubnubService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        homeUuid = getResources().getString(R.string.home_uuid);

        String pubKey = getResources().getString(R.string.pub_key);
        String subKey = getResources().getString(R.string.sub_key);
        mainChannelId = getResources().getString(R.string.main_channel_id);

        pubnub = new Pubnub(pubKey, subKey, true);
        pubnub.setUUID("client_1");

        subscribe(mainChannelId);
    }

    private void subscribe(String mainChannelId) {
        try {
            pubnub.subscribe(mainChannelId, new Callback() {

                @Override
                public void connectCallback(String channel, Object message) {
                    isConnected = true;
                    notifyStateChanged(String.valueOf(message));
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    isConnected = false;
                    notifyStateChanged(error.getErrorString());
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    isConnected = false;
                }

                @Override
                public void reconnectCallback(String channel, Object message) {
                    isConnected = true;
                }
            });
        } catch (PubnubException e) {
            notifyStateChanged(e.getMessage());
        }
    }

    private void notifyStateChanged(String userFriendlyMessage) {
        StateUpdatedEvent event = new StateUpdatedEvent(getState(), userFriendlyMessage);
        getApplicationContext().getEventBus().post(event);
    }

    @Override
    public HomeSitter getApplicationContext() {
        return (HomeSitter) super.getApplicationContext();
    }

    private boolean isHomeOnline(Object message) throws JSONException {
        if (!(message instanceof JSONObject)) {
            throw new JSONException("Not JSON");
        }

        JSONObject update = (JSONObject) message;
        JSONArray uuids = update.getJSONArray("uuids");

        for (int i = 0; i < uuids.length(); i++) {
            if (homeUuid.equals(uuids.getString(i))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDestroy() {
        pubnub.unsubscribe(getResources().getString(R.string.main_channel_id));
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {

        public PubnubService service() {
            return PubnubService.this;
        }
    }

    public static class StateUpdatedEvent {
        public final String userFriendlyMessage;
        public final State state;


        public StateUpdatedEvent(State state, String userFriendlyMessage) {
            this.userFriendlyMessage = userFriendlyMessage;
            this.state = state;
        }
    }
}
