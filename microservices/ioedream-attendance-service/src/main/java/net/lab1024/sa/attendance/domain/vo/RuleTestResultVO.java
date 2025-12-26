package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规则测试结果视图对象
 * <p>
 * 用于规则测试工具的测试结果响应数据
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
@Schema(description = "规则测试结果视图对象")
public class RuleTestResultVO {

    /**
     * 测试ID
     */
    @Schema(description = "测试ID", example = "test-123456")
    private String testId;

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1234567890")
    private Long ruleId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "迟到规则")
    private String ruleName;

    /**
     * 规则条件
     */
    @Schema(description = "规则条件", example = "{\"lateMinutes\": 5}")
    private String ruleCondition;

    /**
     * 规则动作
     */
    @Schema(description = "规则动作", example = "{\"deductAmount\": 50}")
    private String ruleAction;

    /**
     * 测试结果：MATCH-匹配 NOT_MATCH-不匹配 ERROR-错误
     */
    @Schema(description = "测试结果", example = "MATCH")
    private String testResult;

    /**
     * 测试结果描述
     */
    @Schema(description = "测试结果描述", example = "规则匹配成功")
    private String resultMessage;

    /**
     * 条件评估结果
     */
    @Schema(description = "条件评估结果", example = "true")
    private Boolean conditionMatched;

    /**
     * 执行的动作列表
     */
    @Schema(description = "执行的动作列表")
    private List<ActionExecutionVO> executedActions;

    /**
     * 执行时间（毫秒）
     */
    @Schema(description = "执行时间（毫秒）", example = "150")
    private Long executionTime;

    /**
     * 执行日志
     */
    @Schema(description = "执行日志")
    private List<ExecutionLogVO> executionLogs;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 测试时间戳
     */
    @Schema(description = "测试时间戳", example = "2024-01-01T08:00:00")
    private LocalDateTime testTimestamp;

    /**
     * 测试输入数据
     */
    @Schema(description = "测试输入数据")
    private Map<String, Object> testInputData;

    /**
     * 测试输出数据
     */
    @Schema(description = "测试输出数据")
    private Map<String, Object> testOutputData;

    /**
     * 动作执行结果VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "动作执行结果")
    public static class ActionExecutionVO {

        /**
         * 动作名称
         */
        @Schema(description = "动作名称", example = "deductAmount")
        private String actionName;

        /**
         * 动作值
         */
        @Schema(description = "动作值", example = "50")
        private Object actionValue;

        /**
         * 执行状态：SUCCESS-成功 FAILED-失败
         */
        @Schema(description = "执行状态", example = "SUCCESS")
        private String executionStatus;

        /**
         * 执行消息
         */
        @Schema(description = "执行消息", example = "扣款50元")
        private String executionMessage;

        /**
         * 执行时间戳
         */
        @Schema(description = "执行时间戳")
        private LocalDateTime executionTimestamp;
    }

    /**
     * 执行日志VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "执行日志")
    public static class ExecutionLogVO {

        /**
         * 日志级别：INFO DEBUG WARN ERROR
         */
        @Schema(description = "日志级别", example = "INFO")
        private String logLevel;

        /**
         * 日志消息
         */
        @Schema(description = "日志消息", example = "开始评估规则条件")
        private String logMessage;

        /**
         * 日志时间戳
         */
        @Schema(description = "日志时间戳")
        private LocalDateTime logTimestamp;

        /**
         * 日志数据
         */
        @Schema(description = "日志数据")
        private Map<String, Object> logData;
    }
}
