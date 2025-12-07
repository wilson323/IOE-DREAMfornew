# 门禁系统API

<cite>
**本文档引用文件**  
- [access-api-contract.md](file://documentation/api/access/access-api-contract.md)
- [AccessDeviceController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java)
- [AccessRecordController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessRecordController.java)
- [AccessPermissionApplyController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessPermissionApplyController.java)
- [AccessMobileController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessMobileController.java)
- [AccessEmergencyPermissionController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessEmergencyPermissionController.java)
- [AccessDeviceAddForm.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/domain/form/AccessDeviceAddForm.java)
- [AccessDeviceQueryForm.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/domain/form/AccessDeviceQueryForm.java)
- [AccessDeviceVO.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/domain/vo/AccessDeviceVO.java)
- [AccessRecordVO.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/domain/vo/AccessRecordVO.java)
- [AccessDeviceService.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/AccessDeviceService.java)
- [AccessEventService.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/AccessEventService.java)
</cite>

## 目录
1. [简介](#简介)
2. [设备管理API](#设备管理api)
3. [区域管理API](#区域管理api)
4. [权限审批API](#权限审批api)
5. [通行记录查询API](#通行记录查询api)
6. [智能门禁控制API](#智能门禁控制api)
7. [典型场景调用示例](#典型场景调用示例)
8. [API通用规范](#api通用规范)

## 简介

本API文档详细描述了门禁系统的核心功能接口，包括设备管理、区域管理、权限审批、通行记录查询和智能门禁控制等模块。文档基于`access-api-contract.md`中的契约定义，并结合实际Controller代码实现，为开发者提供完整的接口参考。

**Section sources**
- [access-api-contract.md](file://documentation/api/access/access-api-contract.md)

## 设备管理API

提供门禁设备的全生命周期管理功能，包括设备的增删改查、状态管理和设备列表获取。

### 查询设备列表

**接口**: `GET /api/v1/access/device/query`  
**功能**: 分页查询门禁设备，支持多条件筛选  
**认证要求**: 需要`ACCESS_MANAGER`角色权限

**请求参数**:
- `pageNum`: 页码（从1开始，默认1）
- `pageSize`: 每页大小（默认20）
- `keyword`: 关键词（设备名称或设备编号，可选）
- `areaId`: 区域ID（可选）
- `deviceStatus`: 设备状态（ONLINE-在线、OFFLINE-离线、FAULT-故障，可选）
- `enabledFlag`: 启用状态（1-启用、0-禁用，可选）

**响应体结构**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "deviceId": 1,
        "deviceName": "主入口门禁",
        "deviceCode": "ACC001",
        "deviceType": "ACCESS",
        "areaId": 4001,
        "areaName": "办公区",
        "ipAddress": "192.168.1.100",
        "port": 8080,
        "deviceStatus": "ONLINE",
        "enabledFlag": 1,
        "lastOnlineTime": "2025-01-30T10:00:00",
        "createTime": "2025-01-29T09:00:00",
        "updateTime": "2025-01-30T10:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1738224000000
}
```

**错误响应示例**:
```json
{
  "code": "QUERY_DEVICES_ERROR",
  "message": "查询设备失败: 数据库连接异常",
  "data": null,
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessDeviceController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java#L74-L120)
- [AccessDeviceQueryForm.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/domain/form/AccessDeviceQueryForm.java)
- [AccessDeviceVO.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/domain/vo/AccessDeviceVO.java)

### 添加设备

**接口**: `POST /api/v1/access/device/add`  
**功能**: 添加新的门禁设备  
**认证要求**: 需要`ACCESS_MANAGER`角色权限

**请求体JSON Schema**:
```json
{
  "deviceName": "设备名称",
  "deviceCode": "设备编号",
  "areaId": 4001,
  "ipAddress": "192.168.1.100",
  "port": 8080,
  "remark": "设备备注"
}
```

**请求参数验证**:
- `deviceName`: 必填，长度不超过100字符
- `deviceCode`: 必填，长度不超过50字符
- `areaId`: 必填
- `ipAddress`: 必填，长度不超过50字符
- `port`: 必填

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1,
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessDeviceController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java#L149-L161)
- [AccessDeviceAddForm.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/domain/form/AccessDeviceAddForm.java)

### 更新设备

**接口**: `PUT /api/v1/access/device/update`  
**功能**: 更新门禁设备信息  
**认证要求**: 需要`ACCESS_MANAGER`角色权限

**请求体JSON Schema**:
```json
{
  "deviceId": 1,
  "deviceName": "主入口门禁",
  "deviceCode": "ACC001",
  "areaId": 4001,
  "ipAddress": "192.168.1.101",
  "port": 8080,
  "enabledFlag": 1,
  "remark": "更新后的设备备注"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessDeviceController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java#L169-L181)

### 删除设备

**接口**: `DELETE /api/v1/access/device/{deviceId}`  
**功能**: 删除门禁设备（软删除）  
**认证要求**: 需要`ACCESS_MANAGER`角色权限

**路径参数**:
- `deviceId`: 设备ID（必填）

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessDeviceController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessDeviceController.java#L189-L202)

## 区域管理API

提供门禁区域的管理功能，包括区域列表获取和区域详情查询。

### 获取区域列表

**接口**: `GET /api/v1/mobile/access/areas`  
**功能**: 获取用户有权限访问的区域列表  
**认证要求**: 需要登录认证

**请求参数**:
- `userId`: 用户ID（可选，不传则从Token获取）

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "areaId": 4001,
      "areaName": "办公区",
      "areaType": "OFFICE",
      "deviceCount": 5,
      "permissionCount": 100,
      "description": "公司主要办公区域",
      "active": true
    },
    {
      "areaId": 4002,
      "areaName": "研发区",
      "areaType": "R&D",
      "deviceCount": 3,
      "permissionCount": 50,
      "description": "研发中心区域",
      "active": true
    }
  ],
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessMobileController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessMobileController.java#L211-L217)

## 权限审批API

提供门禁权限的申请、审批和紧急权限管理功能。

### 提交权限申请

**接口**: `POST /api/v1/access/permission/apply/submit`  
**功能**: 提交门禁权限申请并启动审批流程  
**认证要求**: 需要登录认证

**请求体JSON Schema**:
```json
{
  "applicantId": 1001,
  "areaId": 4001,
  "applyType": "TEMPORARY",
  "startTime": "2025-02-01T09:00:00",
  "endTime": "2025-02-01T18:00:00",
  "reason": "项目紧急上线需要进入研发区",
  "attachments": [
    "https://example.com/attachment1.pdf",
    "https://example.com/attachment2.jpg"
  ]
}
```

**请求参数**:
- `applicantId`: 申请人ID（必填）
- `areaId`: 区域ID（必填）
- `applyType`: 申请类型（TEMPORARY-临时、PERMANENT-永久，必填）
- `startTime`: 开始时间（ISO格式，临时权限必填）
- `endTime`: 结束时间（ISO格式，临时权限必填）
- `reason`: 申请原因（必填）
- `attachments`: 附件URL列表（可选）

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "applyNo": "APPLY20250130001",
    "applicantId": 1001,
    "areaId": 4001,
    "applyType": "TEMPORARY",
    "status": "PENDING",
    "submitTime": "2025-01-30T10:00:00",
    "workflowInstanceId": "wf-12345"
  },
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessPermissionApplyController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessPermissionApplyController.java#L57-L90)

### 查询权限申请

**接口**: `POST /api/v1/access/permission/apply/query`  
**功能**: 分页查询权限申请记录  
**认证要求**: 需要登录认证

**请求体JSON Schema**:
```json
{
  "applicantId": 1001,
  "status": "PENDING",
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "pageNum": 1,
  "pageSize": 20
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "applyNo": "APPLY20250130001",
        "applicantId": 1001,
        "areaId": 4001,
        "applyType": "TEMPORARY",
        "status": "PENDING",
        "submitTime": "2025-01-30T10:00:00",
        "approvalTime": null,
        "approvalComment": null
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessPermissionApplyController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessPermissionApplyController.java)

## 通行记录查询API

提供门禁通行记录的查询、统计和创建功能。

### 分页查询通行记录

**接口**: `GET /api/v1/access/record/query`  
**功能**: 根据条件分页查询门禁记录  
**认证要求**: 需要`ACCESS_MANAGER`角色权限

**请求参数**:
- `pageNum`: 页码（从1开始，默认1）
- `pageSize`: 每页大小（默认20）
- `userId`: 用户ID（可选）
- `deviceId`: 设备ID（可选）
- `areaId`: 区域ID（可选）
- `startDate`: 开始日期（yyyy-MM-dd格式，可选）
- `endDate`: 结束日期（yyyy-MM-dd格式，可选）
- `accessResult`: 通行结果（1-成功、2-失败、3-异常，可选）

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "logId": 1001,
        "userId": 1001,
        "userName": "张三",
        "deviceId": "ACC001",
        "deviceName": "主入口门禁",
        "areaId": "4001",
        "areaName": "办公区",
        "operation": "FACE",
        "result": 1,
        "createTime": "2025-01-30T09:00:00",
        "ipAddress": "192.168.1.100",
        "remark": null
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessRecordController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessRecordController.java#L117-L157)
- [AccessRecordVO.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/domain/vo/AccessRecordVO.java)

### 获取通行记录统计

**接口**: `GET /api/v1/access/record/statistics`  
**功能**: 根据时间范围和区域获取门禁记录统计数据  
**认证要求**: 需要`ACCESS_MANAGER`角色权限

**请求参数**:
- `startDate`: 开始日期（yyyy-MM-dd格式，必填）
- `endDate`: 结束日期（yyyy-MM-dd格式，必填）
- `areaId`: 区域ID（可选）

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalRecords": 100,
    "successCount": 95,
    "failureCount": 5,
    "successRate": 95.0,
    "peakHour": "09:00",
    "peakCount": 20
  },
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessRecordController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessRecordController.java#L167-L184)

## 智能门禁控制API

提供移动端的门禁检查、验证和实时状态获取功能。

### 移动端门禁检查

**接口**: `POST /api/v1/mobile/access/check`  
**功能**: 移动端门禁权限检查  
**认证要求**: 需要登录认证

**请求体JSON Schema**:
```json
{
  "userId": 1001,
  "deviceId": 1,
  "areaId": 4001,
  "verificationType": "FACE",
  "location": "办公区主入口"
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1738224000000
}
```

**失败响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": false,
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessMobileController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessMobileController.java#L61-L77)

### 二维码验证

**接口**: `POST /api/v1/mobile/access/qr/verify`  
**功能**: 移动端二维码门禁验证  
**认证要求**: 需要登录认证

**请求体JSON Schema**:
```json
{
  "qrCode": "qrcode123456",
  "deviceId": 1
}
```

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true,
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessMobileController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessMobileController.java#L85-L99)

### 获取用户通行记录

**接口**: `GET /api/v1/mobile/access/records/{userId}`  
**功能**: 获取指定用户的通行记录  
**认证要求**: 需要登录认证

**路径参数**:
- `userId`: 用户ID（必填）

**查询参数**:
- `size`: 记录数量（默认20）

**成功响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "recordId": 1001,
      "userId": 1001,
      "deviceId": 1,
      "deviceName": "主入口门禁",
      "accessTime": "2025-01-30T09:00:00",
      "accessType": "IN",
      "accessResult": true
    }
  ],
  "timestamp": 1738224000000
}
```

**Section sources**
- [AccessMobileController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessMobileController.java#L192-L200)

## 典型场景调用示例

### 场景一：创建门禁区域并绑定设备

此场景演示如何通过API创建一个新的门禁区域并添加相关设备。

**步骤1：添加新设备**
```http
POST /api/v1/access/device/add
Content-Type: application/json
Authorization: Bearer <access_token>

{
  "deviceName": "研发区门禁",
  "deviceCode": "RD-ACC-001",
  "areaId": 4002,
  "ipAddress": "192.168.1.102",
  "port": 8080,
  "remark": "研发区主入口门禁设备"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": 2,
  "timestamp": 1738224000000
}
```

**步骤2：查询设备列表验证**
```http
GET /api/v1/access/device/query?pageNum=1&pageSize=20&keyword=研发区
Authorization: Bearer <access_token>
```

### 场景二：查询特定人员的通行记录

此场景演示如何查询特定用户在指定时间范围内的通行记录。

```http
GET /api/v1/access/record/query?pageNum=1&pageSize=10&userId=1001&startDate=2025-01-01&endDate=2025-01-31
Authorization: Bearer <access_token>
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "logId": 1001,
        "userId": 1001,
        "userName": "张三",
        "deviceId": "ACC001",
        "deviceName": "主入口门禁",
        "areaId": "4001",
        "areaName": "办公区",
        "operation": "CARD",
        "result": 1,
        "createTime": "2025-01-15T09:00:00"
      },
      {
        "logId": 1002,
        "userId": 1001,
        "userName": "张三",
        "deviceId": "ACC002",
        "deviceName": "会议室门禁",
        "areaId": "4001",
        "areaName": "办公区",
        "operation": "FACE",
        "result": 1,
        "createTime": "2025-01-15T14:00:00"
      }
    ],
    "total": 2,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "timestamp": 1738224000000
}
```

**Section sources**
- [access-api-contract.md](file://documentation/api/access/access-api-contract.md)
- [AccessRecordController.java](file://microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/controller/AccessRecordController.java)

## API通用规范

### 统一响应格式

所有API接口统一使用`ResponseDTO<T>`格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1738224000000
}
```

**字段说明**:
- `code`: 业务状态码（200表示成功）
- `message`: 提示信息
- `data`: 响应数据
- `timestamp`: 时间戳

### 错误码规范

| 错误码范围 | 类型 | 说明 |
|-----------|------|------|
| 200 | 成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、未授权、禁止访问 |
| 500-599 | 服务端错误 | 服务器内部错误 |
| 5000-5999 | 门禁模块错误 | 门禁相关业务错误 |

### 认证授权

- **移动端接口**: 使用`@SaCheckLogin`注解，需要登录认证
- **PC端接口**: 使用`@PreAuthorize`注解，需要角色权限验证

### 参数验证

- 所有POST/PUT请求使用`@Valid`注解进行参数验证
- 使用Jakarta Validation注解（`@NotNull`, `@NotBlank`, `@Size`等）

**Section sources**
- [access-api-contract.md](file://documentation/api/access/access-api-contract.md)