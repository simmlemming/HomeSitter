package org.homesitter;

import android.content.Context;
import android.util.Log;

import org.homesitter.model.State;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by mtkachenko on 12/09/16.
 */
public class Storage {
    private final Context context;

    Storage(Context context) {
        this.context = context;
    }

    public State getState() {
        FileInputStream fis = null;
        ObjectInputStream is = null;
        State state = null;

        try {
            fis = context.openFileInput("last_state");
            is = new ObjectInputStream(fis);
            state = (State) is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            state = null;
            Log.e(HomeSitter.TAG, null, e);
        } finally {
            closeStream(is);
            closeStream(fis);
        }

        return state;
    }

    public void putState(State state) {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;

        try {
            fos = context.openFileOutput("last_state", Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);

            os.writeObject(state);

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
