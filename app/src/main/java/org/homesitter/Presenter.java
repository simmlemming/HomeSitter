package org.homesitter;

import android.support.annotation.NonNull;

import org.homesitter.model.Connectivity;
import org.homesitter.model.Picture;
import org.homesitter.model.ViewModel;
import org.homesitter.service.PubnubService;

import java.util.Calendar;

/**
 * Created by mtkachenko on 14/09/16.
 */
public class Presenter {
    private final HomeSitter application;
    private boolean pictureRequestIsInProgress = false;
    private Picture[] lastLivePictures;
    private Picture[] lastSeekPictures;

    private long seekTimeMs;
    private int cameraIndex;

    public Presenter(HomeSitter application, int camerasCount) {
        this.application = application;
        lastLivePictures = new Picture[camerasCount];
        lastSeekPictures = new Picture[camerasCount];
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

        return ViewModel.withTimeFromPicture(lastLivePictures[cameraIndex], new Connectivity(Connectivity.State.UNKNOWN, Connectivity.State.UNKNOWN), pictureRequestIsInProgress, cameraIndex);
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
        ViewModel viewModel;
        if (isInSeekingMode()) {
            viewModel = onCameraChangeInSeekMode(service, cameraIndex);
        } else {
            viewModel = ViewModel.withTimeFromPicture(lastLivePictures[cameraIndex], service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
        }

        application.getEventBus().post(viewModel);
    }

    @NonNull
    private ViewModel onCameraChangeInSeekMode(PubnubService service, int cameraIndex) {
        if (lastSeekPictures[cameraIndex] == null) {
            requestPictureAtSeekTime(service);
        } else {
            seekTimeMs = lastSeekPictures[cameraIndex].timeMs;
        }

        return ViewModel.withGivenTime(lastSeekPictures[cameraIndex], seekTimeMs, service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
    }

    public ViewModel requestCurrentState(PubnubService service) {
        service.requestConnectivityStateRefresh();
        Connectivity connectivity = service.getCurrentConnectivity();
        return ViewModel.withTimeFromPicture(lastLivePictures[cameraIndex], connectivity, pictureRequestIsInProgress, cameraIndex);
    }

    public void requestLastPictureIfNeeded(PubnubService service) {
        HomeSitter.HomeSitterSettings settings = application.getSettings();

        long picturesIntervalMs = settings.getPicturesIntervalMs();
        long currentTimeMs = Calendar.getInstance().getTimeInMillis();

        boolean haveRecentPicture = (lastLivePictures[cameraIndex] != null) // Have last picture
                && (currentTimeMs - lastLivePictures[cameraIndex].timeMs) < picturesIntervalMs; // It's recent

        if (!haveRecentPicture) {
            pictureRequestIsInProgress = true;
            service.requestLastPicture(cameraIndex);
        }
    }

    public void requestLivePicture(PubnubService service) {
        pictureRequestIsInProgress = true;
        service.requestLivePicture(cameraIndex);
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePictures[cameraIndex], service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
        application.getEventBus().post(viewModel);
    }

    public void requestPictureAtSeekTime(PubnubService service) {
        pictureRequestIsInProgress = true;
        service.requestPictureAt(cameraIndex, seekTimeMs);

        ViewModel viewModel = ViewModel.withGivenTime(lastSeekPictures[cameraIndex], seekTimeMs, service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
        application.getEventBus().post(viewModel);
    }

    public void enterSeekingMode() {
        lastSeekPictures[cameraIndex] = lastLivePictures[cameraIndex];
        seekTimeMs = lastLivePictures[cameraIndex] == null ? 0 : lastLivePictures[cameraIndex].timeMs;
    }

    public void exitSeekingMode(PubnubService service) {
        seekTimeMs = 0;
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePictures[cameraIndex], service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
        application.getEventBus().post(viewModel);
    }

    public void seekTo(PubnubService service, long timeMs) {
        seekTimeMs = timeMs;

        ViewModel viewModel = ViewModel.withGivenTime(lastSeekPictures[cameraIndex], seekTimeMs, service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
        application.getEventBus().post(viewModel);
    }

    public void seekBy(PubnubService service, long deltaMs) {
        seekTo(service, seekTimeMs + deltaMs);
    }

    private boolean isInSeekingMode() {
        return seekTimeMs != 0;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.NewLivePictureReceivedEvent event) {
        if (isInSeekingMode()) {
            return;
        }

        pictureRequestIsInProgress = false;
        lastLivePictures[event.picture.cameraIndex] = event.picture;

        if (cameraIndex == event.picture.cameraIndex) {
            ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePictures[cameraIndex], event.service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
            application.getEventBus().post(viewModel);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.LivePictureRequestFailedEvent event) {
        if (isInSeekingMode()) {
            return;
        }

        pictureRequestIsInProgress = false;
        lastLivePictures[event.cameraIndex] = null;
        if (cameraIndex == event.cameraIndex) {
            ViewModel viewModel = ViewModel.withTimeFromPicture(null, event.service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
            application.getEventBus().post(viewModel);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.ConnectivityChangedEvent event) {
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePictures[cameraIndex], event.connectivity, pictureRequestIsInProgress, cameraIndex);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.GeneralFailureEvent event) {
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePictures[cameraIndex], event.service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
        viewModel.userFriendlyErrorMessage = event.userFriendlyErrorMessage;
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.PictureAtGivenTimeReceivedEvent event) {
        if (cameraIndex != event.cameraIndex) {
            return;
        }

        pictureRequestIsInProgress = false;
        lastSeekPictures[event.cameraIndex] = event.picture;

        ViewModel viewModel;
        if (event.picture == null) {
            viewModel = ViewModel.withGivenTime(null, seekTimeMs, event.service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
        } else {
            seekTimeMs = event.picture.timeMs;
            viewModel = ViewModel.withTimeFromPicture(event.picture, event.service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
        }
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.PictureAtGivenTimeRequestFailedEvent event) {
        pictureRequestIsInProgress = false;
        lastSeekPictures[event.cameraIndex] = null;
        if (cameraIndex == event.cameraIndex) {
            ViewModel viewModel = ViewModel.withTimeFromPicture(null, event.service.getCurrentConnectivity(), pictureRequestIsInProgress, cameraIndex);
            application.getEventBus().post(viewModel);
        }
    }
}
