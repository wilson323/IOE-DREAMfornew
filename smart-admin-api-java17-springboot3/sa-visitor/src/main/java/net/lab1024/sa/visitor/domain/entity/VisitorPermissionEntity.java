package net.lab1024.sa.visitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 访客权限实体类
 * <p>
 * 严格遵循repowiki规范：
 * - 继承BaseEntity，使用审计字段
 * - 使用jakarta包名
 * - 完整的字段注解和验证
 * - 日期时间格式化
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_visitor_permission")
@Schema(description = "访客权限实体")

public class VisitorPermissionEntity extends net.lab1024.sa.base.common.entity.BaseEntity {

    /**
     * 权限ID
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    @Schema(description = "权限ID", example = "1")
    private Long permissionId;

    /**
     * 预约ID
     */
    @TableField("reservation_id")
    @NotNull(message = "预约ID不能为空")
    @Schema(description = "预约ID", example = "1")
    private Long reservationId;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @NotNull(message = "区域ID不能为空")
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @TableField("area_name")
    @NotBlank(message = "区域名称不能为空")
    @Schema(description = "区域名称", example = "办公楼A栋")
    private String areaName;

    /**
     * 可用设备ID列表
     */
    @TableField("device_ids")
    @Schema(description = "可用设备ID列表(JSON格式)", example = "[1001,1002,1003]")
    private String deviceIds;

    /**
     * 权限开始时间
     */
    @TableField("permission_start_time")
    @NotNull(message = "权限开始时间不能为空")
    
    @Schema(description = "权限开始时间", example = "2025-11-25 09:00:00")
    private LocalDateTime permissionStartTime;

    /**
     * 权限结束时间
     */
    @TableField("permission_end_time")
    @NotNull(message = "权限结束时间不能为空")
    
    @Schema(description = "权限结束时间", example = "2025-11-25 18:00:00")
    private LocalDateTime permissionEndTime;

    /**
     * 最大访问次数
     */
    @TableField("max_access_count")
    @Schema(description = "最大访问次数", example = "3")
    private Integer maxAccessCount;

    /**
     * 已使用访问次数
     */
    @TableField("used_access_count")
    @Schema(description = "已使用访问次数", example = "1")
    private Integer usedAccessCount;

    /**
     * 时间段配置(JSON格式)
     */
    @TableField("time_slot_config")
    @Schema(description = "时间段配置(JSON格式)", example = "{\"timeSlots\":[{\"type\":\"weekday\",\"days\":[1,2,3,4,5],\"timeRanges\":[{\"start\":\"09:00\",\"end\":\"18:00\"}]}]}")
    private String timeSlotConfig;

    /**
     * 权限状态: 0-禁用, 1-启用
     */
    @TableField("permission_status")
    @Schema(description = "权限状态", example = "1")
    private Integer permissionStatus;

    /**
     * 获取可用设备ID列表
     */
    public List<Long> getDeviceIdList() {
        if (deviceIds == null || deviceIds.isEmpty()) {
            return null;
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(deviceIds, List.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置可用设备ID列表
     */
    public void setDeviceIdList(List<Long> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            this.deviceIds = null;
            return;
        }
        try {
            this.deviceIds = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(deviceIds);
        } catch (Exception e) {
            this.deviceIds = null;
        }
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(permissionStatus);
    }

    /**
     * 是否禁用
     */
    public boolean isDisabled() {
        return Integer.valueOf(0).equals(permissionStatus);
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        if (permissionEndTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(permissionEndTime);
    }

    /**
     * 是否还未开始
     */
    public boolean isNotStarted() {
        if (permissionStartTime == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(permissionStartTime);
    }

    /**
     * 是否在有效期内
     */
    public boolean isInValidTimeRange() {
        if (permissionStartTime == null || permissionEndTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(permissionStartTime) && !now.isAfter(permissionEndTime);
    }

    /**
     * 是否还有访问次数
     */
    public boolean hasRemainingAccessCount() {
        if (maxAccessCount == null || maxAccessCount <= 0) {
            return true; // 无限制
        }
        if (usedAccessCount == null) {
            usedAccessCount = 0;
        }
        return usedAccessCount < maxAccessCount;
    }

    /**
     * 获取剩余访问次数
     */
    public Integer getRemainingAccessCount() {
        if (maxAccessCount == null || maxAccessCount <= 0) {
            return null; // 无限制
        }
        if (usedAccessCount == null) {
            usedAccessCount = 0;
        }
        return Math.max(0, maxAccessCount - usedAccessCount);
    }

    /**
     * 增加访问次数
     */
    public void incrementAccessCount() {
        if (usedAccessCount == null) {
            usedAccessCount = 0;
        }
        usedAccessCount++;
    }

    /**
     * 是否可以访问当前设备
     */
    public boolean canAccessDevice(Long deviceId) {
        if (deviceId == null) {
            return false;
        }
        List<Long> allowedDevices = getDeviceIdList();
        if (allowedDevices == null || allowedDevices.isEmpty()) {
            return true; // 无设备限制
        }
        return allowedDevices.contains(deviceId);
    }

    /**
     * 检查是否在允许的时间段内
     */
    public boolean isInAllowedTimeSlot() {
        if (timeSlotConfig == null || timeSlotConfig.isEmpty()) {
            return true; // 无时间段限制
        }
        try {
            // TODO: 实现时间段配置解析和验证逻辑
            // 这里需要解析JSON格式的时间段配置，并验证当前时间是否在允许的时间段内
            return true; // 暂时返回true，后续实现具体逻辑
        } catch (Exception e) {
            return true; // 解析失败时允许访问
        }
    }

    /**
     * 综合检查是否可以访问
     */
    public boolean canAccess(Long deviceId) {
        return isEnabled()
                && isInValidTimeRange()
                && hasRemainingAccessCount()
                && canAccessDevice(deviceId)
                && isInAllowedTimeSlot();
    }

    /**
     * 获取权限状态描述
     */
    public String getPermissionStatusDescription() {
        if (isEnabled()) {
            if (isExpired()) {
                return "已过期";
            } else if (isNotStarted()) {
                return "未生效";
            } else if (!hasRemainingAccessCount()) {
                return "次数已用完";
            } else {
                return "有效";
            }
        } else {
            return "已禁用";
        }
    }
}