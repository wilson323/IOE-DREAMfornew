package net.lab1024.sa.consume.domain.entity;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消费设备实体
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的设备字段定义
 * - 数据验证和审计信息
 * - 软删除和乐观锁
 * - 业务状态判断方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_device")
@Schema(description = "消费设备实体")
public class ConsumeDeviceEntity {

    /**
     * 设备ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    @Schema(description = "设备编码", example = "CONS_001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @TableField("device_name")
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    @Schema(description = "设备名称", example = "食堂1号消费机")
    private String deviceName;

    /**
     * 设备类型（1-消费机 2-充值机 3-闸机 4-自助终端）
     */
    @TableField("device_type")
    @NotNull(message = "设备类型不能为空")
    @Min(value = 1, message = "设备类型值不正确")
    @Max(value = 4, message = "设备类型值不正确")
    @Schema(description = "设备类型", example = "1", allowableValues = {"1", "2", "3", "4"})
    private Integer deviceType;

    /**
     * 设备状态（1-在线 2-离线 3-故障 4-维护 5-停用）
     */
    @TableField("device_status")
    @NotNull(message = "设备状态不能为空")
    @Min(value = 1, message = "设备状态值不正确")
    @Max(value = 5, message = "设备状态值不正确")
    @Schema(description = "设备状态", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer deviceStatus;

    /**
     * 设备位置
     */
    @TableField("device_location")
    @Size(max = 200, message = "设备位置长度不能超过200个字符")
    @Schema(description = "设备位置", example = "A栋1楼食堂")
    private String deviceLocation;

    /**
     * IP地址
     */
    @TableField("ip_address")
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
             message = "IP地址格式不正确")
    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    /**
     * MAC地址
     */
    @TableField("mac_address")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
             message = "MAC地址格式不正确")
    @Schema(description = "MAC地址", example = "00:11:22:33:44:55")
    private String macAddress;

    /**
     * 设备型号
     */
    @TableField("device_model")
    @Size(max = 100, message = "设备型号长度不能超过100个字符")
    @Schema(description = "设备型号", example = "POS-2000")
    private String deviceModel;

    /**
     * 设备制造商
     */
    @TableField("device_manufacturer")
    @Size(max = 100, message = "设备制造商长度不能超过100个字符")
    @Schema(description = "设备制造商", example = "海康威视")
    private String deviceManufacturer;

    /**
     * 固件版本
     */
    @TableField("firmware_version")
    @Size(max = 50, message = "固件版本长度不能超过50个字符")
    @Schema(description = "固件版本", example = "v2.1.0")
    private String firmwareVersion;

    /**
     * 是否支持离线模式
     */
    @TableField("support_offline")
    @Schema(description = "是否支持离线模式", example = "1")
    private Integer supportOffline;

    /**
     * 设备描述
     */
    @TableField("device_description")
    @Size(max = 500, message = "设备描述长度不能超过500个字符")
    @Schema(description = "设备描述", example = "食堂1号窗口消费机，支持多种支付方式")
    private String deviceDescription;

    /**
     * 业务属性（JSON格式，存储业务特定配置）
     */
    @TableField("business_attributes")
    @Schema(description = "业务属性", example = "{\"allowOffline\": true, \"maxAmount\": 1000}")
    private String businessAttributes;

    /**
     * 最后通信时间
     */
    @TableField("last_communication_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最后通信时间", example = "2025-12-21T10:30:00")
    private LocalDateTime lastCommunicationTime;

    /**
     * 所属区域ID
     */
    @TableField("area_id")
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * 健康状态
     */
    @TableField("health_status")
    @Size(max = 50, message = "健康状态长度不能超过50个字符")
    @Schema(description = "健康状态", example = "正常")
    private String healthStatus;

    /**
     * 备注
     */
    @TableField("remark")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "2025年12月新增设备")
    private String remark;

    /**
     * 创建人ID
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-12-21T00:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-12-21T00:00:00")
    private LocalDateTime updateTime;

    /**
     * 删除标记（0-未删除 1-已删除）
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField("version")
    @Schema(description = "乐观锁版本号", example = "1")
    private Integer version;

    // ==================== 业务状态判断方法 ====================

    /**
     * 判断是否在线
     */
    @JsonIgnore
    public boolean isOnline() {
        return deviceStatus != null && deviceStatus == 1;
    }

    /**
     * 判断是否离线
     */
    @JsonIgnore
    public boolean isOffline() {
        return deviceStatus != null && deviceStatus == 2;
    }

    /**
     * 判断是否故障
     */
    @JsonIgnore
    public boolean isFault() {
        return deviceStatus != null && deviceStatus == 3;
    }

    /**
     * 判断是否维护中
     */
    @JsonIgnore
    public boolean isMaintenance() {
        return deviceStatus != null && deviceStatus == 4;
    }

    /**
     * 判断是否停用
     */
    @JsonIgnore
    public boolean isDisabled() {
        return deviceStatus != null && deviceStatus == 5;
    }

    /**
     * 判断是否支持离线模式
     */
    @JsonIgnore
    public boolean supportsOffline() {
        return supportOffline != null && supportOffline == 1;
    }

    /**
     * 判断是否健康
     */
    @JsonIgnore
    public boolean isHealthy() {
        return healthStatus != null && "正常".equals(healthStatus.trim());
    }

    /**
     * 判断是否需要关注（故障或长时间未通信）
     */
    @JsonIgnore
    public boolean needsAttention() {
        if (isFault()) {
            return true;
        }

        // 如果超过30分钟没有通信，需要关注
        if (lastCommunicationTime != null) {
            LocalDateTime now = LocalDateTime.now();
            return now.minusMinutes(30).isAfter(lastCommunicationTime);
        }

        return false;
    }

    /**
     * 获取设备类型名称
     */
    @JsonIgnore
    public String getDeviceTypeName() {
        if (deviceType == null) {
            return "";
        }
        switch (deviceType) {
            case 1: return "消费机";
            case 2: return "充值机";
            case 3: return "闸机";
            case 4: return "自助终端";
            default: return "未知";
        }
    }

    /**
     * 获取设备状态名称
     */
    @JsonIgnore
    public String getDeviceStatusName() {
        if (deviceStatus == null) {
            return "";
        }
        switch (deviceStatus) {
            case 1: return "在线";
            case 2: return "离线";
            case 3: return "故障";
            case 4: return "维护中";
            case 5: return "停用";
            default: return "未知";
        }
    }

    /**
     * 获取设备状态颜色（用于前端显示）
     */
    @JsonIgnore
    public String getDeviceStatusColor() {
        if (deviceStatus == null) {
            return "#95a5a6"; // 灰色
        }
        switch (deviceStatus) {
            case 1: return "#2ed573"; // 绿色 - 在线
            case 2: return "#ffa502"; // 橙色 - 离线
            case 3: return "#ff4757"; // 红色 - 故障
            case 4: return "#f39c12"; // 橙色 - 维护中
            case 5: return "#718093"; // 深灰色 - 停用
            default: return "#95a5a6";
        }
    }

    /**
     * 获取连接状态描述
     */
    @JsonIgnore
    public String getConnectionStatus() {
        if (lastCommunicationTime == null) {
            return "从未通信";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(lastCommunicationTime, now).toMinutes();

        if (minutes <= 5) {
            return "实时连接";
        } else if (minutes <= 30) {
            return "近期活跃";
        } else if (minutes <= 60) {
            return "1小时内";
        } else if (minutes <= 1440) {
            return "1天内";
        } else {
            return "超过1天";
        }
    }

    /**
     * 验证业务规则
     */
    @JsonIgnore
    public java.util.List<String> validateBusinessRules() {
        java.util.List<String> errors = new java.util.ArrayList<>();

        // 验证IP地址和MAC地址不能同时为空
        if ((ipAddress == null || ipAddress.trim().isEmpty()) &&
            (macAddress == null || macAddress.trim().isEmpty())) {
            errors.add("IP地址和MAC地址至少需要填写一个");
        }

        // 验证时间格式
        if (lastCommunicationTime != null && lastCommunicationTime.isAfter(LocalDateTime.now())) {
            errors.add("最后通信时间不能晚于当前时间");
        }

        return errors;
    }
}