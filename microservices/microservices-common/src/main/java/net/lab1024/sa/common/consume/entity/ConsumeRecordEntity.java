package net.lab1024.sa.common.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费记录实体类
 * <p>
 * 用于记录消费流水数据，支持多维度统计分析
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 消费流水记录
 * - 消费统计分析
 * - 对账和审计
 * - 多维度数据查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_record")
public class ConsumeRecordEntity extends BaseEntity {

    /**
     * 消费记录ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 交易流水号（唯一）
     */
    private String transactionNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 消费类型
     * <p>
     * CONSUME-正常消费
     * MAKEUP-补单
     * CORRECT-纠错
     * </p>
     */
    private String consumeType;

    /**
     * 消费模式
     * <p>
     * FIXED-定值
     * AMOUNT-金额
     * PRODUCT-商品
     * COUNT-计次
     * </p>
     */
    private String consumeMode;

    /**
     * 状态
     * <p>
     * SUCCESS-成功
     * FAILED-失败
     * REFUND-已退款
     * </p>
     */
    private String status;
}
