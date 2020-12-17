package io.github.jiashunx.masker.rest.framework.util;

import java.io.File;

/**
 * @author jiashunx
 */
public class FileUtils {

    private FileUtils() {}

    public static void deleteFile(File[] files) {
        if (files == null || files.length == 0) {
            return;
        }
        for (File file: files) {
            deleteFile(file);
        }
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isDirectory()) {
            deleteFile(file.listFiles());
        }
        return file.delete();
    }

}
