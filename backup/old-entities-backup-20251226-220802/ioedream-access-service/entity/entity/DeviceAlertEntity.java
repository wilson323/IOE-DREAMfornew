package net.lab1024.sa.access.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 设备告警实体
 *
 * 记录设备产生的所有告警信息，包括：
 * - 离线告警
 * - 故障告警
 * - 防破坏告警
 * - 低电量告警
 * - 被阻止告警
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_device_alert")
@Schema(description = "设备告警实体")
public class DeviceAlertEntity {

    /**
     * 告警ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "告警ID", example = "1")
    private Long alertId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @Schema(description = "设备ID", example = "DEV001")
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    @Schema(description = "设备名称", example = "主入口门禁")
    private String deviceName;

    /**
     * 设备类型：1-门禁 2-考勤 3-消费 4-视频
     */
    @TableField("device_type")
    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    /**
     * 区域ID
     */
    @TableField("area_id")
    @Schema(description = "区域ID", example = "100")
    private Long areaId;

    /**
     * 区域名称
     */
    @TableField("area_name")
    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    /**
     * 告警类型：OFFLINE-离线 ONLINE-上线 FAULT-故障 TAMPER-防破坏 LOW_BATTERY-低电量 BLOCKED-被阻止
     */
    @TableField("alert_type")
    @Schema(description = "告警类型", example = "OFFLINE")
    @NotBlank(message = "告警类型不能为空")
    private String alertType;

    /**
     * 告警级别：1-提示 2-警告 3-严重 4-紧急
     */
    @TableField("alert_level")
    @Schema(description = "告警级别", example = "3")
    @NotNull(message = "告警级别不能为空")
    private Integer alertLevel;

    /**
     * 告警标题
     */
    @TableField("alert_title")
    @Schema(description = "告警标题", example = "设备离线告警")
    @NotBlank(message = "告警标题不能为空")
    private String alertTitle;

    /**
     * 告警详细信息
     */
    @TableField("alert_message")
    @Schema(description = "告警详细信息", example = "设备超过5分钟无心跳信号")
    private String alertMessage;

    /**
     * 告警状态：1-未处理 2-处理中 3-已处理 4-已忽略
     */
    @TableField("alert_status")
    @Schema(description = "告警状态", example = "1")
    private Integer alertStatus;

    /**
     * 处理人ID
     */
    @TableField("handled_by")
    @Schema(description = "处理人ID", example = "1")
    private Long handledBy;

    /**
     * 处理时间
     */
    @TableField("handled_time")
    @Schema(description = "处理时间", example = "2025-12-26T10:30:00")
    private LocalDateTime handledTime;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    @Schema(description = "处理备注", example = "已派单维修")
    private String handleRemark;

    /**
     * 触发规则ID
     */
    @TableField("rule_id")
    @Schema(description = "触发规则ID", example = "1")
    private Long ruleId;

    /**
     * 触发规则名称
     */
    @TableField("rule_name")
    @Schema(description = "触发规则名称", example = "设备离线告警")
    private String ruleName;

    /**
     * 告警触发时间
     */
    @TableField("trigger_time")
    @Schema(description = "告警触发时间", example = "2025-12-26T10:00:00")
    @NotNull(message = "触发时间不能为空")
    private LocalDateTime triggerTime;

    /**
     * 触发值
     */
    @TableField("trigger_value")
    @Schema(description = "触发值", example = "300")
    private String triggerValue;

    /**
     * 恢复时间
     */
    @TableField("recovered_time")
    @Schema(description = "恢复时间", example = "2025-12-26T10:30:00")
    private LocalDateTime recoveredTime;

    /**
     * 恢复操作人ID
     */
    @TableField("recovered_by")
    @Schema(description = "恢复操作人ID", example = "1")
    private Long recoveredBy;

    /**
     * 是否已恢复：0-未恢复 1-已恢复
     */
    @TableField("is_recovered")
    @Schema(description = "是否已恢复", example = "0")
    private Integer isRecovered;

    /**
     * 是否已发送通知：0-否 1-是
     */
    @TableField("notification_sent")
    @Schema(description = "是否已发送通知", example = "1")
    private Integer notificationSent;

    /**
     * 通知发送次数
     */
    @TableField("notification_count")
    @Schema(description = "通知发送次数", example = "3")
    private Integer notificationCount;

    /**
     * 最后一次通知时间
     */
    @TableField("last_notification_time")
    @Schema(description = "最后一次通知时间", example = "2025-12-26T10:05:00")
    private LocalDateTime lastNotificationTime;

    /**
     * 扩展数据（JSON格式）
     */
    @TableField("extended_data")
    @Schema(description = "扩展数据（JSON）", example = "{}")
    private String extendedData;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间", example = "2025-12-26T10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2025-12-26T10:00:00")
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    // ==================== 便捷方法 ====================

    /**
     * 判断是否未处理
     */
    public boolean isUnhandled() {
        return this.alertStatus != null && this.alertStatus == 1;
    }

    /**
     * 判断是否处理中
     */
    public boolean isHandling() {
        return this.alertStatus != null && this.alertStatus == 2;
    }

    /**
     * 判断是否已处理
     */
    public boolean isHandled() {
        return this.alertStatus != null && this.alertStatus == 3;
    }

    /**
     * 判断是否已忽略
     */
    public boolean isIgnored() {
        return this.alertStatus != null && this.alertStatus == 4;
    }

    /**
     * 判断是否已恢复
     */
    public boolean isRecovered() {
        return this.isRecovered != null && this.isRecovered == 1;
    }

    /**
     * 判断是否已发送通知
     */
    public boolean isNotificationSent() {
        return this.notificationSent != null && this.notificationSent == 1;
    }

    /**
     * 判断是否离线告警
     */
    public boolean isOfflineAlert() {
        return "OFFLINE".equals(this.alertType);
    }

    /**
     * 判断是否故障告警
     */
    public boolean isFaultAlert() {
        return "FAULT".equals(this.alertType);
    }

    /**
     * 判断是否紧急告警（级别4）
     */
    public boolean isEmergency() {
        return this.alertLevel != null && this.alertLevel == 4;
    }

    /**
     * 判断是否严重告警（级别3+）
     */
    public boolean isSevere() {
        return this.alertLevel != null && this.alertLevel >= 3;
    }
}
