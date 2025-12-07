package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费账户实体类
 * <p>
 * 用于管理用户消费账户信息，包括余额、补贴等
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 账户余额管理
 * - 账户充值
 * - 账户消费
 * - 补贴管理
 * </p>
 * <p>
 * 数据库表：POSID_ACCOUNT（业务文档中定义的表名）
 * 注意：根据CLAUDE.md规范，表名应使用t_consume_*格式，但业务文档中使用POSID_*格式
 * 实际使用时需要根据数据库表名调整@TableName注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("account")
public class AccountEntity extends BaseEntity {

    /**
     * 账户ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账户ID（别名，用于兼容测试类）
     */
    public Long getAccountId() {
        return id;
    }

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 人员ID（别名，用于兼容测试类）
     */
    public Long getPersonId() {
        return userId;
    }

    /**
     * 总消费金额（用于测试类，单位：元）
     */
    private java.math.BigDecimal totalConsumeAmount;

    /**
     * 设置账户ID（用于测试类）
     */
    public void setAccountId(Long accountId) {
        this.id = accountId;
    }

    /**
     * 设置人员ID（用于测试类）
     */
    public void setPersonId(Long personId) {
        this.userId = personId;
    }

    /**
     * 账户类别ID
     */
    private Long accountKindId;

    /**
     * 账户余额（单位：分）
     */
    private Long balance;

    /**
     * 补贴余额（单位：分）
     */
    private Long allowanceBalance;

    /**
     * 冻结余额（单位：分）
     */
    private Long frozenBalance;

    /**
     * 账户状态
     * <p>
     * 1-正常
     * 2-冻结
     * 3-注销
     * </p>
     */
    private Integer status;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 账户余额（BigDecimal类型，用于计算）
     * <p>
     * 从balance转换而来
     * </p>
     */
    public BigDecimal getBalanceAmount() {
        return balance != null ? BigDecimal.valueOf(balance).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * 补贴余额（BigDecimal类型，用于计算）
     * <p>
     * 从allowanceBalance转换而来
     * </p>
     */
    public BigDecimal getAllowanceBalanceAmount() {
        return allowanceBalance != null ? BigDecimal.valueOf(allowanceBalance).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * 冻结余额（BigDecimal类型，用于计算）
     * <p>
     * 从frozenBalance转换而来
     * </p>
     */
    public BigDecimal getFrozenBalanceAmount() {
        return frozenBalance != null ? BigDecimal.valueOf(frozenBalance).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }
}
