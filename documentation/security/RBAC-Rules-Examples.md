# RBAC 规则示例配置

## 概述

本文档提供网关 RBAC（基于角色的访问控制）规则的推荐配置示例。

## 配置格式

RBAC 规则通过 `security.rbac` 配置项定义，支持以下结构：

```yaml
security:
  rbac:
    enabled: true  # 启用 RBAC 检查
    rules:
      - path: "/api/v1/admin/**"
        requiredRoles: ["ADMIN"]
        requiredPermissions: []
      - path: "/api/v1/users/**"
        requiredRoles: ["USER", "ADMIN"]
        requiredPermissions: ["user:read"]
```

## 示例场景

### 1. 管理员专属接口

```yaml
security:
  rbac:
    enabled: true
    rules:
      # 系统管理接口
      - path: "/api/v1/admin/system/**"
        requiredRoles: ["ADMIN"]
        requiredPermissions: ["system:manage"]
      
      # 用户管理接口
      - path: "/api/v1/admin/users/**"
        requiredRoles: ["ADMIN"]
        requiredPermissions: ["user:manage"]
      
      # 角色管理接口
      - path: "/api/v1/admin/roles/**"
        requiredRoles: ["ADMIN"]
        requiredPermissions: ["role:manage"]
```

### 2. 部门经理权限

```yaml
security:
  rbac:
    enabled: true
    rules:
      # 部门员工管理
      - path: "/api/v1/department/employees/**"
        requiredRoles: ["MANAGER", "ADMIN"]
        requiredPermissions: ["employee:manage"]
      
      # 部门考勤管理
      - path: "/api/v1/attendance/department/**"
        requiredRoles: ["MANAGER", "ADMIN"]
        requiredPermissions: ["attendance:manage"]
```

### 3. 普通用户权限

```yaml
security:
  rbac:
    enabled: true
    rules:
      # 个人信息查看
      - path: "/api/v1/users/profile"
        requiredRoles: ["USER", "MANAGER", "ADMIN"]
        requiredPermissions: ["profile:read"]
      
      # 请假申请
      - path: "/api/v1/attendance/leave/apply"
        requiredRoles: ["USER", "MANAGER", "ADMIN"]
        requiredPermissions: ["leave:apply"]
      
      # 权限申请
      - path: "/api/v1/access/permission/apply"
        requiredRoles: ["USER", "MANAGER", "ADMIN"]
        requiredPermissions: ["permission:apply"]
```

### 4. 生产环境推荐配置

```yaml
security:
  rbac:
    enabled: true
    rules:
      # 系统管理接口（仅 ADMIN）
      - path: "/api/v1/admin/**"
        requiredRoles: ["ADMIN"]
        requiredPermissions: []
      
      # 部门管理接口（MANAGER 及以上）
      - path: "/api/v1/department/**"
        requiredRoles: ["MANAGER", "ADMIN"]
        requiredPermissions: []
      
      # 业务接口（所有认证用户）
      - path: "/api/v1/**"
        requiredRoles: ["USER", "MANAGER", "ADMIN"]
        requiredPermissions: []
```

## 配置注意事项

1. **路径匹配**：使用 Ant 风格的路径匹配（如 `/api/v1/admin/**`）
2. **角色优先级**：列出的角色为 OR 关系，即用户拥有任一角色即可通过
3. **权限检查**：权限为 AND 关系，即用户需要同时拥有所有列出的权限
4. **默认行为**：`enabled=false` 时，所有规则不生效，所有认证用户可访问
5. **白名单优先**：白名单路径优先于 RBAC 规则，不需要鉴权

## 最佳实践

1. **最小权限原则**：仅授予必要的角色和权限
2. **定期审查**：定期审查和更新 RBAC 规则
3. **文档维护**：在配置文件中添加注释说明规则用途
4. **测试验证**：在生产部署前充分测试 RBAC 规则
5. **监控告警**：监控 403 错误，及时发现权限问题

## 故障排查

### 问题：用户被拒绝访问（403）

**可能原因**：

1. 用户角色不在 `requiredRoles` 中
2. 用户缺少 `requiredPermissions` 中的权限
3. RBAC 规则配置错误

**解决方案**：

1. 检查用户的实际角色和权限
2. 验证 RBAC 规则配置是否正确
3. 查看网关日志中的 RBAC 检查信息

### 问题：规则不生效

**可能原因**：

1. `security.rbac.enabled=false`
2. 路径不匹配
3. 配置文件未正确加载

**解决方案**：

1. 确认 `enabled=true`
2. 检查路径匹配规则
3. 重启网关服务

## 相关文档

- [网关安全基线](./GATEWAY-SECURITY-BASELINE.md)
- [JWT 鉴权指南](./JWT-AUTHENTICATION.md)
- [CORS 配置指南](./CORS-CONFIGURATION.md)
