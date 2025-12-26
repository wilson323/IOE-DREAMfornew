package net.lab1024.sa.access.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 门禁设备权限实体
 * <p>
 * 对应数据库表: t_access_user_permission
 * 门禁设备验证权限的独立模型，避免与软件权限/组织关系混用。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_user_permission")
public class AccessUserPermissionEntity extends BaseEntity {

    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;

    @TableField("user_id")
    private Long userId;

    @TableField("area_id")
    private Long areaId;

    @TableField("permission_type")
    private String permissionType;

    @TableField("permission_status")
    private Integer permissionStatus;

    @TableField("permission_level")
    private Integer permissionLevel;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("device_sync_status")
    private Integer deviceSyncStatus;

    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("last_sync_error")
    private String lastSyncError;

    @TableField("access_time_config")
    private String accessTimes;

    @TableField(exist = false)
    private String areaCode;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String allowStartTime;

    @TableField(exist = false)
    private String allowEndTime;

    @TableField(exist = false)
    private Boolean workdayOnly;

    @TableField(exist = false)
    private String accessPermissions;

    @TableField(exist = false)
    private String extendedAttributes;

    @TableField(exist = false)
    private Boolean permanent;

    public Long getId() {
        return permissionId;
    }

    public void setId(Long id) {
        this.permissionId = id;
    }

    public LocalDateTime getEffectiveTime() {
        return startTime;
    }

    public LocalDateTime getExpireTime() {
        return endTime;
    }
}
