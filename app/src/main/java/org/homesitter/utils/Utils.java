package org.homesitter.utils;

import org.homesitter.R;
import org.homesitter.model.Connectivity;

/**
 * Created by mtkachenko on 16/09/16.
 */
public class Utils {
    public static int[] connectivityToUiParams(Connectivity connectivity) {
        switch (connectivity.self) {
            case DISCONNECTED:
                return new int[]{R.color.error, R.string.status_disconnected};

            case CONNECTED:
                return forHomeState(connectivity.home);

            default:
            case UNKNOWN:
                return new int[]{R.color.neutral, R.string.status_unknown};
        }
    }

    private static int[] forHomeState(Connectivity.State homeState) {
        switch (homeState) {
            case DISCONNECTED:
                return new int[]{R.color.error, R.string.status_home_disconnected};

            case CONNECTED:
                return new int[]{R.color.ok, R.string.status_ok};

            default:
            case UNKNOWN:
                return new int[]{R.color.neutral, R.string.status_unknown};
        }
    }
}
