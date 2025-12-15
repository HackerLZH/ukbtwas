package com.lzh.util;

import cn.hutool.setting.dialect.Props;

/**
 * 获取env.properties配置
 */
public final class PropsUtil {
    private static Props props;

    public static String getProp(String name) {
        if (props == null) {
            synchronized (PropsUtil.class) {
                if (props == null) {
                    props = new Props("env.properties");
                }
            }
        }
        return props.getStr(name);
    }
}
