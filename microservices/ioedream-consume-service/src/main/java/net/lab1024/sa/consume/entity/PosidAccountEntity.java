package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POSID账户主表实体
 *
 * 对应表: POSID_ACCOUNT
 * 表说明: 消费模块账户主表，使用POSID_*命名规范，完全符合业务文档ER图设计
 *
 * 核心字段:
 * - account_id: 账户ID（主键）
 * - user_id: 用户ID（唯一）
 * - account_code: 账户编码（唯一）
 * - balance: 账户余额
 * - frozen_amount: 冻结金额
 * - credit_limit: 信用额度
 * - account_status: 账户状态（0-冻结，1-正常，2-注销）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("POSID_ACCOUNT")
public class PosidAccountEntity {

    /**
     * 账户ID
     */
    @TableId(type = IdType.AUTO)
    private Long accountId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 账户编码（唯一）
     */
    @TableField("account_code")
    private String accountCode;

    /**
     * 账户名称
     */
    @TableField("account_name")
    private String accountName;

    /**
     * 账户类型（1-员工账户，2-访客账户，3-临时账户）
     */
    @TableField("account_type")
    private Integer accountType;

    /**
     * 账户余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @TableField("frozen_amount")
    private BigDecimal frozenAmount;

    /**
     * 信用额度
     */
    @TableField("credit_limit")
    private BigDecimal creditLimit;

    /**
     * 累计充值
     */
    @TableField("total_recharge")
    private BigDecimal totalRecharge;

    /**
     * 累计消费
     */
    @TableField("total_consume")
    private BigDecimal totalConsume;

    /**
     * 账户状态（0-冻结，1-正常，2-注销）
     */
    @TableField("account_status")
    private Integer accountStatus;

    /**
     * 支付密码
     */
    @TableField("password")
    private String password;

    /**
     * 是否启用自动充值
     */
    @TableField("enable_auto_recharge")
    private Integer enableAutoRecharge;

    /**
     * 自动充值金额
     */
    @TableField("auto_recharge_amount")
    private BigDecimal autoRechargeAmount;

    /**
     * 自动充值阈值
     */
    @TableField("auto_recharge_threshold")
    private BigDecimal autoRechargeThreshold;

    /**
     * 最后消费时间
     */
    @TableField("last_consume_time")
    private LocalDateTime lastConsumeTime;

    /**
     * 最后充值时间
     */
    @TableField("last_recharge_time")
    private LocalDateTime lastRechargeTime;

    /**
     * 乐观锁版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
