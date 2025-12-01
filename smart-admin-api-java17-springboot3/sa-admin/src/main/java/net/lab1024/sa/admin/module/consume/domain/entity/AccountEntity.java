package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费账户实体类
 * 管理用户账户信息和余额
 *
 * @author SmartAdmin Team
 * @date 2025/11/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_account")
public class AccountEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long accountId;

    /**
     * 人员ID（主键关联）
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 员工编号
     */
    private String employeeNo;

    /**
     * 账户编号（系统生成的唯一标识）
     */
    private String accountNo;

    /**
     * 可用余额
     */
    private BigDecimal balance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 信用额度
     */
    private BigDecimal creditLimit;

    /**
     * 可用额度（余额 + 信用额度 - 冻结金额）
     */
    private BigDecimal availableLimit;

    /**
     * 账户类型（STAFF/STUDENT/VISITOR/TEMP）
     */
    private String accountType;

    /**
     * 账户状态（ACTIVE/FROZEN/CLOSED/SUSPENDED）
     */
    private String status;

    /**
     * 所属区域ID
     */
    private String regionId;

    /**
     * 所属区域名称
     */
    private String regionName;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 账户等级（普通/VIP/钻石）
     */
    private String accountLevel;

    /**
     * 积分余额
     */
    private Integer points;

    /**
     * 累计消费金额
     */
    private BigDecimal totalConsumeAmount;

    /**
     * 累计充值金额
     */
    private BigDecimal totalRechargeAmount;

    /**
     * 最后消费时间
     */
    private LocalDateTime lastConsumeTime;

    /**
     * 最后充值时间
     */
    private LocalDateTime lastRechargeTime;

    /**
     * 账户创建时间
     */
    private LocalDateTime accountCreateTime;

    /**
     * 账户激活时间
     */
    private LocalDateTime activeTime;

    /**
     * 账户冻结时间
     */
    private LocalDateTime freezeTime;

    /**
     * 账户关闭时间
     */
    private LocalDateTime closeTime;

    /**
     * 冻结原因
     */
    private String freezeReason;

    /**
     * 关闭原因
     */
    private String closeReason;

    /**
     * 月度消费限额
     */
    private BigDecimal monthlyLimit;

    /**
     * 日度消费限额
     */
    private BigDecimal dailyLimit;

    /**
     * 单次消费限额
     */
    private BigDecimal singleLimit;

    /**
     * 当前月度已消费金额
     */
    private BigDecimal currentMonthlyAmount;

    /**
     * 当前日度已消费金额
     */
    private BigDecimal currentDailyAmount;

    /**
     * 密码（用于消费验证）
     */
    private String password;

    /**
     * 支付密码
     */
    private String payPassword;

    /**
     * 卡片ID（物理卡）
     */
    private String cardId;

    /**
     * 卡片状态（NORMAL/LOST/DAMAGE/EXPIRED）
     */
    private String cardStatus;

    /**
     * 人脸特征标识
     */
    private String faceFeatureId;

    /**
     * 指纹特征标识
     */
    private String fingerprintId;

    /**
     * 手机号码（用于通知和验证）
     */
    private String phoneNumber;

    /**
     * 邮箱地址（用于通知）
     */
    private String email;

    /**
     * 账户配置JSON（存储个性化配置）
     */
    private String accountConfig;

    /**
     * 扩展数据JSON（存储扩展字段）
     */
    private String extendData;

    /**
     * 是否开启短信通知
     */
    private Boolean smsNotification;

    /**
     * 是否开启邮件通知
     */
    private Boolean emailNotification;

    /**
     * 是否开启消费提醒
     */
    private Boolean consumeReminder;

    /**
     * 消费提醒阈值（低于此金额时提醒）
     */
    private BigDecimal reminderThreshold;

    /**
     * 自动充值开关
     */
    private Boolean autoRecharge;

    /**
     * 自动充值阈值
     */
    private BigDecimal autoRechargeThreshold;

    /**
     * 自动充值金额
     */
    private BigDecimal autoRechargeAmount;

    /**
     * 绑定支付方式（JSON格式：微信、支付宝、银行卡等）
     */
    private String paymentMethods;

    /**
     * 备注信息
     */
    private String remark;
}