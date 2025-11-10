# 审批流程公共模块开发文档 (smart-workflow)
**基于 warm-flow 工作流引擎集成**

## 1. 模块概述

### 1.1 模块简介

审批流程公共模块基于 **warm-flow** 工作流引擎构建，为SmartAdmin系统提供统一的审批流程管理能力。warm-flow 是一个轻量级、高性能的国产工作流引擎，采用"代码即流程"的设计理念，无需复杂的流程设计器，通过Java代码即可定义流程。

### 1.2 技术选型

**工作流引擎**: [warm-flow](https://gitee.com/dromara/warm-flow) v2.0+
**集成方式**: Spring Boot Starter
**设计理念**: 代码即流程，轻量高效
**核心优势**:
- 无需复杂流程设计器，Java代码定义流程
- 高性能内存执行引擎
- 简单易用的API接口
- 灵活的自定义处理器支持
- 完善的监听器和事件机制

### 1.3 设计目标

- **轻量集成**: 基于 warm-flow 快速构建审批流程
- **代码驱动**: 通过Java代码定义和管理流程
- **高性能**: 利用 warm-flow 的高效执行引擎
- **业务适配**: 支持不同业务场景的审批流程
- **易于扩展**: 基于warm-flow的扩展机制

### 1.4 核心能力

- 流程定义和管理（基于Java代码）
- 流程实例执行和监控
- 任务分配和处理
- 条件路由和分支控制
- 审批记录和历史追溯
- 流程性能监控
- 自定义业务处理器
- 事件监听和回调

## 2. Warm-Flow 集成架构

### 2.1 Warm-Flow 核心表结构

Warm-Flow 使用标准的数据库表结构来管理工作流数据：

#### 2.1.1 流程定义表 (t_definition)

```sql
CREATE TABLE `t_definition` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `definition_code` varchar(64) NOT NULL COMMENT '流程定义编码',
    `definition_name` varchar(255) NOT NULL COMMENT '流程定义名称',
    `definition_desc` varchar(255) DEFAULT NULL COMMENT '流程定义描述',
    `handler` varchar(255) NOT NULL COMMENT '流程处理器',
    `handler_type` tinyint NOT NULL DEFAULT '0' COMMENT '处理器类型 0类 1方法',
    `state` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0禁用 1启用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `version` varchar(255) NOT NULL COMMENT '版本号',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `is_delete` tinyint DEFAULT '0' COMMENT '删除标记 0未删除 1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_definition_code` (`definition_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';
```

#### 2.1.2 流程实例表 (t_instance)

```sql
CREATE TABLE `t_instance` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `definition_id` bigint NOT NULL COMMENT '流程定义ID',
    `instance_code` varchar(64) NOT NULL COMMENT '流程实例编码',
    `instance_name` varchar(255) NOT NULL COMMENT '流程实例名称',
    `instance_desc` varchar(255) DEFAULT NULL COMMENT '流程实例描述',
    `state` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0禁用 1启用 2完成 3拒绝',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `is_delete` tinyint DEFAULT '0' COMMENT '删除标记 0未删除 1删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_instance_code` (`instance_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';
```

#### 2.1.3 流程数据表 (t_data)

```sql
CREATE TABLE `t_data` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `node_code` varchar(64) NOT NULL COMMENT '节点编码',
    `node_name` varchar(255) NOT NULL COMMENT '节点名称',
    `node_type` tinyint NOT NULL COMMENT '节点类型 0开始 1结束 2条件 3串行 4并行',
    `node_state` tinyint NOT NULL DEFAULT '0' COMMENT '节点状态 0待处理 1处理中 2已完成',
    `handler` varchar(255) DEFAULT NULL COMMENT '处理人',
    `handler_type` tinyint DEFAULT '0' COMMENT '处理器类型 0类 1方法',
    `permission` varchar(255) DEFAULT NULL COMMENT '权限标识',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `variables` text COMMENT '变量数据',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程数据表';
```

#### 2.1.4 流程历史表 (t_history)

```sql
CREATE TABLE `t_history` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `instance_id` bigint NOT NULL COMMENT '流程实例ID',
    `node_code` varchar(64) NOT NULL COMMENT '节点编码',
    `node_name` varchar(255) NOT NULL COMMENT '节点名称',
    `node_type` tinyint NOT NULL COMMENT '节点类型 0开始 1结束 2条件 3串行 4并行',
    `handler` varchar(255) DEFAULT NULL COMMENT '处理人',
    `handler_type` tinyint DEFAULT '0' COMMENT '处理器类型 0类 1方法',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `variables` text COMMENT '变量数据',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程历史表';
```

### 2.2 业务扩展表结构

基于 warm-flow 的核心表，我们设计业务扩展表来支持具体的审批场景：

#### 2.2.1 审批业务表 (smart_approval_business)

```sql
CREATE TABLE smart_approval_business (
    business_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '业务ID',
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型：leave-请假，expense-报销，purchase-采购',
    business_code VARCHAR(100) NOT NULL COMMENT '业务编码',
    business_title VARCHAR(500) NOT NULL COMMENT '业务标题',
    initiator_id BIGINT NOT NULL COMMENT '发起人ID',
    initiator_name VARCHAR(100) NOT NULL COMMENT '发起人姓名',
    department_id BIGINT COMMENT '部门ID',
    department_name VARCHAR(100) COMMENT '部门名称',
    form_data JSON COMMENT '表单数据',
    attachment_ids TEXT COMMENT '附件ID列表',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-进行中，2-已完成，3-已拒绝，4-已撤回',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    remark TEXT COMMENT '备注',

    INDEX idx_instance_id (instance_id),
    INDEX idx_business_type (business_type),
    INDEX idx_business_code (business_code),
    INDEX idx_initiator_id (initiator_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    UNIQUE KEY uk_business_code (business_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批业务表';
```

#### 2.2.2 审批任务表 (smart_approval_task)

```sql
CREATE TABLE smart_approval_task (
    task_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    data_id BIGINT NOT NULL COMMENT '流程数据ID',
    node_code VARCHAR(100) NOT NULL COMMENT '节点编码',
    node_name VARCHAR(200) NOT NULL COMMENT '节点名称',
    task_name VARCHAR(500) NOT NULL COMMENT '任务名称',
    assignee_id BIGINT COMMENT '处理人ID',
    assignee_name VARCHAR(100) COMMENT '处理人姓名',
    assignee_type VARCHAR(50) DEFAULT 'user' COMMENT '处理人类型：user-用户，role-角色，dept-部门',
    candidate_users TEXT COMMENT '候选用户ID列表',
    candidate_roles TEXT COMMENT '候选角色列表',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待处理，2-已处理，3-已转办，4-已撤回',
    priority TINYINT DEFAULT 1 COMMENT '优先级：1-低，2-中，3-高，4-紧急',
    due_time DATETIME COMMENT '到期时间',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '处理耗时（秒）',
    form_data JSON COMMENT '表单数据',
    comment TEXT COMMENT '处理意见',
    action_result VARCHAR(50) COMMENT '处理结果：agree-同意，reject-拒绝，return-退回',

    INDEX idx_instance_id (instance_id),
    INDEX idx_data_id (data_id),
    INDEX idx_node_code (node_code),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_due_time (due_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批任务表';
```

#### 2.2.3 审批意见表 (smart_approval_comment)

```sql
CREATE TABLE smart_approval_comment (
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '意见ID',
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    task_id BIGINT COMMENT '任务ID',
    node_code VARCHAR(100) NOT NULL COMMENT '节点编码',
    node_name VARCHAR(200) NOT NULL COMMENT '节点名称',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) NOT NULL COMMENT '操作人姓名',
    action_type VARCHAR(50) NOT NULL COMMENT '操作类型：submit-提交，agree-同意，reject-拒绝，return-退回，delegate-转办',
    action_name VARCHAR(200) COMMENT '操作名称',
    comment TEXT COMMENT '审批意见',
    attachments JSON COMMENT '附件信息',
    operate_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',

    INDEX idx_instance_id (instance_id),
    INDEX idx_task_id (task_id),
    INDEX idx_node_code (node_code),
    INDEX idx_operator_id (operator_id),
    INDEX idx_action_type (action_type),
    INDEX idx_operate_time (operate_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批意见表';
```

#### 2.2.4 流程模板表 (smart_approval_template)

```sql
CREATE TABLE smart_approval_template (
    template_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_code VARCHAR(100) NOT NULL COMMENT '模板编码',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    definition_handler VARCHAR(500) NOT NULL COMMENT '流程处理器类名',
    form_config JSON COMMENT '表单配置',
    permission_config JSON COMMENT '权限配置',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统模板',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description TEXT COMMENT '描述',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_template_code (template_code),
    INDEX idx_business_type (business_type),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order),
    UNIQUE KEY uk_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程模板表';
```

## 3. Warm-Flow 集成实现

### 3.1 Maven 依赖配置

```xml
<!-- warm-flow 依赖 -->
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>warm-flow-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>

<!-- MyBatis-Plus 兼容性 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.12</version>
</dependency>
```

### 3.2 配置文件

```yaml
# application.yml
warm-flow:
  # 开启warm-flow
  enabled: true
  # 是否多租户
  tenant: false
  # 流程处理器包路径
  scanner-handler-packages: net.lab1024.sa.base.module.service.workflow.handler
  # 监听器包路径
  scanner-listener-packages: net.lab1024.sa.base.module.service.workflow.listener
  # 数据库配置
  database:
    # 数据库类型
    type: mysql
    # 是否自动创建表
    create-table: true
    # 是否初始化数据
    init-data: true
  # 缓存配置
  cache:
    # 缓存类型
    type: redis
    # 缓存前缀
    key-prefix: warm-flow
    # 缓存时间(秒)
    timeout: 3600
```

### 3.3 实体类设计

#### 3.3.1 审批业务实体

```java
package net.lab1024.sa.base.module.entity.workflow;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.entity.base.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("smart_approval_business")
public class ApprovalBusinessEntity extends BaseEntity {

    @TableId(value = "business_id", type = IdType.AUTO)
    private Long businessId;

    @TableField("instance_id")
    private Long instanceId;

    @TableField("business_type")
    private String businessType;

    @TableField("business_code")
    private String businessCode;

    @TableField("business_title")
    private String businessTitle;

    @TableField("initiator_id")
    private Long initiatorId;

    @TableField("initiator_name")
    private String initiatorName;

    @TableField("department_id")
    private Long departmentId;

    @TableField("department_name")
    private String departmentName;

    @TableField("form_data")
    private String formData;

    @TableField("attachment_ids")
    private String attachmentIds;

    @TableField("status")
    private Integer status;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("remark")
    private String remark;

    public enum Status {
        RUNNING(1, "进行中"),
        COMPLETED(2, "已完成"),
        REJECTED(3, "已拒绝"),
        WITHDRAWN(4, "已撤回");

        private final Integer value;
        private final String desc;

        Status(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }
}
```

#### 3.3.2 审批任务实体

```java
package net.lab1024.sa.base.module.entity.workflow;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.entity.base.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("smart_approval_task")
public class ApprovalTaskEntity extends BaseEntity {

    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    @TableField("instance_id")
    private Long instanceId;

    @TableField("data_id")
    private Long dataId;

    @TableField("node_code")
    private String nodeCode;

    @TableField("node_name")
    private String nodeName;

    @TableField("task_name")
    private String taskName;

    @TableField("assignee_id")
    private Long assigneeId;

    @TableField("assignee_name")
    private String assigneeName;

    @TableField("assignee_type")
    private String assigneeType;

    @TableField("candidate_users")
    private String candidateUsers;

    @TableField("candidate_roles")
    private String candidateRoles;

    @TableField("status")
    private Integer status;

    @TableField("priority")
    private Integer priority;

    @TableField("due_time")
    private LocalDateTime dueTime;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("duration")
    private Long duration;

    @TableField("form_data")
    private String formData;

    @TableField("comment")
    private String comment;

    @TableField("action_result")
    private String actionResult;

    public enum Status {
        PENDING(1, "待处理"),
        COMPLETED(2, "已处理"),
        DELEGATED(3, "已转办"),
        WITHDRAWN(4, "已撤回");

        private final Integer value;
        private final String desc;

        Status(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }
}
```

#### 3.3.3 审批意见实体

```java
package net.lab1024.sa.base.module.entity.workflow;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.entity.base.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("smart_approval_comment")
public class ApprovalCommentEntity extends BaseEntity {

    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    @TableField("instance_id")
    private Long instanceId;

    @TableField("task_id")
    private Long taskId;

    @TableField("node_code")
    private String nodeCode;

    @TableField("node_name")
    private String nodeName;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("action_type")
    private String actionType;

    @TableField("action_name")
    private String actionName;

    @TableField("comment")
    private String comment;

    @TableField("attachments")
    private String attachments;

    @TableField("operate_time")
    private LocalDateTime operateTime;

    public enum ActionType {
        SUBMIT("submit", "提交"),
        AGREE("agree", "同意"),
        REJECT("reject", "拒绝"),
        RETURN("return", "退回"),
        DELEGATE("delegate", "转办");

        private final String value;
        private final String desc;

        ActionType(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }
}
```

### 3.4 Warm-Flow 流程处理器

#### 3.4.1 请假流程处理器

```java
package net.lab1024.sa.base.module.service.workflow.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.handler.DataHandler;
import org.dromara.warm.flow.core.task.Task;
import org.dromara.warm.flow.orm.entity.Instance;
import org.dromara.warm.flow.orm.entity.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 请假流程处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveProcessHandler implements DataHandler {

    private final ApprovalBusinessService businessService;
    private final ApprovalTaskService taskService;
    private final ApprovalCommentService commentService;

    @Override
    public List<Task> handle(Instance instance, List<Data> dataList, Map<String, Object> variables) {
        List<Task> taskList = new ArrayList<>();

        try {
            // 获取流程变量
            String businessCode = (String) variables.get("businessCode");
            Long initiatorId = Long.valueOf(variables.get("initiatorId").toString());
            String leaveType = (String) variables.get("leaveType");
            Integer leaveDays = (Integer) variables.get("leaveDays");

            // 创建审批业务记录
            ApprovalBusinessEntity business = new ApprovalBusinessEntity();
            business.setInstanceId(instance.getId());
            business.setBusinessType("LEAVE");
            business.setBusinessCode(businessCode);
            business.setBusinessTitle("请假申请");
            business.setInitiatorId(initiatorId);
            business.setFormData(SmartStringUtil.toJsonString(variables));
            business.setStatus(ApprovalBusinessEntity.Status.RUNNING.getValue());
            businessService.save(business);

            // 根据请假天数确定审批节点
            if (leaveDays <= 1) {
                // 1天以内：部门经理审批
                taskList.add(createManagerTask(instance, business, variables));
            } else if (leaveDays <= 3) {
                // 1-3天：部门经理 + HR审批
                taskList.add(createManagerTask(instance, business, variables));
                taskList.add(createHrTask(instance, business, variables));
            } else {
                // 3天以上：部门经理 + HR + 总监审批
                taskList.add(createManagerTask(instance, business, variables));
                taskList.add(createHrTask(instance, business, variables));
                taskList.add(createDirectorTask(instance, business, variables));
            }

            log.info("请假流程处理器执行完成，生成{}个任务", taskList.size());

        } catch (Exception e) {
            log.error("请假流程处理器执行失败", e);
            throw new RuntimeException("请假流程处理失败", e);
        }

        return taskList;
    }

    /**
     * 创建部门经理审批任务
     */
    private Task createManagerTask(Instance instance, ApprovalBusinessEntity business, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("manager_approval");
        task.setNodeName("部门经理审批");
        task.setHandlerType(0); // 类处理器

        // 获取部门经理ID
        Long managerId = getManagerId(business.getInitiatorId());
        task.setHandler(managerId.toString());

        // 设置任务变量
        Map<String, Object> taskVariables = Map.of(
            "businessId", business.getBusinessId(),
            "businessCode", business.getBusinessCode(),
            "taskType", "manager_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    /**
     * 创建HR审批任务
     */
    private Task createHrTask(Instance instance, ApprovalBusinessEntity business, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("hr_approval");
        task.setNodeName("HR审批");
        task.setHandlerType(1); // 方法处理器
        task.setHandler("hrApprovalHandler");

        Map<String, Object> taskVariables = Map.of(
            "businessId", business.getBusinessId(),
            "businessCode", business.getBusinessCode(),
            "taskType", "hr_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    /**
     * 创建总监审批任务
     */
    private Task createDirectorTask(Instance instance, ApprovalBusinessEntity business, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("director_approval");
        task.setNodeName("总监审批");
        task.setHandlerType(0);
        task.setHandler(getDirectorId().toString());

        Map<String, Object> taskVariables = Map.of(
            "businessId", business.getBusinessId(),
            "businessCode", business.getBusinessCode(),
            "taskType", "director_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    /**
     * 获取部门经理ID
     */
    private Long getManagerId(Long userId) {
        // TODO: 根据用户ID获取其部门经理ID
        return 1001L; // 示例值
    }

    /**
     * 获取总监ID
     */
    private Long getDirectorId() {
        // TODO: 获取总监ID
        return 1002L; // 示例值
    }
}
```

#### 3.4.2 费用报销流程处理器

```java
package net.lab1024.sa.base.module.service.workflow.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.handler.DataHandler;
import org.dromara.warm.flow.core.task.Task;
import org.dromara.warm.flow.orm.entity.Instance;
import org.dromara.warm.flow.orm.entity.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 费用报销流程处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpenseProcessHandler implements DataHandler {

    private final ApprovalBusinessService businessService;
    private final ApprovalTaskService taskService;

    @Override
    public List<Task> handle(Instance instance, List<Data> dataList, Map<String, Object> variables) {
        List<Task> taskList = new ArrayList<>();

        try {
            String businessCode = (String) variables.get("businessCode");
            Long initiatorId = Long.valueOf(variables.get("initiatorId").toString());
            Double amount = Double.valueOf(variables.get("amount").toString());
            String expenseType = (String) variables.get("expenseType");

            // 创建审批业务记录
            ApprovalBusinessEntity business = new ApprovalBusinessEntity();
            business.setInstanceId(instance.getId());
            business.setBusinessType("EXPENSE");
            business.setBusinessCode(businessCode);
            business.setBusinessTitle("费用报销申请");
            business.setInitiatorId(initiatorId);
            business.setFormData(SmartStringUtil.toJsonString(variables));
            business.setStatus(ApprovalBusinessEntity.Status.RUNNING.getValue());
            businessService.save(business);

            // 根据金额确定审批流程
            if (amount <= 1000) {
                // 1000元以下：部门经理审批
                taskList.add(createManagerTask(instance, business, variables));
            } else if (amount <= 5000) {
                // 1000-5000元：部门经理 + 财务审批
                taskList.add(createManagerTask(instance, business, variables));
                taskList.add(createFinanceTask(instance, business, variables));
            } else {
                // 5000元以上：部门经理 + 财务 + 总经理审批
                taskList.add(createManagerTask(instance, business, variables));
                taskList.add(createFinanceTask(instance, business, variables));
                taskList.add(createGeneralManagerTask(instance, business, variables));
            }

            log.info("费用报销流程处理器执行完成，生成{}个任务", taskList.size());

        } catch (Exception e) {
            log.error("费用报销流程处理器执行失败", e);
            throw new RuntimeException("费用报销流程处理失败", e);
        }

        return taskList;
    }

    private Task createManagerTask(Instance instance, ApprovalBusinessEntity business, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("manager_approval");
        task.setNodeName("部门经理审批");
        task.setHandlerType(0);
        task.setHandler(getManagerId(business.getInitiatorId()).toString());

        Map<String, Object> taskVariables = Map.of(
            "businessId", business.getBusinessId(),
            "businessCode", business.getBusinessCode(),
            "taskType", "manager_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    private Task createFinanceTask(Instance instance, ApprovalBusinessEntity business, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("finance_approval");
        task.setNodeName("财务审批");
        task.setHandlerType(1);
        task.setHandler("financeApprovalHandler");

        Map<String, Object> taskVariables = Map.of(
            "businessId", business.getBusinessId(),
            "businessCode", business.getBusinessCode(),
            "taskType", "finance_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    private Task createGeneralManagerTask(Instance instance, ApprovalBusinessEntity business, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("gm_approval");
        task.setNodeName("总经理审批");
        task.setHandlerType(0);
        task.setHandler(getGeneralManagerId().toString());

        Map<String, Object> taskVariables = Map.of(
            "businessId", business.getBusinessId(),
            "businessCode", business.getBusinessCode(),
            "taskType", "gm_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    private Long getManagerId(Long userId) {
        // TODO: 实现获取部门经理逻辑
        return 1001L;
    }

    private Long getGeneralManagerId() {
        // TODO: 获取总经理ID
        return 1003L;
    }
}
```

### 3.5 流程服务层

#### 3.5.1 审批流程服务

```java
package net.lab1024.sa.base.module.service.workflow;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import net.lab1024.sa.base.module.entity.workflow.ApprovalBusinessEntity;
import net.lab1024.sa.base.module.mapper.workflow.ApprovalBusinessMapper;
import net.lab1024.sa.base.module.service.workflow.dto.*;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.FlowParams;
import org.dromara.warm.flow.core.dto.NextNode;
import org.dromara.warm.flow.orm.entity.Instance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalBusinessService extends ServiceImpl<ApprovalBusinessMapper, ApprovalBusinessEntity> {

    private final FlowEngine flowEngine;
    private final ApprovalTaskService taskService;
    private final ApprovalCommentService commentService;
    private final UserService userService;

    /**
     * 启动审批流程
     */
    @Transactional(rollbackFor = Exception.class)
    public ApprovalBusinessVO startApproval(ApprovalStartDTO startDTO, Long userId) {
        try {
            // 1. 获取用户信息
            UserDTO user = userService.getUserById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 2. 构建流程参数
            Map<String, Object> variables = new HashMap<>();
            variables.put("businessCode", startDTO.getBusinessCode());
            variables.put("initiatorId", userId);
            variables.put("initiatorName", user.getUserName());
            variables.put("departmentId", user.getDepartmentId());
            variables.putAll(startDTO.getVariables());

            // 3. 设置流程启动参数
            FlowParams flowParams = FlowParams.builder()
                    .handler(getHandlerByBusinessType(startDTO.getBusinessType()))
                    .variables(variables)
                    .build();

            // 4. 启动流程
            Instance instance = flowEngine.start(flowParams);
            if (instance == null) {
                throw new BusinessException("流程启动失败");
            }

            // 5. 创建业务记录
            ApprovalBusinessEntity business = new ApprovalBusinessEntity();
            business.setInstanceId(instance.getId());
            business.setBusinessType(startDTO.getBusinessType());
            business.setBusinessCode(startDTO.getBusinessCode());
            business.setBusinessTitle(startDTO.getBusinessTitle());
            business.setInitiatorId(userId);
            business.setInitiatorName(user.getUserName());
            business.setDepartmentId(user.getDepartmentId());
            business.setDepartmentName(user.getDepartmentName());
            business.setFormData(SmartStringUtil.toJsonString(startDTO.getFormData()));
            business.setAttachmentIds(String.join(",", startDTO.getAttachmentIds()));
            business.setStatus(ApprovalBusinessEntity.Status.RUNNING.getValue());
            business.setRemark(startDTO.getRemark());
            this.save(business);

            // 6. 创建提交记录
            commentService.createComment(instance.getId(), null, "start", "提交申请",
                    startDTO.getRemark(), userId, user.getUserName());

            log.info("启动审批流程成功：{} - {}", startDTO.getBusinessCode(), startDTO.getBusinessTitle());

            return convertToVO(business);

        } catch (Exception e) {
            log.error("启动审批流程失败", e);
            throw new BusinessException("启动审批流程失败：" + e.getMessage());
        }
    }

    /**
     * 处理审批任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleApproval(ApprovalHandleDTO handleDTO, Long userId) {
        try {
            // 1. 获取任务信息
            ApprovalTaskEntity task = taskService.getById(handleDTO.getTaskId());
            if (task == null) {
                throw new BusinessException("任务不存在");
            }

            if (!task.getAssigneeId().equals(userId)) {
                throw new BusinessException("无权限处理此任务");
            }

            // 2. 获取业务信息
            ApprovalBusinessEntity business = this.getById(task.getBusinessId());
            if (business == null) {
                throw new BusinessException("业务记录不存在");
            }

            // 3. 获取用户信息
            UserDTO user = userService.getUserById(userId);

            // 4. 构建处理变量
            Map<String, Object> variables = new HashMap<>();
            variables.put("action", handleDTO.getAction());
            variables.put("comment", handleDTO.getComment());
            variables.put("operatorId", userId);
            variables.put("operatorName", user.getUserName());
            if (handleDTO.getVariables() != null) {
                variables.putAll(handleDTO.getVariables());
            }

            // 5. 执行流程处理
            boolean success = false;
            String actionName = "";

            switch (handleDTO.getAction()) {
                case "agree":
                    success = flowEngine.agree(task.getDataId(), variables);
                    actionName = "同意";
                    break;
                case "reject":
                    success = flowEngine.reject(task.getDataId(), variables);
                    actionName = "拒绝";
                    break;
                case "return":
                    success = flowEngine.returnTask(task.getDataId(), variables);
                    actionName = "退回";
                    break;
                default:
                    throw new BusinessException("不支持的操作类型");
            }

            if (!success) {
                throw new BusinessException("任务处理失败");
            }

            // 6. 更新任务状态
            task.setStatus(ApprovalTaskEntity.Status.COMPLETED.getValue());
            task.setActionResult(handleDTO.getAction());
            task.setComment(handleDTO.getComment());
            task.setEndTime(LocalDateTime.now());
            task.setDuration(java.time.Duration.between(task.getStartTime(), task.getEndTime()).getSeconds());
            taskService.updateById(task);

            // 7. 创建审批记录
            commentService.createComment(business.getInstanceId(), task.getTaskId(),
                    handleDTO.getAction(), actionName, handleDTO.getComment(),
                    userId, user.getUserName());

            // 8. 检查流程是否结束
            checkProcessCompletion(business.getInstanceId());

            log.info("处理审批任务成功：{} - {}", task.getTaskId(), actionName);

        } catch (Exception e) {
            log.error("处理审批任务失败", e);
            throw new BusinessException("处理审批任务失败：" + e.getMessage());
        }
    }

    /**
     * 撤回审批
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdrawApproval(Long businessId, Long userId) {
        try {
            ApprovalBusinessEntity business = this.getById(businessId);
            if (business == null) {
                throw new BusinessException("业务记录不存在");
            }

            if (!business.getInitiatorId().equals(userId)) {
                throw new BusinessException("只有发起人可以撤回");
            }

            if (business.getStatus() != ApprovalBusinessEntity.Status.RUNNING.getValue()) {
                throw new BusinessException("当前状态不允许撤回");
            }

            // 检查是否可以撤回
            List<ApprovalTaskEntity> activeTasks = taskService.getActiveTasks(business.getInstanceId());
            boolean canWithdraw = activeTasks.stream()
                    .anyMatch(task -> task.getAssigneeId() != null && task.getAssigneeId().equals(userId));

            if (!canWithdraw && !activeTasks.isEmpty()) {
                throw new BusinessException("流程已进入审批环节，无法撤回");
            }

            // 终止流程
            boolean success = flowEngine.terminate(business.getInstanceId());
            if (!success) {
                throw new BusinessException("撤回失败");
            }

            // 更新业务状态
            business.setStatus(ApprovalBusinessEntity.Status.WITHDRAWN.getValue());
            business.setEndTime(LocalDateTime.now());
            this.updateById(business);

            // 终止所有活动任务
            for (ApprovalTaskEntity task : activeTasks) {
                task.setStatus(ApprovalTaskEntity.Status.WITHDRAWN.getValue());
                task.setEndTime(LocalDateTime.now());
                taskService.updateById(task);
            }

            // 创建撤回记录
            UserDTO user = userService.getUserById(userId);
            commentService.createComment(business.getInstanceId(), null, "withdraw", "撤回申请",
                    "用户主动撤回", userId, user.getUserName());

            log.info("撤回审批成功：{} - {}", businessId, business.getBusinessCode());

        } catch (Exception e) {
            log.error("撤回审批失败", e);
            throw new BusinessException("撤回审批失败：" + e.getMessage());
        }
    }

    /**
     * 转办任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(ApprovalDelegateDTO delegateDTO, Long userId) {
        try {
            ApprovalTaskEntity task = taskService.getById(delegateDTO.getTaskId());
            if (task == null) {
                throw new BusinessException("任务不存在");
            }

            if (!task.getAssigneeId().equals(userId)) {
                throw new BusinessException("无权限转办此任务");
            }

            // 获取被转办人信息
            UserDTO toUser = userService.getUserById(delegateDTO.getToUserId());
            if (toUser == null) {
                throw new BusinessException("被转办人不存在");
            }

            // 更新任务处理人
            task.setAssigneeId(delegateDTO.getToUserId());
            task.setAssigneeName(toUser.getUserName());
            task.setStatus(ApprovalTaskEntity.Status.DELEGATED.getValue());
            task.setComment(task.getComment() + "\n转办给：" + toUser.getUserName());
            taskService.updateById(task);

            // 创建转办记录
            ApprovalBusinessEntity business = this.getById(task.getBusinessId());
            UserDTO fromUser = userService.getUserById(userId);
            commentService.createComment(business.getInstanceId(), task.getTaskId(), "delegate", "转办任务",
                    String.format("转办给：%s，原因：%s", toUser.getUserName(), delegateDTO.getReason()),
                    userId, fromUser.getUserName());

            log.info("转办任务成功：{} -> {}", task.getTaskId(), toUser.getUserName());

        } catch (Exception e) {
            log.error("转办任务失败", e);
            throw new BusinessException("转办任务失败：" + e.getMessage());
        }
    }

    /**
     * 根据业务类型获取处理器
     */
    private String getHandlerByBusinessType(String businessType) {
        switch (businessType) {
            case "LEAVE":
                return "leaveProcessHandler";
            case "EXPENSE":
                return "expenseProcessHandler";
            case "PURCHASE":
                return "purchaseProcessHandler";
            default:
                throw new BusinessException("不支持的业务类型：" + businessType);
        }
    }

    /**
     * 检查流程完成状态
     */
    private void checkProcessCompletion(Long instanceId) {
        try {
            // 获取流程实例状态
            Instance instance = flowEngine.queryInstance(instanceId);
            if (instance == null) {
                return;
            }

            // 更新业务状态
            List<ApprovalBusinessEntity> businessList = this.lambdaQuery()
                    .eq(ApprovalBusinessEntity::getInstanceId, instanceId)
                    .list();

            for (ApprovalBusinessEntity business : businessList) {
                if (instance.getState() == 2) { // 已完成
                    business.setStatus(ApprovalBusinessEntity.Status.COMPLETED.getValue());
                    business.setEndTime(LocalDateTime.now());
                } else if (instance.getState() == 3) { // 已拒绝
                    business.setStatus(ApprovalBusinessEntity.Status.REJECTED.getValue());
                    business.setEndTime(LocalDateTime.now());
                }
                this.updateById(business);
            }

        } catch (Exception e) {
            log.error("检查流程完成状态失败", e);
        }
    }

    /**
     * 转换为VO
     */
    private ApprovalBusinessVO convertToVO(ApprovalBusinessEntity entity) {
        ApprovalBusinessVO vo = new ApprovalBusinessVO();
        SmartBeanUtil.copyProperties(entity, vo);
        return vo;
    }
}
```

#### 3.5.2 审批任务服务

```java
package net.lab1024.sa.base.module.service.workflow;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.module.entity.workflow.ApprovalTaskEntity;
import net.lab1024.sa.base.module.mapper.workflow.ApprovalTaskMapper;
import net.lab1024.sa.base.module.service.workflow.dto.ApprovalTaskQueryDTO;
import net.lab1024.sa.base.module.service.workflow.dto.ApprovalTaskVO;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.orm.entity.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalTaskService extends ServiceImpl<ApprovalTaskMapper, ApprovalTaskEntity> {

    private final FlowEngine flowEngine;
    private final ApprovalBusinessService businessService;
    private final UserService userService;

    /**
     * 获取我的待办任务
     */
    public PageResult<ApprovalTaskVO> getMyPendingTasks(ApprovalTaskQueryDTO queryDTO, Long userId) {
        // 查询warm-flow的待办任务
        List<Data> dataList = flowEngine.queryUserTask(userId);

        // 转换为审批任务并分页
        List<ApprovalTaskVO> taskList = dataList.stream()
                .map(data -> convertToTaskVO(data))
                .collect(Collectors.toList());

        // 应用查询条件
        if (SmartStringUtil.isNotEmpty(queryDTO.getBusinessType())) {
            taskList = taskList.stream()
                    .filter(task -> queryDTO.getBusinessType().equals(task.getBusinessType()))
                    .collect(Collectors.toList());
        }

        if (SmartStringUtil.isNotEmpty(queryDTO.getTaskName())) {
            taskList = taskList.stream()
                    .filter(task -> task.getTaskName().contains(queryDTO.getTaskName()))
                    .collect(Collectors.toList());
        }

        // 分页处理
        int total = taskList.size();
        int start = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        int end = Math.min(start + queryDTO.getPageSize(), total);

        List<ApprovalTaskVO> pageList = taskList.subList(start, end);

        return PageResult.of(pageList, total);
    }

    /**
     * 获取我的已办任务
     */
    public PageResult<ApprovalTaskVO> getMyCompletedTasks(ApprovalTaskQueryDTO queryDTO, Long userId) {
        List<ApprovalTaskEntity> taskList = this.lambdaQuery()
                .eq(ApprovalTaskEntity::getAssigneeId, userId)
                .eq(ApprovalTaskEntity::getStatus, ApprovalTaskEntity.Status.COMPLETED.getValue())
                .orderByDesc(ApprovalTaskEntity::getEndTime)
                .list();

        // 应用查询条件
        if (SmartStringUtil.isNotEmpty(queryDTO.getBusinessType())) {
            taskList = taskList.stream()
                    .filter(task -> queryDTO.getBusinessType().equals(task.getBusinessType()))
                    .collect(Collectors.toList());
        }

        // 转换为VO
        List<ApprovalTaskVO> voList = taskList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 分页处理
        int total = voList.size();
        int start = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        int end = Math.min(start + queryDTO.getPageSize(), total);

        List<ApprovalTaskVO> pageList = voList.subList(start, end);

        return PageResult.of(pageList, total);
    }

    /**
     * 获取活动任务
     */
    public List<ApprovalTaskEntity> getActiveTasks(Long instanceId) {
        return this.lambdaQuery()
                .eq(ApprovalTaskEntity::getInstanceId, instanceId)
                .in(ApprovalTaskEntity::getStatus,
                    ApprovalTaskEntity.Status.PENDING.getValue(),
                    ApprovalTaskEntity.Status.DELEGATED.getValue())
                .list();
    }

    /**
     * 转换warm-flow数据为任务VO
     */
    private ApprovalTaskVO convertToTaskVO(Data data) {
        ApprovalTaskVO vo = new ApprovalTaskVO();
        vo.setDataId(data.getId());
        vo.setInstanceId(data.getInstanceId());
        vo.setNodeCode(data.getNodeCode());
        vo.setNodeName(data.getNodeName());
        vo.setTaskName(data.getNodeName());
        vo.setAssigneeId(Long.valueOf(data.getHandler()));
        vo.setStartTime(data.getCreateTime());
        vo.setNodeState(data.getNodeState());

        // 获取业务信息
        ApprovalBusinessEntity business = businessService.lambdaQuery()
                .eq(ApprovalBusinessEntity::getInstanceId, data.getInstanceId())
                .one();

        if (business != null) {
            vo.setBusinessId(business.getBusinessId());
            vo.setBusinessType(business.getBusinessType());
            vo.setBusinessCode(business.getBusinessCode());
            vo.setBusinessTitle(business.getBusinessTitle());
            vo.setInitiatorId(business.getInitiatorId());
            vo.setInitiatorName(business.getInitiatorName());
        }

        // 获取处理人信息
        if (vo.getAssigneeId() != null) {
            UserDTO user = userService.getUserById(vo.getAssigneeId());
            if (user != null) {
                vo.setAssigneeName(user.getUserName());
            }
        }

        // 解析任务变量
        if (SmartStringUtil.isNotEmpty(data.getVariables())) {
            try {
                Map<String, Object> variables = SmartStringUtil.parseObject(data.getVariables(), Map.class);
                vo.setVariables(variables);
            } catch (Exception e) {
                log.warn("解析任务变量失败", e);
            }
        }

        return vo;
    }

    /**
     * 转换为VO
     */
    private ApprovalTaskVO convertToVO(ApprovalTaskEntity entity) {
        ApprovalTaskVO vo = new ApprovalTaskVO();
        SmartBeanUtil.copyProperties(entity, vo);

        // 获取业务信息
        ApprovalBusinessEntity business = businessService.getById(entity.getBusinessId());
        if (business != null) {
            vo.setBusinessType(business.getBusinessType());
            vo.setBusinessCode(business.getBusinessCode());
            vo.setBusinessTitle(business.getBusinessTitle());
            vo.setInitiatorId(business.getInitiatorId());
            vo.setInitiatorName(business.getInitiatorName());
        }

        // 获取处理人信息
        if (entity.getAssigneeId() != null) {
            UserDTO user = userService.getUserById(entity.getAssigneeId());
            if (user != null) {
                vo.setAssigneeName(user.getUserName());
            }
        }

        return vo;
    }
}
```

### 3.3 控制器层

#### 3.3.1 流程定义控制器

```java
package net.lab1024.sa.base.module.controller.workflow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.ResponseResult;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.module.service.workflow.WorkflowDefinitionService;
import net.lab1024.sa.base.module.service.workflow.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "流程定义", description = "流程定义管理接口")
@RequestMapping("/api/workflow/definition")
public class WorkflowDefinitionController extends SupportBaseController {

    private final WorkflowDefinitionService definitionService;

    @Operation(summary = "创建流程定义")
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('workflow:definition:create')")
    public ResponseResult<Long> createDefinition(@RequestBody @Valid WorkflowDefinitionCreateDTO createDTO) {
        Long userId = LoginUtil.getLoginUserId();
        Long definitionId = definitionService.createDefinition(createDTO, userId);
        return ResponseResult.ok(definitionId);
    }

    @Operation(summary = "更新流程定义")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('workflow:definition:update')")
    public ResponseResult<Void> updateDefinition(@RequestBody @Valid WorkflowDefinitionUpdateDTO updateDTO) {
        Long userId = LoginUtil.getLoginUserId();
        definitionService.updateDefinition(updateDTO.getDefinitionId(), updateDTO, userId);
        return ResponseResult.ok();
    }

    @Operation(summary = "发布流程定义")
    @PostMapping("/{definitionId}/publish")
    @PreAuthorize("hasAuthority('workflow:definition:publish')")
    public ResponseResult<Void> publishDefinition(@PathVariable Long definitionId) {
        Long userId = LoginUtil.getLoginUserId();
        definitionService.publishDefinition(definitionId, userId);
        return ResponseResult.ok();
    }

    @Operation(summary = "禁用流程定义")
    @PostMapping("/{definitionId}/disable")
    @PreAuthorize("hasAuthority('workflow:definition:disable')")
    public ResponseResult<Void> disableDefinition(@PathVariable Long definitionId) {
        Long userId = LoginUtil.getLoginUserId();
        definitionService.disableDefinition(definitionId, userId);
        return ResponseResult.ok();
    }

    @Operation(summary = "获取流程定义详情")
    @GetMapping("/{definitionId}")
    @PreAuthorize("hasAuthority('workflow:definition:query')")
    public ResponseResult<WorkflowDefinitionDetailDTO> getDefinitionDetail(@PathVariable Long definitionId) {
        WorkflowDefinitionDetailDTO detail = definitionService.getDefinitionDetail(definitionId);
        return ResponseResult.ok(detail);
    }

    @Operation(summary = "获取流程定义列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('workflow:definition:query')")
    public ResponseResult<PageResult<WorkflowDefinitionVO>> getDefinitionPage(WorkflowDefinitionQueryDTO queryDTO) {
        PageResult<WorkflowDefinitionVO> pageResult = definitionService.getDefinitionPage(queryDTO);
        return ResponseResult.ok(pageResult);
    }

    @Operation(summary = "删除流程定义")
    @DeleteMapping("/{definitionId}")
    @PreAuthorize("hasAuthority('workflow:definition:delete')")
    public ResponseResult<Void> deleteDefinition(@PathVariable Long definitionId) {
        // TODO: 实现删除逻辑（需要检查是否有运行中的实例）
        return ResponseResult.ok();
    }

    @Operation(summary = "复制流程定义")
    @PostMapping("/{definitionId}/copy")
    @PreAuthorize("hasAuthority('workflow:definition:create')")
    public ResponseResult<Long> copyDefinition(@PathVariable Long definitionId,
                                              @RequestBody @Valid WorkflowDefinitionCopyDTO copyDTO) {
        Long userId = LoginUtil.getLoginUserId();
        // TODO: 实现复制逻辑
        return ResponseResult.ok();
    }

    @Operation(summary = "获取流程分类列表")
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('workflow:definition:query')")
    public ResponseResult<List<WorkflowCategoryVO>> getCategories() {
        // TODO: 获取分类列表
        return ResponseResult.ok();
    }

    @Operation(summary = "验证流程配置")
    @PostMapping("/validate")
    @PreAuthorize("hasAuthority('workflow:definition:create')")
    public ResponseResult<WorkflowValidateResult> validateDefinition(
            @RequestBody @Valid WorkflowDefinitionValidateDTO validateDTO) {
        // TODO: 实现验证逻辑
        return ResponseResult.ok();
    }
}
```

#### 3.3.2 流程实例控制器

```java
package net.lab1024.sa.base.module.controller.workflow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.controller.SupportBaseController;
import net.lab1024.sa.base.common.domain.ResponseResult;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.module.service.workflow.WorkflowInstanceService;
import net.lab1024.sa.base.module.service.workflow.dto.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "流程实例", description = "流程实例管理接口")
@RequestMapping("/api/workflow/instance")
public class WorkflowInstanceController extends SupportBaseController {

    private final WorkflowInstanceService instanceService;

    @Operation(summary = "启动流程实例")
    @PostMapping("/start")
    @PreAuthorize("hasAuthority('workflow:instance:start')")
    public ResponseResult<WorkflowInstanceVO> startProcess(@RequestBody @Valid WorkflowStartDTO startDTO) {
        Long userId = LoginUtil.getLoginUserId();
        WorkflowInstanceVO instance = instanceService.startProcess(startDTO, userId);
        return ResponseResult.ok(instance);
    }

    @Operation(summary = "完成任务")
    @PostMapping("/task/complete")
    @PreAuthorize("hasAuthority('workflow:task:complete')")
    public ResponseResult<Void> completeTask(@RequestBody @Valid WorkflowCompleteDTO completeDTO) {
        Long userId = LoginUtil.getLoginUserId();
        instanceService.completeTask(completeDTO, userId);
        return ResponseResult.ok();
    }

    @Operation(summary = "拒绝任务")
    @PostMapping("/task/reject")
    @PreAuthorize("hasAuthority('workflow:task:reject')")
    public ResponseResult<Void> rejectTask(@RequestBody @Valid WorkflowRejectDTO rejectDTO) {
        Long userId = LoginUtil.getLoginUserId();
        instanceService.rejectTask(rejectDTO, userId);
        return ResponseResult.ok();
    }

    @Operation(summary = "撤回流程")
    @PostMapping("/{instanceId}/withdraw")
    @PreAuthorize("hasAuthority('workflow:instance:withdraw')")
    public ResponseResult<Void> withdrawProcess(@PathVariable Long instanceId) {
        Long userId = LoginUtil.getLoginUserId();
        instanceService.withdrawProcess(instanceId, userId);
        return ResponseResult.ok();
    }

    @Operation(summary = "转办任务")
    @PostMapping("/task/delegate")
    @PreAuthorize("hasAuthority('workflow:task:delegate')")
    public ResponseResult<Void> delegateTask(@RequestBody @Valid WorkflowDelegateDTO delegateDTO) {
        Long userId = LoginUtil.getLoginUserId();
        instanceService.delegateTask(delegateDTO, userId);
        return ResponseResult.ok();
    }

    @Operation(summary = "获取我的待办任务")
    @GetMapping("/tasks/todo")
    @PreAuthorize("hasAuthority('workflow:task:query')")
    public ResponseResult<PageResult<WorkflowTaskVO>> getTodoTasks(WorkflowTaskQueryDTO queryDTO) {
        Long userId = LoginUtil.getLoginUserId();
        queryDTO.setAssigneeId(userId);
        PageResult<WorkflowTaskVO> pageResult = taskService.getTaskPage(queryDTO);
        return ResponseResult.ok(pageResult);
    }

    @Operation(summary = "获取我的已办任务")
    @GetMapping("/tasks/done")
    @PreAuthorize("hasAuthority('workflow:task:query')")
    public ResponseResult<PageResult<WorkflowTaskVO>> getDoneTasks(WorkflowTaskQueryDTO queryDTO) {
        Long userId = LoginUtil.getLoginUserId();
        queryDTO.setAssigneeId(userId);
        queryDTO.setStatus(WorkflowTaskEntity.Status.COMPLETED.getValue());
        PageResult<WorkflowTaskVO> pageResult = taskService.getTaskPage(queryDTO);
        return ResponseResult.ok(pageResult);
    }

    @Operation(summary = "获取我发起的流程")
    @GetMapping("/my/processes")
    @PreAuthorize("hasAuthority('workflow:instance:query')")
    public ResponseResult<PageResult<WorkflowInstanceVO>> getMyProcesses(WorkflowInstanceQueryDTO queryDTO) {
        Long userId = LoginUtil.getLoginUserId();
        queryDTO.setInitiatorId(userId);
        PageResult<WorkflowInstanceVO> pageResult = instanceService.getInstancePage(queryDTO);
        return ResponseResult.ok(pageResult);
    }

    @Operation(summary = "获取流程实例详情")
    @GetMapping("/{instanceId}")
    @PreAuthorize("hasAuthority('workflow:instance:query')")
    public ResponseResult<WorkflowInstanceDetailDTO> getInstanceDetail(@PathVariable Long instanceId) {
        WorkflowInstanceDetailDTO detail = instanceService.getInstanceDetail(instanceId);
        return ResponseResult.ok(detail);
    }

    @Operation(summary = "获取流程图")
    @GetMapping("/{instanceId}/diagram")
    @PreAuthorize("hasAuthority('workflow:instance:query')")
    public ResponseResult<WorkflowDiagramDTO> getProcessDiagram(@PathVariable Long instanceId) {
        WorkflowDiagramDTO diagram = instanceService.getProcessDiagram(instanceId);
        return ResponseResult.ok(diagram);
    }

    @Operation(summary = "获取流程记录")
    @GetMapping("/{instanceId}/records")
    @PreAuthorize("hasAuthority('workflow:instance:query')")
    public ResponseResult<List<WorkflowRecordVO>> getProcessRecords(@PathVariable Long instanceId) {
        List<WorkflowRecordVO> records = recordService.getRecordsByInstanceId(instanceId);
        return ResponseResult.ok(records);
    }
}
```

## 4. 前端实现

### 4.1 API 接口定义

#### 4.1.1 流程定义 API

```javascript
// src/api/workflow/definition.js
import request from '@/utils/request';

// 创建流程定义
export function createDefinition(data) {
  return request({
    url: '/workflow/definition/create',
    method: 'post',
    data
  });
}

// 更新流程定义
export function updateDefinition(data) {
  return request({
    url: '/workflow/definition/update',
    method: 'post',
    data
  });
}

// 发布流程定义
export function publishDefinition(definitionId) {
  return request({
    url: `/workflow/definition/${definitionId}/publish`,
    method: 'post'
  });
}

// 禁用流程定义
export function disableDefinition(definitionId) {
  return request({
    url: `/workflow/definition/${definitionId}/disable`,
    method: 'post'
  });
}

// 获取流程定义详情
export function getDefinitionDetail(definitionId) {
  return request({
    url: `/workflow/definition/${definitionId}`,
    method: 'get'
  });
}

// 获取流程定义列表
export function getDefinitionPage(params) {
  return request({
    url: '/workflow/definition/page',
    method: 'get',
    params
  });
}

// 删除流程定义
export function deleteDefinition(definitionId) {
  return request({
    url: `/workflow/definition/${definitionId}`,
    method: 'delete'
  });
}

// 复制流程定义
export function copyDefinition(definitionId, data) {
  return request({
    url: `/workflow/definition/${definitionId}/copy`,
    method: 'post',
    data
  });
}

// 获取流程分类
export function getCategories() {
  return request({
    url: '/workflow/definition/categories',
    method: 'get'
  });
}

// 验证流程配置
export function validateDefinition(data) {
  return request({
    url: '/workflow/definition/validate',
    method: 'post',
    data
  });
}
```

#### 4.1.2 流程实例 API

```javascript
// src/api/workflow/instance.js
import request from '@/utils/request';

// 启动流程实例
export function startProcess(data) {
  return request({
    url: '/workflow/instance/start',
    method: 'post',
    data
  });
}

// 完成任务
export function completeTask(data) {
  return request({
    url: '/workflow/instance/task/complete',
    method: 'post',
    data
  });
}

// 拒绝任务
export function rejectTask(data) {
  return request({
    url: '/workflow/instance/task/reject',
    method: 'post',
    data
  });
}

// 撤回流程
export function withdrawProcess(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}/withdraw`,
    method: 'post'
  });
}

