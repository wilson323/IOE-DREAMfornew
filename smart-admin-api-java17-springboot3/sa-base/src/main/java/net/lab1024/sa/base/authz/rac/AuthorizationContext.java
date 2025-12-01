package net.lab1024.sa.base.authz.rac;

import java.util.Set;
import java.util.Map;

/**
 * 授权上下文类
 * 用于封装用户权限和数据域访问的相关信息
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-23
 */
public class AuthorizationContext {

    // 构造函数
    public AuthorizationContext() {}

    public AuthorizationContext(Long userId, String username, Set<String> roleCodes, DataScope dataScope,
                              Set<Long> areaIds, Set<Long> deptIds, Set<Long> userIds, Map<String, Object> customRules,
                              boolean superAdmin, String sessionId, String clientIp, Long requestTime) {
        this.userId = userId;
        this.username = username;
        this.roleCodes = roleCodes;
        this.dataScope = dataScope;
        this.areaIds = areaIds;
        this.deptIds = deptIds;
        this.userIds = userIds;
        this.customRules = customRules;
        this.superAdmin = superAdmin;
        this.sessionId = sessionId;
        this.clientIp = clientIp;
        this.requestTime = requestTime;
    }

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色代码集合
     */
    private Set<String> roleCodes = Set.of();

    /**
     * 数据域范围
     */
    private DataScope dataScope;

    /**
     * 可访问的区域ID集合
     */
    private Set<Long> areaIds = Set.of();

    /**
     * 可访问的部门ID集合
     */
    private Set<Long> deptIds = Set.of();

    /**
     * 可访问的用户ID集合
     */
    private Set<Long> userIds = Set.of();

    /**
     * 自定义权限规则
     */
    private Map<String, Object> customRules = Map.of();

    /**
     * 是否为超级管理员
     */
    private boolean superAdmin = false;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 请求时间戳
     */
    private Long requestTime;

    // Getter 和 Setter 方法
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Set<String> getRoleCodes() { return roleCodes; }
    public void setRoleCodes(Set<String> roleCodes) { this.roleCodes = roleCodes; }

    public DataScope getDataScope() { return dataScope; }
    public void setDataScope(DataScope dataScope) { this.dataScope = dataScope; }

    public Set<Long> getAreaIds() { return areaIds; }
    public void setAreaIds(Set<Long> areaIds) { this.areaIds = areaIds; }

    public Set<Long> getDeptIds() { return deptIds; }
    public void setDeptIds(Set<Long> deptIds) { this.deptIds = deptIds; }

    public Set<Long> getUserIds() { return userIds; }
    public void setUserIds(Set<Long> userIds) { this.userIds = userIds; }

    public Map<String, Object> getCustomRules() { return customRules; }
    public void setCustomRules(Map<String, Object> customRules) { this.customRules = customRules; }

