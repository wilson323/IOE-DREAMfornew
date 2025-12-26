package net.lab1024.sa.common.entity.consume;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.NoArgsConstructor;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 对账记录实体类
 * <p>
 * 记录系统交易与设备交易的对账结果
 * 支持自动对账、手动对账和差异处理
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 每日自动对账
 * - 手动触发对账
 * - 差异记录处理
 * - 对账结果查询
 * - 对账统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@TableName("t_consume_reconciliation_record")
@Schema(description = "对账记录实体")
public class ReconciliationRecordEntity extends BaseEntity {

    /**
     * 对账记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "对账记录ID")
    private Long reconciliationId;

    /**
     * 对账批次号
     */
    @Schema(description = "对账批次号")
    private String batchNo;

    /**
     * 对账类型（1-自动对账 2-手动对账）
     */
    @Schema(description = "对账类型")
    private Integer reconciliationType;

    /**
     * 对账类型名称
     */
    @Schema(description = "对账类型名称")
    private String reconciliationTypeName;

    /**
     * 对账开始日期
     */
    @Schema(description = "对账开始日期")
    private LocalDate startDate;

    /**
     * 对账结束日期
     */
    @Schema(description = "对账结束日期")
    private LocalDate endDate;

    /**
     * 系统交易数量
     */
    @Schema(description = "系统交易数量")
    private Integer systemTransactionCount;

    /**
     * 系统交易总金额
     */
    @Schema(description = "系统交易总金额")
    private BigDecimal systemTotalAmount;

    /**
     * 设备交易数量
     */
    @Schema(description = "设备交易数量")
    private Integer deviceTransactionCount;

    /**
     * 设备交易总金额
     */
    @Schema(description = "设备交易总金额")
    private BigDecimal deviceTotalAmount;

    /**
     * 差异数量（|系统数量-设备数量|）
     */
    @Schema(description = "差异数量")
    private Integer discrepancyCount;

    /**
     * 差异金额（|系统金额-设备金额|）
     */
    @Schema(description = "差异金额")
    private BigDecimal discrepancyAmount;

    /**
     * 对账状态（1-对账中 2-对账成功 3-存在差异 4-对账失败）
     */
    @Schema(description = "对账状态")
    private Integer reconciliationStatus;

    /**
     * 对账状态描述
     */
    @Schema(description = "对账状态描述")
    private String reconciliationStatusDesc;

    /**
     * 对账开始时间
     */
    @Schema(description = "对账开始时间")
    private LocalDateTime startTime;

    /**
     * 对账完成时间
     */
    @Schema(description = "对账完成时间")
    private LocalDateTime completeTime;

    /**
     * 对账耗时（毫秒）
     */
    @Schema(description = "对账耗时")
    private Long duration;

    /**
     * 操作员ID
     */
    @Schema(description = "操作员ID")
    private Long operatorId;

    /**
     * 操作员姓名
     */
    @Schema(description = "操作员姓名")
    private String operatorName;

    /**
     * 差异说明
     */
    @Schema(description = "差异说明")
    private String discrepancyDescription;

    /**
     * 处理方案
     */
    @Schema(description = "处理方案")
    private String handlingPlan;

    /**
     * 是否已处理（0-未处理 1-已处理）
     */
    @Schema(description = "是否已处理")
    private Integer isHandled;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID")
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @Schema(description = "处理人姓名")
    private String handlerName;

    /**
     * 对账备注
     */
    @Schema(description = "对账备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
