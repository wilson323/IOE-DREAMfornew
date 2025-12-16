package net.lab1024.sa.attendance.service;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 移动端考勤服务接口
 * <p>
 * 提供移动端考勤相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回ResponseDTO统一格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AttendanceMobileService {

    /**
     * GPS定位打卡
     *
     * @param employeeId 员工ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param address 地址
     * @param photoUrl 照片URL
     * @return 打卡结果
     */
    ResponseDTO<String> gpsPunch(Long employeeId, Double latitude, Double longitude, String address, String photoUrl);
}


