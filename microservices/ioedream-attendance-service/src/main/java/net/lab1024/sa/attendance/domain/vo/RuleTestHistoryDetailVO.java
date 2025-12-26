package net.lab1024.sa.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规则测试历史详情视图对象
 * <p>
 * 包含完整的测试历史详细信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder(builderMethodName = "detailBuilder")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "规则测试历史详情视图对象")
public class RuleTestHistoryDetailVO extends RuleTestHistoryVO {

    /**
     * 测试ID（重新声明以支持Builder）
     */
    @Schema(description = "测试ID", example = "test-123456")
    private String testId;

    /**
     * 历史ID（重新声明以支持Builder）
     */
    @Schema(description = "历史ID", example = "1234567890123456789")
    private Long historyId;

    /**
     * 规则ID（重新声明以支持Builder）
     */
    @Schema(description = "规则ID", example = "1234567890")
    private Long ruleId;

    /**
     * 规则名称（重新声明以支持Builder）
     */
    @Schema(description = "规则名称", example = "迟到扣款规则")
    private String ruleName;

    /**
     * 测试结果（重新声明以支持Builder）
     */
    @Schema(description = "测试结果", example = "MATCH")
    private String testResult;

    /**
     * 测试结果描述（重新声明以支持Builder）
     */
    @Schema(description = "测试结果描述", example = "规则匹配成功")
    private String resultMessage;

    /**
     * 条件是否匹配（重新声明以支持Builder）
     */
    @Schema(description = "条件是否匹配", example = "true")
    private Boolean conditionMatched;

    /**
     * 执行时间（重新声明以支持Builder）
     */
    @Schema(description = "执行时间（毫秒）", example = "150")
    private Long executionTime;

    /**
     * 测试场景（重新声明以支持Builder）
     */
    @Schema(description = "测试场景", example = "LATE")
    private String testScenario;

    /**
     * 测试用户ID（重新声明以支持Builder）
     */
    @Schema(description = "测试用户ID", example = "1001")
    private Long testUserId;

    /**
     * 测试用户名（重新声明以支持Builder）
     */
    @Schema(description = "测试用户名", example = "admin")
    private String testUserName;

    /**
     * 测试时间戳（重新声明以支持Builder）
     */
    @Schema(description = "测试时间戳", example = "2024-01-01T08:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testTimestamp;

    /**
     * 创建时间（重新声明以支持Builder）
     */
    @Schema(description = "创建时间", example = "2024-01-01T08:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

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
     * 执行的动作列表
     */
    @Schema(description = "执行的动作列表")
    private List<RuleTestResultVO.ActionExecutionVO> executedActions;

    /**
     * 执行日志列表
     */
    @Schema(description = "执行日志列表")
    private List<RuleTestResultVO.ExecutionLogVO> executionLogs;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

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
}
