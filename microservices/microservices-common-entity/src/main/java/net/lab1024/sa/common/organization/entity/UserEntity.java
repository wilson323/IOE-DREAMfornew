package net.lab1024.sa.common.organization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体
 * <p>
 * 对应数据库表: t_common_user
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_user")
public class UserEntity extends BaseEntity {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("real_name")
    private String realName;

    @TableField("nickname")
    private String nickname;

    @TableField("gender")
    private Integer gender;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("avatar")
    private String avatar;

    @TableField("birthday")
    private LocalDate birthday;

    @TableField("address")
    private String address;

    @TableField("status")
    private Integer status;

    @TableField("department_id")
    private Long departmentId;

    @TableField("position")
    private String position;

    @TableField("employee_no")
    private String employeeNo;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("account_locked")
    private Integer accountLocked;

    @TableField("lock_reason")
    private String lockReason;

    @TableField("lock_expire_time")
    private LocalDateTime lockExpireTime;

    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;

    @TableField("account_expire_time")
    private LocalDateTime accountExpireTime;

    @TableField("remark")
    private String remark;

    // 便捷方法
    public Long getId() {
        return userId;
    }

    public void setId(Long id) {
        this.userId = id;
    }

    /**
     * 获取账户是否锁定（兼容方法）
     */
    public Boolean getAccountLocked() {
        return accountLocked != null && accountLocked == 1;
    }

    /**
     * 设置账户是否锁定（兼容方法）
     */
    public void setAccountLocked(Boolean locked) {
        this.accountLocked = locked != null && locked ? 1 : 0;
    }
}

