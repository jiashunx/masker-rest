package io.github.jiashunx.masker.rest.framework.util;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

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

}