// 转办任务
export function delegateTask(data) {
  return request({
    url: '/workflow/instance/task/delegate',
    method: 'post',
    data
  });
}

// 获取我的待办任务
export function getTodoTasks(params) {
  return request({
    url: '/workflow/instance/tasks/todo',
    method: 'get',
    params
  });
}

// 获取我的已办任务
export function getDoneTasks(params) {
  return request({
    url: '/workflow/instance/tasks/done',
    method: 'get',
    params
  });
}

// 获取我发起的流程
export function getMyProcesses(params) {
  return request({
    url: '/workflow/instance/my/processes',
    method: 'get',
    params
  });
}

// 获取流程实例详情
export function getInstanceDetail(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}`,
    method: 'get'
  });
}

// 获取流程图
export function getProcessDiagram(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}/diagram`,
    method: 'get'
  });
}

// 获取流程记录
export function getProcessRecords(instanceId) {
  return request({
    url: `/workflow/instance/${instanceId}/records`,
    method: 'get'
  });
}
```

### 4.2 流程设计器组件

#### 4.2.1 流程设计器主组件

```vue
<!-- src/views/workflow/designer/WorkflowDesigner.vue -->
<template>
  <div class="workflow-designer">
    <!-- 工具栏 -->
    <div class="designer-toolbar">
      <a-space>
        <a-button type="primary" @click="saveDefinition" :loading="saving">
          保存
        </a-button>
        <a-button @click="publishDefinition" :disabled="!canPublish">
          发布
        </a-button>
        <a-button @click="previewProcess">
          预览
        </a-button>
        <a-button @click="validateFlow">
          验证
        </a-button>
      </a-space>

      <a-space class="right-toolbar">
        <a-button @click="zoomIn">
          <template #icon><ZoomInOutlined /></template>
        </a-button>
        <a-button @click="zoomOut">
          <template #icon><ZoomOutOutlined /></template>
        </a-button>
        <a-button @click="resetZoom">
          <template #icon><UndoOutlined /></template>
        </a-button>
      </a-space>
    </div>

    <!-- 主内容区 -->
    <div class="designer-content">
      <!-- 左侧节点面板 -->
      <div class="node-panel">
        <NodePanel
          @node-drag-start="handleNodeDragStart"
          @node-select="handleNodeSelect"
        />
      </div>

      <!-- 中间画布区域 -->
      <div class="canvas-container">
        <WorkflowCanvas
          ref="canvasRef"
          :definition="definition"
          :zoom="zoom"
          @node-add="handleNodeAdd"
          @node-update="handleNodeUpdate"
          @node-delete="handleNodeDelete"
          @edge-add="handleEdgeAdd"
          @edge-update="handleEdgeUpdate"
          @edge-delete="handleEdgeDelete"
          @node-select="handleCanvasNodeSelect"
        />
      </div>

      <!-- 右侧属性面板 -->
      <div class="property-panel">
        <PropertyPanel
          v-if="selectedElement"
          :element="selectedElement"
          :definition="definition"
          @property-change="handlePropertyChange"
        />
      </div>
    </div>

    <!-- 流程信息弹窗 -->
    <WorkflowInfoModal
      v-model:visible="infoModalVisible"
      :definition="definition"
      @ok="handleInfoSave"
    />

    <!-- 预览弹窗 -->
    <ProcessPreviewModal
      v-model:visible="previewModalVisible"
      :definition="definition"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  ZoomInOutlined,
  ZoomOutOutlined,
  UndoOutlined
} from '@ant-design/icons-vue';
import WorkflowCanvas from './components/WorkflowCanvas.vue';
import NodePanel from './components/NodePanel.vue';
import PropertyPanel from './components/PropertyPanel.vue';
import WorkflowInfoModal from './components/WorkflowInfoModal.vue';
import ProcessPreviewModal from './components/ProcessPreviewModal.vue';
import { createDefinition, updateDefinition, publishDefinition, validateDefinition } from '@/api/workflow/definition';

