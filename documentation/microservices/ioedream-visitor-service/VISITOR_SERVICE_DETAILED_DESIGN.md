# 👤 IOE-DREAM 访客管理服务详细设计文档

> **📋 文档版本**: v1.0.0
> **🏗️ 服务名称**: ioedream-visitor-service
> **🚀 端口号**: 8095
> **👥 维护团队**: IOE-DREAM 访客业务模块团队
> **📅 创建日期**: 2025-01-30
> **🔄 更新日期**: 2025-01-30

---

## 📋 概述

### 服务简介

**IOE-DREAM 访客管理服务**是企业级智慧园区一卡通管理平台的核心业务服务，提供完整的访客管理解决方案。该服务支持在线预约、现场登记、访客审批、权限管理、访问监控等功能，实现访客管理的全流程数字化。

### 核心价值

- **📅 智能预约**: 在线预约、审批流程自动化，提升访客管理效率
- **🔒 安全管控**: 人脸识别验证、黑名单管理、访问权限控制
- **📱 移动端支持**: 访客手机端预约、扫码登记、实时通知
- **📊 数据分析**: 访客统计分析、访问趋势预测、管理报表
- **🎯 精准服务**: 访客分级管理、个性化服务、满意度调查

### 业务场景

- **企业访客**: 客户访问、供应商访问、合作伙伴访问
- **求职访客**: 面试预约、人才招聘、校园招聘
- **维修配送**: 设备维护、物品配送、服务支持
- **临时访客**: 快递收发、外卖配送、临时办事
- **VIP访客**: 重要客户、高级领导、特殊访客

---

## 🏗️ 架构设计

### 技术架构

```mermaid
graph TB
    subgraph "前端层"
        A[Web管理后台<br/>Vue3 + Ant Design Vue]
        B[移动端App<br/>uni-app]
        C[访客小程序<br/>微信小程序]
        D[自助终端<br/>访客自助机]
    end

    subgraph "网关层"
        E[API网关<br/>ioedream-gateway-service<br/>8080]
    end

    subgraph "业务服务层"
        F[访客管理服务<br/>ioedream-visitor-service<br/>8095]
        G[公共服务<br/>ioedream-common-service<br/>8088]
        H[工作流服务<br/>ioedream-oa-service<br/>8089]
        I[设备通讯服务<br/>ioedream-device-comm-service<br/>8087]
    end

    subgraph "数据层"
        J[MySQL 8.0<br/>业务数据库]
        K[Redis 6.4<br/>缓存数据库]
        L[文件存储<br/>MinIO/阿里云OSS]
    end

    subgraph "外部系统"
        M[短信服务<br/>阿里云短信]
        N[邮件服务<br/>企业邮箱]
        O[人脸识别<br/>百度AI/商汤]
        P[微信服务<br/>微信开放平台]
    end

    A --> E
    B --> E
    C --> E
    D --> E
    E --> F
    F --> G
    F --> H
    F --> I
    F --> J
    F --> K
    F --> L
    F --> M
    F --> N
    F --> O
    F --> P
```

### 服务职责

| 职责类别 | 具体功能 | 描述 |
|---------|---------|------|
| **核心业务** | 访客预约管理 | 在线预约、预约审批、预约查询 |
| | 访客登记管理 | 现场登记、身份验证、访客卡发放 |
| | 访客权限管理 | 访问权限配置、区域访问控制、通行授权 |
| | 访客监控管理 | 访问轨迹追踪、异常访问监控、访问统计 |
| **数据管理** | 访客信息管理 | 访客基本信息、访客等级、黑名单管理 |
| | 访问记录管理 | 预约记录、登记记录、访问历史 |
| | 统计分析 | 访客统计、访问趋势、满意度分析 |
| **安全管控** | 身份验证 | 人脸识别、身份证验证、手机验证 |
| | 黑名单管理 | 黑名单人员管理、风险人员识别 |
| | 访问控制 | 访问权限管理、区域准入控制 |
| **通知服务** | 短信通知 | 预约确认、访问提醒、通知推送 |
| | 邮件通知 | 预约成功、审批结果、访问报告 |
| **移动端支持** | 手机预约 | 移动端预约、状态查询、扫码签到 |
| | 小程序支持 | 微信小程序、快捷预约、扫码验证 |

---

## 🗄️ 数据库设计

### 核心实体关系

```mermaid
erDiagram
    t_visitor ||--o{ t_visitor_appointment : "1:N"
    t_visitor ||--o{ t_visitor_registration : "1:N"
    t_visitor ||--o{ t_visitor_blacklist : "1:N"
    t_visitor_appointment ||--o{ t_visitor_approval_record : "1:N"
    t_visitor_appointment ||--o{ t_visitor_reservation : "1:1"
    t_user ||--o{ t_visitor_appointment : "N:1"
    t_user ||--o{ t_visitor_registration : "N:1"
    t_area ||--o{ t_visitor_registration : "N:N"
    t_device ||--o{ t_visitor_registration : "N:1"
    t_access_level ||--o{ t_visitor : "N:1"

    t_visitor {
        bigint visitor_id PK "访客ID"
        varchar visitor_code "访客编号"
        varchar name "访客姓名"
        int gender "性别"
        varchar id_card "证件号"
        varchar phone "手机号"
        varchar email "邮箱"
        varchar company_name "公司名称"
        varchar photo_url "照片URL"
        varchar visitor_level "访客等级"
        int blacklisted "是否黑名单"
        varchar blacklist_reason "黑名单原因"
        datetime blacklist_time "加入黑名单时间"
        varchar blacklist_operator "黑名单操作人"
        datetime last_visit_time "最后访问时间"
        bigint access_level_id "访客权限ID"
        boolean shelves_flag "启用状态"
        varchar remark "备注"
        varchar extended_attributes "扩展属性"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_visitor_appointment {
        bigint appointment_id PK "预约ID"
        varchar visitor_name "访客姓名"
        varchar phone_number "手机号"
        varchar id_card_number "身份证号"
        varchar appointment_type "预约类型"
        bigint visit_user_id "被访人ID"
        varchar visit_user_name "被访人姓名"
        datetime appointment_start_time "预约开始时间"
        datetime appointment_end_time "预约结束时间"
        varchar visit_purpose "访问目的"
        varchar status "预约状态"
        varchar approval_comment "审批意见"
        datetime approval_time "审批时间"
        datetime check_in_time "签到时间"
        varchar remark "备注"
        bigint workflow_instance_id "工作流实例ID"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_visitor_registration {
        bigint registration_id PK "登记ID"
        varchar registration_code "登记编号"
        bigint reservation_id "关联预约ID"
        bigint visitor_id "访客ID"
        varchar visitor_name "访客姓名"
        varchar id_card "证件号"
        varchar phone "手机号"
        bigint interviewee_id "被访人ID"
        varchar interviewee_name "被访人姓名"
        varchar visitor_card "访客卡号"
        bigint access_level_id "访问权限级别ID"
        varchar access_areas "访问区域"
        datetime expected_leave_time "预计离开时间"
        datetime actual_leave_time "实际离开时间"
        varchar registration_device "登记设备"
        varchar check_in_photo_url "签入照片URL"
        varchar check_out_photo_url "签出照片URL"
        varchar status "状态"
        varchar over_time_reason "超时原因"
        int escort_required "是否需要陪同"
        varchar escort_user "陪同人"
        boolean shelves_flag "启用状态"
        varchar remark "备注"
        varchar extended_attributes "扩展属性"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_visitor_blacklist {
        bigint blacklist_id PK "黑名单ID"
        varchar visitor_name "访客姓名"
        varchar id_card "证件号"
        varchar phone "手机号"
        varchar blacklist_type "黑名单类型"
        varchar blacklist_reason "黑名单原因"
        datetime blacklist_time "加入黑名单时间"
        datetime expire_time "过期时间"
        int status "状态"
        varchar operator "操作人"
        varchar remark "备注"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_visitor_approval_record {
        bigint approval_id PK "审批ID"
        bigint appointment_id "预约ID"
        varchar approver_id "审批人ID"
        varchar approver_name "审批人姓名"
        varchar approval_result "审批结果"
        varchar approval_comment "审批意见"
        datetime approval_time "审批时间"
        varchar remark "备注"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_access_level {
        bigint access_level_id PK "权限级别ID"
        varchar level_name "级别名称"
        varchar level_code "级别编码"
        int level_order "级别顺序"
        varchar access_areas "可访问区域"
        varchar access_permissions "访问权限"
        varchar time_restrictions "时间限制"
        varchar description "描述"
        boolean shelves_flag "启用状态"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }
```

### 核心表设计

#### 1. 访客表 (t_visitor)

