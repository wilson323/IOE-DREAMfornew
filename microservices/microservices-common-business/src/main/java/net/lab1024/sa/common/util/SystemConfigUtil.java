package net.lab1024.sa.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置工具类
 * <p>
 * 提供系统配置参数的统一管理和获取
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Component
public class SystemConfigUtil {

    private static SystemConfigUtil instance;

    /**
     * 系统配置缓存
     */
    private static final Map<String, Object> CONFIG_CACHE = new HashMap<>();

    /**
     * 默认工作日配置
     */
    @Value("${system.workday.enabled:true}")
    private boolean workdayEnabled;

    /**
     * 工作时间开始
     */
    @Value("${system.work.start-time:09:00}")
    private String workStartTime;

    /**
     * 工作时间结束
     */
    @Value("${system.work.end-time:18:00}")
    private String workEndTime;

    @PostConstruct
    public void init() {
        instance = this;
        CONFIG_CACHE.put("workday.enabled", workdayEnabled);
        CONFIG_CACHE.put("work.start-time", workStartTime);
        CONFIG_CACHE.put("work.end-time", workEndTime);
    }

    /**
     * 获取配置值
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @param <T> 值类型
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getConfig(String key, T defaultValue) {
        Object value = CONFIG_CACHE.get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 获取字符串配置
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getString(String key, String defaultValue) {
        return getConfig(key, defaultValue);
    }

    /**
     * 获取整数配置
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static Integer getInteger(String key, Integer defaultValue) {
        Object value = CONFIG_CACHE.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 获取布尔配置
     *
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static Boolean getBoolean(String key, Boolean defaultValue) {
        Object value = CONFIG_CACHE.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * 设置配置值
     *
     * @param key 配置键
     * @param value 配置值
     */
    public static void setConfig(String key, Object value) {
        CONFIG_CACHE.put(key, value);
    }

    /**
     * 判断是否启用工作日检查
     *
     * @return 是否启用
     */
    public static boolean isWorkdayCheckEnabled() {
        return getBoolean("workday.enabled", true);
    }

    /**
     * 获取工作开始时间
     *
     * @return 工作开始时间
     */
    public static String getWorkStartTime() {
        return getString("work.start-time", "09:00");
    }

    /**
     * 获取工作结束时间
     *
     * @return 工作结束时间
     */
    public static String getWorkEndTime() {
        return getString("work.end-time", "18:00");
    }
}