// 组件引用
const canvasRef = ref(null);

// 响应式数据
const saving = ref(false);
const zoom = ref(1);
const infoModalVisible = ref(false);
const previewModalVisible = ref(false);

// 流程定义数据
const definition = reactive({
  definitionId: null,
  definitionKey: '',
  definitionName: '',
  definitionDesc: '',
  categoryId: null,
  version: 1,
  status: 1,
  formConfig: '{}',
  flowConfig: '{}',
  nodeConfig: '{}',
  startNodeId: '',
  endNodeId: '',
  nodes: [],
  edges: []
});

// 选中的元素
const selectedElement = ref(null);

// 计算属性
const canPublish = computed(() => {
  return definition.definitionId &&
         definition.status === 3 && // 草稿状态
         definition.nodes.length > 0 &&
         validateFlowStructure();
});

// 方法
const saveDefinition = async () => {
  if (!validateDefinition()) {
    return;
  }

  saving.value = true;
  try {
    const data = {
      ...definition,
      nodes: definition.nodes,
      edges: definition.edges
    };

    let result;
    if (definition.definitionId) {
      result = await updateDefinition(data);
    } else {
      result = await createDefinition(data);
      definition.definitionId = result.data;
    }

    message.success('保存成功');
  } catch (error) {
    message.error('保存失败：' + error.message);
  } finally {
    saving.value = false;
  }
};