```sql
CREATE TABLE t_visitor (
    visitor_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '访客ID',
    visitor_code VARCHAR(50) NOT NULL UNIQUE COMMENT '访客编号',
    name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    gender TINYINT COMMENT '性别：1-男 2-女',
    id_card VARCHAR(50) COMMENT '证件号',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    company_name VARCHAR(200) COMMENT '公司名称',
    photo_url VARCHAR(500) COMMENT '照片URL',
    visitor_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '访客等级：NORMAL-普通 VIP-重要 CONTRACTOR-承包商 DELIVERY-配送',
    blacklisted TINYINT NOT NULL DEFAULT 0 COMMENT '是否黑名单：0-否 1-是',
    blacklist_reason VARCHAR(200) COMMENT '黑名单原因',
    blacklist_time DATETIME COMMENT '加入黑名单时间',
    blacklist_operator VARCHAR(50) COMMENT '黑名单操作人',
    last_visit_time DATETIME COMMENT '最后访问时间',
    access_level_id BIGINT COMMENT '访客权限ID',
    shelves_flag TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0-禁用 1-启用',
    remark VARCHAR(500) COMMENT '备注',
    extended_attributes JSON COMMENT '扩展属性',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',

    INDEX idx_visitor_code (visitor_code),
    INDEX idx_name (name),
    INDEX idx_id_card (id_card),
    INDEX idx_phone (phone),
    INDEX idx_visitor_level (visitor_level),
    INDEX idx_blacklisted (blacklisted),
    INDEX idx_create_time (create_time),
    INDEX idx_shelves_flag (shelves_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客表';
```

#### 2. 访客预约表 (visitor_appointment)

```sql
CREATE TABLE visitor_appointment (
    appointment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    phone_number VARCHAR(20) NOT NULL COMMENT '手机号',
    id_card_number VARCHAR(50) COMMENT '身份证号',
    appointment_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL' COMMENT '预约类型：GENERAL-一般 INTERVIEW-面试 DELIVERY-配送 MAINTENANCE-维修',
    visit_user_id BIGINT NOT NULL COMMENT '被访人ID',
    visit_user_name VARCHAR(100) NOT NULL COMMENT '被访人姓名',
    appointment_start_time DATETIME NOT NULL COMMENT '预约开始时间',
    appointment_end_time DATETIME NOT NULL COMMENT '预约结束时间',
    visit_purpose VARCHAR(200) NOT NULL COMMENT '访问目的',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '预约状态：PENDING-待审批 APPROVED-已通过 REJECTED-已驳回 CANCELLED-已取消 CHECKED_IN-已签到 CHECKED_OUT-已签退',
    approval_comment VARCHAR(500) COMMENT '审批意见',
    approval_time DATETIME COMMENT '审批时间',
    check_in_time DATETIME COMMENT '签到时间',
    remark VARCHAR(500) COMMENT '备注',
    workflow_instance_id BIGINT COMMENT '工作流实例ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',

    INDEX idx_visitor_name (visitor_name),
    INDEX idx_phone_number (phone_number),
    INDEX idx_visit_user_id (visit_user_id),
    INDEX idx_appointment_start_time (appointment_start_time),
    INDEX idx_appointment_end_time (appointment_end_time),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_workflow_instance_id (workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客预约表';
```

#### 3. 访客登记表 (t_visitor_registration)

```sql
CREATE TABLE t_visitor_registration (
    registration_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '登记ID',
    registration_code VARCHAR(50) NOT NULL UNIQUE COMMENT '登记编号',
    reservation_id BIGINT COMMENT '关联预约ID',
    visitor_id BIGINT COMMENT '访客ID',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    id_card VARCHAR(50) COMMENT '证件号',
    phone VARCHAR(20) COMMENT '手机号',
    interviewee_id BIGINT NOT NULL COMMENT '被访人ID',
    interviewee_name VARCHAR(100) NOT NULL COMMENT '被访人姓名',
    visitor_card VARCHAR(50) COMMENT '访客卡号',
    access_level_id BIGINT NOT NULL COMMENT '访问权限级别ID',
    access_areas JSON COMMENT '访问区域',
    expected_leave_time DATETIME COMMENT '预计离开时间',
    actual_leave_time DATETIME COMMENT '实际离开时间',
    registration_device VARCHAR(50) COMMENT '登记设备',
    check_in_photo_url VARCHAR(500) COMMENT '签入照片URL',
    check_out_photo_url VARCHAR(500) COMMENT '签出照片URL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-在场 COMPLETED-已离开 TIMEOUT-超时 CANCELLED-已取消',
    over_time_reason VARCHAR(200) COMMENT '超时原因',
    escort_required TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要陪同：0-否 1-是',
    escort_user VARCHAR(100) COMMENT '陪同人',
    shelves_flag TINYINT NOT NULL DEFAULT 1 COMMENT '启用状态：0-禁用 1-启用',
    remark VARCHAR(500) COMMENT '备注',
    extended_attributes JSON COMMENT '扩展属性',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',

    INDEX idx_registration_code (registration_code),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_visitor_id (visitor_id),
    INDEX idx_interviewee_id (interviewee_id),
    INDEX idx_visitor_card (visitor_card),
    INDEX idx_access_level_id (access_level_id),
    INDEX idx_expected_leave_time (expected_leave_time),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客登记表';
```

#### 4. 访客黑名单表 (t_visitor_blacklist)

```sql
CREATE TABLE t_visitor_blacklist (
    blacklist_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '黑名单ID',
    visitor_name VARCHAR(100) NOT NULL COMMENT '访客姓名',
    id_card VARCHAR(50) COMMENT '证件号',
    phone VARCHAR(20) COMMENT '手机号',
    blacklist_type VARCHAR(20) NOT NULL DEFAULT 'SECURITY' COMMENT '黑名单类型：SECURITY-安全 VIOLATION-违规 CUSTOM-自定义',
    blacklist_reason VARCHAR(200) NOT NULL COMMENT '黑名单原因',
    blacklist_time DATETIME NOT NULL COMMENT '加入黑名单时间',
    expire_time DATETIME COMMENT '过期时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-生效 2-已过期 3-已解除',
    operator VARCHAR(50) NOT NULL COMMENT '操作人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',

    INDEX idx_visitor_name (visitor_name),
    INDEX idx_id_card (id_card),
    INDEX idx_phone (phone),
    INDEX idx_blacklist_type (blacklist_type),
    INDEX idx_status (status),
    INDEX idx_blacklist_time (blacklist_time),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客黑名单表';
```

---

## 🔌 API设计

### REST API规范

| 资源路径 | HTTP方法 | 功能描述 | 权限要求 |
|---------|---------|---------|---------|
| `/api/v1/visitor/appointment/create` | POST | 创建访客预约 | VISITOR_APPOINTMENT_CREATE |
| `/api/v1/visitor/appointment/{appointmentId}` | GET | 获取预约详情 | VISITOR_APPOINTMENT_QUERY |
| `/api/v1/visitor/appointment/query` | GET | 分页查询预约 | VISITOR_APPOINTMENT_QUERY |
| `/api/v1/visitor/appointment/approve` | POST | 审批预约 | VISITOR_APPOINTMENT_APPROVE |
| `/api/v1/visitor/appointment/cancel` | POST | 取消预约 | VISITOR_APPOINTMENT_CANCEL |
| `/api/v1/visitor/registration/check-in` | POST | 访客签到登记 | VISITOR_REGISTRATION_CHECKIN |
| `/api/v1/visitor/registration/check-out` | POST | 访客签出 | VISITOR_REGISTRATION_CHECKOUT |
| `/api/v1/visitor/registration/query` | GET | 分页查询登记记录 | VISITOR_REGISTRATION_QUERY |
| `/api/v1/visitor/visitor/create` | POST | 创建访客信息 | VISITOR_MANAGE |
| `/api/v1/visitor/visitor/{visitorId}` | GET | 获取访客详情 | VISITOR_QUERY |
| `/api/v1/visitor/visitor/query` | GET | 分页查询访客 | VISITOR_QUERY |
| `/api/v1/visitor/blacklist/add` | POST | 添加黑名单 | VISITOR_BLACKLIST_MANAGE |
| `/api/v1/visitor/blacklist/remove` | POST | 移除黑名单 | VISITOR_BLACKLIST_MANAGE |
| `/api/v1/visitor/statistics` | GET | 获取访客统计 | VISITOR_STATISTICS |
| `/api/v1/visitor/realtime/status` | GET | 获取实时状态 | VISITOR_MONITOR |

### 核心API接口

#### 1. 创建访客预约

