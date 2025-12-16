package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 退款操作记录视图对象
 * <p>
 * 用于记录退款处理过程中的操作历史
 * 包括申请、审批、处理等各个环节
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Schema(description = "退款操作记录视图对象")
public class RefundOperationVO {

    @Schema(description = "操作ID", example = "1")
    private Long operationId;

    @Schema(description = "退款ID", example = "1")
    private Long refundId;

    @Schema(description = "操作类型：1-申请，2-审批，3-处理，4-取消", example = "1")
    private Integer operationType;

    @Schema(description = "操作类型名称", example = "申请")
    private String operationTypeName;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "张三")
    private String operatorName;

    @Schema(description = "操作时间", example = "2025-12-09T10:30:00")
    private LocalDateTime operationTime;

    @Schema(description = "操作结果：1-成功，2-失败", example = "1")
    private Integer operationResult;

    @Schema(description = "操作结果名称", example = "成功")
    private String operationResultName;

    @Schema(description = "操作说明", example = "提交退款申请")
    private String operationRemark;

    @Schema(description = "操作前状态", example = "无")
    private String beforeStatus;

    @Schema(description = "操作后状态", example = "待审批")
    private String afterStatus;
}


