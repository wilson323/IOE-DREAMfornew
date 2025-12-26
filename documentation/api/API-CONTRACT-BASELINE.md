# API 契约基线

## 概述

本文档定义 IOE-DREAM 平台的统一 API 契约基线，确保前端、网关、微服务之间的调用路径一致。

## 统一 API 前缀

**Canonical API Prefix**: `/api/v1`

所有对外 API 必须遵循此前缀规范。

### 前缀映射

| 服务 | 前缀 | 说明 |
|------|------|------|
| 网关 | `/api/v1` | 统一入口，所有请求必须通过网关 |
| 门禁服务 | `/api/v1/access` | 门禁权限管理 |
| 考勤服务 | `/api/v1/attendance` | 考勤管理 |
| 访客服务 | `/api/v1/visitor` | 访客管理 |
| 消费服务 | `/api/v1/consume` | 消费管理 |
| OA 服务 | `/api/v1/workflow` | 工作流管理 |
| 身份服务 | `/api/v1/identity` | 身份认证与授权 |

## 服务间调用规范

### GatewayServiceClient

所有服务间调用必须通过 `GatewayServiceClient`，调用路径格式：

```text
http://{gateway-url}/api/v1/{service}/{resource}
```

**示例**：

```java
// 调用门禁服务
ResponseDTO<AreaEntity> result = gatewayServiceClient.get(
    "/access/area/{areaId}",
    new TypeReference<ResponseDTO<AreaEntity>>() {}
);

// 调用考勤服务
ResponseDTO<AttendanceLeaveEntity> result = gatewayServiceClient.post(
    "/attendance/leave/apply",
    leaveForm,
    new TypeReference<ResponseDTO<AttendanceLeaveEntity>>() {}
);
```

### DirectServiceClient

仅在特殊场景（如内部管理接口）使用，调用路径格式：

```text
http://{service-url}:{port}/api/v1/{resource}
```

## 兼容性策略

### 兼容窗口

旧 API 前缀的兼容期为 **30 天**，之后将下线。

### 兼容路由

网关在兼容期内提供以下路由重写：

| 旧前缀 | 新前缀 | 状态 |
|--------|--------|------|
| `/access/**` | `/api/v1/access/**` | 兼容中（30天） |
| `/attendance/**` | `/api/v1/attendance/**` | 兼容中（30天） |
| `/visitor/**` | `/api/v1/visitor/**` | 兼容中（30天） |
| `/consume/**` | `/api/v1/consume/**` | 兼容中（30天） |

## 前端调用规范

### 基础 URL 配置

前端应使用统一的 `baseURL` 配置：

```javascript
// 管理端
const apiClient = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
});

// 移动端
const mobileApiClient = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
});
```

### 避免硬编码

**禁止**：

```javascript
// ❌ 错误：硬编码域名
fetch('http://localhost:8080/api/v1/users')
fetch('http://example.com/api/v1/users')
```

**推荐**：

```javascript
// ✅ 正确：使用相对路径或配置的 baseURL
fetch('/api/v1/users')
apiClient.get('/users')
```

## TraceId 传播规范

### 请求头

所有请求必须包含 `X-Trace-Id` 头：

```text
X-Trace-Id: {uuid}
```

### 传播链路

```text
前端 → 网关 → 服务A → 服务B → 数据库
 ↓      ↓       ↓       ↓
X-Trace-Id 透传整个链路
```

### 实现要点

1. **入站注入**：网关和服务入站 Filter 注入 `X-Trace-Id` 到 MDC
2. **出站透传**：服务间调用时从 MDC 读取 `traceId` 并添加到请求头
3. **日志关联**：所有日志自动包含 `traceId`（通过 MDC）

## 验证清单

- [ ] 所有 Controller 路径使用 `/api/v1` 前缀
- [ ] 网关路由规则与 Controller 路径一致
- [ ] 服务间调用使用 `GatewayServiceClient` 且路径正确
- [ ] 前端调用使用统一 `baseURL`，无硬编码域名
- [ ] 所有请求包含 `X-Trace-Id` 头
- [ ] 兼容路由在 30 天后下线

## 常见问题

### Q: 为什么要统一 API 前缀？

**A**: 统一前缀便于：

- 网关路由管理
- 前端 API 调用配置
- 服务间调用路径一致
- 安全策略统一应用

### Q: 旧前缀何时下线？

**A**: 兼容期为 30 天，之后旧前缀将被删除。请在此期间完成迁移。

### Q: 如何处理特殊的内部接口？

**A**: 内部接口仍需遵循 `/api/v1` 前缀，通过网关白名单或 RBAC 进行访问控制。

## 相关文档

- [网关安全基线](./GATEWAY-SECURITY-BASELINE.md)
- [RBAC 规则示例](./RBAC-Rules-Examples.md)
- [开发指南](../technical/DEVELOPMENT_QUICK_START.md)
