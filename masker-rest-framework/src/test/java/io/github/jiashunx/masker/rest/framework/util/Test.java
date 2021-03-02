package io.github.jiashunx.masker.rest.framework.util;

import org.junit.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jiashunx
 */
public class Test {

    @org.junit.Test
    public void test() {
        String[] arr = "/a/b/c".split("/");
        Assert.assertEquals(arr.length, 4);
        arr = "a/b/c".split("/");
        Assert.assertEquals(arr.length, 3);
        arr = "a".split("/");
        Assert.assertEquals(arr.length, 1);
//        Assert.assertTrue(url.matches("^\\{((?!(\\{|\\}))|\\S)+\\}$"));


        String url = "/{a}/{b}/c";
        Pattern pattern = Pattern.compile("\\{a}");
        Matcher matcher = pattern.matcher(url);
        boolean find = matcher.find();
        if (find) {
            System.out.println(matcher.groupCount());
            for (int i = 0, size = matcher.groupCount(); i < size; i++) {
                System.out.println(matcher.group(i));
            }
        }

    }

}
