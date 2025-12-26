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
 * 用户区域权限实体类（遗留）
 * <p>
 * 对应数据库表: t_access_user_permission
 * 该实体为历史遗留模型，容易与门禁设备权限混用。
 * 门禁设备验证权限已迁移至门禁服务内的AccessUserPermissionEntity。
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Deprecated
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_user_permission")
public class UserAreaPermissionEntity extends BaseEntity {

    /**
     * 权限ID（主键）
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 区域ID
     */
    @TableField("area_id")
    private Long areaId;

    /**
     * 权限类型（1-访问权限 2-管理权限 3-监控权限 4-紧急权限）
     */
    @TableField("permission_type")
    private Integer permissionType;

    /**
     * 访问级别（1-10）
     */
    @TableField("access_level")
    private Integer accessLevel;

    /**
     * 权限级别（1-读写 2-只读 3-无权限）
     */
    @TableField("permission_level")
    private Integer permissionLevel;

    /**
     * 权限状态（0-停用 1-启用 2-待激活 3-已过期）
     */
    @TableField("permission_status")
    private Integer permissionStatus;

    /**
     * 是否临时权限（0-否 1-是）
     */
    @TableField("is_temporary")
    private Integer isTemporary;

    /**
     * 是否紧急权限（0-否 1-是）
     */
    @TableField("is_emergency")
    private Integer isEmergency;

    /**
     * 生效开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 时间规则ID
     */
    @TableField("time_rule_id")
    private Long timeRuleId;

    /**
     * 要求卡片（0-否 1-是）
     */
    @TableField("require_card")
    private Integer requireCard;

    /**
     * 要求密码（0-否 1-是）
     */
    @TableField("require_password")
    private Integer requirePassword;

    /**
     * 要求人脸（0-否 1-是）
     */
    @TableField("require_face")
    private Integer requireFace;

    /**
     * 要求指纹（0-否 1-是）
     */
    @TableField("require_fingerprint")
    private Integer requireFingerprint;

    /**
     * 要求多因子（0-否 1-是）
     */
    @TableField("require_multi_factor")
    private Integer requireMultiFactor;

    /**
     * 最少认证因子数
     */
    @TableField("min_auth_factors")
    private Integer minAuthFactors;

    /**
     * 继承到子区域（0-否 1-是）
     */
    @TableField("inherit_to_children")
    private Integer inheritToChildren;

    /**
     * 从父级继承（0-否 1-是）
     */
    @TableField("inherit_from_parent")
    private Integer inheritFromParent;

    /**
     * 继承优先级
     */
    @TableField("inherit_priority")
    private Integer inheritPriority;

    /**
     * 授权人ID
     */
    @TableField("grant_user_id")
    private Long grantUserId;

    /**
     * 授权原因
     */
    @TableField("grant_reason")
    private String grantReason;

    /**
     * 最后访问时间
     */
    @TableField("last_access_time")
    private LocalDateTime lastAccessTime;

    /**
     * 获取权限ID（兼容BaseEntity的getId方法）
     *
     * @return 权限ID
     */
    public Long getId() {
        return permissionId;
    }

    /**
     * 设置权限ID（兼容BaseEntity的setId方法）
     *
     * @param id 权限ID
     */
    public void setId(Long id) {
        this.permissionId = id;
    }

    /**
     * 获取访问时间段配置（兼容方法）
     * <p>
     * 注意：此方法用于兼容AccessVerificationManager中的getAccessTimes()调用
     * 实际应该通过time_rule_id关联查询时间规则配置
     * 这里返回null，由Service层处理关联查询
     * </p>
     *
     * @return 访问时间段配置JSON字符串（兼容旧代码）
     */
    public String getAccessTimes() {
        // 实际实现中应该通过time_rule_id关联查询时间规则配置
        // 这里返回null，由Service层处理关联查询
        return null;
    }

    /**
     * 设置访问时间段配置（兼容方法）
     *
     * @param accessTimes 访问时间段配置JSON字符串
     */
    public void setAccessTimes(String accessTimes) {
        // 实际实现中应该解析JSON并设置到time_rule_id相关配置
        // 这里不做处理，由Service层处理
    }

    /**
     * 设置权限类型（兼容方法，支持字符串输入）
     * <p>
     * 注意：此方法用于兼容AccessAreaServiceImpl中的setPermissionType("ALWAYS")调用
     * 实际字段类型为Integer，映射规则：
     * - "ALWAYS" -> 1 (访问权限)
     * - "MANAGE" -> 2 (管理权限)
     * - "MONITOR" -> 3 (监控权限)
     * - "EMERGENCY" -> 4 (紧急权限)
     * 如果无法识别，默认设置为1（访问权限）
     * </p>
     *
     * @param permissionTypeStr 权限类型字符串
     */
    public void setPermissionType(String permissionTypeStr) {
        if (permissionTypeStr == null || permissionTypeStr.trim().isEmpty()) {
            this.permissionType = 1; // 默认访问权限
            return;
        }
        
        String upperType = permissionTypeStr.trim().toUpperCase();
        switch (upperType) {
            case "ALWAYS":
            case "ACCESS":
                this.permissionType = 1; // 访问权限
                break;
            case "MANAGE":
            case "MANAGEMENT":
                this.permissionType = 2; // 管理权限
                break;
            case "MONITOR":
                this.permissionType = 3; // 监控权限
                break;
            case "EMERGENCY":
                this.permissionType = 4; // 紧急权限
                break;
            default:
                this.permissionType = 1; // 默认访问权限
        }
    }
}

