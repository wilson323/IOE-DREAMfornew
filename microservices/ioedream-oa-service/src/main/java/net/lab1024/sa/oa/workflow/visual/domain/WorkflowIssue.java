package net.lab1024.sa.oa.workflow.visual.domain;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工作流问题类
 * <p>
 * 封装工作流运行或配置过程中的问题和异常
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowIssue {

    /**
     * 问题ID
     */
    private String issueId;

    /**
     * 问题类型
     * CONFIG_ERROR - 配置错误
     * RUNTIME_ERROR - 运行时错误
     * PERFORMANCE_ISSUE - 性能问题
     * SECURITY_ISSUE - 安全问题
     * COMPATIBILITY_ISSUE - 兼容性问题
     */
    private IssueType issueType;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 问题详情
     */
    private String detail;

    /**
     * 严重程度：CRITICAL-严重 HIGH-高 MEDIUM-中 LOW-低
     */
    private Severity severity;

    /**
     * 问题状态：OPEN-打开 IN_PROGRESS-进行中 RESOLVED-已解决 CLOSED-已关闭
     */
    private IssueStatus status;

    /**
     * 影响范围
     */
    private String impactScope;

    /**
     * 解决建议
     */
    private String suggestion;

    /**
     * 解决方案
     */
    private String solution;

    /**
     * 问题来源
     */
    private String source;

    /**
     * 问题位置
     */
    private String location;

    /**
     * 关联的流程定义ID
     */
    private String processDefinitionId;

    /**
     * 关联的流程实例ID
     */
    private String processInstanceId;

    /**
     * 关联的任务ID
     */
    private String taskId;

    /**
     * 关联的节点ID
     */
    private String nodeId;

    /**
     * 异常堆栈
     */
    private String exceptionStack;

    /**
     * 发生时间
     */
    private LocalDateTime occurTime;

    /**
     * 发现时间
     */
    private LocalDateTime discoveredTime;

    /**
     * 报告人ID
     */
    private Long reporterUserId;

    /**
     * 报告人姓名
     */
    private String reporterUserName;

    /**
     * 处理人ID
     */
    private Long assigneeUserId;

    /**
     * 处理人姓名
     */
    private String assigneeUserName;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 问题类型枚举
     */
    public enum IssueType {
        /**
         * 配置错误
         */
        CONFIG_ERROR,

        /**
         * 运行时错误
         */
        RUNTIME_ERROR,

        /**
         * 性能问题
         */
        PERFORMANCE_ISSUE,

        /**
         * 安全问题
         */
        SECURITY_ISSUE,

        /**
         * 兼容性问题
         */
        COMPATIBILITY_ISSUE,

        /**
         * 逻辑问题
         */
        LOGIC_ISSUE,

        /**
         * 数据问题
         */
        DATA_ISSUE,

        /**
         * 权限问题
         */
        PERMISSION_ISSUE
    }

    /**
     * 严重程度枚举
     */
    public enum Severity {
        /**
         * 严重
         */
        CRITICAL,

        /**
         * 高
         */
        HIGH,

        /**
         * 中
         */
        MEDIUM,

        /**
         * 低
         */
        LOW,

        /**
         * 信息
         */
        INFO
    }

    /**
     * 问题状态枚举
     */
    public enum IssueStatus {
        /**
         * 打开
         */
        OPEN,

        /**
         * 进行中
         */
        IN_PROGRESS,

        /**
         * 已解决
         */
        RESOLVED,

        /**
         * 已关闭
         */
        CLOSED,

        /**
         * 忽略
         */
        IGNORED,

        /**
         * 待验证
         */
        PENDING_VERIFICATION
    }

    /**
     * 创建严重错误
     */
    public static WorkflowIssue criticalError(String title, String description, String location) {
        return WorkflowIssue.builder()
                .issueId(generateIssueId())
                .issueType(IssueType.RUNTIME_ERROR)
                .title(title)
                .description(description)
                .severity(Severity.CRITICAL)
                .status(IssueStatus.OPEN)
                .location(location)
                .occurTime(LocalDateTime.now())
                .discoveredTime(LocalDateTime.now())
                .priority(1)
                .build();
    }

    /**
     * 创建配置错误
     */
    public static WorkflowIssue configError(String title, String description, String location) {
        return WorkflowIssue.builder()
                .issueId(generateIssueId())
                .issueType(IssueType.CONFIG_ERROR)
                .title(title)
                .description(description)
                .severity(Severity.HIGH)
                .status(IssueStatus.OPEN)
                .location(location)
                .occurTime(LocalDateTime.now())
                .discoveredTime(LocalDateTime.now())
                .priority(2)
                .build();
    }

    /**
     * 生成问题ID
     */
    private static String generateIssueId() {
        return "ISSUE-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    /**
     * 是否需要立即处理
     */
    public boolean requiresImmediateAction() {
        return severity == Severity.CRITICAL || (severity == Severity.HIGH && status == IssueStatus.OPEN);
    }

    /**
     * 是否已解决
     */
    public boolean isResolved() {
        return status == IssueStatus.RESOLVED || status == IssueStatus.CLOSED;
    }

    /**
     * 是否可以关闭
     */
    public boolean canClose() {
        return status == IssueStatus.RESOLVED;
    }
}
