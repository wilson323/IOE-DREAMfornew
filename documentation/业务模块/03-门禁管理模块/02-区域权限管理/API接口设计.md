# 区域权限管理模块 - API接口设计

## 📋 模块概述

**模块名称**: 02-区域权限管理
**模块编码**: ACCESS-PERMISSION-MGMT
**版本**: v1.0.0
**创建日期**: 2025-12-17

## 🔌 API设计原则

### RESTful设计规范
- **资源导向**: 使用名词表示资源，避免动词
- **HTTP语义**: 正确使用GET、POST、PUT、DELETE、PATCH方法
- **状态码规范**: 标准HTTP状态码 + 业务状态码
- **统一响应格式**: ResponseDTO包装所有响应
- **版本管理**: 通过URL路径进行版本控制 `/api/v1/`

### 安全设计规范
- **身份认证**: 基于JWT Token的身份验证
- **权限校验**: 基于RBAC的接口权限控制
- **数据加密**: 敏感数据传输和存储加密
- **审计日志**: 完整记录接口调用和权限变更
- **防护机制**: 接口防刷、参数校验、SQL注入防护

## 🏗️ 区域管理API

### 1. 区域结构管理

#### 1.1 区域查询接口

```http
GET /api/v1/access/areas
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "parentId": "parent001",
  "areaType": "BUILDING",
  "areaStatus": 1,
  "keyword": "研发",
  "pageNum": 1,
  "pageSize": 20
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "list": [
      {
        "areaId": "area001",
        "areaCode": "BUILD-A-001",
        "areaName": "A栋研发楼",
        "areaType": "BUILDING",
        "areaStatus": 1,
        "parentId": "parent001",
        "level": 2,
        "path": "/园区/研发园区/A栋研发楼",
        "description": "主要研发办公区域",
        "location": {
          "address": "北京市海淀区中关村软件园",
          "longitude": 116.307429,
          "latitude": 40.059037
        },
        "capacity": 500,
        "createTime": "2025-01-01T08:00:00",
        "updateTime": "2025-01-15T10:30:00"
      }
    ],
    "pageNum": 1,
    "pageSize": 20,
    "pages": 3
  },
  "timestamp": 1705123456789
}
```

#### 1.2 区域树结构查询

```http
GET /api/v1/access/areas/tree
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "tree": [
      {
        "areaId": "area001",
        "areaName": "北京总部园区",
        "areaType": "CAMPUS",
        "level": 1,
        "children": [
          {
            "areaId": "area002",
            "areaName": "研发园区",
            "areaType": "ZONE",
            "level": 2,
            "children": [
              {
                "areaId": "area003",
                "areaName": "A栋研发楼",
                "areaType": "BUILDING",
                "level": 3,
                "children": [
                  {
                    "areaId": "area004",
                    "areaName": "10楼办公区",
                    "areaType": "FLOOR",
                    "level": 4,
                    "children": []
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  },
  "timestamp": 1705123456789
}
```

#### 1.3 创建区域

```http
POST /api/v1/access/areas
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "areaCode": "BUILD-B-002",
  "areaName": "B栋办公楼",
  "areaType": "BUILDING",
  "parentId": "area002",
  "description": "B栋主要办公区域",
  "location": {
    "address": "北京市海淀区中关村软件园二期",
    "longitude": 116.308123,
    "latitude": 40.060234
  },
  "capacity": 300,
  "areaStatus": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "区域创建成功",
  "data": {
    "areaId": "area005",
    "areaCode": "BUILD-B-002",
    "areaName": "B栋办公楼",
    "createTime": "2025-01-17T14:30:00"
  },
  "timestamp": 1705123456789
}
```

#### 1.4 更新区域信息

```http
PUT /api/v1/access/areas/{areaId}
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "areaName": "B栋综合办公楼",
  "description": "B栋办公和综合服务区域",
  "capacity": 350,
  "areaStatus": 1
}
```

#### 1.5 删除区域

