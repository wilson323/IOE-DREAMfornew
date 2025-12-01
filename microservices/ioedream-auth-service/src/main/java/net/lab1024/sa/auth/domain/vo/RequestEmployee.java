package net.lab1024.sa.auth.domain.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工请求VO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-30
 */
@Data
public class RequestEmployee {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度必须在4-50个字符之间")
    private String username;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 邮箱
     */
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 手机号
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 职位
     */
    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;

    /**
     * 入职时间
     */
    private LocalDateTime hireDate;

    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remarks;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 管理员标识
     */
    private Boolean administratorFlag;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 用户名（别名，兼容性）
     */
    public String getUserName() {
        return this.username;
    }

    /**
     * 设置用户名（别名，兼容性）
     */
    public void setUserName(String userName) {
        this.username = userName;
    }

    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return this.employeeId;
    }

    /**
     * 设置用户ID
     */
    public void setUserId(Long userId) {
        this.employeeId = userId;
    }

    /**
     * 获取登录时间
     */
    public Long getLoginTime() {
        return this.loginTime;
    }

    /**
     * 设置登录时间
     */
    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    /**
     * 获取管理员标识
     */
    public Boolean getAdministratorFlag() {
        return this.administratorFlag != null ? this.administratorFlag : false;
    }

    /**
     * 设置管理员标识
     */
    public void setAdministratorFlag(Boolean administratorFlag) {
        this.administratorFlag = administratorFlag;
    }

    /**
     * 获取用户IP
     */
    public String getUserIp() {
        return this.userIp;
    }

    /**
     * 设置用户IP
     */
    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    /**
     * 获取用户代理
     */
    public String getUserAgent() {
        return this.userAgent;
    }

    /**
     * 设置用户代理
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
