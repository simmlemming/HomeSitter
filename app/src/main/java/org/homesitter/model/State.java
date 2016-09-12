package org.homesitter.model;

import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by mtkachenko on 12/09/16.
 */
public class State implements Serializable {
    public final boolean isConnected;
    public final boolean isHomeConnected;
    @Nullable
    public final Picture lastPicture;

    public State(boolean isConnected, boolean isHomeConnected, @Nullable Picture lastPicture) {
        this.isConnected = isConnected;
        this.isHomeConnected = isHomeConnected;
        this.lastPicture = lastPicture;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof State)) {
            return false;
        }

        State other = (State) o;
        boolean pictureMatches = this.lastPicture == null
                ? other.lastPicture == null
                : this.lastPicture.equals(other.lastPicture);

        return this.isConnected == other.isConnected
                && this.isHomeConnected == other.isHomeConnected
                && pictureMatches;
    }

    @Override
    public int hashCode() {
        int code = 31 + (isConnected ? 11 : 13);
        code = code + (isHomeConnected ? 17 : 31);
        code = code + (lastPicture == null ? 0 : lastPicture.hashCode());

        return code;
    }
}