```http
DELETE /api/v1/access/areas/{areaId}
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "区域删除成功",
  "data": null,
  "timestamp": 1705123456789
}
```

## 👥 用户权限管理API

### 2. 用户权限配置

#### 2.1 用户权限查询

```http
GET /api/v1/access/users/{userId}/permissions
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "areaId": "area003",
  "permissionType": "ACCESS",
  "permissionStatus": 1,
  "effectiveDate": "2025-01-01",
  "expireDate": "2025-12-31",
  "pageNum": 1,
  "pageSize": 20
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 15,
    "list": [
      {
        "permissionId": "perm001",
        "userId": "user001",
        "areaId": "area003",
        "areaName": "A栋研发楼",
        "areaPath": "/园区/研发园区/A栋研发楼",
        "permissionType": "ACCESS",
        "permissionLevel": "NORMAL",
        "effectiveTime": "2025-01-01T00:00:00",
        "expireTime": "2025-12-31T23:59:59",
        "permissionStatus": 1,
        "timeRules": [
          {
            "ruleId": "rule001",
            "ruleName": "工作日访问",
            "weekdays": [1, 2, 3, 4, 5],
            "startTime": "08:30",
            "endTime": "18:30",
            "priority": 1
          }
        ],
        "createTime": "2025-01-01T10:00:00",
        "createUser": "admin001",
        "updateTime": "2025-01-10T15:30:00",
        "updateUser": "admin002"
      }
    ],
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1705123456789
}
```

#### 2.2 分配用户权限

```http
POST /api/v1/access/users/{userId}/permissions
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "permissions": [
    {
      "areaId": "area003",
      "permissionType": "ACCESS",
      "permissionLevel": "NORMAL",
      "effectiveTime": "2025-01-20T00:00:00",
      "expireTime": "2025-06-30T23:59:59",
      "timeRules": [
        {
          "ruleName": "标准工作时间",
          "weekdays": [1, 2, 3, 4, 5],
          "startTime": "09:00",
          "endTime": "18:00",
          "priority": 1
        }
      ],
      "remark": "研发部员工标准权限"
    }
  ],
  "conflictStrategy": "OVERRIDE"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "权限分配成功",
  "data": {
    "successCount": 1,
    "failureCount": 0,
    "conflictCount": 0,
    "assignedPermissions": [
      {
        "permissionId": "perm002",
        "areaId": "area003",
        "permissionStatus": 1
      }
    ]
  },
  "timestamp": 1705123456789
}
```

#### 2.3 批量权限分配

```http
POST /api/v1/access/permissions/batch
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "userIds": ["user001", "user002", "user003"],
  "templateId": "template001",
  "effectiveTime": "2025-01-20T00:00:00",
  "expireTime": "2025-12-31T23:59:59",
  "conflictStrategy": "SKIP_CONFLICT",
  "remark": "新员工批量权限分配"
}
```

#### 2.4 撤销用户权限

```http
DELETE /api/v1/access/users/{userId}/permissions/{permissionId}
Authorization: Bearer {token}
```

#### 2.5 权限冲突检查

```http
POST /api/v1/access/permissions/conflict-check
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "userId": "user001",
  "permissions": [
    {
      "areaId": "area003",
      "permissionType": "ACCESS",
      "effectiveTime": "2025-01-20T00:00:00",
      "expireTime": "2025-06-30T23:59:59"
    }
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "冲突检查完成",
  "data": {
    "hasConflict": true,
    "conflicts": [
      {
        "permissionId": "perm001",
        "conflictType": "TIME_OVERLAP",
        "conflictDescription": "与现有权限时间重叠",
        "existingPermission": {
          "areaId": "area003",
          "effectiveTime": "2025-01-01T00:00:00",
          "expireTime": "2025-12-31T23:59:59"
        },
        "suggestedSolutions": [
          "调整新权限时间范围",
          "撤销现有权限",
          "合并权限配置"
        ]
      }
    ],
    "recommendation": "建议撤销现有权限后重新分配"
  },
  "timestamp": 1705123456789
}
```

