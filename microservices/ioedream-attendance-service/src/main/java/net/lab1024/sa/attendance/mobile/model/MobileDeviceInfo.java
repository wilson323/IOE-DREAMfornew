package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 移动端设备信息
 * <p>
 * 用于缓存移动端设备信息
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
public class MobileDeviceInfo {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 设备信息
     */
    private Map<String, Object> deviceInfo;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;
}
