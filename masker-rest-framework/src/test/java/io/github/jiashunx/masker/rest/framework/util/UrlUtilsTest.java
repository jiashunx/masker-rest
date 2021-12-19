package io.github.jiashunx.masker.rest.framework.util;

import static org.junit.Assert.*;
import org.junit.Test;

public class UrlUtilsTest {

    @Test
    public void test_nullToEmpty() {
        assertEquals("", UrlUtils.nullToEmpty(null));
        assertEquals("", UrlUtils.nullToEmpty(""));
        assertEquals(" ", UrlUtils.nullToEmpty(" "));
        assertEquals("///", UrlUtils.nullToEmpty("///"));
        assertEquals("/xxx", UrlUtils.nullToEmpty("/xxx"));
        assertEquals("jjjj", UrlUtils.nullToEmpty("jjjj"));
    }

    @Test
    public void test_replaceWinSep() {
        assertEquals("", UrlUtils.replaceWinSep(null));
        assertEquals("", UrlUtils.replaceWinSep(""));
        assertEquals(" ", UrlUtils.replaceWinSep(" "));
        assertEquals("kkkkk/kkkkk", UrlUtils.replaceWinSep("kkkkk\\kkkkk"));
    }

    @Test
    public void test_removePrefixSep0() {
        assertEquals("", UrlUtils.removePrefixSep0(null));
        assertEquals("", UrlUtils.removePrefixSep0(""));
        assertEquals(" ", UrlUtils.removePrefixSep0(" "));
        // 移除前缀
        assertEquals("", UrlUtils.removePrefixSep0("/"));
        assertEquals("/", UrlUtils.removePrefixSep0("//"));
        assertEquals("ddd/ddd", UrlUtils.removePrefixSep0("ddd/ddd"));
        assertEquals("ddd/ddd", UrlUtils.removePrefixSep0("/ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.removePrefixSep0("//ddd/ddd"));
        assertEquals("//ddd/ddd", UrlUtils.removePrefixSep0("///ddd/ddd"));
    }

    @Test
    public void test_removePrefixSep() {
        assertEquals("", UrlUtils.removePrefixSep(null));
        assertEquals("", UrlUtils.removePrefixSep(""));
        assertEquals(" ", UrlUtils.removePrefixSep(" "));
        // 移除前缀-保留最后一位前缀
        assertEquals("/", UrlUtils.removePrefixSep("/"));
        assertEquals("/", UrlUtils.removePrefixSep("//"));
        assertEquals("ddd/ddd", UrlUtils.removePrefixSep("ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.removePrefixSep("/ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.removePrefixSep("//ddd/ddd"));
        assertEquals("//ddd/ddd", UrlUtils.removePrefixSep("///ddd/ddd"));
    }

    @Test
    public void test_removePrefixSeps0() {
        assertEquals("", UrlUtils.removePrefixSeps0(null));
        assertEquals("", UrlUtils.removePrefixSeps0(""));
        assertEquals(" ", UrlUtils.removePrefixSeps0(" "));
        // 循环移除前缀-不保留前缀
        assertEquals("", UrlUtils.removePrefixSeps0("/"));
        assertEquals("", UrlUtils.removePrefixSeps0("//"));
        assertEquals("ddd/ddd", UrlUtils.removePrefixSeps0("ddd/ddd"));
        assertEquals("ddd/ddd", UrlUtils.removePrefixSeps0("/ddd/ddd"));
        assertEquals("ddd/ddd", UrlUtils.removePrefixSeps0("//ddd/ddd"));
        assertEquals("ddd/ddd", UrlUtils.removePrefixSeps0("///ddd/ddd"));
    }

    @Test
    public void test_removePrefixSeps() {
        assertEquals("", UrlUtils.removePrefixSeps(null));
        assertEquals("", UrlUtils.removePrefixSeps(""));
        assertEquals(" ", UrlUtils.removePrefixSeps(" "));
        // 循环移除前缀-保留最后一位前缀
        assertEquals("/", UrlUtils.removePrefixSeps("/"));
        assertEquals("/", UrlUtils.removePrefixSeps("//"));
        assertEquals("ddd/ddd", UrlUtils.removePrefixSeps("ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.removePrefixSeps("/ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.removePrefixSeps("//ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.removePrefixSeps("///ddd/ddd"));
    }

    @Test
    public void test_removeSuffixSep0() {
        assertEquals("", UrlUtils.removeSuffixSep0(null));
        assertEquals("", UrlUtils.removeSuffixSep0(""));
        assertEquals(" ", UrlUtils.removeSuffixSep0(" "));
        // 移除后缀
        assertEquals("", UrlUtils.removeSuffixSep0("/"));
        assertEquals("/", UrlUtils.removeSuffixSep0("//"));
        assertEquals("/ddd/ddd", UrlUtils.removeSuffixSep0("/ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.removeSuffixSep0("/ddd/ddd/"));
        assertEquals("/ddd/ddd/", UrlUtils.removeSuffixSep0("/ddd/ddd//"));
        assertEquals("/ddd/ddd//", UrlUtils.removeSuffixSep0("/ddd/ddd///"));
    }

    @Test
    public void test_removeSuffixSep() {
        assertEquals("", UrlUtils.removeSuffixSep(null));
        assertEquals("", UrlUtils.removeSuffixSep(""));
        assertEquals(" ", UrlUtils.removeSuffixSep(" "));
        // 移除后缀-保留最后一位后缀
        assertEquals("/", UrlUtils.removeSuffixSep("/"));
        assertEquals("/", UrlUtils.removeSuffixSep("//"));
        assertEquals("/ddd/ddd", UrlUtils.removeSuffixSep("/ddd/ddd"));
        assertEquals("/ddd/ddd/", UrlUtils.removeSuffixSep("/ddd/ddd/"));
        assertEquals("/ddd/ddd/", UrlUtils.removeSuffixSep("/ddd/ddd//"));
        assertEquals("/ddd/ddd//", UrlUtils.removeSuffixSep("/ddd/ddd///"));
    }

    @Test
    public void test_removeSuffixSeps0() {
        assertEquals("", UrlUtils.removeSuffixSeps0(null));
        assertEquals("", UrlUtils.removeSuffixSeps0(""));
        assertEquals(" ", UrlUtils.removeSuffixSeps0(" "));
        // 循环移除后缀-不保留后缀
        assertEquals("", UrlUtils.removeSuffixSeps0("/"));
        assertEquals("", UrlUtils.removeSuffixSeps0("//"));
        assertEquals("/ddd/ddd", UrlUtils.removeSuffixSeps0("/ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.removeSuffixSeps0("/ddd/ddd/"));
        assertEquals("/ddd/ddd", UrlUtils.removeSuffixSeps0("/ddd/ddd//"));
        assertEquals("/ddd/ddd", UrlUtils.removeSuffixSeps0("/ddd/ddd///"));
    }

    @Test
    public void test_removeSuffixSeps() {
        assertEquals("", UrlUtils.removeSuffixSeps(null));
        assertEquals("", UrlUtils.removeSuffixSeps(""));
        assertEquals(" ", UrlUtils.removeSuffixSeps(" "));
        // 循环移除后缀-保留最后一位后缀
        assertEquals("/", UrlUtils.removeSuffixSeps("/"));
        assertEquals("/", UrlUtils.removeSuffixSeps("//"));
        assertEquals("/ddd/ddd", UrlUtils.removeSuffixSeps("/ddd/ddd"));
        assertEquals("/ddd/ddd/", UrlUtils.removeSuffixSeps("/ddd/ddd/"));
        assertEquals("/ddd/ddd/", UrlUtils.removeSuffixSeps("/ddd/ddd//"));
        assertEquals("/ddd/ddd/", UrlUtils.removeSuffixSeps("/ddd/ddd///"));
    }

    @Test
    public void test_appendPrefixSep() {
        assertEquals("/", UrlUtils.appendPrefixSep(null));
        assertEquals("/", UrlUtils.appendPrefixSep(""));
        assertEquals("/ ", UrlUtils.appendPrefixSep(" "));
        assertEquals("/", UrlUtils.appendPrefixSep("/"));
        assertEquals("//", UrlUtils.appendPrefixSep("//"));
        assertEquals("/ddd/ddd", UrlUtils.appendPrefixSep("ddd/ddd"));
        assertEquals("/ddd/ddd", UrlUtils.appendPrefixSep("/ddd/ddd"));
        assertEquals("//ddd/ddd", UrlUtils.appendPrefixSep("//ddd/ddd"));
        assertEquals("///ddd/ddd", UrlUtils.appendPrefixSep("///ddd/ddd"));
    }

    @Test
    public void test_appendSuffixSep() {
        assertEquals("/", UrlUtils.appendSuffixSep(null));
        assertEquals("/", UrlUtils.appendSuffixSep(""));
        assertEquals(" /", UrlUtils.appendSuffixSep(" "));
        assertEquals("/", UrlUtils.appendSuffixSep("/"));
        assertEquals("//", UrlUtils.appendSuffixSep("//"));
        assertEquals("/ddd/ddd/", UrlUtils.appendSuffixSep("/ddd/ddd"));
        assertEquals("/ddd/ddd/", UrlUtils.appendSuffixSep("/ddd/ddd/"));
        assertEquals("/ddd/ddd//", UrlUtils.appendSuffixSep("/ddd/ddd//"));
        assertEquals("/ddd/ddd///", UrlUtils.appendSuffixSep("/ddd/ddd///"));
    }

}
