package net.lab1024.sa.admin.module.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 异常操作日志实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_abnormal_operation_log")
public class AbnormalOperationLogEntity extends BaseEntity {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 异常类型：LOCATION-位置异常，TIME-时间异常，AMOUNT-金额异常，FREQUENCY-频率异常，DEVICE-设备异常，SEQUENCE-序列异常
     */
    @NotNull(message = "异常类型不能为空")
    private String anomalyType;

    /**
     * 风险等级：LOW-低风险，MEDIUM-中风险，HIGH-高风险，CRITICAL-严重风险
     */
    @NotNull(message = "风险等级不能为空")
    private String riskLevel;

    /**
     * 异常描述
     */
    private String anomalyDescription;

    /**
     * 操作时间
     */
    @NotNull(message = "操作时间不能为空")
    private LocalDateTime operationTime;

    /**
     * 操作类型：CONSUME-消费，RECHARGE-充值，TRANSFER-转账，QUERY-查询
     */
    @NotNull(message = "操作类型不能为空")
    private String operationType;

    /**
     * 操作金额
     */
    private BigDecimal operationAmount;

    /**
     * 操作位置
     */
    private String operationLocation;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 触发规则ID
     */
    private Long triggerRuleId;

    /**
     * 触发规则名称
     */
    private String triggerRuleName;

    /**
     * 异常分数
     */
    private BigDecimal anomalyScore;

    /**
     * 基线偏差值
     */
    private String baselineDeviation;

    /**
     * 处理状态：0-待处理，1-处理中，2-已处理，3-已忽略
     */
    @NotNull(message = "处理状态不能为空")
    private Integer handleStatus;

    /**
     * 处理人ID
     */
    private Long handleUserId;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 是否自动处理：0-否，1-是
     */
    private Integer autoHandled;

    /**
     * 关联订单号
     */
    private String relatedOrderNo;

    /**
     * 详细数据（JSON格式）
     */
    private String detailData;

    /**
     * 备注
     */
    private String remark;
}
