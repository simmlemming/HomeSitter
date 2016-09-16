package org.homesitter.model;

import android.support.annotation.Nullable;

import org.homesitter.HomeSitter;
import org.homesitter.R;
import org.homesitter.utils.Utils;

import java.text.SimpleDateFormat;

/**
 * Created by mtkachenko on 14/09/16.
 */
public class ViewModel {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd',' HH:mm:ss");
    private static final String ZERO_TIME_TEXT = "--- --, --:--:--";

    private Picture picture;
    private String timeText;
    private int stateColorResId, stateTextResId;
    private boolean takePictureButtonEnabled;
    private int camIndex;
    private long seekTime = 0;

    @Nullable
    private String userFriendlyErrorMessage;

    public ViewModel(@Nullable Picture picture, int camIndex) {
        this.picture = picture;
        this.camIndex = camIndex;
        setTimeText(picture == null ? 0 : picture.timeMs);
        takePictureButtonEnabled = true;
        this.stateColorResId = R.color.neutral;
        this.stateTextResId = R.string.status_unknown;
    }

    public Picture getPicture() {
        return picture;
    }

//    public ViewModel setPictureAndTime(Picture picture, long fallbackTimeMs) {
//        setPictureAndTime(picture);
//
//        if (picture == null) {
//            setTimeText(fallbackTimeMs);
//        }
//
//        return this;
//    }

    public ViewModel setPictureAndTime(Picture picture) {
        this.picture = picture;
        setTimeText(picture == null ? 0 : picture.timeMs);
        return this;
    }

    public ViewModel setConnectivity(Connectivity connectivity) {
        int[] params = Utils.connectivityToUiParams(connectivity);
        stateColorResId = params[0];
        stateTextResId = params[1];
        return this;
    }

    public int getStateColorResId() {
        return stateColorResId;
    }

    public int getStateTextResId() {
        return stateTextResId;
    }

    public boolean isTakePictureButtonEnabled() {
        return takePictureButtonEnabled;
    }

    public ViewModel setTakePictureButtonEnabled(boolean takePictureButtonEnabled) {
        this.takePictureButtonEnabled = takePictureButtonEnabled;
        return this;
    }

    public String getTimeText() {
        return timeText;
    }

    public ViewModel setTimeText(long timeMs) {
        this.timeText = timeMs == 0
                ? ZERO_TIME_TEXT
                : DATE_FORMAT.format(timeMs);

        return this;
    }

    public ViewModel setSeekTime(long seekTime) {
        this.seekTime = seekTime;
        return this;
    }

    public long getSeekTime() {
        return seekTime;
    }

    @Nullable
    public String getUserFriendlyErrorMessage() {
        return userFriendlyErrorMessage;
    }

    public ViewModel setUserFriendlyErrorMessage(@Nullable String userFriendlyErrorMessage) {
        this.userFriendlyErrorMessage = userFriendlyErrorMessage;
        return this;
    }

    public int getCamIndex() {
        return camIndex;
    }

    public ViewModel setCamIndex(int camIndex) {
        this.camIndex = camIndex;
        return this;
    }

    public void notifyInvalidated(HomeSitter application) {
        InvalidatedEvent event = new InvalidatedEvent(this);
        application.getEventBus().post(event);
    }

    public static class InvalidatedEvent {
        public final ViewModel viewModel;

        private InvalidatedEvent(ViewModel viewModel) {
            this.viewModel = viewModel;
        }
    }
}
