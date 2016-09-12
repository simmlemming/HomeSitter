package org.homesitter;

import com.jcraft.jsch.JSchException;
import org.wonderant.ssh.Scp;
import org.wonderant.ssh.SecureContext;

import java.io.File;
import java.io.IOException;

/**
 * Created by mtkachenko on 11/09/16.
 */
public class Copier {
    public static class CannotCopyException extends Exception {
        public CannotCopyException(Throwable cause) {
            super(cause);
        }
    }

    public void scp(String localFile) throws CannotCopyException {
        String remoteFile = "/mnt/volume-fra1-01/homesitter/p/";

        SecureContext context = new SecureContext(Keys.REMOTE_USER_NAME, Keys.REMOTE_IP);
        context.setTrustAllHosts(true);
        context.setPrivateKeyFile(new File("id_rsa"));

        try {
            Scp.exec(context, localFile, remoteFile);
        } catch (JSchException e) {
            throw new CannotCopyException(e);
        } catch (IOException e) {
            throw new CannotCopyException(e);
        }
    }
}
