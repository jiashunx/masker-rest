package io.github.jiashunx.masker.rest.framework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

/**
 * @author jiashunx
 */
public class StringUtils {

    private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

    /**
     * "null"字符串.
     */
    public static final String NULL = "null";
    /**
     * ""空字符串.
     */
    public static final String EMPTY = "";
    /**
     * ";"分号.
     */
    public static final String SEMI_COLON = ";";
    /**
     * true.
     */
    public static final String TRUE = "true";
    /**
     * false.
     */
    public static final String FALSE = "false";
    /**
     * 换行.
     */
    public static final String ENTER = "\r\n";
    /**
     * 点号.
     */
    public static final String DOT = ".";
    /**
     * 路径分隔符
     */
    public static final String SEP_PATH = "/";
    /**
     * 路径分隔符-UNIX.
     */
    public static final String SEP_UNIX = "/";
    /**
     * 路径分隔符-WIN.
     */
    public static final String SEP_WIN = "\\";

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(str.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    public static String trimOrNull(String str) {
        String ts = trim(str);
        return isEmpty(ts) ? null : ts;
    }

    public static String trimOrEmpty(String str) {
        return str == null ? EMPTY : str.trim();
    }

    public static String replaceDotToSep(String str) {
        return str == null ? null : str.replace(DOT, SEP_PATH);
    }

    public static String replaceSepToDot(String str) {
        return str == null ? null : str.replace(SEP_PATH, DOT);
    }

    /**
     * 转换className, "."转换为"/"
     * @param className 类的全路径, 形如: com.jiashunx.Test
     * @return com/jiashunx/Test
     */
    public static String transferClassName(String className) {
        if (isNotBlank(className)) {
            return className.replaceAll("\\.", "/");
        }
        return className;
    }

    /**
     * 打印堆栈异常信息.
     * @param e 异常对象
     * @return 异常信息
     */
    public static String getExceptionStackTrace(final Throwable e) {
        return getExceptionStackTrace(e, false);
    }

    /**
     * 打印堆栈异常信息.
     * @param e 异常对象
     * @param oneLine 是否单行, 如果为true, 则替换所有换行符.
     * @return 异常信息
     */
    public static String getExceptionStackTrace(final Throwable e, boolean oneLine) {
        if (e == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
        } catch (Exception ex) {
            logger.error("获取异常 {} 堆栈失败", e.hashCode(), ex);
        }
        String content = sb.toString().replaceAll(" ", " ");
        if (oneLine) {
            content = contentToLine(content);
        }
        return content;
    }

    /**
     * 文本内容转换为行记录.
     * @param content 文本内容(可能有多行)
     * @return 单行记录.
     */
    public static String contentToLine(String content) {
        if (isNotEmpty(content)) {
            return content.replaceAll("\\r", " ").replaceAll("\\n", " ");
        }
        return content;
    }

    /**
     * 获取线程堆栈.
     * @param thread 线程对象
     * @return 线程堆栈
     */
    public static String getThreadStackTrace(final Thread thread) {
        if (thread == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        final StackTraceElement[] stackTraceList = thread.getStackTrace();
        if (stackTraceList != null) {
            for (StackTraceElement stack: stackTraceList) {
                sb.append(stack).append(ENTER);
            }
        }
        return sb.toString();
    }

    /**
     * 字符串合并.
     * @param separator 分隔符
     * @param params 待合并数据
     * @return 合并后的字符串
     */
    public static String join(String separator, Object... params) {
        StringBuilder builder = new StringBuilder();
        if (params != null && params.length > 0 && isNotEmpty(separator)) {
            for (int i = 0, length = params.length; i < length; i++) {
                builder.append(String.valueOf(params[i])).append(separator);
            }
            int length = builder.length();
            builder.delete(length - separator.length(), length);
        }
        return builder.toString();
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
