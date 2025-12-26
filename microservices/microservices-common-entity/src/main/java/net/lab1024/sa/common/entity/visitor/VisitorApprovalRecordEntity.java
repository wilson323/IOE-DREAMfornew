package net.lab1024.sa.common.entity.visitor;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客预约审批记录实体
 * <p>
 * 内存优化设计：
 * - 严格控制字段数量(≤15个)
 * - 合理使用VARCHAR长度，避免过度分配
 * - 使用索引优化查询性能
 * - 避免大字段，使用关联表存储详细信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_visitor_approval_record")
@Schema(description = "访客预约审批记录实体")
public class VisitorApprovalRecordEntity {

    /**
     * 审批记录ID（主键）
     */
    @TableId(value = "approval_id", type = IdType.ASSIGN_ID)
    @Schema(description = "审批记录ID", example = "1001")
    private Long approvalId;

    /**
     * 预约ID
     */
    @NotNull(message = "预约ID不能为空")
    @TableField("appointment_id")
    @Schema(description = "预约ID", example = "1001")
    private Long appointmentId;

    /**
     * 审批人ID
     */
    @NotNull(message = "审批人ID不能为空")
    @TableField("approver_id")
    @Schema(description = "审批人ID", example = "2001")
    private Long approverId;

    /**
     * 审批人姓名（冗余存储，减少关联查询）
     */
    @NotBlank(message = "审批人姓名不能为空")
    @Size(max = 50, message = "审批人姓名长度不能超过50个字符")
    @TableField("approver_name")
    @Schema(description = "审批人姓名", example = "张三")
    private String approverName;

    /**
     * 审批级别
     * <p>
     * 1-一级审批（部门经理）
     * 2-二级审批（安全部门）
     * 3-三级审批（高层管理）
     * </p>
     */
    @NotNull(message = "审批级别不能为空")
    @TableField("approval_level")
    @Schema(description = "审批级别", example = "1")
    private Integer approvalLevel;

    /**
     * 审批结果
     * <p>
     * PENDING-待审批
     * APPROVED-已通过
     * REJECTED-已拒绝
     * </p>
     */
    @NotBlank(message = "审批结果不能为空")
    @Size(max = 20, message = "审批结果长度不能超过20个字符")
    @TableField("approval_result")
    @Schema(description = "审批结果", example = "APPROVED")
    private String approvalResult;

    /**
     * 审批意见（限制长度，避免大字段）
     */
    @Size(max = 500, message = "审批意见长度不能超过500个字符")
    @TableField("approval_comment")
    @Schema(description = "审批意见", example = "同意预约，请遵守园区管理规定")
    private String approvalComment;

    /**
     * 审批时间
     */
    @NotNull(message = "审批时间不能为空")
    @TableField("approval_time")
    @Schema(description = "审批时间", example = "2025-01-30T10:30:00")
    private LocalDateTime approvalTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2025-01-30T10:30:00")
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    /**
     * 兼容历史代码：获取访问结束时间
     *
     * <p>
     * 审批记录本身不包含预约的访问时间窗口，该字段用于兼容设备侧“临时访客”流程。
     * 当前以审批时间作为近似返回值，后续应调整为从预约记录（Appointment）中获取真实的访问结束时间。
     * </p>
     *
     * @return 访问结束时间（近似值）
     */
    public LocalDateTime getVisitEndTime() {
        return this.approvalTime;
    }
}
