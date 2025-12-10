package net.lab1024.sa.attendance.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.attendance.dao.AttendanceRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceShiftDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceRecordEntity;
import net.lab1024.sa.attendance.service.AttendanceMobileService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.EmployeeDao;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.entity.EmployeeEntity;

/**
 * 移动端考勤服务实现类
 * <p>
 * 实现移动端考勤打卡的核心业务功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务类
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 遵循四层架构规范
 * </p>
 * <p>
 * 业务场景：
 * - GPS定位打卡
 * - 人脸识别打卡
 * - NFC打卡
 * - 打卡记录管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceMobileServiceImpl implements AttendanceMobileService {

    @Resource
    private AttendanceRecordDao attendanceRecordDao;

    @Resource
    private EmployeeDao employeeDao;

    @Resource
    private AttendanceShiftDao attendanceShiftDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

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
    @Override
    public ResponseDTO<String> gpsPunch(Long employeeId, Double latitude, Double longitude, String address, String photoUrl) {
        log.info("[移动端考勤] GPS打卡开始，employeeId={}, latitude={}, longitude={}, address={}",
                employeeId, latitude, longitude, address);

        try {
            // 参数验证
            if (employeeId == null) {
                return ResponseDTO.error("PARAM_ERROR", "员工ID不能为空");
            }

            if (latitude == null || longitude == null) {
                return ResponseDTO.error("PARAM_ERROR", "GPS坐标不能为空");
            }

            if (address == null || address.trim().isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "打卡地址不能为空");
            }

            // 验证员工是否存在
            EmployeeEntity employee = employeeDao.selectById(employeeId);
            if (employee == null) {
                log.warn("[移动端考勤] 员工不存在，employeeId={}", employeeId);
                return ResponseDTO.error("EMPLOYEE_NOT_FOUND", "员工不存在");
            }

            // 检查员工是否在指定考勤区域范围内
            boolean inRange = checkLocationInRange(employeeId, latitude, longitude);
            if (!inRange) {
                log.warn("[移动端考勤] 不在考勤范围内，employeeId={}, latitude={}, longitude={}",
                        employeeId, latitude, longitude);
                return ResponseDTO.error("OUT_OF_RANGE", "不在考勤范围内");
            }

            // 检查今日是否已经打卡
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();

            // 查询今日的上班打卡记录
            LambdaQueryWrapper<AttendanceRecordEntity> checkInWrapper = new LambdaQueryWrapper<>();
            checkInWrapper.eq(AttendanceRecordEntity::getUserId, employeeId)
                    .eq(AttendanceRecordEntity::getAttendanceDate, today)
                    .eq(AttendanceRecordEntity::getAttendanceType, "CHECK_IN");
            AttendanceRecordEntity checkInRecord = attendanceRecordDao.selectOne(checkInWrapper);

            // 查询今日的下班打卡记录
            LambdaQueryWrapper<AttendanceRecordEntity> checkOutWrapper = new LambdaQueryWrapper<>();
            checkOutWrapper.eq(AttendanceRecordEntity::getUserId, employeeId)
                    .eq(AttendanceRecordEntity::getAttendanceDate, today)
                    .eq(AttendanceRecordEntity::getAttendanceType, "CHECK_OUT");
            AttendanceRecordEntity checkOutRecord = attendanceRecordDao.selectOne(checkOutWrapper);

            if (checkInRecord != null && checkOutRecord != null) {
                // 已完成上下班打卡
                log.warn("[移动端考勤] 今日已完成上下班打卡，employeeId={}", employeeId);
                return ResponseDTO.error("ALREADY_PUNCHED", "今日已完成上下班打卡");
            } else if (checkInRecord != null && checkOutRecord == null) {
                // 已上班打卡，创建下班打卡记录
                AttendanceRecordEntity newCheckOutRecord = new AttendanceRecordEntity();
                newCheckOutRecord.setUserId(employeeId);
                newCheckOutRecord.setUserName(employee.getName() != null ? employee.getName() : "");
                newCheckOutRecord.setDepartmentId(employee.getDepartmentId());
                newCheckOutRecord.setAttendanceDate(today);
                newCheckOutRecord.setPunchTime(now);
                newCheckOutRecord.setAttendanceType("CHECK_OUT");
                newCheckOutRecord.setLatitude(BigDecimal.valueOf(latitude));
                newCheckOutRecord.setLongitude(BigDecimal.valueOf(longitude));
                newCheckOutRecord.setPunchAddress(address);
                newCheckOutRecord.setShiftId(checkInRecord.getShiftId());
                newCheckOutRecord.setShiftName(checkInRecord.getShiftName());

                attendanceRecordDao.insert(newCheckOutRecord);
                log.info("[移动端考勤] 下班打卡成功，employeeId={}, address={}", employeeId, address);
                return ResponseDTO.ok("下班打卡成功");
            }

            // 创建新的上班打卡记录
            AttendanceRecordEntity record = new AttendanceRecordEntity();
            record.setUserId(employeeId);
            record.setUserName(employee.getName() != null ? employee.getName() : "");
            record.setDepartmentId(employee.getDepartmentId());
            record.setAttendanceDate(today);
            record.setPunchTime(now);
            record.setAttendanceType("CHECK_IN");
            record.setLatitude(BigDecimal.valueOf(latitude));
            record.setLongitude(BigDecimal.valueOf(longitude));
            record.setPunchAddress(address);

            attendanceRecordDao.insert(record);

            log.info("[移动端考勤] GPS打卡成功，employeeId={}, address={}", employeeId, address);
            return ResponseDTO.ok("上班打卡成功");

        } catch (Exception e) {
            log.error("[移动端考勤] GPS打卡异常，employeeId={}", employeeId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "打卡失败：" + e.getMessage());
        }
    }

    /**
     * 检查位置是否在考勤范围内
     * <p>
     * 预留方法，待实现位置验证功能时使用
     * </p>
     *
     * @param employeeId 员工ID
     * @param latitude 纬度
     * @param longitude 经度
     * @return 是否在范围内
     */
    @SuppressWarnings("unused")
    private boolean checkLocationInRange(Long employeeId, Double latitude, Double longitude) {
        try {
            log.debug("[移动端考勤] 检查位置范围，employeeId={}, latitude={}, longitude={}",
                    employeeId, latitude, longitude);

            // 获取员工信息以获取userId
            EmployeeEntity employee = employeeDao.selectById(employeeId);
            if (employee == null || employee.getUserId() == null) {
                log.warn("[移动端考勤] 员工不存在或userId为空，employeeId={}", employeeId);
                return false;
            }

            // 获取员工的考勤区域（通过GatewayServiceClient调用公共服务）
            // 注意：API路径可能需要根据实际Controller调整
            String apiPath = "/api/v1/area/user/" + employee.getUserId() + "/accessible";
            try {
                ResponseDTO<List<AreaEntity>> areaResponse = gatewayServiceClient.callCommonService(
                        apiPath,
                        HttpMethod.GET,
                        null,
                        new TypeReference<ResponseDTO<List<AreaEntity>>>() {}
                );

                if (areaResponse == null || !areaResponse.isSuccess() || areaResponse.getData() == null || areaResponse.getData().isEmpty()) {
                    log.warn("[移动端考勤] 员工没有可访问的考勤区域，employeeId={}, userId={}", employeeId, employee.getUserId());
                    // 如果没有配置区域，默认允许打卡（兼容性处理）
                    return true;
                }

                List<AreaEntity> accessibleAreas = areaResponse.getData();
                double defaultAllowedDistance = 100.0; // 默认允许距离100米

                // 检查是否在任何一个可访问区域的范围内
                for (AreaEntity area : accessibleAreas) {
                    if (area.getLatitude() != null && area.getLongitude() != null) {
                        // 计算距离（使用Haversine公式）
                        double distance = calculateDistance(
                                latitude,
                                longitude,
                                area.getLatitude().doubleValue(),
                                area.getLongitude().doubleValue()
                        );

                        // 获取区域业务属性中的允许距离（如果有配置）
                        double allowedDistance = defaultAllowedDistance;
                        try {
                            ResponseDTO<Object> attributesResponse = gatewayServiceClient.callCommonService(
                                    "/api/v1/area/" + area.getId() + "/business-attributes/attendance",
                                    HttpMethod.GET,
                                    null,
                                    Object.class
                            );
                            if (attributesResponse != null && attributesResponse.isSuccess() && attributesResponse.getData() != null) {
                                // 如果业务属性中有允许距离配置，使用配置值
                                // 这里简化处理，实际应该解析JSON获取allowDistance字段
                            }
                        } catch (Exception e) {
                            log.debug("[移动端考勤] 获取区域业务属性失败，使用默认距离，areaId={}", area.getId());
                        }

                        boolean inRange = distance <= allowedDistance;
                        log.debug("[移动端考勤] 位置范围检查结果，employeeId={}, areaId={}, areaName={}, distance={}, allowedDistance={}, inRange={}",
                                employeeId, area.getId(), area.getAreaName(), distance, allowedDistance, inRange);

                        if (inRange) {
                            return true; // 在任何一个区域的范围内即可
                        }
                    }
                }

                log.warn("[移动端考勤] 不在任何考勤区域范围内，employeeId={}, latitude={}, longitude={}",
                        employeeId, latitude, longitude);
                return false;
            } catch (Exception apiException) {
                log.warn("[移动端考勤] 调用区域服务API失败，使用默认策略，employeeId={}, error={}",
                        employeeId, apiException.getMessage());
                // API调用失败时，为了不影响打卡流程，默认允许打卡
                return true;
            }

        } catch (Exception e) {
            log.error("[移动端考勤] 检查位置范围异常，employeeId={}", employeeId, e);
            // 异常情况下，为了不影响打卡流程，返回true（但记录错误日志）
            return true;
        }
    }

    /**
     * 计算两点之间的距离（米）
     * <p>
     * 使用Haversine公式计算地球表面两点间距离
     * 预留方法，待实现位置验证功能时使用
     * </p>
     *
     * @param lat1 第一个点纬度
     * @param lng1 第一个点经度
     * @param lat2 第二个点纬度
     * @param lng2 第二个点经度
     * @return 距离（米）
     */
    @SuppressWarnings("unused")
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; // 地球半径，单位：米

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }
}
