package net.lab1024.sa.common.entity.consume;

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
 * 消费补贴实体类
 * <p>
 * 管理消费补贴信息，支持多种补贴类型和周期管理
 * 提供余额管理、使用记录统计和过期处理等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 餐饮补贴发放
 * - 余额管理和使用
 * - 每日限额控制
 * - 自动过期处理
 * - 补贴统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_subsidy")
@Schema(description = "消费补贴实体")
public class ConsumeSubsidyEntity extends BaseEntity {

    /**
     * 补贴ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "补贴ID")
    private Long subsidyId;

    /**
     * 补贴编码（唯一）
     */
    @Schema(description = "补贴编码")
    private String subsidyCode;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String departmentName;

    /**
     * 补贴类型（1-餐饮补贴 2-交通补贴 3-通讯补贴 4-其他补贴）
     */
    @Schema(description = "补贴类型（1-餐饮补贴 2-交通补贴 3-通讯补贴 4-其他补贴）")
    private Integer subsidyType;

    /**
     * 补贴类型名称
     */
    @Schema(description = "补贴类型名称")
    private String subsidyTypeName;

    /**
     * 补贴周期（1-每日 2-每周 3-每月 4-每季度 5-每年 6-一次性）
     */
    @Schema(description = "补贴周期（1-每日 2-每周 3-每月 4-每季度 5-每年 6-一次性）")
    private Integer subsidyPeriod;

    /**
     * 补贴周期名称
     */
    @Schema(description = "补贴周期名称")
    private String subsidyPeriodName;

    /**
     * 补贴总金额（元）
     */
    @Schema(description = "补贴总金额")
    private BigDecimal totalAmount;

    /**
     * 已使用金额（元）
     */
    @Schema(description = "已使用金额")
    private BigDecimal usedAmount;

    /**
     * 剩余金额（元）
     */
    @Schema(description = "剩余金额")
    private BigDecimal remainingAmount;

    /**
     * 每日限额（元）
     */
    @Schema(description = "每日限额")
    private BigDecimal dailyLimit;

    /**
     * 当日使用金额（元）
     */
    @Schema(description = "当日使用金额")
    private BigDecimal dailyUsedAmount;

    /**
     * 使用限制次数（0-不限制）
     */
    @Schema(description = "使用限制次数")
    private Integer usageLimit;

    /**
     * 已使用次数
     */
    @Schema(description = "已使用次数")
    private Integer usedCount;

    /**
     * 生效日期
     */
    @Schema(description = "生效日期")
    private LocalDateTime effectiveDate;

    /**
     * 过期日期
     */
    @Schema(description = "过期日期")
    private LocalDateTime expireDate;

    /**
     * 发放日期
     */
    @Schema(description = "发放日期")
    private LocalDateTime issueDate;

    /**
     * 发放人ID
     */
    @Schema(description = "发放人ID")
    private Long issuerId;

    /**
     * 发放人姓名
     */
    @Schema(description = "发放人姓名")
    private String issuerName;

    /**
     * 审批人ID
     */
    @Schema(description = "审批人ID")
    private Long approverId;

    /**
     * 审批人姓名
     */
    @Schema(description = "审批人姓名")
    private String approverName;

    /**
     * 审批时间
     */
    @Schema(description = "审批时间")
    private LocalDateTime approvalTime;

    /**
     * 补贴状态（0-待发放 1-已发放 2-已过期 3-已使用 4-已停用）
     */
    @Schema(description = "补贴状态（0-待发放 1-已发放 2-已过期 3-已使用 4-已停用）")
    private Integer status;

    /**
     * 自动续期标识（0-否 1-是）
     */
    @Schema(description = "自动续期标识")
    private Integer autoRenew;

    /**
     * 续期周期（天）
     */
    @Schema(description = "续期周期")
    private Integer renewPeriod;

    /**
     * 补贴说明
     */
    @Schema(description = "补贴说明")
    private String description;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    // ==================== 业务方法 ====================

    /**
     * 检查补贴是否有效
     *
     * @return true-有效，false-无效
     */
    public boolean isValid() {
        return status != null && status == 1
            && effectiveDate != null && effectiveDate.isBefore(LocalDateTime.now())
            && expireDate != null && expireDate.isAfter(LocalDateTime.now());
    }

    /**
     * 检查是否已过期
     *
     * @return true-已过期，false-未过期
     */
    public boolean isExpired() {
        return expireDate != null && expireDate.isBefore(LocalDateTime.now());
    }

    /**
     * 检查是否即将过期（指定天数内）
     *
     * @param days 天数
     * @return true-即将过期，false-不在即将过期范围内
     */
    public boolean isExpiringSoon(int days) {
        if (expireDate == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().plusDays(days);
        return expireDate.isBefore(threshold);
    }

    /**
     * 检查余额是否充足
     *
     * @param amount 需要的金额
     * @return true-余额充足，false-余额不足
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return remainingAmount != null && remainingAmount.compareTo(amount) >= 0;
    }

    /**
     * 检查是否超过每日限额
     *
     * @param amount 需要消费的金额
     * @return true-超过限额，false-未超过限额
     */
    public boolean exceedsDailyLimit(BigDecimal amount) {
        if (dailyLimit == null) {
            return false;
        }
        BigDecimal newDailyUsed = (dailyUsedAmount != null ? dailyUsedAmount : BigDecimal.ZERO).add(amount);
        return newDailyUsed.compareTo(dailyLimit) > 0;
    }

    /**
     * 检查是否即将用完（使用比例超过指定阈值）
     *
     * @param percentage 使用比例阈值（如0.8表示80%）
     * @return true-即将用完，false-未达到阈值
     */
    public boolean isNearlyDepleted(BigDecimal percentage) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal usedPercentage = usedAmount != null
            ? usedAmount.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
        return usedPercentage.compareTo(percentage) >= 0;
    }

    /**
     * 获取使用比例（0-1）
     *
     * @return 使用比例
     */
    public BigDecimal getUsagePercentage() {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return usedAmount != null
            ? usedAmount.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 获取剩余天数
     *
     * @return 剩余天数，如果已过期返回0
     */
    public long getRemainingDays() {
        if (expireDate == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (expireDate.isBefore(now)) {
            return 0;
        }
        return java.time.Duration.between(now, expireDate).toDays();
    }
}
