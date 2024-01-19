package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.exception.MRestFileOperateException;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
        return loadProperties(inputStream, true);
    }

    public static Properties loadProperties(InputStream inputStream, boolean autoClose) {
        Properties properties = new Properties();
        try  {
            properties.load(inputStream);
        } catch (Throwable throwable) {
            logger.error("load properties from inputStream failed", throwable);
        } finally {
            if (autoClose) {
                close(inputStream);
            }
        }
        return properties;
    }

    public static byte[] loadBytesFromClasspath(String filePath) {
        return loadBytesFromClasspath(filePath, IOUtils.class.getClassLoader());
    }

    public static byte[] loadBytesFromClasspath(String filePath, ClassLoader classLoader) {
        return loadBytesFromClasspath(filePath, classLoader, true);
    }

    public static byte[] loadBytesFromClasspath(String filePath, ClassLoader classLoader, boolean printErrStack) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            // filePath若为文件夹而不是文件，此时读取的是该文件夹下的文件名列表（默认）
            // notice: 该问题无法修复
            inputStream = classLoader.getResourceAsStream(filePath);
            outputStream = new ByteArrayOutputStream();
            copy(inputStream, outputStream);
            return outputStream.toByteArray();
        } catch (Throwable e) {
            if (printErrStack) {
                logger.error("load classpath file [{}] failed, classloader [{}]", filePath, classLoader, e);
            } else {
                logger.error("load classpath file [{}] failed, classloader [{}], error reason: {}", filePath, classLoader, e.getMessage());
            }
        } finally {
            close(inputStream);
            close(outputStream);
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
        return loadContentFromClasspath(filePath, IOUtils.class.getClassLoader(), charset);
    }

    public static String loadContentFromClasspath(String filePath, ClassLoader classLoader, Charset charset) {
        byte[] bytes = loadBytesFromClasspath(filePath, classLoader);
        if (bytes != null) {
            return new String(bytes, charset);
        }
        return null;
    }

    public static class DiskFileResource {
        private String filePath;
        private String absoluteFilePath;
        private byte[] bytes;
        private String contentType;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getAbsoluteFilePath() {
            return absoluteFilePath;
        }

        public void setAbsoluteFilePath(String absoluteFilePath) {
            this.absoluteFilePath = absoluteFilePath;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }

    public static Map<String, DiskFileResource> loadResourceFromDiskDir(File file) {
        return loadResourceFromDiskDir(file.getAbsolutePath());
    }

    public static Map<String, DiskFileResource> loadResourceFromDiskDir(String dirPath) {
        Map<String, DiskFileResource> resourceModelMap = new HashMap<>();
        File parentFile = new File(dirPath);
        String parentFilePath = parentFile.getAbsolutePath();
        if (parentFile.isFile()) {
            resourceModelMap.put(parentFilePath, loadResourceFromDisk(parentFilePath));
        } else if (parentFile.isDirectory()) {
            File[] files = parentFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file: files) {
                    String filePath = file.getAbsolutePath();
                    if (file.isFile()) {
                        resourceModelMap.put(filePath, loadResourceFromDisk(filePath));
                    } else if (file.isDirectory()) {
                        resourceModelMap.putAll(loadResourceFromDiskDir(filePath));
                    }
                }
            }
        }
        return resourceModelMap;
    }

    public static DiskFileResource loadResourceFromDisk(File file) {
        return loadResourceFromDisk(file.getAbsolutePath());
    }

    public static DiskFileResource loadResourceFromDisk(String filePath) {
        return loadResourceFromDisk(filePath, true);
    }

    public static DiskFileResource loadResourceFromDisk(String filePath, boolean printErrStack) {
        try {
            DiskFileResource diskFileResource = new DiskFileResource();
            diskFileResource.setFilePath(filePath);
            diskFileResource.setAbsoluteFilePath(new File(filePath).getAbsolutePath());
            diskFileResource.setBytes(loadBytesFromDisk(filePath, printErrStack));
            diskFileResource.setContentType(new MimetypesFileTypeMap().getContentType(filePath));
            return diskFileResource;
        } catch (Throwable e) {
            if (printErrStack) {
                logger.error("load diskpath file [{}] failed", filePath, e);
            } else {
                logger.error("load diskpath file [{}] failed, error reason: {}", filePath, e.getMessage());
            }
        }
        return null;
    }

    public static Map<String, byte[]> loadBytesFromDiskDir(File dir) {
        return loadBytesFromDiskDir(dir.getAbsolutePath());
    }

    public static Map<String, byte[]> loadBytesFromDiskDir(String dirPath) {
        Map<String, byte[]> resourceBytesMap = new HashMap<>();
        File parentFile = new File(dirPath);
        String parentFilePath = parentFile.getAbsolutePath();
        if (parentFile.isFile()) {
            resourceBytesMap.put(parentFilePath, loadBytesFromDisk(parentFilePath));
        } else if (parentFile.isDirectory()) {
            File[] files = parentFile.listFiles();
            if (files != null && files.length > 0) {
                for (File file: files) {
                    String filePath = file.getAbsolutePath();
                    if (file.isFile()) {
                        resourceBytesMap.put(filePath, loadBytesFromDisk(filePath));
                    } else if (file.isDirectory()) {
                        resourceBytesMap.putAll(loadBytesFromDiskDir(filePath));
                    }
                }
            }
        }
        return resourceBytesMap;

    }

    public static byte[] loadBytesFromDisk(File file) {
        return loadBytesFromDisk(file.getAbsolutePath());
    }

    public static byte[] loadBytesFromDisk(String filePath) {
        return loadBytesFromDisk(filePath, true);
    }

    public static byte[] loadBytesFromDisk(String filePath, boolean printErrStack) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.warn("load diskpath file [{}] failed, it's not exists", filePath);
                return null;
            }
            if (!file.isFile()) {
                logger.warn("load diskpath file [{}] failed, it's not a file", filePath);
                return null;
            }
            inputStream = new FileInputStream(filePath);
            outputStream = new ByteArrayOutputStream();
            copy(inputStream, outputStream);
            // String contentType = new MimetypesFileTypeMap().getContentType(filePath);
            return outputStream.toByteArray();
        } catch (Throwable e) {
            if (printErrStack) {
                logger.error("load diskpath file [{}] failed", filePath, e);
            } else {
                logger.error("load diskpath file [{}] failed, error reason: {}", filePath, e.getMessage());
            }
        } finally {
            close(inputStream);
            close(outputStream);
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
        } catch (Throwable throwable) {
            logger.error("read bytes failed", throwable);
        }
        return null;
    }

    public static byte[] readBytes(File file) {
        return loadBytesFromDisk(file.getAbsolutePath());
    }

    public static void close(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("close InputStream failed", e);
            }
        }
    }

    public static void close(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException exception) {
                logger.error("close OutputStream failed", exception);
            }
        }
    }

    public static void close(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException exception) {
                logger.error("close Reader failed", exception);
            }
        }
    }

    public static void close(Writer writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException exception) {
                logger.error("close Writer failed", exception);
            }
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
