package net.lab1024.sa.base.module.support.auth;

/**
 * 数据权限范围枚举
 * 用于控制用户可以访问的数据范围
 *
 * @author IOE-DREAM System
 * @version 1.0
 * @since 2025-11-18
 */
public enum DataScope {

    /**
     * 全部数据
     */
    ALL("全部数据"),

    /**
     * 本部门数据
     */
    DEPT("本部门数据"),

    /**
     * 本部门及子部门数据
     */
    DEPT_WITH_CHILD("本部门及子部门数据"),

    /**
     * 仅本人数据
     */
    SELF("仅本人数据"),

    /**
     * 自定义数据范围
     */
    CUSTOM("自定义数据范围"),

    /**
     * 指定区域数据
     */
    AREA("指定区域数据");

    private final String description;

    DataScope(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 转换为字符串 - 用于支持DataScope到String的转换
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * 根据描述获取枚举
     */
    public static DataScope fromDescription(String description) {
        for (DataScope dataScope : values()) {
            if (dataScope.getDescription().equals(description)) {
                return dataScope;
            }
        }
        return null;
    }
}