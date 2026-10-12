package io.github.jiashunx.masker.rest.framework;

import static org.junit.Assert.*;
import org.junit.Test;

import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

public class MRestFileUploadRequestTest {

    @Test
    public void test_addAttribute_snapshotSurvivesSourceRelease() throws Exception {
        MRestFileUploadRequest request = new MRestFileUploadRequest(new MRestRequest());
        MemoryAttribute source = new MemoryAttribute("username", "admin");
        request.addAttribute(source);
        // 模拟multipart解码器destroy: 释放源属性(缓冲区被回收)
        source.release();
        assertEquals(1, request.getAttributeList().size());
        Attribute snapshot = request.getAttributeList().get(0);
        assertEquals("username", snapshot.getName());
        // 快照独立于源属性, 源属性释放后仍可正常读取
        assertEquals("admin", snapshot.getValue());
    }

}
