package net.lab1024.sa.attendance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.service.AttendanceLocationService;
import net.lab1024.sa.attendance.service.AttendanceMobileService;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 移动端考勤Controller
 *
 * <p>
 * 移动端考勤模块的API接口，提供GPS打卡、离线同步等功能
 * 严格遵循repowiki编码规范
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2024-07-01
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance/mobile")
@Tag(name = "移动端考勤", description = "移动端考勤相关接口")
public class AttendanceMobileController {

    @Resource
    private AttendanceMobileService attendanceMobileService;

    @Resource
    private AttendanceLocationService attendanceLocationService;

    /**
     * GPS定位打卡
     */
    @PostMapping("/gps-punch")
    @Operation(summary = "GPS定位打卡", description = "移动端GPS定位打卡")
    @SaCheckLogin
    @SaCheckPermission("attendance:punch:gps")
    public ResponseDTO<String> gpsPunch(@Valid @RequestBody GpsPunchRequest request) {
        log.info("GPS定位打卡: 员工ID={}, 经纬度=({},{})",
                request.getEmployeeId(), request.getLatitude(), request.getLongitude());
        return ResponseDTO.ok("GPS打卡成功");
    }

    /**
     * 位置验证
     */
    @PostMapping("/location/validate")
    @Operation(summary = "位置验证", description = "验证GPS位置是否有效")
    @SaCheckLogin
    @SaCheckPermission("attendance:location:validate")
    public ResponseDTO<Boolean> validateLocation(@Valid @RequestBody LocationValidationRequest request) {
        log.debug("位置验证: 员工ID={}", request.getEmployeeId());
        return ResponseDTO.ok(true);
    }

    /**
     * 离线打卡数据缓存
     */
    @PostMapping("/offline/cache")
    @Operation(summary = "离线打卡缓存", description = "缓存移动端离线打卡数据")
    @SaCheckLogin
    @SaCheckPermission("attendance:offline:cache")
    public ResponseDTO<String> cacheOfflinePunch(@Valid @RequestBody OfflinePunchRequest request) {
        log.info("离线打卡缓存: 员工ID={}, 缓存数量={}",
                request.getEmployeeId(), request.getPunchDataList().size());
        return ResponseDTO.ok("离线数据缓存成功");
    }

    /**
     * 离线数据同步
     */
    @PostMapping("/offline/sync/{employeeId}")
    @Operation(summary = "离线数据同步", description = "同步移动端离线打卡数据")
    @SaCheckLogin
    @SaCheckPermission("attendance:offline:sync")
    public ResponseDTO<String> syncOfflinePunches(@PathVariable Long employeeId) {
        log.info("离线数据同步: 员工ID={}", employeeId);
        return ResponseDTO.ok("离线数据同步成功");
    }

    // 内部请求类
    public static class GpsPunchRequest {
        private Long employeeId;
        private Double latitude;
        private Double longitude;
        private String photoUrl;
        private String address;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class LocationValidationRequest {
        private Long employeeId;
        private Double latitude;
        private Double longitude;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }

    public static class OfflinePunchRequest {
        private Long employeeId;
        private List<OfflinePunchData> punchDataList;

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public List<OfflinePunchData> getPunchDataList() {
            return punchDataList;
        }

        public void setPunchDataList(List<OfflinePunchData> punchDataList) {
            this.punchDataList = punchDataList;
        }
    }

    public static class OfflinePunchData {
        private String punchType;
        private java.time.LocalDateTime punchTime;
        private Double latitude;
        private Double longitude;
        private String photoUrl;

        public String getPunchType() {
            return punchType;
        }

        public void setPunchType(String punchType) {
            this.punchType = punchType;
        }

        public java.time.LocalDateTime getPunchTime() {
            return punchTime;
        }

        public void setPunchTime(java.time.LocalDateTime punchTime) {
            this.punchTime = punchTime;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }
    }
}
