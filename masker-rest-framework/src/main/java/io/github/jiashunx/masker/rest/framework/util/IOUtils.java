package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.model.DiskFileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * io util.
 * @author jiashunx
 */
public final class IOUtils {

    private IOUtils() {}

    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    public static Properties loadPropertiesFromClasspath(String filePath) {
        return loadPropertiesFromClasspath(filePath, IOUtils.class.getClassLoader());
    }

    public static Properties loadPropertiesFromClasspath(String filePath, ClassLoader classLoader) {
        return loadPropertiesFromByteArray(loadFileBytesFromClasspath(filePath, classLoader));
    }

    public static Properties loadPropertiesFromDisk(String filePath) {
        return loadPropertiesFromByteArray(loadFileBytesFromDisk(filePath).getBytes());
    }

    private static Properties loadPropertiesFromByteArray(byte[] bytes) {
        Properties properties = new Properties();
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            properties.load(inputStream);
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("load properties from byte array failed.", e);
            }
        }
        return properties;
    }

    public static byte[] loadFileBytesFromClasspath(String filePath) {
        return loadFileBytesFromClasspath(filePath, IOUtils.class.getClassLoader());
    }

    public static byte[] loadFileBytesFromClasspath(String filePath, ClassLoader classLoader) {
        byte[] tmp = new byte[1024];
        int size = 0;
        try (InputStream inputStream = classLoader.getResourceAsStream(filePath);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while ((size = inputStream.read(tmp)) != -1) {
                outputStream.write(tmp, 0, size);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("load classpath file [{}] failed, classloader [{}]", filePath, classLoader, e);
            }
        }
        return null;
    }

    public static String loadFileContentFromClasspath(String filePath) {
        return loadFileContentFromClasspath(filePath, StandardCharsets.UTF_8);
    }

    public static String loadFileContentFromClasspath(String filePath, ClassLoader classLoader) {
        return loadFileContentFromClasspath(filePath, classLoader, StandardCharsets.UTF_8);
    }

    public static String loadFileContentFromClasspath(String filePath, Charset charset) {
        byte[] bytes = loadFileBytesFromClasspath(filePath);
        if (bytes != null) {
            return new String(bytes, charset);
        }
        return null;
    }

    public static String loadFileContentFromClasspath(String filePath, ClassLoader classLoader, Charset charset) {
        byte[] bytes = loadFileBytesFromClasspath(filePath, classLoader);
        if (bytes != null) {
            return new String(bytes, charset);
        }
        return null;
    }

    public static Map<String, DiskFileResource> loadFilesBytesFromDiskDir(String dirPath) {
        Map<String, DiskFileResource> resourceModelMap = new HashMap<>();
        File parentDir = new File(dirPath);
        File[] files = Objects.requireNonNull(parentDir.listFiles());
        for (File file: files) {
            String filePath = file.getAbsolutePath();
            if (file.isDirectory()) {
                resourceModelMap.putAll(loadFilesBytesFromDiskDir(filePath));
            }
            if (file.isFile()) {
                resourceModelMap.put(filePath, loadFileBytesFromDisk(filePath));
            }
        }
        return resourceModelMap;
    }

    public static DiskFileResource loadFileBytesFromDisk(String filePath) {
        byte[] tmp = new byte[1024];
        int size = 0;
        try (InputStream inputStream = new FileInputStream(filePath);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            while ((size = inputStream.read(tmp)) != -1) {
                outputStream.write(tmp, 0, size);
            }
            DiskFileResource model = new DiskFileResource();
            model.setBytes(outputStream.toByteArray());
            model.setUrl(filePath);
            model.setDiskFilePath(filePath);
//            String contentType = Files.probeContentType(Paths.get(filePath));
            String contentType = new MimetypesFileTypeMap().getContentType(filePath);
//            String contentType = URLConnection.getFileNameMap().getContentTypeFor(filePath);
            model.setContentType(contentType);
            return model;
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("load disk file [{}] failed", filePath, e);
            }
        }
        return null;
    }

    public static Map<String, String> loadFilesContentsFromDiskDir(String dirPath) {
        return loadFilesContentsFromDiskDir(dirPath, StandardCharsets.UTF_8);
    }

    public static Map<String, String> loadFilesContentsFromDiskDir(String dirPath, Charset charset) {
        Map<String, DiskFileResource> resourceModelMap = loadFilesBytesFromDiskDir(dirPath);
        Map<String, String> contentMap = new HashMap<>();
        resourceModelMap.forEach((filePath, model) -> {
            contentMap.put(filePath, new String(model.getBytes(), charset));
        });
        return contentMap;
    }

    public static String loadFileContentFromDisk(String filePath) {
        return loadFileContentFromDisk(filePath, StandardCharsets.UTF_8);
    }

    public static String loadFileContentFromDisk(String filePath, Charset charset) {
        DiskFileResource model = loadFileBytesFromDisk(filePath);
        if (model != null) {
            return new String(model.getBytes(), charset);
        }
        return null;
    }

}
