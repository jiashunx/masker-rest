package io.github.jiashunx.masker.rest.framework.util;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class IOUtilsTest {

    @Test
    public void test_copy_largeData() throws Exception {
        byte[] data = new byte[20000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 251);
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, outputStream, false);
        assertArrayEquals(data, outputStream.toByteArray());
    }

}
