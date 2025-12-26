package net.lab1024.sa.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则测试历史视图对象
 * <p>
 * 用于展示规则测试历史列表
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
@Schema(description = "规则测试历史视图对象")
public class RuleTestHistoryVO {

    /**
     * 历史ID
     */
    @Schema(description = "历史ID", example = "1234567890123456789")
    private Long historyId;

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
    @Schema(description = "规则名称", example = "迟到扣款规则")
    private String ruleName;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testTimestamp;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01T08:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
