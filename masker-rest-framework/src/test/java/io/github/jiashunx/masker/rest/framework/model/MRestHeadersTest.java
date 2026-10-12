package io.github.jiashunx.masker.rest.framework.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class MRestHeadersTest {

    @Test
    public void test_set_replacesSingleValue() {
        MRestHeaders headers = new MRestHeaders();
        headers.set("Content-Type", "text/plain");
        headers.set("Content-Type", "application/json");
        assertEquals(1, headers.getAll("Content-Type").size());
        assertEquals("application/json", headers.get("Content-Type"));
    }

    @Test
    public void test_add_appendsMultiValue() {
        MRestHeaders headers = new MRestHeaders();
        headers.add("Set-Cookie", "a=1");
        headers.add("Set-Cookie", "b=2");
        assertEquals(2, headers.getAll("Set-Cookie").size());
    }

    @Test
    public void test_merge_singleOverrideAndCookieAppend() {
        MRestHeaders base = new MRestHeaders();
        base.set("Content-Type", "text/plain");
        base.add("Set-Cookie", "a=1");
        MRestHeaders other = new MRestHeaders();
        other.set("Content-Type", "application/json");
        other.add("Set-Cookie", "b=2");
        base.merge(other);
        // 单值header被覆盖, 不产生重复
        assertEquals(1, base.getAll("Content-Type").size());
        assertEquals("application/json", base.get("Content-Type"));
        // Set-Cookie保留多值
        assertEquals(2, base.getAll("Set-Cookie").size());
    }

}
