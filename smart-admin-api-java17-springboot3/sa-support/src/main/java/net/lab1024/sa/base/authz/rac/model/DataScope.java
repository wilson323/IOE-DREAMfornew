package net.lab1024.sa.base.authz.rac.model;

/**
 * 数据权限枚举
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
public enum DataScope {

    /**
     * 全部数据权限
     */
    ALL("全部"),

    /**
     * 本部门及以下数据权限
     */
    DEPT_WITH_CHILD("本部门及以下"),

    /**
     * 本部门数据权限
     */
    DEPT("本部门"),

    /**
     * 仅本人数据权限
     */
    SELF("仅本人"),

    /**
     * 自定义数据权限
     */
    CUSTOM("自定义"),

    /**
     * 指定区域数据权限
     */
    AREA("指定区域");

    private final String description;

    DataScope(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
