package net.lab1024.sa.admin.module.access.domain.enums;


/**
 * 门禁区域类型枚举
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举替代魔法数字
 * - 提供类型名称和描述
 * - 支持类型转换和验证
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */


public enum AccessAreaTypeEnum {

    /**
     * 园区
     */
    CAMPUS(1, "园区", "CAMPUS"),

    /**
     * 建筑
     */
    BUILDING(2, "建筑", "BUILDING"),

    /**
     * 楼层
     */
    FLOOR(3, "楼层", "FLOOR"),

    /**
     * 房间
     */
    ROOM(4, "房间", "ROOM"),

    /**
     * 区域
     */
    AREA(5, "区域", "AREA"),

    /**
     * 其他
     */
    OTHER(6, "其他", "OTHER");

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 构造函数
     *
     * @param value 类型值
     * @param name 类型名称
     * @param code 类型代码
     */
    AccessAreaTypeEnum(Integer value, String name, String code) {
        this.value = value;
        this.name = name;
        this.code = code;
    }

    /**
     * 获取类型值
     *
     * @return 类型值
     */
    public Integer getValue() {
        return value;
    }

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取类型代码
     *
     * @return 类型代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 类型值
     * @return 枚举对象，如果未找到返回null
     */
    public static AccessAreaTypeEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (AccessAreaTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 类型代码
     * @return 枚举对象，如果未找到返回null
     */
    public static AccessAreaTypeEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (AccessAreaTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查值是否有效
     *
     * @param value 类型值
     * @return 是否有效
     */
    public static boolean isValid(Integer value) {
        return fromValue(value) != null;
    }
}
