package io.github.jiashunx.masker.rest.framework.type;

/**
 * @author jiashunx
 */
public enum UrlPatternType {
    /**
     * 未匹配
     */
    NONE,
    /**
     * 精确匹配：确定url进行匹配，例：/user/id.
     */
    STRICTLY,
    /**
     * 路径匹配：以"/"开头并以"/*"结尾，例：/*，/user/*
     */
    PATH_MATCH,
    /**
     * 扩展名匹配，以"*."开头的字符串用于拓展名匹配，例：*.do
     */
    EXT;
}
