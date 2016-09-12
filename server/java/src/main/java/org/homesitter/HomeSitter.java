package org.homesitter;

import org.homesitter.model.Messages;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class HomeSitter {
    private static final int DEFAULT_PICTURE_INTERVAL_MS = 5 * 1000 * 60; // 5 min

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
                    sendNewPicture(pubnub, pictureTaker, copier, files);
                }
            }
        });

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendNewPicture(pubnub, pictureTaker, copier, files);
            }
        }, 0, DEFAULT_PICTURE_INTERVAL_MS);
    }

    private static void sendNewPicture(Pubnub pubnub, PictureTaker pictureTaker, Copier copier, Files files) {
        try {
            String fileName = pictureTaker.takePicture();
            copier.scp(fileName);
            files.deleteFile(fileName);

            pubnub.publish(Keys.MAIN_CHANNEL, Messages.newPicture(fileName), new BasePubnubCallback("publish"));
        } catch (Copier.CannotCopyException e) {
            Log.e(e);
        } catch (PictureTaker.CannotTakePictureException e) {
            Log.e(e);
        } catch (PubnubException e) {
            Log.e(e);
        }
    }
}
