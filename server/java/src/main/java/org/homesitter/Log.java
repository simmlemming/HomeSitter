package org.homesitter;

/**
 * Created by mtkachenko on 11/09/16.
 */
public class Log {
    public static void i(String message) {
        System.out.println(message);
    }

    public static void e(Exception e) {
        e.printStackTrace();
    }
}
