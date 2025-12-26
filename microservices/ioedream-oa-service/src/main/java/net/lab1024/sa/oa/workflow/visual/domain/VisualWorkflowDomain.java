package net.lab1024.sa.oa.workflow.visual.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 可视化工作流配置领域对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public class VisualWorkflowDomain {

    /**
     * 可视化工作流配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualWorkflowConfig {
        /**
         * 流程定义ID
         */
        private String processDefinitionId;

        /**
         * 流程Key
         */
        private String processKey;

        /**
         * 流程名称
         */
        private String processName;

        /**
         * 流程描述
         */
        private String processDescription;

        /**
         * 流程分类
         */
        private String processCategory;

        /**
         * 流程版本
         */
        private String version;

        /**
         * 流程节点列表
         */
        private List<ProcessNode> nodes;

        /**
         * 流程连线列表
         */
        private List<ProcessEdge> edges;

        /**
         * 节点配置映射
         */
        private Map<String, NodeConfig> nodeConfigs;

        /**
         * 扩展属性
         */
        private Map<String, Object> extendedAttributes;
    }

    /**
     * 可视化配置表单
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualWorkflowConfigForm {
        /**
         * 流程Key
         */
        private String processKey;

        /**
         * 流程名称
         */
        private String processName;

        /**
         * 流程描述
         */
        private String processDescription;

        /**
         * 流程分类
         */
        private String processCategory;

        /**
         * 流程节点列表
         */
        private List<ProcessNode> nodes;

        /**
         * 流程连线列表
         */
        private List<ProcessEdge> edges;

        /**
         * 节点配置映射
         */
        private Map<String, NodeConfig> nodeConfigs;

        /**
         * 扩展属性
         */
        private Map<String, Object> extendedAttributes;
    }

    /**
     * 流程节点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessNode {
        /**
         * 节点ID
         */
        private String nodeId;

        /**
         * 节点类型
         * - START: 开始节点
         * - END: 结束节点
         * - USER_TASK: 用户任务
         * - SERVICE_TASK: 服务任务
         * - EXCLUSIVE_GATEWAY: 排他网关
         * - PARALLEL_GATEWAY: 并行网关
         * - INCLUSIVE_GATEWAY: 包容网关
         */
        private String nodeType;

        /**
         * 节点名称
         */
        private String name;

        /**
         * UI坐标X
         */
        private Integer x;

        /**
         * UI坐标Y
         */
        private Integer y;

        /**
         * 宽度
         */
        @Builder.Default
        private Integer width = 100;

        /**
         * 高度
         */
        @Builder.Default
        private Integer height = 80;

        /**
         * 入口连线列表
         */
        private List<String> incoming;

        /**
         * 出口连线列表
         */
        private List<String> outgoing;
    }

    /**
     * 流程连线
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessEdge {
        /**
         * 连线ID
         */
        private String edgeId;

        /**
         * 源节点ID
         */
        private String source;

        /**
         * 目标节点ID
         */
        private String target;

        /**
         * 连线名称
         */
        private String name;

        /**
         * 连线条件表达式
         */
        private String conditionExpression;
    }

    /**
     * 节点配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeConfig {
        /**
         * 节点ID
         */
        private String nodeId;

        /**
         * 审批人类型
         * - USER: 指定用户
         * - ROLE: 角色
         * - DEPT_LEADER: 部门领导
         * - INITIATOR: 发起人
         * - SCRIPT: 脚本
         */
        private String assigneeType;

        /**
         * 审批人值
         */
        private String assigneeValue;

        /**
         * 表单Key
         */
        private String formKey;

        /**
         * 到期时间（天数）
         */
        private Integer dueDate;

        /**
         * 是否多实例
         */
        private Boolean multiInstance;

        /**
         * 多实例类型
         * - SEQUENTIAL: 串行
         * - PARALLEL: 并行
         */
        private String multiInstanceType;

        /**
         * 通知类型
         */
        private List<String> notificationType;

        /**
         * 优先级
         */
        private String priority;

        /**
         * 跳过表达式
         */
        private String skipExpression;

        /**
         * 扩展属性
         */
        private Map<String, Object> extendedAttributes;
    }

    /**
     * 流程模板
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessTemplate {
        /**
         * 模板ID
         */
        private String templateId;

        /**
         * 模板名称
         */
        private String templateName;

        /**
         * 模板描述
         */
        private String templateDescription;

        /**
         * 分类
         */
        private String category;

        /**
         * 标签
         */
        private List<String> tags;

        /**
         * BPMN XML
         */
        private String bpmnXml;

        /**
         * 截图URL
         */
        private String screenshotUrl;

        /**
         * 使用次数
         */
        private Long usageCount;

        /**
         * 评分
         */
        private Double rating;
    }

    /**
     * 流程模板详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessTemplateDetail {
        /**
         * 模板ID
         */
        private String templateId;

        /**
         * 模板名称
         */
        private String templateName;

        /**
         * 模板描述
         */
        private String templateDescription;

        /**
         * 分类
         */
        private String category;

        /**
         * 标签
         */
        private List<String> tags;

        /**
         * 流程节点列表
         */
        private List<ProcessNode> nodes;

        /**
         * 流程连线列表
         */
        private List<ProcessEdge> edges;

        /**
         * 节点配置映射
         */
        private Map<String, NodeConfig> nodeConfigs;

        /**
         * BPMN XML
         */
        private String bpmnXml;

        /**
         * 截图URL
         */
        private String screenshotUrl;

        /**
         * 使用次数
         */
        private Long usageCount;

        /**
         * 评分
         */
        private Double rating;

        /**
         * 是否为系统模板
         */
        private Boolean systemTemplate;

        /**
         * 创建时间
         */
        private Long createTime;
    }

    /**
     * 验证错误
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * 节点ID
         */
        private String nodeId;

        /**
         * 连线ID
         */
        private String edgeId;

        /**
         * 错误消息
         */
        private String message;

        /**
         * 错误代码
         */
        private String errorCode;

        /**
         * 错误级别
         */
        private String level;
    }

    /**
     * 验证结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResult {
        /**
         * 是否验证通过
         */
        private Boolean valid;

        /**
         * 错误列表
         */
        private List<ValidationError> errors;

        /**
         * 警告列表
         */
        private List<ValidationError> warnings;

        /**
         * 信息列表
         */
        private List<ValidationError> infos;
    }

    /**
     * 工作流问题
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowIssue {
        /**
         * 问题ID
         */
        private String issueId;

        /**
         * 问题类型
         * - NODE_UNREACHABLE: 节点不可达
         * - GATEWAY_INVALID: 网关配置无效
         * - CIRCULAR_REFERENCE: 循环引用
         * - MISSING_ASSIGNEE: 缺少审批人
         * - MISSING_FORM: 缺少表单
         */
        private String issueType;

        /**
         * 问题描述
         */
        private String description;

        /**
         * 问题级别
         * - ERROR: 错误
         * - WARNING: 警告
         * - INFO: 信息
         */
        private String level;

        /**
         * 节点ID
         */
        private String nodeId;

        /**
         * 连线ID
         */
        private String edgeId;

        /**
         * 修复建议
         */
        private String suggestion;
    }

    /**
     * 工作流仿真请求
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowSimulationRequest {
        /**
         * 流程定义ID
         */
        private String processDefinitionId;

        /**
         * 发起人ID
         */
        private Long initiatorUserId;

        /**
         * 模拟表单数据
         */
        private Map<String, Object> formData;

        /**
         * 模拟变量
         */
        private Map<String, Object> variables;

        /**
         * 模拟路径（指定节点ID列表）
         */
        private List<String> simulationPath;
    }

    /**
     * 工作流仿真结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowSimulationResult {
        /**
         * 仿真是否成功
         */
        private Boolean success;

        /**
         * 执行路径
         */
        private List<String> executionPath;

        /**
         * 节点执行结果
         */
        private List<NodeExecutionResult> nodeResults;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 执行时间（毫秒）
         */
        private Long executionTime;
    }

    /**
     * 节点执行结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeExecutionResult {
        /**
         * 节点ID
         */
        private String nodeId;

        /**
         * 节点名称
         */
        private String nodeName;

        /**
         * 节点类型
         */
        private String nodeType;

        /**
         * 执行状态
         * - SUCCESS: 执行成功
         * - FAILED: 执行失败
         * - SKIPPED: 已跳过
         */
        private String status;

        /**
         * 审批人
         */
        private String assignee;

        /**
         * 审批意见
         */
        private String comment;

        /**
         * 执行时间
         */
        private Long executionTime;

        /**
         * 错误信息
         */
        private String errorMessage;
    }

    /**
     * 节点类型
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeType {
        /**
         * 节点类型
         */
        private String type;

        /**
         * 节点名称
         */
        private String name;

        /**
         * 节点描述
         */
        private String description;

        /**
         * 节点图标
         */
        private String icon;

        /**
         * 节点分类
         * - event: 事件节点（开始、结束）
         * - task: 任务节点（用户任务、服务任务）
         * - gateway: 网关节点（排他、并行、包容）
         * - other: 其他节点
         */
        private String category;

        /**
         * 是否支持配置
         */
        private Boolean configurable;

        /**
         * 配置Schema
         */
        private NodeConfigSchema configSchema;
    }

    /**
     * 节点配置Schema
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeConfigSchema {
        /**
         * 节点类型
         */
        private String nodeType;

        /**
         * 配置属性列表
         */
        private List<PropertyDefinition> properties;

        /**
         * 属性定义
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PropertyDefinition {
            /**
             * 属性名称
             */
            private String name;

            /**
             * 属性标签
             */
            private String label;

            /**
             * 属性类型
             */
            private String type;

            /**
             * 是否必填
             */
            private Boolean required;

            /**
             * 默认值
             */
            private Object defaultValue;

            /**
             * 可选值
             */
            private List<PropertyValue> options;

            /**
             * 帮助文本
             */
            private String helpText;
        }

        /**
         * 属性值
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PropertyValue {
            /**
             * 值
             */
            private Object value;

            /**
             * 标签
             */
            private String label;
        }
    }
}
