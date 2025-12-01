# SmartAdmin 公共模块接口契约

> 版本: v1.0.0  
> 责任人: 公共平台组  
> 最后更新: 2025-11-12

## 1. 适用范围
- 涵盖 `COMMON_MODULES` 下的所有公共能力: 权限、设备、区域、人员、实时、告警、工作流等;
- 约束 `smart-admin-api-java17-springboot3` 中的微服务接口及前端 `/@/api` 目录调用;
- 所有新增公共接口需在合并前同步更新本契约。

## 2. 通用规范
- 请求协议: HTTPS, UTF-8, JSON;
- 鉴权: Sa-Token Header `satoken` 必传, 需满足对应角色权限;
- 幂等性: 对外POST类接口需携带 `Idempotent-Key`;
- 时间: 统一使用 ISO8601, 时区 `UTC+8`;
- 错误响应结构:
  ```json
  {
    "code": "PERMISSION_DENIED",
    "message": "无访问权限",
    "traceId": "2025-11-12-001",
    "details": {}
  }
  ```

## 3. 权限模块 (smart-permission)
- 服务前缀: `/api/admin/permission`
- 关键实体: Role, Menu, Resource, DataScope
- 接口列表:
  | 方法 | 路径 | 描述 | 权限码 | 限流 | 返回值 |
  | --- | --- | --- | --- | --- | --- |
  | GET | `/roles` | 查询角色列表 | `system:role:query` | 100rpm | `RoleVO[]` |
  | POST | `/role` | 创建角色 | `system:role:add` | 20rpm | `Long` |
  | PUT | `/role/{id}` | 更新角色 | `system:role:update` | 20rpm | `Void` |
  | DELETE | `/role/{id}` | 软删除角色 | `system:role:delete` | 10rpm | `Void` |
- 关键字段说明:
  - `dataScopes`: Array<String>, 值来自数据域字典;
  - `menus`: Array<Long>, 绑定菜单ID;
  - `resources`: Array<String>, 绑定接口资源码。

## 4. 设备模块 (smart-device)
- 服务前缀: `/api/admin/device`
- 接口:
  | 方法 | 路径 | 描述 | 权限码 | 限流 | 备注 |
  | --- | --- | --- | --- | --- | --- |
  | POST | `/register` | 注册设备 | `device:manage:add` | 30rpm | 需带签名 `deviceSignature` |
  | GET | `/{deviceId}` | 查询设备详情 | `device:manage:query` | 120rpm | 支持缓存 60s |
  | PATCH | `/{deviceId}/status` | 更新设备状态 | `device:manage:update` | 40rpm | 需记录审计日志 |
- 通用字段:
  - `deviceId`: Long; `deviceType`: Enum(`ACCESS`,`POS`,`CAMERA`);
  - `onlineStatus`: Enum(`ONLINE`,`OFFLINE`,`FAULT`);
  - `firmwareVersion`: String, 语义化版本。

## 5. 区域模块 (smart-area)
- 服务前缀: `/api/admin/area`
- 主要接口:
  | 方法 | 路径 | 描述 | 权限码 | 限流 |
  | --- | --- | --- | --- | --- |
  | GET | `/tree` | 区域树查询 | `area:query` | 60rpm |
  | POST | `/batch-link` | 批量绑定区域与设备 | `area:config` | 15rpm |
- 特殊约束: 区域深度 ≤ 6, 禁止循环依赖; 批量接口最大 100 条。

## 6. 人员模块 (smart-person)
- 服务前缀: `/api/admin/person`
- 主要接口: CRUD + 权限授权;
- 数据字段: `personId`, `credentialType`, `credentialNo`, `validTimeRange`, `status`;
- 数据脱敏: `credentialNo` 返回时仅展示后4位。

## 7. 实时推送模块 (smart-realtime)
- WebSocket 入口: `wss://{host}/ws/realtime`
- 订阅主题: `device-status`, `alarm-event`, `workflow-task`;
- 心跳: 30s 客户端心跳包 `{"type":"PING"}`;
- 回压: 当未消费队列超过 500 条, 服务端返回 `429` 并要求重新订阅。

## 8. 告警模块 (smart-alert)
- 服务前缀: `/api/admin/alert`
- 核心接口:
  | 方法 | 路径 | 描述 | 权限码 | 限流 |
  | --- | --- | --- | --- | --- |
  | POST | `/rule` | 新增告警规则 | `alert:rule:add` | 10rpm |
  | GET | `/events` | 查询告警事件 | `alert:event:query` | 120rpm |
  | POST | `/event/{id}/ack` | 确认告警 | `alert:event:ack` | 60rpm |
- 事件字段: `eventId`, `alertLevel`, `sourceType`, `occurredAt`, `status`, `context`。

## 9. 工作流模块 (smart-workflow)
- 服务前缀: `/api/admin/workflow`
- 集成 Flowable 引擎, 统一 BPMN 模版;
- 关键接口:
  | 方法 | 路径 | 描述 | 权限码 | 限流 |
  | --- | --- | --- | --- | --- |
  | POST | `/process/deploy` | 部署流程 | `workflow:deploy` | 5rpm |
  | POST | `/process/start` | 发起流程 | `workflow:start` | 30rpm |
  | POST | `/task/{taskId}/complete` | 完成任务 | `workflow:task:complete` | 60rpm |
- 回调: 所有流程完成需回调 `/api/internal/workflow/callback`。

## 10. 错误码规范
- 前缀 `P-` 权限、`D-` 设备、`A-` 告警、`W-` 工作流;
- 示例:
  | 错误码 | HTTP 状态 | 描述 |
  | --- | --- | --- |
  | `P-401-001` | 401 | 未登录或Token失效 |
  | `D-403-002` | 403 | 设备操作权限不足 |
  | `A-429-003` | 429 | 告警确认超出限流 |
  | `W-500-001` | 500 | 流程引擎异常 |

## 11. 变更流程
1. 提交接口设计评审, 附示例与测试用例;
2. 更新本契约与 `SmartAdmin规范体系_v4/CHANGELOG.md`;
3. 同步前端 `/@/api` 请求封装与文档索引。

## 12. 附录: 示例调用
```http
POST /api/admin/device/register HTTP/1.1
Host: api.smartadmin.internal
satoken: xxx
Idempotent-Key: 2025-11-12-0001
Content-Type: application/json

{
  "deviceId": 9001,
  "deviceName": "北门闸机",
  "deviceType": "ACCESS",
  "firmwareVersion": "3.2.1",
  "signature": "BASE64..."
}
```

```json
{
  "code": "OK",
  "data": 9001,
  "traceId": "req-2025-11-12-0001"
}
```

> 注意: 所有示例需结合真实环境变量, 禁止在生产日志中打印敏感字段。