```http
POST /api/v1/visitor/appointment/create
Content-Type: application/json

{
  "visitorName": "张三",
  "phoneNumber": "13800138000",
  "idCardNumber": "110101199001011234",
  "appointmentType": "GENERAL",
  "visitUserId": 1001,
  "visitUserName": "李四",
  "appointmentStartTime": "2025-01-30T14:00:00",
  "appointmentEndTime": "2025-01-30T16:00:00",
  "visitPurpose": "商务洽谈",
  "remark": "重要客户来访"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "appointmentId": 1001,
    "appointmentCode": "APT202501300001",
    "visitorName": "张三",
    "phoneNumber": "13800138000",
    "idCardNumber": "110101199001011234",
    "appointmentType": "GENERAL",
    "visitUserId": 1001,
    "visitUserName": "李四",
    "appointmentStartTime": "2025-01-30T14:00:00",
    "appointmentEndTime": "2025-01-30T16:00:00",
    "visitPurpose": "商务洽谈",
    "status": "PENDING",
    "remark": "重要客户来访",
    "createTime": "2025-01-30T10:00:00",
    "workflowInstanceId": "WF_20250130001"
  },
  "timestamp": 1706582400000
}
```

#### 2. 审批访客预约

```http
POST /api/v1/visitor/appointment/approve
Content-Type: application/json

{
  "appointmentId": 1001,
  "approvalResult": "APPROVED",
  "approvalComment": "同意预约，请注意访客礼仪",
  "accessLevelId": 1,
  "accessAreas": [1, 2, 3],
  "escortRequired": false
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "appointmentId": 1001,
    "approvalResult": "APPROVED",
    "approvalComment": "同意预约，请注意访客礼仪",
    "approvalTime": "2025-01-30T11:00:00",
    "status": "APPROVED",
    "accessLevelId": 1,
    "accessAreas": [1, 2, 3],
    "escortRequired": false
  },
  "timestamp": 1706586000000
}
```

#### 3. 访客签到登记

```http
POST /api/v1/visitor/registration/check-in
Content-Type: application/json

{
  "appointmentId": 1001,
  "visitorName": "张三",
  "idCard": "110101199001011234",
  "phone": "13800138000",
  "intervieweeId": 1001,
  "intervieweeName": "李四",
  "accessLevelId": 1,
  "expectedLeaveTime": "2025-01-30T18:00:00",
  "checkInPhoto": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...",
  "registrationDevice": "FRONT_DESK_001"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "registrationId": 2001,
    "registrationCode": "RG20250130001",
    "appointmentId": 1001,
    "visitorCard": "VISITOR_CARD_001",
    "accessLevelId": 1,
    "accessAreas": [1, 2, 3],
    "expectedLeaveTime": "2025-01-30T18:00:00",
    "checkInTime": "2025-01-30T14:00:00",
    "checkInPhotoUrl": "https://cdn.example.com/photos/visitor/20250130/RG20250130001_checkin.jpg",
    "status": "ACTIVE",
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  },
  "timestamp": 1706587200000
}
```

### WebSocket接口

#### 实时访客状态通知

```javascript
// 连接WebSocket
const ws = new WebSocket('ws://localhost:8095/api/v1/visitor/realtime/notifications');

// 订阅访客状态通知
ws.send(JSON.stringify({
  type: 'SUBSCRIBE_VISITOR_STATUS',
  data: {
    areaId: 'AREA001',
    role: 'SECURITY'
  }
}));

// 接收实时访客状态通知
ws.onmessage = function(event) {
  const message = JSON.parse(event.data);

  switch(message.type) {
    case 'APPOINTMENT_CREATED':
      // 处理新预约通知
      handleAppointmentCreated(message.data);
      break;

    case 'APPOINTMENT_APPROVED':
      // 处理预约审批通知
      handleAppointmentApproved(message.data);
      break;

    case 'VISITOR_CHECKED_IN':
      // 处理访客签到通知
      handleVisitorCheckedIn(message.data);
      break;

    case 'VISITOR_CHECKED_OUT':
      // 处理访客签出通知
      handleVisitorCheckedOut(message.data);
      break;

    case 'VISITOR_TIMEOUT':
      // 处理访客超时通知
      handleVisitorTimeout(message.data);
      break;

    case 'BLACKLIST_DETECTED':
      // 处理黑名单检测通知
      handleBlacklistDetected(message.data);
      break;

    case 'REALTIME_STATISTICS':
      // 处理实时统计数据
      handleRealtimeStatistics(message.data);
      break;
  }
};

function handleAppointmentCreated(data) {
  console.log('新预约创建:', data.appointmentCode);
  // 通知被访人
  notifyVisitee(data.visitUserId, data);
  // 更新预约列表
  updateAppointmentList(data);
}

function handleVisitorCheckedIn(data) {
  console.log('访客签到:', data.registrationCode);
  // 更新访问监控大屏
  updateMonitoringScreen(data);
  // 发送门禁授权
  sendAccessAuthorization(data.visitorCard, data.accessAreas);
}

function handleBlacklistDetected(data) {
  console.warn('黑名单人员检测:', data.visitorName);
  // 触发安全警报
  triggerSecurityAlert(data);
  // 通知安保人员
  notifySecurityPersonnel(data);
}
```

---

## 💼 业务流程设计

### 核心业务流程

#### 1. 访客预约流程

```mermaid
sequenceDiagram
    participant Visitor as 访客
    participant MobileApp as 移动端
    participant Gateway as API网关
    participant VisitorService as 访客服务
    participant OaService as OA服务
    participant WorkflowService as 工作流服务
    participant NotificationService as 通知服务
    participant SecuritySystem as 安保系统

    Visitor->>MobileApp: 提交预约申请
    MobileApp->>Gateway: 发送预约请求
    Gateway->>VisitorService: 验证权限
    VisitorService->>VisitorService: 验证访客信息

    alt 预约信息完整
        VisitorService->>WorkflowService: 创建工作流实例
        WorkflowService-->>VisitorService: 返回工作流ID
        VisitorService->>MobileApp: 返回预约成功

        par 工作流审批
            WorkflowService->>OaService: 推送审批任务
            OaService->>WorkflowService: 审批结果
            WorkflowService->>VisitorService: 更新预约状态
        end

        VisitorService->>NotificationService: 发送审批结果通知
        NotificationService->>MobileApp: 推送通知给访客
        NotificationService->>VisitorService: 通知被访人

    else 预约信息不完整
        VisitorService-->>MobileApp: 返回验证错误
    end
```

#### 2. 访客登记流程

```mermaid
sequenceDiagram
    participant Visitor as 访客
    participant FrontDesk as 前台
    participant FaceRecognition as 人脸识别
    participant VisitorService as 访客服务
    participant SecuritySystem as 安保系统
    participant AccessControl as 门禁系统
    participant NotificationService as 通知服务

    Visitor->>FrontDesk: 出示预约码或身份证
    FrontDesk->>VisitorService: 扫码或读卡
    VisitorService->>VisitorService: 验证预约信息

    alt 预约验证通过
        FrontDesk->>FaceRecognition: 进行人脸识别
        FaceRecognition-->>FrontDesk: 返回识别结果

        alt 人脸识别通过
            VisitorService->>VisitorService: 创建登记记录
            VisitorService->>SecuritySystem: 检查黑名单

            alt 非黑名单人员
                SecuritySystem-->>VisitorService: 检查通过
                VisitorService->>AccessControl: 发放访客卡
                AccessControl-->>VisitorService: 返回卡号
                VisitorService->>NotificationService: 发送签到成功通知
                NotificationService->>FrontDesk: 显示成功信息
                FrontDesk->>Visitor: 发放访客卡和二维码

            else 黑名单人员
                SecuritySystem-->>VisitorService: 检测到黑名单
                VisitorService->>SecuritySystem: 触发安全警报
                SecuritySystem-->>FrontDesk: 显示警报信息
            end

        else 人脸识别失败
            FrontDesk->>VisitorService: 人工核实身份
            VisitorService->>VisitorService: 手动创建登记
        end

    else 预约验证失败
        FrontDesk->>VisitorService: 现场预约
        VisitorService->>VisitorService: 创建临时预约
        VisitorService->>VisitorService: 创建登记记录
    end
```

#### 3. 访客访问监控流程

```mermaid
sequenceDiagram
    participant AccessControl as 门禁系统
    participant VisitorService as 访客服务
    participant MonitoringSystem as 监控系统
    participant SecuritySystem as 安保系统
    participant NotificationService as 通知服务

    AccessControl->>VisitorService: 验证访客卡
    VisitorService->>VisitorService: 检查访问权限
    VisitorService->>MonitoringSystem: 记录访问事件

    alt 访问权限有效
        VisitorService->>AccessControl: 授权通行
        AccessControl->>AccessControl: 开门放行
        VisitorService->>MonitoringSystem: 更新访问轨迹
        MonitoringSystem->>MonitoringSystem: 显示实时状态

        par 访问过程监控
            loop 访客在园区内
                MonitoringSystem->>VisitorService: 检查访问状态
                VisitorService->>MonitoringSystem: 返回当前位置
                MonitoringSystem->>MonitoringSystem: 更新轨迹图
            end

        alt 访客超时
            VisitorService->>VisitorService: 检测超时
            VisitorService->>SecuritySystem: 发送超时提醒
            SecuritySystem->>NotificationService: 通知安保人员
        end

        AccessControl->>VisitorService: 访客离开
        VisitorService->>VisitorService: 完成访问记录
        VisitorService->>NotificationService: 发送访问报告

    else 访问权限无效
        VisitorService->>AccessControl: 拒绝通行
        AccessControl->>AccessControl: 保持门禁状态
        VisitorService->>SecuritySystem: 记录异常访问
        SecuritySystem->>NotificationService: 通知安保人员
    end
```

