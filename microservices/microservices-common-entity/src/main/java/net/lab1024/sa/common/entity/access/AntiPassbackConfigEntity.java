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
 * 反潜回配置实体
 *
 * 支持四种反潜回模式：
 * 1. 全局反潜回 - 全局范围内防止重复通行
 * 2. 区域反潜回 - 指定区域内防止重复通行
 * 3. 软反潜回 - 时间窗口内允许第二次通行（先进后出）
 * 4. 硬反潜回 - 严格禁止重复通行，触发后阻止一段时间
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_anti_passback_config")
@Schema(description = "反潜回配置实体")
public class AntiPassbackConfigEntity {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "配置ID", example = "1")
    private Long configId;

    /**
     * 配置名称
     */
    @TableField("config_name")
    @Schema(description = "配置名称", example = "全局软反潜回配置")
    @NotBlank(message = "配置名称不能为空")
    private String configName;

    /**
     * 配置编码（唯一标识）
     */
    @TableField("config_code")
    @Schema(description = "配置编码", example = "GLOBAL_SOFT_PASSBACK")
    @NotBlank(message = "配置编码不能为空")
    private String configCode;

    /**
     * 反潜回模式
     * 1-全局反潜回 2-区域反潜回 3-软反潜回 4-硬反潜回
     */
    @TableField("anti_passback_mode")
    @Schema(description = "反潜回模式：1-全局 2-区域 3-软 4-硬", example = "3")
    @NotNull(message = "反潜回模式不能为空")
    private Integer antiPassbackMode;

    /**
     * 区域ID（区域反潜回时使用）
     */
    @TableField("area_id")
    @Schema(description = "区域ID", example = "100")
    private Long areaId;

    /**
     * 设备ID（指定设备时使用）
     */
    @TableField("device_id")
    @Schema(description = "设备ID", example = "DEV001")
    private String deviceId;

    /**
     * 设备列表（JSON数组，多设备反潜回）
     */
    @TableField("device_ids")
    @Schema(description = "设备列表（JSON数组）", example = "[\"DEV001\",\"DEV002\"]")
    private String deviceIds;

    /**
     * 时间窗口（秒），软反潜回允许时间内第二次通行
     */
    @TableField("time_window_seconds")
    @Schema(description = "时间窗口（秒）", example = "60")
    private Integer timeWindowSeconds;

    /**
     * 是否允许先进后出：0-否 1-是
     */
    @TableField("allow_first_exit")
    @Schema(description = "是否允许先进后出", example = "1")
    private Integer allowFirstExit;

    /**
     * 阻止时长（秒），硬反潜回触发后的阻止时长
     */
    @TableField("block_duration_seconds")
    @Schema(description = "阻止时长（秒）", example = "300")
    private Integer blockDurationSeconds;

    /**
     * 触发时是否告警：0-否 1-是
     */
    @TableField("alarm_on_trigger")
    @Schema(description = "触发时是否告警", example = "1")
    private Integer alarmOnTrigger;

    /**
     * 用户范围：1-全部用户 2-指定用户组 3-指定用户
     */
    @TableField("user_scope")
    @Schema(description = "用户范围：1-全部 2-用户组 3-指定用户", example = "1")
    private Integer userScope;

    /**
     * 用户范围值（JSON：用户组ID列表或用户ID列表）
     */
    @TableField("user_scope_value")
    @Schema(description = "用户范围值（JSON）", example = "[1,2,3]")
    private String userScopeValue;

    /**
     * 生效时间
     */
    @TableField("effective_time")
    @Schema(description = "生效时间", example = "2025-01-01T00:00:00")
    @NotNull(message = "生效时间不能为空")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间（NULL表示永久有效）
     */
    @TableField("expire_time")
    @Schema(description = "失效时间", example = "2025-12-31T23:59:59")
    private LocalDateTime expireTime;

    /**
     * 优先级（1-100，数值越大优先级越高）
     */
    @TableField("priority")
    @Schema(description = "优先级", example = "50")
    private Integer priority;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    @Schema(description = "状态：0-禁用 1-启用", example = "1")
    private Integer status;

    /**
     * 描述
     */
    @TableField("description")
    @Schema(description = "描述", example = "全局软反潜回配置")
    private String description;

    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注", example = "默认启用")
    private String remark;

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
     * 创建人ID
     */
    @TableField("create_user_id")
    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    /**
     * 删除标记：0-未删除 1-已删除
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
    @Schema(description = "版本号", example = "0")
    private Integer version;

    // ==================== 便捷方法 ====================

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 判断是否全局反潜回
     */
    public boolean isGlobalMode() {
        return this.antiPassbackMode != null && this.antiPassbackMode == 1;
    }

    /**
     * 判断是否区域反潜回
     */
    public boolean isAreaMode() {
        return this.antiPassbackMode != null && this.antiPassbackMode == 2;
    }

    /**
     * 判断是否软反潜回
     */
    public boolean isSoftMode() {
        return this.antiPassbackMode != null && this.antiPassbackMode == 3;
    }

    /**
     * 判断是否硬反潜回
     */
    public boolean isHardMode() {
        return this.antiPassbackMode != null && this.antiPassbackMode == 4;
    }

    /**
     * 判断是否在有效期内
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        boolean afterEffective = now.isAfter(this.effectiveTime) || now.isEqual(this.effectiveTime);
        boolean beforeExpire = this.expireTime == null ||
                now.isBefore(this.expireTime) || now.isEqual(this.expireTime);
        return afterEffective && beforeExpire;
    }

    /**
     * 判断是否允许先进后出
     */
    public boolean isAllowFirstExit() {
        return this.allowFirstExit != null && this.allowFirstExit == 1;
    }

    /**
     * 判断是否触发告警
     */
    public boolean isAlarmOnTrigger() {
        return this.alarmOnTrigger != null && this.alarmOnTrigger == 1;
    }
}
