package org.homesitter;

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
    private Picture lastLivePicture;
    private Picture lastSeekPicture;

    private long seekTimeMs;

    public Presenter(HomeSitter application) {
        this.application = application;
    }

    public void saveState() {
        if (lastLivePicture != null) {
            application.getStorage().savePicture(lastLivePicture);
        }
    }

    public ViewModel restoreState() {
        lastLivePicture = application.getStorage().restorePicture();
        return ViewModel.withTimeFromPicture(lastLivePicture, new Connectivity(Connectivity.State.UNKNOWN, Connectivity.State.UNKNOWN), pictureRequestIsInProgress);
    }

    public void onTakePictureClick(PubnubService service) {
        if (isInSeekingMode()) {
            requestPictureAtSeekTime(service);
        } else {
            requestLivePicture(service);
        }
    }

    public ViewModel requestCurrentState(PubnubService service) {
        service.requestConnectivityStateRefresh();
        Connectivity connectivity = service.getCurrentConnectivity();
        return ViewModel.withTimeFromPicture(lastLivePicture, connectivity, pictureRequestIsInProgress);
    }

    public void requestLastPictureIfNeeded(PubnubService service) {
        HomeSitter.HomeSitterSettings settings = application.getSettings();

        long picturesIntervalMs = settings.getPicturesIntervalMs();
        long currentTimeMs = Calendar.getInstance().getTimeInMillis();

        boolean haveRecentPicture = (lastLivePicture != null) // Have last picture
                && (currentTimeMs - lastLivePicture.timeMs) < picturesIntervalMs; // It's recent

        if (!haveRecentPicture) {
            pictureRequestIsInProgress = true;
            service.requestLastPicture();
        }
    }

    public void requestLivePicture(PubnubService service) {
        pictureRequestIsInProgress = true;
        service.requestLivePicture();
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePicture, service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    public void requestPictureAtSeekTime(PubnubService service) {
        pictureRequestIsInProgress = true;
        service.requestPictureAt(seekTimeMs);

        ViewModel viewModel = ViewModel.withGivenTime(lastSeekPicture, seekTimeMs, service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    public void enterSeekingMode() {
        lastSeekPicture = lastLivePicture;
        seekTimeMs = lastLivePicture == null ? 0 : lastLivePicture.timeMs;
    }

    public void exitSeekingMode(PubnubService service) {
        seekTimeMs = 0;
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePicture, service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    public void seekTo(PubnubService service, long timeMs) {
        seekTimeMs = timeMs;

        ViewModel viewModel = ViewModel.withGivenTime(lastSeekPicture, seekTimeMs, service.getCurrentConnectivity(), pictureRequestIsInProgress);
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
        lastLivePicture = event.picture;
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePicture, event.service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.LivePictureRequestFailedEvent event) {
        if (isInSeekingMode()) {
            return;
        }

        pictureRequestIsInProgress = false;
        lastLivePicture = null;
        ViewModel viewModel = ViewModel.withTimeFromPicture(null, event.service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.ConnectivityChangedEvent event) {
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePicture, event.connectivity, pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.GeneralFailureEvent event) {
        ViewModel viewModel = ViewModel.withTimeFromPicture(lastLivePicture, event.service.getCurrentConnectivity(), pictureRequestIsInProgress);
        viewModel.userFriendlyErrorMessage = event.userFriendlyErrorMessage;
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.PictureAtGivenTimeReceivedEvent event) {
        pictureRequestIsInProgress = false;
        lastSeekPicture = event.picture;
        if (event.picture != null) {
            seekTimeMs = event.picture.timeMs;
        }

        ViewModel viewModel = ViewModel.withTimeFromPicture(event.picture, event.service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.PictureAtGivenTimeRequestFailedEvent event) {
        pictureRequestIsInProgress = false;
        lastSeekPicture = null;
        ViewModel viewModel = ViewModel.withTimeFromPicture(null, event.service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }
}
