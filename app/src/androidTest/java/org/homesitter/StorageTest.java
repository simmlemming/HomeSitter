package org.homesitter;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.homesitter.model.Picture;
import org.homesitter.model.State;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.homesitter.service.PubnubService.ConnectionState.CONNECTED;
import static org.homesitter.service.PubnubService.ConnectionState.DISCONNECTED;
import static org.junit.Assert.assertEquals;

/**
 * Created by mtkachenko on 12/09/16.
 */
@RunWith(AndroidJUnit4.class)
public class StorageTest {

    @Test
    public void getState_withPicture() {
        Picture pic = new Picture("http://last.pic/wwefw.jpg", 987293782762L);
        State state = new State(CONNECTED, DISCONNECTED, pic);

        Storage storage = getStorage();

        storage.putState(state);
        State restoredState = storage.getState();

        assertEquals(state, restoredState);
    }

    @Test
    public void getState_withoutPicture() {
        State state = new State(CONNECTED, DISCONNECTED, null);

        Storage storage = getStorage();

        storage.putState(state);
        State restoredState = storage.getState();

        assertEquals(state, restoredState);
    }

    @NonNull
    private Storage getStorage() {
        return new Storage(InstrumentationRegistry.getTargetContext());
    }
}
