package com.moonike.project.common.constant;

/**
 * Redis Key 常量类
 */
public class RedisKeyConstant {

    /**
     * 短链接跳转前缀 Key
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";

    /**
     * 短链接跳转 空值前缀 Key
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:goto:isNull:%s";

    /**
     * 短链接跳转 分布式锁前缀 Key
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "lock:short-link:goto:%s";
}