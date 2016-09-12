package org.homesitter.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.homesitter.HomeSitter;
import org.homesitter.Messages;
import org.homesitter.R;
import org.homesitter.model.Picture;
import org.homesitter.model.State;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by mtkachenko on 24/11/15.
 */
public class PubnubService extends Service {
    public static final String TAG = HomeSitter.TAG;

    private Pubnub pubnub;
    private String homeUuid, mainChannelId;

    private boolean isConnected = false;
    private boolean isHomeConnected = false;
    private Picture lastPicture;

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

        State lastState = getApplicationContext().getStorage().getState();
        if (lastState != null) {
            lastPicture = lastState.lastPicture;
        }

        subscribe(mainChannelId);
        requestCheckIsHomeConnected();
        subscribeForPresence();
        requestLastPictureIfNeeded();
    }

    public void requestLastPictureIfNeeded() {
        HomeSitter.HomeSitterSettings settings = getApplicationContext().getSettings();

        long picturesIntervalMs = settings.getPicturesIntervalMs();
        long currentTimeMs = Calendar.getInstance().getTimeInMillis();

        boolean haveRecentPicture = (lastPicture != null) // Have last picture
                                    && (currentTimeMs - lastPicture.timeMs) < picturesIntervalMs; // It's recent

        if (!haveRecentPicture) {
            pubnub.history(mainChannelId, false, new HistoryCallback(this));
        }
    }

    public void requestNewPicture() {
        JSONObject message = Messages.newPictureRequest();
        pubnub.publish(mainChannelId, message, new BasePubnubCallback(this) {
            @Override
            public void errorCallback(String channel, PubnubError error) {
                super.errorCallback(channel, error);
                notifyStateChanged("Cannot request new picture: " + error.getErrorString());
            }
        });
    }

    public void onNewPicture(Picture picture) {
        lastPicture = picture;
        getApplicationContext().getStorage().putState(currentState());

        notifyStateChanged(null);
    }

    private void subscribeForPresence() {
        try {
            pubnub.presence(mainChannelId, new PresenceCallback(this));
        } catch (PubnubException e) {
            notifyStateChanged(e.getMessage());
        }
    }

    private void subscribe(String mainChannelId) {
        try {
            pubnub.subscribe(mainChannelId, new SubscribeCallback(this));
        } catch (PubnubException e) {
            notifyStateChanged(e.getMessage());
        }
    }

    private void requestCheckIsHomeConnected() {
        pubnub.hereNow(mainChannelId, false, true, new HomeConnectedCallback(this));
    }

    public void requestRefreshState() {
        requestCheckIsHomeConnected();
        notifyStateChanged(null);
    }

    private State currentState() {
        return new State(isConnected, isHomeConnected, lastPicture);
    }

    void setConnected(boolean connected) {
        boolean stateChanged = isConnected == connected;
        isConnected = connected;

        if (stateChanged) {
            notifyStateChanged(null);
        }
    }

    void setHomeConnected(boolean connected) {
        boolean stateChanged = (isHomeConnected != connected);
        isHomeConnected = connected;

        if (stateChanged) {
            notifyStateChanged(null);
        }
    }

    void notifyStateChanged(String userFriendlyMessage) {
        StateUpdatedEvent event = new StateUpdatedEvent(currentState(), userFriendlyMessage);
        getApplicationContext().getEventBus().post(event);
    }

    @Override
    public HomeSitter getApplicationContext() {
        return (HomeSitter) super.getApplicationContext();
    }

    String getHomeUuid() {
        return homeUuid;
    }

    @Override
    public void onDestroy() {
        pubnub.unsubscribe(mainChannelId);
        pubnub.unsubscribePresence(mainChannelId);
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
