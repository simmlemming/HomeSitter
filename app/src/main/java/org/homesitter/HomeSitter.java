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

        public static final String LAST_PICTURE_LINK = "last_picture_link";
        public static final String LAST_PICTURE_TIME = "last_picture_time";

        private HomeSitterSettings() {

        }

        public String getString(String key, String defaultValue) {
            return getPreferences().getString(key, defaultValue);
        }

        public void putLastPicture(Picture picture) {
            getPreferences()
                    .edit()
                    .putString(LAST_PICTURE_LINK, picture.link)
                    .putLong(LAST_PICTURE_TIME, picture.time)
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

        public void put(String key, String value) {
            getPreferences()
                    .edit()
                    .putString(key, value)
                    .apply();
        }

        private SharedPreferences getPreferences() {
            return getSharedPreferences("homesitter", MODE_PRIVATE);
        }
    }
}
