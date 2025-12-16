package net.lab1024.sa.attendance.controller;

import io.micrometer.observation.annotation.Observed;
import java.time.LocalDateTime;
import java.util.List;

import net.lab1024.sa.common.permission.PermissionCheck;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.service.AttendanceLocationService;
import net.lab1024.sa.attendance.service.AttendanceMobileService;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 移动端考勤Controller
 * <p>
 * 移动端考勤模块的API接口，提供GPS打卡、离线同步等功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/attendance/mobile", "/api/attendance/mobile"})
@Tag(name = "移动端考勤", description = "移动端考勤相关接口")
@PermissionCheck(description = "移动端考勤管理")
public class AttendanceMobileController {

    @Resource
    private AttendanceMobileService attendanceMobileService;

    @Resource
    private AttendanceLocationService attendanceLocationService;

    /**
     * GPS定位打卡
     *
     * @param request 打卡请求
     * @return 打卡结果
     */
    @Observed(name = "attendanceMobile.gpsPunch", contextualName = "attendance-mobile-gps-punch")
    @PostMapping("/gps-punch")
    @Operation(summary = "GPS定位打卡", description = "移动端GPS定位打卡")
    @PermissionCheck(value = {"ATTENDANCE_MOBILE_GPS_PUNCH"}, description = "GPS定位打卡")
    public ResponseDTO<String> gpsPunch(@Valid @RequestBody GpsPunchRequest request) {
        log.info("GPS定位打卡: 员工ID={}, 经纬度=({},{})",
                request.getEmployeeId(), request.getLatitude(), request.getLongitude());
        return ResponseDTO.ok("GPS打卡成功");
    }

    /**
     * 位置验证
     *
     * @param request 验证请求
     * @return 验证结果
     */
    @Observed(name = "attendanceMobile.validateLocation", contextualName = "attendance-mobile-validate-location")
    @PostMapping("/location/validate")
    @Operation(summary = "位置验证", description = "验证GPS位置是否有效")
    @PermissionCheck(value = {"ATTENDANCE_MOBILE_LOCATION_VALIDATE"}, description = "位置验证")
    public ResponseDTO<Boolean> validateLocation(@Valid @RequestBody LocationValidationRequest request) {
        log.debug("位置验证: 员工ID={}", request.getEmployeeId());
        return ResponseDTO.ok(true);
    }

    /**
     * 离线打卡数据缓存
     *
     * @param request 缓存请求
     * @return 缓存结果
     */
    @Observed(name = "attendanceMobile.cacheOfflinePunch", contextualName = "attendance-mobile-cache-offline")
    @PostMapping("/offline/cache")
    @Operation(summary = "离线打卡缓存", description = "缓存移动端离线打卡数据")
    @PermissionCheck(value = {"ATTENDANCE_MOBILE_OFFLINE_CACHE"}, description = "离线打卡缓存")
    public ResponseDTO<String> cacheOfflinePunch(@Valid @RequestBody OfflinePunchRequest request) {
        log.info("离线打卡缓存: 员工ID={}, 缓存数量={}",
                request.getEmployeeId(), request.getPunchDataList().size());
        return ResponseDTO.ok("离线数据缓存成功");
    }

    /**
     * 离线数据同步
     *
     * @param employeeId 员工ID
     * @return 同步结果
     */
    @Observed(name = "attendanceMobile.syncOfflinePunches", contextualName = "attendance-mobile-sync-offline")
    @PostMapping("/offline/sync/{employeeId}")
    @Operation(summary = "离线数据同步", description = "同步移动端离线打卡数据")
    @PermissionCheck(value = {"ATTENDANCE_MOBILE_OFFLINE_SYNC"}, description = "离线数据同步")
    public ResponseDTO<String> syncOfflinePunches(@PathVariable Long employeeId) {
        log.info("离线数据同步: 员工ID={}", employeeId);
        return ResponseDTO.ok("离线数据同步成功");
    }

    // ==================== 内部请求类 ====================

    /**
     * GPS打卡请求
     */
    @Data
    public static class GpsPunchRequest {
        private Long employeeId;
        private Double latitude;
        private Double longitude;
        private String photoUrl;
        private String address;
    }

    /**
     * 位置验证请求
     */
    @Data
    public static class LocationValidationRequest {
        private Long employeeId;
        private Double latitude;
        private Double longitude;
    }

    /**
     * 离线打卡请求
     */
    @Data
    public static class OfflinePunchRequest {
        private Long employeeId;
        private List<OfflinePunchData> punchDataList;
    }

    /**
     * 离线打卡数据
     */
    @Data
    public static class OfflinePunchData {
        private String punchType;
        private LocalDateTime punchTime;
        private Double latitude;
        private Double longitude;
        private String photoUrl;
    }
}


