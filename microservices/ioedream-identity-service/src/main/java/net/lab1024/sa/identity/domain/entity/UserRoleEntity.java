package net.lab1024.sa.identity.domain.entity;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 用户角色关联实体类
 * 基于现有RbacUserRoleEntity重构，适配Identity Service需求
 * 表名: t_user_role
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原RbacUserRoleEntity重构)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@org.springframework.stereotype.Repository
@com.baomidou.mybatisplus.annotation.TableName("t_user_role")
public class UserRoleEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @com.baomidou.mybatisplus.annotation.TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;

    /**
     * 用户ID（对应原employee_id）
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 授权时间（基于原effectiveTime）
     */
    private LocalDateTime grantTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态(0-禁用,1-启用)
     */
    private Integer status;

    /**
     * 授权类型(MANUAL|AUTO)
     */
    private String grantType;

    /**
     * 授权来源
     */
    private String grantSource;

    // 兼容性方法，保持与原有RbacUserRoleEntity的兼容性
    /**
     * 获取生效时间（兼容性方法）
     */
    public LocalDateTime getEffectiveTime() {
        return grantTime;
    }

    /**
     * 设置生效时间（兼容性方法）
     */
    public void setEffectiveTime(LocalDateTime effectiveTime) {
        this.grantTime = effectiveTime;
    }

    /**
     * Builder模式支持
     */
    @Builder
    public UserRoleEntity(Long id, Long userId, Long roleId, LocalDateTime grantTime,
            LocalDateTime expireTime, Integer status, String grantType, String grantSource) {
        this.id = id;
        this.userId = userId;
        this.roleId = roleId;
        this.grantTime = grantTime;
        this.expireTime = expireTime;
        this.status = status;
        this.grantType = grantType;
        this.grantSource = grantSource;
    }

    /**
     * 默认构造函数
     */
    public UserRoleEntity() {
    }

    /**
     * 判断是否有效（业务方法）
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status != null && status == 1
                && (grantTime == null || !now.isBefore(grantTime))
                && (expireTime == null || !now.isAfter(expireTime));
    }

    /**
     * 是否系统授权
     */
    public boolean isSystemGranted() {
        return "SYSTEM".equals(grantType);
    }

    /**
     * 是否手动授权
     */
    public boolean isManuallyGranted() {
        return "MANUAL".equals(grantType);
    }
}