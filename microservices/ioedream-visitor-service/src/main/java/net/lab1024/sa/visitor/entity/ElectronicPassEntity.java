package net.lab1024.sa.visitor.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 电子出门单实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_visitor_electronic_pass")
public class ElectronicPassEntity {

    /**
     * 出门单ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列pass_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "pass_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 出门单编号
     */
    @TableField("pass_number")
    private String passNumber;

    /**
     * 车辆ID
     */
    @TableField("vehicle_id")
    private Long vehicleId;

    /**
     * 司机ID
     */
    @TableField("driver_id")
    private Long driverId;

    /**
     * 物品清单（JSON格式）
     */
    @TableField("goods_list")
    private String goodsList;

    /**
     * 物品总价值
     */
    @TableField("total_value")
    private BigDecimal totalValue;

    /**
     * 出发地
     */
    @TableField("departure_place")
    private String departurePlace;

    /**
     * 目的地
     */
    @TableField("destination_place")
    private String destinationPlace;

    /**
     * 预计出发时间
     */
    @TableField("scheduled_departure_time")
    private LocalDateTime scheduledDepartureTime;

    /**
     * 实际出发时间
     */
    @TableField("actual_departure_time")
    private LocalDateTime actualDepartureTime;

    /**
     * 预计返回时间
     */
    @TableField("scheduled_return_time")
    private LocalDateTime scheduledReturnTime;

    /**
     * 实际返回时间
     */
    @TableField("actual_return_time")
    private LocalDateTime actualReturnTime;

    /**
     * 申请人ID
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @TableField("applicant_name")
    private String applicantName;

    /**
     * 审批人ID
     */
    @TableField("approver_id")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @TableField("approver_name")
    private String approverName;

    /**
     * 审批状态（0-待审批 1-已同意 2-已拒绝）
     */
    @TableField("approval_status")
    private Integer approvalStatus;

    /**
     * 审批时间
     */
    @TableField("approval_time")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 出门单状态（1-已创建 2-已审批 3-已出门 4-已返回 5-已取消）
     */
    @TableField("pass_status")
    private Integer passStatus;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    /**
     * 删除标记（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    /**
     * 兼容历史代码：获取出门单ID
     *
     * <p>部分历史逻辑使用 passId 概念，这里提供兼容方法，等价于 {@link #getId()}。</p>
     *
     * @return 出门单ID
     */
    public Long getPassId() {
        return this.id;
    }

    /**
     * 兼容历史代码：获取出门单状态
     *
     * <p>部分历史逻辑使用 status 概念，这里提供兼容方法，等价于 {@link #getPassStatus()}。</p>
     *
     * @return 出门单状态
     */
    public Integer getStatus() {
        return this.passStatus;
    }

    /**
     * 兼容历史代码：获取过期时间
     *
     * <p>开放API/设备验证场景中通常需要“有效期”概念，这里使用预计返回时间作为过期时间的近似值。</p>
     *
     * @return 过期时间（可能为空）
     */
    public LocalDateTime getExpireTime() {
        return this.scheduledReturnTime;
    }
}

