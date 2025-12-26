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
import lombok.Builder;
import lombok.NoArgsConstructor;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费充值记录实体类
 * <p>
 * 记录账户充值操作详情，支持多种充值方式和渠道
 * 提供充值审核、统计和对账功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 现金充值
 * - 在线支付充值
 * - 批量充值导入
 * - 补贴发放
 * - 充值审核
 * - 充值统计
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
@TableName("t_consume_recharge")
@Schema(description = "消费充值记录实体")
public class ConsumeRechargeEntity extends BaseEntity {

    /**
     * 充值记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "充值记录ID")
    private Long rechargeId;

    /**
     * 交易流水号（业务唯一）
     */
    @Schema(description = "交易流水号")
    private String transactionNo;

    /**
     * 批次号（批量充值时使用）
     */
    @Schema(description = "批次号")
    private String batchNo;

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
     * 账户ID
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户编号
     */
    @Schema(description = "账户编号")
    private String accountNo;

    /**
     * 充值金额
     */
    @Schema(description = "充值金额")
    private BigDecimal rechargeAmount;

    /**
     * 赠送金额
     */
    @Schema(description = "赠送金额")
    private BigDecimal giftAmount;

    /**
     * 实际到账金额（充值金额+赠送金额）
     */
    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;

    /**
     * 充值前余额
     */
    @Schema(description = "充值前余额")
    private BigDecimal balanceBefore;

    /**
     * 充值后余额
     */
    @Schema(description = "充值后余额")
    private BigDecimal balanceAfter;

    /**
     * 充值状态（1-待审核 2-审核通过 3-审核拒绝 4-充值成功 5-充值失败）
     */
    @Schema(description = "充值状态")
    private Integer rechargeStatus;

    /**
     * 充值状态描述
     */
    @Schema(description = "充值状态描述")
    private String rechargeStatusDesc;

    /**
     * 审核状态（1-待审核 2-审核通过 3-审核拒绝）
     */
    @Schema(description = "审核状态")
    private Integer auditStatus;

    /**
     * 充值方式（1-现金 2-刷卡 3-在线支付 4-转账 5-批量导入）
     */
    @Schema(description = "充值方式")
    private Integer rechargeWay;

    /**
     * 充值方式名称
     */
    @Schema(description = "充值方式名称")
    private String rechargeWayName;

    /**
     * 充值渠道（1-线下窗口 2-自助终端 3-微信 4-支付宝 5-银行转账）
     */
    @Schema(description = "充值渠道")
    private Integer rechargeChannel;

    /**
     * 充值渠道名称
     */
    @Schema(description = "充值渠道名称")
    private String rechargeChannelName;

    /**
     * 第三方交易号（支付平台返回）
     */
    @Schema(description = "第三方交易号")
    private String thirdPartyNo;

    /**
     * 支付凭证号
     */
    @Schema(description = "支付凭证号")
    private String paymentVoucherNo;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

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
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private Long auditorId;

    /**
     * 审核人姓名
     */
    @Schema(description = "审核人姓名")
    private String auditorName;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String auditRemark;

    /**
     * 充值时间
     */
    @Schema(description = "充值时间")
    private LocalDateTime rechargeTime;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String failureReason;

    /**
     * 充值备注
     */
    @Schema(description = "充值备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
