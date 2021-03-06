package org.homesitter.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.homesitter.HomeSitter;
import org.homesitter.Messages;
import org.homesitter.R;
import org.homesitter.model.Connectivity;
import org.homesitter.model.Picture;
import org.json.JSONObject;

/**
 * Created by mtkachenko on 24/11/15.
 */
public class PubnubService extends Service {
    public static final String TAG = HomeSitter.TAG;

    private Pubnub pubnub;
    private String homeUuid, mainChannelId;

    private Connectivity connectivity = new Connectivity(Connectivity.State.UNKNOWN, Connectivity.State.UNKNOWN);

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
        requestCheckIsHomeConnected();
        subscribeForPresence();
    }

    public void requestLastPicture(final int cameraIndex) {
        requestPictureAt(System.currentTimeMillis(), new HistoryCallback(this, cameraIndex) {
            @Override
            protected void onSuccess(Picture picture) {
                onLivePictureReceived(picture);
            }

            @Override
            protected void onError(String userFriendlyMessage) {
                notifyNewPictureRequestFailed(cameraIndex, userFriendlyMessage);
            }
        });
    }

    public void requestPictureAt(final int cameraIndex, long timeMs) {
        requestPictureAt(timeMs, new HistoryCallback(this, cameraIndex) {
            @Override
            protected void onSuccess(Picture picture) {
                PictureAtGivenTimeReceivedEvent event = new PictureAtGivenTimeReceivedEvent(picture, cameraIndex);
                getApplicationContext().getEventBus().post(event);
            }

            @Override
            protected void onError(String userFriendlyMessage) {
                PictureAtGivenTimeRequestFailedEvent event = new PictureAtGivenTimeRequestFailedEvent(cameraIndex, userFriendlyMessage);
                getApplicationContext().getEventBus().post(event);
            }
        });
    }

    private void requestPictureAt(long timeMs, Callback callback) {
        pubnub.history(mainChannelId,
                timeMs * 10000, // start time
                -1L, // end time
                20, // page size
                false, // reverse
                false, // include timetoken
                callback);
    }

    public void requestLivePicture(final int cameraIndex) {
        JSONObject message = Messages.newPictureRequest(cameraIndex);
        pubnub.publish(mainChannelId, message, new BasePubnubCallback(this) {
            @Override
            public void errorCallback(String channel, PubnubError error) {
                super.errorCallback(channel, error);
                notifyNewPictureRequestFailed(cameraIndex, error.getErrorString());
            }
        });
    }

    public void onLivePictureReceived(@NonNull Picture picture) {
        NewLivePictureReceivedEvent event = new NewLivePictureReceivedEvent(picture);
        getApplicationContext().getEventBus().post(event);
    }

    private void subscribeForPresence() {
        try {
            pubnub.presence(mainChannelId, new PresenceCallback(this));
        } catch (PubnubException e) {
            notifyGeneralFail(e.getMessage());
        }
    }

    private void subscribe(String mainChannelId) {
        try {
            pubnub.subscribe(mainChannelId, new SubscribeCallback(this));
        } catch (PubnubException e) {
            notifyGeneralFail(e.getMessage());
        }
    }

    private void requestCheckIsHomeConnected() {
        pubnub.hereNow(mainChannelId, false, true, new HomeConnectedCallback(this));
    }

    public void requestConnectivityStateRefresh() {
        requestCheckIsHomeConnected();
    }

    void setConnected(boolean connected) {
        Connectivity newConnectivity = new Connectivity(Connectivity.State.fromBoolean(connected), this.connectivity.home);
        setAndNotifyIfChanged(newConnectivity);
    }

    void setHomeConnected(boolean connected) {
        Connectivity newConnectivity = new Connectivity(connectivity.self, Connectivity.State.fromBoolean(connected));
        setAndNotifyIfChanged(newConnectivity);
    }

    private void setAndNotifyIfChanged(Connectivity newConnectivity) {
        boolean stateChanged = !connectivity.equals(newConnectivity);

        connectivity = newConnectivity;

        if (stateChanged) {
            ConnectivityChangedEvent event = new ConnectivityChangedEvent(connectivity);
            getApplicationContext().getEventBus().post(event);
        }
    }

    private void notifyGeneralFail(String userFriendlyErrorMessage) {
        GeneralFailureEvent event = new GeneralFailureEvent(userFriendlyErrorMessage);
        getApplicationContext().getEventBus().post(event);
    }

    private void notifyNewPictureRequestFailed(int cameraIndex, String userFriendlyErrorMessage) {
        LivePictureRequestFailedEvent event = new LivePictureRequestFailedEvent(cameraIndex, userFriendlyErrorMessage);
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

    public class NewLivePictureReceivedEvent {
        public final PubnubService service;
        public final Picture picture;

        NewLivePictureReceivedEvent(Picture picture) {
            this.service = PubnubService.this;
            this.picture = picture;
        }
    }

    public class PictureAtGivenTimeReceivedEvent {
        @Nullable
        public final Picture picture;
        public final PubnubService service;
        public final int cameraIndex; // For cases when picture is null

        PictureAtGivenTimeReceivedEvent(@Nullable Picture picture, int cameraIndex) {
            this.picture = picture;
            this.cameraIndex = cameraIndex;
            service = PubnubService.this;
        }
    }

    public class PictureAtGivenTimeRequestFailedEvent extends RequestFailedEvent {
        public final int cameraIndex;

        PictureAtGivenTimeRequestFailedEvent(int cameraIndex, String userFriendlyErrorMessage) {
            super(userFriendlyErrorMessage);
            this.cameraIndex = cameraIndex;
        }
    }

    public abstract class RequestFailedEvent {
        public final String userFriendlyErrorMessage;

        RequestFailedEvent(String userFriendlyErrorMessage) {
            this.userFriendlyErrorMessage = userFriendlyErrorMessage;
        }
    }

    public class GeneralFailureEvent extends RequestFailedEvent {
        GeneralFailureEvent(String userFriendlyErrorMessage) {
            super(userFriendlyErrorMessage);
        }
    }

    public class LivePictureRequestFailedEvent extends RequestFailedEvent {
        public final int cameraIndex;

        private LivePictureRequestFailedEvent(int cameraIndex, String userFriendlyErrorMessage) {
            super(userFriendlyErrorMessage);
            this.cameraIndex = cameraIndex;
        }
    }

    public static class ConnectivityChangedEvent {
        public final Connectivity connectivity;

        ConnectivityChangedEvent(Connectivity connectivity) {
            this.connectivity = connectivity;
        }
    }
}
