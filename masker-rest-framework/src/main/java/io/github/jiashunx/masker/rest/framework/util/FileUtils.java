package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.exception.MRestFileOperateException;
import io.github.jiashunx.masker.rest.framework.exception.MRestZipException;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author jiashunx
 */
public class FileUtils {

    private FileUtils() {}

    public static void deleteFile(File[] files) throws MRestFileOperateException {
        if (files == null || files.length == 0) {
            return;
        }
        for (File file: files) {
            deleteFile(file);
        }
    }

    public static boolean deleteFile(File file) throws MRestFileOperateException {
        if (file == null || !file.exists()) {
            return false;
        }
        try {
            if (file.isDirectory()) {
                deleteFile(file.listFiles());
            }
            return file.delete();
        } catch (Throwable throwable) {
            throw new MRestFileOperateException(throwable);
        }
    }

    public static File newDirectory(String path) throws MRestFileOperateException {
        File newDirectory = new File(path);
        try {
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }
            return newDirectory;
        } catch (Throwable throwable) {
            throw new MRestFileOperateException(throwable);
        }
    }

    public static File newFile(String path) throws MRestFileOperateException {
        File newFile = new File(path);
        try {
            if (!newFile.exists()) {
                newDirectory(newFile.getParentFile().getAbsolutePath());
                newFile.createNewFile();
            }
        } catch (Throwable throwable) {
            throw new MRestFileOperateException(throwable);
        }
        return newFile;
    }

    /**
     * 压缩文件
     * @param srcFileArr 待压缩的文件列表
     * @param targetFile 目标压缩文件
     * @throws MRestZipException MRestZipException
     */
    public static void zip(File[] srcFileArr, File targetFile) throws MRestZipException {
        newFile(targetFile.getAbsolutePath());
        try(FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            for (File srcFile: srcFileArr) {
                zip(srcFile, zipOutputStream, srcFile.getName());
            }
        } catch (Throwable exception) {
            throw new MRestZipException(exception);
        }
    }

    /**
     * 压缩文件
     * @param srcFile 待压缩的文件
     * @param zipOutputStream 压缩文件输出流
     * @param entryName 文件在压缩文件中的entry名称
     * @throws IOException IOException
     */
    private static void zip(File srcFile, ZipOutputStream zipOutputStream, String entryName) throws IOException {
        if (srcFile.isFile()) {
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            try (FileInputStream inputStream = new FileInputStream(srcFile);) {
                IOUtils.copy(inputStream, zipOutputStream, false);
            } finally {
                zipOutputStream.closeEntry();
            }
        } else if (srcFile.isDirectory()) {
            File[] childFileArr = srcFile.listFiles();
            if (childFileArr == null || childFileArr.length == 0) {
                zipOutputStream.putNextEntry(new ZipEntry(entryName + "/"));
                zipOutputStream.closeEntry();
            } else {
                for (File childFile: childFileArr) {
                    zip(childFile, zipOutputStream, entryName + "/" + childFile.getName());
                }
            }
        }
    }

    /**
     * 解压文件到指定目录下的指定文件夹
     * @param zipFile 待解压文件
     * @param targetDir 解压目录
     * @param unzipDirName 指定文件夹名称
     */
    public static void unzip(File zipFile, File targetDir, String unzipDirName) throws MRestZipException {
        unzip(zipFile, new File(targetDir.getAbsolutePath() + File.separator + unzipDirName));
    }

    /**
     * 解压文件到指定目录
     * @param zipFile 待解压文件
     * @param targetDir 解压目录
     */
    public static void unzip(File zipFile, File targetDir) throws MRestZipException {
        try (ZipFile zip = new ZipFile(zipFile)) {
            newDirectory(targetDir.getAbsolutePath());
            for (Enumeration<? extends  ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                File newFile = new File(targetDir.getAbsolutePath() + "/" + entryName);
                if (zipEntry.isDirectory()) {
                    newDirectory(newFile.getAbsolutePath());
                } else {
                    newFile(newFile.getAbsolutePath());
                    try (InputStream inputStream = zip.getInputStream(zipEntry);
                        OutputStream outputStream = new FileOutputStream(newFile)) {
                        IOUtils.copy(inputStream, outputStream);
                    }
                }
            }
        } catch (Throwable throwable) {
            throw new MRestZipException(throwable);
        }
    }

}
