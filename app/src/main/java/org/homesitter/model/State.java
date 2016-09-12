package org.homesitter.model;

import android.support.annotation.Nullable;

import org.homesitter.service.PubnubService;

import java.io.Serializable;

/**
 * Created by mtkachenko on 12/09/16.
 */
public class State implements Serializable {
    public final PubnubService.ConnectionState connectionState;
    public final PubnubService.ConnectionState homeConnectionState;
    @Nullable
    public final Picture lastPicture;

    public State(PubnubService.ConnectionState connectionState, PubnubService.ConnectionState homeConnectionState, @Nullable Picture lastPicture) {
        this.connectionState = connectionState;
        this.homeConnectionState = homeConnectionState;
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

        return this.connectionState == other.connectionState
                && this.homeConnectionState == other.homeConnectionState
                && pictureMatches;
    }

    @Override
    public int hashCode() {
        int code = 31 + 11 * connectionState.hashCode();
        code = code + 13 * homeConnectionState.hashCode();
        code = code + (lastPicture == null ? 0 : lastPicture.hashCode());

        return code;
    }
}
