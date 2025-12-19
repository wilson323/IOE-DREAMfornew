package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 移动端用户会话
 * <p>
 * 用于缓存移动端用户登录会话信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileUserSession {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 过期时间
     */
    private LocalDateTime expiresTime;

    /**
     * 设备信息
     */
    private MobileLoginRequest.DeviceInfo deviceInfo;
}
