package io.github.jiashunx.masker.rest.framework.util;

import static org.junit.Assert.*;
import org.junit.Test;

import io.github.jiashunx.masker.rest.framework.exception.MRestZipException;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtilsTest {

    @Test
    public void test_isUnderDirectory() throws Exception {
        File root = Files.createTempDirectory("masker-rest-ut").toFile();
        // 正常子文件/子目录
        assertTrue(FileUtils.isUnderDirectory(root, new File(root, "a/b/c.txt")));
        assertTrue(FileUtils.isUnderDirectory(root, new File(root, "a")));
        // 目录本身不属于其下
        assertFalse(FileUtils.isUnderDirectory(root, root));
        // 路径穿越
        assertFalse(FileUtils.isUnderDirectory(root, new File(root, "../outside.txt")));
        assertFalse(FileUtils.isUnderDirectory(root, new File(root, "a/../../outside.txt")));
        // 名称前缀相同的同级目录(必须按路径段判断, 不能仅字符串前缀)
        assertFalse(FileUtils.isUnderDirectory(root, new File(root.getAbsolutePath() + "_backup")));
        // null
        assertFalse(FileUtils.isUnderDirectory(null, new File(root, "a")));
        assertFalse(FileUtils.isUnderDirectory(root, null));
    }

    @Test
    public void test_unzip_normal() throws Exception {
        File workDir = Files.createTempDirectory("masker-rest-zip-ok").toFile();
        File zipFile = new File(workDir, "ok.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zos.putNextEntry(new ZipEntry("a/b.txt"));
            zos.write("hello".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        File targetDir = new File(workDir, "out");
        FileUtils.unzip(zipFile, targetDir);
        File extracted = new File(targetDir, "a/b.txt");
        assertTrue(extracted.exists());
        assertEquals("hello", new String(Files.readAllBytes(extracted.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void test_unzip_zipSlipRejected() throws Exception {
        File workDir = Files.createTempDirectory("masker-rest-zip-evil").toFile();
        File zipFile = new File(workDir, "evil.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zos.putNextEntry(new ZipEntry("../evil.txt"));
            zos.write("evil".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        File targetDir = new File(workDir, "out");
        File escaped = new File(workDir, "evil.txt");
        try {
            FileUtils.unzip(zipFile, targetDir);
            fail("should reject zip slip entry");
        } catch (MRestZipException expected) {
            // expected
        }
        assertFalse("escaped file must not be created", escaped.exists());
    }

}