const publishDefinition = async () => {
  Modal.confirm({
    title: '确认发布',
    content: '发布后将不能修改流程结构，是否确认发布？',
    onOk: async () => {
      try {
        await publishDefinition(definition.definitionId);
        definition.status = 1; // 启用状态
        message.success('发布成功');
      } catch (error) {
        message.error('发布失败：' + error.message);
      }
    }
  });
};

const previewProcess = () => {
  previewModalVisible.value = true;
};

const validateFlow = async () => {
  try {
    const result = await validateDefinition({
      nodes: definition.nodes,
      edges: definition.edges
    });

    if (result.data.valid) {
      message.success('流程配置正确');
    } else {
      message.error('流程配置错误：' + result.data.message);
    }
  } catch (error) {
    message.error('验证失败：' + error.message);
  }
};

const zoomIn = () => {
  zoom.value = Math.min(zoom.value + 0.1, 2);
};

const zoomOut = () => {
  zoom.value = Math.max(zoom.value - 0.1, 0.5);
};

const resetZoom = () => {
  zoom.value = 1;
};

// 节点拖拽处理
const handleNodeDragStart = (nodeType) => {
  // 开始拖拽节点
};

const handleNodeAdd = (nodeData) => {
  definition.nodes.push(nodeData);

  // 自动设置开始和结束节点
  if (nodeData.type === 'start') {
    definition.startNodeId = nodeData.id;
  } else if (nodeData.type === 'end') {
    definition.endNodeId = nodeData.id;
  }
};

const handleNodeUpdate = (nodeData) => {
  const index = definition.nodes.findIndex(node => node.id === nodeData.id);
  if (index !== -1) {
    definition.nodes[index] = { ...definition.nodes[index], ...nodeData };
  }
};

const handleNodeDelete = (nodeId) => {
  const index = definition.nodes.findIndex(node => node.id === nodeId);
  if (index !== -1) {
    definition.nodes.splice(index, 1);

    // 删除相关连线
    definition.edges = definition.edges.filter(edge =>
      edge.source !== nodeId && edge.target !== nodeId
    );
  }
};

// 连线处理
const handleEdgeAdd = (edgeData) => {
  definition.edges.push(edgeData);
};

const handleEdgeUpdate = (edgeData) => {
  const index = definition.edges.findIndex(edge => edge.id === edgeData.id);
  if (index !== -1) {
    definition.edges[index] = { ...definition.edges[index], ...edgeData };
  }
};

const handleEdgeDelete = (edgeId) => {
  const index = definition.edges.findIndex(edge => edge.id === edgeId);
  if (index !== -1) {
    definition.edges.splice(index, 1);
  }
};

// 选中处理
const handleNodeSelect = (node) => {
  selectedElement.value = node;
};