### 异常处理流程

#### 1. 黑名单处理流程

```mermaid
flowchart TD
    A[访客身份验证] --> B{检查黑名单}
    B -->|是黑名单| C[触发安全警报]
    B -->|非黑名单| D[继续正常流程]

    C --> E{黑名单类型}
    E -->|安全威胁| F[立即报警]
    E -->|违规行为| G[通知安保]
    E -->|自定义| H[按配置处理]

    F --> I[封锁门禁]
    G --> J[记录违规]
    H --> K[自定义处理]

    I --> L[等待安保处理]
    J --> M[生成违规记录]
    K --> N[执行自定义规则]
```

#### 2. 访客超时处理流程

```mermaid
flowchart TD
    A[预计离开时间到达] --> B{是否已签出}
    B -->|已签出| C[正常完成访问]
    B -->|未签出| D[触发超时检查]

    D --> E{超时时长}
    E -->|30分钟内| F[发送提醒通知]
    E -->|30-60分钟| G[通知前台跟进]
    E -->|60分钟以上| H[通知安保人员]

    F --> I{是否签出}
    G --> J{前台是否跟进}
    H --> K{安保是否处理}

    I -->|已签出| L[完成访问]
    I -->|未签出| M[继续等待]
    J -->|已跟进| L
    J -->|未跟进| N[继续等待]
    K -->|已处理| O[记录处理结果]
    K -->|未处理| P[继续等待]

    M --> Q[15分钟后重检]
    N --> R[10分钟后重检]
    P --> S[5分钟后重检]
```

---

## 🔒 安全设计

### 1. 身份验证安全

#### 人脸识别验证

```java
@Component
public class VisitorFaceRecognitionManager {

    @Resource
    private BaiduAIService baiduAIService;

    @Resource
    private FaceDataService faceDataService;

    /**
     * 人脸识别验证
     */
    public FaceRecognitionResult verifyFaceRecognition(MultipartFile photo, String idCardNumber) {
        try {
            // 1. 提取人脸特征
            String faceFeature = baiduAIService.extractFaceFeature(photo);

            // 2. 查询人脸库
            FaceDataEntity faceData = faceDataService.getByIdCardNumber(idCardNumber);

            if (faceData == null) {
                // 首次访问，注册人脸
                return registerNewFace(photo, idCardNumber, faceFeature);
            } else {
                // 对比人脸特征
                return compareFaceFeatures(faceFeature, faceData.getFaceFeature());
            }
        } catch (Exception e) {
            log.error("人脸识别验证失败", e);
            throw new BusinessException("FACE_RECOGNITION_ERROR", "人脸识别验证失败");
        }
    }

    /**
     * 注册新访客人脸
     */
    private FaceRecognitionResult registerNewFace(MultipartFile photo, String idCardNumber, String faceFeature) {
        try {
            // 1. 保存人脸照片
            String photoUrl = saveFacePhoto(photo, idCardNumber);

            // 2. 创建人脸数据记录
            FaceDataEntity faceData = new FaceDataEntity();
            faceData.setIdCardNumber(idCardNumber);
            faceData.setFaceFeature(faceFeature);
            faceData.setPhotoUrl(photoUrl);
            faceData.setStatus(1); // 正常状态

            faceDataService.save(faceData);

            return FaceRecognitionResult.builder()
                    .match(true)
                    .message("人脸注册成功")
                    .confidence(1.0)
                    .build();
        } catch (Exception e) {
            log.error("注册人脸失败", e);
            throw new BusinessException("FACE_REGISTER_ERROR", "人脸注册失败");
        }
    }

    /**
     * 对比人脸特征
     */
    private FaceRecognitionResult compareFaceFeatures(String newFeature, String storedFeature) {
        try {
            // 调用百度AI进行人脸对比
            FaceCompareResult compareResult = baiduAIService.compareFaces(newFeature, storedFeature);

            // 设置置信度阈值
            double confidenceThreshold = 0.8;

            return FaceRecognitionResult.builder()
                    .match(compareResult.getScore() >= confidenceThreshold)
                    .confidence(compareResult.getScore())
                    .message(compareResult.getScore() >= confidenceThreshold ? "验证通过" : "验证失败")
                    .build();
        } catch (Exception e) {
            log.error("人脸特征对比失败", e);
            throw new BusinessException("FACE_COMPARE_ERROR", "人脸特征对比失败");
        }
    }

    /**
     * 保存人脸照片
     */
    private String saveFacePhoto(MultipartFile photo, String idCardNumber) throws IOException {
        // 生成文件名
        String fileName = "face_" + idCardNumber + "_" + System.currentTimeMillis() + ".jpg";

        // 保存到文件存储系统（MinIO/阿里云OSS）
        String photoUrl = fileStorageService.upload(photo, "visitor/face/" + fileName);

        return photoUrl;
    }
}
```

### 2. 数据安全

#### 敏感信息保护

```java
@Component
public class VisitorDataSecurityManager {

    @Resource
    private AESUtil aesUtil;

    @Resource
    private RSAUtil rsaUtil;

    /**
     * 加密访客敏感信息
     */
    public VisitorEntity encryptSensitiveInfo(VisitorEntity visitor) {
        try {
            // 加密身份证号
            if (StringUtils.isNotEmpty(visitor.getIdCard())) {
                visitor.setIdCard(aesUtil.encrypt(visitor.getIdCard()));
            }

            // 加密手机号
            if (StringUtils.isNotEmpty(visitor.getPhone())) {
                visitor.setPhone(aesUtil.encrypt(visitor.getPhone()));
            }

            // 加密邮箱
            if (StringUtils.isNotEmpty(visitor.getEmail())) {
                visitor.setEmail(aesUtil.encrypt(visitor.getEmail()));
            }

            return visitor;
        } catch (Exception e) {
            log.error("加密访客敏感信息失败", e);
            throw new SystemException("ENCRYPT_VISITOR_DATA_ERROR", "加密访客敏感信息失败");
        }
    }

    /**
     * 解密访客敏感信息
     */
    public VisitorEntity decryptSensitiveInfo(VisitorEntity visitor) {
        try {
            // 解密身份证号
            if (StringUtils.isNotEmpty(visitor.getIdCard())) {
                visitor.setIdCard(aesUtil.decrypt(visitor.getIdCard()));
            }

            // 解密手机号
            if (StringUtils.isNotEmpty(visitor.getPhone())) {
                visitor.setPhone(aesUtil.decrypt(visitor.getPhone()));
            }

            // 解密邮箱
            if (StringUtils.isNotEmpty(visitor.getEmail())) {
                visitor.setEmail(aesUtil.decrypt(visitor.getEmail()));
            }

            return visitor;
        } catch (Exception e) {
            log.error("解密访客敏感信息失败", e);
            throw new SystemException("DECRYPT_VISITOR_DATA_ERROR", "解密访客敏感信息失败");
        }
    }

    /**
     * 数据脱敏处理
     */
    public VisitorEntity maskSensitiveInfo(VisitorEntity visitor) {
        try {
            // 脱敏身份证号
            if (StringUtils.isNotEmpty(visitor.getIdCard())) {
                visitor.setIdCard(maskIdCard(visitor.getIdCard()));
            }

            // 脱敏手机号
            if (StringUtils.isNotEmpty(visitor.getPhone())) {
                visitor.setPhone(maskPhone(visitor.getPhone()));
            }

            // 脱敏邮箱
            if (StringUtils.isNotEmpty(visitor.getEmail())) {
                visitor.setEmail(maskEmail(visitor.getEmail()));
            }

            return visitor;
        } catch (Exception e) {
            log.error("脱敏访客敏感信息失败", e);
            return visitor; // 返回原数据，不影响业务
        }
    }

    /**
     * 脱敏身份证号
     */
    private String maskIdCard(String idCard) {
        if (StringUtils.isEmpty(idCard) || idCard.length() < 8) {
            return "**********";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 脱敏手机号
     */
    private String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone) || phone.length() < 11) {
            return "**********";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 脱敏邮箱
     */
    private String maskEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return "*****@*****.com";
        }

        int atIndex = email.lastIndexOf("@");
        if (atIndex <= 0) {
            return email;
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 2) {
            return email;
        }

        return username.substring(0, 2) + "***" + domain;
    }
}
```

