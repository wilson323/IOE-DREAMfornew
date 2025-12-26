# IOE-DREAM OpenAPI/Swagger 规范

> **版本**: v1.0.0  
> **更新日期**: 2025-12-17  
> **基准**: 基于实际代码Controller扫描生成

---

## 1. API规范总览

### 1.1 基础信息

```yaml
openapi: 3.0.3
info:
  title: IOE-DREAM智慧园区API
  version: 1.0.0
  description: 智慧园区一卡通管理平台API接口规范
servers:
  - url: http://localhost:8080/api/v1
    description: 开发环境(通过网关)
  - url: https://api.ioedream.com/api/v1
    description: 生产环境
```

### 1.2 认证方式

```yaml
securityDefinitions:
  BearerAuth:
    type: http
    scheme: bearer
    bearerFormat: JWT
security:
  - BearerAuth: []
```

### 1.3 通用请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | Bearer {token} |
| X-User-Id | 否 | 网关透传用户ID |
| X-User-Roles | 否 | 网关透传用户角色 |
| X-Request-Id | 否 | 请求追踪ID |

---

## 2. 统一响应格式

### 2.1 成功响应

```json
{
  "code": 200,
  "msg": "success",
  "data": { ... },
  "timestamp": 1702800000000
}
```

### 2.2 分页响应

```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "list": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  }
}
```

### 2.3 错误响应

```json
{
  "code": "ERROR_CODE",
  "msg": "错误描述",
  "data": null,
  "timestamp": 1702800000000
}
```

---

## 3. 门禁服务API (access-service:8090)

### 3.1 设备管理 `/api/v1/access/device`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /query | 分页查询设备 | ACCESS_DEVICE_VIEW |
| GET | /{deviceId} | 查询设备详情 | ACCESS_DEVICE_VIEW |
| POST | /add | 添加设备 | ACCESS_DEVICE_ADD |
| PUT | /update | 更新设备 | ACCESS_DEVICE_UPDATE |
| DELETE | /{deviceId} | 删除设备 | ACCESS_DEVICE_DELETE |
| PUT | /{deviceId}/status | 更新设备状态 | ACCESS_DEVICE_UPDATE |

**查询参数 (GET /query)**:
```
pageNum: int (默认1)
pageSize: int (默认20)
keyword: string (设备名称/编号)
areaId: long (区域ID)
deviceStatus: string (ONLINE/OFFLINE/FAULT)
enabledFlag: int (1-启用/0-禁用)
```

### 3.2 通行记录 `/api/v1/access/record`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /query | 分页查询通行记录 | ACCESS_RECORD_VIEW |
| GET | /statistics | 通行统计 | ACCESS_RECORD_VIEW |

### 3.3 移动端 `/api/v1/access/mobile`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /unlock | 手机开门 |
| GET | /doors | 可开门列表 |
| POST | /temp-auth | 临时授权 |

### 3.4 高级功能

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/access/antipassback/config | 反潜配置 |
| POST | /api/v1/access/biometric/verify | 生物识别验证 |
| GET | /api/v1/access/permission/apply/page | 权限申请列表 |

---

## 4. 考勤服务API (attendance-service:8091)

### 4.1 考勤记录 `/api/v1/attendance/record`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /query | 分页查询考勤记录 | ATTENDANCE_MANAGE |
| GET | /statistics | 考勤统计 | ATTENDANCE_MANAGE |
| POST | /create | 创建考勤记录(设备推送) | - |

**查询参数 (GET /query)**:
```
pageNum: int (默认1)
pageSize: int (默认20)
employeeId: long (员工ID)
departmentId: long (部门ID)
startDate: date (yyyy-MM-dd)
endDate: date (yyyy-MM-dd)
status: string (NORMAL/LATE/EARLY/ABSENT)
attendanceType: string (CHECK_IN/CHECK_OUT)
```

### 4.2 排班管理 `/api/v1/attendance/schedule`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /query | 查询排班 |
| POST | /create | 创建排班 |
| PUT | /update | 更新排班 |

### 4.3 请假管理 `/api/v1/attendance/leave`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /apply | 请假申请 |
| GET | /query | 请假记录查询 |
| PUT | /{id}/approve | 审批请假 |

### 4.4 移动端 `/api/v1/attendance/mobile`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /clock-in | 打卡 |
| GET | /today | 今日打卡状态 |
| GET | /monthly | 月度考勤统计 |

---

## 5. 消费服务API (consume-service:8094)

### 5.1 消费交易 `/api/v1/consume/transaction`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /execute | 执行消费交易 | CONSUME_ACCOUNT_MANAGE/USE |
| GET | /detail/{transactionNo} | 交易详情 | CONSUME_ACCOUNT_MANAGE/USE |
| GET | /query | 分页查询消费记录 | CONSUME_ACCOUNT_MANAGE |
| GET | /device/{deviceId} | 设备详情 | CONSUME_ACCOUNT_MANAGE |
| GET | /device/statistics | 设备统计 | CONSUME_ACCOUNT_MANAGE |
| GET | /realtime-statistics | 实时统计 | CONSUME_ACCOUNT_MANAGE |

**消费交易请求体 (POST /execute)**:
```json
{
  "userId": 1001,
  "accountId": 2001,
  "deviceId": 3001,
  "areaId": 4001,
  "amount": 50.00,
  "consumeMode": "CARD"  // CARD/FACE/NFC/MOBILE
}
```

### 5.2 账户管理 `/api/v1/consume/account`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /{userId}/balance | 余额查询 |
| POST | /recharge | 充值 |
| GET | /recharge/records | 充值记录 |

