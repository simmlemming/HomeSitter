package org.homesitter;

import android.app.Application;

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

        startService(PubnubService.intent(this));
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
