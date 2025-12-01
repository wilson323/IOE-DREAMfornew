package net.lab1024.sa.base.module.support.auth;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

/**
 * 统一鉴权上下文：承载登录用户的数据域与资源能力快照
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AuthorizationContext {
    private Long userId;
    private String username;
    private String userCode; // 用户编码
    private String userName; // 用户姓名（支持builder）
    private Set<String> roleCodes;
    private Set<String> resourceCodes;
    private List<Long> areaIds; // 数据域：区域
    private Set<Long> areaIdSet; // 数据域：区域（Set版本）
    private List<Long> deptIds; // 数据域：部门
    private Set<Long> deptIdSet; // 数据域：部门（Set版本）
    private Long departmentId; // 当前部门ID
    private Boolean superAdmin; // 是否超级管理员
    private String resourceCode; // 请求的资源编码
    private String requestedAction; // 请求的操作动作
    private String dataScope; // 数据域范围
    private String clientIp; // 客户端IP
    private String requestPath; // 请求路径
    private long requestTime; // 请求时间
    private Long validStartTime; // 有效开始时间
    private Long validEndTime; // 有效结束时间
    private Map<String, Object> attributes; // 扩展属性

    /**
     * 检查上下文是否有效
     */
    public boolean isValid() {
        return userId != null && StringUtils.hasText(resourceCode);
    }

    /**
     * 检查是否在有效期内
     */
    public boolean isInValidPeriod() {
        return true; // 简化实现，总是返回true
    }

    /**
     * 获取是否超级管理员（默认false）
     */
    public boolean getIsSuperAdmin() {
        return Boolean.TRUE.equals(superAdmin);
    }

    /**
     * 设置超级管理员标识
     */
    public void setIsSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    /**
     * 获取区域ID集合（Set版本）
     */
    public Set<Long> getAreaIdSet() {
        return areaIds != null ? new HashSet<>(areaIds) : new HashSet<>();
    }

    /**
     * 获取部门ID集合（Set版本）
     */
    public Set<Long> getDeptIdSet() {
        return deptIds != null ? new HashSet<>(deptIds) : new HashSet<>();
    }

    /**
     * 获取扩展属性
     */
    public Object getAttribute(String key) {
        return attributes != null ? attributes.get(key) : null;
    }

    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }
}