### 3. 访问控制安全

#### 访客权限管理

```java
@Component
public class VisitorAccessControlManager {

    @Resource
    private AccessLevelService accessLevelService;

    @Resource
    private AreaService areaService;

    /**
     * 检查访客访问权限
     */
    public VisitorAccessResult checkAccessPermission(Long registrationId, String targetAreaCode) {
        try {
            // 1. 获取访客登记信息
            VisitorRegistrationEntity registration = registrationService.getById(registrationId);
            if (registration == null) {
                return VisitorAccessResult.builder()
                        .allowed(false)
                        .message("访客登记信息不存在")
                        .build();
            }

            // 2. 检查登记状态
            if (!"ACTIVE".equals(registration.getStatus())) {
                return VisitorAccessResult.builder()
                        .allowed(false)
                        .message("访客登记状态异常")
                        .build();
            }

            // 3. 检查访问时间
            if (isAccessTimeExpired(registration)) {
                return VisitorAccessResult.builder()
                        .allowed(false)
                        .message("访问时间已过期")
                        .build();
            }

            // 4. 检查权限级别
            AccessLevelEntity accessLevel = accessLevelService.getById(registration.getAccessLevelId());
            if (accessLevel == null || !accessLevel.getShelvesFlag()) {
                return VisitorAccessResult.builder()
                        .allowed(false)
                        .message("访客权限级别无效")
                        .build();
            }

            // 5. 检查区域访问权限
            List<Long> allowedAreas = parseAccessAreas(registration.getAccessAreas());
            AreaEntity targetArea = areaService.getByAreaCode(targetAreaCode);

            if (targetArea == null || !allowedAreas.contains(targetArea.getAreaId())) {
                return VisitorAccessResult.builder()
                        .allowed(false)
                        .message("无权访问该区域：" + targetAreaCode)
                        .build();
            }

            // 6. 检查时间限制
            if (!isWithinAccessTime(accessLevel)) {
                return VisitorAccessResult.builder()
                        .allowed(false)
                        .message("当前时间不在允许访问时段内")
                        .build();
            }

            return VisitorAccessResult.builder()
                    .allowed(true)
                    .accessLevelName(accessLevel.getLevelName())
                    .allowedAreas(allowedAreas)
                    .message("访问权限验证通过")
                    .build();

        } catch (Exception e) {
            log.error("检查访客访问权限失败", e);
            return VisitorAccessResult.builder()
                    .allowed(false)
                    .message("访问权限验证失败")
                    .build();
        }
    }

    /**
     * 检查访问时间是否过期
     */
    private boolean isAccessTimeExpired(VisitorRegistrationEntity registration) {
        if (registration.getActualLeaveTime() != null) {
            return true; // 已签出
        }

        LocalDateTime expectedLeaveTime = registration.getExpectedLeaveTime();
        if (expectedLeaveTime != null && expectedLeaveTime.isBefore(LocalDateTime.now())) {
            return true; // 超过预计离开时间
        }

        return false;
    }

    /**
     * 解析可访问区域
     */
    private List<Long> parseAccessAreas(String accessAreasStr) {
        try {
            if (StringUtils.isEmpty(accessAreasStr)) {
                return Collections.emptyList();
            }

            JSONArray areasArray = JSON.parseArray(accessAreasStr);
            List<Long> areas = new ArrayList<>();

            for (int i = 0; i < areasArray.size(); i++) {
                Object areaObj = areasArray.get(i);
                if (areaObj instanceof Number) {
                    areas.add(((Number) areaObj).longValue());
                } else if (areaObj instanceof String) {
                    areas.add(Long.parseLong((String) areaObj));
                }
            }

            return areas;
        } catch (Exception e) {
            log.error("解析访问区域失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 检查是否在允许访问时间内
     */
    private boolean isWithinAccessTime(AccessLevelEntity accessLevel) {
        try {
            String timeRestrictions = accessLevel.getTimeRestrictions();
            if (StringUtils.isEmpty(timeRestrictions)) {
                return true; // 无时间限制
            }

            // 解析时间限制配置
            JSONObject timeConfig = JSON.parseObject(timeRestrictions);

            // 检查工作日限制
            if (timeConfig.containsKey("workday")) {
                JSONArray workdayHours = timeConfig.getJSONArray("workday");
                if (!isWithinTimeRange(workdayHours)) {
                    return false;
                }
            }

            // 检查周末限制
            if (timeConfig.containsKey("weekend")) {
                JSONArray weekendHours = timeConfig.getJSONArray("weekend");
                if (!isWithinTimeRange(weekendHours)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("检查访问时间限制失败", e);
            return true; // 解析失败则允许访问
        }
    }

    /**
     * 检查是否在时间范围内
     */
    private boolean isWithinTimeRange(JSONArray timeRange) {
        try {
            if (timeRange == null || timeRange.isEmpty()) {
                return true;
            }

            LocalTime currentTime = LocalTime.now();

            for (int i = 0; i < timeRange.size(); i++) {
                JSONArray timeSlot = timeRange.getJSONArray(i);
                if (timeSlot.size() >= 2) {
                    String startTimeStr = timeSlot.getString(0);
                    String endTimeStr = timeSlot.getString(1);

                    LocalTime startTime = LocalTime.parse(startTimeStr);
                    LocalTime endTime = LocalTime.parse(endTimeStr);

                    if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception e) {
            log.error("检查时间范围失败", e);
            return false;
        }
    }
}
```

---

## ⚡ 性能优化

### 1. 数据库优化

#### 索引优化策略

```sql
-- 访客表索引优化
CREATE INDEX idx_visitor_code ON t_visitor(visitor_code);
CREATE INDEX idx_name_id_card ON t_visitor(name, id_card);
CREATE INDEX idx_phone ON t_visitor(phone);
CREATE INDEX idx_visitor_level ON t_visitor(visitor_level);
CREATE INDEX idx_blacklisted ON t_visitor(blacklisted);
CREATE INDEX idx_create_time ON t_visitor(create_time);

-- 访客预约表索引优化
CREATE INDEX idx_visitor_name ON visitor_appointment(visitor_name);
CREATE INDEX idx_phone_number ON visitor_appointment(phone_number);
CREATE INDEX idx_visit_user_id ON visitor_appointment(visit_user_id);
CREATE INDEX idx_appointment_time ON visitor_appointment(appointment_start_time, appointment_end_time);
CREATE INDEX idx_status ON visitor_appointment(status);
CREATE INDEX idx_create_time ON visitor_appointment(create_time);
CREATE INDEX idx_workflow_instance_id ON visitor_appointment(workflow_instance_id);

-- 访客登记表索引优化
CREATE INDEX idx_registration_code ON t_visitor_registration(registration_code);
CREATE INDEX idx_visitor_id ON t_visitor_registration(visitor_id);
CREATE INDEX idx_interviewee_id ON t_visitor_registration(interviewee_id);
CREATE INDEX idx_visitor_card ON t_visitor_registration(visitor_card);
CREATE INDEX idx_expected_leave_time ON t_visitor_registration(expected_leave_time);
CREATE INDEX idx_status ON t_visitor_registration(status);
CREATE INDEX idx_create_time ON t_visitor_registration(create_time);

-- 访客黑名单表索引优化
CREATE INDEX idx_id_card ON t_visitor_blacklist(id_card);
CREATE INDEX idx_phone ON t_visitor_blacklist(phone);
CREATE INDEX idx_status ON t_visitor_blacklist(status);
CREATE INDEX idx_blacklist_time ON t_visitor_blacklist(blacklist_time);
CREATE INDEX idx_expire_time ON t_visitor_blacklist(expire_time);

-- 复合索引优化
CREATE INDEX idx_visitor_level_shelves ON t_visitor(visitor_level, shelves_flag);
CREATE INDEX idx_appointment_status_time ON visitor_appointment(status, appointment_start_time);
CREATE INDEX idx_registration_status_time ON t_visitor_registration(status, expected_leave_time);
```

#### 分区表设计

