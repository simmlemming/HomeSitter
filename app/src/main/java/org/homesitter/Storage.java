package org.homesitter;

import android.content.Context;
import android.util.Log;

import org.homesitter.model.Picture;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by mtkachenko on 12/09/16.
 */
public class Storage {
    private final Context context;

    Storage(Context context) {
        this.context = context;
    }

    public void savePicture(Picture picture) {
        saveValue("last_picture", picture);
    }

    public Picture restorePicture() {
        return restoreValue("last_picture", Picture.class);
    }

    private <T extends Serializable> T restoreValue(String fileName, Class<T> clazz) {
        FileInputStream fis = null;
        ObjectInputStream is = null;
        T value = null;

        try {
            fis = context.openFileInput(fileName);
            is = new ObjectInputStream(fis);
            Object o = is.readObject();
            value = clazz.cast(o);
        } catch (IOException | ClassNotFoundException e) {
            value = null;
            Log.e(HomeSitter.TAG, null, e);
        } finally {
            closeStream(is);
            closeStream(fis);
        }

        return value;
    }

    private void saveValue(String fileName, Serializable value) {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;

        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);

            os.writeObject(value);

            os.flush();
            fos.flush();
        } catch (IOException e) {
            Log.e(HomeSitter.TAG, null, e);
        } finally {
            closeStream(os);
            closeStream(fos);
        }
    }

    private void closeStream(InputStream stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
        } catch (IOException e) {
            Log.e(HomeSitter.TAG, null, e);
        }
    }

    private void closeStream(OutputStream stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
        } catch (IOException e) {
            Log.e(HomeSitter.TAG, null, e);
        }
    }
}
