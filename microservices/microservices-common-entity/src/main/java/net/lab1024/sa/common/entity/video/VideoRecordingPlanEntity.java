package net.lab1024.sa.common.entity.video;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 视频录像计划实体类
 * <p>
 * 管理视频录像计划信息，支持多种录像策略
 * 提供录像计划配置、时间表管理和优先级控制等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 全天录像计划
 * - 定时录像计划
 * - 事件触发录像
 * - 移动侦测录像
 * - 报警录像
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video_recording_plan")
@Schema(description = "视频录像计划实体")
public class VideoRecordingPlanEntity extends BaseEntity {

    /**
     * 计划ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "计划ID")
    private Long planId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private String deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 计划名称
     */
    @Schema(description = "计划名称")
    private String planName;

    /**
     * 计划类型（1-全天录像 2-定时录像 3-事件触发 4-移动侦测 5-报警录像）
     */
    @Schema(description = "计划类型（1-全天录像 2-定时录像 3-事件触发 4-移动侦测 5-报警录像）")
    private Integer planType;

    /**
     * 计划类型名称
     */
    @Schema(description = "计划类型名称")
    private String planTypeName;

    /**
     * 优先级（1-10，数字越大优先级越高）
     */
    @Schema(description = "优先级（1-10）")
    private Integer priority;

    /**
     * 录像开始时间
     */
    @Schema(description = "录像开始时间")
    private LocalTime startTime;

    /**
     * 录像结束时间
     */
    @Schema(description = "录像结束时间")
    private LocalTime endTime;

    /**
     * 生效星期（JSON数组，如[1,2,3,4,5]表示周一到周五）
     */
    @Schema(description = "生效星期（JSON数组）")
    private String weekdays;

    /**
     * 生效日期开始
     */
    @Schema(description = "生效日期开始")
    private LocalDateTime effectiveStartDate;

    /**
     * 生效日期结束
     */
    @Schema(description = "生效日期结束")
    private LocalDateTime effectiveEndDate;

    /**
     * 录像类型（1-连续录像 2-动态检测 3-报警触发）
     */
    @Schema(description = "录像类型（1-连续录像 2-动态检测 3-报警触发）")
    private Integer recordingType;

    /**
     * 视频质量（1-主码流 2-子码流 3-第三码流）
     */
    @Schema(description = "视频质量（1-主码流 2-子码流 3-第三码流）")
    private Integer videoQuality;

    /**
     * 视频质量名称
     */
    @Schema(description = "视频质量名称")
    private String videoQualityName;

    /**
     * 分辨率（如1080P、720P、D1）
     */
    @Schema(description = "分辨率")
    private String resolution;

    /**
     * 帧率（fps）
     */
    @Schema(description = "帧率（fps）")
    private Integer frameRate;

    /**
     * 码率（kbps）
     */
    @Schema(description = "码率（kbps）")
    private Integer bitrate;

    /**
     * 预录时间（秒，事件触发前的录像时间）
     */
    @Schema(description = "预录时间（秒）")
    private Integer preRecordTime;

    /**
     * 延录时间（秒，事件触发后的录像时间）
     */
    @Schema(description = "延录时间（秒）")
    private Integer postRecordTime;

    /**
     * 移动侦测灵敏度（1-10，数字越大越灵敏）
     */
    @Schema(description = "移动侦测灵敏度（1-10）")
    private Integer motionSensitivity;

    /**
     * 报警类型（JSON数组，如[1,2,3]对应不同报警类型）
     */
    @Schema(description = "报警类型（JSON数组）")
    private String alarmTypes;

    /**
     * ROI区域（JSON格式，感兴趣区域配置）
     */
    @Schema(description = "ROI区域（JSON格式）")
    private String roiRegions;

    /**
     * 是否启用（0-禁用 1-启用）
     */
    @Schema(description = "是否启用")
    private Integer enabled;

    /**
     * 状态（0-未生效 1-生效中 2-已过期 3-已停用）
     */
    @Schema(description = "状态（0-未生效 1-生效中 2-已过期 3-已停用）")
    private Integer status;