const handleCanvasNodeSelect = (element) => {
  selectedElement.value = element;
};

// 属性变更处理
const handlePropertyChange = (property, value) => {
  if (selectedElement.value) {
    selectedElement.value[property] = value;

    // 更新对应的节点或连线
    if (selectedElement.value.type) {
      handleNodeUpdate(selectedElement.value);
    } else {
      handleEdgeUpdate(selectedElement.value);
    }
  }
};

// 流程信息保存
const handleInfoSave = (info) => {
  Object.assign(definition, info);
};

// 验证流程定义
const validateDefinition = () => {
  if (!definition.definitionName) {
    message.error('请输入流程名称');
    return false;
  }

  if (!definition.definitionKey) {
    message.error('请输入流程标识');
    return false;
  }

  return true;
};

// 验证流程结构
const validateFlowStructure = () => {
  const hasStartNode = definition.nodes.some(node => node.type === 'start');
  const hasEndNode = definition.nodes.some(node => node.type === 'end');

  return hasStartNode && hasEndNode;
};

// 生命周期
onMounted(() => {
  // 初始化流程信息
  if (!definition.definitionKey) {
    infoModalVisible.value = true;
  }
});

// 监听选中元素变化
watch(selectedElement, (newVal) => {
  if (newVal) {
    // 更新属性面板
  }
});
</script>

