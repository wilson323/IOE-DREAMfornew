package net.lab1024.sa.devicecomm.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.HttpMethod;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.devicecomm.cache.ProtocolCacheManager;
import net.lab1024.sa.devicecomm.monitor.ProtocolMetricsCollector;
import net.lab1024.sa.devicecomm.protocol.router.MessageRouter;

/**
 * 协议控制器
 * <p>
 * 提供HTTP接口接收设备推送数据
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource注入依赖
 * - 统一使用ResponseDTO封装响应
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/device/protocol")
public class ProtocolController {

    /**
     * 消息路由器
     */
    @Resource
    private MessageRouter messageRouter;

    /**
     * 网关服务客户端（用于调用其他微服务）
     */
    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 协议监控指标收集器
     */
    @Resource
    private ProtocolMetricsCollector metricsCollector;

    /**
     * 协议缓存管理器（多级缓存）
     */
    @Resource
    private ProtocolCacheManager cacheManager;

    /**
     * 接收设备推送数据（字节数组）
     * <p>
     * 接收设备通过HTTP POST推送的协议数据
     * </p>
     *
     * @param protocolType 协议类型代码（如：ATTENDANCE_ENTROPY_V4.0）
     * @param deviceId 设备ID
     * @param rawData 原始数据（Base64编码的字节数组）
     * @return 响应结果
     */
    @PostMapping("/push")
    public ResponseDTO<String> receivePush(
            @RequestParam("protocolType") String protocolType,
            @RequestParam("deviceId") Long deviceId,
            @RequestBody byte[] rawData) {

        log.info("[协议控制器] 接收到设备推送，协议类型={}, 设备ID={}, 数据长度={}",
                protocolType, deviceId, rawData != null ? rawData.length : 0);

        try {
            // 异步路由消息
            CompletableFuture<net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage> future =
                    messageRouter.route(protocolType, rawData, deviceId);

            // 等待处理完成（实际应该异步返回，这里简化处理）
            future.join();

            log.info("[协议控制器] 设备推送处理成功，协议类型={}, 设备ID={}", protocolType, deviceId);
            return ResponseDTO.ok("消息处理成功");

        } catch (Exception e) {
            log.error("[协议控制器] 设备推送处理失败，协议类型={}, 设备ID={}, 错误={}",
                    protocolType, deviceId, e.getMessage(), e);
            return ResponseDTO.error("PROCESS_ERROR", "消息处理失败：" + e.getMessage());
        }
    }

    /**
     * 接收设备推送数据（根据设备类型和厂商）
     * <p>
     * 根据设备类型和厂商自动识别协议类型
     * </p>
     *
     * @param deviceType 设备类型（ATTENDANCE、ACCESS、CONSUME）
     * @param manufacturer 厂商名称（熵基科技、中控智慧）
     * @param deviceId 设备ID
     * @param rawData 原始数据（字节数组）
     * @return 响应结果
     */
    @PostMapping("/push/auto")
    public ResponseDTO<String> receivePushAuto(
            @RequestParam("deviceType") String deviceType,
            @RequestParam("manufacturer") String manufacturer,
            @RequestParam("deviceId") Long deviceId,
            @RequestBody byte[] rawData) {

        log.info("[协议控制器] 接收到设备推送（自动识别），设备类型={}, 厂商={}, 设备ID={}, 数据长度={}",
                deviceType, manufacturer, deviceId, rawData != null ? rawData.length : 0);

        try {
            // 异步路由消息
            CompletableFuture<net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage> future =
                    messageRouter.route(deviceType, manufacturer, rawData, deviceId);

            // 等待处理完成
            future.join();

            log.info("[协议控制器] 设备推送处理成功，设备类型={}, 厂商={}, 设备ID={}", deviceType, manufacturer, deviceId);
            return ResponseDTO.ok("消息处理成功");

        } catch (Exception e) {
            log.error("[协议控制器] 设备推送处理失败，设备类型={}, 厂商={}, 设备ID={}, 错误={}",
                    deviceType, manufacturer, deviceId, e.getMessage(), e);
            return ResponseDTO.error("PROCESS_ERROR", "消息处理失败：" + e.getMessage());
        }
    }

    /**
     * 接收设备推送数据（HTTP文本格式）
     * <p>
     * 接收设备通过HTTP POST推送的文本格式协议数据
     * 支持门禁、考勤、消费协议的HTTP文本格式
     * </p>
     * <p>
     * 协议格式：
     * - 门禁协议：键值对格式（key=value，制表符分隔）
     * - 考勤协议：制表符分隔的文本格式
     * - 消费协议：制表符分隔的文本格式
     * </p>
     * <p>
     * 企业级高可用特性：
     * - 限流防刷：使用Resilience4j RateLimiter，限制100请求/秒
     * - 监控指标：记录请求处理量和延迟
     * </p>
     *
     * @param serialNumber 设备序列号（从HTTP请求参数中提取）
     * @param table 数据表名（rtlog/ATTLOG/BUYLOG等）
     * @param protocolType 协议类型代码（可选，如不提供则根据table自动识别）
     * @param deviceId 设备ID（可选，如不提供则根据serialNumber查找）
     * @param rawData 原始数据（文本格式）
     * @return 响应结果（HTTP协议返回"OK"）
     */
    @PostMapping(value = "/push/text", consumes = {"text/plain", "text/html;charset=utf-8", "application/x-www-form-urlencoded;charset=UTF-8", "application/x-www-form-urlencoded;charset=GB18030"})
    @RateLimiter(name = "protocol-push", fallbackMethod = "receivePushTextFallback")
    public ResponseDTO<String> receivePushText(
            @RequestParam(value = "SN", required = false) String serialNumber,
            @RequestParam(value = "table", required = false) String table,
            @RequestParam(value = "protocolType", required = false) String protocolType,
            @RequestParam(value = "deviceId", required = false) Long deviceId,
            @RequestBody(required = false) String rawData) {

        long startTime = System.currentTimeMillis();
        String finalProtocolType = protocolType; // 声明在try块外，以便在catch块中使用

        log.info("[协议控制器] 接收到HTTP文本推送，SN={}, table={}, protocolType={}, deviceId={}, 数据长度={}",
                serialNumber, table, protocolType, deviceId, rawData != null ? rawData.length() : 0);

        try {
            // 如果没有提供rawData，尝试从请求参数中获取
            if (rawData == null || rawData.isEmpty()) {
                // 可以从请求参数中构建数据（如果需要）
                log.warn("[协议控制器] 请求体为空，尝试从参数中获取数据");
            }

            // 根据table参数自动识别协议类型
            finalProtocolType = protocolType;
            if (finalProtocolType == null || finalProtocolType.isEmpty()) {
                if (table != null) {
                    switch (table.toUpperCase()) {
                        case "RTLOG":
                        case "RTSTATE":
                            finalProtocolType = "ACCESS_ENTROPY_V4.8";
                            break;
                        case "ATTLOG":
                        case "ATTPHOTO":
                            finalProtocolType = "ATTENDANCE_ENTROPY_V4.0";
                            break;
                        case "BUYLOG":
                        case "FULLLOG":
                        case "ALLOWLOG":
                            finalProtocolType = "CONSUME_ZKTECO_V1.0";
                            break;
                        default:
                            log.warn("[协议控制器] 无法根据table识别协议类型，table={}", table);
                    }
                }
            }

            if (finalProtocolType == null || finalProtocolType.isEmpty()) {
                return ResponseDTO.error("PROTOCOL_TYPE_REQUIRED", "无法识别协议类型，请提供protocolType或table参数");
            }

            // 如果没有提供deviceId，尝试根据SN查找（使用多级缓存）
            Long finalDeviceId = deviceId;
            if (finalDeviceId == null && serialNumber != null && !serialNumber.isEmpty()) {
                try {
                    // 1. 先查询缓存（L1本地缓存 -> L2 Redis缓存）
                    DeviceEntity cachedDevice = cacheManager.getDeviceByCode(serialNumber);
                    if (cachedDevice != null) {
                        finalDeviceId = cachedDevice.getId();
                        log.info("[协议控制器] 从缓存获取设备ID，SN={}, deviceId={}", serialNumber, finalDeviceId);
                    } else {
                        // 2. 缓存未命中，通过网关调用公共服务查询
                        ResponseDTO<DeviceEntity> deviceResponse = gatewayServiceClient.callCommonService(
                                "/api/v1/device/code/" + serialNumber,
                                HttpMethod.GET,
                                null,
                                DeviceEntity.class
                        );

                        if (deviceResponse != null && deviceResponse.isSuccess() && deviceResponse.getData() != null) {
                            DeviceEntity device = deviceResponse.getData();
                            finalDeviceId = device.getId();

                            // 3. 缓存设备信息（多级缓存）
                            cacheManager.cacheDevice(device);

                            log.info("[协议控制器] 根据SN查询到设备ID，SN={}, deviceId={}", serialNumber, finalDeviceId);
                        } else {
                            log.warn("[协议控制器] 根据SN未查询到设备，SN={}, message={}",
                                    serialNumber, deviceResponse != null ? deviceResponse.getMessage() : "响应为空");
                        }
                    }
                } catch (Exception e) {
                    log.warn("[协议控制器] 根据SN查询设备ID异常，SN={}, error={}", serialNumber, e.getMessage());
                }
            }

            // 如果仍然没有deviceId，记录警告并使用默认值（兼容性处理）
            if (finalDeviceId == null) {
                log.warn("[协议控制器] 未提供deviceId且无法根据SN查询，使用默认值1，SN={}", serialNumber);
                finalDeviceId = 1L;
            }

            // 异步路由文本消息
            CompletableFuture<net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage> future =
                    messageRouter.route(finalProtocolType, rawData, finalDeviceId);

            // 等待处理完成
            future.join();

            long duration = System.currentTimeMillis() - startTime;
            log.info("[协议控制器] HTTP文本推送处理成功，协议类型={}, 设备ID={}, duration={}ms",
                    finalProtocolType, finalDeviceId, duration);

            // 记录监控指标
            metricsCollector.recordSuccess(finalProtocolType, duration);

            // HTTP协议返回"OK"
            return ResponseDTO.ok("OK");

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[协议控制器] HTTP文本推送处理失败，SN={}, table={}, 错误={}, duration={}ms",
                    serialNumber, table, e.getMessage(), duration, e);

            // 记录错误指标
            metricsCollector.recordError(finalProtocolType != null ? finalProtocolType : "UNKNOWN", "PROCESS_ERROR");

            // HTTP协议返回错误描述
            return ResponseDTO.error("PROCESS_ERROR", "消息处理失败：" + e.getMessage());
        }
    }

    /**
     * 限流降级方法
     * <p>
     * 当请求超过限流阈值时，调用此降级方法
     * </p>
     *
     * @param serialNumber 设备序列号
     * @param table 数据表名
     * @param protocolType 协议类型
     * @param deviceId 设备ID
     * @param rawData 原始数据
     * @param exception 限流异常
     * @return 降级响应
     */
    public ResponseDTO<String> receivePushTextFallback(
            @RequestParam(value = "SN", required = false) String serialNumber,
            @RequestParam(value = "table", required = false) String table,
            @RequestParam(value = "protocolType", required = false) String protocolType,
            @RequestParam(value = "deviceId", required = false) Long deviceId,
            @RequestBody(required = false) String rawData,
            Exception exception) {

        log.warn("[协议控制器] 请求被限流，SN={}, table={}, 错误={}",
                serialNumber, table, exception != null ? exception.getMessage() : "限流触发");

        // 记录限流指标
        metricsCollector.recordError(protocolType != null ? protocolType : "UNKNOWN", "RATE_LIMIT");

        // 返回限流错误（HTTP协议返回错误描述）
        return ResponseDTO.error("RATE_LIMIT", "请求过于频繁，请稍后重试");
    }
}

