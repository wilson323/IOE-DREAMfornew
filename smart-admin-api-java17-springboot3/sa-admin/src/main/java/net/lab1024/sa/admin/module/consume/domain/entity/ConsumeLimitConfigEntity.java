package net.lab1024.sa.admin.module.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 消费限额配置实体
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_limit_config")
public class ConsumeLimitConfigEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long limitConfigId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 限额类型：DAILY-每日，WEEKLY-每周，MONTHLY-每月，SINGLE-单次
     */
    private String limitType;

    /**
     * 限额金额
     */
    private BigDecimal limitAmount;

    /**
     * 已使用金额
     */
    private BigDecimal usedAmount;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 失效时间
     */
    private LocalDateTime expireTime;

    /**
     * 限额状态：ENABLE-启用，DISABLE-禁用
     */
    private String limitStatus;

    /**
     * 限额描述
     */
    private String limitDescription;

    /**
     * 限额规则（JSON格式）
     */
    private String limitRules;

    /**
     * 优先级
     */
    private Integer priority;

    // =================== 重要提示 ===================
    // ❌ 不要重复定义以下审计字段（BaseEntity已包含）:
    // - createTime (创建时间)
    // - updateTime (更新时间)
    // - createUserId (创建人ID) - 已删除重复定义
    // - updateUserId (更新人ID) - 已删除重复定义
    // - deletedFlag (软删除标记) - 已删除重复定义
    // - version (乐观锁版本号) - 已删除重复定义
    //
    // ✅ 如需使用这些字段，直接通过getter/setter访问即可
    // ================================================
}
