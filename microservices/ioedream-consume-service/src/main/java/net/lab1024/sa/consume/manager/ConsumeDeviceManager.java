package net.lab1024.sa.consume.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.consume.dao.ConsumeDeviceDao;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;
import net.lab1024.sa.common.entity.consume.ConsumeDeviceEntity;
import net.lab1024.sa.consume.exception.ConsumeDeviceException;

/**
 * 消费设备业务管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 负责复杂的业务逻辑编排
 * - 处理设备状态管理和通信验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
public class ConsumeDeviceManager {

    private final ConsumeDeviceDao consumeDeviceDao;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数注入依赖
     *
     * @param consumeDeviceDao 设备数据访问对象
     * @param objectMapper JSON处理工具
     */
    public ConsumeDeviceManager(ConsumeDeviceDao consumeDeviceDao, ObjectMapper objectMapper) {
        this.consumeDeviceDao = consumeDeviceDao;
        this.objectMapper = objectMapper;
    }

    /**
     * 验证设备编码唯一性
     *
     * @param deviceCode 设备编码
     * @param excludeId 排除的设备ID
     * @return 是否唯一
     */
    public boolean isDeviceCodeUnique(String deviceCode, Long excludeId) {
        if (deviceCode == null || deviceCode.trim().isEmpty()) {
            return false;
        }

        int count = consumeDeviceDao.countByDeviceCode(deviceCode.trim(), excludeId);
        return count == 0;
    }

    /**
     * 验证IP地址唯一性
     *
     * @param ipAddress IP地址
     * @param excludeId 排除的设备ID
     * @return 是否唯一
     */
    public boolean isIpAddressUnique(String ipAddress, Long excludeId) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return true; // IP地址可以为空，不强制唯一
        }

        int count = consumeDeviceDao.countByIpAddress(ipAddress.trim(), excludeId);
        return count == 0;
    }

    /**
     * 验证MAC地址唯一性
     *
     * @param macAddress MAC地址
     * @param excludeId 排除的设备ID
     * @return 是否唯一
     */
    public boolean isMacAddressUnique(String macAddress, Long excludeId) {
        if (macAddress == null || macAddress.trim().isEmpty()) {
            return true; // MAC地址可以为空，不强制唯一
        }

        int count = consumeDeviceDao.countByMacAddress(macAddress.trim(), excludeId);
        return count == 0;
    }

    /**
     * 检查设备是否可执行操作
     *
     * @param device 设备信息
     * @param operation 操作类型
     * @return 是否可执行
     */
    public boolean isDeviceOperable(ConsumeDeviceEntity device, String operation) {
        if (device == null) {
            return false;
        }

        // 检查设备状态
        if (isMaintenance(device)) {
            log.warn("设备维护中，无法执行操作: deviceId={}, operation={}", device.getDeviceId(), operation);
            return false;
        }

        if (isDisabled(device)) {
            log.warn("设备已停用，无法执行操作: deviceId={}, operation={}", device.getDeviceId(), operation);
            return false;
        }

        // 如果是重启、同步配置等操作，设备必须在线
        if ("restart".equals(operation) || "sync".equals(operation) || "upgrade".equals(operation)) {
            if (!isOnline(device)) {
                log.warn("设备离线，无法执行操作: deviceId={}, operation={}", device.getDeviceId(), operation);
                return false;
            }
        }

        return true;
    }

    /**
     * 检查设备健康状态
     *
     * @param device 设备信息
     * @param currentTime 当前时间
     * @return 健康检查结果
     */
    public Map<String, Object> checkDeviceHealth(ConsumeDeviceEntity device, LocalDateTime currentTime) {
        Map<String, Object> healthResult = new HashMap<>();

        healthResult.put("deviceId", device.getDeviceId());
        healthResult.put("deviceName", device.getDeviceName());
        healthResult.put("checkTime", currentTime);

        // 基础健康状态
        String healthStatus = "正常";
        List<String> issues = new ArrayList<>();

        // 检查连接状态
        if (device.getLastCommunicationTime() != null) {
            long minutesSinceLastCommunication =
                java.time.Duration.between(device.getLastCommunicationTime(), currentTime).toMinutes();

            if (minutesSinceLastCommunication > 60) {
                healthStatus = "异常";
                issues.add("超过60分钟未通信");
            } else if (minutesSinceLastCommunication > 30) {
                if (healthStatus.equals("正常")) {
                    healthStatus = "警告";
                }
                issues.add("超过30分钟未通信");
            }
        } else {
            healthStatus = "未知";
            issues.add("从未通信记录");
        }

        // 检查设备状态
        if (isFault(device)) {
            healthStatus = "故障";
            issues.add("设备故障");
        }

        // 检查固件版本是否需要更新
        String latestFirmware = getLatestFirmwareForDeviceType(device.getDeviceType());
        if (latestFirmware != null && !latestFirmware.equals(device.getFirmwareVersion())) {
            healthStatus = "需要更新";
            issues.add("固件版本过时，当前: " + device.getFirmwareVersion() + "，最新: " + latestFirmware);
        }

        healthResult.put("healthStatus", healthStatus);
        healthResult.put("issues", issues);
        healthResult.put("needsAttention", !issues.isEmpty());

        return healthResult;
    }

    /**
     * 获取设备类型的最新固件版本
     */
    private String getLatestFirmwareForDeviceType(Integer deviceType) {
        // 这里可以配置不同设备类型的最新固件版本
        switch (deviceType) {
            case 1: return "v2.1.0"; // 消费机
            case 2: return "v1.8.5"; // 充值机
            case 3: return "v3.0.2"; // 闸机
            case 4: return "v1.5.1"; // 自助终端
            default: return null;
        }
    }

    /**
     * 验证设备配置参数
     *
     * @param device 设备信息
     * @param configParams 配置参数
     * @return 验证结果
     */
    public Map<String, Object> validateDeviceConfig(ConsumeDeviceEntity device, Map<String, Object> configParams) {
        Map<String, Object> validationResult = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 验证必需参数
        if (!configParams.containsKey("serverUrl")) {
            errors.add("缺少服务器URL配置");
        }

        if (!configParams.containsKey("timeout")) {
            warnings.add("未配置超时时间，将使用默认值");
        }

        // 验证参数格式
        if (configParams.containsKey("port")) {
            try {
                Integer port = Integer.parseInt(configParams.get("port").toString());
                if (port < 1 || port > 65535) {
                    errors.add("端口号必须在1-65535范围内");
                }
            } catch (NumberFormatException e) {
                errors.add("端口号格式不正确");
            }
        }

        if (configParams.containsKey("retries")) {
            try {
                Integer retries = Integer.parseInt(configParams.get("retries").toString());
                if (retries < 0 || retries > 10) {
                    warnings.add("重试次数建议在0-10之间");
                }
            } catch (NumberFormatException e) {
                errors.add("重试次数格式不正确");
            }
        }

        // 验证业务属性
        try {
            if (device.getBusinessAttributes() != null && !device.getBusinessAttributes().trim().isEmpty()) {
                Map<String, Object> businessAttrs = getBusinessAttributes(device);

                // 验证离线模式配置
                if (supportsOffline(device) && !businessAttrs.containsKey("offlineConfig")) {
                    warnings.add("离线设备建议配置离线同步参数");
                }

                // 验证最大金额限制
                if (businessAttrs.containsKey("maxAmount")) {
                    try {
                    BigDecimal maxAmount = new BigDecimal(businessAttrs.get("maxAmount").toString());
                    if (maxAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        errors.add("最大金额必须大于0");
                    }
                } catch (NumberFormatException e) {
                    errors.add("最大金额格式不正确");
                }
                }
            }
        } catch (JsonProcessingException e) {
            errors.add("业务属性JSON格式不正确: " + e.getMessage());
        }

        validationResult.put("valid", errors.isEmpty());
        validationResult.put("errors", errors);
        validationResult.put("warnings", warnings);
        validationResult.put("timestamp", LocalDateTime.now());

        return validationResult;
    }

    /**
     * 解析业务属性JSON
     */
    public Map<String, Object> parseBusinessAttributes(String businessAttributesJson) throws JsonProcessingException {
        if (businessAttributesJson == null || businessAttributesJson.trim().isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(businessAttributesJson, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 生成设备配置
     *
     * @param device 设备信息
     * @return 配置信息
     */
    public Map<String, Object> generateDeviceConfig(ConsumeDeviceEntity device) {
        Map<String, Object> config = new HashMap<>();

        // 基础配置
        config.put("deviceId", device.getDeviceId());
        config.put("deviceCode", device.getDeviceCode());
        config.put("deviceName", device.getDeviceName());
        config.put("deviceType", device.getDeviceType());

        // 网络配置
        if (device.getIpAddress() != null && !device.getIpAddress().trim().isEmpty()) {
            config.put("ipAddress", device.getIpAddress());
            config.put("port", 8080); // 默认端口
        }
        config.put("timeout", 30000); // 30秒超时
        config.put("retries", 3); // 重试3次

        // 业务配置
        try {
            if (device.getBusinessAttributes() != null && !device.getBusinessAttributes().trim().isEmpty()) {
                Map<String, Object> businessAttrs = getBusinessAttributes(device);
                config.putAll(businessAttrs);
            }
        } catch (JsonProcessingException e) {
            log.warn("解析业务属性失败，使用默认配置: deviceId={}, error={}",
                device.getDeviceId(), e.getMessage());
        }

        // 安全配置
        config.put("encryptionEnabled", true);
        config.put("heartbeatInterval", 60); // 60秒心跳
        config.put("maxOfflineDuration", 7200); // 2小时离线缓存

        // 功能配置
        config.put("supportOffline", device.supportsOffline());
        config.put("allowPartialOffline", true);
        config.put("autoSync", false);
        config.put("logLevel", "INFO");

        return config;
    }

    /**
     * 检查设备冲突
     *
     * @param deviceCode 设备编码
     * @param ipAddress IP地址
     * @param macAddress MAC地址
     * @param excludeId 排除的设备ID
     * @return 冲突检测结果
     */
    public Map<String, Object> checkDeviceConflicts(String deviceCode, String ipAddress, String macAddress, Long excludeId) {
        Map<String, Object> conflicts = new HashMap<>();
        List<String> conflictFields = new ArrayList<>();

        // 检查设备编码冲突
        if (!isDeviceCodeUnique(deviceCode, excludeId)) {
            conflictFields.add("设备编码已存在: " + deviceCode);
        }

        // 检查IP地址冲突
        if (!isIpAddressUnique(ipAddress, excludeId)) {
            conflictFields.add("IP地址已存在: " + ipAddress);
        }

        // 检查MAC地址冲突
        if (!isMacAddressUnique(macAddress, excludeId)) {
            conflictFields.add("MAC地址已存在: " + macAddress);
        }

        conflicts.put("hasConflict", !conflictFields.isEmpty());
        conflicts.put("conflictFields", conflictFields);
        conflicts.put("timestamp", LocalDateTime.now());

        return conflicts;
    }

    /**
     * 更新设备通信时间
     *
     * @param deviceId 设备ID
     * @param communicationTime 通信时间
     * @return 更新是否成功
     */
    public boolean updateLastCommunicationTime(Long deviceId, LocalDateTime communicationTime) {
        try {
            int updatedRows = consumeDeviceDao.updateLastCommunicationTime(deviceId, communicationTime);
            return updatedRows > 0;
        } catch (Exception e) {
            log.error("更新设备通信时间失败: deviceId={}, time={}, error={}",
                deviceId, communicationTime, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量更新设备状态
     *
     * @param deviceIds 设备ID列表
     * @param targetStatus 目标状态
     * @param reason 更新原因
     * @return 更新结果
     */
    public Map<String, Object> batchUpdateDeviceStatus(List<Long> deviceIds, Integer targetStatus, String reason) {
        Map<String, Object> result = new HashMap<>();
        List<String> failedDevices = new ArrayList<>();
        int successCount = 0;

        if (deviceIds == null || deviceIds.isEmpty()) {
            result.put("success", false);
            result.put("message", "设备ID列表为空");
            result.put("failedCount", 0);
            return result;
        }

        for (Long deviceId : deviceIds) {
            try {
                ConsumeDeviceEntity device = consumeDeviceDao.selectById(deviceId);
                if (device == null) {
                    failedDevices.add("设备不存在: " + deviceId);
                    continue;
                }

                // 检查状态转换是否合法
                if (!isValidStatusTransition(device.getDeviceStatus(), targetStatus)) {
                    failedDevices.add("状态转换不合法: " + device.getDeviceName() +
                                     " (" + getDeviceStatusName(device.getDeviceStatus()) + " → " +
                                     getDeviceStatusName(targetStatus) + ")");
                    continue;
                }

                // 更新状态
                device.setDeviceStatus(targetStatus);
                device.setUpdateTime(LocalDateTime.now());
                consumeDeviceDao.updateById(device);
                successCount++;

            } catch (Exception e) {
                failedDevices.add("设备ID: " + deviceId + ", 错误: " + e.getMessage());
            }
        }

        result.put("success", failedDevices.isEmpty());
        result.put("message", failedDevices.isEmpty() ? "批量更新成功" : "部分更新失败");
        result.put("successCount", successCount);
        result.put("failedCount", failedDevices.size());
        result.put("failedDevices", failedDevices);
        result.put("timestamp", LocalDateTime.now());

        return result;
    }

    /**
     * 验证状态转换是否合法
     */
    private boolean isValidStatusTransition(Integer currentStatus, Integer targetStatus) {
        // 允许的状态转换
        switch (currentStatus) {
            case 1: // 在线
                return targetStatus == 2 || targetStatus == 3 || targetStatus == 4 || targetStatus == 5; // 在线→离线、故障、维护、停用
            case 2: // 离线
                return targetStatus == 1 || targetStatus == 5; // 离线→在线、停用
            case 3: // 故障
                return targetStatus == 1 || targetStatus == 2; // 故障→在线、离线
            case 4: // 维护中
                return targetStatus == 1; // 维护中→在线
            case 5: // 停用
                return targetStatus == 1; // 停用→在线
            default:
                return false;
        }
    }

    /**
     * 获取设备统计信息
     *
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计信息
     */
    public Map<String, Object> getDeviceStatistics(String startDate, String endDate) {
        Map<String, Object> statistics = new HashMap<>();

        // 总体统计
        Map<String, Object> overallStats = consumeDeviceDao.getOverallStatistics(startDate, endDate);
        statistics.putAll(overallStats);

        // 按类型统计
        List<Map<String, Object>> typeStats = consumeDeviceDao.countByDeviceType();
        statistics.put("typeStatistics", typeStats);

        // 按状态统计
        List<Map<String, Object>> statusStats = consumeDeviceDao.countByDeviceStatus();
        statistics.put("statusStatistics", statusStats);

        // 健康统计
        Map<String, Object> healthStats = consumeDeviceDao.countByHealthStatus();
        statistics.put("healthStatistics", healthStats);

        // 离线时间统计
        List<Map<String, Object>> offlineStats = consumeDeviceDao.countByOfflineDuration();
        statistics.put("offlineDurationStatistics", offlineStats);

        // 通信统计
        List<Map<String, Object>> commStats = consumeDeviceDao.countByCommunicationPattern();
        statistics.put("communicationStatistics", commStats);

        statistics.put("timestamp", LocalDateTime.now());

        return statistics;
    }

    /**
     * 获取设备分组统计
     *
     * @param groupType 分组类型（byType、byStatus、byLocation、byHealth）
     * @return 分组统计
     */
    public List<Map<String, Object>> getDeviceGroupStatistics(String groupType) {
        try {
            switch (groupType.toLowerCase()) {
                case "bytype":
                    return consumeDeviceDao.countByDeviceType();
                case "bystatus":
                    return consumeDeviceDao.countByDeviceStatus();
                case "bylocation":
                    return consumeDeviceDao.countByLocation();
                case "byhealth":
                    Map<String, Object> healthData = consumeDeviceDao.countByHealthStatus();
                    List<Map<String, Object>> healthList = new ArrayList<>();
                    if (healthData != null) {
                        healthList.add(healthData);
                    }
                    return healthList;
                default:
                    return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("获取设备分组统计失败: groupType={}, error={}", groupType, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成设备报告
     *
     * @param deviceIds 设备ID列表
     * @param reportType 报告类型（summary、detailed、health）
     * @return 设备报告
     */
    public Map<String, Object> generateDeviceReport(List<Long> deviceIds, String reportType) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> deviceReports = new ArrayList<>();

        if (deviceIds == null || deviceIds.isEmpty()) {
            report.put("success", false);
            report.put("message", "设备ID列表为空");
            return report;
        }

        for (Long deviceId : deviceIds) {
            try {
                ConsumeDeviceEntity device = consumeDeviceDao.selectById(deviceId);
                if (device == null) {
                    continue;
                }

                Map<String, Object> deviceReport = new HashMap<>();
                deviceReport.put("deviceId", device.getDeviceId());
                deviceReport.put("deviceCode", device.getDeviceCode());
                deviceReport.put("deviceName", device.getDeviceName());
                deviceReport.put("deviceType", device.getDeviceType());
                deviceReport.put("deviceStatus", device.getDeviceStatus());
                deviceReport.put("deviceLocation", device.getDeviceLocation());
                deviceReport.put("lastCommunicationTime", device.getLastCommunicationTime());
                deviceReport.put("healthStatus", device.getHealthStatus());
                deviceReport.put("reportType", reportType);
                deviceReport.put("reportTime", LocalDateTime.now());

                if ("detailed".equals(reportType)) {
                    deviceReport.put("ipAddress", device.getIpAddress());
                    deviceReport.put("macAddress", device.getMacAddress());
                    deviceReport.put("deviceModel", device.getDeviceModel());
                    deviceReport.put("deviceManufacturer", device.getDeviceManufacturer());
                    deviceReport.put("firmwareVersion", device.getFirmwareVersion());
                    deviceReport.put("supportsOffline", device.supportsOffline());
                    deviceReport.put("businessAttributes", device.getBusinessAttributes());
                    deviceReport.put("deviceDescription", device.getDeviceDescription());
                    deviceReport.put("createTime", device.getCreateTime());
                    deviceReport.put("updateTime", device.getUpdateTime());
                }

                deviceReports.add(deviceReport);

            } catch (Exception e) {
                log.error("生成设备报告失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            }
        }

        report.put("success", true);
        report.put("deviceCount", deviceReports.size());
        report.put("devices", deviceReports);
        report.put("generatedAt", LocalDateTime.now());

        return report;
    }

    /**
     * 获取设备类型名称
     */
    private String getDeviceStatusName(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 1: return "在线";
            case 2: return "离线";
            case 3: return "故障";
            case 4: return "维护中";
            case 5: return "停用";
            default: return "未知";
        }
    }
}