### 3. 权限模板管理

#### 3.1 权限模板查询

```http
GET /api/v1/access/permission-templates
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 10,
    "list": [
      {
        "templateId": "template001",
        "templateName": "研发工程师标准模板",
        "templateType": "STANDARD",
        "targetUserType": "DEVELOPER",
        "version": "1.0",
        "description": "研发部门工程师标准权限配置",
        "templateStatus": 1,
        "areaPermissions": [
          {
            "areaId": "area003",
            "areaName": "A栋研发楼",
            "permissionType": "ACCESS",
            "permissionLevel": "NORMAL"
          }
        ],
        "timeRules": [
          {
            "ruleName": "工作时间",
            "weekdays": [1, 2, 3, 4, 5],
            "startTime": "09:00",
            "endTime": "18:30"
          }
        ],
        "usageCount": 156,
        "createTime": "2025-01-01T08:00:00",
        "updateTime": "2025-01-10T14:30:00"
      }
    ]
  },
  "timestamp": 1705123456789
}
```

#### 3.2 创建权限模板

```http
POST /api/v1/access/permission-templates
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "templateName": "实习生权限模板",
  "templateType": "TEMPORARY",
  "targetUserType": "INTERN",
  "description": "实习生访问权限配置",
  "areaPermissions": [
    {
      "areaId": "area003",
      "permissionType": "ACCESS",
      "permissionLevel": "LIMITED"
    },
    {
      "areaId": "area005",
      "permissionType": "ACCESS",
      "permissionLevel": "LIMITED"
    }
  ],
  "timeRules": [
    {
      "ruleName": "实习生工作时间",
      "weekdays": [1, 2, 3, 4, 5],
      "startTime": "09:00",
      "endTime": "18:00"
    }
  ],
  "validityPeriod": {
    "defaultDays": 90,
    "maxDays": 180
  }
}
```

#### 3.3 应用权限模板

```http
POST /api/v1/access/permission-templates/{templateId}/apply
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "userIds": ["user001", "user002"],
  "effectiveTime": "2025-01-20T00:00:00",
  "expireTime": "2025-04-20T23:59:59",
  "conflictStrategy": "MERGE",
  "customizations": [
    {
      "areaId": "area003",
      "permissionLevel": "NORMAL"
    }
  ]
}
```

## ⏰ 时间规则管理API

### 4. 时间权限规则

#### 4.1 时间规则查询

```http
GET /api/v1/access/time-rules
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 25,
    "list": [
      {
        "ruleId": "rule001",
        "ruleName": "标准工作时间",
        "ruleType": "WEEKDAY",
        "weekdays": [1, 2, 3, 4, 5],
        "startTime": "09:00",
        "endTime": "18:00",
        "effectiveDate": "2025-01-01",
        "expireDate": "2025-12-31",
        "priority": 1,
        "ruleStatus": 1,
        "description": "周一至周五标准工作时间",
        "usageCount": 289,
        "createTime": "2025-01-01T08:00:00"
      }
    ]
  },
  "timestamp": 1705123456789
}
```

#### 4.2 创建时间规则

```http
POST /api/v1/access/time-rules
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "ruleName": "研发弹性工作时间",
  "ruleType": "WEEKDAY",
  "weekdays": [1, 2, 3, 4, 5],
  "startTime": "08:30",
  "endTime": "20:00",
  "effectiveDate": "2025-01-01",
  "expireDate": "2025-12-31",
  "priority": 2,
  "description": "研发部门弹性工作时间",
  "specialDates": [
    {
      "date": "2025-01-15",
      "startTime": "09:00",
      "endTime": "17:00",
      "reason": "公司活动日"
    }
  ]
}
```

#### 4.3 时间规则验证

