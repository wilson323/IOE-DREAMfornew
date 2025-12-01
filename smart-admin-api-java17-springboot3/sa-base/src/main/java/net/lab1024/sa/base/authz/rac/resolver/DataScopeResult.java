package net.lab1024.sa.base.authz.rac.resolver;

import java.util.Set;
import java.util.Map;

/**
 * 数据域解析结果类
 * 用于封装数据权限解析的结果信息
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-23
 */
public class DataScopeResult {

    // 构造函数
    public DataScopeResult() {}

    public DataScopeResult(boolean allDataAccess, Set<Long> accessibleAreaIds, Set<Long> accessibleDeptIds,
                          Set<Long> accessibleUserIds, Map<String, Object> customRules) {
        this.allDataAccess = allDataAccess;
        this.accessibleAreaIds = accessibleAreaIds;
        this.accessibleDeptIds = accessibleDeptIds;
        this.accessibleUserIds = accessibleUserIds;
        this.customRules = customRules;
    }

    /**
     * 是否允许访问所有数据
     */
    private boolean allDataAccess = false;

    /**
     * 可访问的区域ID集合
     */
    private Set<Long> accessibleAreaIds = Set.of();

    /**
     * 可访问的部门ID集合
     */
    private Set<Long> accessibleDeptIds = Set.of();

    /**
     * 可访问的用户ID集合
     */
    private Set<Long> accessibleUserIds = Set.of();

    /**
     * 自定义权限规则
     */
    private Map<String, Object> customRules = Map.of();

    /**
     * 解析耗时（毫秒）
     */
    private long resolveTime = 0L;

    /**
     * 缓存命中标识
     */
    private boolean cacheHit = false;

    // Getter 和 Setter 方法
    public boolean isAllDataAccess() { return allDataAccess; }
    public void setAllDataAccess(boolean allDataAccess) { this.allDataAccess = allDataAccess; }

    public Set<Long> getAccessibleAreaIds() { return accessibleAreaIds; }
    public void setAccessibleAreaIds(Set<Long> accessibleAreaIds) { this.accessibleAreaIds = accessibleAreaIds; }

    public Set<Long> getAccessibleDeptIds() { return accessibleDeptIds; }
    public void setAccessibleDeptIds(Set<Long> accessibleDeptIds) { this.accessibleDeptIds = accessibleDeptIds; }

    public Set<Long> getAccessibleUserIds() { return accessibleUserIds; }
    public void setAccessibleUserIds(Set<Long> accessibleUserIds) { this.accessibleUserIds = accessibleUserIds; }

    public Map<String, Object> getCustomRules() { return customRules; }
    public void setCustomRules(Map<String, Object> customRules) { this.customRules = customRules; }

    public long getResolveTime() { return resolveTime; }
    public void setResolveTime(long resolveTime) { this.resolveTime = resolveTime; }

    public boolean isCacheHit() { return cacheHit; }
    public void setCacheHit(boolean cacheHit) { this.cacheHit = cacheHit; }

    // Builder 类支持
    public static class Builder {
        private boolean allDataAccess = false;
        private Set<Long> accessibleAreaIds = Set.of();
        private Set<Long> accessibleDeptIds = Set.of();
        private Set<Long> accessibleUserIds = Set.of();
        private Map<String, Object> customRules = Map.of();

        public Builder allDataAccess(boolean allDataAccess) { this.allDataAccess = allDataAccess; return this; }
        public Builder accessibleAreaIds(Set<Long> accessibleAreaIds) { this.accessibleAreaIds = accessibleAreaIds; return this; }
        public Builder accessibleDeptIds(Set<Long> accessibleDeptIds) { this.accessibleDeptIds = accessibleDeptIds; return this; }
        public Builder accessibleUserIds(Set<Long> accessibleUserIds) { this.accessibleUserIds = accessibleUserIds; return this; }
        public Builder customRules(Map<String, Object> customRules) { this.customRules = customRules; return this; }

        public DataScopeResult build() {
            DataScopeResult result = new DataScopeResult();
            result.allDataAccess = this.allDataAccess;
            result.accessibleAreaIds = this.accessibleAreaIds;
            result.accessibleDeptIds = this.accessibleDeptIds;
            result.accessibleUserIds = this.accessibleUserIds;
            result.customRules = this.customRules;
            return result;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 检查是否有指定数据的访问权限
     *
     * @param areaId   区域ID（可选）
     * @param deptId   部门ID（可选）
     * @param userId   用户ID（可选）
     * @return 是否有访问权限
     */
    public boolean hasDataAccess(Long areaId, Long deptId, Long userId) {
        // 全部数据权限
        if (allDataAccess) {
            return true;
        }

        // 区域权限检查
        if (areaId != null && !accessibleAreaIds.isEmpty()) {
            if (!accessibleAreaIds.contains(areaId)) {
                return false;
            }
        }

        // 部门权限检查
        if (deptId != null && !accessibleDeptIds.isEmpty()) {
            if (!accessibleDeptIds.contains(deptId)) {
                return false;
            }
        }

        // 用户权限检查
        if (userId != null && !accessibleUserIds.isEmpty()) {
            if (!accessibleUserIds.contains(userId)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检查是否可以访问指定区域
     *
     * @param areaId 区域ID
     * @return 是否可以访问
     */
    public boolean canAccessArea(Long areaId) {
        return allDataAccess || (accessibleAreaIds != null && accessibleAreaIds.contains(areaId));
    }

    /**
     * 检查是否可以访问指定部门
     *
     * @param deptId 部门ID
     * @return 是否可以访问
     */
    public boolean canAccessDept(Long deptId) {
        return allDataAccess || (accessibleDeptIds != null && accessibleDeptIds.contains(deptId));
    }

    /**
     * 检查是否可以访问指定用户
     *
     * @param userId 用户ID
     * @return 是否可以访问
     */
    public boolean canAccessUser(Long userId) {
        return allDataAccess || (accessibleUserIds != null && accessibleUserIds.contains(userId));
    }

    /**
     * 获取权限范围描述
     *
     * @return 权限范围描述
     */
    public String getScopeDescription() {
        if (allDataAccess) {
            return "全部数据权限";
        }

        StringBuilder desc = new StringBuilder();
        if (!accessibleAreaIds.isEmpty()) {
            desc.append("区域权限: ").append(accessibleAreaIds.size()).append("个区域");
        }
        if (!accessibleDeptIds.isEmpty()) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("部门权限: ").append(accessibleDeptIds.size()).append("个部门");
        }
        if (!accessibleUserIds.isEmpty()) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("用户权限: ").append(accessibleUserIds.size()).append("个用户");
        }

        return desc.length() > 0 ? desc.toString() : "无数据权限";
    }

    /**
     * 是否有任何权限限制
     *
     * @return 是否有权限限制
     */
    public boolean hasAnyRestriction() {
        return !allDataAccess &&
               (!accessibleAreaIds.isEmpty() ||
                !accessibleDeptIds.isEmpty() ||
                !accessibleUserIds.isEmpty());
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DataScopeResult that = (DataScopeResult) obj;

        return allDataAccess == that.allDataAccess &&
               accessibleAreaIds.equals(that.accessibleAreaIds) &&
               accessibleDeptIds.equals(that.accessibleDeptIds) &&
               accessibleUserIds.equals(that.accessibleUserIds) &&
               customRules.equals(that.customRules);
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(allDataAccess);
        result = 31 * result + accessibleAreaIds.hashCode();
        result = 31 * result + accessibleDeptIds.hashCode();
        result = 31 * result + accessibleUserIds.hashCode();
        result = 31 * result + customRules.hashCode();
        return result;
    }
}