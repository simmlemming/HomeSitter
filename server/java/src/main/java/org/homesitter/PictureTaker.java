package org.homesitter;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by mtkachenko on 11/09/16.
 */
public class PictureTaker {
    public static class CannotTakePictureException extends Exception {
        public CannotTakePictureException(String message) {
            super(message);
        }

        public CannotTakePictureException(Throwable cause) {
            super(cause);
        }
    }
    private static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("YYYY-MM-dd-HH-mm-ss-");

    public String takePicture(int cameraIndex) throws CannotTakePictureException {
        String fileName = FILE_NAME_FORMAT.format(new Date()) + cameraIndex + ".jpg";


        Webcam webcam;
        try {
            List<Webcam> webcams = Webcam.getWebcams();
            if (webcams.size() <= cameraIndex) {
                throw new IllegalArgumentException("Only " + webcams.size() + " cameras detected, but requested camera #" + cameraIndex);
            }

            webcam = webcams.get(cameraIndex);
        } catch (RuntimeException e) {
            throw new CannotTakePictureException(e);
        }

        if (webcam.isOpen()) {
            throw new CannotTakePictureException("Webcam already open");
        }

        BufferedImage image = null;
        Exception webcamException = null;
        try {
            webcam.setViewSize(new Dimension(640, 480));
            webcam.open();
            image = webcam.getImage();
        } catch (RuntimeException e) {
            webcamException = e;
        } finally {
            webcam.close();
        }

        if (webcamException != null) {
            throw new CannotTakePictureException(webcamException);
        }

        if (image == null) {
            throw new CannotTakePictureException("Image is null");
        }

        try {
            ImageIO.write(image, "JPG", new File(fileName));
        } catch (IOException e) {
            throw new CannotTakePictureException(e);
        }

        return fileName;
    }
}