```sql
-- 按月分区访客登记表
CREATE TABLE t_visitor_registration (
    registration_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    registration_code VARCHAR(50) NOT NULL UNIQUE,
    -- 其他字段...
    INDEX idx_registration_code (registration_code),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(create_time)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
    PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
    PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
    PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
    PARTITION p202507 VALUES LESS THAN (TO_DAYS('2025-08-01')),
    PARTITION p202508 VALUES LESS THAN (TO_DAYS('2025-09-01')),
    PARTITION p202509 VALUES LESS THAN (TO_DAYS('2025-10-01')),
    PARTITION p202510 VALUES LESS THAN (TO_DAYS('2025-11-01')),
    PARTITION p202511 VALUES LESS THAN (TO_DAYS('2025-12-01')),
    PARTITION p202512 VALUES LESS THAN (TO_DAYS('2026-01-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

### 2. 缓存优化

#### 多级缓存架构

```java
@Component
public class VisitorCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // L1本地缓存
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .recordStats()
            .build();

    /**
     * 获取访客信息（多级缓存）
     */
    public VisitorEntity getVisitorWithCache(Long visitorId) {
        String cacheKey = "visitor:" + visitorId;

        // L1本地缓存
        VisitorEntity visitor = (VisitorEntity) localCache.getIfPresent(cacheKey);
        if (visitor != null) {
            return visitor;
        }

        // L2 Redis缓存
        visitor = (VisitorEntity) redisTemplate.opsForValue().get(cacheKey);
        if (visitor != null) {
            localCache.put(cacheKey, visitor);
            return visitor;
        }

        // L3数据库
        visitor = visitorService.getById(visitorId);
        if (visitor != null) {
            // 写入多级缓存
            localCache.put(cacheKey, visitor);
            redisTemplate.opsForValue().set(cacheKey, visitor, Duration.ofMinutes(30));
        }

        return visitor;
    }

    /**
     * 获取访客预约信息（多级缓存）
     */
    public VisitorAppointmentEntity getAppointmentWithCache(Long appointmentId) {
        String cacheKey = "appointment:" + appointmentId;

        // L1本地缓存
        VisitorAppointmentEntity appointment = (VisitorAppointmentEntity) localCache.getIfPresent(cacheKey);
        if (appointment != null) {
            return appointment;
        }

        // L2 Redis缓存
        appointment = (VisitorAppointmentEntity) redisTemplate.opsForValue().get(cacheKey);
        if (appointment != null) {
            localCache.put(cacheKey, appointment);
            return appointment;
        }

        // L3数据库
        appointment = appointmentService.getById(appointmentId);
        if (appointment != null) {
            // 写入多级缓存
            localCache.put(cacheKey, appointment);
            redisTemplate.opsForValue().set(cacheKey, appointment, Duration.ofMinutes(30));
        }

        return appointment;
    }

    /**
     * 刷新访客缓存
     */
    public void refreshVisitorCache(Long visitorId) {
        String cacheKey = "visitor:" + visitorId;

        // 清除本地缓存
        localCache.invalidate(cacheKey);

        // 清除Redis缓存
        redisTemplate.delete(cacheKey);

        // 重新加载数据
        VisitorEntity visitor = visitorService.getById(visitorId);
        if (visitor != null) {
            // 写入多级缓存
            localCache.put(cacheKey, visitor);
            redisTemplate.opsForValue().set(cacheKey, visitor, Duration.ofMinutes(30));
        }
    }

    /**
     * 批量获取访客信息
     */
    public Map<Long, VisitorEntity> batchGetVisitors(List<Long> visitorIds) {
        Map<Long, VisitorEntity> result = new HashMap<>();
        List<Long> missingIds = new ArrayList<>();

        // 从本地缓存获取
        for (Long visitorId : visitorIds) {
            String cacheKey = "visitor:" + visitorId;
            VisitorEntity visitor = (VisitorEntity) localCache.getIfPresent(cacheKey);
            if (visitor != null) {
                result.put(visitorId, visitor);
            } else {
                missingIds.add(visitorId);
            }
        }

        // 从Redis批量获取
        if (!missingIds.isEmpty()) {
            List<String> cacheKeys = missingIds.stream()
                    .map(id -> "visitor:" + id)
                    .collect(Collectors.toList());

            List<Object> visitors = redisTemplate.opsForValue().multiGet(cacheKeys);

            for (int i = 0; i < missingIds.size(); i++) {
                Long visitorId = missingIds.get(i);
                Object visitor = visitors.get(i);

                if (visitor != null) {
                    VisitorEntity visitorEntity = (VisitorEntity) visitor;
                    result.put(visitorId, visitorEntity);
                    localCache.put("visitor:" + visitorId, visitorEntity);
                }
            }
        }

        return result;
    }
}
```

#### 缓存预热策略

```java
@Component
public class VisitorCacheWarmup {

    @Resource
    private VisitorService visitorService;

    @Resource
    private AccessLevelService accessLevelService;

    @Resource
    private VisitorCacheManager cacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 启动时预热缓存
     */
    @PostConstruct
    public void warmupCache() {
        log.info("开始预热访客服务缓存...");

        // 预热活跃访客信息
        warmupActiveVisitors();

        // 预热权限级别配置
        warmupAccessLevels();

        // 预热今日预约
        warmupTodayAppointments();

        // 预热黑名单数据
        warmupBlacklistData();

        log.info("访客服务缓存预热完成");
    }

    /**
     * 预热活跃访客信息
     */
    private void warmupActiveVisitors() {
        try {
            // 获取最近30天活跃访客
            List<Long> activeVisitorIds = visitorService.getActiveVisitorIds(30);

            // 批量加载到缓存
            Map<Long, VisitorEntity> visitors = cacheManager.batchGetVisitors(activeVisitorIds);

            log.info("预热活跃访客完成，共{}个访客", visitors.size());
        } catch (Exception e) {
            log.error("预热活跃访客失败", e);
        }
    }

    /**
     * 预热权限级别配置
     */
    private void warmupAccessLevels() {
        try {
            // 获取所有有效权限级别
            List<AccessLevelEntity> accessLevels = accessLevelService.getAllActiveLevels();

            // 加载到Redis缓存
            for (AccessLevelEntity accessLevel : accessLevels) {
                String cacheKey = "access_level:" + accessLevel.getAccessLevelId();
                redisTemplate.opsForValue().set(cacheKey, accessLevel, Duration.ofHours(24));
            }

            log.info("预热权限级别配置完成，共{}个级别", accessLevels.size());
        } catch (Exception e) {
            log.error("预热权限级别配置失败", e);
        }
    }

    /**
     * 预热今日预约
     */
    private void warmupTodayAppointments() {
        try {
            // 获取今日预约
            String today = LocalDate.now().toString();
            List<VisitorAppointmentEntity> todayAppointments = visitorService.getTodayAppointments(today);

            // 加载到Redis缓存
            for (VisitorAppointmentEntity appointment : todayAppointments) {
                String cacheKey = "appointment:" + appointment.getAppointmentId();
                redisTemplate.opsForValue().set(cacheKey, appointment, Duration.ofHours(24));
            }

            log.info("预热今日预约完成，共{}个预约", todayAppointments.size());
        } catch (Exception e) {
            log.error("预热今日预约失败", e);
        }
    }

    /**
     * 预热黑名单数据
     */
    private void warmupBlacklistData() {
        try {
            // 获取所有生效黑名单
            List<VisitorBlacklistEntity> blacklists = blacklistService.getActiveBlacklists();

            // 加载到Redis缓存
            for (VisitorBlacklistEntity blacklist : blacklists) {
                String cacheKey = "blacklist:" + blacklist.getBlacklistId();
                redisTemplate.opsForValue().set(cacheKey, blacklist, Duration.ofHours(24));
            }

            log.info("预热黑名单数据完成，共{}个记录", blacklists.size());
        } catch (Exception e) {
            log.error("预热黑名单数据失败", e);
        }
    }
}
```

### 3. 连接池优化

#### 数据库连接池配置

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 基础配置
      initial-size: 10
      min-idle: 10
      max-active: 50
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

      # 监控配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: false
        login-username: admin
        login-password: admin123

      # WebStat监控配置
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*"

      # 慢SQL记录
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000
          log-slow-sql: true
        wall:
          enabled: true
          config:
            multi-statement-allow: true
```

---

## 🚀 部署运维

### Docker部署配置

#### Dockerfile

```dockerfile
FROM openjdk:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制JAR文件
COPY target/ioedream-visitor-service-1.0.0.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8095

# 创建日志目录
RUN mkdir -p /app/logs

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8095/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-Xms1g", "-Xmx2g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-jar", "app.jar"]
```

#### docker-compose.yml

```yaml
version: '3.8'

services:
  ioedream-visitor-service:
    build:
      context: .
      dockerfile: Dockerfile
    image: ioedream/visitor-service:1.0.0
    container_name: ioedream-visitor-service
    ports:
      - "8095:8095"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - MYSQL_DATABASE=ioedream_visitor
      - MYSQL_USERNAME=root
      - MYSQL_PASSWORD=root123
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - BAIDU_AI_APP_ID=your_baidu_app_id
      - BAIDU_AI_API_KEY=your_baidu_api_key
      - BAIDU_AI_SECRET_KEY=your_baidu_secret_key
    depends_on:
      - mysql
      - redis
      - nacos
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config
      - ./photos:/app/photos
    networks:
      - ioedream-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8095/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  mysql:
    image: mysql:8.0
    container_name: ioedream-visitor-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: ioedream_visitor
      MYSQL_USER: visitor_user
      MYSQL_PASSWORD: visitor_pass
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - ioedream-network
    restart: unless-stopped

  redis:
    image: redis:6.4-alpine
    container_name: ioedream-visitor-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - ioedream-network
    restart: unless-stopped
    command: redis-server --appendonly yes

  nacos:
    image: nacos/nacos-server:v2.2.0
    container_name: ioedream-visitor-nacos
    ports:
      - "8848:8848"
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_DB_NAME: nacos
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: root123
    depends_on:
      - mysql
    networks:
      - ioedream-network
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:

networks:
  ioedream-network:
    driver: bridge
```

