package net.lab1024.sa.common.visitor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 物流预约实体
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯数据模型，无业务逻辑方法
 * - Entity≤200行，理想≤100行
 * - 包含数据字段、基础注解、构造方法
 * - 无static方法，无业务计算逻辑
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0 (重构版)
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_logistics_reservation")
@Schema(description = "物流预约实体")
public class LogisticsReservationEntity extends BaseEntity {

    /**
     * 预约ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "预约ID", example = "1689854272000000001")
    private Long reservationId;

    /**
     * 预约编号
     */
    @TableField("reservation_code")
    @Schema(description = "预约编号", example = "LR20251216000001")
    private String reservationCode;

    /**
     * 司机ID
     */
    @TableField("driver_id")
    @Schema(description = "司机ID", example = "1001")
    private Long driverId;

    /**
     * 司机姓名
     */
    @NotBlank(message = "司机姓名不能为空")
    @Size(max = 100, message = "司机姓名长度不能超过100个字符")
    @TableField("driver_name")
    @Schema(description = "司机姓名", example = "张师傅")
    private String driverName;

    /**
     * 车牌号
     */
    @NotBlank(message = "车牌号不能为空")
    @Size(max = 20, message = "车牌号长度不能超过20个字符")
    @TableField("plate_number")
    @Schema(description = "车牌号", example = "京A12345")
    private String plateNumber;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    @TableField("contact_phone")
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    /**
     * 预约类型
     */
    @NotBlank(message = "预约类型不能为空")
    @Size(max = 20, message = "预约类型长度不能超过20个字符")
    @TableField("reservation_type")
    @Schema(description = "预约类型", example = "DELIVERY")
    private String reservationType;

    /**
     * 货物类型
     */
    @Size(max = 50, message = "货物类型长度不能超过50个字符")
    @TableField("goods_type")
    @Schema(description = "货物类型", example = "原材料")
    private String goodsType;

    /**
     * 货物重量（吨）
     */
    @DecimalMin(value = "0.0", message = "货物重量不能为负数")
    @TableField("goods_weight")
    @Schema(description = "货物重量（吨）", example = "10.5")
    private BigDecimal goodsWeight;

    /**
     * 货物体积（立方米）
     */
    @DecimalMin(value = "0.0", message = "货物体积不能为负数")
    @TableField("goods_volume")
    @Schema(description = "货物体积（立方米）", example = "5.2")
    private BigDecimal goodsVolume;

    /**
     * 预计到达日期
     */
    @NotNull(message = "预计到达日期不能为空")
    @TableField("expected_arrive_date")
    @Schema(description = "预计到达日期", example = "2025-12-16")
    private LocalDate expectedArriveDate;

    /**
     * 预计到达开始时间
     */
    @TableField("expected_arrive_time_start")
    @Schema(description = "预计到达开始时间", example = "08:00")
    private LocalTime expectedArriveTimeStart;

    /**
     * 预计到达结束时间
     */
    @TableField("expected_arrive_time_end")
    @Schema(description = "预计到达结束时间", example = "12:00")
    private LocalTime expectedArriveTimeEnd;

    /**
     * 作业类型
     */
    @TableField("operation_type")
    @Schema(description = "作业类型", example = "UNLOADING")
    private String operationType;

    /**
     * 作业区域ID
     */
    @TableField("operation_area_id")
    @Schema(description = "作业区域ID", example = "1001")
    private Long operationAreaId;

    /**
     * 被访人ID
     */
    @TableField("interviewee_id")
    @Schema(description = "被访人ID", example = "1001")
    private Long intervieweeId;

    /**
     * 预约状态
     */
    @NotBlank(message = "预约状态不能为空")
    @Size(max = 20, message = "预约状态长度不能超过20个字符")
    @TableField("status")
    @Schema(description = "预约状态", example = "PENDING")
    private String status;

    /**
     * 审批人
     */
    @Size(max = 100, message = "审批人长度不能超过100个字符")
    @TableField("approve_user")
    @Schema(description = "审批人", example = "管理员")
    private String approveUser;

    /**
     * 审批时间
     */
    @TableField("approve_time")
    @Schema(description = "审批时间", example = "2025-12-16T10:30:00")
    private LocalDateTime approveTime;

    /**
     * 扩展属性（JSON格式）
     */
    @Size(max = 2000, message = "扩展属性长度不能超过2000个字符")
    @TableField("extended_attributes")
    @Schema(description = "扩展属性", example = "{\"special_requirements\":\"温控\"}")
    private String extendedAttributes;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @TableField("remark")
    @Schema(description = "备注", example = "重要货物运输")
    private String remark;

    // ==================== 业务状态常量 ====================

    /**
     * 预约类型常量
     */
    public static class ReservationType {
        public static final String DELIVERY = "DELIVERY";  // 送货
        public static final String PICKUP = "PICKUP";       // 提货
        public static final String TRANSFER = "TRANSFER";    // 转运
    }

    /**
     * 作业类型常量
     */
    public static class OperationType {
        public static final String LOADING = "LOADING";     // 装载
        public static final String UNLOADING = "UNLOADING"; // 卸货
        public static final String BOTH = "BOTH";           // 装卸
    }

    /**
     * 预约状态常量
     */
    public static class Status {
        public static final String PENDING = "PENDING";     // 待审核
        public static final String APPROVED = "APPROVED";   // 已通过
        public static final String REJECTED = "REJECTED";   // 已拒绝
        public static final String CANCELLED = "CANCELLED";  // 已取消
        public static final String COMPLETED = "COMPLETED"; // 已完成
        public static final String EXPIRED = "EXPIRED";     // 已过期
    }
}