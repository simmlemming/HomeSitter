package org.homesitter;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mtkachenko on 11/09/16.
 */
public class PictureTaker {
    public static class CannotTakePictureException extends Exception {
        public CannotTakePictureException(Throwable cause) {
            super(cause);
        }
    }
    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss'.jpg'");

    public String takePicture() throws CannotTakePictureException {
        String fileName = FILE_NAME_FORMAT.format(new Date());
//        Webcam.setDriver(new WebcamDefaultDriver());
        Webcam webcam = Webcam.getDefault();
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open();

        try {
            ImageIO.write(webcam.getImage(), "JPG", new File(fileName));
        } catch (IOException e) {
            throw new CannotTakePictureException(e);
        }

        webcam.close();
        return fileName;
    }
}
