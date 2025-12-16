package net.lab1024.sa.attendance.service;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 考勤位置服务接口
 * <p>
 * 提供考勤位置验证相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回ResponseDTO统一格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AttendanceLocationService {

    /**
     * 验证位置
     *
     * @param employeeId 员工ID
     * @param latitude 纬度
     * @param longitude 经度
     * @return 验证结果
     */
    ResponseDTO<Boolean> validateLocation(Long employeeId, Double latitude, Double longitude);
}


