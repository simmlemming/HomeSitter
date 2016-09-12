package org.homesitter;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.homesitter.model.Picture;

import de.greenrobot.event.EventBus;

/**
 * Created by mtkachenko on 02/09/16.
 */
public class HomeSitter extends Application {
    private static final int DEFAULT_PICTURES_INTERVAL_MS = 5 * 60 * 1000;

    public static String TAG = "HomeSitter";
    private EventBus eventBus;

    @Override
    public void onCreate() {
        super.onCreate();
        eventBus = EventBus.getDefault();

//        startService(PubnubService.intent(this));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public HomeSitterSettings getSettings() {
        return new HomeSitterSettings();
    }

    public class HomeSitterSettings {
        private static final String LAST_PICTURE_LINK = "last_picture_link";
        private static final String LAST_PICTURE_TIME = "last_picture_time";
        private static final String PICTURES_INTERVAL = "pictures_interval";

        private HomeSitterSettings() {

        }

        // This value must be in sync with server (currently it's also hardcoded on server)
        public long getPicturesIntervalMs() {
            return getPreferences().getLong(PICTURES_INTERVAL, DEFAULT_PICTURES_INTERVAL_MS);
        }

        public void putLastPicture(Picture picture) {
            getPreferences()
                    .edit()
                    .putString(LAST_PICTURE_LINK, picture.link)
                    .putLong(LAST_PICTURE_TIME, picture.timeMs)
                    .apply();
        }

        @Nullable
        public Picture getLastPicture() {
            SharedPreferences preferences = getPreferences();

            String link = preferences.getString(LAST_PICTURE_LINK, null);
            if (TextUtils.isEmpty(link)) {
                return null;
            }

            long time = preferences.getLong(LAST_PICTURE_TIME, 0);
            return new Picture(link, time);
        }

        private SharedPreferences getPreferences() {
            return getSharedPreferences("homesitter", MODE_PRIVATE);
        }
    }
}
