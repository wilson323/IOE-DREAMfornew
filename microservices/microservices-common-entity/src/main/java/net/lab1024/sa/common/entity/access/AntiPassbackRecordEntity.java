package net.lab1024.sa.common.entity.access;

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
 * 反潜回检测记录实体
 *
 * 记录所有反潜回检测日志，包括：
 * - 正常通行记录
 * - 违规通行记录（过快重复通行）
 * - 告警记录
 * - 处理记录
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_anti_passback_record")
@Schema(description = "反潜回检测记录实体")
public class AntiPassbackRecordEntity {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    /**
     * 配置ID
     */
    @TableField("config_id")
    @Schema(description = "配置ID", example = "1")
    @NotNull(message = "配置ID不能为空")
    private Long configId;

    /**
     * 配置编码
     */
    @TableField("config_code")
    @Schema(description = "配置编码", example = "GLOBAL_SOFT_PASSBACK")
    @NotBlank(message = "配置编码不能为空")
    private String configCode;

    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID", example = "1001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 用户类型：1-员工 2-访客 3-黑名单
     */
    @TableField("user_type")
    @Schema(description = "用户类型：1-员工 2-访客 3-黑名单", example = "1")
    private Integer userType;

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
     * 通行时间
     */
    @TableField("access_time")
    @Schema(description = "通行时间", example = "2025-12-26T10:30:00")
    @NotNull(message = "通行时间不能为空")
    private LocalDateTime accessTime;

    /**
     * 通行方向：1-进 2-出
     */
    @TableField("access_direction")
    @Schema(description = "通行方向：1-进 2-出", example = "1")
    private Integer accessDirection;

    /**
     * 通行结果：0-失败（被阻止） 1-成功
     */
    @TableField("access_result")
    @Schema(description = "通行结果：0-失败 1-成功", example = "1")
    private Integer accessResult;

    /**
     * 卡号
     */
    @TableField("access_card_no")
    @Schema(description = "卡号", example = "1234567890")
    private String accessCardNo;

    /**
     * 生物识别类型：1-人脸 2-指纹 3-掌纹
     */
    @TableField("biometric_type")
    @Schema(description = "生物识别类型：1-人脸 2-指纹 3-掌纹", example = "1")
    private Integer biometricType;

    /**
     * 反潜回模式：1-全局 2-区域 3-软 4-硬
     */
    @TableField("anti_passback_mode")
    @Schema(description = "反潜回模式", example = "3")
    @NotNull(message = "反潜回模式不能为空")
    private Integer antiPassbackMode;

    /**
     * 是否违规：0-否 1-是
     */
    @TableField("is_violation")
    @Schema(description = "是否违规", example = "0")
    private Integer isViolation;

    /**
     * 违规类型：PASSBACK_ALREADY_IN-已在内 PASSBACK_TOO_SOON-过快 PASSBACK_BLOCKED-被阻止
     */
    @TableField("violation_type")
    @Schema(description = "违规类型", example = "PASSBACK_TOO_SOON")
    private String violationType;

    /**
     * 违规原因说明
     */
    @TableField("violation_reason")
    @Schema(description = "违规原因", example = "60秒内重复通行")
    private String violationReason;

    /**
     * 上一次通行时间
     */
    @TableField("last_access_time")
    @Schema(description = "上一次通行时间", example = "2025-12-26T10:29:00")
    private LocalDateTime lastAccessTime;

    /**
     * 上一次通行设备ID
     */
    @TableField("last_access_device")
    @Schema(description = "上一次通行设备ID", example = "DEV001")
    private String lastAccessDevice;

    /**
     * 上一次通行方向：1-进 2-出
     */
    @TableField("last_access_direction")
    @Schema(description = "上一次通行方向", example = "1")
    private Integer lastAccessDirection;

    /**
     * 与上次通行时间差（秒）
     */
    @TableField("time_diff_seconds")
    @Schema(description = "与上次通行时间差（秒）", example = "30")
    private Integer timeDiffSeconds;

    /**
     * 是否产生告警：0-否 1-是
     */
    @TableField("is_alarm")
    @Schema(description = "是否产生告警", example = "0")
    private Integer isAlarm;

    /**
     * 告警级别：1-提示 2-警告 3-严重
     */
    @TableField("alarm_level")
    @Schema(description = "告警级别：1-提示 2-警告 3-严重", example = "2")
    private Integer alarmLevel;

    /**
     * 告警消息
     */
    @TableField("alarm_message")
    @Schema(description = "告警消息", example = "检测到反潜回违规")
    private String alarmMessage;

    /**
     * 是否已处理：0-否 1-是
     */
    @TableField("is_handled")
    @Schema(description = "是否已处理", example = "0")
    private Integer isHandled;

    /**
     * 处理人ID
     */
    @TableField("handle_user_id")
    @Schema(description = "处理人ID", example = "1")
    private Long handleUserId;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    @Schema(description = "处理时间", example = "2025-12-26T11:00:00")
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    @Schema(description = "处理备注", example = "已核实为误报")
    private String handleRemark;

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
    @Schema(description = "创建时间", example = "2025-12-26T10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间", example = "2025-12-26T10:30:00")
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
     * 判断是否违规
     */
    public boolean isViolation() {
        return this.isViolation != null && this.isViolation == 1;
    }

    /**
     * 判断是否告警
     */
    public boolean isAlarm() {
        return this.isAlarm != null && this.isAlarm == 1;
    }

    /**
     * 判断是否已处理
     */
    public boolean isHandled() {
        return this.isHandled != null && this.isHandled == 1;
    }

    /**
     * 判断是否通行成功
     */
    public boolean isAccessSuccess() {
        return this.accessResult != null && this.accessResult == 1;
    }

    /**
     * 判断是否进入方向
     */
    public boolean isInDirection() {
        return this.accessDirection != null && this.accessDirection == 1;
    }

    /**
     * 判断是否外出方向
     */
    public boolean isOutDirection() {
        return this.accessDirection != null && this.accessDirection == 2;
    }
}
