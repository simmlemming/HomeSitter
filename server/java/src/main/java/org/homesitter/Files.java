package org.homesitter;

import java.io.File;

/**
 * Created by mtkachenko on 11/09/16.
 */
public class Files {

    public void deleteFile(String fileName) {
        File fileWithPicture = new File(fileName);
        boolean deleted = fileWithPicture.delete();
        if (!deleted) {
            Log.e(new IllegalStateException("Cannot remove file " + fileWithPicture.getAbsolutePath()));
        }
    }

}
