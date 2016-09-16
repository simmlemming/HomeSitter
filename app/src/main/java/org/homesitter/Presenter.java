package org.homesitter;

import org.homesitter.model.Picture;
import org.homesitter.model.ViewModel;
import org.homesitter.service.PubnubService;

import java.util.Calendar;

/**
 * Created by mtkachenko on 14/09/16.
 */
public class Presenter {
    private final HomeSitter application;
    private Picture[] lastLivePictures;
    private Picture[] lastSeekPictures;

    private long seekTimeMs;
    private int cameraIndex;

    private ViewModel viewModel;

    public Presenter(HomeSitter application, int camerasCount) {
        this.application = application;
        lastLivePictures = new Picture[camerasCount];
        lastSeekPictures = new Picture[camerasCount];
        viewModel = new ViewModel(null, 0);
    }

    public void saveState() {
        for (int i = 0; i < lastLivePictures.length; i++) {
            application.getStorage().savePicture(lastLivePictures[i], i);
        }
    }

    public ViewModel restoreState() {
        for (int i = 0; i < lastLivePictures.length; i++) {
            lastLivePictures[i] = application.getStorage().restorePicture(i);
        }

        viewModel.setPictureAndTime(lastLivePictures[cameraIndex])
                .setCamIndex(cameraIndex);

        return viewModel;
    }

    public void onTakePictureClick(PubnubService service) {
        if (isInSeekingMode()) {
            requestPictureAtSeekTime(service);
        } else {
            requestLivePicture(service);
        }
    }

    public void onCameraChange(PubnubService service, int cameraIndex) {
        if (this.cameraIndex == cameraIndex) {
            return;
        }

        this.cameraIndex = cameraIndex;

        if (isInSeekingMode()) {
            onCameraChangeInSeekMode(service, cameraIndex);
        } else {
            viewModel.setPictureAndTime(lastLivePictures[cameraIndex])
                    .setCamIndex(cameraIndex)
                    .notifyInvalidated(application);
        }
    }

    private void onCameraChangeInSeekMode(PubnubService service, int cameraIndex) {
        if (lastSeekPictures[cameraIndex] == null) {
            requestPictureAtSeekTime(service);
        } else {
            seekTimeMs = lastSeekPictures[cameraIndex].timeMs;
        }

        viewModel.setPictureAndTime(lastSeekPictures[cameraIndex], seekTimeMs)
                .setCamIndex(cameraIndex)
                .notifyInvalidated(application);
    }

    public void requestCurrentState(PubnubService service) {
        service.requestConnectivityStateRefresh();
    }

    public void requestLastPictureIfNeeded(PubnubService service) {
        HomeSitter.HomeSitterSettings settings = application.getSettings();

        long picturesIntervalMs = settings.getPicturesIntervalMs();
        long currentTimeMs = Calendar.getInstance().getTimeInMillis();

        boolean haveRecentPicture = (lastLivePictures[cameraIndex] != null) // Have last picture
                && (currentTimeMs - lastLivePictures[cameraIndex].timeMs) < picturesIntervalMs; // It's recent

        if (!haveRecentPicture) {
            service.requestLastPicture(cameraIndex);
            viewModel.setTakePictureButtonEnabled(false)
                    .notifyInvalidated(application);
        }
    }

    public void requestLivePicture(PubnubService service) {
        service.requestLivePicture(cameraIndex);

        viewModel.setTakePictureButtonEnabled(false)
                .notifyInvalidated(application);
    }

    public void requestPictureAtSeekTime(PubnubService service) {
        service.requestPictureAt(cameraIndex, seekTimeMs);

        viewModel.setTakePictureButtonEnabled(false)
                .notifyInvalidated(application);
    }

    public void enterSeekingMode() {
        lastSeekPictures[cameraIndex] = lastLivePictures[cameraIndex];
        seekTimeMs = lastLivePictures[cameraIndex] == null ? 0 : lastLivePictures[cameraIndex].timeMs;
    }

    public void exitSeekingMode(PubnubService service) {
        seekTimeMs = 0;
        viewModel.setPictureAndTime(lastLivePictures[cameraIndex])
                .notifyInvalidated(application);

        requestLastPictureIfNeeded(service);
    }

    public void seekTo(long timeMs) {
        seekTimeMs = timeMs;

        viewModel.setTimeText(timeMs)
                .notifyInvalidated(application);
    }

    public void seekBy(long deltaMs) {
        seekTo(seekTimeMs + deltaMs);
    }

    private boolean isInSeekingMode() {
        return seekTimeMs != 0;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.NewLivePictureReceivedEvent event) {
        if (isInSeekingMode()) {
            return;
        }

        lastLivePictures[event.picture.cameraIndex] = event.picture;

        if (cameraIndex == event.picture.cameraIndex) {
            viewModel.setPictureAndTime(event.picture)
                    .setTakePictureButtonEnabled(true)
                    .notifyInvalidated(application);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.LivePictureRequestFailedEvent event) {
        if (isInSeekingMode()) {
            return;
        }

        lastLivePictures[event.cameraIndex] = null;

        if (cameraIndex == event.cameraIndex) {
            viewModel.setPictureAndTime(null)
                    .setTakePictureButtonEnabled(true)
                    .setUserFriendlyErrorMessage(event.userFriendlyErrorMessage)
                    .notifyInvalidated(application);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.ConnectivityChangedEvent event) {
        viewModel.setConnectivity(event.connectivity)
                .notifyInvalidated(application);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.GeneralFailureEvent event) {
        viewModel.setUserFriendlyErrorMessage(event.userFriendlyErrorMessage)
                .notifyInvalidated(application);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.PictureAtGivenTimeReceivedEvent event) {
        lastSeekPictures[event.cameraIndex] = event.picture;

        if (cameraIndex == event.cameraIndex) {
            seekTimeMs = event.picture == null ? seekTimeMs : event.picture.timeMs;
            viewModel.setPictureAndTime(event.picture, seekTimeMs)
                    .setTakePictureButtonEnabled(true)
                    .notifyInvalidated(application);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.PictureAtGivenTimeRequestFailedEvent event) {
        lastSeekPictures[event.cameraIndex] = null;
        if (cameraIndex == event.cameraIndex) {
            viewModel.setPictureAndTime(null, seekTimeMs)
                    .notifyInvalidated(application);
        }
    }
}
