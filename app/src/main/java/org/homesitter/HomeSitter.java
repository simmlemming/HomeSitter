package org.homesitter;

import android.app.Application;
import android.content.SharedPreferences;

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
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public HomeSitterSettings getSettings() {
        return new HomeSitterSettings();
    }

    public Storage getStorage() {
        return new Storage(this);
    }

    public class HomeSitterSettings {
        private static final String PICTURES_INTERVAL = "pictures_interval";

        private HomeSitterSettings() {

        }

        // This value must be in sync with server (currently it's also hardcoded on server)
        public long getPicturesIntervalMs() {
            return getPreferences().getLong(PICTURES_INTERVAL, DEFAULT_PICTURES_INTERVAL_MS);
        }

        private SharedPreferences getPreferences() {
            return getSharedPreferences("homesitter", MODE_PRIVATE);
        }
    }
}
