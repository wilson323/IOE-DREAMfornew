package net.lab1024.sa.device.comm.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.device.comm.dao.DeviceCommLogDao;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolHeartbeatResult;
import net.lab1024.sa.device.comm.protocol.rs485.RS485DeviceStatus;
import net.lab1024.sa.device.comm.protocol.rs485.RS485DeviceStatusVO;
import net.lab1024.sa.device.comm.protocol.rs485.RS485HeartbeatResult;
import net.lab1024.sa.device.comm.protocol.rs485.RS485HeartbeatResultVO;
import net.lab1024.sa.device.comm.protocol.rs485.RS485InitResult;
import net.lab1024.sa.device.comm.protocol.rs485.RS485InitResultVO;
import net.lab1024.sa.device.comm.protocol.rs485.RS485ProcessResult;
import net.lab1024.sa.device.comm.protocol.rs485.RS485ProcessResultVO;
import net.lab1024.sa.device.comm.protocol.rs485.RS485ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.rs485.RS485ProtocolManager;
import net.lab1024.sa.device.comm.service.RS485ProtocolService;

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
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
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

            // 调用Adapter层处理
            var initFuture = rs485ProtocolAdapter.initializeDevice(deviceInfo, config);
            var initResult = initFuture.get();

            // 记录操作日志
            logDeviceCommOperation("INITIALIZE_DEVICE", deviceId,
                    initResult.isSuccess() ? "设备初始化成功" : "设备初始化失败: " + initResult.getErrorMessage());

            if (initResult.isSuccess()) {
                RS485InitResult rs485Result = RS485InitResult.success(
                        deviceId,
                        (String) deviceInfo.getOrDefault("serialNumber", "UNKNOWN"));
                rs485Result.setMessage("设备初始化成功");
                RS485InitResultVO vo = convertToInitResultVO(rs485Result);
                return ResponseDTO.ok(vo);
            } else {
                return ResponseDTO.error("INITIALIZE_FAILED", initResult.getErrorMessage());
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

            // 调用Manager层处理 (protocolType暂不使用，Manager已有协议类型)
            var processResultFuture = rs485ProtocolManager.processDeviceMessage(rawData, deviceId);
            var processResult = processResultFuture.get();

            // 转换为RS485ProcessResult
            RS485ProcessResult result;
            if (processResult.isSuccess()) {
                result = RS485ProcessResult.success(deviceId, protocolType, processResult.getResultData());
                result.setMessage(processResult.getMessage() != null ? processResult.getMessage() : "消息处理成功");
            } else {
                result = RS485ProcessResult.failure(
                        processResult.getErrorCode() != null ? processResult.getErrorCode() : "PROCESS_ERROR",
                        processResult.getErrorMessage() != null ? processResult.getErrorMessage() : "消息处理失败");
            }

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
    public ResponseDTO<RS485HeartbeatResultVO> sendHeartbeat(Long deviceId) {
        try {
            log.debug("[RS485服务] 发送心跳检测, deviceId={}", deviceId);

            // 参数验证
            if (deviceId == null) {
                return ResponseDTO.error("PARAM_ERROR", "设备ID不能为空");
            }

            // 调用Adapter层处理心跳（主动发送心跳）
            Map<String, Object> heartbeatData = new HashMap<>();
            heartbeatData.put("timestamp", System.currentTimeMillis());
            ProtocolHeartbeatResult heartbeatResult = rs485ProtocolAdapter.handleDeviceHeartbeat(heartbeatData,
                    deviceId);

            // 转换为VO
            RS485HeartbeatResultVO vo = RS485HeartbeatResultVO.builder()
                    .success(heartbeatResult.isSuccess())
                    .message(heartbeatResult.getResponseMessage() != null ? heartbeatResult.getResponseMessage()
                            : "心跳处理成功")
                    .deviceId(deviceId)
                    .online(heartbeatResult.isOnline())
                    .latency(0L) // ProtocolHeartbeatResult没有latency字段，使用默认值0
                    .heartbeatTime(heartbeatResult.getHeartbeatTime() != null
                            ? heartbeatResult.getHeartbeatTime().atZone(java.time.ZoneId.systemDefault()).toInstant()
                                    .toEpochMilli()
                            : System.currentTimeMillis())
                    .build();

            if (heartbeatResult.isSuccess()) {
                return ResponseDTO.ok(vo);
            } else {
                return ResponseDTO.error("HEARTBEAT_FAILED", heartbeatResult.getResponseMessage());
            }

        } catch (Exception e) {
            log.error("[RS485服务] 发送心跳检测异常, deviceId={}", deviceId, e);
            logDeviceCommOperation("SEND_HEARTBEAT_ERROR", deviceId, e.getMessage());
            return ResponseDTO.error("SYSTEM_ERROR", "心跳发送失败: " + e.getMessage());
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
    @Cacheable(value = "device:comm:rs485:status", key = "#deviceId", unless = "#result == null || !#result.isSuccess()")
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
    public ResponseDTO<Void> disconnectDevice(Long deviceId) {
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

            if (result) {
                return ResponseDTO.ok();
            } else {
                return ResponseDTO.error("DISCONNECT_FAILED", "设备断开失败");
            }

        } catch (Exception e) {
            log.error("[RS485服务] 断开设备连接异常, deviceId={}", deviceId, e);
            logDeviceCommOperation("DISCONNECT_DEVICE_ERROR", deviceId, e.getMessage());
            return ResponseDTO.error("SYSTEM_ERROR", "断开设备连接失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "device:comm:rs485:statistics", key = "'all'", unless = "#result == null || !#result.isSuccess()")
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
    @Cacheable(value = "device:comm:rs485:models", key = "'all'", unless = "#result == null || !#result.isSuccess()")
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
     * <p>
     * 采用适配器模式进行Result到VO的转换
     * </p>
     */
    private RS485InitResultVO convertToInitResultVO(RS485InitResult result) {
        return RS485InitResultVO.builder()
                .success(result.isSuccess())
                .message(result.getMessage() != null ? result.getMessage() : (result.isSuccess() ? "初始化成功" : "初始化失败"))
                .deviceId(result.getDeviceId())
                .serialNumber(result.getSerialNumber())
                .protocolVersion(result.getProtocolVersion())
                .initTime(result.getInitTime() != null
                        ? result.getInitTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        : System.currentTimeMillis())
                .status(result.isSuccess() ? "SUCCESS" : "FAILED")
                .build();
    }

    /**
     * 转换处理结果为VO
     * <p>
     * 采用适配器模式进行Result到VO的转换
     * </p>
     */
    private RS485ProcessResultVO convertToProcessResultVO(RS485ProcessResult result) {
        RS485ProcessResultVO.RS485ProcessResultVOBuilder builder = RS485ProcessResultVO.builder()
                .success(result.isSuccess())
                .message(result.getMessage() != null ? result.getMessage() : (result.isSuccess() ? "处理成功" : "处理失败"))
                .deviceId(result.getDeviceId())
                .messageType(result.getMessageType())
                .processTime(result.getProcessTime() != null
                        ? result.getProcessTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        : System.currentTimeMillis())
                .responseData(result.getResponseData())
                .status(result.isSuccess() ? "SUCCESS" : "FAILED");

        // 如果有业务数据，设置业务相关字段
        if (result.getResponseData() != null && !result.getResponseData().isEmpty()) {
            builder.businessData(result.getResponseData());
        }

        return builder.build();
    }

    /**
     * 转换心跳结果为VO
     * <p>
     * 采用适配器模式进行Result到VO的转换
     * </p>
     */
    private RS485HeartbeatResultVO convertToHeartbeatResultVO(RS485HeartbeatResult result) {
        return RS485HeartbeatResultVO.builder()
                .success(result.isSuccess())
                .message(result.getErrorMessage() != null ? result.getErrorMessage()
                        : (result.isSuccess() ? "心跳成功" : "心跳失败"))
                .deviceId(result.getDeviceId())
                .online(result.isOnline())
                .latency(result.getLatency())
                .heartbeatTime(result.getHeartbeatTime() != null
                        ? result.getHeartbeatTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        : System.currentTimeMillis())
                .build();
    }

    /**
     * 转换设备状态为VO
     */
    private RS485DeviceStatusVO convertToDeviceStatusVO(RS485DeviceStatus status) {
        return RS485DeviceStatusVO.builder()
                .deviceId(status.getDeviceId())
                .status(status.isOnline() ? "ONLINE" : "OFFLINE")
                .message(status.getConnectionStatus())
                .checkTime(System.currentTimeMillis())
                .deviceData(null) // RS485DeviceStatus没有deviceData字段
                .build();
    }

    /**
     * 记录设备通讯操作日志
     */
    private void logDeviceCommOperation(String operation, Long deviceId, String message) {
        try {
            // 异步记录日志，避免影响主流程性能
            if (deviceCommLogDao != null) {
                net.lab1024.sa.device.comm.entity.DeviceCommLogEntity logEntity = new net.lab1024.sa.device.comm.entity.DeviceCommLogEntity();
                logEntity.setDeviceId(deviceId);
                logEntity.setCommType(operation);
                logEntity.setRequestContent(message);
                logEntity.setStatus("SUCCESS");
                logEntity.setCreateTime(java.time.LocalDateTime.now());
                deviceCommLogDao.insert(logEntity);
            }
        } catch (Exception e) {
            log.warn("[RS485服务] 记录设备通讯日志失败, operation={}, deviceId={}", operation, deviceId, e);
        }
    }

    // 注：所有VO类已移动到 net.lab1024.sa.device.comm.protocol.rs485 包下
}