```http
POST /api/v1/access/time-rules/validate
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "rules": [
    {
      "ruleName": "测试规则",
      "weekdays": [1, 2, 3, 4, 5],
      "startTime": "09:00",
      "endTime": "18:00",
      "priority": 1
    }
  ],
  "checkConflicts": true,
  "checkOverlaps": true
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "验证完成",
  "data": {
    "isValid": true,
    "conflicts": [],
    "overlaps": [],
    "warnings": [
      {
        "type": "TIME_RECOMMENDATION",
        "message": "建议考虑添加午休时间例外"
      }
    ]
  },
  "timestamp": 1705123456789
}
```

## 📊 监控统计API

### 5. 权限监控

#### 5.1 权限状态监控

```http
GET /api/v1/access/permissions/monitoring
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "summary": {
      "totalPermissions": 5847,
      "activePermissions": 5623,
      "expiredPermissions": 156,
      "expiringIn7Days": 89,
      "conflictPermissions": 12
    },
    "statistics": {
      "byAreaType": [
        {
          "areaType": "BUILDING",
          "count": 2341,
          "percentage": 40.1
        },
        {
          "areaType": "FLOOR",
          "count": 1876,
          "percentage": 32.1
        }
      ],
      "byUserType": [
        {
          "userType": "FULL_TIME",
          "count": 4123,
          "percentage": 70.5
        },
        {
          "userType": "INTERN",
          "count": 856,
          "percentage": 14.6
        }
      ]
    },
    "alerts": [
      {
        "type": "EXPIRING_SOON",
        "count": 89,
        "severity": "MEDIUM",
        "description": "89个权限将在7天内过期"
      }
    ]
  },
  "timestamp": 1705123456789
}
```

#### 5.2 权限使用统计

```http
GET /api/v1/access/permissions/usage-statistics
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "areaId": "area003",
  "userType": "FULL_TIME",
  "groupBy": "DAY"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "period": {
      "startDate": "2025-01-01",
      "endDate": "2025-01-31"
    },
    "totalUsage": 15420,
    "uniqueUsers": 289,
    "peakDay": {
      "date": "2025-01-15",
      "usage": 687
    },
    "dailyStats": [
      {
        "date": "2025-01-01",
        "usage": 456,
        "uniqueUsers": 89
      }
    ],
    "areaUsage": [
      {
        "areaId": "area003",
        "areaName": "A栋研发楼",
        "usage": 8234,
        "percentage": 53.4
      }
    ]
  },
  "timestamp": 1705123456789
}
```

#### 5.3 权限审计报告

```http
GET /api/v1/access/permissions/audit-report
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "reportType": "COMPLIANCE",
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "areaIds": ["area003", "area005"],
  "userTypes": ["FULL_TIME", "INTERN"],
  "format": "PDF"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "报告生成成功",
  "data": {
    "reportId": "report001",
    "reportType": "COMPLIANCE",
    "generateTime": "2025-01-17T16:30:00",
    "downloadUrl": "/api/v1/access/reports/report001/download",
    "summary": {
      "totalUsers": 567,
      "compliantUsers": 543,
      "nonCompliantUsers": 24,
      "complianceRate": 95.8
    }
  },
  "timestamp": 1705123456789
}
```

## 🔔 通知预警API

### 6. 权限通知管理

#### 6.1 权限到期提醒

```http
GET /api/v1/access/permissions/expiry-alerts
Authorization: Bearer {token}
```

**请求参数**:
```json
{
  "daysBeforeExpiry": 7,
  "areaId": "area003",
  "userType": "INTERN",
  "pageNum": 1,
  "pageSize": 20
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 89,
    "list": [
      {
        "userId": "user001",
        "userName": "张三",
        "userType": "INTERN",
        "permissions": [
          {
            "permissionId": "perm001",
            "areaName": "A栋研发楼",
            "expireTime": "2025-01-24T23:59:59",
            "daysToExpiry": 7,
            "lastAccessTime": "2025-01-16T14:30:00"
          }
        ],
        "supervisor": {
          "userId": "user999",
          "userName": "李经理"
        }
      }
    ]
  },
  "timestamp": 1705123456789
}
```

#### 6.2 发送延期通知

