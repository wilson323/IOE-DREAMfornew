package net.lab1024.sa.attendance.rule.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考勤规则配置
 * <p>
 * 封装考勤规则的配置信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRuleConfig {

    /**
     * 规则ID
     */
    private String ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则描述
     */
    private String ruleDescription;

    /**
     * 规则类型
     */
    private RuleType ruleType;

    /**
     * 规则类别
     */
    private RuleCategory ruleCategory;

    /**
     * 规则优先级
     */
    private Integer priority;

    /**
     * 规则状态
     */
    private RuleStatus status;

    /**
     * 适用范围
     */
    private RuleScope scope;

    /**
     * 适用部门ID列表
     */
    private List<Long> departmentIds;

    /**
     * 适用员工ID列表
     */
    private List<Long> employeeIds;

    /**
     * 适用岗位ID列表
     */
    private List<Long> positionIds;

    /**
     * 适用班次ID列表
     */
    private List<String> shiftIds;

    /**
     * 规则条件
     */
    private List<RuleCondition> conditions;

    /**
     * 规则动作
     */
    private List<RuleAction> actions;

    /**
     * 执行时机
     */
    private ExecutionTiming executionTiming;

    /**
     * 生效日期
     */
    private String effectiveDate;

    /**
     * 失效日期
     */
    private String expiryDate;

    /**
     * 执行频率
     */
    private ExecutionFrequency executionFrequency;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 是否为系统规则
     */
    private Boolean isSystemRule;

    /**
     * 规则版本
     */
    private String version;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    private java.time.LocalDateTime createTime;

    /**
     * 更新时间
     */
    private java.time.LocalDateTime updateTime;

    /**
     * 最后执行时间
     */
    private java.time.LocalDateTime lastExecutionTime;

    /**
     * 执行次数
     */
    private Long executionCount;

    /**
     * 成功执行次数
     */
    private Long successCount;

    /**
     * 失败执行次数
     */
    private Long failureCount;

    /**
     * 平均执行时间（毫秒）
     */
    private Double averageExecutionTimeMs;

    /**
     * 规则参数
     */
    private Map<String, Object> parameters;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 规则类型枚举
     */
    public enum RuleType {
        TIME_BASED("时间规则"),
        ATTENDANCE_BASED("考勤规则"),
        BEHAVIOR_BASED("行为规则"),
        PERFORMANCE_BASED("绩效规则"),
        COMPLIANCE_BASED("合规规则"),
        CUSTOM("自定义规则");

        private final String description;

        RuleType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 规则类别枚举
     */
    public enum RuleCategory {
        CLOCK_IN_RULES("打卡规则"),
        CLOCK_OUT_RULES("下班打卡规则"),
        LATE_RULES("迟到规则"),
        EARLY_LEAVE_RULES("早退规则"),
        ABSENCE_RULES("缺勤规则"),
        OVERTIME_RULES("加班规则"),
        LEAVE_RULES("请假规则"),
        HOLIDAY_RULES("节假日规则"),
        SCHEDULE_RULES("排班规则"),
        EXCEPTION_RULES("异常处理规则");

        private final String description;

        RuleCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 规则状态枚举
     */
    public enum RuleStatus {
        DRAFT("草稿"),
        ACTIVE("启用"),
        INACTIVE("禁用"),
        TESTING("测试中"),
        DEPRECATED("已废弃");

        private final String description;

        RuleStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 规则范围枚举
     */
    public enum RuleScope {
        GLOBAL("全局"),
        DEPARTMENT("部门"),
        EMPLOYEE("员工"),
        POSITION("岗位"),
        SHIFT("班次"),
        CUSTOM("自定义");

        private final String description;

        RuleScope(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 执行时机枚举
     */
    public enum ExecutionTiming {
        BEFORE_CLOCK_IN("上班打卡前"),
        AFTER_CLOCK_IN("上班打卡后"),
        BEFORE_CLOCK_OUT("下班打卡前"),
        AFTER_CLOCK_OUT("下班打卡后"),
        DAILY_CALCULATION("每日计算时"),
        WEEKLY_CALCULATION("每周计算时"),
        MONTHLY_CALCULATION("每月计算时"),
        REAL_TIME("实时"),
        SCHEDULED("定时"),
        MANUAL("手动");

        private final String description;

        ExecutionTiming(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 执行频率枚举
     */
    public enum ExecutionFrequency {
        ONCE("仅一次"),
        EVERY_TIME("每次触发"),
        DAILY("每天"),
        WEEKLY("每周"),
        MONTHLY("每月"),
        QUARTERLY("每季度"),
        YEARLY("每年"),
        CUSTOM_INTERVAL("自定义间隔");

        private final String description;

        ExecutionFrequency(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 规则条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleCondition {
        /**
         * 条件ID
         */
        private String conditionId;

        /**
         * 条件类型
         */
        private ConditionType conditionType;

        /**
         * 字段名称
         */
        private String fieldName;

        /**
         * 操作符
         */
        private OperatorType operator;

        /**
         * 比较值
         */
        private Object compareValue;

        /**
         * 逻辑连接符
         */
        private LogicalOperator logicalOperator;

        /**
         * 条件参数
         */
        private Map<String, Object> parameters;

        /**
         * 是否启用
         */
        private Boolean enabled;
    }

    /**
     * 条件类型枚举
     */
    public enum ConditionType {
        TIME("时间条件"),
        DATE("日期条件"),
        EMPLOYEE("员工条件"),
        DEPARTMENT("部门条件"),
        POSITION("岗位条件"),
        SHIFT("班次条件"),
        ATTENDANCE("考勤条件"),
        WORK_HOURS("工时条件"),
        LOCATION("位置条件"),
        DEVICE("设备条件"),
        CUSTOM("自定义条件");

        private final String description;

        ConditionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 操作符类型枚举
     */
    public enum OperatorType {
        EQUALS("等于"),
        NOT_EQUALS("不等于"),
        GREATER_THAN("大于"),
        LESS_THAN("小于"),
        GREATER_THAN_OR_EQUAL("大于等于"),
        LESS_THAN_OR_EQUAL("小于等于"),
        CONTAINS("包含"),
        NOT_CONTAINS("不包含"),
        STARTS_WITH("开始于"),
        ENDS_WITH("结束于"),
        IN("在列表中"),
        NOT_IN("不在列表中"),
        BETWEEN("在范围内"),
        NOT_BETWEEN("不在范围内"),
        IS_NULL("为空"),
        IS_NOT_NULL("不为空");

        private final String description;

        OperatorType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 逻辑操作符枚举
     */
    public enum LogicalOperator {
        AND("并且"),
        OR("或者"),
        NOT("非"),
        XOR("异或");

        private final String description;

        LogicalOperator(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 规则动作
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleAction {
        /**
         * 动作ID
         */
        private String actionId;

        /**
         * 动作类型
         */
        private ActionType actionType;

        /**
         * 动作参数
         */
        private Map<String, Object> parameters;

        /**
         * 执行顺序
         */
        private Integer executionOrder;

        /**
         * 是否必须执行成功
         */
        private Boolean required;

        /**
         * 失败时的处理方式
         */
        private FailureHandling failureHandling;

        /**
         * 重试次数
         */
        private Integer retryCount;

        /**
         * 重试间隔（秒）
         */
        private Integer retryIntervalSeconds;

        /**
         * 是否启用
         */
        private Boolean enabled;
    }

    /**
     * 动作类型枚举
     */
    public enum ActionType {
        CALCULATION("计算动作"),
        VALIDATION("验证动作"),
        NOTIFICATION("通知动作"),
        ALERT("预警动作"),
        UPDATE("更新动作"),
        INSERT("插入动作"),
        DELETE("删除动作"),
        LOG("日志动作"),
        EMAIL("邮件动作"),
        SMS("短信动作"),
        WEBHOOK("Webhook动作"),
        SCRIPT("脚本动作"),
        CUSTOM("自定义动作");

        private final String description;

        ActionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 失败处理方式枚举
     */
    public enum FailureHandling {
        IGNORE("忽略"),
        LOG("记录日志"),
        ROLLBACK("回滚"),
        CONTINUE("继续执行"),
        ABORT("终止执行"),
        RETRY("重试");

        private final String description;

        FailureHandling(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 验证规则配置是否有效
     */
    public boolean isValid() {
        if (ruleName == null || ruleName.trim().isEmpty()) {
            return false;
        }

        if (ruleType == null || ruleCategory == null) {
            return false;
        }

        if (priority == null || priority < 0) {
            return false;
        }

        if (scope == null) {
            return false;
        }

        if (conditions != null && !conditions.isEmpty()) {
            for (RuleCondition condition : conditions) {
                if (condition.getConditionType() == null || condition.getOperator() == null) {
                    return false;
                }
            }
        }

        if (actions != null && !actions.isEmpty()) {
            for (RuleAction action : actions) {
                if (action.getActionType() == null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 获取规则执行成功率
     */
    public Double getSuccessRate() {
        if (executionCount == null || executionCount == 0) {
            return 0.0;
        }

        long successCountValue = successCount != null ? successCount : 0;
        return (double) successCountValue / executionCount * 100;
    }

    /**
     * 获取规则描述摘要
     */
    public String getRuleSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append(ruleName != null ? ruleName : "未命名规则");
        summary.append(" - ");
        summary.append(ruleType != null ? ruleType.getDescription() : "未知类型");

        if (ruleCategory != null) {
            summary.append(" (");
            summary.append(ruleCategory.getDescription());
            summary.append(")");
        }

        if (priority != null) {
            summary.append(" - 优先级: ");
            summary.append(priority);
        }

        if (enabled != null && Boolean.FALSE.equals(enabled)) {
            summary.append(" [已禁用]");
        }

        return summary.toString();
    }

    /**
     * 检查规则是否适用于指定条件
     */
    public boolean isApplicableTo(Long employeeId, Long departmentId, String shiftId) {
        if (scope == RuleScope.GLOBAL) {
            return true;
        }

        if (scope == RuleScope.EMPLOYEE && employeeIds != null) {
            return employeeIds.contains(employeeId);
        }

        if (scope == RuleScope.DEPARTMENT && departmentIds != null) {
            return departmentIds.contains(departmentId);
        }

        if (scope == RuleScope.SHIFT && shiftIds != null) {
            return shiftIds.contains(shiftId);
        }

        return false;
    }

    /**
     * 获取规则复杂度评分
     */
    public Integer getComplexityScore() {
        int score = 0;

        // 条件复杂度
        if (conditions != null) {
            score += conditions.size() * 10;
            for (RuleCondition condition : conditions) {
                if (condition.getParameters() != null) {
                    score += condition.getParameters().size() * 5;
                }
            }
        }

        // 动作复杂度
        if (actions != null) {
            score += actions.size() * 15;
            for (RuleAction action : actions) {
                if (action.getParameters() != null) {
                    score += action.getParameters().size() * 8;
                }
                if (action.getRetryCount() != null && action.getRetryCount() > 0) {
                    score += 10;
                }
            }
        }

        // 其他因素
        if (parameters != null) {
            score += parameters.size() * 5;
        }

        return score;
    }

    /**
     * 估算规则执行时间（毫秒）
     */
    public Long estimateExecutionTime() {
        long baseTime = 50; // 基础执行时间

        // 条件评估时间
        if (conditions != null) {
            baseTime += conditions.size() * 20;
        }

        // 动作执行时间
        if (actions != null) {
            for (RuleAction action : actions) {
                switch (action.getActionType()) {
                    case CALCULATION:
                        baseTime += 100;
                        break;
                    case VALIDATION:
                        baseTime += 30;
                        break;
                    case NOTIFICATION:
                        baseTime += 50;
                        break;
                    case UPDATE:
                    case INSERT:
                    case DELETE:
                        baseTime += 80;
                        break;
                    case EMAIL:
                        baseTime += 500;
                        break;
                    case SMS:
                        baseTime += 300;
                        break;
                    case WEBHOOK:
                        baseTime += 200;
                        break;
                    case SCRIPT:
                        baseTime += 1000;
                        break;
                    default:
                        baseTime += 20;
                        break;
                }
            }
        }

        return baseTime;
    }

    /**
     * 克隆规则配置
     */
    public AttendanceRuleConfig clone() {
        AttendanceRuleConfig cloned = AttendanceRuleConfig.builder()
                .ruleName(this.ruleName + " - 副本")
                .ruleDescription(this.ruleDescription)
                .ruleType(this.ruleType)
                .ruleCategory(this.ruleCategory)
                .priority(this.priority)
                .status(RuleStatus.DRAFT)
                .scope(this.scope)
                .departmentIds(this.departmentIds != null ? new ArrayList<>(this.departmentIds) : null)
                .employeeIds(this.employeeIds != null ? new ArrayList<>(this.employeeIds) : null)
                .positionIds(this.positionIds != null ? new ArrayList<>(this.positionIds) : null)
                .shiftIds(this.shiftIds != null ? new ArrayList<>(this.shiftIds) : null)
                .executionTiming(this.executionTiming)
                .effectiveDate(this.effectiveDate)
                .expiryDate(this.expiryDate)
                .executionFrequency(this.executionFrequency)
                .enabled(true)
                .isSystemRule(false)
                .version("1.0.0")
                .parameters(this.parameters != null ? new HashMap<>(this.parameters) : null)
                .extendedAttributes(this.extendedAttributes != null ? new HashMap<>(this.extendedAttributes) : null)
                .build();

        // 深拷贝条件和动作
        if (this.conditions != null) {
            cloned.setConditions(this.conditions.stream()
                    .map(condition -> RuleCondition.builder()
                            .conditionType(condition.getConditionType())
                            .fieldName(condition.getFieldName())
                            .operator(condition.getOperator())
                            .compareValue(condition.getCompareValue())
                            .logicalOperator(condition.getLogicalOperator())
                            .parameters(condition.getParameters() != null ? new HashMap<>(condition.getParameters()) : null)
                            .enabled(condition.getEnabled())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (this.actions != null) {
            cloned.setActions(this.actions.stream()
                    .map(action -> RuleAction.builder()
                            .actionType(action.getActionType())
                            .parameters(action.getParameters() != null ? new HashMap<>(action.getParameters()) : null)
                            .executionOrder(action.getExecutionOrder())
                            .required(action.getRequired())
                            .failureHandling(action.getFailureHandling())
                            .retryCount(action.getRetryCount())
                            .retryIntervalSeconds(action.getRetryIntervalSeconds())
                            .enabled(action.getEnabled())
                            .build())
                    .collect(Collectors.toList()));
        }

        return cloned;
    }
}