package net.lab1024.sa.devicecomm.controller;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micrometer.observation.annotation.Observed;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.devicecomm.cache.ProtocolCacheService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
// import io.micrometer.core.annotation.Timed; // 未使用
// import io.micrometer.core.annotation.Counted; // 未使用
// import net.lab1024.sa.devicecomm.monitor.ProtocolMetricsCollector; // 已废弃，已移除
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
@PermissionCheck(value = "DEVICE_PROTOCOL", description = "设备协议管理模块权限")
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
     * Micrometer指标注册表（用于编程式指标收集）
     */
    @Resource
    private MeterRegistry meterRegistry;

    /**
     * 协议缓存服务（使用Spring Cache）
     */
    @Resource
    private ProtocolCacheService cacheService;

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
    @Observed(name = "protocol.receivePush", contextualName = "protocol-receive-push")
    @PermissionCheck(value = "DEVICE_PROTOCOL_PUSH", description = "接收设备推送数据")
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
            joinOrThrow(future);

            log.info("[协议控制器] 设备推送处理成功，协议类型={}, 设备ID={}", protocolType, deviceId);
            return ResponseDTO.ok("消息处理成功");

        } catch (ParamException e) {
            log.warn("[协议控制器] 设备推送处理参数错误，协议类型={}, 设备ID={}: {}", protocolType, deviceId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[协议控制器] 设备推送处理业务异常，协议类型={}, 设备ID={}: {}", protocolType, deviceId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[协议控制器] 设备推送处理系统异常，协议类型={}, 设备ID={}: {}", protocolType, deviceId, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[协议控制器] 设备推送处理执行异常，协议类型={}, 设备ID={}: {}", protocolType, deviceId, e.getMessage(), e);
            return ResponseDTO.error("PROCESS_ERROR", "消息处理失败");
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
    @Observed(name = "protocol.receivePushAuto", contextualName = "protocol-receive-push-auto")
    @PermissionCheck(value = "DEVICE_PROTOCOL_PUSH_AUTO", description = "接收设备推送数据(自动识别)")
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
            joinOrThrow(future);

            log.info("[协议控制器] 设备推送处理成功，设备类型={}, 厂商={}, 设备ID={}", deviceType, manufacturer, deviceId);
            return ResponseDTO.ok("消息处理成功");

        } catch (ParamException e) {
            log.warn("[协议控制器] 设备推送处理参数错误，设备类型={}, 厂商={}, 设备ID={}: {}", deviceType, manufacturer, deviceId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[协议控制器] 设备推送处理业务异常，设备类型={}, 厂商={}, 设备ID={}: {}", deviceType, manufacturer, deviceId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[协议控制器] 设备推送处理系统异常，设备类型={}, 厂商={}, 设备ID={}: {}", deviceType, manufacturer, deviceId, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[协议控制器] 设备推送处理执行异常，设备类型={}, 厂商={}, 设备ID={}: {}", deviceType, manufacturer, deviceId, e.getMessage(), e);
            return ResponseDTO.error("PROCESS_ERROR", "消息处理失败");
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
    @Observed(name = "protocol.receivePushText", contextualName = "protocol-receive-push-text")
    @RateLimiter(name = "protocol-push", fallbackMethod = "receivePushTextFallback")
    @PermissionCheck(value = "DEVICE_PROTOCOL_PUSH_TEXT", description = "接收设备推送数据(文本格式)")
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
                    // 使用Spring Cache查询缓存（L1本地缓存 -> L2 Redis缓存 -> 数据库）
                    // @Cacheable注解会自动处理缓存逻辑
                    DeviceEntity device = cacheService.getDeviceByCode(serialNumber);
                    if (device != null) {
                        finalDeviceId = device.getId();
                        log.info("[协议控制器] 获取设备ID成功，SN={}, deviceId={}", serialNumber, finalDeviceId);
                    } else {
                        log.warn("[协议控制器] 根据SN未查询到设备，SN={}", serialNumber);
                    }
                } catch (ParamException | BusinessException | SystemException e) {
                    log.warn("[协议控制器] 根据SN查询设备ID异常，SN={}, error={}", serialNumber, e.getMessage());
                } catch (Exception e) {
                    log.warn("[协议控制器] 根据SN查询设备ID未知异常，SN={}, error={}", serialNumber, e.getMessage());
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
            joinOrThrow(future);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[协议控制器] HTTP文本推送处理成功，协议类型={}, 设备ID={}, duration={}ms",
                    finalProtocolType, finalDeviceId, duration);

            // 记录监控指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.process")
                    .tag("protocol_type", finalProtocolType != null ? finalProtocolType : "unknown")
                    .tag("status", "success")
                    .register(meterRegistry)
                    .increment();

            // HTTP协议返回"OK"
            return ResponseDTO.ok("OK");

        } catch (ParamException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[协议控制器] HTTP文本推送处理参数错误，SN={}, table={}, duration={}ms: {}", serialNumber, table, duration, e.getMessage());
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", finalProtocolType != null ? finalProtocolType : "UNKNOWN")
                    .tag("error_type", "PARAM_ERROR")
                    .register(meterRegistry)
                    .increment();
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("[协议控制器] HTTP文本推送处理业务异常，SN={}, table={}, duration={}ms: {}", serialNumber, table, duration, e.getMessage());
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", finalProtocolType != null ? finalProtocolType : "UNKNOWN")
                    .tag("error_type", "BUSINESS_ERROR")
                    .register(meterRegistry)
                    .increment();
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[协议控制器] HTTP文本推送处理系统异常，SN={}, table={}, duration={}ms: {}", serialNumber, table, duration, e.getMessage(), e);
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", finalProtocolType != null ? finalProtocolType : "UNKNOWN")
                    .tag("error_type", "SYSTEM_ERROR")
                    .register(meterRegistry)
                    .increment();
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[协议控制器] HTTP文本推送处理执行异常，SN={}, table={}, duration={}ms: {}", serialNumber, table, duration, e.getMessage(), e);

            // 记录错误指标（使用Micrometer编程式API）
            Counter.builder("protocol.message.error")
                    .tag("protocol_type", finalProtocolType != null ? finalProtocolType : "UNKNOWN")
                    .tag("error_type", "PROCESS_ERROR")
                    .register(meterRegistry)
                    .increment();

            // HTTP协议返回错误描述
            return ResponseDTO.error("PROCESS_ERROR", "消息处理失败");
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

        // 记录限流指标（使用Micrometer编程式API）
        Counter.builder("protocol.message.error")
                .tag("protocol_type", protocolType != null ? protocolType : "UNKNOWN")
                .tag("error_type", "RATE_LIMIT")
                .register(meterRegistry)
                .increment();

        // 返回限流错误（HTTP协议返回错误描述）
        return ResponseDTO.error("RATE_LIMIT", "请求过于频繁，请稍后重试");
    }

    private static <T> T joinOrThrow(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }
}

