package net.lab1024.sa.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消费记录对账实体
 * <p>
 * 记录消费交易对账结果，包括：
 * - 对账日期范围
 * - 交易总数和金额统计
 * - 差异记录
 * - 对账状态
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_consume_reconciliation_record")
@Schema(description = "消费记录对账实体")
public class ReconciliationRecordEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "对账ID", example = "1")
    private Long reconciliationId;

    @Schema(description = "对账开始日期", example = "2025-12-26")
    private LocalDate startDate;

    @Schema(description = "对账结束日期", example = "2025-12-26")
    private LocalDate endDate;

    @Schema(description = "系统交易总数", example = "100")
    private Integer systemTransactionCount;

    @Schema(description = "系统交易总金额", example = "1000.00")
    private BigDecimal systemTotalAmount;

    @Schema(description = "设备交易总数", example = "98")
    private Integer deviceTransactionCount;

    @Schema(description = "设备交易总金额", example = "980.00")
    private BigDecimal deviceTotalAmount;

    @Schema(description = "差异交易数", example = "2")
    private Integer discrepancyCount;

    @Schema(description = "差异金额", example = "20.00")
    private BigDecimal discrepancyAmount;

    @Schema(description = "对账状态：1-对账中 2-对账成功 3-存在差异", example = "2")
    private Integer reconciliationStatus;

    @Schema(description = "对账类型：1-自动对账 2-手动对账", example = "1")
    private Integer reconciliationType;

    @Schema(description = "差异详情（JSON格式）")
    private String discrepancyDetails;

    @Schema(description = "对账备注", example = "对账完成")
    private String remark;

    @Schema(description = "创建时间", example = "2025-12-26T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "完成时间", example = "2025-12-26T10:05:00")
    private LocalDateTime completeTime;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "创建人姓名", example = "管理员")
    private String createUserName;
}
