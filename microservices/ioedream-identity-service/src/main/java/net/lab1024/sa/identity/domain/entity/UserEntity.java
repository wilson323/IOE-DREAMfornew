package net.lab1024.sa.identity.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 用户实体
 * 基于现有EmployeeEntity重构，扩展为身份权限服务核心实体
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27 (基于原EmployeeEntity重构)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_hr_user") // 重命名表，保持与原表结构兼容
public class UserEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名（基于原employeeName扩展）
     */
    private String username;

    /**
     * 真实姓名（原employeeName）
     */
    private String realName;

    /**
     * 密码（新增字段，用于身份认证）
     */
    private String password;

    /**
     * 性别(1-男，2-女)（原字段保持）
     */
    private Integer gender;

    /**
     * 邮箱（原字段保持）
     */
    private String email;

    /**
     * 手机号（原字段保持）
     */
    private String phone;

    /**
     * 身份证号（原字段保持）
     */
    private String idCard;

    /**
     * 头像URL（新增字段）
     */
    private String avatarUrl;

    /**
     * 部门ID（原字段保持）
     */
    private Long departmentId;

    /**
     * 职位（原position字段扩展为userType）
     */
    private String position;

    /**
     * 用户类型(1-管理员，2-员工,3-访客)（新增字段）
     */
    private Integer userType;

    /**
     * 状态：0禁用 1启用（原字段保持，调整语义）
     */
    private Integer status;

    /**
     * 薪资（原字段保持，HR模块专用）
     */
    private Double salary;

    /**
     * 入职日期（原字段保持）
     */
    private LocalDate joinDate;

    /**
     * 最后登录时间（新增字段）
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP（新增字段）
     */
    private String lastLoginIp;

    /**
     * 登录失败次数（新增字段）
     */
    private Integer loginFailedCount;

    /**
     * 账户锁定时间（新增字段）
     */
    private LocalDateTime lockTime;

    /**
     * 密码修改时间（新增字段）
     */
    private LocalDateTime passwordUpdateTime;

    /**
     * 地址（原字段保持）
     */
    private String address;

    /**
     * 备注（原字段保持）
     */
    private String remark;

    // 业务方法

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
     * 检查是否为管理员用户
     */
    public boolean isAdmin() {
        return userType != null && userType == 1;
    }

    /**
     * 检查是否为员工用户
     */
    public boolean isEmployee() {
        return userType != null && userType == 2;
    }

    /**
     * 检查是否为访客用户
     */
    public boolean isVisitor() {
        return userType != null && userType == 3;
    }

    /**
     * 获取显示名称（优先使用真实姓名）
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

    // 兼容性方法，保持与原有EmployeeEntity的兼容性
    /**
     * 获取员工ID（兼容性方法）
     */
    public Long getEmployeeId() {
        return userId;
    }

    /**
     * 设置员工ID（兼容性方法）
     */
    public void setEmployeeId(Long employeeId) {
        this.userId = employeeId;
    }

    /**
     * 获取员工姓名（兼容性方法）
     */
    public String getEmployeeName() {
        return realName;
    }

    /**
     * 设置员工姓名（兼容性方法）
     */
    public void setEmployeeName(String employeeName) {
        this.realName = employeeName;
    }

    // 用户类型常量
    public static class UserTypes {
        public static final int ADMIN = 1; // 管理员
        public static final int EMPLOYEE = 2; // 员工
        public static final int VISITOR = 3; // 访客
    }

    // 状态常量
    public static class Statuses {
        public static final int DISABLED = 0; // 禁用
        public static final int ENABLED = 1; // 启用
    }
}
