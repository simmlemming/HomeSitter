package org.homesitter;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.homesitter.model.Picture;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by mtkachenko on 12/09/16.
 */
@RunWith(AndroidJUnit4.class)
public class StorageTest {

    @Test
    public void restorePicture_withPicture() {
        Picture pic = new Picture("http://last.pic/wwefw.jpg", 987293782762L, 1);

        Storage storage = getStorage();

        storage.savePicture(pic, 1);
        Picture restoredPic = storage.restorePicture(1);

        assertEquals(pic, restoredPic);
    }

    @Test
    public void restorePicture_withoutPicture() {
        Storage storage = getStorage();

        storage.savePicture(null, 0);
        Picture restoredPic = storage.restorePicture(0);

        assertNull(restoredPic);
    }

    @NonNull
    private Storage getStorage() {
        return new Storage(InstrumentationRegistry.getTargetContext());
    }
}
