package net.lab1024.sa.base.module.support.datamasking;

/**
 * 数据脱敏工具占位实现。
 */
public final class SmartDataMaskingUtil {
    private SmartDataMaskingUtil() {}

    public static String dataMasking(String value) {
        if (value == null || value.length() <= 2) {
            return value;
        }
        return value.charAt(0) + "***" + value.charAt(value.length() - 1);
    }

    public static Object dataMasking(Object value, DataMaskingTypeEnum type) {
        return value == null ? null : dataMasking(String.valueOf(value));
    }
}
