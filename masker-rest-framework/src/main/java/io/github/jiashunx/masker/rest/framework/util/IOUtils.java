package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.exception.MRestFileOperateException;
import io.github.jiashunx.masker.rest.framework.model.DiskFileResource;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
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
        return loadProperties(loadBytesFromClasspath(filePath, classLoader));
    }

    public static Properties loadPropertiesFromDisk(File file) {
        return loadProperties(loadBytesFromDisk(file));
    }

    public static Properties loadPropertiesFromDisk(String filePath) {
        return loadProperties(loadBytesFromDisk(filePath));
    }

    public static Properties loadProperties(byte[] bytes) {
        return loadProperties(new ByteArrayInputStream(bytes));
    }

    public static Properties loadProperties(InputStream inputStream) {
        Properties properties = new Properties();
        try  {
            properties.load(inputStream);
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("load properties from byte array failed.", throwable);
            }
        }
        return properties;
    }

    public static byte[] loadBytesFromClasspath(String filePath) {
        return loadBytesFromClasspath(filePath, IOUtils.class.getClassLoader());
    }

    public static byte[] loadBytesFromClasspath(String filePath, ClassLoader classLoader) {
        try (InputStream inputStream = classLoader.getResourceAsStream(filePath);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            copy(inputStream, outputStream);
            return outputStream.toByteArray();
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error("load classpath file [{}] failed, classloader [{}]", filePath, classLoader, e);
            }
        }
        return null;
    }

    public static String loadContentFromClasspath(String filePath) {
        return loadContentFromClasspath(filePath, IOUtils.class.getClassLoader());
    }

    public static String loadContentFromClasspath(String filePath, ClassLoader classLoader) {
        return loadContentFromClasspath(filePath, classLoader, StandardCharsets.UTF_8);
    }

    public static String loadContentFromClasspath(String filePath, Charset charset) {
        return loadContentFromClasspath(filePath, IOUtils.class.getClassLoader(), StandardCharsets.UTF_8);
    }

    public static String loadContentFromClasspath(String filePath, ClassLoader classLoader, Charset charset) {
        byte[] bytes = loadBytesFromClasspath(filePath, classLoader);
        if (bytes != null) {
            return new String(bytes, charset);
        }
        return null;
    }

    public static Map<String, DiskFileResource> loadResourceFromDiskDir(File file) {
        return loadResourceFromDiskDir(file.getAbsolutePath());
    }

    public static Map<String, DiskFileResource> loadResourceFromDiskDir(String dirPath) {
        Map<String, DiskFileResource> resourceModelMap = new HashMap<>();
        File parentDir = new File(dirPath);
        File[] files = Objects.requireNonNull(parentDir.listFiles());
        for (File file: files) {
            String filePath = file.getAbsolutePath();
            if (file.isDirectory()) {
                resourceModelMap.putAll(loadResourceFromDiskDir(filePath));
            }
            if (file.isFile()) {
                DiskFileResource resource = loadResourceFromDisk(filePath);
                if (resource != null) {
                    resourceModelMap.put(filePath, resource);
                }
            }
        }
        return resourceModelMap;
    }

    public static DiskFileResource loadResourceFromDisk(File file) {
        return loadResourceFromDisk(file.getAbsolutePath());
    }

    public static DiskFileResource loadResourceFromDisk(String filePath) {
        try (InputStream inputStream = new FileInputStream(filePath);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            copy(inputStream, outputStream);
            DiskFileResource model = new DiskFileResource();
            model.setBytes(outputStream.toByteArray());
            model.setUrl(filePath);
            model.setDiskFilePath(filePath);
//            String contentType = Files.probeContentType(Paths.get(filePath));
            String contentType = new MimetypesFileTypeMap().getContentType(filePath);
//            String contentType = URLConnection.getFileNameMap().getContentTypeFor(filePath);
            model.setContentType(contentType);
            return model;
        } catch (Throwable e) {
            if (logger.isErrorEnabled()) {
                logger.error("load disk file [{}] failed", filePath, e);
            }
        }
        return null;
    }

    public static Map<String, byte[]> loadBytesFromDiskDir(File dir) {
        return loadBytesFromDiskDir(dir.getAbsolutePath());
    }

    public static Map<String, byte[]> loadBytesFromDiskDir(String dirPath) {
        Map<String, byte[]> retMap = new HashMap<>();
        Map<String, DiskFileResource> resourceMap = loadResourceFromDiskDir(dirPath);
        resourceMap.forEach((key, value) -> {
            retMap.put(key, value.getBytes());
        });
        return retMap;
    }

    public static byte[] loadBytesFromDisk(File file) {
        return loadBytesFromDisk(file.getAbsolutePath());
    }

    public static byte[] loadBytesFromDisk(String filePath) {
        DiskFileResource model = loadResourceFromDisk(filePath);
        if (model != null) {
            return model.getBytes();
        }
        return null;
    }

    public static Map<String, String> loadContentFromDiskDir(File file) {
        return loadContentFromDiskDir(file.getAbsolutePath());
    }

    public static Map<String, String> loadContentFromDiskDir(String dirPath) {
        return loadContentFromDiskDir(dirPath, StandardCharsets.UTF_8);
    }

    public static Map<String, String> loadContentFromDiskDir(File file, Charset charset) {
        return loadContentFromDiskDir(file.getAbsolutePath(), charset);
    }

    public static Map<String, String> loadContentFromDiskDir(String dirPath, Charset charset) {
        Map<String, String> contentMap = new HashMap<>();
        Map<String, byte[]> bytesMap = loadBytesFromDiskDir(dirPath);
        bytesMap.forEach((key, value) -> {
            contentMap.put(key, new String(value, charset));
        });
        return contentMap;
    }

    public static String loadContentFromDisk(File file) {
        return loadContentFromDisk(file.getAbsolutePath());
    }

    public static String loadContentFromDisk(String filePath) {
        return loadContentFromDisk(filePath, StandardCharsets.UTF_8);
    }

    public static String loadContentFromDisk(File file, Charset charset) {
        return loadContentFromDisk(file.getAbsolutePath(), charset);
    }

    public static String loadContentFromDisk(String filePath, Charset charset) {
        byte[] bytes = loadBytesFromDisk(filePath);
        if (bytes != null) {
            return new String(bytes, charset);
        }
        return null;
    }

    public static void write(Object object, File file) {
        write(object, false, file);
    }

    public static void write(Object object, boolean pretty, File file) {
        write(MRestSerializer.objectToJson(object, pretty), file);
    }

    public static void write(String string, File file) {
        write(string, StandardCharsets.UTF_8, file);
    }

    public static void write(String string, Charset charset, File file) {
        write(string.getBytes(charset), file);
    }

    public static void write(byte[] bytes, File file) {
        FileUtils.newFile(file.getAbsolutePath());
        write(new ByteArrayInputStream(bytes), file);
    }

    public static void write(InputStream inputStream, File file) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            copy(inputStream, outputStream);
        } catch (Throwable throwable) {
            throw new MRestFileOperateException(throwable);
        }
    }

    public static byte[] readBytes(InputStream inputStream) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputStream.available());) {
            copy(inputStream, outputStream);
            return outputStream.toByteArray();
        } catch (Throwable throwable) {}
        return null;
    }

    public static byte[] readBytes(File file) {
        return loadBytesFromDisk(file.getAbsolutePath());
    }

    public static void close(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {}
        }
    }

    public static void close(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException exception) {}
        }
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        copy(inputStream, outputStream, true);
    }

    public static void copy(InputStream inputStream, OutputStream outputStream, boolean autoClose) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int readSize = 0;
            while ((readSize = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, readSize);
            }
        } finally {
            if (autoClose) {
                close(inputStream);
                close(outputStream);
            }
        }
    }

    public static void copy(InputStream inputStream, File targetFile)throws IOException {
        copy(inputStream, new FileOutputStream(targetFile));
    }

    public static void copy(File sourceFile, OutputStream outputStream) throws IOException {
        copy(new FileInputStream(sourceFile), outputStream);
    }

    public static void copy(File sourceFile, File targetFile) throws IOException {
        copy(sourceFile, new FileOutputStream(targetFile));
    }

    public static void copy(String sourceFilePath, OutputStream outputStream) throws IOException {
        copy(new File(sourceFilePath), outputStream);
    }

    public static void copy(String sourceFilePath, File targetFile) throws IOException {
        copy(sourceFilePath, new FileOutputStream(targetFile));
    }

    public static void copy(String sourceFilePath, String targetFilePath) throws IOException {
        copy(sourceFilePath, new File(targetFilePath));
    }

    public static void copy(InputStream inputStream, String targetFilePath) throws IOException {
        copy(inputStream, new File(targetFilePath));
    }

    public static void copy(File sourceFile, String targetFilePath) throws IOException {
        copy(sourceFile, new File(targetFilePath));
    }

}