    /**
     * 计划描述
     */
    @Schema(description = "计划描述")
    private String description;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    // ==================== 业务方法 ====================

    /**
     * 检查计划是否启用
     *
     * @return true-启用，false-禁用
     */
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }

    /**
     * 检查计划是否生效中
     *
     * @return true-生效中，false-未生效
     */
    public boolean isActive() {
        return status != null && status == 1 && isEnabled();
    }

    /**
     * 检查计划是否已过期
     *
     * @return true-已过期，false-未过期
     */
    public boolean isExpired() {
        return effectiveEndDate != null && effectiveEndDate.isBefore(LocalDateTime.now());
    }

    /**
     * 检查是否在指定时间录像
     *
     * @param currentTime 当前时间
     * @return true-应该录像，false-不应该录像
     */
    public boolean shouldRecordAtTime(LocalDateTime currentTime) {
        if (!isEnabled() || isExpired()) {
            return false;
        }

        // 检查生效日期范围
        if (effectiveStartDate != null && currentTime.isBefore(effectiveStartDate)) {
            return false;
        }
        if (effectiveEndDate != null && currentTime.isAfter(effectiveEndDate)) {
            return false;
        }

        // 检查星期
        if (!matchesWeekday(currentTime)) {
            return false;
        }

        // 检查时间范围
        LocalTime time = currentTime.toLocalTime();
        if (startTime != null && endTime != null) {
            if (startTime.isBefore(endTime)) {
                // 正常时间范围（如08:00-18:00）
                return !time.isBefore(startTime) && !time.isAfter(endTime);
            } else {
                // 跨天时间范围（如22:00-06:00）
                return !time.isBefore(startTime) || !time.isAfter(endTime);
            }
        }

        return true;
    }

    /**
     * 检查是否匹配指定星期
     *
     * @param datetime 日期时间
     * @return true-匹配，false-不匹配
     */
    public boolean matchesWeekday(LocalDateTime datetime) {
        if (weekdays == null || weekdays.isEmpty()) {
            return true; // 未设置星期限制，默认每天生效
        }

        int weekday = datetime.getDayOfWeek().getValue();
        try {
            // weekdays格式为JSON数组，如[1,2,3,4,5]
            String[] weekdayArray = weekdays.replaceAll("[\\[\\]\"]", "").split(",");
            for (String day : weekdayArray) {
                if (day.trim().equals(String.valueOf(weekday))) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 解析失败，返回true
        }

        return false;
    }

    /**
     * 检查优先级是否高于另一个计划
     *
     * @param other 另一个计划
     * @return true-优先级更高，false-优先级更低或相同
     */
    public boolean hasHigherPriorityThan(VideoRecordingPlanEntity other) {
        if (other == null || other.getPriority() == null) {
            return true;
        }
        return priority != null && priority > other.getPriority();
    }

    /**
     * 获取录像类型名称
     *
     * @return 录像类型名称
     */
    public String getRecordingTypeName() {
        if (recordingType == null) {
            return "未知";
        }
        switch (recordingType) {
            case 1: return "连续录像";
            case 2: return "动态检测";
            case 3: return "报警触发";
            default: return "未知";
        }
    }

    /**
     * 计算录像时长（秒）
     *
     * @return 录像时长，跨天时计算实际时长
     */
    public int calculateRecordingDuration() {
        if (startTime == null || endTime == null) {
            return 86400; // 24小时
        }

        int startSeconds = startTime.toSecondOfDay();
        int endSeconds = endTime.toSecondOfDay();

        if (startSeconds < endSeconds) {
            // 正常时间范围
            return endSeconds - startSeconds;
        } else {
            // 跨天时间范围
            return (86400 - startSeconds) + endSeconds;
        }
    }

    /**
     * 获取计划类型名称
     *
     * @return 计划类型名称
     */
    public String getPlanTypeName() {
        if (planType == null) {
            return "未知";
        }
        switch (planType) {
            case 1: return "全天录像";
            case 2: return "定时录像";
            case 3: return "事件触发";
            case 4: return "移动侦测";
            case 5: return "报警录像";
            default: return "未知";
        }
    }
}
