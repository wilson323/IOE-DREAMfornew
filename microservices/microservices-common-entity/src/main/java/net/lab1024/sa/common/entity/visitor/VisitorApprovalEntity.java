package net.lab1024.sa.common.entity.visitor;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 访客审批流程实体类
 * <p>
 * 存储访客登记的审批流程信息
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * 字段数量: 5个 (符合≤30字段标准)
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_approval")
@Schema(description = "访客审批流程实体")
public class VisitorApprovalEntity extends BaseEntity {

    /**
     * 审批ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "审批ID")
    private Long approvalId;

    /**
     * 关联的自助登记ID（外键）
     */
    @TableField("registration_id")
    @Schema(description = "自助登记ID")
    private Long registrationId;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    @Schema(description = "审批人ID")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @TableField("approver_name")
    @Schema(description = "审批人姓名")
    private String approverName;

    /**
     * 审批时间
     */
    @TableField("approval_time")
    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    @TableField("approval_comment")
    @Schema(description = "审批意见")
    private String approvalComment;
}