    public boolean isSuperAdmin() { return superAdmin; }
    public void setSuperAdmin(boolean superAdmin) { this.superAdmin = superAdmin; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public Long getRequestTime() { return requestTime; }
    public void setRequestTime(Long requestTime) { this.requestTime = requestTime; }

    /**
     * 检查是否有指定的角色
     *
     * @param roleCode 角色代码
     * @return 是否有角色
     */
    public boolean hasRole(String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }

    /**
     * 检查是否有任意一个指定的角色
     *
     * @param roleCodes 角色代码数组
     * @return 是否有任意一个角色
     */
    public boolean hasAnyRole(String... roleCodes) {
        if (this.roleCodes == null || roleCodes == null) {
            return false;
        }
        for (String roleCode : roleCodes) {
            if (this.roleCodes.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否有所有指定的角色
     *
     * @param roleCodes 角色代码数组
     * @return 是否有所有角色
     */
    public boolean hasAllRoles(String... roleCodes) {
        if (this.roleCodes == null || roleCodes == null) {
            return false;
        }
        for (String roleCode : roleCodes) {
            if (!this.roleCodes.contains(roleCode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取有效的数据域范围
     *
     * @return 数据域枚举
     */
    public DataScope getEffectiveDataScope() {
        return superAdmin ? DataScope.ALL : (dataScope != null ? dataScope : DataScope.NONE);
    }

    /**
     * 检查是否可以访问指定区域的数据
     *
     * @param areaId 区域ID
     * @return 是否可以访问
     */
    public boolean canAccessArea(Long areaId) {
        if (superAdmin || getEffectiveDataScope() == DataScope.ALL) {
            return true;
        }
        return areaIds != null && areaIds.contains(areaId);
    }

    /**
     * 检查是否可以访问指定部门的数据
     *
     * @param deptId 部门ID
     * @return 是否可以访问
     */
    public boolean canAccessDept(Long deptId) {
        if (superAdmin || getEffectiveDataScope() == DataScope.ALL) {
            return true;
        }
        return deptIds != null && deptIds.contains(deptId);
    }

    /**
     * 检查是否可以访问指定用户的数据
     *
     * @param targetUserId 目标用户ID
     * @return 是否可以访问
     */
    public boolean canAccessUser(Long targetUserId) {
        if (superAdmin || getEffectiveDataScope() == DataScope.ALL) {
            return true;
        }
        if (getEffectiveDataScope() == DataScope.SELF) {
            return targetUserId != null && targetUserId.equals(this.userId);
        }
        return userIds != null && userIds.contains(targetUserId);
    }

    /**
     * 获取自定义规则值
     *
     * @param key 规则键
     * @param defaultValue 默认值
     * @return 规则值
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomRule(String key, T defaultValue) {
        if (customRules == null || !customRules.containsKey(key)) {
            return defaultValue;
        }
        Object value = customRules.get(key);
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /**
     * 检查请求是否过期
     *
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否过期
     */
    public boolean isExpired(long timeoutMs) {
        if (requestTime == null) {
            return true;
        }
        return System.currentTimeMillis() - requestTime > timeoutMs;
    }

    /**
     * Builder 类支持
     */
    public static class Builder {
        private Long userId;
        private String username;
        private Set<String> roleCodes = Set.of();
        private DataScope dataScope;
        private Set<Long> areaIds = Set.of();
        private Set<Long> deptIds = Set.of();
        private Set<Long> userIds = Set.of();
        private Map<String, Object> customRules = Map.of();
        private boolean superAdmin = false;
        private String sessionId;
        private String clientIp;
        private Long requestTime;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder roleCodes(Set<String> roleCodes) { this.roleCodes = roleCodes; return this; }
        public Builder dataScope(DataScope dataScope) { this.dataScope = dataScope; return this; }
        public Builder areaIds(Set<Long> areaIds) { this.areaIds = areaIds; return this; }
        public Builder deptIds(Set<Long> deptIds) { this.deptIds = deptIds; return this; }
        public Builder userIds(Set<Long> userIds) { this.userIds = userIds; return this; }
        public Builder customRules(Map<String, Object> customRules) { this.customRules = customRules; return this; }
        public Builder superAdmin(boolean superAdmin) { this.superAdmin = superAdmin; return this; }
        public Builder sessionId(String sessionId) { this.sessionId = sessionId; return this; }
        public Builder clientIp(String clientIp) { this.clientIp = clientIp; return this; }
        public Builder requestTime(Long requestTime) { this.requestTime = requestTime; return this; }

        public AuthorizationContext build() {
            return new AuthorizationContext(userId, username, roleCodes, dataScope, areaIds, deptIds,
                                          userIds, customRules, superAdmin, sessionId, clientIp, requestTime);
        }
    }

    /**
     * 创建Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建副本
     *
     * @return 授权上下文副本
     */
    public AuthorizationContext copy() {
        return new AuthorizationContext(userId, username, roleCodes, dataScope, areaIds, deptIds,
                                      userIds, customRules, superAdmin, sessionId, clientIp, requestTime);
    }

    /**
     * 数据域枚举
     */
    public enum DataScope {
        /**
         * 全部数据
         */
        ALL,

        /**
         * 区域数据
         */
        AREA,

        /**
         * 部门数据
         */
        DEPT,

        /**
         * 个人数据
         */
        SELF,

        /**
         * 自定义数据
         */
        CUSTOM,

        /**
         * 无数据权限
         */
        NONE
    }
}