package net.lab1024.sa.device.comm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.device.comm.protocol.ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.domain.*;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolBuildException;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolParseException;
import net.lab1024.sa.device.comm.protocol.factory.ProtocolAdapterFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 设备通讯协议统一管理控制器
 * <p>
 * 提供多厂商设备协议的统一接入和管理接口
 * 支持设备通讯协议的动态注册、消息解析和业务处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/device-comm")
@Tag(name = "设备通讯协议管理", description = "多厂商设备通讯协议的统一管理")
@PermissionCheck(value = "DEVICE_COMM", description = "设备通讯管理模块权限")
public class DeviceCommunicationController {

    @Resource
    private ProtocolAdapterFactory protocolAdapterFactory;

    /**
     * 解析设备消息
     * <p>
     * 根据设备协议类型解析原始设备消息数据
     * </p>
     *
     * @param protocolType 协议类型
     * @param hexData 十六进制数据
     * @param deviceId 设备ID
     * @return 解析后的协议消息
     */
    @Operation(summary = "解析设备消息", description = "根据协议类型解析设备原始消息数据")
    @PostMapping("/parse-message")
    @PermissionCheck(value = "DEVICE_COMM_PARSE", description = "解析设备消息")
    public ResponseDTO<ProtocolMessage> parseDeviceMessage(
            @RequestParam String protocolType,
            @RequestParam String hexData,
            @RequestParam(required = false) Long deviceId) {

        try {
            log.info("[设备通讯] 开始解析设备消息, protocolType={}, deviceId={}, dataLength={}",
                protocolType, deviceId, hexData != null ? hexData.length() : 0);

            ProtocolAdapter adapter = protocolAdapterFactory.getAdapter(protocolType);
            if (adapter == null) {
                return ResponseDTO.error("PROTOCOL_NOT_SUPPORTED", "不支持的协议类型: " + protocolType);
            }

            ProtocolMessage message = adapter.parseDeviceMessage(hexData, deviceId);

            log.info("[设备通讯] 设备消息解析成功, protocolType={}, messageType={}",
                protocolType, message != null ? message.getClass().getSimpleName() : "null");

            return ResponseDTO.ok(message);

        } catch (ProtocolParseException e) {
            log.error("[设备通讯] 消息解析异常, protocolType={}", protocolType, e);
            return ResponseDTO.error("PARSE_ERROR", "消息解析失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("[设备通讯] 系统异常, protocolType={}", protocolType, e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 构建设备响应消息
     * <p>
     * 根据协议类型构建设备响应消息
     * </p>
     *
     * @param protocolType 协议类型
     * @param messageType 消息类型
     * @param businessData 业务数据
     * @param deviceId 设备ID
     * @return 响应消息的十六进制数据
     */
    @Operation(summary = "构建设备响应", description = "根据协议类型构建设备响应消息")
    @PostMapping("/build-response")
    @PermissionCheck(value = "DEVICE_COMM_BUILD", description = "构建设备响应")
    public ResponseDTO<String> buildDeviceResponse(
            @RequestParam String protocolType,
            @RequestParam String messageType,
            @RequestBody Map<String, Object> businessData,
            @RequestParam(required = false) Long deviceId) {

        try {
            log.info("[设备通讯] 开始构建设备响应, protocolType={}, messageType={}, deviceId={}",
                protocolType, messageType, deviceId);

            ProtocolAdapter adapter = protocolAdapterFactory.getAdapter(protocolType);
            if (adapter == null) {
                return ResponseDTO.error("PROTOCOL_NOT_SUPPORTED", "不支持的协议类型: " + protocolType);
            }

            String responseData = adapter.buildDeviceResponseHex(messageType, businessData, deviceId);

            log.info("[设备通讯] 设备响应构建成功, protocolType={}, messageType={}, responseLength={}",
                protocolType, messageType, responseData != null ? responseData.length() : 0);

            return ResponseDTO.ok(responseData);

        } catch (ProtocolBuildException e) {
            log.error("[设备通讯] 响应构建异常, protocolType={}, messageType={}", protocolType, messageType, e);
            return ResponseDTO.error("BUILD_ERROR", "响应构建失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("[设备通讯] 系统异常, protocolType={}, messageType={}", protocolType, messageType, e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 处理门禁业务数据
     * <p>
     * 通过对应协议处理器处理门禁相关业务数据
     * </p>
     *
     * @param protocolType 协议类型
     * @param businessType 业务类型
     * @param businessData 业务数据
     * @param deviceId 设备ID
     * @return 业务处理结果
     */
    @Operation(summary = "处理门禁业务", description = "处理门禁相关业务数据")
    @PostMapping("/process-access")
    public ResponseDTO<ProtocolProcessResult> processAccessBusiness(
            @RequestParam String protocolType,
            @RequestParam String businessType,
            @RequestBody Map<String, Object> businessData,
            @RequestParam(required = false) Long deviceId) {

        try {
            log.info("[设备通讯] 开始处理门禁业务, protocolType={}, businessType={}, deviceId={}",
                protocolType, businessType, deviceId);

            ProtocolAdapter adapter = protocolAdapterFactory.getAdapter(protocolType);
            if (adapter == null) {
                return ResponseDTO.error("PROTOCOL_NOT_SUPPORTED", "不支持的协议类型: " + protocolType);
            }

            Future<ProtocolProcessResult> future = adapter.processAccessBusiness(businessType, businessData, deviceId);
            ProtocolProcessResult result = future.get();

            log.info("[设备通讯] 门禁业务处理完成, protocolType={}, businessType={}, success={}",
                protocolType, businessType, result != null ? result.isSuccess() : false);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[设备通讯] 门禁业务处理异常, protocolType={}, businessType={}", protocolType, businessType, e);
            return ResponseDTO.error("PROCESS_ERROR", "业务处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理消费业务数据
     * <p>
     * 通过对应协议处理器处理消费相关业务数据
     * </p>
     *
     * @param protocolType 协议类型
     * @param businessType 业务类型
     * @param businessData 业务数据
     * @param deviceId 设备ID
     * @return 业务处理结果
     */
    @Operation(summary = "处理消费业务", description = "处理消费相关业务数据")
    @PostMapping("/process-consume")
    public ResponseDTO<ProtocolProcessResult> processConsumeBusiness(
            @RequestParam String protocolType,
            @RequestParam String businessType,
            @RequestBody Map<String, Object> businessData,
            @RequestParam(required = false) Long deviceId) {

        try {
            log.info("[设备通讯] 开始处理消费业务, protocolType={}, businessType={}, deviceId={}",
                protocolType, businessType, deviceId);

            ProtocolAdapter adapter = protocolAdapterFactory.getAdapter(protocolType);
            if (adapter == null) {
                return ResponseDTO.error("PROTOCOL_NOT_SUPPORTED", "不支持的协议类型: " + protocolType);
            }

            Future<ProtocolProcessResult> future = adapter.processConsumeBusiness(businessType, businessData, deviceId);
            ProtocolProcessResult result = future.get();

            log.info("[设备通讯] 消费业务处理完成, protocolType={}, businessType={}, success={}",
                protocolType, businessType, result != null ? result.isSuccess() : false);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[设备通讯] 消费业务处理异常, protocolType={}, businessType={}", protocolType, businessType, e);
            return ResponseDTO.error("PROCESS_ERROR", "业务处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理考勤业务数据
     * <p>
     * 通过对应协议处理器处理考勤相关业务数据
     * </p>
     *
     * @param protocolType 协议类型
     * @param businessType 业务类型
     * @param businessData 业务数据
     * @param deviceId 设备ID
     * @return 业务处理结果
     */
    @Operation(summary = "处理考勤业务", description = "处理考勤相关业务数据")
    @PostMapping("/process-attendance")
    public ResponseDTO<ProtocolProcessResult> processAttendanceBusiness(
            @RequestParam String protocolType,
            @RequestParam String businessType,
            @RequestBody Map<String, Object> businessData,
            @RequestParam(required = false) Long deviceId) {

        try {
            log.info("[设备通讯] 开始处理考勤业务, protocolType={}, businessType={}, deviceId={}",
                protocolType, businessType, deviceId);

            ProtocolAdapter adapter = protocolAdapterFactory.getAdapter(protocolType);
            if (adapter == null) {
                return ResponseDTO.error("PROTOCOL_NOT_SUPPORTED", "不支持的协议类型: " + protocolType);
            }

            Future<ProtocolProcessResult> future = adapter.processAttendanceBusiness(businessType, businessData, deviceId);
            ProtocolProcessResult result = future.get();

            log.info("[设备通讯] 考勤业务处理完成, protocolType={}, businessType={}, success={}",
                protocolType, businessType, result != null ? result.isSuccess() : false);

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[设备通讯] 考勤业务处理异常, protocolType={}, businessType={}", protocolType, businessType, e);
            return ResponseDTO.error("PROCESS_ERROR", "业务处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的协议类型列表
     *
     * @return 协议类型列表
     */
    @Operation(summary = "获取支持的协议类型", description = "获取系统支持的所有协议类型")
    @GetMapping("/protocols")
    public ResponseDTO<List<String>> getSupportedProtocols() {
        try {
            List<String> protocols = protocolAdapterFactory.getSupportedProtocolTypes();
            log.info("[设备通讯] 获取支持的协议类型, 数量: {}", protocols.size());
            return ResponseDTO.ok(protocols);
        } catch (Exception e) {
            log.error("[设备通讯] 获取协议类型异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 获取支持的设备型号列表
     *
     * @return 设备型号列表
     */
    @Operation(summary = "获取支持的设备型号", description = "获取系统支持的所有设备型号")
    @GetMapping("/device-models")
    public ResponseDTO<List<String>> getSupportedDeviceModels() {
        try {
            List<String> deviceModels = protocolAdapterFactory.getSupportedDeviceModels();
            log.info("[设备通讯] 获取支持的设备型号, 数量: {}", deviceModels.size());
            return ResponseDTO.ok(deviceModels);
        } catch (Exception e) {
            log.error("[设备通讯] 获取设备型号异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 根据设备型号获取适配器信息
     *
     * @param deviceModel 设备型号
     * @return 适配器信息
     */
    @Operation(summary = "获取设备适配器信息", description = "根据设备型号获取对应的协议适配器信息")
    @GetMapping("/adapter/{deviceModel}")
    public ResponseDTO<Map<String, Object>> getAdapterByDeviceModel(@PathVariable String deviceModel) {
        try {
            ProtocolAdapter adapter = protocolAdapterFactory.getAdapterByDeviceModel(deviceModel);
            if (adapter == null) {
                return ResponseDTO.error("DEVICE_MODEL_NOT_SUPPORTED", "不支持的设备型号: " + deviceModel);
            }

            Map<String, Object> adapterInfo = Map.of(
                "protocolType", adapter.getProtocolType(),
                "manufacturer", adapter.getManufacturer(),
                "version", adapter.getVersion(),
                "supportedDeviceModels", adapter.getSupportedDeviceModels(),
                "status", adapter.getAdapterStatus(),
                "performanceStatistics", adapter.getPerformanceStatistics()
            );

            log.info("[设备通讯] 获取适配器信息成功, deviceModel={}, protocolType={}",
                deviceModel, adapter.getProtocolType());

            return ResponseDTO.ok(adapterInfo);

        } catch (Exception e) {
            log.error("[设备通讯] 获取适配器信息异常, deviceModel={}", deviceModel, e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 检查协议类型是否支持
     *
     * @param protocolType 协议类型
     * @return 是否支持
     */
    @Operation(summary = "检查协议类型支持", description = "检查指定的协议类型是否支持")
    @GetMapping("/protocols/{protocolType}/supported")
    public ResponseDTO<Boolean> isProtocolTypeSupported(@PathVariable String protocolType) {
        try {
            boolean supported = protocolAdapterFactory.isProtocolTypeSupported(protocolType);
            log.info("[设备通讯] 检查协议类型支持, protocolType={}, supported={}", protocolType, supported);
            return ResponseDTO.ok(supported);
        } catch (Exception e) {
            log.error("[设备通讯] 检查协议类型支持异常, protocolType={}", protocolType, e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 检查设备型号是否支持
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    @Operation(summary = "检查设备型号支持", description = "检查指定的设备型号是否支持")
    @GetMapping("/device-models/{deviceModel}/supported")
    public ResponseDTO<Boolean> isDeviceModelSupported(@PathVariable String deviceModel) {
        try {
            boolean supported = protocolAdapterFactory.isDeviceModelSupported(deviceModel);
            log.info("[设备通讯] 检查设备型号支持, deviceModel={}, supported={}", deviceModel, supported);
            return ResponseDTO.ok(supported);
        } catch (Exception e) {
            log.error("[设备通讯] 检查设备型号支持异常, deviceModel={}", deviceModel, e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 获取协议适配器工厂统计信息
     *
     * @return 统计信息
     */
    @Operation(summary = "获取工厂统计信息", description = "获取协议适配器工厂的详细统计信息")
    @GetMapping("/factory/statistics")
    public ResponseDTO<Map<String, Object>> getFactoryStatistics() {
        try {
            Map<String, Object> statistics = protocolAdapterFactory.getFactoryStatistics();
            log.info("[设备通讯] 获取工厂统计信息成功");
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[设备通讯] 获取工厂统计信息异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 检查适配器健康状态
     *
     * @return 健康状态报告
     */
    @Operation(summary = "检查适配器健康状态", description = "检查所有协议适配器的健康状态")
    @GetMapping("/factory/health")
    public ResponseDTO<Map<String, Object>> checkAdapterHealth() {
        try {
            Map<String, Object> healthReport = protocolAdapterFactory.checkAdapterHealth();
            log.info("[设备通讯] 检查适配器健康状态成功");
            return ResponseDTO.ok(healthReport);
        } catch (Exception e) {
            log.error("[设备通讯] 检查适配器健康状态异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 重新加载适配器
     *
     * @return 重新加载结果
     */
    @Operation(summary = "重新加载适配器", description = "重新加载所有协议适配器")
    @PostMapping("/factory/reload")
    public ResponseDTO<String> reloadAdapters() {
        try {
            protocolAdapterFactory.reloadAdapters();
            log.info("[设备通讯] 适配器重新加载成功");
            return ResponseDTO.ok("适配器重新加载成功");
        } catch (Exception e) {
            log.error("[设备通讯] 适配器重新加载异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "重新加载失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备SN协议缓存
     *
     * @param deviceSn 设备SN
     * @param protocolType 协议类型
     * @return 更新结果
     */
    @Operation(summary = "更新设备SN缓存", description = "更新设备SN到协议类型的缓存映射")
    @PostMapping("/cache/device-sn")
    public ResponseDTO<String> updateDeviceSnProtocolCache(
            @RequestParam String deviceSn,
            @RequestParam String protocolType) {

        try {
            protocolAdapterFactory.updateDeviceSnProtocolCache(deviceSn, protocolType);
            log.info("[设备通讯] 更新设备SN协议缓存成功, deviceSn={}, protocolType={}", deviceSn, protocolType);
            return ResponseDTO.ok("设备SN缓存更新成功");
        } catch (Exception e) {
            log.error("[设备通讯] 更新设备SN缓存异常, deviceSn={}, protocolType={}", deviceSn, protocolType, e);
            return ResponseDTO.error("SYSTEM_ERROR", "缓存更新失败: " + e.getMessage());
        }
    }

    /**
     * 清理设备SN缓存
     *
     * @param deviceSn 设备SN
     * @return 清理结果
     */
    @Operation(summary = "清理设备SN缓存", description = "清理指定设备SN的协议缓存")
    @DeleteMapping("/cache/device-sn/{deviceSn}")
    public ResponseDTO<String> clearDeviceSnCache(@PathVariable String deviceSn) {
        try {
            protocolAdapterFactory.clearDeviceSnCache(deviceSn);
            log.info("[设备通讯] 清理设备SN缓存成功, deviceSn={}", deviceSn);
            return ResponseDTO.ok("设备SN缓存清理成功");
        } catch (Exception e) {
            log.error("[设备通讯] 清理设备SN缓存异常, deviceSn={}", deviceSn, e);
            return ResponseDTO.error("SYSTEM_ERROR", "缓存清理失败: " + e.getMessage());
        }
    }

    /**
     * 清理所有设备SN缓存
     *
     * @return 清理结果
     */
    @Operation(summary = "清理所有设备SN缓存", description = "清理所有设备SN的协议缓存")
    @DeleteMapping("/cache/device-sn")
    public ResponseDTO<String> clearAllDeviceSnCache() {
        try {
            protocolAdapterFactory.clearAllDeviceSnCache();
            log.info("[设备通讯] 清理所有设备SN缓存成功");
            return ResponseDTO.ok("所有设备SN缓存清理成功");
        } catch (Exception e) {
            log.error("[设备通讯] 清理所有设备SN缓存异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "缓存清理失败: " + e.getMessage());
        }
    }
}
