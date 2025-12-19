package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 移动端离线记录
 * <p>
 * 封装移动端离线考勤记录信息
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
public class MobileOfflineRecord {

    /**
     * 记录ID
     */
    private String recordId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 记录类型
     */
    private String recordType;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 位置
     */
    private LocationInfo location;
}
