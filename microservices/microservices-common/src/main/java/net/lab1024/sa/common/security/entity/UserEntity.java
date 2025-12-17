package net.lab1024.sa.common.security.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表 t_user
 * 继承 BaseEntity 复用审计字段
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;

    @TableField("real_name")
    private String realName;

    @TableField("avatar")
    private String avatar;

    @TableField("department_id")
    private Long departmentId;

    @TableField("status")
    private Integer status;

    @TableField("account_locked")
    private Integer accountLocked;

    @TableField("lock_reason")
    private String lockReason;

    @TableField("lock_time")
    private LocalDateTime lockTime;

    @TableField("unlock_time")
    private LocalDateTime unlockTime;

    @TableField("login_fail_count")
    private Integer loginFailCount;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;

    @TableField("account_expire_time")
    private LocalDateTime accountExpireTime;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("create_user_id")
    private Long createUserId;

    @TableField("update_user_id")
    private Long updateUserId;

    @TableField("deleted_flag")
    @TableLogic
    private Integer deletedFlag;

    /**
     * 获取用户ID（兼容getId()调用）
     */
    public Long getId() {
        return this.userId;
    }

    /**
     * 设置用户ID（兼容setId()调用）
     */
    public void setId(Long id) {
        this.userId = id;
    }
}
