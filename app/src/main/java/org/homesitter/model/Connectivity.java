package org.homesitter.model;

/**
 * Created by mtkachenko on 14/09/16.
 */
public class Connectivity {
    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED;

        public static State fromBoolean(boolean connected) {
            return connected ? CONNECTED : DISCONNECTED;
        }
    }

    public final State self, home;

    public Connectivity(State self, State home) {
        this.self = self;
        this.home = home;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Connectivity)) {
            return false;
        }

        Connectivity other = (Connectivity) o;
        return self == other.self && home == other.home;
    }

    @Override
    public int hashCode() {
        return self.hashCode() + home.hashCode() * 11;
    }
}
