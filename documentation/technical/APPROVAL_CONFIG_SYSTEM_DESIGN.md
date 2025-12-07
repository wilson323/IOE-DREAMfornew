# 可配置化审批系统设计文档

**版本**: v1.0.0  
**创建时间**: 2025-01-30  
**状态**: 已完成

---

## 📋 概述

本文档描述IOE-DREAM可配置化审批系统的设计，支持自定义审批类型和流程配置，无需修改代码即可配置新的审批流程。

### 核心特性

- ✅ **动态配置**：支持通过配置表动态配置审批流程，无需修改代码
- ✅ **自定义业务类型**：支持自定义业务类型，不局限于枚举
- ✅ **审批规则配置**：支持金额阈值、天数阈值等审批规则配置
- ✅ **审批后处理配置**：支持配置审批通过后的处理逻辑
- ✅ **超时配置**：支持配置审批超时策略
- ✅ **通知配置**：支持配置审批通知渠道和时机

---

## 🏗️ 架构设计

### 1. 核心组件

```
ApprovalConfigEntity (审批配置实体)
    ↓
ApprovalConfigDao (审批配置DAO)
    ↓
ApprovalConfigManager (审批配置管理器)
    ↓
WorkflowApprovalManager (工作流审批管理器)
    ↓
WorkflowEngineService (工作流引擎服务)
```

### 2. 数据流转

```
业务模块提交审批
    ↓
WorkflowApprovalManager.startApprovalProcess()
    ↓
ApprovalConfigManager.getDefinitionId() (动态获取流程定义ID)
    ↓
WorkflowEngineService.startProcess() (启动审批流程)
    ↓
审批完成后
    ↓
WorkflowApprovalResultListener.handleApprovalResult()
    ↓
ApprovalConfigManager.parsePostApprovalHandler() (解析审批后处理配置)
    ↓
执行审批后处理逻辑
```

---

## 📊 数据库设计

### 审批配置表 (t_common_approval_config)

```sql
CREATE TABLE `t_common_approval_config` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `business_type` varchar(100) NOT NULL COMMENT '业务类型（唯一标识）',
    `business_type_name` varchar(200) NOT NULL COMMENT '业务类型名称',
    `module` varchar(100) NOT NULL COMMENT '所属模块',
    `definition_id` bigint DEFAULT NULL COMMENT '流程定义ID',
    `process_key` varchar(100) DEFAULT NULL COMMENT '流程定义Key（备用）',
    `approval_rules` text COMMENT '审批规则配置（JSON格式）',
    `post_approval_handler` text COMMENT '审批后处理配置（JSON格式）',
    `timeout_config` text COMMENT '超时配置（JSON格式）',
    `notification_config` text COMMENT '通知配置（JSON格式）',
    `applicable_scope` text COMMENT '适用范围配置（JSON格式）',
    `status` varchar(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态（ENABLED-启用 DISABLED-禁用）',
    `sort_order` int DEFAULT 0 COMMENT '排序号',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `effective_time` datetime DEFAULT NULL COMMENT '生效时间',
    `expire_time` datetime DEFAULT NULL COMMENT '失效时间（null表示永久有效）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` tinyint NOT NULL DEFAULT 0 COMMENT '删除标志（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_business_type` (`business_type`, `deleted_flag`),
    KEY `idx_module` (`module`),
    KEY `idx_status` (`status`),
    KEY `idx_definition_id` (`definition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批配置表';
```

---

## 🔧 配置示例

### 1. 自定义审批类型配置

```json
{
  "businessType": "CUSTOM_APPROVAL_001",
  "businessTypeName": "自定义审批001",
  "module": "自定义模块",
  "definitionId": 100,
  "approvalRules": {
    "amount_threshold": 1000,
    "days_threshold": 3,
    "auto_approve_conditions": {
      "max_amount": 100,
      "max_days": 1
    },
    "approval_levels": [
      {
        "level": 1,
        "approver_type": "direct_manager",
        "required": true
      },
      {
        "level": 2,
        "approver_type": "hr_manager",
        "required": false,
        "condition": "days >= 3"
      }
    ]
  },
  "postApprovalHandler": {
    "on_approved": {
      "handler_type": "service_call",
      "service_name": "custom-service",
      "method": "processCustomApproval",
      "params": {
        "businessKey": "${businessKey}",
        "approverId": "${approverId}"
      }
    }
  },
  "timeoutConfig": {
    "timeout_hours": 24,
    "timeout_strategy": "escalate",
    "escalate_to": "next_level"
  },
  "notificationConfig": {
    "notify_applicant": true,
    "notify_approver": true,
    "notify_channels": ["email", "sms", "wechat"]
  },
  "status": "ENABLED"
}
```

### 2. 使用示例

#### 2.1 业务模块启动审批（支持动态配置）

```java
// 方式1：传入null，从配置中动态获取流程定义ID
ResponseDTO<Long> result = workflowApprovalManager.startApprovalProcess(
    null,  // definitionId为null，从配置中获取
    "CUSTOM_APPROVAL_001",
    "自定义审批-001",
    userId,
    "CUSTOM_APPROVAL_001",  // 自定义业务类型
    formData,
    variables
);

// 方式2：传入流程定义ID（兼容旧代码）
ResponseDTO<Long> result = workflowApprovalManager.startApprovalProcess(
    WorkflowDefinitionConstants.ATTENDANCE_LEAVE,  // 硬编码流程定义ID
    leaveNo,
    "请假申请-" + leaveNo,
    userId,
    BusinessTypeEnum.ATTENDANCE_LEAVE.name(),
    formData,
    variables
);
```

#### 2.2 通过API创建审批配置

```bash
POST /api/v1/workflow/approval-config
Content-Type: application/json

{
  "businessType": "CUSTOM_APPROVAL_001",
  "businessTypeName": "自定义审批001",
  "module": "自定义模块",
  "definitionId": 100,
  "approvalRules": "{\"amount_threshold\": 1000}",
  "status": "ENABLED"
}
```

---

## 📚 API接口

### 审批配置管理接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/workflow/approval-config/page` | 分页查询审批配置 |
| GET | `/api/v1/workflow/approval-config/{id}` | 根据ID查询审批配置 |
| GET | `/api/v1/workflow/approval-config/business-type/{businessType}` | 根据业务类型查询审批配置 |
| POST | `/api/v1/workflow/approval-config` | 创建审批配置 |
| PUT | `/api/v1/workflow/approval-config/{id}` | 更新审批配置 |
| DELETE | `/api/v1/workflow/approval-config/{id}` | 删除审批配置 |
| PUT | `/api/v1/workflow/approval-config/{id}/enable` | 启用审批配置 |
| PUT | `/api/v1/workflow/approval-config/{id}/disable` | 禁用审批配置 |

---

## ✅ 实施检查清单

- [x] 创建审批配置实体（ApprovalConfigEntity）
- [x] 创建审批配置DAO（ApprovalConfigDao）
- [x] 创建审批配置Manager（ApprovalConfigManager）
- [x] 修改WorkflowApprovalManager支持动态配置
- [x] 创建审批配置Service和Controller
- [x] 创建数据库表SQL脚本
- [x] 创建MyBatis XML映射文件
- [ ] 更新WorkflowApprovalResultListener支持动态配置的审批后处理
- [ ] 编写单元测试
- [ ] 更新API文档

---

**👥 制定人**: IOE-DREAM 架构团队  
**🏗️ 技术架构师**: SmartAdmin 核心团队  
**✅ 最终解释权**: IOE-DREAM 项目架构委员会  
**📅 版本**: v1.0.0

