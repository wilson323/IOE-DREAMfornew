package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 访客批量审批VO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@Schema(description = "访客批量审批请求")
public class VisitorBatchApprovalVO {

    @NotEmpty(message = "访客ID列表不能为空")
    @Schema(description = "访客ID列表", example = "[1, 2, 3]", required = true)
    private List<Long> visitorIds;

    @Schema(description = "是否通过", example = "true", required = true)
    private Boolean approved;

    @Schema(description = "审批人ID", example = "1001", required = true)
    private Long approverId;

    @Schema(description = "审批意见", example = "批量审批通过")
    private String approvalComment;
}
