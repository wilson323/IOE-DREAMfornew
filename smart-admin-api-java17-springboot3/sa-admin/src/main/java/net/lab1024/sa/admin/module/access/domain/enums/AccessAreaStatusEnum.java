package net.lab1024.sa.admin.module.access.domain.enums;


/**
 * 门禁区域状态枚举
 * <p>
 * 严格遵循repowiki规范：
 * - 使用枚举替代魔法数字
 * - 提供状态名称和描述
 * - 支持状态转换和验证
 *
 * @author IOE-DREAM Team
 * @since 2025-11-19
 */


public enum AccessAreaStatusEnum {

    /**
     * 启用
     */
    ENABLED(1, "启用", "ENABLED"),

    /**
     * 禁用
     */
    DISABLED(0, "禁用", "DISABLED");

    /**
     * 状态值
     */
    private final Integer value;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 构造函数
     *
     * @param value 状态值
     * @param name 状态名称
     * @param code 状态代码
     */
    AccessAreaStatusEnum(Integer value, String name, String code) {
        this.value = value;
        this.name = name;
        this.code = code;
    }

    /**
     * 获取状态值
     *
     * @return 状态值
     */
    public Integer getValue() {
        return value;
    }

    /**
     * 获取状态名称
     *
     * @return 状态名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取状态代码
     *
     * @return 状态代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 根据值获取枚举
     *
     * @param value 状态值
     * @return 枚举对象，如果未找到返回null
     */
    public static AccessAreaStatusEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (AccessAreaStatusEnum status : values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 状态代码
     * @return 枚举对象，如果未找到返回null
     */
    public static AccessAreaStatusEnum fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        for (AccessAreaStatusEnum status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查值是否有效
     *
     * @param value 状态值
     * @return 是否有效
     */
    public static boolean isValid(Integer value) {
        return fromValue(value) != null;
    }
}
