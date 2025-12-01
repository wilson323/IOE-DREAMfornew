package net.lab1024.sa.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Entity
@Table(name = "sys_user", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_phone", columnList = "phone"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseEntity {

    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度必须在4-50个字符之间")
    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 255, message = "密码长度必须在6-255个字符之间")
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 真实姓名
     */
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Column(name = "real_name", length = 50)
    private String realName;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 手机号
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    /**
     * 状态：1-启用，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @Column(name = "login_failed_count")
    private Integer loginFailedCount;

    /**
     * 账户锁定时间
     */
    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    /**
     * 密码修改时间
     */
    @Column(name = "password_update_time")
    private LocalDateTime passwordUpdateTime;

    /**
     * 用户角色关联
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRoleEntity> userRoles;

    /**
     * 用户会话信息
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSessionEntity> userSessions;

    /**
     * 检查用户是否被锁定
     */
    public boolean isLocked() {
        return lockTime != null && lockTime.isAfter(LocalDateTime.now());
    }

    /**
     * 检查用户是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return realName != null && !realName.trim().isEmpty() ? realName : username;
    }

    /**
     * 增加登录失败次数
     */
    public void incrementLoginFailedCount() {
        this.loginFailedCount = (this.loginFailedCount == null ? 0 : this.loginFailedCount) + 1;
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailedCount() {
        this.loginFailedCount = 0;
        this.lockTime = null;
    }

    /**
     * 锁定账户
     */
    public void lockAccount(int lockDurationMinutes) {
        this.lockTime = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }

    /**
     * 更新最后登录信息
     */
    public void updateLastLogin(String loginIp) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = loginIp;
        this.resetLoginFailedCount();
    }

    // ========== 确保Lombok正确生成的getter/setter方法 ==========

    /**
     * 获取用户ID - 确保Lombok生成
     */
    public Long getUserId() {
        return this.userId;
    }

    /**
     * 设置用户ID - 确保Lombok生成
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名 - 确保Lombok生成
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * 设置用户名 - 确保Lombok生成
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码 - 确保Lombok生成
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * 设置密码 - 确保Lombok生成
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取状态 - 确保Lombok生成
     */
    public Integer getStatus() {
        return this.status;
    }

    /**
     * 设置状态 - 确保Lombok生成
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 设置密码更新时间 - 确保Lombok生成
     */
    public void setPasswordUpdateTime(LocalDateTime passwordUpdateTime) {
        this.passwordUpdateTime = passwordUpdateTime;
    }

    /**
     * 获取真实姓名 - 确保Lombok生成
     */
    public String getRealName() {
        return this.realName;
    }

    /**
     * 获取邮箱 - 确保Lombok生成
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * 获取手机号 - 确保Lombok生成
     */
    public String getPhone() {
        return this.phone;
    }

    /**
     * Builder模式支持 - 确保Lombok生成
     */
    public static UserEntityBuilder builder() {
        return new UserEntityBuilder();
    }

    /**
     * Builder实现类 - 确保Lombok生成
     */
    public static class UserEntityBuilder {
        private Long userId;
        private String username;
        private String password;
        private String realName;
        private String email;
        private String phone;
        private Integer status;
        private LocalDateTime passwordUpdateTime;

        public UserEntityBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public UserEntityBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserEntityBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserEntityBuilder realName(String realName) {
            this.realName = realName;
            return this;
        }

        public UserEntityBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserEntityBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserEntityBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public UserEntityBuilder passwordUpdateTime(LocalDateTime passwordUpdateTime) {
            this.passwordUpdateTime = passwordUpdateTime;
            return this;
        }

        public UserEntity build() {
            UserEntity userEntity = new UserEntity();
            userEntity.userId = this.userId;
            userEntity.username = this.username;
            userEntity.password = this.password;
            userEntity.realName = this.realName;
            userEntity.email = this.email;
            userEntity.phone = this.phone;
            userEntity.status = this.status;
            userEntity.passwordUpdateTime = this.passwordUpdateTime;
            return userEntity;
        }
    }
}