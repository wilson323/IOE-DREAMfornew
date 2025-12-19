package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费账户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_account")
@Schema(description = "消费账户实体")
public class ConsumeAccountEntity extends BaseEntity {

    /**
     * 账户ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "账户ID")
    private Long id;

    /**
     * 账户编号
     */
    @TableField("account_no")
    @Schema(description = "账户编号")
    private String accountNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("user_name")
    @Schema(description = "用户名")
    private String userName;

    /**
     * 账户类型
     */
    @TableField("account_type")
    @Schema(description = "账户类型")
    private Integer accountType;

    /**
     * 账户名称
     */
    @TableField("account_name")
    @Schema(description = "账户名称")
    private String accountName;

    /**
     * 账户余额
     */
    @TableField("balance")
    @Schema(description = "账户余额")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @TableField("frozen_amount")
    @Schema(description = "冻结金额")
    private BigDecimal frozenAmount;

    /**
     * 可用余额
     */
    @TableField("available_balance")
    @Schema(description = "可用余额")
    private BigDecimal availableBalance;

    /**
     * 信用额度
     */
    @TableField("credit_limit")
    @Schema(description = "信用额度")
    private BigDecimal creditLimit;

    /**
     * 账户状态
     */
    @TableField("status")
    @Schema(description = "账户状态")
    private Integer status;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 最后消费时间
     */
    @TableField("last_consume_time")
    @Schema(description = "最后消费时间")
    private LocalDateTime lastConsumeTime;

    /**
     * 最后充值时间
     */
    @TableField("last_recharge_time")
    @Schema(description = "最后充值时间")
    private LocalDateTime lastRechargeTime;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;
}