package net.lab1024.sa.common.entity.consume;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费账户实体类（纯数据模型）
 * <p>
 * 管理消费账户信息，支持余额管理和信用支付
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * </p>
 * <p>
 * <strong>设计原则：</strong>
 * <ul>
 *   <li>纯数据模型：不包含业务逻辑方法</li>
 *   <li>业务逻辑已迁移至ConsumeAccountManager</li>
 *   <li>符合单一职责原则</li>
 * </ul>
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 2.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_account")
@Schema(description = "消费账户实体")
public class ConsumeAccountEntity extends BaseEntity {

    /**
     * 账户ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户编号（唯一）
     */
    @Schema(description = "账户编号")
    private String accountCode;

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
     * 账户类型（1-个人账户 2-部门账户 3-临时账户 4-访客账户）
     */
    @Schema(description = "账户类型")
    private Integer accountType;

    /**
     * 账户余额（元）
     */
    @Schema(description = "账户余额")
    private BigDecimal balance;

    /**
     * 冻结金额（元）
     */
    @Schema(description = "冻结金额")
    private BigDecimal frozenAmount;

    /**
     * 可用余额（元）= 余额 - 冻结金额
     */
    @Schema(description = "可用余额")
    private BigDecimal availableBalance;

    /**
     * 信用额度（元）
     */
    @Schema(description = "信用额度")
    private BigDecimal creditLimit;

    /**
     * 已用信用额度（元）
     */
    @Schema(description = "已用信用额度")
    private BigDecimal usedCredit;

    /**
     * 可用信用额度（元）= 信用额度 - 已用信用额度
     */
    @Schema(description = "可用信用额度")
    private BigDecimal availableCredit;

    /**
     * 账户状态（1-正常 2-冻结 3-注销 4-挂失）
     */
    @Schema(description = "账户状态")
    private Integer status;

    /**
     * 支付密码（加密存储）
     */
    @Schema(description = "支付密码")
    private String password;

    /**
     * 是否设置密码（0-未设置 1-已设置）
     */
    @Schema(description = "是否设置密码")
    private Integer hasPassword;

    /**
     * 安全等级（1-低 2-中 3-高）
     */
    @Schema(description = "安全等级")
    private Integer securityLevel;

    /**
     * 积分
     */
    @Schema(description = "积分")
    private Integer points;

    /**
     * 最后交易时间
     */
    @Schema(description = "最后交易时间")
    private LocalDateTime lastTransactionTime;

    /**
     * 最后交易地点
     */
    @Schema(description = "最后交易地点")
    private String lastTransactionLocation;

    /**
     * 最后交易金额（元）
     */
    @Schema(description = "最后交易金额")
    private BigDecimal lastTransactionAmount;

    /**
     * 总充值金额（元）
     */
    @Schema(description = "总充值金额")
    private BigDecimal totalRecharge;

    /**
     * 总消费金额（元）
     */
    @Schema(description = "总消费金额")
    private BigDecimal totalConsume;

    /**
     * 总退款金额（元）
     */
    @Schema(description = "总退款金额")
    private BigDecimal totalRefund;

    /**
     * 交易次数
     */
    @Schema(description = "交易次数")
    private Integer transactionCount;

    /**
     * 冻结原因
     */
    @Schema(description = "冻结原因")
    private String freezeReason;

    /**
     * 冻结时间
     */
    @Schema(description = "冻结时间")
    private LocalDateTime freezeTime;

    /**
     * 冻结人ID
     */
    @Schema(description = "冻结人ID")
    private Long freezerId;

    /**
     * 冻结人姓名
     */
    @Schema(description = "冻结人姓名")
    private String freezerName;

    /**
     * 开户时间
     */
    @Schema(description = "开户时间")
    private LocalDateTime openTime;

    /**
     * 账户备注
     */
    @Schema(description = "账户备注")
    private String remark;

    /**
     * 乐观锁版本号
     */
    @Version
    @Schema(description = "乐观锁版本号")
    private Integer version;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;
}
