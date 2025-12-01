package net.lab1024.sa.base.module.area.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 设备下发记录表单
 * 用于记录设备下发操作的详细信息
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "设备下发记录表单")
public class DeviceDispatchRecordForm {

    /**
     * 记录ID
     * 新增时为空，更新时必填
     */
    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    @Schema(description = "业务类型", example = "ACCESS")
    private String businessType;

    /**
     * 同步类型
     * PERSON-人员同步，BIOMETRIC-生物特征同步，CONFIG-配置同步
     */
    @Schema(description = "同步类型", example = "BIOMETRIC", allowableValues = {"PERSON", "BIOMETRIC", "CONFIG"})
    private String syncType;

    /**
     * 目标对象ID列表
     * 人员ID、区域ID或其他业务对象ID
     */
    @NotEmpty(message = "目标对象ID列表不能为空")
    @Schema(description = "目标对象ID列表", example = "[1, 2, 3]")
    private List<Long> targetIds;

    /**
     * 下发状态
     * PENDING-待下发，DISPATCHING-下发中，COMPLETED-已完成，FAILED-失败
     */
    @Schema(description = "下发状态", example = "COMPLETED", allowableValues = {"PENDING", "DISPATCHING", "COMPLETED", "FAILED"})
    private String dispatchStatus;

    /**
     * 下发时间
     */
    @Schema(description = "下发时间", example = "2025-11-25T10:30:00")
    private java.time.LocalDateTime dispatchTime;

    /**
     * 完成时间
     */
    @Schema(description = "完成时间", example = "2025-11-25T10:35:00")
    private java.time.LocalDateTime completeTime;

    /**
     * 成功数量
     */
    @Schema(description = "成功数量", example = "10")
    private Integer successCount = 0;

    /**
     * 失败数量
     */
    @Schema(description = "失败数量", example = "2")
    private Integer failureCount = 0;

    /**
     * 下发结果详情
     * JSON格式的详细结果信息
     */
    @Schema(description = "下发结果详情", example = "{\"total\": 12, \"success\": 10, \"failed\": 2}")
    private String dispatchResult;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "设备连接超时")
    private String errorMessage;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数", example = "3")
    private Integer retryCount = 0;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "批量下发生物特征数据")
    private String remark;
}