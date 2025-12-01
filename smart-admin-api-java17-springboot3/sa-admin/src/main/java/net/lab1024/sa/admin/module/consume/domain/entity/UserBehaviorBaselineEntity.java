package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户行为基线实体
 *
 * @author SmartAdmin Team
 * @since 2025-11-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_behavior_baseline")
public class UserBehaviorBaselineEntity extends net.lab1024.sa.base.common.entity.BaseEntity {

    /**
     * 基线ID
     */
    @TableId(type = IdType.AUTO)
    private Long baselineId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 基线类型：CONSUMPTION-消费行为，LOCATION-位置行为，TIME-时间行为，DEVICE-设备行为
     */
    @NotNull(message = "基线类型不能为空")
    private String baselineType;

    /**
     * 基线数据（JSON格式）
     */
    private String baselineData;

    /**
     * 平均消费金额
     */
    private BigDecimal averageAmount;

    /**
     * 消费频率（次/天）
     */
    private BigDecimal frequency;

    /**
     * 常用位置列表
     */
    private String commonLocations;

    /**
     * 常用时间段
     */
    private String commonTimeRanges;

    /**
     * 常用设备列表
     */
    private String commonDevices;

    /**
     * 基线置信度
     */
    private BigDecimal confidence;

    /**
     * 样本数量
     */
    private Integer sampleCount;

    /**
     * 基线有效期开始时间
     */
    private LocalDateTime effectiveStartTime;

    /**
     * 基线有效期结束时间
     */
    private LocalDateTime effectiveEndTime;

    /**
     * 更新频率（天）
     */
    private Integer updateFrequency;

    /**
     * 最后分析时间
     */
    private LocalDateTime lastAnalysisTime;

    /**
     * 基线状态：0-无效，1-有效，2-待更新
     */
    @NotNull(message = "基线状态不能为空")
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}