### 5.3 退款管理 `/api/v1/consume/refund`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /apply | 退款申请 |
| GET | /query | 退款记录 |
| PUT | /{id}/approve | 审批退款 |

---

## 6. 访客服务API (visitor-service:8095)

### 6.1 访客预约 `/api/v1/visitor/appointment`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /create | 创建预约 |
| GET | /query | 预约列表 |
| GET | /{id} | 预约详情 |
| PUT | /{id}/cancel | 取消预约 |
| PUT | /{id}/approve | 审批预约 |

### 6.2 访客签到 `/api/v1/visitor`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /checkin | 访客签到 |
| POST | /checkout | 访客签退 |
| GET | /current | 当前在访 |

### 6.3 黑名单 `/api/v1/visitor/blacklist`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /query | 黑名单列表 |
| POST | /add | 添加黑名单 |
| DELETE | /{id} | 移除黑名单 |

---

## 7. 视频服务API (video-service:8092)

### 7.1 设备管理 `/api/v1/video/device`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /query | 设备列表 |
| GET | /{deviceId} | 设备详情 |
| POST | /add | 添加设备 |
| PUT | /update | 更新设备 |

### 7.2 视频流 `/api/v1/video/stream`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /{deviceId}/live | 获取实时流地址 |
| POST | /start | 开始推流 |
| POST | /stop | 停止推流 |

### 7.3 云台控制 `/api/v1/video/ptz`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /control | 云台控制 |
| GET | /preset/{deviceId} | 预置位列表 |
| POST | /preset | 设置预置位 |
| POST | /goto | 转到预置位 |

### 7.4 录像回放 `/api/v1/video/playback`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /query | 录像查询 |
| GET | /url | 获取回放地址 |
| POST | /download | 下载录像 |

### 7.5 AI分析 `/api/v1/video/ai`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /face/search | 人脸搜索 |
| GET | /behavior/events | 行为事件 |
| POST | /rule/config | 规则配置 |

### 7.6 告警管理 `/api/v1/video/alarm`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /query | 告警列表 |
| PUT | /{id}/handle | 处理告警 |
| GET | /statistics | 告警统计 |

---

## 8. 公共服务API (common-service:8088)

### 8.1 认证 `/api/v1/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /login | 登录 |
| POST | /logout | 登出 |
| GET | /captcha | 获取验证码 |
| POST | /refresh | 刷新令牌 |

### 8.2 员工管理 `/api/v1/employee`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /page | 分页查询 |
| GET | /{id} | 详情 |
| POST | /add | 新增 |
| PUT | /update | 更新 |
| DELETE | /{id} | 删除 |

### 8.3 部门管理 `/api/v1/department`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /tree | 部门树 |
| POST | /add | 新增部门 |
| PUT | /update | 更新部门 |

### 8.4 区域管理 `/api/v1/area`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /tree | 区域树 |
| GET | /permission/tree | 权限区域树 |

### 8.5 菜单角色 `/api/v1/menu` `/api/v1/role`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /menu/tree | 菜单树 |
| GET | /role/list | 角色列表 |
| POST | /role/permission | 角色权限配置 |

---

## 9. 设备通讯服务API (device-comm-service:8087)

### 9.1 设备同步 `/api/v1/device/sync`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /push | 设备数据推送 |
| GET | /status | 设备状态查询 |

### 9.2 协议管理 `/api/v1/protocol`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /list | 协议列表 |
| POST | /command | 发送指令 |

### 9.3 生物识别 `/api/v1/biometric`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /face/register | 人脸注册 |
| POST | /face/verify | 人脸验证 |
| POST | /finger/register | 指纹注册 |

---

## 10. 通用参数规范

### 10.1 分页参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20，最大100 |

### 10.2 时间参数

| 参数 | 格式 | 示例 |
|------|------|------|
| 日期 | yyyy-MM-dd | 2025-12-17 |
| 时间 | yyyy-MM-dd HH:mm:ss | 2025-12-17 10:30:00 |
| 时间戳 | long | 1702800000000 |

### 10.3 排序参数

| 参数 | 类型 | 示例 |
|------|------|------|
| sortField | string | createTime |
| sortOrder | string | asc/desc |

---

## 11. Swagger配置

### 11.1 SpringDoc配置

```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
  packages-to-scan: net.lab1024.sa
  paths-to-match: /api/**
```

### 11.2 各服务Swagger地址

| 服务 | Swagger UI |
|------|------------|
| 网关 | http://localhost:8080/swagger-ui.html |
| 公共服务 | http://localhost:8088/swagger-ui.html |
| 门禁服务 | http://localhost:8090/swagger-ui.html |
| 考勤服务 | http://localhost:8091/swagger-ui.html |
| 视频服务 | http://localhost:8092/swagger-ui.html |
| 消费服务 | http://localhost:8094/swagger-ui.html |
| 访客服务 | http://localhost:8095/swagger-ui.html |

---

## 12. WebSocket接口

### 12.1 实时事件推送

| 服务 | 端点 | 事件类型 |
|------|------|----------|
| 门禁 | ws://host:8090/ws/access | DOOR_STATUS, ACCESS_EVENT, ALARM |
| 考勤 | ws://host:8091/ws/attendance | CLOCK_EVENT |
| 视频 | ws://host:8092/ws/video | DEVICE_STATUS, ALARM, AI_EVENT |
| 消费 | ws://host:8094/ws/consume | TRANSACTION |

---

**📝 文档维护**: IOE-DREAM架构团队  
**📊 Controller数量**: 95个  
**📅 最后更新**: 2025-12-17
