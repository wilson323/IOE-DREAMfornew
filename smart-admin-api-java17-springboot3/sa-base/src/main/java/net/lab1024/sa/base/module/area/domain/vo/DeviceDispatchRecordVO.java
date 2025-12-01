package net.lab1024.sa.base.module.area.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 设备下发记录视图对象
 * 用于设备下发记录的查询和展示
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "设备下发记录视图对象")
public class DeviceDispatchRecordVO {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    /**
     * 关联ID
     */
    @Schema(description = "关联ID", example = "1")
    private Long relationId;

    /**
     * 人员ID
     */
    @Schema(description = "人员ID", example = "1001")
    private Long personId;

    /**
     * 人员姓名
     */
    @Schema(description = "人员姓名", example = "张三")
    private String personName;

    /**
     * 人员编号
     */
    @Schema(description = "人员编号", example = "EMP001")
    private String personCode;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "101")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "办公区A")
    private String areaName;

    /**
     * 区域编码
     */
    @Schema(description = "区域编码", example = "OFFICE_A")
    private String areaCode;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主门禁读卡器")
    private String deviceName;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "ACCESS")
    private String deviceType;

    /**
     * 设备类型描述
     */
    @Schema(description = "设备类型描述", example = "门禁设备")
    private String deviceTypeDesc;

    /**
     * 设备IP地址
     */
    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String deviceIp;

    /**
     * 设备端口
     */
    @Schema(description = "设备端口", example = "8080")
    private Integer devicePort;

    /**
     * 策略ID
     */
    @Schema(description = "策略ID", example = "1")
    private Long strategyId;

    /**
     * 策略名称
     */
    @Schema(description = "策略名称", example = "门禁设备-员工默认策略")
    private String strategyName;

    /**
     * 下发类型
     * ADD-新增，UPDATE-更新，DELETE-删除
     */
    @Schema(description = "下发类型", example = "ADD")
    private String dispatchType;

    /**
     * 下发类型描述
     */
    @Schema(description = "下发类型描述", example = "新增")
    private String dispatchTypeDesc;

    /**
     * 下发状态
     * PENDING-待下发，DISPATCHING-下发中，COMPLETED-已完成，FAILED-失败
     */
    @Schema(description = "下发状态", example = "SUCCESS")
    private String dispatchStatus;

    /**
     * 下发状态描述
     */
    @Schema(description = "下发状态描述", example = "成功")
    private String dispatchStatusDesc;

    /**
     * 下发时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "下发时间", example = "2025-11-25 10:30:00")
    private LocalDateTime dispatchTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "完成时间", example = "2025-11-25 10:30:05")
    private LocalDateTime completeTime;

    /**
     * 执行耗时(毫秒)
     */
    @Schema(description = "执行耗时(毫秒)", example = "5000")
    private Integer durationMs;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数", example = "1")
    private Integer retryCount;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "连接超时")
    private String errorMessage;

    /**
     * 错误代码
     */
    @Schema(description = "错误代码", example = "TIMEOUT")
    private String errorCode;

    /**
     * 业务类型
     */
    @Schema(description = "业务类型", example = "ACCESS")
    private String businessType;

    /**
     * 同步类型
     * PERSON-人员同步，BIOMETRIC-生物特征同步，CONFIG-配置同步
     */
    @Schema(description = "同步类型", example = "BIOMETRIC")
    private String syncType;

    /**
     * 成功数量
     */
    @Schema(description = "成功数量", example = "10")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Schema(description = "失败数量", example = "2")
    private Integer failureCount;

    /**
     * 下发结果详情
     * JSON格式的详细结果信息
     */
    @Schema(description = "下发结果详情", example = "{\"total\": 12, \"success\": 10, \"failed\": 2}")
    private String dispatchResult;

    /**
     * 操作用户ID
     */
    @Schema(description = "操作用户ID", example = "10001")
    private Long operateUserId;

    /**
     * 操作用户姓名
     */
    @Schema(description = "操作用户姓名", example = "管理员")
    private String operateUserName;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "批量下发生物特征数据")
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-11-25 10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-11-25 10:30:05")
    private LocalDateTime updateTime;

    /**
     * 是否下发成功
     */
    public boolean isSuccess() {
        return "COMPLETED".equals(dispatchStatus) || "SUCCESS".equals(dispatchStatus);
    }

    /**
     * 是否正在下发
     */
    public boolean isDispatching() {
        return "DISPATCHING".equals(dispatchStatus);
    }

    /**
     * 是否下发失败
     */
    public boolean isFailed() {
        return "FAILED".equals(dispatchStatus);
    }

    /**
     * 是否需要重试
     */
    public boolean needsRetry() {
        return isFailed() && retryCount < 3;
    }

    /**
     * 获取执行耗时描述
     */
    public String getDurationDesc() {
        if (durationMs == null) {
            return "未知";
        }
        if (durationMs < 1000) {
            return durationMs + "毫秒";
        } else if (durationMs < 60000) {
            return String.format("%.1f秒", durationMs / 1000.0);
        } else {
            return String.format("%.1f分钟", durationMs / 60000.0);
        }
    }
}