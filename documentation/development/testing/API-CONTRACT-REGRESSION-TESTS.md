# API 契约回归测试清单

## 概述

本文档列出 API 契约对齐的最小回归测试集，确保系统在迭代中不会出现路由、鉴权、tracing 的断裂。

## 测试范围

### 1. 网关路由验证

**测试目标**：确保网关能正确路由到各业务服务

| 路径 | 预期状态 | 说明 |
|------|---------|------|
| `GET /api/v1/access/area/{id}` | 200/401 | 门禁服务路由 |
| `GET /api/v1/attendance/leave/{id}` | 200/401 | 考勤服务路由 |
| `GET /api/v1/visitor/appointment/{id}` | 200/401 | 访客服务路由 |
| `GET /api/v1/consume/refund/{id}` | 200/401 | 消费服务路由 |
| `GET /api/v1/workflow/instance/{id}` | 200/401 | OA 服务路由 |

### 2. 鉴权验证

**测试目标**：确保网关鉴权正确拦截未授权请求

| 场景 | 预期状态 | 说明 |
|------|---------|------|
| 无 Token 访问受保护路径 | 401 | 缺少令牌 |
| 无效 Token 访问受保护路径 | 401 | 令牌无效 |
| 刷新令牌访问业务接口 | 401 | 令牌类型不支持 |
| 有效 Token 访问受保护路径 | 200 | 鉴权通过 |
| 访问白名单路径 | 200 | 无需鉴权 |

### 3. TraceId 透传验证

**测试目标**：确保 traceId 在整个链路中连续

| 场景 | 验证点 | 说明 |
|------|--------|------|
| 入站请求 | 响应头包含 `X-Trace-Id` | 网关注入 traceId |
| 服务日志 | 日志包含 traceId（MDC） | 服务记录 traceId |
| 服务间调用 | 下游服务收到相同 traceId | traceId 透传 |
| 跨服务链路 | 前端→网关→服务A→服务B 使用相同 traceId | 完整链路追踪 |

### 4. 兼容路由验证

**测试目标**：确保旧前缀在兼容期内仍可用

| 旧路径 | 新路径 | 预期状态 | 说明 |
|--------|--------|---------|------|
| `GET /access/area/{id}` | `/api/v1/access/area/{id}` | 200/401 | 兼容重写 |
| `GET /attendance/leave/{id}` | `/api/v1/attendance/leave/{id}` | 200/401 | 兼容重写 |
| `GET /visitor/appointment/{id}` | `/api/v1/visitor/appointment/{id}` | 200/401 | 兼容重写 |
| `GET /consume/refund/{id}` | `/api/v1/consume/refund/{id}` | 200/401 | 兼容重写 |

## 执行方式

### 使用 curl 测试

```bash
# 1. 获取有效 Token
TOKEN=$(curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' \
  | jq -r '.data.accessToken')

# 2. 测试鉴权
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/access/area/1

# 3. 测试 TraceId
curl -H "Authorization: Bearer $TOKEN" \
  -H "X-Trace-Id: test-trace-123" \
  http://localhost:8080/api/v1/access/area/1 \
  -v | grep X-Trace-Id

# 4. 测试兼容路由
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/access/area/1
```

### 使用 Maven 测试

```bash
# 运行所有回归测试
mvn -pl microservices -am test

# 运行特定模块测试
mvn -pl ioedream-gateway-service -am test
mvn -pl ioedream-access-service -am test
```

## 验证清单

- [ ] 所有网关路由测试通过
- [ ] 所有鉴权测试通过
- [ ] TraceId 在整个链路中连续
- [ ] 兼容路由在 30 天内可用
- [ ] 30 天后兼容路由已下线
- [ ] 前端调用已迁移到新前缀

## 故障排查

### 问题：404 Not Found

**可能原因**：

1. 路由规则配置错误
2. 服务未启动
3. 路径前缀不匹配

**解决方案**：

1. 检查网关路由配置
2. 确认服务已启动
3. 验证 Controller 路径与网关配置一致

### 问题：401 Unauthorized

**可能原因**：

1. Token 缺失或无效
2. Token 已过期
3. Token 类型不支持

**解决方案**：

1. 重新获取有效 Token
2. 检查 Token 过期时间
3. 确认使用 ACCESS Token

### 问题：TraceId 断裂

**可能原因**：

1. 入站 Filter 未注入 traceId
2. 服务间调用未透传 traceId
3. MDC 未正确清理

**解决方案**：

1. 检查 TraceIdMdcFilter 是否启用
2. 验证 GatewayServiceClient 是否从 MDC 读取 traceId
3. 查看日志中的 traceId 是否一致

## 相关文档

- [API 契约基线](../api/API-CONTRACT-BASELINE.md)
- [网关安全基线](../security/GATEWAY-SECURITY-BASELINE.md)
- [开发指南](../technical/DEVELOPMENT_QUICK_START.md)
