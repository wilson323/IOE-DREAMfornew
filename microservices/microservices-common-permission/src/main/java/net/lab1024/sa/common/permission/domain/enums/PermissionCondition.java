package net.lab1024.sa.common.permission.domain.enums;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 权限条件
 * <p>
 * 用于复合权限验证的条件封装
 * 支持AND/OR逻辑组合
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionCondition {

    /**
     * 条件类型
     * <p>
     * PERMISSION-权限条件
     * ROLE-角色条件
     * DATA_SCOPE-数据权限条件
     * AREA-区域权限条件
     * DEVICE-设备权限条件
     * MODULE-模块权限条件
     * CUSTOM-自定义条件
     * </p>
     */
    private String conditionType;

    /**
     * 条件值
     * <p>
     * 权限标识、角色标识、数据类型等
     * </p>
     */
    private String conditionValue;

    /**
     * 资源标识
     * <p>
     * 可选的资源标识
     * </p>
     */
    private String resource;

    /**
     * 条件参数
     * <p>
     * 用于传递额外的条件参数
     * 如数据权限的资源ID、区域ID等
     * </p>
     */
    private Object conditionParams;

    /**
     * 条件权重
     * <p>
     * 用于条件优先级排序
     * 数值越大优先级越高
     * </p>
     */
    private Integer weight;

    /**
     * 是否必须满足
     * <p>
     * true-必须满足
     * false-可选满足
     * </p>
     */
    private Boolean required;

    /**
     * 条件描述
     */
    private String description;

    /**
     * 创建权限条件
     */
    public static PermissionCondition ofPermission(String permission, String resource) {
        return PermissionCondition.ofPermission(permission, resource, null);
    }

    /**
     * 创建权限条件（带参数）
     */
    public static PermissionCondition ofPermission(String permission, String resource, Object params) {
        PermissionCondition condition = new PermissionCondition();
        condition.setConditionType("PERMISSION");
        condition.setConditionValue(permission);
        condition.setResource(resource);
        condition.setConditionParams(params);
        condition.setWeight(1);
        condition.setRequired(true);
        condition.setDescription("权限条件: " + permission);
        return condition;
    }

    /**
     * 创建角色条件
     */
    public static PermissionCondition ofRole(String role, String resource) {
        return PermissionCondition.ofRole(role, resource, null);
    }

    /**
     * 创建角色条件（带参数）
     */
    public static PermissionCondition ofRole(String role, String resource, Object params) {
        PermissionCondition condition = new PermissionCondition();
        condition.setConditionType("ROLE");
        condition.setConditionValue(role);
        condition.setResource(resource);
        condition.setConditionParams(params);
        condition.setWeight(1);
        condition.setRequired(true);
        condition.setDescription("角色条件: " + role);
        return condition;
    }

    /**
     * 创建数据权限条件
     */
    public static PermissionCondition ofDataScope(String dataType, Object resourceId) {
        PermissionCondition condition = new PermissionCondition();
        condition.setConditionType("DATA_SCOPE");
        condition.setConditionValue(dataType);
        condition.setConditionParams(resourceId);
        condition.setWeight(2);
        condition.setRequired(true);
        condition.setDescription("数据权限条件: " + dataType);
        return condition;
    }

    /**
     * 创建区域权限条件
     */
    public static PermissionCondition ofAreaPermission(Long areaId, String permission) {
        PermissionCondition condition = new PermissionCondition();
        condition.setConditionType("AREA");
        condition.setConditionValue(permission);
        condition.setConditionParams(areaId);
        condition.setWeight(2);
        condition.setRequired(true);
        condition.setDescription("区域权限条件: " + permission);
        return condition;
    }

    /**
     * 创建设备权限条件
     */
    public static PermissionCondition ofDevicePermission(String deviceId, String permission) {
        PermissionCondition condition = new PermissionCondition();
        condition.setConditionType("DEVICE");
        condition.setConditionValue(permission);
        condition.setConditionParams(deviceId);
        condition.setWeight(2);
        condition.setRequired(true);
        condition.setDescription("设备权限条件: " + permission);
        return condition;
    }

    /**
     * 创建模块权限条件
     */
    public static PermissionCondition ofModulePermission(String moduleCode, String permission) {
        PermissionCondition condition = new PermissionCondition();
        condition.setConditionType("MODULE");
        condition.setConditionValue(permission);
        condition.setConditionParams(moduleCode);
        condition.setWeight(1);
        condition.setRequired(true);
        condition.setDescription("模块权限条件: " + moduleCode + ":" + permission);
        return condition;
    }

    /**
     * 创建自定义条件
     */
    public static PermissionCondition ofCustom(String conditionValue, Object params) {
        PermissionCondition condition = new PermissionCondition();
        condition.setConditionType("CUSTOM");
        condition.setConditionValue(conditionValue);
        condition.setConditionParams(params);
        condition.setWeight(3);
        condition.setRequired(true);
        condition.setDescription("自定义条件: " + conditionValue);
        return condition;
    }

    /**
     * 创建可选条件
     */
    public PermissionCondition optional() {
        this.required = false;
        return this;
    }

    /**
     * 设置权重
     */
    public PermissionCondition withWeight(Integer weight) {
        this.weight = weight;
        return this;
    }

    /**
     * 设置描述
     */
    public PermissionCondition withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * 判断是否为权限条件
     */
    public boolean isPermissionCondition() {
        return "PERMISSION".equals(this.conditionType);
    }

    /**
     * 判断是否为角色条件
     */
    public boolean isRoleCondition() {
        return "ROLE".equals(this.conditionType);
    }

    /**
     * 判断是否为数据权限条件
     */
    public boolean isDataScopeCondition() {
        return "DATA_SCOPE".equals(this.conditionType);
    }

    /**
     * 判断是否为区域权限条件
     */
    public boolean isAreaCondition() {
        return "AREA".equals(this.conditionType);
    }

    /**
     * 判断是否为设备权限条件
     */
    public boolean isDeviceCondition() {
        return "DEVICE".equals(this.conditionType);
    }

    /**
     * 判断是否为模块权限条件
     */
    public boolean isModuleCondition() {
        return "MODULE".equals(this.conditionType);
    }

    /**
     * 判断是否为自定义条件
     */
    public boolean isCustomCondition() {
        return "CUSTOM".equals(this.conditionType);
    }

    /**
     * 获取参数值（指定类型）
     */
    @SuppressWarnings("unchecked")
    public <T> T getConditionParams(Class<T> clazz) {
        if (conditionParams != null && clazz.isInstance(conditionParams)) {
            return (T) conditionParams;
        }
        return null;
    }
}