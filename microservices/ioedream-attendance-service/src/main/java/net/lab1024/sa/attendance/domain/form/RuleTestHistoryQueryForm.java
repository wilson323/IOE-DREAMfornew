package net.lab1024.sa.attendance.domain.form;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则测试历史查询表单
 * <p>
 * 用于查询规则测试历史记录
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
@Schema(description = "规则测试历史查询表单")
public class RuleTestHistoryQueryForm {

    /**
     * 规则ID
     */
    @Schema(description = "规则ID", example = "1234567890")
    private Long ruleId;

    /**
     * 规则名称（模糊查询）
     */
    @Schema(description = "规则名称", example = "迟到扣款")
    private String ruleName;

    /**
     * 测试用户ID
     */
    @Schema(description = "测试用户ID", example = "1001")
    private Long testUserId;

    /**
     * 测试用户名（模糊查询）
     */
    @Schema(description = "测试用户名", example = "admin")
    private String testUserName;

    /**
     * 测试结果：MATCH-匹配 NOT_MATCH-不匹配 ERROR-错误
     */
    @Schema(description = "测试结果", example = "MATCH")
    private String testResult;

    /**
     * 测试场景
     */
    @Schema(description = "测试场景", example = "LATE")
    private String testScenario;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2024-01-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2024-12-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
