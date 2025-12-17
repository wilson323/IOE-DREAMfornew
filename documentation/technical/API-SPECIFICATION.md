# IOE-DREAM OpenAPI/Swagger 规范

> **版本**: v1.0.0  
> **更新日期**: 2025-12-17

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
    description: 开发环境
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
  "code": 40001,
  "msg": "参数校验失败",
  "data": null,
  "timestamp": 1702800000000
}
```

---

## 3. 各模块API端点

### 3.1 公共服务 (common-service)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/login | 用户登录 |
| POST | /auth/logout | 用户登出 |
| GET | /auth/captcha | 获取验证码 |
| GET | /employee/page | 员工分页查询 |
| POST | /employee | 新增员工 |
| PUT | /employee/{id} | 更新员工 |
| DELETE | /employee/{id} | 删除员工 |
| GET | /department/tree | 部门树 |
| GET | /role/list | 角色列表 |
| GET | /menu/tree | 菜单树 |

### 3.2 门禁服务 (access-service)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /access/device/page | 设备分页 |
| POST | /access/device | 新增设备 |
| POST | /access/device/{id}/unlock | 远程开门 |
| GET | /access/event/page | 事件查询 |
| GET | /access/area/tree | 区域树 |
| POST | /access/permission | 权限分配 |

### 3.3 考勤服务 (attendance-service)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /attendance/clock | 打卡 |
| GET | /attendance/record/page | 打卡记录 |
| GET | /attendance/schedule/page | 排班查询 |
| POST | /attendance/leave | 请假申请 |
| GET | /attendance/statistics | 考勤统计 |

### 3.4 消费服务 (consume-service)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /consume/transaction | 消费交易 |
| GET | /consume/record/page | 消费记录 |
| POST | /consume/recharge | 充值 |
| GET | /consume/balance/{userId} | 余额查询 |

### 3.5 访客服务 (visitor-service)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /visitor/appointment | 访客预约 |
| GET | /visitor/appointment/page | 预约列表 |
| POST | /visitor/checkin | 访客签到 |
| POST | /visitor/checkout | 访客签退 |

### 3.6 视频服务 (video-service)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /video/device/page | 设备列表 |
| GET | /video/stream/{deviceId} | 获取流地址 |
| POST | /video/ptz/control | 云台控制 |
| GET | /video/playback | 录像回放 |
| GET | /video/alarm/page | 告警列表 |

---

## 4. 通用参数规范

### 4.1 分页参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页条数，默认20，最大100 |

### 4.2 时间参数

| 参数 | 格式 | 示例 |
|------|------|------|
| 日期 | yyyy-MM-dd | 2025-12-17 |
| 时间 | yyyy-MM-dd HH:mm:ss | 2025-12-17 10:30:00 |
| 时间戳 | long | 1702800000000 |

### 4.3 排序参数

| 参数 | 类型 | 示例 |
|------|------|------|
| sortField | string | createTime |
| sortOrder | string | asc/desc |

---

## 5. Swagger配置

### 5.1 SpringDoc配置

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

### 5.2 访问地址

| 环境 | Swagger UI | API Docs |
|------|------------|----------|
| 开发 | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |

---

**📝 文档维护**: IOE-DREAM架构团队