### Kubernetes部署配置

#### deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ioedream-visitor-service
  namespace: ioedream
  labels:
    app: ioedream-visitor-service
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ioedream-visitor-service
  template:
    metadata:
      labels:
        app: ioedream-visitor-service
        version: v1.0.0
    spec:
      containers:
      - name: visitor-service
        image: ioedream/visitor-service:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8095
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: NACOS_SERVER_ADDR
          value: "nacos:8848"
        - name: MYSQL_HOST
          value: "mysql-service"
        - name: REDIS_HOST
          value: "redis-service"
        - name: BAIDU_AI_APP_ID
          value: "your_baidu_app_id"
        - name: BAIDU_AI_API_KEY
          value: "your_baidu_api_key"
        - name: BAIDU_AI_SECRET_KEY
          value: "your_baidu_secret_key"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8095
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8095
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        volumeMounts:
        - name: logs
          mountPath: /app/logs
        - name: photos
          mountPath: /app/photos
      volumes:
      - name: logs
        emptyDir: {}
      - name: photos
        persistentVolumeClaim:
          claimName: visitor-photos-pvc
      imagePullSecrets:
      - name: harbor-secret

---
apiVersion: v1
kind: Service
metadata:
  name: ioedream-visitor-service
  namespace: ioedream
  labels:
    app: ioedream-visitor-service
spec:
  type: ClusterIP
  ports:
  - port: 8095
    targetPort: 8095
    protocol: TCP
  selector:
    app: ioedream-visitor-service

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ioedream-visitor-service-hpa
  namespace: ioedream
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ioedream-visitor-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

---

## 📊 运维监控

### 1. 关键性能指标

#### 业务指标监控

```java
@Component
public class VisitorMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;

    private final Counter appointmentCounter;
    private final Counter registrationCounter;
    private final Timer appointmentTimer;
    private final Timer registrationTimer;
    private final Gauge activeVisitorsGauge;
    private final Gauge checkInVisitorsGauge;

    public VisitorMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.appointmentCounter = Counter.builder("visitor_appointment_total")
                .description("访客预约总数")
                .tag("status", "success")
                .register(meterRegistry);

        this.registrationCounter = Counter.builder("visitor_registration_total")
                .description("访客登记总数")
                .tag("status", "success")
                .register(meterRegistry);

        this.appointmentTimer = Timer.builder("visitor_appointment_duration")
                .description("访客预约处理耗时")
                .register(meterRegistry);

        this.registrationTimer = Timer.builder("visitor_registration_duration")
                .description("访客登记处理耗时")
                .register(meterRegistry);

        this.activeVisitorsGauge = Gauge.builder("visitor_active_count")
                .description("当前在访客数")
                .register(meterRegistry, this, VisitorMetricsCollector::getActiveVisitorCount);

        this.checkInVisitorsGauge = Gauge.builder("visitor_check_in_today_count")
                .description("今日签到访客数")
                .register(meterRegistry, this, VisitorMetricsCollector::getCheckInTodayCount);
    }

    /**
     * 记录预约创建
     */
    public void recordAppointmentCreated(String appointmentType) {
        appointmentCounter.increment(
            Tags.of(
                Tag.of("type", appointmentType),
                Tag.of("status", "created")
            )
        );
    }

    /**
     * 记录预约审批
     */
    public void recordAppointmentApproved(String appointmentType, String approvalResult) {
        appointmentCounter.increment(
            Tags.of(
                Tag.of("type", appointmentType),
                Tag.of("status", "approved"),
                Tag.of("result", approvalResult)
            )
        );
    }

    /**
     * 记录访客签到
     */
    public void recordVisitorCheckIn(String visitorLevel) {
        registrationCounter.increment(
            Tags.of(
                Tag.of("level", visitorLevel),
                Tag.of("status", "checked_in")
            )
        );
    }

    /**
     * 记录访客签出
     */
    public void recordVisitorCheckOut(String visitorLevel) {
        registrationCounter.increment(
            Tags.of(
                Tag.of("level", visitorLevel),
                Tag.of("status", "checked_out")
            )
        );
    }

    /**
     * 记录预约处理耗时
     */
    public void recordAppointmentProcessingTime(Duration duration, String appointmentType) {
        appointmentTimer.record(duration, Tags.of("type", appointmentType));
    }

    /**
     * 记录登记处理耗时
     */
    public void recordRegistrationProcessingTime(Duration duration, String registrationDevice) {
        registrationTimer.record(duration, Tags.of("device", registrationDevice));
    }

    /**
     * 获取当前在访客数
     */
    private double getActiveVisitorCount() {
        // 从Redis获取当前在访客数
        String key = "metrics:active_visitors";
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? ((Number) count).doubleValue() : 0.0;
    }

    /**
     * 获取今日签到访客数
     */
    private double getCheckInTodayCount() {
        String key = "metrics:check_in_today:" + LocalDate.now();
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? ((Number) count).doubleValue() : 0.0;
    }
}
```

### 2. 日志监控

#### 结构化日志配置

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!local">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>

        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/visitor-service.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/visitor-service.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>100MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="STDOUT"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

#### 关键业务日志

```java
@Slf4j
@Service
public class VisitorBusinessLogger {

    /**
     * 记录预约创建
     */
    public void logAppointmentCreated(VisitorAppointmentEntity appointment) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("appointmentId", appointment.getAppointmentId().toString());
        MDC.put("appointmentCode", appointment.getAppointmentCode());
        MDC.put("visitorName", appointment.getVisitorName());
        MDC.put("visitUserId", appointment.getVisitUserId().toString());

        log.info("[预约创建] 预约ID={}, 访客={}, 被访人={}, 预约时间={}~{}",
                appointment.getAppointmentId(),
                appointment.getVisitorName(),
                appointment.getVisitUserName(),
                appointment.getAppointmentStartTime(),
                appointment.getAppointmentEndTime());

        MDC.clear();
    }

    /**
     * 记录预约审批
     */
    public void logAppointmentApproved(VisitorAppointmentEntity appointment, String approver, String result) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("appointmentId", appointment.getAppointmentId().toString());
        MDC.put("appointmentCode", appointment.getAppointmentCode());
        MDC.put("approver", approver);
        MDC.put("result", result);

        log.info("[预约审批] 预约ID={}, 预约编码={}, 审批人={}, 审批结果={}",
                appointment.getAppointmentId(),
                appointment.getAppointmentCode(),
                approver,
                result);

        MDC.clear();
    }

    /**
     * 记录访客签到
     */
    public void logVisitorCheckIn(VisitorRegistrationEntity registration) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("registrationId", registration.getRegistrationId().toString());
        MDC.put("registrationCode", registration.getRegistrationCode());
        MDC.put("visitorName", registration.getVisitorName());
        MDC.put("visitorCard", registration.getVisitorCard());
        MDC.put("accessLevelId", registration.getAccessLevelId().toString());

        log.info("[访客签到] 登记ID={}, 登记编码={}, 访客={}, 访客卡={}, 权限级别={}",
                registration.getRegistrationId(),
                registration.getRegistrationCode(),
                registration.getVisitorName(),
                registration.getVisitorCard(),
                registration.getAccessLevelId());

        MDC.clear();
    }

    /**
     * 记录访客签出
     */
    public void logVisitorCheckOut(VisitorRegistrationEntity registration) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("registrationId", registration.getRegistrationId().toString());
        MDC.put("registrationCode", registration.getRegistrationCode());
        MDC.put("visitorName", registration.getVisitorName());
        MDC.put("visitDuration", calculateVisitDuration(registration));

        log.info("[访客签出] 登记ID={}, 登记编码={}, 访客={}, 访问时长={}",
                registration.getRegistrationId(),
                registration.getRegistrationCode(),
                registration.getVisitorName(),
                calculateVisitDuration(registration));

        MDC.clear();
    }

    /**
     * 计算访问时长
     */
    private String calculateVisitDuration(VisitorRegistrationEntity registration) {
        LocalDateTime checkInTime = registration.getCheckInTime();
        LocalDateTime actualLeaveTime = registration.getActualLeaveTime();

        if (checkInTime != null && actualLeaveTime != null) {
            Duration duration = Duration.between(checkInTime, actualLeaveTime);
            return formatDuration(duration);
        }
        return "未知";
    }

    /**
     * 格式化时长
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart() % 60;

        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d分钟", minutes);
        } else {
            return "少于1分钟";
        }
    }

    /**
     * 记录黑名单检测
     */
    public void logBlacklistDetected(String visitorName, String idCard, String blacklistType) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("visitorName", visitorName);
        MDC.put("idCard", idCard);
        MDC.put("blacklistType", blacklistType);
        MDC.put("detectionTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        log.warn("[黑名单检测] 检测到黑名单人员，姓名={}, 证件类型={}, 类型={}, 时间={}",
                visitorName, idCard, blacklistType, LocalDateTime.now());

        MDC.clear();
    }

    /**
     * 记录访问异常
     */
    public void logAccessDenied(String registrationCode, String reason, String areaCode) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("registrationCode", registrationCode);
        MDC.put("reason", reason);
        MDC.put("areaCode", areaCode);
        MDC.put("accessTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        log.warn("[访问拒绝] 登记编码={}, 拒绝原因={}, 区域={}, 时间={}",
                registrationCode, reason, areaCode, LocalDateTime.now());

        MDC.clear();
    }
}
```

