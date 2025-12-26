package net.lab1024.sa.common.entity.attendance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 规则测试历史实体类
 * <p>
 * 记录规则测试的执行历史，用于回溯和对比分析
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
@EqualsAndHashCode
@TableName("t_attendance_rule_test_history")
@Schema(description = "规则测试历史实体")
public class RuleTestHistoryEntity {

    /**
     * 测试历史ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "测试历史ID", example = "1234567890123456789")
    private Long historyId;

    /**
     * 测试ID
     */
    @Schema(description = "测试ID", example = "test-123456")
    private String testId;

    /**
     * 规则ID（测试已有规则时使用）
     */
    @Schema(description = "规则ID", example = "1234567890")
    private Long ruleId;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称", example = "迟到扣款规则")
    private String ruleName;

    /**
     * 规则条件（JSON格式）
     */
    @Schema(description = "规则条件（JSON）", example = "{\"lateMinutes\": 5}")
    private String ruleCondition;

    /**
     * 规则动作（JSON格式）
     */
    @Schema(description = "规则动作（JSON）", example = "{\"deductAmount\": 50}")
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
     * 条件是否匹配
     */
    @Schema(description = "条件是否匹配", example = "true")
    private Boolean conditionMatched;

    /**
     * 执行时间（毫秒）
     */
    @Schema(description = "执行时间（毫秒）", example = "150")
    private Long executionTime;

    /**
     * 执行的动作列表（JSON格式）
     */
    @Schema(description = "执行的动作列表（JSON）")
    private String executedActions;

    /**
     * 执行日志（JSON格式）
     */
    @Schema(description = "执行日志（JSON）")
    private String executionLogs;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 测试输入数据（JSON格式）
     */
    @Schema(description = "测试输入数据（JSON）")
    private String testInputData;

    /**
     * 测试输出数据（JSON格式）
     */
    @Schema(description = "测试输出数据（JSON）")
    private String testOutputData;

    /**
     * 测试场景
     */
    @Schema(description = "测试场景", example = "LATE")
    private String testScenario;

    /**
     * 测试用户ID
     */
    @Schema(description = "测试用户ID", example = "1001")
    private Long testUserId;

    /**
     * 测试用户名
     */
    @Schema(description = "测试用户名", example = "admin")
    private String testUserName;

    /**
     * 测试时间戳
     */
    @Schema(description = "测试时间戳", example = "2024-01-01T08:35:00")
    private LocalDateTime testTimestamp;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T08:35:00")
    private LocalDateTime createTime;
}
