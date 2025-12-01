package net.lab1024.sa.base.authz.rac.model;

/**
 * 数据权限范围枚举（RAC权限模型）
 * 用于控制用户可以访问的数据范围
 *
 * @author SmartAdmin Team
 * @since 2025-11-17
 */
public enum DataScope {

    /**
     * 全部数据
     */
    ALL("全部数据", "0"),

    /**
     * 自定义数据
     */
    CUSTOM("自定义数据", "1"),

    /**
     * 本部门数据
     */
    DEPT("本部门数据", "2"),

    /**
     * 本部门及子部门数据
     */
    DEPT_AND_CHILD("本部门及子部门数据", "3"),

    /**
     * 仅本人数据
     */
    SELF("仅本人数据", "4"),

    /**
     * 指定区域数据
     */
    AREA("指定区域数据", "5");

    private final String description;
    private final String code;

    DataScope(String description, String code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    /**
     * 根据代码获取枚举
     */
    public static DataScope getByCode(String code) {
        for (DataScope dataScope : values()) {
            if (dataScope.getCode().equals(code)) {
                return dataScope;
            }
        }
        return null;
    }

    /**
     * 根据描述获取枚举
     */
    public static DataScope getByDescription(String description) {
        for (DataScope dataScope : values()) {
            if (dataScope.getDescription().equals(description)) {
                return dataScope;
            }
        }
        return null;
    }

    /**
     * 转换为字符串 - 用于支持DataScope到String的转换
     */
    @Override
    public String toString() {
        return description;
    }
}