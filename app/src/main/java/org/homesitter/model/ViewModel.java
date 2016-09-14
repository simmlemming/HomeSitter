package org.homesitter.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.homesitter.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mtkachenko on 14/09/16.
 */
public class ViewModel {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d',' HH:mm:ss");

    public final Picture picture;
    public final String timeText;
    public final int stateColorResId, stateTextResId;
    public final boolean takePictureButtonEnabled;

    public String userFriendlyErrorMessage;


    public static ViewModel withValues(@Nullable Picture picture, long timeMs, Connectivity connectivity, boolean takePictureButtonEnabled) {
        if (picture == null) {
            return new ViewModel(timeMs, connectivity, takePictureButtonEnabled);
        } else {
            return new ViewModel(picture, connectivity, takePictureButtonEnabled);
        }
    }

    private ViewModel(@NonNull Picture picture, Connectivity connectivity, boolean takePictureButtonEnabled) {
        this.picture = picture;
        this.timeText = DATE_FORMAT.format(new Date(picture.timeMs));
        this.takePictureButtonEnabled = takePictureButtonEnabled;

        int[] state = stateParams(connectivity);
        stateColorResId = state[0];
        stateTextResId = state[1];
    }

    private ViewModel(long timeMs, Connectivity connectivity, boolean takePictureButtonEnabled) {
        this.picture = null;
        this.timeText = DATE_FORMAT.format(new Date(timeMs));
        this.takePictureButtonEnabled = takePictureButtonEnabled;

        int[] state = stateParams(connectivity);
        stateColorResId = state[0];
        stateTextResId = state[1];
    }

    private int[] stateParams(Connectivity connectivity) {
        switch (connectivity.self) {
            case DISCONNECTED:
                return new int[] {R.color.error, R.string.status_disconnected};

            case CONNECTED:
                return forHomeState(connectivity.home);

            default:
            case CONNECTING:
                return new int[] {R.color.neutral, R.string.status_connecting};
        }
    }

    private int[] forHomeState(Connectivity.State homeState) {
        switch (homeState) {
            case DISCONNECTED:
                return new int[] {R.color.error, R.string.status_home_disconnected};

            case CONNECTED:
                return new int[] {R.color.ok, R.string.status_ok};

            default:
            case CONNECTING:
                return new int[] {R.color.neutral, R.string.status_connecting};
        }
    }
}
