package net.lab1024.sa.device.comm.service.impl;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.device.comm.protocol.rs485.*;
import net.lab1024.sa.device.comm.protocol.*;
import net.lab1024.sa.device.comm.service.RS485ProtocolService;
import net.lab1024.sa.device.comm.dao.DeviceCommLogDao;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Future;

/**
 * RS485协议服务实现
 * <p>
 * 严格遵循四层架构规范的Service层实现：
 * - 位于Service层，负责业务接口定义和事务管理
 * - 使用@Resource注解进行依赖注入
 * - 调用Manager层处理复杂业务逻辑
 * - 处理事务边界和异常转换
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RS485ProtocolServiceImpl implements RS485ProtocolService {

    @Resource
    private RS485ProtocolManager rs485ProtocolManager;

    @Resource
    private RS485ProtocolAdapter rs485ProtocolAdapter;

    @Resource
    private DeviceCommLogDao deviceCommLogDao;

    @Override
    public ResponseDTO<RS485InitResultVO> initializeDevice(Long deviceId,
                                                            Map<String, Object> deviceInfo,
                                                            Map<String, Object> config) {
        try {
            log.info("[RS485服务] 初始化设备, deviceId={}", deviceId);

            // 参数验证
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }
            if (deviceInfo == null || deviceInfo.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "设备信息不能为空");
            }
            if (config == null || config.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "配置参数不能为空");
            }

            // 验证设备型号
            String deviceModel = (String) deviceInfo.get("deviceModel");
            if (deviceModel == null || !rs485ProtocolAdapter.isDeviceModelSupported(deviceModel)) {
                return ResponseDTO.error("UNSUPPORTED_DEVICE", "不支持的设备型号: " + deviceModel);
            }

            // 调用Manager层处理
            Future<RS485InitResult> future = rs485ProtocolManager.initializeDevice(deviceId, deviceInfo, config);
            RS485InitResult result = future.get();

            // 记录操作日志
            logDeviceCommOperation("INITIALIZE_DEVICE", deviceId,
                    result.isSuccess() ? "设备初始化成功" : "设备初始化失败: " + result.getErrorMessage());

            if (result.isSuccess()) {
                RS485InitResultVO vo = convertToInitResultVO(result);
                return ResponseDTO.ok(vo);
            } else {
                return ResponseDTO.error("INITIALIZE_FAILED", result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("[RS485服务] 设备初始化异常, deviceId={}", deviceId, e);
            logDeviceCommOperation("INITIALIZE_DEVICE_ERROR", deviceId, e.getMessage());
            return ResponseDTO.error("SYSTEM_ERROR", "设备初始化失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<RS485ProcessResultVO> processDeviceMessage(Long deviceId,
                                                                 byte[] rawData,
                                                                 String protocolType) {
        try {
            log.debug("[RS485服务] 处理设备消息, deviceId={}, protocolType={}", deviceId, protocolType);

            // 参数验证
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }
            if (rawData == null || rawData.length == 0) {
                return ResponseDTO.error("PARAM_ERROR", "消息数据不能为空");
            }
            if (protocolType == null || protocolType.trim().isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "协议类型不能为空");
            }

            // 调用Manager层处理
            Future<RS485ProcessResult> future = rs485ProtocolManager.processDeviceMessage(deviceId, rawData, protocolType);
            RS485ProcessResult result = future.get();

            // 记录操作日志
            logDeviceCommOperation("PROCESS_MESSAGE", deviceId,
                    result.isSuccess() ? "消息处理成功" : "消息处理失败: " + result.getErrorMessage());

            if (result.isSuccess()) {
                RS485ProcessResultVO vo = convertToProcessResultVO(result);
                return ResponseDTO.ok(vo);
            } else {
                return ResponseDTO.error("PROCESS_FAILED", result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("[RS485服务] 设备消息处理异常, deviceId={}", deviceId, e);
            logDeviceCommOperation("PROCESS_MESSAGE_ERROR", deviceId, e.getMessage());
            return ResponseDTO.error("SYSTEM_ERROR", "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<RS485HeartbeatResultVO> processDeviceHeartbeat(Long deviceId,
                                                                     Map<String, Object> heartbeatData) {
        try {
            log.debug("[RS485服务] 处理设备心跳, deviceId={}", deviceId);

            // 参数验证
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }
            if (heartbeatData == null || heartbeatData.isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "心跳数据不能为空");
            }

            // 调用Manager层处理
            Future<RS485HeartbeatResult> future = rs485ProtocolManager.processDeviceHeartbeat(deviceId, heartbeatData);
            RS485HeartbeatResult result = future.get();

            // 记录操作日志
            logDeviceCommOperation("PROCESS_HEARTBEAT", deviceId,
                    result.isSuccess() ? "心跳处理成功" : "心跳处理失败: " + result.getErrorMessage());

            if (result.isSuccess()) {
                RS485HeartbeatResultVO vo = convertToHeartbeatResultVO(result);
                return ResponseDTO.ok(vo);
            } else {
                return ResponseDTO.error("HEARTBEAT_FAILED", result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("[RS485服务] 设备心跳处理异常, deviceId={}", deviceId, e);
            logDeviceCommOperation("PROCESS_HEARTBEAT_ERROR", deviceId, e.getMessage());
            return ResponseDTO.error("SYSTEM_ERROR", "心跳处理失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<byte[]> buildDeviceResponse(Long deviceId,
                                                   String messageType,
                                                   Map<String, Object> businessData) {
        try {
            log.debug("[RS485服务] 构建设备响应, deviceId={}, messageType={}", deviceId, messageType);

            // 参数验证
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }
            if (messageType == null || messageType.trim().isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "消息类型不能为空");
            }
            if (businessData == null) {
                businessData = new HashMap<>();
            }

            // 调用Manager层处理
            Future<byte[]> future = rs485ProtocolManager.buildDeviceResponse(deviceId, messageType, businessData);
            byte[] responseData = future.get();

            // 记录操作日志
            logDeviceCommOperation("BUILD_RESPONSE", deviceId, "响应构建成功");

            return ResponseDTO.ok(responseData);

        } catch (Exception e) {
            log.error("[RS485服务] 设备响应构建异常, deviceId={}", deviceId, e);
            logDeviceCommOperation("BUILD_RESPONSE_ERROR", deviceId, e.getMessage());
            return ResponseDTO.error("SYSTEM_ERROR", "响应构建失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<RS485DeviceStatusVO> getDeviceStatus(Long deviceId) {
        try {
            log.debug("[RS485服务] 获取设备状态, deviceId={}", deviceId);

            // 参数验证
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }

            // 调用Manager层处理
            RS485DeviceStatus status = rs485ProtocolManager.getDeviceStatus(deviceId);

            RS485DeviceStatusVO vo = convertToDeviceStatusVO(status);
            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("[RS485服务] 获取设备状态异常, deviceId={}", deviceId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取设备状态失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> disconnectDevice(Long deviceId) {
        try {
            log.info("[RS485服务] 断开设备连接, deviceId={}", deviceId);

            // 参数验证
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }

            // 调用Manager层处理
            boolean result = rs485ProtocolManager.disconnectDevice(deviceId);

            // 记录操作日志
            logDeviceCommOperation("DISCONNECT_DEVICE", deviceId,
                    result ? "设备断开成功" : "设备断开失败");

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[RS485服务] 断开设备连接异常, deviceId={}", deviceId, e);
            logDeviceCommOperation("DISCONNECT_DEVICE_ERROR", deviceId, e.getMessage());
            return ResponseDTO.error("SYSTEM_ERROR", "断开设备连接失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getPerformanceStatistics() {
        try {
            log.debug("[RS485服务] 获取性能统计");

            // 调用Manager层处理
            Map<String, Object> statistics = rs485ProtocolManager.getPerformanceStatistics();

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("[RS485服务] 获取性能统计异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取性能统计失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<String[]> getSupportedDeviceModels() {
        try {
            log.debug("[RS485服务] 获取支持的设备型号");

            // 调用Manager层处理
            String[] models = rs485ProtocolManager.getSupportedDeviceModels();

            return ResponseDTO.ok(models);

        } catch (Exception e) {
            log.error("[RS485服务] 获取支持的设备型号异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取支持的设备型号失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Boolean> isDeviceModelSupported(String deviceModel) {
        try {
            log.debug("[RS485服务] 检查设备型号支持, deviceModel={}", deviceModel);

            // 参数验证
            if (deviceModel == null || deviceModel.trim().isEmpty()) {
                return ResponseDTO.error("PARAM_ERROR", "设备型号不能为空");
            }

            // 调用Manager层处理
            boolean supported = rs485ProtocolManager.isDeviceModelSupported(deviceModel);

            return ResponseDTO.ok(supported);

        } catch (Exception e) {
            log.error("[RS485服务] 检查设备型号支持异常, deviceModel={}", deviceModel, e);
            return ResponseDTO.error("SYSTEM_ERROR", "检查设备型号支持失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 转换初始化结果为VO
     */
    private RS485InitResultVO convertToInitResultVO(RS485InitResult result) {
        RS485InitResultVO vo = new RS485InitResultVO();
        vo.setSuccess(result.isSuccess());
        vo.setMessage(result.getMessage());
        vo.setInitTime(System.currentTimeMillis());
        return vo;
    }

    /**
     * 转换处理结果为VO
     */
    private RS485ProcessResultVO convertToProcessResultVO(RS485ProcessResult result) {
        RS485ProcessResultVO vo = new RS485ProcessResultVO();
        vo.setSuccess(result.isSuccess());
        vo.setMessage(result.getMessage());
        vo.setProcessTime(System.currentTimeMillis());

        if (result.getBusinessResult() != null) {
            vo.setBusinessData(result.getBusinessResult().getBusinessData());
            vo.setBusinessType(result.getBusinessResult().getBusinessType());
        }

        return vo;
    }

    /**
     * 转换心跳结果为VO
     */
    private RS485HeartbeatResultVO convertToHeartbeatResultVO(RS485HeartbeatResult result) {
        RS485HeartbeatResultVO vo = new RS485HeartbeatResultVO();
        vo.setSuccess(result.isSuccess());
        vo.setMessage(result.getMessage());
        vo.setHeartbeatTime(System.currentTimeMillis());

        if (result.getDeviceStatus() != null) {
            vo.setDeviceStatus(result.getDeviceStatus());
        }

        if (result.getHealthStatus() != null) {
            vo.setHealthStatus(result.getHealthStatus().name());
        }

        return vo;
    }

    /**
     * 转换设备状态为VO
     */
    private RS485DeviceStatusVO convertToDeviceStatusVO(RS485DeviceStatus status) {
        RS485DeviceStatusVO vo = new RS485DeviceStatusVO();
        vo.setDeviceId(status.getDeviceId());
        vo.setStatus(status.getStatus());
        vo.setMessage(status.getMessage());
        vo.setCheckTime(System.currentTimeMillis());
        vo.setDeviceData(status.getDeviceData());
        return vo;
    }

    /**
     * 记录设备通讯操作日志
     */
    private void logDeviceCommOperation(String operation, Long deviceId, String message) {
        try {
            // 异步记录日志，避免影响主流程性能
            if (deviceCommLogDao != null) {
                deviceCommLogDao.insertCommLog(deviceId, operation, message, null);
            }
        } catch (Exception e) {
            log.warn("[RS485服务] 记录设备通讯日志失败, operation={}, deviceId={}", operation, deviceId, e);
        }
    }

    // ==================== VO类 ====================

    /**
     * RS485初始化结果VO
     */
    @Schema(description = "RS485初始化结果")
    public static class RS485InitResultVO {
        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "消息")
        private String message;

        @Schema(description = "初始化时间")
        private Long initTime;

        // getters and setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getInitTime() { return initTime; }
        public void setInitTime(Long initTime) { this.initTime = initTime; }
    }

    /**
     * RS485处理结果VO
     */
    @Schema(description = "RS485处理结果")
    public static class RS485ProcessResultVO {
        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "消息")
        private String message;

        @Schema(description = "处理时间")
        private Long processTime;

        @Schema(description = "业务类型")
        private String businessType;

        @Schema(description = "业务数据")
        private Map<String, Object> businessData;

        // getters and setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getProcessTime() { return processTime; }
        public void setProcessTime(Long processTime) { this.processTime = processTime; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public Map<String, Object> getBusinessData() { return businessData; }
        public void setBusinessData(Map<String, Object> businessData) { this.businessData = businessData; }
    }

    /**
     * RS485心跳结果VO
     */
    @Schema(description = "RS485心跳结果")
    public static class RS485HeartbeatResultVO {
        @Schema(description = "是否成功")
        private Boolean success;

        @Schema(description = "消息")
        private String message;

        @Schema(description = "心跳时间")
        private Long heartbeatTime;

        @Schema(description = "设备状态")
        private Map<String, Object> deviceStatus;

        @Schema(description = "健康状态")
        private String healthStatus;

        // getters and setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getHeartbeatTime() { return heartbeatTime; }
        public void setHeartbeatTime(Long heartbeatTime) { this.heartbeatTime = heartbeatTime; }
        public Map<String, Object> getDeviceStatus() { return deviceStatus; }
        public void setDeviceStatus(Map<String, Object> deviceStatus) { this.deviceStatus = deviceStatus; }
        public String getHealthStatus() { return healthStatus; }
        public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    }

    /**
     * RS485设备状态VO
     */
    @Schema(description = "RS485设备状态")
    public static class RS485DeviceStatusVO {
        @Schema(description = "设备ID")
        private Long deviceId;

        @Schema(description = "状态")
        private String status;

        @Schema(description = "消息")
        private String message;

        @Schema(description = "检查时间")
        private Long checkTime;

        @Schema(description = "设备数据")
        private Map<String, Object> deviceData;

        // getters and setters
        public Long getDeviceId() { return deviceId; }
        public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getCheckTime() { return checkTime; }
        public void setCheckTime(Long checkTime) { this.checkTime = checkTime; }
        public Map<String, Object> getDeviceData() { return deviceData; }
        public void setDeviceData(Map<String, Object> deviceData) { this.deviceData = deviceData; }
    }
}