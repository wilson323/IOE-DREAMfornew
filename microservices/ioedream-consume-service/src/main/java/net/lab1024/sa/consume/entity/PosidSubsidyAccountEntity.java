package net.lab1024.sa.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POSID补贴账户表实体
 *
 * 对应表: POSID_SUBSIDY_ACCOUNT
 * 表说明: 补贴账户表，支持多补贴账户体系
 *
 * 核心字段:
 * - subsidy_account_id: 补贴账户ID（主键）
 * - user_id: 用户ID
 * - subsidy_type_id: 补贴类型ID（外键关联POSID_SUBSIDY_TYPE）
 * - balance: 补贴余额
 * - expire_time: 过期时间
 * - account_status: 账户状态（ACTIVE-正常，FROZEN-冻结，EXPIRED-已过期，CLEARED-已清零）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Data
@TableName("POSID_SUBSIDY_ACCOUNT")
public class PosidSubsidyAccountEntity {

    /**
     * 补贴账户ID
     */
    @TableId(type = IdType.AUTO)
    private Long subsidyAccountId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 补贴类型ID（外键关联POSID_SUBSIDY_TYPE.subsidy_type_id）
     */
    @TableField("subsidy_type_id")
    private Long subsidyTypeId;

    /**
     * 账户编码
     */
    @TableField("account_code")
    private String accountCode;

    /**
     * 账户名称
     */
    @TableField("account_name")
    private String accountName;

    /**
     * 补贴余额
     */
    @TableField("balance")
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    @TableField("frozen_amount")
    private BigDecimal frozenAmount;

    /**
     * 初始金额
     */
    @TableField("initial_amount")
    private BigDecimal initialAmount;

    /**
     * 累计发放
     */
    @TableField("total_granted")
    private BigDecimal totalGranted;

    /**
     * 累计使用
     */
    @TableField("total_used")
    private BigDecimal totalUsed;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 账户状态（1-正常ACTIVE，2-冻结FROZEN，3-已过期EXPIRED，4-已清零CLEARED）
     */
    @TableField("account_status")
    private Integer accountStatus;

    /**
     * 发放批次号
     */
    @TableField("grant_batch_no")
    private String grantBatchNo;

    /**
     * 发放人ID
     */
    @TableField("grant_user_id")
    private Long grantUserId;

    /**
     * 发放时间
     */
    @TableField("grant_time")
    private LocalDateTime grantTime;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

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
     * 删除标记
     */
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
