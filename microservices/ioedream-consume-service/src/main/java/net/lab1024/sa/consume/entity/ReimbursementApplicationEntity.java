package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 报销申请实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_reimbursement_application")
@Schema(description = "报销申请实体")
public class ReimbursementApplicationEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 申请ID
     */
    @TableField("application_id")
    @Schema(description = "申请ID")
    private Long applicationId;
    /**
     * 申请编号
     */
    @TableField("application_no")
    @Schema(description = "申请编号")
    private String applicationNo;
    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    /**
     * 总金额
     */
    @TableField("total_amount")
    @Schema(description = "总金额")
    private java.math.BigDecimal totalAmount;
    /**
     * 报销类型
     */
    @TableField("reimbursement_type")
    @Schema(description = "报销类型")
    private String reimbursementType;
    /**
     * 申请状态
     */
    @TableField("status")
    @Schema(description = "申请状态")
    private Integer status;
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
