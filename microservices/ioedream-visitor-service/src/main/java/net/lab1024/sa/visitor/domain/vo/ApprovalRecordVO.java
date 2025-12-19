package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批记录VO
 * <p>
 * 内存优化设计：
 * - 精简字段，只返回必要信息
 * - 使用基本数据类型，减少对象开销
 * - 避免冗余字段，按需加载
 * - 合理的数据类型选择
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审批记录VO")
public class ApprovalRecordVO {

    /**
     * 审批记录ID
     */
    @Schema(description = "审批记录ID", example = "1001")
    private Long approvalId;

    /**
     * 审批人ID
     */
    @Schema(description = "审批人ID", example = "2001")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Schema(description = "审批人姓名", example = "张三")
    private String approverName;

    /**
     * 审批级别
     */
    @Schema(description = "审批级别", example = "1")
    private Integer approvalLevel;

    /**
     * 审批级别名称
     */
    @Schema(description = "审批级别名称", example = "一级审批")
    private String approvalLevelName;

    /**
     * 审批结果
     */
    @Schema(description = "审批结果", example = "APPROVED")
    private String approvalResult;

    /**
     * 审批结果名称
     */
    @Schema(description = "审批结果名称", example = "已通过")
    private String approvalResultName;

    /**
     * 审批意见
     */
    @Schema(description = "审批意见", example = "同意预约，请遵守园区管理规定")
    private String approvalComment;

    /**
     * 审批时间
     */
    @Schema(description = "审批时间", example = "2025-01-30T10:30:00")
    private LocalDateTime approvalTime;

    /**
     * 审批耗时（分钟）
     */
    @Schema(description = "审批耗时（分钟）", example = "30")
    private Integer approvalDuration;
}