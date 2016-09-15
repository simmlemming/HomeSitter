package org.homesitter;

import org.homesitter.model.Messages;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;
import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class HomeSitter {
    private static final int DEFAULT_PICTURE_INTERVAL_MS = 5 * 1000 * 60; // 5 min
    private static final int CAMERAS_COUNT = 2;

    public static void main(String[] args) throws PubnubException {
        final Pubnub pubnub = new Pubnub(Keys.PUB_KEY, Keys.SUB_KEY, true);
        pubnub.setUUID("home");

        final PictureTaker pictureTaker = new PictureTaker();
        final Copier copier = new Copier();
        Timer timer = new Timer();
        final Files files = new Files();

        pubnub.subscribe(Keys.MAIN_CHANNEL, new BasePubnubCallback("subscribe") {
            @Override
            public void successCallback(String channel, Object message) {
                super.successCallback(channel, message);
                if (Messages.isNewPictureRequest(message)) {
                    int cameraIndex = Messages.getCameraIndexFromNewPictureRequest(message);
                    sendNewPicture(pubnub, pictureTaker, copier, files, cameraIndex);
                }
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendNewPictures(pubnub, pictureTaker, copier, files);
            }
        }, 0, DEFAULT_PICTURE_INTERVAL_MS);
    }

    private static void sendNewPictures(Pubnub pubnub, PictureTaker pictureTaker, Copier copier, Files files) {
        for (int i = 0; i < CAMERAS_COUNT; i++) {
            sendNewPicture(pubnub, pictureTaker, copier, files, i);
        }
    }

    private static void sendNewPicture(Pubnub pubnub, PictureTaker pictureTaker, Copier copier, Files files, int cameraIndex) {
        try {
            String fileName = pictureTaker.takePicture(cameraIndex);
            copier.scp(fileName);
            files.deleteFile(fileName);

            pubnub.publish(Keys.MAIN_CHANNEL, Messages.newPicture(fileName), new BasePubnubCallback("publish"));
        } catch (Copier.CannotCopyException e) {
            Log.e(e);
        } catch (PictureTaker.CannotTakePictureException e) {
            Log.e(e);
        } catch (PubnubException e) {
            Log.e(e);
        } catch (RuntimeException e) {
            Log.e(e); // To keep pubnub subscribed no matter what
        }
    }
}