<style scoped>
.workflow-designer {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.designer-toolbar {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.designer-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.node-panel {
  width: 240px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  overflow-y: auto;
}

.canvas-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.property-panel {
  width: 320px;
  background: #fff;
  border-left: 1px solid #e8e8e8;
  overflow-y: auto;
}

.right-toolbar {
  margin-left: auto;
}
</style>
```

#### 4.2.2 流程画布组件

```vue
<!-- src/views/workflow/designer/components/WorkflowCanvas.vue -->
<template>
  <div class="workflow-canvas" ref="canvasRef">
    <div class="canvas-wrapper" :style="canvasStyle">
      <!-- 网格背景 -->
      <svg class="grid-background" width="100%" height="100%">
        <defs>
          <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
            <circle cx="1" cy="1" r="1" fill="#e0e0e0" />
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#grid)" />
      </svg>

      <!-- SVG 画布 -->
      <svg
        class="flow-canvas"
        :width="canvasWidth"
        :height="canvasHeight"
        @mousedown="handleCanvasMouseDown"
        @mousemove="handleCanvasMouseMove"
        @mouseup="handleCanvasMouseUp"
        @contextmenu.prevent
      >
        <!-- 连线层 -->
        <g class="edges-layer">
          <g
            v-for="edge in edges"
            :key="edge.id"
            class="edge-group"
            @click="selectEdge(edge)"
            @contextmenu.prevent="showEdgeContextMenu($event, edge)"
          >
            <path
              :d="getEdgePath(edge)"
              :class="getEdgeClass(edge)"
              stroke="#666"
              stroke-width="2"
              fill="none"
              marker-end="url(#arrowhead)"
            />
            <text
              v-if="edge.label"
              :x="getEdgeLabelPosition(edge).x"
              :y="getEdgeLabelPosition(edge).y"
              class="edge-label"
              text-anchor="middle"
            >
              {{ edge.label }}
            </text>
          </g>
        </g>

        <!-- 节点层 -->
        <g class="nodes-layer">
          <g
            v-for="node in nodes"
            :key="node.id"
            :transform="`translate(${node.x}, ${node.y})`"
            class="node-group"
            @mousedown="handleNodeMouseDown($event, node)"
            @click="selectNode(node)"
            @contextmenu.prevent="showNodeContextMenu($event, node)"
          >
            <component
              :is="getNodeComponent(node.type)"
              :node="node"
              :selected="selectedNode?.id === node.id"
              @property-change="$emit('nodeUpdate', node)"
            />
          </g>
        </g>

        <!-- 临时连线 -->
        <path
          v-if="tempEdge"
          :d="tempEdge.path"
          class="temp-edge"
          stroke="#1890ff"
          stroke-width="2"
          stroke-dasharray="5,5"
          fill="none"
        />

        <!-- 箭头标记 -->
        <defs>
          <marker
            id="arrowhead"
            markerWidth="10"
            markerHeight="10"
            refX="9"
            refY="3"
            orient="auto"
          >
            <polygon
              points="0 0, 10 3, 0 6"
              fill="#666"
            />
          </marker>
        </defs>
      </svg>
    </div>

    <!-- 节点右键菜单 -->
    <NodeContextMenu
      v-model:visible="nodeContextMenu.visible"
      :position="nodeContextMenu.position"
      :node="nodeContextMenu.node"
      @edit="editNode"
      @delete="deleteNode"
      @copy="copyNode"
    />

    <!-- 连线右键菜单 -->
    <EdgeContextMenu
      v-model:visible="edgeContextMenu.visible"
      :position="edgeContextMenu.position"
      :edge="edgeContextMenu.edge"
      @edit="editEdge"
      @delete="deleteEdge"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue';
import StartNode from './nodes/StartNode.vue';
import EndNode from './nodes/EndNode.vue';
import UserTaskNode from './nodes/UserTaskNode.vue';
import ServiceTaskNode from './nodes/ServiceTaskNode.vue';
import GatewayNode from './nodes/GatewayNode.vue';
import NodeContextMenu from './NodeContextMenu.vue';
import EdgeContextMenu from './EdgeContextMenu.vue';

const props = defineProps({
  definition: {
    type: Object,
    required: true
  },
  zoom: {
    type: Number,
    default: 1
  }
});

const emit = defineEmits([
  'nodeAdd',
  'nodeUpdate',
  'nodeDelete',
  'edgeAdd',
  'edgeUpdate',
  'edgeDelete',
  'nodeSelect'
]);

// 组件引用
const canvasRef = ref(null);

// 响应式数据
const canvasWidth = ref(2000);
const canvasHeight = ref(2000);
const selectedNode = ref(null);
const selectedEdge = ref(null);

// 拖拽状态
const dragging = ref(false);
const dragNode = ref(null);
const dragOffset = ref({ x: 0, y: 0 });

// 连线状态
const connecting = ref(false);
const connectingNode = ref(null);
const tempEdge = ref(null);

// 右键菜单
const nodeContextMenu = reactive({
  visible: false,
  position: { x: 0, y: 0 },
  node: null
});

const edgeContextMenu = reactive({
  visible: false,
  position: { x: 0, y: 0 },
  edge: null
});

// 计算属性
const canvasStyle = computed(() => ({
  transform: `scale(${props.zoom})`,
  transformOrigin: '0 0'
}));

const nodes = computed(() => props.definition.nodes || []);
const edges = computed(() => props.definition.edges || []);

// 节点组件映射
const nodeComponents = {
  start: StartNode,
  end: EndNode,
  userTask: UserTaskNode,
  serviceTask: ServiceTaskNode,
  gateway: GatewayNode
};

// 方法
const getNodeComponent = (nodeType) => {
  return nodeComponents[nodeType] || StartNode;
};

const getEdgePath = (edge) => {
  const sourceNode = nodes.value.find(node => node.id === edge.source);
  const targetNode = nodes.value.find(node => node.id === edge.target);

  if (!sourceNode || !targetNode) return '';

  const sourceX = sourceNode.x + 60;
  const sourceY = sourceNode.y + 30;
  const targetX = targetNode.x + 60;
  const targetY = targetNode.y + 30;

  // 简单的贝塞尔曲线
  const midX = (sourceX + targetX) / 2;
  const midY = (sourceY + targetY) / 2;

  return `M ${sourceX} ${sourceY} Q ${midX} ${sourceY}, ${midX} ${midY} T ${targetX} ${targetY}`;
};

const getEdgeLabelPosition = (edge) => {
  const sourceNode = nodes.value.find(node => node.id === edge.source);
  const targetNode = nodes.value.find(node => node.id === edge.target);

  if (!sourceNode || !targetNode) return { x: 0, y: 0 };

  return {
    x: (sourceNode.x + targetNode.x) / 2 + 60,
    y: (sourceNode.y + targetNode.y) / 2 + 30
  };
};

const getEdgeClass = (edge) => {
  return {
    'edge-selected': selectedEdge.value?.id === edge.id,
    'edge-hover': true
  };
};

// 鼠标事件处理
const handleCanvasMouseDown = (event) => {
  if (event.target === event.currentTarget) {
    // 点击空白区域，取消选中
    selectedNode.value = null;
    selectedEdge.value = null;
    emit('nodeSelect', null);
  }
};

const handleCanvasMouseMove = (event) => {
  if (dragging.value && dragNode.value) {
    // 拖拽节点
    const rect = canvasRef.value.getBoundingClientRect();
    const x = (event.clientX - rect.left) / props.zoom - dragOffset.value.x;
    const y = (event.clientY - rect.top) / props.zoom - dragOffset.value.y;

    dragNode.value.x = Math.max(0, Math.min(x, canvasWidth.value - 120));
    dragNode.value.y = Math.max(0, Math.min(y, canvasHeight.value - 60));

    emit('nodeUpdate', dragNode.value);
  } else if (connecting.value && connectingNode.value) {
    // 绘制临时连线
    const rect = canvasRef.value.getBoundingClientRect();
    const mouseX = (event.clientX - rect.left) / props.zoom;
    const mouseY = (event.clientY - rect.top) / props.zoom;

    const sourceX = connectingNode.value.x + 60;
    const sourceY = connectingNode.value.y + 30;

    tempEdge.value = {
      path: `M ${sourceX} ${sourceY} L ${mouseX} ${mouseY}`
    };
  }
};

const handleCanvasMouseUp = (event) => {
  dragging.value = false;
  dragNode.value = null;

  if (connecting.value) {
    connecting.value = false;
    connectingNode.value = null;
    tempEdge.value = null;
  }
};

const handleNodeMouseDown = (event, node) => {
  if (event.button === 0) { // 左键
    const rect = canvasRef.value.getBoundingClientRect();
    dragOffset.value = {
      x: (event.clientX - rect.left) / props.zoom - node.x,
      y: (event.clientY - rect.top) / props.zoom - node.y
    };
    dragging.value = true;
    dragNode.value = node;
  } else if (event.button === 2) { // 右键
    // 显示右键菜单
    showNodeContextMenu(event, node);
  }

  event.stopPropagation();
};

// 节点操作
const selectNode = (node) => {
  selectedNode.value = node;
  selectedEdge.value = null;
  emit('nodeSelect', node);
};

const editNode = (node) => {
  emit('nodeSelect', node);
  nodeContextMenu.visible = false;
};

const deleteNode = (node) => {
  const index = nodes.value.findIndex(n => n.id === node.id);
  if (index !== -1) {
    nodes.value.splice(index, 1);
    emit('nodeDelete', node.id);
  }
  nodeContextMenu.visible = false;
};

const copyNode = (node) => {
  const newNode = {
    ...node,
    id: 'node_' + Date.now(),
    x: node.x + 50,
    y: node.y + 50,
    name: node.name + '_副本'
  };
  nodes.value.push(newNode);
  emit('nodeAdd', newNode);
  nodeContextMenu.visible = false;
};

// 连线操作
const selectEdge = (edge) => {
  selectedEdge.value = edge;
  selectedNode.value = null;
  emit('nodeSelect', edge);
};

const startConnecting = (node) => {
  connecting.value = true;
  connectingNode.value = node;
};

const finishConnecting = (targetNode) => {
  if (connecting.value && connectingNode.value && targetNode !== connectingNode.value) {
    // 检查是否已存在连线
    const existingEdge = edges.value.find(edge =>
      edge.source === connectingNode.value.id && edge.target === targetNode.id
    );

    if (!existingEdge) {
      const newEdge = {
        id: 'edge_' + Date.now(),
        source: connectingNode.value.id,
        target: targetNode.id,
        label: '',
        condition: ''
      };
      edges.value.push(newEdge);
      emit('edgeAdd', newEdge);
    }
  }

  connecting.value = false;
  connectingNode.value = null;
  tempEdge.value = null;
};

const editEdge = (edge) => {
  emit('nodeSelect', edge);
  edgeContextMenu.visible = false;
};

const deleteEdge = (edge) => {
  const index = edges.value.findIndex(e => e.id === edge.id);
  if (index !== -1) {
    edges.value.splice(index, 1);
    emit('edgeDelete', edge.id);
  }
  edgeContextMenu.visible = false;
};

// 右键菜单
const showNodeContextMenu = (event, node) => {
  nodeContextMenu.visible = true;
  nodeContextMenu.position = { x: event.clientX, y: event.clientY };
  nodeContextMenu.node = node;
};

const showEdgeContextMenu = (event, edge) => {
  edgeContextMenu.visible = true;
  edgeContextMenu.position = { x: event.clientX, y: event.clientY };
  edgeContextMenu.edge = edge;
};

// 键盘事件
const handleKeyDown = (event) => {
  if (event.key === 'Delete') {
    if (selectedNode.value) {
      deleteNode(selectedNode.value);
    } else if (selectedEdge.value) {
      deleteEdge(selectedEdge.value);
    }
  }
};

// 生命周期
onMounted(() => {
  document.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeyDown);
});

// 监听画布大小变化
const updateCanvasSize = () => {
  const container = canvasRef.value;
  if (container) {
    canvasWidth.value = Math.max(2000, container.scrollWidth);
    canvasHeight.value = Math.max(2000, container.scrollHeight);
  }
};

onMounted(() => {
  updateCanvasSize();
  window.addEventListener('resize', updateCanvasSize);
});

onUnmounted(() => {
  window.removeEventListener('resize', updateCanvasSize);
});
</script>

<style scoped>
.workflow-canvas {
  width: 100%;
  height: 100%;
  overflow: auto;
  position: relative;
}

.canvas-wrapper {
  position: relative;
  min-width: 2000px;
  min-height: 2000px;
}

.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.flow-canvas {
  position: relative;
  z-index: 1;
}

.node-group {
  cursor: move;
}

.node-group:hover .node-rect {
  stroke-width: 2;
  stroke: #1890ff;
}

.edge-group {
  cursor: pointer;
}

.edge-group:hover path {
  stroke: #1890ff;
  stroke-width: 3;
}

.edge-selected path {
  stroke: #1890ff;
  stroke-width: 3;
}

.temp-edge {
  pointer-events: none;
}

.edge-label {
  font-size: 12px;
  fill: #666;
  pointer-events: none;
}
</style>
```

### 4.3 任务处理组件

#### 4.3.1 待办任务列表

```vue
<!-- src/views/workflow/task/TodoTaskList.vue -->
<template>
  <div class="todo-task-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="任务名称">
          <a-input
            v-model:value="searchForm.taskName"
            placeholder="请输入任务名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="流程名称">
          <a-select
            v-model:value="searchForm.definitionId"
            placeholder="请选择流程"
            allow-clear
            style="width: 200px"
          >
            <a-select-option
              v-for="definition in definitionList"
              :key="definition.definitionId"
              :value="definition.definitionId"
            >
              {{ definition.definitionName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="优先级">
          <a-select
            v-model:value="searchForm.priority"
            placeholder="请选择优先级"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">低</a-select-option>
            <a-select-option :value="2">中</a-select-option>
            <a-select-option :value="3">高</a-select-option>
            <a-select-option :value="4">紧急</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="searchTasks">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 任务列表 -->
    <div class="task-table">
      <a-table
        :columns="columns"
        :data-source="taskList"
        :loading="loading"
        :pagination="pagination"
        row-key="taskId"
        @change="handleTableChange"
      >
        <!-- 任务名称 -->
        <template #taskName="{ record }">
          <a @click="viewTask(record)">{{ record.taskName }}</a>
        </template>

        <!-- 流程信息 -->
        <template #processInfo="{ record }">
          <div>
            <div>{{ record.definitionName }}</div>
            <div class="text-gray">{{ record.instanceKey }}</div>
          </div>
        </template>

        <!-- 优先级 -->
        <template #priority="{ record }">
          <a-tag :color="getPriorityColor(record.priority)">
            {{ getPriorityText(record.priority) }}
          </a-tag>
        </template>

        <!-- 发起人 -->
        <template #initiator="{ record }">
          <a-avatar :size="24" :src="record.initiatorAvatar">
            {{ record.initiatorName?.charAt(0) }}
          </a-avatar>
          <span class="ml-2">{{ record.initiatorName }}</span>
        </template>

        <!-- 到期时间 -->
        <template #dueTime="{ record }">
          <span v-if="record.dueTime" :class="getDueTimeClass(record.dueTime)">
            {{ formatDateTime(record.dueTime) }}
          </span>
          <span v-else class="text-gray">无限制</span>
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="handleTask(record)">
              处理
            </a-button>
            <a-button type="link" size="small" @click="delegateTask(record)">
              转办
            </a-button>
            <a-dropdown>
              <template #overlay>
                <a-menu @click="({ key }) => handleMenuClick(key, record)">
                  <a-menu-item key="view">查看详情</a-menu-item>
                  <a-menu-item key="history">处理历史</a-menu-item>
                  <a-menu-item key="diagram">流程图</a-menu-item>
                </a-menu>
              </template>
              <a-button type="link" size="small">
                更多 <DownOutlined />
              </a-button>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </div>

    <!-- 任务处理弹窗 -->
    <TaskHandleModal
      v-model:visible="handleModalVisible"
      :task="currentTask"
      @success="handleSuccess"
    />

    <!-- 转办弹窗 -->
    <TaskDelegateModal
      v-model:visible="delegateModalVisible"
      :task="currentTask"
      @success="delegateSuccess"
    />

    <!-- 流程图弹窗 -->
    <ProcessDiagramModal
      v-model:visible="diagramModalVisible"
      :instance-id="currentInstance?.instanceId"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  DownOutlined
} from '@ant-design/icons-vue';
import { getTodoTasks } from '@/api/workflow/instance';
import { getDefinitionPage } from '@/api/workflow/definition';
import TaskHandleModal from './components/TaskHandleModal.vue';
import TaskDelegateModal from './components/TaskDelegateModal.vue';
import ProcessDiagramModal from './components/ProcessDiagramModal.vue';
import { formatDateTime } from '@/utils/date';
import { useRouter } from 'vue-router';

const router = useRouter();

// 响应式数据
const loading = ref(false);
const taskList = ref([]);
const definitionList = ref([]);
const handleModalVisible = ref(false);
const delegateModalVisible = ref(false);
const diagramModalVisible = ref(false);
const currentTask = ref(null);
const currentInstance = ref(null);

// 搜索表单
const searchForm = reactive({
  taskName: '',
  definitionId: undefined,
  priority: undefined,
  pageNum: 1,
  pageSize: 10
});

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 条数据`
});

// 表格列配置
const columns = [
  {
    title: '任务名称',
    dataIndex: 'taskName',
    key: 'taskName',
    slots: { customRender: 'taskName' },
    width: 200
  },
  {
    title: '流程信息',
    key: 'processInfo',
    slots: { customRender: 'processInfo' },
    width: 250
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    slots: { customRender: 'priority' },
    width: 80
  },
  {
    title: '发起人',
    key: 'initiator',
    slots: { customRender: 'initiator' },
    width: 120
  },
  {
    title: '开始时间',
    dataIndex: 'startTime',
    key: 'startTime',
    width: 150
  },
  {
    title: '到期时间',
    dataIndex: 'dueTime',
    key: 'dueTime',
    slots: { customRender: 'dueTime' },
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    width: 200,
    fixed: 'right'
  }
];

// 方法
const loadTasks = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    };

    const result = await getTodoTasks(params);
    taskList.value = result.data.list;
    pagination.total = result.data.total;
  } catch (error) {
    message.error('加载任务列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

const loadDefinitions = async () => {
  try {
    const result = await getDefinitionPage({
      pageNum: 1,
      pageSize: 1000,
      status: 1 // 只获取启用的流程
    });
    definitionList.value = result.data.list;
  } catch (error) {
    console.error('加载流程列表失败：', error);
  }
};

const searchTasks = () => {
  pagination.current = 1;
  loadTasks();
};

const resetSearch = () => {
  Object.assign(searchForm, {
    taskName: '',
    definitionId: undefined,
    priority: undefined,
    pageNum: 1,
    pageSize: 10
  });
  pagination.current = 1;
  loadTasks();
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadTasks();
};

const viewTask = (task) => {
  currentTask.value = task;
  handleModalVisible.value = true;
};

const handleTask = (task) => {
  currentTask.value = task;
  handleModalVisible.value = true;
};

const delegateTask = (task) => {
  currentTask.value = task;
  delegateModalVisible.value = true;
};

const handleSuccess = () => {
  handleModalVisible.value = false;
  loadTasks();
};

const delegateSuccess = () => {
  delegateModalVisible.value = false;
  loadTasks();
};

const handleMenuClick = (key, task) => {
  currentTask.value = task;
  currentInstance.value = {
    instanceId: task.instanceId,
    definitionId: task.definitionId
  };

  switch (key) {
    case 'view':
      viewTask(task);
      break;
    case 'history':
      router.push(`/workflow/task/history/${task.taskId}`);
      break;
    case 'diagram':
      diagramModalVisible.value = true;
      break;
  }
};

// 工具方法
const getPriorityColor = (priority) => {
  const colors = {
    1: 'blue',    // 低
    2: 'green',   // 中
    3: 'orange',  // 高
    4: 'red'      // 紧急
  };
  return colors[priority] || 'default';
};

const getPriorityText = (priority) => {
  const texts = {
    1: '低',
    2: '中',
    3: '高',
    4: '紧急'
  };
  return texts[priority] || '未知';
};

const getDueTimeClass = (dueTime) => {
  const now = new Date();
  const due = new Date(dueTime);
  const diffHours = (due - now) / (1000 * 60 * 60);

  if (diffHours < 0) {
    return 'text-red'; // 已过期
  } else if (diffHours < 24) {
    return 'text-orange'; // 即将到期
  }
  return '';
};

// 生命周期
onMounted(() => {
  loadTasks();
  loadDefinitions();
});
</script>

<style scoped>
.todo-task-list {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}

.search-bar {
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.task-table {
  margin-top: 16px;
}

.text-gray {
  color: #999;
  font-size: 12px;
}

.text-red {
  color: #ff4d4f;
}

.text-orange {
  color: #fa8c16;
}

.ml-2 {
  margin-left: 8px;
}
</style>
```

## 5. 测试策略

### 5.1 单元测试

#### 5.1.1 流程定义服务测试

```java
package net.lab1024.sa.base.module.service.workflow;

import net.lab1024.sa.base.BaseTest;
import net.lab1024.sa.base.module.entity.workflow.WorkflowDefinitionEntity;
import net.lab1024.sa.base.module.service.workflow.dto.WorkflowDefinitionCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WorkflowDefinitionServiceTest extends BaseTest {

    @Autowired
    private WorkflowDefinitionService definitionService;

    @Test
    public void testCreateDefinition() {
        WorkflowDefinitionCreateDTO createDTO = new WorkflowDefinitionCreateDTO();
        createDTO.setDefinitionKey("test_leave_process");
        createDTO.setDefinitionName("请假流程");
        createDTO.setDefinitionDesc("员工请假审批流程");
        createDTO.setCategoryId(1L);
        createDTO.setStartNodeId("start_1");
        createDTO.setEndNodeId("end_1");

        Long definitionId = definitionService.createDefinition(createDTO, 1L);

        assertNotNull(definitionId);

        WorkflowDefinitionEntity definition = definitionService.getById(definitionId);
        assertNotNull(definition);
        assertEquals("test_leave_process", definition.getDefinitionKey());
        assertEquals("请假流程", definition.getDefinitionName());
        assertEquals(WorkflowDefinitionEntity.Status.DRAFT.getValue(), definition.getStatus());
        assertEquals(1, definition.getVersion());
    }

    @Test
    public void testPublishDefinition() {
        // 创建流程定义
        WorkflowDefinitionCreateDTO createDTO = new WorkflowDefinitionCreateDTO();
        createDTO.setDefinitionKey("test_publish_process");
        createDTO.setDefinitionName("测试发布流程");
        createDTO.setStartNodeId("start_1");
        createDTO.setEndNodeId("end_1");

        Long definitionId = definitionService.createDefinition(createDTO, 1L);

        // 发布流程定义
        definitionService.publishDefinition(definitionId, 1L);

        WorkflowDefinitionEntity definition = definitionService.getById(definitionId);
        assertEquals(WorkflowDefinitionEntity.Status.ENABLED.getValue(), definition.getStatus());
    }

    @Test
    public void testCreateNewVersion() {
        // 创建并发布流程定义
        WorkflowDefinitionCreateDTO createDTO = new WorkflowDefinitionCreateDTO();
        createDTO.setDefinitionKey("test_version_process");
        createDTO.setDefinitionName("测试版本流程");
        createDTO.setStartNodeId("start_1");
        createDTO.setEndNodeId("end_1");

        Long definitionId = definitionService.createDefinition(createDTO, 1L);
        definitionService.publishDefinition(definitionId, 1L);

        // 创建新版本
        WorkflowDefinitionUpdateDTO updateDTO = new WorkflowDefinitionUpdateDTO();
        updateDTO.setDefinitionId(definitionId);
        updateDTO.setDefinitionName("测试版本流程 v2");

        definitionService.updateDefinition(definitionId, updateDTO, 1L);

        // 验证新版本创建
        WorkflowDefinitionEntity oldVersion = definitionService.getById(definitionId);
        assertEquals(WorkflowDefinitionEntity.Status.DISABLED.getValue(), oldVersion.getStatus());

        // 获取最新版本
        List<WorkflowDefinitionEntity> versions = definitionService.lambdaQuery()
                .eq(WorkflowDefinitionEntity::getDefinitionKey, "test_version_process")
                .orderByDesc(WorkflowDefinitionEntity::getVersion)
                .list();

        assertEquals(2, versions.size());
        assertEquals(2, versions.get(0).getVersion());
        assertEquals("测试版本流程 v2", versions.get(0).getDefinitionName());
        assertEquals(WorkflowDefinitionEntity.Status.DRAFT.getValue(), versions.get(0).getStatus());
    }
}
```

#### 5.1.2 流程引擎测试

```java
package net.lab1024.sa.base.module.service.workflow;

import net.lab1024.sa.base.BaseTest;
import net.lab1024.sa.base.module.entity.workflow.WorkflowDefinitionEntity;
import net.lab1024.sa.base.module.entity.workflow.WorkflowInstanceEntity;
import net.lab1024.sa.base.module.service.workflow.dto.WorkflowStartDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WorkflowInstanceServiceTest extends BaseTest {

    @Autowired
    private WorkflowInstanceService instanceService;

    @Autowired
    private WorkflowDefinitionService definitionService;

    @Test
    public void testStartProcess() {
        // 创建流程定义
        WorkflowDefinitionCreateDTO createDTO = createTestDefinition();
        Long definitionId = definitionService.createDefinition(createDTO, 1L);
        definitionService.publishDefinition(definitionId, 1L);

        // 启动流程实例
        WorkflowStartDTO startDTO = new WorkflowStartDTO();
        startDTO.setDefinitionId(definitionId);
        startDTO.setBusinessKey("LEAVE_001");
        startDTO.setBusinessType("LEAVE");
        startDTO.setTitle("张三的请假申请");

        Map<String, Object> variables = new HashMap<>();
        variables.put("leaveDays", 3);
        variables.put("leaveReason", "家中有事");
        startDTO.setVariables(variables);

        WorkflowInstanceVO instance = instanceService.startProcess(startDTO, 1L);

        assertNotNull(instance);
        assertEquals("LEAVE_001", instance.getBusinessKey());
        assertEquals("张三的请假申请", instance.getTitle());
        assertEquals(WorkflowInstanceEntity.Status.RUNNING.getValue(), instance.getStatus());
        assertNotNull(instance.getInstanceKey());
    }

    @Test
    public void testCompleteTask() {
        // 启动流程
        WorkflowInstanceVO instance = startTestProcess();

        // 获取第一个任务
        List<WorkflowTaskEntity> tasks = taskService.getActiveTasks(instance.getInstanceId());
        assertFalse(tasks.isEmpty());

        WorkflowTaskEntity task = tasks.get(0);

        // 完成任务
        WorkflowCompleteDTO completeDTO = new WorkflowCompleteDTO();
        completeDTO.setTaskId(task.getTaskId());
        completeDTO.setActionName("同意");
        completeDTO.setComment("同意请假申请");

        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        completeDTO.setVariables(variables);

        instanceService.completeTask(completeDTO, task.getAssigneeId());

        // 验证任务状态
        WorkflowTaskEntity updatedTask = taskService.getById(task.getTaskId());
        assertEquals(WorkflowTaskEntity.Status.COMPLETED.getValue(), updatedTask.getStatus());
        assertNotNull(updatedTask.getEndTime());
    }

    @Test
    public void testRejectTask() {
        // 启动流程
        WorkflowInstanceVO instance = startTestProcess();

        // 获取第一个任务
        List<WorkflowTaskEntity> tasks = taskService.getActiveTasks(instance.getInstanceId());
        WorkflowTaskEntity task = tasks.get(0);

        // 拒绝任务
        WorkflowRejectDTO rejectDTO = new WorkflowRejectDTO();
        rejectDTO.setTaskId(task.getTaskId());
        rejectDTO.setComment("请假天数过长，不予批准");

        instanceService.rejectTask(rejectDTO, task.getAssigneeId());

        // 验证流程状态
        WorkflowInstanceEntity updatedInstance = instanceService.getById(instance.getInstanceId());
        assertEquals(WorkflowInstanceEntity.Status.REJECTED.getValue(), updatedInstance.getStatus());
        assertNotNull(updatedInstance.getEndTime());
    }

    @Test
    public void testWithdrawProcess() {
        // 启动流程
        WorkflowInstanceVO instance = startTestProcess();

        // 撤回流程
        instanceService.withdrawProcess(instance.getInstanceId(), instance.getInitiatorId());

        // 验证流程状态
        WorkflowInstanceEntity updatedInstance = instanceService.getById(instance.getInstanceId());
        assertEquals(WorkflowInstanceEntity.Status.WITHDRAWN.getValue(), updatedInstance.getStatus());
        assertNotNull(updatedInstance.getEndTime());
    }

    private WorkflowDefinitionCreateDTO createTestDefinition() {
        WorkflowDefinitionCreateDTO createDTO = new WorkflowDefinitionCreateDTO();
        createDTO.setDefinitionKey("test_leave_process");
        createDTO.setDefinitionName("请假流程");
        createDTO.setStartNodeId("start_1");
        createDTO.setEndNodeId("end_1");
        return createDTO;
    }

    private WorkflowInstanceVO startTestProcess() {
        WorkflowDefinitionCreateDTO createDTO = createTestDefinition();
        Long definitionId = definitionService.createDefinition(createDTO, 1L);
        definitionService.publishDefinition(definitionId, 1L);

        WorkflowStartDTO startDTO = new WorkflowStartDTO();
        startDTO.setDefinitionId(definitionId);
        startDTO.setBusinessKey("LEAVE_001");
        startDTO.setTitle("请假申请");

        return instanceService.startProcess(startDTO, 1L);
    }
}
```

### 5.2 集成测试

#### 5.2.1 完整流程测试

```java
package net.lab1024.sa.base.module.integration.workflow;

import net.lab1024.sa.base.BaseTest;
import net.lab1024.sa.base.module.service.workflow.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WorkflowIntegrationTest extends BaseTest {

    @Autowired
    private WorkflowDefinitionService definitionService;

    @Autowired
    private WorkflowInstanceService instanceService;

    @Autowired
    private WorkflowTaskService taskService;

    @Test
    public void testCompleteApprovalProcess() {
        // 1. 创建流程定义
        Long definitionId = createApprovalProcess();

        // 2. 启动流程实例
        WorkflowInstanceVO instance = startApprovalProcess(definitionId);

        // 3. 部门经理审批
        WorkflowTaskEntity managerTask = getNextTask(instance.getInstanceId());
        completeTask(managerTask, "同意", Map.of("approved", true));

        // 4. HR审批
        WorkflowTaskEntity hrTask = getNextTask(instance.getInstanceId());
        completeTask(hrTask, "同意", Map.of("approved", true));

        // 5. 验证流程完成
        WorkflowInstanceEntity completedInstance = instanceService.getById(instance.getInstanceId());
        assertEquals(WorkflowInstanceEntity.Status.COMPLETED.getValue(), completedInstance.getStatus());
        assertNotNull(completedInstance.getEndTime());
    }

    @Test
    public void testRejectionProcess() {
        // 1. 创建并启动流程
        Long definitionId = createApprovalProcess();
        WorkflowInstanceVO instance = startApprovalProcess(definitionId);

        // 2. 部门经理拒绝
        WorkflowTaskEntity managerTask = getNextTask(instance.getInstanceId());
        rejectTask(managerTask, "请假理由不充分");

        // 3. 验证流程被拒绝
        WorkflowInstanceEntity rejectedInstance = instanceService.getById(instance.getInstanceId());
        assertEquals(WorkflowInstanceEntity.Status.REJECTED.getValue(), rejectedInstance.getStatus());
    }

    @Test
    public void testDelegateProcess() {
        // 1. 创建并启动流程
        Long definitionId = createApprovalProcess();
        WorkflowInstanceVO instance = startApprovalProcess(definitionId);

        // 2. 获取任务并转办
        WorkflowTaskEntity task = getNextTask(instance.getInstanceId());
        Long originalAssignee = task.getAssigneeId();
        Long newAssignee = 3L;

        delegateTask(task, newAssignee, "代理审批");

        // 3. 验证转办成功
        WorkflowTaskEntity delegatedTask = taskService.getById(task.getTaskId());
        assertEquals(newAssignee, delegatedTask.getAssigneeId());
        assertEquals(WorkflowTaskEntity.Status.DELEGATED.getValue(), delegatedTask.getStatus());
    }

    private Long createApprovalProcess() {
        WorkflowDefinitionCreateDTO createDTO = new WorkflowDefinitionCreateDTO();
        createDTO.setDefinitionKey("approval_process");
        createDTO.setDefinitionName("审批流程");
        createDTO.setStartNodeId("start_1");
        createDTO.setEndNodeId("end_1");

        return definitionService.createDefinition(createDTO, 1L);
    }

    private WorkflowInstanceVO startApprovalProcess(Long definitionId) {
        definitionService.publishDefinition(definitionId, 1L);

        WorkflowStartDTO startDTO = new WorkflowStartDTO();
        startDTO.setDefinitionId(definitionId);
        startDTO.setBusinessKey("REQ_001");
        startDTO.setTitle("测试审批申请");

        return instanceService.startProcess(startDTO, 1L);
    }

    private WorkflowTaskEntity getNextTask(Long instanceId) {
        List<WorkflowTaskEntity> tasks = taskService.getActiveTasks(instanceId);
        assertFalse(tasks.isEmpty(), "应该有待处理的任务");
        return tasks.get(0);
    }

    private void completeTask(WorkflowTaskEntity task, String comment, Map<String, Object> variables) {
        WorkflowCompleteDTO completeDTO = new WorkflowCompleteDTO();
        completeDTO.setTaskId(task.getTaskId());
        completeDTO.setActionName("同意");
        completeDTO.setComment(comment);
        completeDTO.setVariables(variables);

        instanceService.completeTask(completeDTO, task.getAssigneeId());
    }

    private void rejectTask(WorkflowTaskEntity task, String comment) {
        WorkflowRejectDTO rejectDTO = new WorkflowRejectDTO();
        rejectDTO.setTaskId(task.getTaskId());
        rejectDTO.setComment(comment);

        instanceService.rejectTask(rejectDTO, task.getAssigneeId());
    }

    private void delegateTask(WorkflowTaskEntity task, Long toUserId, String reason) {
        WorkflowDelegateDTO delegateDTO = new WorkflowDelegateDTO();
        delegateDTO.setTaskId(task.getTaskId());
        delegateDTO.setToUserId(toUserId);
        delegateDTO.setToUserName("用户" + toUserId);
        delegateDTO.setReason(reason);

        instanceService.delegateTask(delegateDTO, task.getAssigneeId());
    }
}
```

### 5.3 性能测试

#### 5.3.1 并发处理测试

```java
package net.lab1024.sa.base.module.performance.workflow;

import net.lab1024.sa.base.BaseTest;
import net.lab1024.sa.base.module.service.workflow.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class WorkflowPerformanceTest extends BaseTest {

    @Autowired
    private WorkflowDefinitionService definitionService;

    @Autowired
    private WorkflowInstanceService instanceService;

    @Test
    public void testConcurrentProcessStart() throws Exception {
        int threadCount = 10;
        int processesPerThread = 5;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 准备流程定义
        Long definitionId = prepareProcessDefinition();

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < processesPerThread; j++) {
                    try {
                        startProcess(definitionId, threadIndex, j);
                    } catch (Exception e) {
                        System.err.println("启动流程失败: " + e.getMessage());
                    }
                }
            }, executor);
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 验证结果
        int totalProcesses = threadCount * processesPerThread;
        // TODO: 验证实际启动的流程数量

        executor.shutdown();
    }

    @Test
    public void testConcurrentTaskCompletion() throws Exception {
        // 启动多个流程实例
        List<Long> instanceIds = startMultipleProcesses(20);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 并发完成任务
        for (Long instanceId : instanceIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    completeFirstTask(instanceId);
                } catch (Exception e) {
                    System.err.println("完成任务失败: " + e.getMessage());
                }
            }, executor);
            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        executor.shutdown();
    }

    private Long prepareProcessDefinition() {
        WorkflowDefinitionCreateDTO createDTO = new WorkflowDefinitionCreateDTO();
        createDTO.setDefinitionKey("performance_test_process");
        createDTO.setDefinitionName("性能测试流程");
        createDTO.setStartNodeId("start_1");
        createDTO.setEndNodeId("end_1");

        Long definitionId = definitionService.createDefinition(createDTO, 1L);
        definitionService.publishDefinition(definitionId, 1L);

        return definitionId;
    }

    private void startProcess(Long definitionId, int threadIndex, int processIndex) {
        WorkflowStartDTO startDTO = new WorkflowStartDTO();
        startDTO.setDefinitionId(definitionId);
        startDTO.setBusinessKey("PERF_" + threadIndex + "_" + processIndex);
        startDTO.setTitle("性能测试流程 " + threadIndex + "_" + processIndex);

        instanceService.startProcess(startDTO, 1L);
    }

    private List<Long> startMultipleProcesses(int count) {
        List<Long> instanceIds = new ArrayList<>();
        Long definitionId = prepareProcessDefinition();

        for (int i = 0; i < count; i++) {
            WorkflowStartDTO startDTO = new WorkflowStartDTO();
            startDTO.setDefinitionId(definitionId);
            startDTO.setBusinessKey("PERF_" + i);
            startDTO.setTitle("并发测试流程 " + i);

            WorkflowInstanceVO instance = instanceService.startProcess(startDTO, 1L);
            instanceIds.add(instance.getInstanceId());
        }

        return instanceIds;
    }

    private void completeFirstTask(Long instanceId) {
        // 获取第一个任务并完成
        List<WorkflowTaskEntity> tasks = taskService.getActiveTasks(instanceId);
        if (!tasks.isEmpty()) {
            WorkflowTaskEntity task = tasks.get(0);

            WorkflowCompleteDTO completeDTO = new WorkflowCompleteDTO();
            completeDTO.setTaskId(task.getTaskId());
            completeDTO.setActionName("同意");
            completeDTO.setComment("性能测试任务处理");

            instanceService.completeTask(completeDTO, task.getAssigneeId());
        }
    }
}
```

## 6. 部署与配置

### 6.1 数据库初始化脚本

```sql
-- 审批流程模块数据库初始化脚本

-- 1. 创建分类数据
INSERT INTO smart_workflow_category (category_name, category_code, parent_id, sort_order, icon, description, status) VALUES
('人事流程', 'HR_PROCESS', 0, 1, 'user', '人事相关审批流程', 1),
('财务流程', 'FINANCE_PROCESS', 0, 2, 'dollar', '财务相关审批流程', 1),
('行政流程', 'ADMIN_PROCESS', 0, 3, 'file', '行政相关审批流程', 1),
('业务流程', 'BUSINESS_PROCESS', 0, 4, 'shop', '业务相关审批流程', 1),
('IT流程', 'IT_PROCESS', 0, 5, 'laptop', 'IT相关审批流程', 1);

-- 2. 创建示例流程定义
INSERT INTO smart_workflow_definition (
    definition_key, definition_name, definition_desc, version, status,
    category_id, form_config, flow_config, node_config,
    start_node_id, end_node_id, is_system, created_by
) VALUES
(
    'leave_request',
    '请假申请流程',
    '员工请假审批流程，包括请假申请、部门审批、HR备案等环节',
    1,
    1,
    (SELECT category_id FROM smart_workflow_category WHERE category_code = 'HR_PROCESS'),
    '{"fields":[{"name":"leaveType","label":"请假类型","type":"select","required":true,"options":["事假","病假","年假","婚假","产假"]},{"name":"startDate","label":"开始日期","type":"date","required":true},{"name":"endDate","label":"结束日期","type":"date","required":true},{"name":"reason","label":"请假原因","type":"textarea","required":true}]}',
    '{"timeout":{"enabled":true,"hours":48},"reminder":{"enabled":true,"beforeHours":2}}',
    '{"start":{"name":"开始","type":"start"},"manager":{"name":"部门审批","type":"userTask","assigneeType":"role","assigneeValue":"manager"},"hr":{"name":"HR备案","type":"userTask","assigneeType":"role","assigneeValue":"hr"},"end":{"name":"结束","type":"end"}}',
    'start_1',
    'end_1',
    1,
    1
),
(
    'expense_claim',
    '费用报销流程',
    '员工费用报销审批流程，包括报销申请、部门审批、财务审核等环节',
    1,
    1,
    (SELECT category_id FROM smart_workflow_category WHERE category_code = 'FINANCE_PROCESS'),
    '{"fields":[{"name":"expenseType","label":"费用类型","type":"select","required":true,"options":["差旅费","办公费","招待费","培训费","其他"]},{"name":"amount","label":"报销金额","type":"number","required":true,"min":0},{"name":"description","label":"费用说明","type":"textarea","required":true},{"name":"attachments","label":"附件","type":"file","required":false}]}',
    '{"timeout":{"enabled":true,"hours":72},"reminder":{"enabled":true,"beforeHours":4}}',
    '{"start":{"name":"开始","type":"start"},"manager":{"name":"部门审批","type":"userTask","assigneeType":"role","assigneeValue":"manager"},"finance":{"name":"财务审核","type":"userTask","assigneeType":"role","assigneeValue":"finance"},"end":{"name":"结束","type":"end"}}',
    'start_1',
    'end_1',
    1,
    1
),
(
    'purchase_request',
    '采购申请流程',
    '物品采购申请流程，包括采购申请、部门审批、采购部处理等环节',
    1,
    1,
    (SELECT category_id FROM smart_workflow_category WHERE category_code = 'ADMIN_PROCESS'),
    '{"fields":[{"name":"itemType","label":"物品类型","type":"select","required":true,"options":["办公用品","设备备件","维修材料","其他"]},{"name":"itemName","label":"物品名称","type":"text","required":true},{"name":"quantity","label":"数量","type":"number","required":true,"min":1},{"name":"urgency","label":"紧急程度","type":"radio","required":true,"options":["普通","紧急","非常紧急"]},{"name":"reason","label":"申请原因","type":"textarea","required":true}]}',
    '{"timeout":{"enabled":true,"hours":48},"reminder":{"enabled":true,"beforeHours":2}}',
    '{"start":{"name":"开始","type":"start"},"manager":{"name":"部门审批","type":"userTask","assigneeType":"role","assigneeValue":"manager"},"purchase":{"name":"采购处理","type":"userTask","assigneeType":"role","assigneeValue":"purchase"},"end":{"name":"结束","type":"end"}}',
    'start_1',
    'end_1',
    1,
    1
);

-- 3. 创建流程节点数据
INSERT INTO smart_workflow_node (node_id, definition_id, node_type, node_name, node_desc, position_x, position_y, properties, handlers, conditions, buttons, validations, sort_order) VALUES
-- 请假申请流程节点
('start_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'leave_request'), 'start', '开始', '流程开始节点', 100, 100, '{}', '{}', '{}', '[]', '[]', 1),
('manager_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'leave_request'), 'userTask', '部门审批', '部门经理审批', 300, 100, '{"multiInstance":false,"sequential":false,"assigneeType":"role","assigneeValue":"manager"}', '{"type":"role","value":"manager"}', '{}', '[{"key":"agree","name":"同意","primary":true},{"key":"reject","name":"拒绝","danger":true}]', '[{"field":"reason","required":true,"message":"请填写审批意见"}]', 2),
('hr_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'leave_request'), 'userTask', 'HR备案', 'HR部门备案', 500, 100, '{"assigneeType":"role","assigneeValue":"hr"}', '{"type":"role","value":"hr"}', '{}', '[{"key":"confirm","name":"确认","primary":true}]', '[]', 3),
('end_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'leave_request'), 'end', '结束', '流程结束节点', 700, 100, '{}', '{}', '{}', '[]', '[]', 4),

-- 费用报销流程节点
('start_2', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'expense_claim'), 'start', '开始', '流程开始节点', 100, 100, '{}', '{}', '{}', '[]', '[]', 1),
('manager_2', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'expense_claim'), 'userTask', '部门审批', '部门经理审批', 300, 100, '{"assigneeType":"role","assigneeValue":"manager"}', '{"type":"role","value":"manager"}', '{}', '[{"key":"approve","name":"同意","primary":true},{"key":"reject","name":"拒绝","danger":true}]', '[{"field":"comment","required":true,"message":"请填写审批意见"}]', 2),
('finance_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'expense_claim'), 'userTask', '财务审核', '财务部门审核', 500, 100, '{"assigneeType":"role","assigneeValue":"finance"}', '{"type":"role","value":"finance"}', '{}', '[{"key":"confirm","name":"确认","primary":true},{"key":"reject","name":"拒绝","danger":true}]', '[{"field":"auditComment","required":true,"message":"请填写审核意见"}]', 3),
('end_2', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'expense_claim'), 'end', '结束', '流程结束节点', 700, 100, '{}', '{}', '{}', '[]', '[]', 4),

-- 采购申请流程节点
('start_3', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'purchase_request'), 'start', '开始', '流程开始节点', 100, 100, '{}', '{}', '{}', '[]', '[]', 1),
('manager_3', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'purchase_request'), 'userTask', '部门审批', '部门经理审批', 300, 100, '{"assigneeType":"role","assigneeValue":"manager"}', '{"type":"role","value":"manager"}', '{}', '[{"key":"approve","name":"同意","primary":true},{"key":"reject","name":"拒绝","danger":true}]', '[{"field":"comment","required":true,"message":"请填写审批意见"}]', 2),
('purchase_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'purchase_request'), 'userTask', '采购处理', '采购部门处理', 500, 100, '{"assigneeType":"role","assigneeValue":"purchase"}', '{"type":"role","value":"purchase"}', '{}', '[{"key":"process","name":"处理","primary":true},{"key":"reject","name":"拒绝","danger":true}]', '[{"field":"processResult","required":true,"message":"请填写处理结果"}]', 3),
('end_3', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'purchase_request'), 'end', '结束', '流程结束节点', 700, 100, '{}', '{}', '{}', '[]', '[]', 4);

-- 4. 创建流程连线数据
INSERT INTO smart_workflow_edge (edge_id, definition_id, source_node_id, target_node_id, edge_name, condition_expr, condition_config) VALUES
-- 请假申请流程连线
('edge_1_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'leave_request'), 'start_1', 'manager_1', '提交申请', null, '{}'),
('edge_1_2', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'leave_request'), 'manager_1', 'hr_1', '同意', '${approved} == true', '{}'),
('edge_1_3', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'leave_request'), 'manager_1', 'end_1', '拒绝', '${approved} == false', '{}'),
('edge_1_4', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'leave_request'), 'hr_1', 'end_1', '完成', null, '{}'),

-- 费用报销流程连线
('edge_2_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'expense_claim'), 'start_2', 'manager_2', '提交申请', null, '{}'),
('edge_2_2', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'expense_claim'), 'manager_2', 'finance_1', '同意', '${approved} == true', '{}'),
('edge_2_3', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'expense_claim'), 'manager_2', 'end_2', '拒绝', '${approved} == false', '{}'),
('edge_2_4', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'expense_claim'), 'finance_1', 'end_2', '完成', null, '{}'),

-- 采购申请流程连线
('edge_3_1', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'purchase_request'), 'start_3', 'manager_3', '提交申请', null, '{}'),
('edge_3_2', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'purchase_request'), 'manager_3', 'purchase_1', '同意', '${approved} == true', '{}'),
('edge_3_3', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'purchase_request'), 'manager_3', 'end_3', '拒绝', '${approved} == false', '{}'),
('edge_3_4', (SELECT definition_id FROM smart_workflow_definition WHERE definition_key = 'purchase_request'), 'purchase_1', 'end_3', '完成', null, '{}');

-- 5. 创建流程模板数据
INSERT INTO smart_workflow_template (template_name, template_desc, category_id, definition_config, form_template, is_public, use_count, created_by) VALUES
(
    '请假申请模板',
    '标准请假申请流程模板，包含请假类型、日期、原因等信息',
    (SELECT category_id FROM smart_workflow_category WHERE category_code = 'HR_PROCESS'),
    '{"nodes":[{"id":"start_1","type":"start","name":"开始","x":100,"y":100},{"id":"manager_1","type":"userTask","name":"部门审批","x":300,"y":100,"assigneeType":"role","assigneeValue":"manager"},{"id":"hr_1","type":"userTask","name":"HR备案","x":500,"y":100,"assigneeType":"role","assigneeValue":"hr"},{"id":"end_1","type":"end","name":"结束","x":700,"y":100}],"edges":[{"id":"edge_1","source":"start_1","target":"manager_1"},{"id":"edge_2","source":"manager_1","target":"hr_1","condition":"${approved} == true"},{"id":"edge_3","source":"manager_1","target":"end_1","condition":"${approved} == false"},{"id":"edge_4","source":"hr_1","target":"end_1"}]}',
    '{"title":"请假申请","fields":[{"name":"leaveType","label":"请假类型","type":"select","required":true,"options":["事假","病假","年假"]},{"name":"startDate","label":"开始日期","type":"date","required":true},{"name":"endDate","label":"结束日期","type":"date","required":true},{"name":"reason","label":"请假原因","type":"textarea","required":true}]}',
    1,
    0,
    1
),
(
    '费用报销模板',
    '标准费用报销流程模板，包含费用类型、金额、说明等信息',
    (SELECT category_id FROM smart_workflow_category WHERE category_code = 'FINANCE_PROCESS'),
    '{"nodes":[{"id":"start_1","type":"start","name":"开始","x":100,"y":100},{"id":"manager_1","type":"userTask","name":"部门审批","x":300,"y":100,"assigneeType":"role","assigneeValue":"manager"},{"id":"finance_1","type":"userTask","name":"财务审核","x":500,"y":100,"assigneeType":"role","assigneeValue":"finance"},{"id":"end_1","type":"end","name":"结束","x":700,"y":100}],"edges":[{"id":"edge_1","source":"start_1","target":"manager_1"},{"id":"edge_2","source":"manager_1","target":"finance_1","condition":"${approved} == true"},{"id":"edge_3","source":"manager_1","target":"end_1","condition":"${approved} == false"},{"id":"edge_4","source":"finance_1","target":"end_1"}]}',
    '{"title":"费用报销","fields":[{"name":"expenseType","label":"费用类型","type":"select","required":true,"options":["差旅费","办公费","招待费"]},{"name":"amount","label":"报销金额","type":"number","required":true,"min":0},{"name":"description","label":"费用说明","type":"textarea","required":true}]}',
    1,
    0,
    1
);
```

### 6.2 配置文件

```yaml
# application-workflow.yml
workflow:
  # 流程引擎配置
  engine:
    # 异步执行配置
    async:
      enabled: true
      core-pool-size: 5
      max-pool-size: 20
      queue-capacity: 100
      thread-name-prefix: workflow-

    # 任务调度配置
    scheduler:
      enabled: true
      pool-size: 10

    # 缓存配置
    cache:
      enabled: true
      cache-manager: workflowCacheManager
      default-ttl: 3600 # 1小时

    # 事件监听配置
    event:
      enabled: true
      async: true
      retry-times: 3

  # 流程定义配置
  definition:
    # 版本控制
    version:
      max-versions: 10
      auto-cleanup: true
      cleanup-days: 30

    # 验证配置
    validation:
      enabled: true
      strict-mode: true

    # 导入导出配置
      import-export:
        max-file-size: 10MB
        allowed-types: [json, xml, bpmn]

  # 任务配置
  task:
    # 超时配置
    timeout:
      enabled: true
      default-hours: 48
      check-interval: 1h

    # 提醒配置
    reminder:
      enabled: true
      before-hours: [2, 24, 48]
      channels: [email, sms, system]

    # 代理配置
    delegation:
      enabled: true
      max-days: 30
      require-approval: false

  # 流程实例配置
  instance:
    # 实例限制
    limits:
      max-active-per-user: 50
      max-total-active: 1000

    # 历史配置
    history:
      enabled: true
      cleanup-days: 90
      archive-days: 365

  # 通知配置
  notification:
    # 通知渠道
    channels:
      email:
        enabled: true
        template-path: classpath:templates/workflow/
      sms:
        enabled: false
      system:
        enabled: true
        push-enabled: true
      wechat:
        enabled: false

    # 通知事件
    events:
      task-created: true
      task-completed: true
      task-overdue: true
      instance-completed: true
      instance-rejected: true

  # 报表配置
  report:
    enabled: true
    cache-ttl: 1800 # 30分钟
    export:
      enabled: true
      formats: [excel, pdf, csv]
      max-rows: 10000

  # 安全配置
  security:
    # 权限控制
    permission:
      enabled: true
      strict-mode: true

    # 数据权限
    data-permission:
      enabled: true
      dept-isolation: true
      user-isolation: false

    # 操作审计
    audit:
      enabled: true
      log-all: false
      log-sensitive: true

  # 集成配置
  integration:
    # LDAP集成
    ldap:
      enabled: false
      url: ldap://localhost:389
      base-dn: dc=company,dc=com

    # OA系统集成
    oa:
      enabled: false
      api-url: http://oa.company.com/api
      api-key: ${OA_API_KEY}

    # 移动端集成
    mobile:
      enabled: true
      push-enabled: true
      api-version: v1
```

### 6.3 监控配置

```yaml
# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,workflow
  endpoint:
    workflow:
      enabled: true
    health:
      workflow:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        workflow.process.duration: true
        workflow.task.duration: true
      percentiles:
        workflow.process.duration: 0.5,0.75,0.95,0.99
        workflow.task.duration: 0.5,0.75,0.95,0.99

# 日志配置
logging:
  level:
    net.lab1024.sa.base.module.service.workflow: DEBUG
    net.lab1024.sa.base.module.service.workflow.engine: TRACE
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - [%X{traceId}] %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - [%X{traceId}] %msg%n"
  file:
    name: logs/workflow.log
    max-size: 100MB
    max-history: 30
```

## 7. 总结

### 7.1 模块特点

审批流程公共模块作为SmartAdmin系统的核心业务流程引擎，具有以下特点：

1. **功能完整**
   - 支持完整的流程生命周期管理
   - 提供可视化的流程设计器
   - 支持多种节点类型和流转条件
   - 具备完善的任务处理机制

2. **设计灵活**
   - 支持动态流程定义和配置
   - 可扩展的节点类型和处理器
   - 灵活的条件路由和分支控制
   - 支持多实例和并行处理

3. **性能优异**
   - 高性能的流程引擎实现
   - 多级缓存机制
   - 异步处理和任务调度
   - 支持高并发场景

4. **易于集成**
   - 标准化的接口设计
   - 丰富的扩展点
   - 完善的事件机制
   - 支持与其他系统集成

### 7.2 最佳实践

1. **流程设计**
   - 保持流程简洁明了
   - 合理设置节点和条件
   - 避免过度复杂的分支
   - 定期优化流程性能

2. **权限控制**
   - 严格的数据权限控制
   - 基于角色的任务分配
   - 操作审计和日志记录
   - 敏感信息保护

3. **性能优化**
   - 合理使用缓存
   - 定期清理历史数据
   - 监控流程执行性能
   - 优化数据库查询

4. **用户体验**
   - 直观的流程设计界面
   - 清晰的任务处理界面
   - 及时的通知提醒
   - 完善的帮助文档

### 7.3 应用场景

审批流程公共模块适用于以下业务场景：

1. **人事管理**
   - 请假申请流程
   - 入职离职流程
   - 转正晋升流程
   - 薪资调整流程

2. **财务管理**
   - 费用报销流程
   - 付款申请流程
   - 预算审批流程
   - 发票管理流程

3. **行政管理**
   - 采购申请流程
   - 资产领用流程
   - 用章申请流程
   - 会议申请流程

4. **业务流程**
   - 合同审批流程
   - 项目立项流程
   - 客户管理流程
   - 销售审批流程

通过这个完整的审批流程公共模块，SmartAdmin系统为各种业务流程提供了统一的、高性能的、可扩展的流程引擎支持，大幅提升了系统的业务流程管理能力和用户体验。