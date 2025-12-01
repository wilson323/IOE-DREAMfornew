package net.lab1024.sa.base.module.area.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 区域类型枚举
 * 定义系统中不同类型的区域，支持层级化的区域管理
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
public enum AreaTypeEnum {

    /**
     * 园区
     */
    CAMPUS(1, "园区", "Campus"),

    /**
     * 建筑
     */
    BUILDING(2, "建筑", "Building"),

    /**
     * 楼层
     */
    FLOOR(3, "楼层", "Floor"),

    /**
     * 房间
     */
    ROOM(4, "房间", "Room"),

    /**
     * 区域
     */
    AREA(5, "区域", "Area"),

    /**
     * 其他
     */
    OTHER(6, "其他", "Other");

    private final Integer value;
    private final String description;
    private final String englishName;

    AreaTypeEnum(Integer value, String description, String englishName) {
        this.value = value;
        this.description = description;
        this.englishName = englishName;
    }

    @JsonValue
    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getEnglishName() {
        return englishName;
    }

    /**
     * 根据数值获取枚举
     *
     * @param value 数值
     * @return 枚举值
     */
    @JsonCreator
    public static AreaTypeEnum fromValue(Integer value) {
        for (AreaTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 根据字符串数值获取枚举
     *
     * @param value 字符串数值
     * @return 枚举值
     */
    public static AreaTypeEnum fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        try {
            Integer intValue = Integer.valueOf(value.trim());
            return fromValue(intValue);
        } catch (NumberFormatException e) {
            return OTHER;
        }
    }

    /**
     * 根据描述获取枚举
     *
     * @param description 描述
     * @return 枚举值
     */
    public static AreaTypeEnum fromDescription(String description) {
        for (AreaTypeEnum type : values()) {
            if (type.getDescription().equals(description)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 根据英文名获取枚举
     *
     * @param englishName 英文名
     * @return 枚举值
     */
    public static AreaTypeEnum fromEnglishName(String englishName) {
        for (AreaTypeEnum type : values()) {
            if (type.getEnglishName().equalsIgnoreCase(englishName)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 检查是否为有效的区域类型值
     *
     * @param value 数值
     * @return 是否有效
     */
    public static boolean isValid(Integer value) {
        for (AreaTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有枚举值的数组
     *
     * @return 数值数组
     */
    public static Integer[] getAllValues() {
        AreaTypeEnum[] types = values();
        Integer[] values = new Integer[types.length];
        for (int i = 0; i < types.length; i++) {
            values[i] = types[i].getValue();
        }
        return values;
    }
}