package net.lab1024.sa.admin.module.attendance.manager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.device.entity.SmartDeviceEntity;
import net.lab1024.sa.admin.module.device.service.SmartDeviceService;
import net.lab1024.sa.base.module.area.domain.entity.AreaEntity;
import net.lab1024.sa.base.module.area.service.AreaService;
import net.lab1024.sa.admin.module.attendance.domain.data.AttendancePunchData;
import net.lab1024.sa.admin.module.attendance.domain.result.DeviceDataProcessResult;

// 导入内部类和VO
import static net.lab1024.sa.admin.module.attendance.manager.AttendanceDeviceManager.*;

/**
 * 考勤设备管理器
 * <p>
 * 基于SmartDeviceEntity的考勤设备管理，通过extensionConfig存储考勤特有配置
 * 复用SmartDeviceService的基础设备管理功能，避免重复开发
 * 严格遵循四层架构规范，在Manager层处理复杂业务逻辑
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AttendanceDeviceManager {

    @Resource
    private SmartDeviceService smartDeviceService;

    @Resource
    private AreaService areaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 考勤设备管理核心方法 ====================

    /**
     * 注册考勤设备
     * 复用SmartDeviceService的设备注册功能，添加考勤特有配置
     */
    public ResponseDTO<String> registerAttendanceDevice(SmartDeviceEntity device, AttendanceDeviceConfig attendanceConfig) {
        try {
            log.debug("注册考勤设备，deviceCode: {}", device.getDeviceCode());

            // 1. 设置设备类型为考勤设备
            device.setDeviceType("ATTENDANCE");

            // 2. 设置考勤扩展配置
            if (attendanceConfig != null) {
                device.setExtensionConfig(objectMapper.convertValue(attendanceConfig, Map.class));
            } else {
                device.setExtensionConfig(objectMapper.convertValue(createDefaultAttendanceConfig(), Map.class));
            }

            // 3. 复用基础设备注册功能
            ResponseDTO<String> result = smartDeviceService.registerDevice(device);
            if (result.getOk()) {
                log.info("考勤设备注册成功，deviceId: {}, deviceCode: {}", device.getDeviceId(), device.getDeviceCode());
            }

            return result;

        } catch (Exception e) {
            log.error("注册考勤设备失败，deviceCode: {}", device.getDeviceCode(), e);
            return ResponseDTO.error("注册考勤设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新考勤设备配置
     * 更新设备基础信息和考勤特有配置
     */
    public ResponseDTO<String> updateAttendanceDevice(SmartDeviceEntity device, AttendanceDeviceConfig attendanceConfig) {
        try {
            log.debug("更新考勤设备配置，deviceId: {}", device.getDeviceId());

            // 1. 确保设备类型为考勤设备
            if (!"ATTENDANCE".equals(device.getDeviceType())) {
                return ResponseDTO.error("设备类型不是考勤设备");
            }

            // 2. 更新考勤扩展配置
            if (attendanceConfig != null) {
                device.setExtensionConfig(objectMapper.convertValue(attendanceConfig, Map.class));
            }

            // 3. 复用基础设备更新功能
            ResponseDTO<String> result = smartDeviceService.updateDevice(device);
            if (result.getOk()) {
                log.info("考勤设备配置更新成功，deviceId: {}", device.getDeviceId());
            }

            return result;

        } catch (Exception e) {
            log.error("更新考勤设备配置失败，deviceId: {}", device.getDeviceId(), e);
            return ResponseDTO.error("更新考勤设备配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤设备详情
     * 包含基础设备信息和考勤特有配置
     */
    public ResponseDTO<AttendanceDeviceDetailVO> getAttendanceDeviceDetail(Long deviceId) {
        try {
            log.debug("获取考勤设备详情，deviceId: {}", deviceId);

            // 1. 获取基础设备信息
            ResponseDTO<SmartDeviceEntity> deviceResult = smartDeviceService.getDeviceDetail(deviceId);
            if (!deviceResult.getOk()) {
                return deviceResult.convertType(AttendanceDeviceDetailVO.class);
            }

            SmartDeviceEntity device = deviceResult.getData();
            if (!device.isAttendanceDevice()) {
                return ResponseDTO.error("设备不是考勤设备");
            }

            // 2. 解析考勤扩展配置
            AttendanceDeviceConfig attendanceConfig = parseAttendanceConfig(device.getExtensionConfig());

            // 3. 获取区域考勤配置
            AttendanceAreaConfigEntity areaConfig = null;
            if (device.getAreaId() != null) {
                // 这里暂时设为null，实际实现时需要调用区域配置服务
                areaConfig = null;
            }

            // 4. 构建返回对象
            AttendanceDeviceDetailVO detailVO = new AttendanceDeviceDetailVO();
            detailVO.setDevice(device);
            detailVO.setAttendanceConfig(attendanceConfig);
            detailVO.setAreaConfig(areaConfig);
            detailVO.setDeviceStatusText(getDeviceStatusText(device.getDeviceStatus()));

            return ResponseDTO.ok(detailVO);

        } catch (Exception e) {
            log.error("获取考勤设备详情失败，deviceId: {}", deviceId, e);
            return ResponseDTO.error("获取考勤设备详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤设备列表
     * 复用SmartDeviceService的设备查询功能，过滤考勤设备
     */
    public ResponseDTO<List<AttendanceDeviceVO>> getAttendanceDeviceList(String deviceStatus, String deviceName) {
        try {
            log.debug("获取考勤设备列表，status: {}, name: {}", deviceStatus, deviceName);

            // 1. 获取所有考勤设备
            List<SmartDeviceEntity> attendanceDevices = smartDeviceService.getOnlineDevices().stream()
                .filter(SmartDeviceEntity::isAttendanceDevice)
                .filter(device -> deviceStatus == null || deviceStatus.equals(device.getDeviceStatus()))
                .filter(device -> deviceName == null || device.getDeviceName().contains(deviceName))
                .toList();

            // 2. 转换为考勤设备VO
            List<AttendanceDeviceVO> resultList = attendanceDevices.stream()
                .map(this::convertToAttendanceDeviceVO)
                .toList();

            return ResponseDTO.ok(resultList);

        } catch (Exception e) {
            log.error("获取考勤设备列表失败", e);
            return ResponseDTO.error("获取考勤设备列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除考勤设备
     * 复用SmartDeviceService的设备删除功能
     */
    public ResponseDTO<String> deleteAttendanceDevice(Long deviceId) {
        try {
            log.debug("删除考勤设备，deviceId: {}", deviceId);

            // 1. 验证设备是否为考勤设备
            ResponseDTO<SmartDeviceEntity> deviceResult = smartDeviceService.getDeviceDetail(deviceId);
            if (!deviceResult.getOk()) {
                return ResponseDTO.error("设备不存在");
            }

            SmartDeviceEntity device = deviceResult.getData();
            if (!device.isAttendanceDevice()) {
                return ResponseDTO.error("只能删除考勤设备");
            }

            // 2. 复用基础设备删除功能
            return smartDeviceService.deleteDevice(deviceId);

        } catch (Exception e) {
            log.error("删除考勤设备失败，deviceId: {}", deviceId, e);
            return ResponseDTO.error("删除考勤设备失败: " + e.getMessage());
        }
    }

    // ==================== 考勤设备特有功能 ====================

    /**
     * 更新考勤设备工作模式
     */
    public ResponseDTO<String> updateAttendanceWorkMode(Long deviceId, String workMode) {
        try {
            log.debug("更新考勤设备工作模式，deviceId: {}, workMode: {}", deviceId, workMode);

            // 1. 获取设备信息
            ResponseDTO<SmartDeviceEntity> deviceResult = smartDeviceService.getDeviceDetail(deviceId);
            if (!deviceResult.getOk()) {
                return ResponseDTO.error("设备不存在");
            }

            SmartDeviceEntity device = deviceResult.getData();
            if (!device.isAttendanceDevice()) {
                return ResponseDTO.error("只能更新考勤设备工作模式");
            }

            // 2. 解析和更新扩展配置
            AttendanceDeviceConfig config = parseAttendanceConfig(device.getExtensionConfig());
            config.setWorkMode(workMode);
            device.setExtensionConfig(objectMapper.convertValue(config, Map.class));

            // 3. 更新设备
            return smartDeviceService.updateDevice(device);

        } catch (Exception e) {
            log.error("更新考勤设备工作模式失败，deviceId: {}, workMode: {}", deviceId, workMode, e);
            return ResponseDTO.error("更新工作模式失败: " + e.getMessage());
        }
    }

    /**
     * 同步考勤设备时间
     */
    public ResponseDTO<String> syncAttendanceDeviceTime(Long deviceId) {
        try {
            log.debug("同步考勤设备时间，deviceId: {}", deviceId);

            // 1. 验证设备状态
            ResponseDTO<SmartDeviceEntity> deviceResult = smartDeviceService.getDeviceDetail(deviceId);
            if (!deviceResult.getOk()) {
                return ResponseDTO.error("设备不存在");
            }

            SmartDeviceEntity device = deviceResult.getData();
            if (!device.isOnline()) {
                return ResponseDTO.error("设备离线，无法同步时间");
            }

            // 2. 发送时间同步命令
            Map<String, Object> params = new HashMap<>();
            params.put("currentTime", LocalDateTime.now());
            params.put("timezone", "Asia/Shanghai");

            return smartDeviceService.remoteControlDevice(deviceId, "SYNC_TIME", params);

        } catch (Exception e) {
            log.error("同步考勤设备时间失败，deviceId: {}", deviceId, e);
            return ResponseDTO.error("同步时间失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤设备状态统计
     */
    public Map<String, Object> getAttendanceDeviceStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 1. 获取基础设备统计
            Map<String, Object> baseStats = smartDeviceService.getDeviceStatusStatistics();

            // 2. 获取考勤设备列表
            List<SmartDeviceEntity> attendanceDevices = smartDeviceService.getOnlineDevices().stream()
                .filter(SmartDeviceEntity::isAttendanceDevice)
                .toList();

            // 3. 统计考勤设备状态
            long onlineCount = attendanceDevices.stream().filter(SmartDeviceEntity::isOnline).count();
            long offlineCount = attendanceDevices.stream().filter(SmartDeviceEntity::isOffline).count();
            long faultCount = attendanceDevices.stream().filter(SmartDeviceEntity::hasFault).count();

            statistics.put("total", attendanceDevices.size());
            statistics.put("online", onlineCount);
            statistics.put("offline", offlineCount);
            statistics.put("fault", faultCount);
            statistics.put("onlineRate", attendanceDevices.isEmpty() ? 0 : (onlineCount * 100.0 / attendanceDevices.size()));

            return statistics;

        } catch (Exception e) {
            log.error("获取考勤设备统计失败", e);
            return new HashMap<>();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 解析考勤设备配置
     */
    private AttendanceDeviceConfig parseAttendanceConfig(Map<String, Object> extensionConfig) {
        if (extensionConfig == null || extensionConfig.isEmpty()) {
            return createDefaultAttendanceConfig();
        }

        try {
            return objectMapper.convertValue(extensionConfig, AttendanceDeviceConfig.class);
        } catch (Exception e) {
            log.warn("解析考勤设备配置失败，使用默认配置，config: {}", extensionConfig, e);
            return createDefaultAttendanceConfig();
        }
    }

    /**
     * 获取设备状态文本
     */
    private String getDeviceStatusText(String deviceStatus) {
        return DeviceStatus.getDescription(deviceStatus);
    }

    /**
     * 创建默认考勤设备配置
     */
    private AttendanceDeviceConfig createDefaultAttendanceConfig() {
        AttendanceDeviceConfig config = new AttendanceDeviceConfig();
        config.setWorkMode("NORMAL"); // 正常模式
        config.setPunchMode("FINGERPRINT"); // 指纹打卡
        config.setGpsEnabled(false); // GPS验证关闭
        config.setPhotoEnabled(false); // 拍照验证关闭
        config.setFaceRecognitionEnabled(false); // 人脸识别关闭
        config.setVoicePromptEnabled(true); // 语音提示开启
        config.setLedIndicatorEnabled(true); // LED指示灯开启
        config.setAutoSyncTime(true); // 自动同步时间
        config.setSyncInterval(3600); // 同步间隔1小时
        config.setOfflineStorageEnabled(true); // 离线存储开启
        config.setMaxOfflineRecords(10000); // 最大离线记录数
        config.setTemperatureDetectionEnabled(false); // 体温检测关闭
        config.setMaskDetectionEnabled(false); // 口罩检测关闭
        config.setDuressAlarmEnabled(false); // 胁迫报警关闭
        return config;
    }

    /**
     * 转换为考勤设备VO
     */
    private AttendanceDeviceVO convertToAttendanceDeviceVO(SmartDeviceEntity device) {
        AttendanceDeviceVO vo = new AttendanceDeviceVO();
        vo.setDeviceId(device.getDeviceId());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setDeviceName(device.getDeviceName());
        vo.setDeviceStatus(device.getDeviceStatus());
        vo.setIpAddress(device.getIpAddress());
        vo.setPort(device.getPort());
        vo.setLocation(device.getLocation());
        vo.setLastOnlineTime(device.getLastOnlineTime());
        vo.setDeviceStatusText(getDeviceStatusText(device.getDeviceStatus()));
        vo.setConnectionAddress(device.getConnectionAddress());

        // 解析考勤配置
        AttendanceDeviceConfig config = parseAttendanceConfig(device.getExtensionConfig());
        vo.setWorkMode(config.getWorkMode());
        vo.setPunchMode(config.getPunchMode());
        vo.setGpsEnabled(config.getGpsEnabled());
        vo.setPhotoEnabled(config.getPhotoEnabled());

        return vo;
    }

    
    // ==================== 内部类定义 ====================

    /**
     * 考勤设备配置类
     */
    public static class AttendanceDeviceConfig {
        private String workMode; // 工作模式 (NORMAL/OVERTIME/HOLIDAY)
        private String punchMode; // 打卡方式 (FINGERPRINT/FACE/CARD/PASSWORD/QR)
        private Boolean gpsEnabled; // GPS验证启用
        private Boolean photoEnabled; // 拍照验证启用
        private Boolean faceRecognitionEnabled; // 人脸识别启用
        private Boolean voicePromptEnabled; // 语音提示启用
        private Boolean ledIndicatorEnabled; // LED指示灯启用
        private Boolean autoSyncTime; // 自动同步时间
        private Integer syncInterval; // 同步间隔（秒）
        private Boolean offlineStorageEnabled; // 离线存储启用
        private Integer maxOfflineRecords; // 最大离线记录数
        private Boolean temperatureDetectionEnabled; // 体温检测启用
        private Boolean maskDetectionEnabled; // 口罩检测启用
        private Boolean duressAlarmEnabled; // 胁迫报警启用

        // Getter和Setter方法
        public String getWorkMode() { return workMode; }
        public void setWorkMode(String workMode) { this.workMode = workMode; }
        public String getPunchMode() { return punchMode; }
        public void setPunchMode(String punchMode) { this.punchMode = punchMode; }
        public Boolean getGpsEnabled() { return gpsEnabled; }
        public void setGpsEnabled(Boolean gpsEnabled) { this.gpsEnabled = gpsEnabled; }
        public Boolean getPhotoEnabled() { return photoEnabled; }
        public void setPhotoEnabled(Boolean photoEnabled) { this.photoEnabled = photoEnabled; }
        public Boolean getFaceRecognitionEnabled() { return faceRecognitionEnabled; }
        public void setFaceRecognitionEnabled(Boolean faceRecognitionEnabled) { this.faceRecognitionEnabled = faceRecognitionEnabled; }
        public Boolean getVoicePromptEnabled() { return voicePromptEnabled; }
        public void setVoicePromptEnabled(Boolean voicePromptEnabled) { this.voicePromptEnabled = voicePromptEnabled; }
        public Boolean getLedIndicatorEnabled() { return ledIndicatorEnabled; }
        public void setLedIndicatorEnabled(Boolean ledIndicatorEnabled) { this.ledIndicatorEnabled = ledIndicatorEnabled; }
        public Boolean getAutoSyncTime() { return autoSyncTime; }
        public void setAutoSyncTime(Boolean autoSyncTime) { this.autoSyncTime = autoSyncTime; }
        public Integer getSyncInterval() { return syncInterval; }
        public void setSyncInterval(Integer syncInterval) { this.syncInterval = syncInterval; }
        public Boolean getOfflineStorageEnabled() { return offlineStorageEnabled; }
        public void setOfflineStorageEnabled(Boolean offlineStorageEnabled) { this.offlineStorageEnabled = offlineStorageEnabled; }
        public Integer getMaxOfflineRecords() { return maxOfflineRecords; }
        public void setMaxOfflineRecords(Integer maxOfflineRecords) { this.maxOfflineRecords = maxOfflineRecords; }
        public Boolean getTemperatureDetectionEnabled() { return temperatureDetectionEnabled; }
        public void setTemperatureDetectionEnabled(Boolean temperatureDetectionEnabled) { this.temperatureDetectionEnabled = temperatureDetectionEnabled; }
        public Boolean getMaskDetectionEnabled() { return maskDetectionEnabled; }
        public void setMaskDetectionEnabled(Boolean maskDetectionEnabled) { this.maskDetectionEnabled = maskDetectionEnabled; }
        public Boolean getDuressAlarmEnabled() { return duressAlarmEnabled; }
        public void setDuressAlarmEnabled(Boolean duressAlarmEnabled) { this.duressAlarmEnabled = duressAlarmEnabled; }
    }

    /**
     * 考勤设备详情VO
     */
    public static class AttendanceDeviceDetailVO {
        private SmartDeviceEntity device;
        private AttendanceDeviceConfig attendanceConfig;
        private AreaEntity areaConfig;
        private String deviceStatusText;

        // Getter和Setter方法
        public SmartDeviceEntity getDevice() { return device; }
        public void setDevice(SmartDeviceEntity device) { this.device = device; }
        public AttendanceDeviceConfig getAttendanceConfig() { return attendanceConfig; }
        public void setAttendanceConfig(AttendanceDeviceConfig attendanceConfig) { this.attendanceConfig = attendanceConfig; }
        public AreaEntity getAreaConfig() { return areaConfig; }
        public void setAreaConfig(AreaEntity areaConfig) { this.areaConfig = areaConfig; }
        public String getDeviceStatusText() { return deviceStatusText; }
        public void setDeviceStatusText(String deviceStatusText) { this.deviceStatusText = deviceStatusText; }
    }

    /**
     * 考勤设备VO
     */
    public static class AttendanceDeviceVO {
        private Long deviceId;
        private String deviceCode;
        private String deviceName;
        private String deviceStatus;
        private String deviceStatusText;
        private String ipAddress;
        private Integer port;
        private String location;
        private LocalDateTime lastOnlineTime;
        private String connectionAddress;
        private String workMode;
        private String punchMode;
        private Boolean gpsEnabled;
        private Boolean photoEnabled;

        // Getter和Setter方法
        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
        public String getDeviceCode() { return deviceCode; }
        public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public String getDeviceStatus() { return deviceStatus; }
        public void setDeviceStatus(String deviceStatus) { this.deviceStatus = deviceStatus; }
        public String getDeviceStatusText() { return deviceStatusText; }
        public void setDeviceStatusText(String deviceStatusText) { this.deviceStatusText = deviceStatusText; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public Integer getPort() { return port; }
        public void setPort(Integer port) { this.port = port; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public LocalDateTime getLastOnlineTime() { return lastOnlineTime; }
        public void setLastOnlineTime(LocalDateTime lastOnlineTime) { this.lastOnlineTime = lastOnlineTime; }
        public String getConnectionAddress() { return connectionAddress; }
        public void setConnectionAddress(String connectionAddress) { this.connectionAddress = connectionAddress; }
        public String getWorkMode() { return workMode; }
        public void setWorkMode(String workMode) { this.workMode = workMode; }
        public String getPunchMode() { return punchMode; }
        public void setPunchMode(String punchMode) { this.punchMode = punchMode; }
        public Boolean getGpsEnabled() { return gpsEnabled; }
        public void setGpsEnabled(Boolean gpsEnabled) { this.gpsEnabled = gpsEnabled; }
        public Boolean getPhotoEnabled() { return photoEnabled; }
        public void setPhotoEnabled(Boolean photoEnabled) { this.photoEnabled = photoEnabled; }
    }
}