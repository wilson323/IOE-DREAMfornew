package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 验证步骤
 * <p>
 * 用于规则验证流程中的步骤记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationStep {

    /**
     * 步骤ID
     */
    private Long stepId;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 步骤描述
     */
    private String description;

    /**
     * 步骤序号
     */
    private Integer stepOrder;

    /**
     * 步骤状态：PENDING-待处理 RUNNING-运行中 SUCCESS-成功 FAILED-失败 SKIPPED-跳过
     */
    private String stepStatus;

    /**
     * 步骤类型：VALIDATION-验证 TRANSFORMATION-转换 CALCULATION-计算
     */
    private String stepType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时长（毫秒）
     */
    private Long duration;

    /**
     * 结果数据
     */
    private Object resultData;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 创建验证步骤
     */
    public static ValidationStep create(String stepName, Integer stepOrder, String stepType) {
        return ValidationStep.builder()
            .stepName(stepName)
            .stepOrder(stepOrder)
            .stepType(stepType)
            .stepStatus("PENDING")
            .build();
    }

    /**
     * 标记步骤开始
     */
    public void markStart() {
        this.stepStatus = "RUNNING";
        this.startTime = LocalDateTime.now();
    }

    /**
     * 标记步骤成功
     */
    public void markSuccess(Object result) {
        this.stepStatus = "SUCCESS";
        this.endTime = LocalDateTime.now();
        this.resultData = result;
        if (this.startTime != null) {
            this.duration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }

    /**
     * 标记步骤失败
     */
    public void markFailure(String error) {
        this.stepStatus = "FAILED";
        this.endTime = LocalDateTime.now();
        this.errorMessage = error;
        if (this.startTime != null) {
            this.duration = java.time.Duration.between(this.startTime, this.endTime).toMillis();
        }
    }
}
