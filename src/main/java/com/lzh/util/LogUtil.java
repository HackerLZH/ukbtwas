package com.lzh.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogUtil {
    private final static Map<Class<?>, Logger> loggerMap = new ConcurrentHashMap<>();
    
    private LogUtil(){}

    public static Logger getLogger(Class<?> c) {
        return loggerMap.computeIfAbsent(c, LoggerFactory::getLogger);
    }
}