```http
POST /api/v1/access/permissions/expiry-alerts/notify
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**:
```json
{
  "userIds": ["user001", "user002"],
  "messageTemplate": "DEFAULT_EXPIRY",
  "customMessage": "您的权限即将到期，请及时续期",
  "notifySupervisor": true,
  "channels": ["EMAIL", "SMS"]
}
```

## 🛡️ 错误码定义

### 业务错误码

| 错误码 | 错误描述 | HTTP状态码 | 解决方案 |
|--------|---------|-----------|---------|
| **40001** | 区域编码已存在 | 400 | 使用不同的区域编码 |
| **40002** | 父级区域不存在 | 400 | 检查父级区域ID |
| **40003** | 区域层级关系错误 | 400 | 检查区域层级配置 |
| **40004** | 用户权限不存在 | 404 | 检查权限ID |
| **40005** | 权限已过期 | 400 | 重新申请权限 |
| **40006** | 权限时间冲突 | 400 | 调整权限时间 |
| **40007** | 权限模板不存在 | 404 | 检查模板ID |
| **40008** | 时间规则冲突 | 400 | 调整时间规则 |
| **40009** | 超出权限数量限制 | 400 | 联系管理员 |
| **41001** | 权限不足 | 403 | 联系管理员授权 |
| **41002** | 操作被拒绝 | 403 | 检查操作权限 |
| **50001** | 系统内部错误 | 500 | 联系技术支持 |
| **50002** | 数据库操作失败 | 500 | 联系技术支持 |
| **50003** | 第三方服务异常 | 500 | 稍后重试 |

### 响应格式示例

```json
{
  "code": 40006,
  "message": "权限时间冲突",
  "data": {
    "conflictDetails": [
      {
        "existingPermission": {
          "permissionId": "perm001",
          "areaId": "area003",
          "startTime": "09:00",
          "endTime": "18:00"
        },
        "newPermission": {
          "areaId": "area003",
          "startTime": "10:00",
          "endTime": "19:00"
        },
        "conflictTimeRange": "10:00-18:00"
      }
    ],
    "suggestions": [
      "调整新的时间范围",
      "撤销现有权限"
    ]
  },
  "timestamp": 1705123456789
}
```

## 🔒 安全设计

### 1. 身份认证
- **JWT Token**: 使用标准JWT进行身份认证
- **Token有效期**: Access Token 2小时，Refresh Token 7天
- **Token刷新**: 自动刷新机制，无需用户重新登录

### 2. 权限校验
- **RBAC模型**: 基于角色的访问控制
- **API级权限**: 每个API都需要相应的权限
- **数据级权限**: 用户只能访问自己权限范围内的数据

### 3. 数据安全
- **传输加密**: 所有API调用使用HTTPS
- **敏感数据**: 用户手机号、身份证号等敏感信息脱敏
- **操作审计**: 完整记录所有权限相关操作

### 4. 防护机制
- **参数校验**: 严格的参数格式和长度校验
- **SQL注入防护**: 使用参数化查询
- **接口防刷**: 基于用户和IP的频率限制
- **异常监控**: 实时监控异常API调用

## 📈 性能设计

### 1. 缓存策略
- **权限缓存**: Redis缓存用户权限，5分钟更新
- **区域缓存**: 缓存区域树结构，30分钟更新
- **模板缓存**: 缓存权限模板，1小时更新

### 2. 数据库优化
- **索引优化**: 为常用查询字段创建复合索引
- **分页查询**: 使用游标分页，避免深度分页问题
- **读写分离**: 查询操作使用只读数据库

### 3. 接口性能
- **批量操作**: 支持批量权限分配和回收
- **异步处理**: 权限同步到设备使用异步消息队列
- **响应时间**: 95%的API响应时间小于500ms

---

**文档版本**: v1.0.0
**创建日期**: 2025-12-17
**创建人**: AI助手
**审核人**: 待定
**批准人**: 待定

**备注**: 本API接口设计文档涵盖了区域权限管理模块的所有核心功能接口，为前后端开发提供了详细的接口规范。