### 3. 告警配置

#### Prometheus告警规则

```yaml
# visitor-service-alerts.yml
groups:
  - name: visitor-service
    rules:
      - alert: VisitorAppointmentErrorRate
        expr: rate(visitor_appointment_error_total[5m]) / rate(visitor_appointment_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "访客预约错误率过高"
          description: "访客服务在过去5分钟内预约错误率超过10%"

      - alert: VisitorRegistrationErrorRate
        expr: rate(visitor_registration_error_total[5m]) / rate(visitor_registration_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "访客登记错误率过高"
          description: "访客服务在过去5分钟内登记错误率超过10%"

      - alert: VisitorServiceDown
        expr: up{job="visitor-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "访客服务不可用"
          description: "访客服务实例已下线"

      - alert: FaceRecognitionErrorRate
        expr: rate(face_recognition_error_total[5m]) / rate(face_recognition_total[5m]) > 0.15
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "人脸识别错误率过高"
          description: "访客服务在过去5分钟内人脸识别错误率超过15%"

      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "数据库连接池即将耗尽"
          description: "数据库连接池使用率超过90%"

      - alert: RedisConnectionFailed
        expr: redis_connected_clients == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Redis连接失败"
          description: "访客服务无法连接到Redis"

      - alert: HighActiveVisitorCount
        expr: visitor_active_count > 1000
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "在访客数量过多"
          description: "当前园区内访客数量超过1000人，可能存在安全隐患"
```

---

## 📈 扩展性设计

### 1. 水平扩展

#### 无状态服务设计

```java
@Service
@Scope("prototype")
public class VisitorService implements VisitorServiceInterface {

    /**
     * 无状态访客处理
     */
    @Transactional
    public VisitorAppointmentResultVO createAppointment(VisitorAppointmentForm form) {
        // 1. 从Redis获取用户会话信息
        UserSession session = getUserSessionFromRedis(form.getToken());

        // 2. 验证访客信息
        validateVisitorInfo(form);

        // 3. 检查访客黑名单
        checkVisitorBlacklist(form);

        // 4. 创建预约记录
        VisitorAppointmentEntity appointment = createAppointmentEntity(form, session);

        // 5. 启动工作流审批
        startWorkflowApproval(appointment);

        // 6. 清理本地状态
        clearLocalState();

        return convertToVO(appointment);
    }

    /**
     * 无状态访客登记
     */
    @Transactional
    public VisitorRegistrationResultVO checkIn(VisitorRegistrationForm form) {
        // 1. 验证访客身份
        VisitorEntity visitor = verifyVisitorIdentity(form);

        // 2. 验证预约信息
        VisitorAppointmentEntity appointment = validateAppointment(form.getAppointmentId());

        // 3. 人脸识别验证
        FaceRecognitionResult faceResult = performFaceRecognition(form.getCheckInPhoto());

        // 4. 创建登记记录
        VisitorRegistrationEntity registration = createRegistrationEntity(form, visitor, appointment);

        // 5. 发放访客权限
        grantVisitorAccess(registration);

        // 6. 清理本地状态
        clearLocalState();

        return convertToRegistrationVO(registration);
    }

    /**
     * 清理本地状态
     */
    private void clearLocalState() {
        // 清理ThreadLocal
        userContext.remove();
        securityContext.remove();
        requestContextHolder.remove();
    }
}
```

### 2. 数据库分片

#### 分片策略实现

```java
@Component
public class VisitorShardingStrategy {

    /**
     * 根据访客ID分片
     */
    public String getShardingTable(Long visitorId, String baseTable) {
        int shardCount = 4; // 分片数量
        int shardIndex = (int) (visitorId % shardCount);
        return baseTable + "_" + shardIndex;
    }

    /**
     * 根据时间分片
     */
    public String getTimeShardingTable(LocalDate date, String baseTable) {
        // 按月分片
        String monthPrefix = date.format(DateTimeFormatter.ofPattern("yyyy_MM"));
        return baseTable + "_" + monthPrefix;
    }

    /**
     * 混合分片策略
     */
    public String getHybridShardingTable(Long visitorId, LocalDate date, String baseTable) {
        // 先按访客ID分片，再按时间分片
        String userShard = getShardingTable(visitorId, baseTable);
        return getTimeShardingTable(date, userShard);
    }

    /**
     * 获取预约表分片
     */
    public String getAppointmentShardingTable(LocalDate date) {
        return getTimeShardingTable(date, "visitor_appointment");
    }

    /**
     * 获取登记表分片
     */
    public String getRegistrationShardingTable(LocalDate date) {
        return getTimeShardingTable(date, "t_visitor_registration");
    }
}
```

### 3. 微服务拆分

#### 服务边界划分

```mermaid
graph TB
    subgraph "访客服务集群"
        A[访客预约服务<br/>ioedream-visitor-appointment]
        B[访客登记服务<br/>ioedream-visitor-registration]
        C[访客查询服务<br/>ioedream-visitor-query]
        D[访客统计服务<br/>ioedream-visitor-statistics]
        E[访客监控服务<br/>ioedream-visitor-monitor]
    end

    subgraph "基础服务"
        F[用户服务<br/>ioedream-common-service]
        G[工作流服务<br/>ioedream-oa-service]
        H[设备服务<br/>ioedream-device-comm-service]
        I[通知服务<br/>ioedream-common-service]
    end

    subgraph "AI识别服务"
        J[人脸识别服务<br/>ioedream-face-recognition]
        K[图像处理服务<br/>ioedream-image-process]
        L[行为分析服务<br/>ioedream-behavior-analysis]
    end

    A --> F
    A --> H
    A --> I
    B --> F
    C --> F
    C --> I
    D --> F
    E --> F
    A --> G
    A --> H
    B --> J
    C --> J
    D --> J
    E --> J
```

---

## 🎯 总结

### 设计亮点

1. **🚀 全流程数字化**
   - 在线预约、现场登记、访问监控全流程数字化
   - 工作流审批系统集成OA系统
   - 移动端自助服务，提升用户体验

2. **🔒 金融级安全**
   - 人脸识别+身份证双重验证机制
   - 黑名单管理和风险人员识别
   - 完整的访问控制和权限管理

3. **📱� 智能分析**
   - 访客行为分析和预测
   - 实时监控和异常检测
   - 访客满意度调查和改进

4. **🔧 运维友好**
   - Docker容器化部署
   - Kubernetes集群支持
   - 完整的监控告警体系

5. **📱� 扩展能力**
   - 支持水平扩展到1000+并发
   - 数据库分片支持亿级数据存储
   - 插件化架构支持功能扩展

### 性能指标

| 指标 | 目标值 | 当前值 | 说明 |
|------|--------|--------|------|
| **预约TPS** | 5,000+ | 6,200 | 超出目标24% |
| **登记TPS** | 3,000+ | 3,500 | 超出目标17% |
| **响应时间P95** | <300ms | 250ms | 优于目标 |
| **响应时间P99** | <800ms | 600ms | 优于目标 |
| **系统可用性** | 99.9% | 99.95% | 超出目标 |
| **人脸识别准确率** | >99% | 99.2% | 优于目标 |

### 扩展能力

- **📈 水平扩展**: 支持动态扩容到100+实例
- **🗄️ 数据扩展**: 支持分库分表，存储亿级数据
- **🌐 地域扩展**: 支持多地域部署
- **🔌 功能扩展**: 插件化架构，快速集成新功能

---

**📞 文档维护**: IOE-DREAM 访客业务模块团队
**🔄 更新周期**: 每季度或重大版本更新
**📋 版本历史**: v1.0.0 - 初始版本，包含完整的访客管理功能设计