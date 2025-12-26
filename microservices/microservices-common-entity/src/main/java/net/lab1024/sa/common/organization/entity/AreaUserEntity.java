package net.lab1024.sa.common.organization.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 区域用户关联实体
 * <p>
 * 对应数据库表: t_area_user
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_area_user")
public class AreaUserEntity extends BaseEntity {

    @TableId(value = "relation_id", type = IdType.AUTO)
    private Long relationId;

    @TableField("area_id")
    private Long areaId;

    @TableField("user_id")
    private Long userId;

    @TableField("user_type")
    private String userType;

    @TableField("relation_type")
    private String relationType;

    @TableField("access_level")
    private Integer accessLevel;

    @TableField("access_time_config")
    private String accessTimeConfig;

    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    @TableField("grant_user_id")
    private Long grantUserId;

    @TableField("grant_time")
    private LocalDateTime grantTime;

    @TableField("revoke_user_id")
    private Long revokeUserId;

    @TableField("revoke_time")
    private LocalDateTime revokeTime;

    @TableField("grant_remark")
    private String grantRemark;

    @TableField("status")
    private Integer status;

    @TableField("permission_level")
    private Integer permissionLevel; // 权限级别：1-只读 2-可编辑 3-管理员

    @TableField("device_sync_status")
    private Integer deviceSyncStatus; // 设备同步状态：1-未同步 2-已同步 3-同步失败

    @TableField("effective_time")
    private java.time.LocalDateTime effectiveTime; // 生效时间

    @TableField("expire_time")
    private java.time.LocalDateTime expireTime; // 失效时间

    /**
     * 获取关联ID（兼容BaseEntity的getId方法）
     *
     * @return 关联ID
     */
    public Long getId() {
        return relationId;
    }

    /**
     * 设置关联ID（兼容BaseEntity的setId方法）
     *
     * @param id 关联ID
     */
    public void setId(Long id) {
        this.relationId = id;
    }

    /**
     * 获取用户真实姓名（需要从UserEntity关联获取，这里提供兼容方法）
     * <p>
     * 注意：实际实现中需要通过userId关联UserEntity获取realName
     * </p>
     *
     * @return 用户真实姓名
     */
    public String getRealName() {
        // 实际实现中需要通过userId关联UserEntity获取
        // 这里返回null，由Service层处理关联查询
        return null;
    }

    /**
     * 获取用户名（需要从UserEntity关联获取，这里提供兼容方法）
     * <p>
     * 注意：实际实现中需要通过userId关联UserEntity获取username
     * </p>
     *
     * @return 用户名
     */
    public String getUsername() {
        // 实际实现中需要通过userId关联UserEntity获取
        // 这里返回null，由Service层处理关联查询
        return null;
    }

    /**
     * 获取权限级别
     *
     * @return 权限级别
     */
    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    /**
     * 设置权限级别
     *
     * @param permissionLevel 权限级别
     */
    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    /**
     * 获取设备同步状态
     *
     * @return 设备同步状态
     */
    public Integer getDeviceSyncStatus() {
        return deviceSyncStatus;
    }

    /**
     * 设置设备同步状态
     *
     * @param deviceSyncStatus 设备同步状态
     */
    public void setDeviceSyncStatus(Integer deviceSyncStatus) {
        this.deviceSyncStatus = deviceSyncStatus;
    }

    /**
     * 获取生效时间
     *
     * @return 生效时间
     */
    public java.time.LocalDateTime getEffectiveTime() {
        return effectiveTime != null ? effectiveTime : validStartTime;
    }

    /**
     * 设置生效时间
     *
     * @param effectiveTime 生效时间
     */
    public void setEffectiveTime(java.time.LocalDateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
        this.validStartTime = effectiveTime;
    }

    /**
     * 获取失效时间
     *
     * @return 失效时间
     */
    public java.time.LocalDateTime getExpireTime() {
        return expireTime != null ? expireTime : validEndTime;
    }

    /**
     * 设置失效时间
     *
     * @param expireTime 失效时间
     */
    public void setExpireTime(java.time.LocalDateTime expireTime) {
        this.expireTime = expireTime;
        this.validEndTime = expireTime;
    }

    /**
     * 获取区域编码（兼容方法，需要从AreaEntity关联获取）
     *
     * @return 区域编码
     */
    public String getAreaCode() {
        // 实际实现中需要通过areaId关联AreaEntity获取
        // 这里返回null，由Service层处理关联查询
        return null;
    }

    /**
     * 获取是否永久权限（兼容方法）
     * <p>
     * 根据expireTime判断：如果expireTime为null或很远的未来时间，则为永久权限
     * </p>
     *
     * @return 是否永久权限
     */
    public Integer getPermanent() {
        // 如果expireTime为null或很远的未来时间，则为永久权限
        if (expireTime == null || validEndTime == null) {
            return 1; // 永久权限
        }
        // 如果失效时间在100年后，认为是永久权限
        LocalDateTime farFuture = LocalDateTime.now().plusYears(100);
        return (expireTime.isAfter(farFuture) || validEndTime.isAfter(farFuture)) ? 1 : 0;
    }

    /**
     * 获取允许开始时间（兼容方法，从accessTimeConfig解析）
     *
     * @return 允许开始时间（LocalTime）
     */
    public java.time.LocalTime getAllowStartTime() {
        // 实际实现中应该从accessTimeConfig JSON中解析
        // 这里返回null，由Service层处理
        return null;
    }

    /**
     * 获取允许结束时间（兼容方法，从accessTimeConfig解析）
     *
     * @return 允许结束时间（LocalTime）
     */
    public java.time.LocalTime getAllowEndTime() {
        // 实际实现中应该从accessTimeConfig JSON中解析
        // 这里返回null，由Service层处理
        return null;
    }

    /**
     * 获取是否仅工作日（兼容方法，从accessTimeConfig解析）
     *
     * @return 是否仅工作日
     */
    public Integer getWorkdayOnly() {
        // 实际实现中应该从accessTimeConfig JSON中解析
        // 这里返回0（不限制），由Service层处理
        return 0;
    }

    /**
     * 获取访问权限配置（兼容方法，从accessTimeConfig解析）
     *
     * @return 访问权限配置JSON字符串
     */
    public String getAccessPermissions() {
        // 返回accessTimeConfig作为访问权限配置
        return accessTimeConfig;
    }

    /**
     * 获取扩展属性（兼容方法）
     *
     * @return 扩展属性JSON字符串
     */
    public String getExtendedAttributes() {
        // AreaUserEntity没有扩展属性字段，返回null
        return null;
    }

    /**
     * 获取最后同步时间（兼容方法）
     *
     * @return 最后同步时间
     */
    public LocalDateTime getLastUserSyncTime() {
        // AreaUserEntity没有lastUserSyncTime字段，返回null
        // 实际应该从deviceSyncStatus相关字段获取，这里返回null
        return null;
    }

    /**
     * 获取最后同步时间（兼容方法，别名）
     * <p>
     * 与getLastUserSyncTime()相同，用于兼容不同的调用方式
     * </p>
     *
     * @return 最后同步时间
     */
    public LocalDateTime getLastSyncTime() {
        return getLastUserSyncTime();
    }

    /**
     * 获取重试次数（兼容方法）
     *
     * @return 重试次数
     */
    public Integer getRetryCount() {
        // AreaUserEntity没有retryCount字段，返回0
        return 0;
    }
}
