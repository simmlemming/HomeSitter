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
    private long seekTime;

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
        return ViewModel.withValues(lastLivePicture, 0, new Connectivity(Connectivity.State.CONNECTING, Connectivity.State.DISCONNECTED), pictureRequestIsInProgress);
    }

    public ViewModel requestCurrentState(PubnubService service) {
        service.requestConnectivityStateRefresh();
        Connectivity connectivity = service.getCurrentConnectivity();
        return ViewModel.withValues(lastLivePicture, 0, connectivity, pictureRequestIsInProgress);
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
        ViewModel viewModel = ViewModel.withValues(lastLivePicture, 0, service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.NewLivePictureReceivedEvent event) {
        pictureRequestIsInProgress = false;
        lastLivePicture = event.picture;
        ViewModel viewModel = ViewModel.withValues(lastLivePicture, 0, event.service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.LivePictureRequestFailedEvent event) {
        pictureRequestIsInProgress = false;
        lastLivePicture = null;
        ViewModel viewModel = ViewModel.withValues(null, 0, event.service.getCurrentConnectivity(), pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.ConnectivityChangedEvent event) {
        ViewModel viewModel = ViewModel.withValues(lastLivePicture, 0, event.connectivity, pictureRequestIsInProgress);
        application.getEventBus().post(viewModel);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(PubnubService.GeneralFailureEvent event) {
        ViewModel viewModel = ViewModel.withValues(lastLivePicture, 0, event.service.getCurrentConnectivity(), pictureRequestIsInProgress);
        viewModel.userFriendlyErrorMessage = event.userFriendlyErrorMessage;
        application.getEventBus().post(viewModel);
    }
}
