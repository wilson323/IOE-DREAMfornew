package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费补贴发放记录实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_subsidy_issue_record")
@Schema(description = "消费补贴发放记录实体")
public class ConsumeSubsidyIssueRecordEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 发放ID
     */
    @TableField("issue_id")
    @Schema(description = "发放ID")
    private Long issueId;
    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    /**
     * 补贴类型
     */
    @TableField("subsidy_type")
    @Schema(description = "补贴类型")
    private String subsidyType;
    /**
     * 补贴金额
     */
    @TableField("subsidy_amount")
    @Schema(description = "补贴金额")
    private java.math.BigDecimal subsidyAmount;
    /**
     * 发放时间
     */
    @TableField("issue_time")
    @Schema(description = "发放时间")
    private java.time.LocalDateTime issueTime;
    /**
     * 发放状态
     */
    @TableField("status")
    @Schema(description = "发放状态")
    private Integer status;
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    /**
     * 补贴账户ID
     */
    @TableField("subsidy_account_id")
    @Schema(description = "补贴账户ID")
    private Long subsidyAccountId;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
