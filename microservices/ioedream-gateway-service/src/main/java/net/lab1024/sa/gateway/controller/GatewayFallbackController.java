package net.lab1024.sa.gateway.controller;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 使用Spring Cloud的CircuitBreaker而非Resilience4j
import io.micrometer.observation.annotation.Observed;
/**
 * 网关服务降级控制器
 *
 * @author IOE-DREAM Team
 * @date 2025-12-09
 * @description 提供统一的服务降级处理，当微服务不可用时返回友好响应
 */
@RestController
@RequestMapping("/api/v1/fallback")
@Slf4j
public class GatewayFallbackController {

    // CircuitBreakerFactory需要额外配置，暂时禁用
    // @Resource
    // private CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    // ============================================================
    // 服务降级处理方法
    // ============================================================

    /**
     * 公共服务降级处理
     */
    @Observed(name = "fallback.common", contextualName = "fallback-common")
    @GetMapping("/common")
    public ResponseEntity<Map<String, Object>> commonServiceFallback () {
        log.warn ("[网关降级] 公共服务不可用，触发降级处理");

        return createFallbackResponse ("COMMON_SERVICE_UNAVAILABLE", "公共服务暂时不可用，请稍后重试", "建议稍后重试或联系管理员");
    }

    /**
     * 设备通讯服务降级处理
     */
    @Observed(name = "fallback.device", contextualName = "fallback-device")
    @GetMapping("/device")
    public ResponseEntity<Map<String, Object>> deviceServiceFallback () {
        log.warn ("[网关降级] 设备通讯服务不可用，触发降级处理");

        // 设备服务降级可以返回设备离线状态
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put ("deviceStatus", "OFFLINE");
        fallbackData.put ("message", "设备通讯暂时不可用");
        fallbackData.put ("timestamp", LocalDateTime.now ());

        return createFallbackResponse ("DEVICE_SERVICE_UNAVAILABLE", "设备通讯服务暂时不可用", "设备可能离线或网络异常，请检查设备状态",
                fallbackData);
    }

    /**
     * OA服务降级处理
     */
    @Observed(name = "fallback.oa", contextualName = "fallback-oa")
    @GetMapping("/oa")
    public ResponseEntity<Map<String, Object>> oaServiceFallback () {
        log.warn ("[网关降级] OA服务不可用，触发降级处理");

        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put ("systemStatus", "MAINTENANCE");
        fallbackData.put ("availableFunctions", new String[] { "基础查询", "离线文档" });

        return createFallbackResponse ("OA_SERVICE_UNAVAILABLE", "OA服务暂时不可用", "系统正在维护中，部分功能受限", fallbackData);
    }

    /**
     * 门禁服务降级处理 - 关键服务，提供基础功能
     */
    @Observed(name = "fallback.access", contextualName = "fallback-access")
    @GetMapping("/access")
    public ResponseEntity<Map<String, Object>> accessServiceFallback () {
        log.warn ("[网关降级] 门禁服务不可用，触发紧急降级处理");

        // 门禁是关键服务，需要提供基础降级方案
        Map<String, Object> emergencyData = new HashMap<>();
        emergencyData.put ("accessMode", "EMERGENCY");
        emergencyData.put ("fallbackMode", "LOCAL_VERIFICATION");
        emergencyData.put ("message", "系统使用本地验证模式");
        emergencyData.put ("contactAdmin", true);

        return createFallbackResponse ("ACCESS_SERVICE_DEGRADED", "门禁服务降级运行", "系统已切换到本地验证模式，请联络管理员", emergencyData);
    }

    /**
     * 考勤服务降级处理
     */
    @Observed(name = "fallback.attendance", contextualName = "fallback-attendance")
    @GetMapping("/attendance")
    public ResponseEntity<Map<String, Object>> attendanceServiceFallback () {
        log.warn ("[网关降级] 考勤服务不可用，触发降级处理");

        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put ("attendanceMode", "MANUAL");
        fallbackData.put ("message", "请手动记录考勤");
        fallbackData.put ("manualRecordUrl", "/attendance/manual-record");

        return createFallbackResponse ("ATTENDANCE_SERVICE_UNAVAILABLE", "考勤服务暂时不可用", "请使用手动考勤记录或稍后重试", fallbackData);
    }

    /**
     * 消费服务降级处理 - 关键服务，提供基础功能
     */
    @Observed(name = "fallback.consume", contextualName = "fallback-consume")
    @GetMapping("/consume")
    public ResponseEntity<Map<String, Object>> consumeServiceFallback () {
        log.warn ("[网关降级] 消费服务不可用，触发紧急降级处理");

        // 消费服务是关键服务，提供离线模式
        Map<String, Object> emergencyData = new HashMap<>();
        emergencyData.put ("consumeMode", "OFFLINE");
        emergencyData.put ("maxAmount", 50.0); // 离线模式限额
        emergencyData.put ("message", "系统已切换到离线消费模式");
        emergencyData.put ("syncPending", true);

        return createFallbackResponse ("CONSUME_SERVICE_DEGRADED", "消费服务降级运行", "系统已切换到离线模式，消费记录将在网络恢复后同步",
                emergencyData);
    }

    /**
     * 访客服务降级处理
     */
    @Observed(name = "fallback.visitor", contextualName = "fallback-visitor")
    @GetMapping("/visitor")
    public ResponseEntity<Map<String, Object>> visitorServiceFallback () {
        log.warn ("[网关降级] 访客服务不可用，触发降级处理");

        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put ("visitorMode", "MANUAL_REGISTRATION");
        fallbackData.put ("message", "请使用手动访客登记");
        fallbackData.put ("contactPhone", "前台电话: 010-12345678");

        return createFallbackResponse ("VISITOR_SERVICE_UNAVAILABLE", "访客服务暂时不可用", "请在前台进行手动访客登记", fallbackData);
    }

    /**
     * 视频服务降级处理
     */
    @Observed(name = "fallback.video", contextualName = "fallback-video")
    @GetMapping("/video")
    public ResponseEntity<Map<String, Object>> videoServiceFallback () {
        log.warn ("[网关降级] 视频服务不可用，触发降级处理");

        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put ("videoMode", "RECORDING_ONLY");
        fallbackData.put ("message", "视频监控仅支持录像回放");
        fallbackData.put ("liveViewAvailable", false);

        return createFallbackResponse ("VIDEO_SERVICE_UNAVAILABLE", "视频服务暂时不可用", "实时监控暂时不可用，仅支持录像回放", fallbackData);
    }

    /**
     * 通用服务降级处理
     */
    @Observed(name = "fallback.general", contextualName = "fallback-general")
    @GetMapping("/general/{serviceName}")
    public ResponseEntity<Map<String, Object>> generalServiceFallback (@PathVariable String serviceName) {
        log.warn ("[网关降级] 服务不可用: {}", serviceName);

        return createFallbackResponse ("SERVICE_UNAVAILABLE", serviceName + "服务暂时不可用", "请稍后重试或联系系统管理员");
    }

    // ============================================================
    // 熔断器状态查询
    // ============================================================

    /**
     * 查询所有熔断器状态
     */
    @Observed(name = "fallback.getCircuitBreakerStatus", contextualName = "fallback-circuit-breaker-status")
    @GetMapping("/circuitbreakers")
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus () {
        Map<String, Object> status = new HashMap<>();

        // 获取网关熔断器状态 - 使用Spring Cloud CircuitBreaker
        status.put ("gateway", Map.of ("name", "gateway-circuitbreaker", "available", true));

        // 这里可以添加更多熔断器状态查询
        // 实际项目中可以通过注入CircuitBreakerRegistry获取所有熔断器

        Map<String, Object> response = new HashMap<>();
        response.put ("success", true);
        response.put ("data", status);
        response.put ("timestamp", LocalDateTime.now ());
        return ResponseEntity.ok (response);
    }

    /**
     * 查询指定服务的健康状态
     */
    @Observed(name = "fallback.getServiceHealth", contextualName = "fallback-service-health")
    @GetMapping("/health/{serviceName}")
    public ResponseEntity<Map<String, Object>> getServiceHealth (@PathVariable String serviceName) {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put ("serviceName", serviceName);
        healthInfo.put ("timestamp", LocalDateTime.now ());

        // 模拟健康检查逻辑
        // 实际项目中应该调用实际的健康检查接口
        boolean isHealthy = checkServiceHealth (serviceName);
        healthInfo.put ("status", isHealthy ? "UP" : "DOWN");
        healthInfo.put ("lastCheckTime", LocalDateTime.now ());

        if (!isHealthy) {
            healthInfo.put ("reason", "服务响应超时或返回错误");
            healthInfo.put ("suggestion", "请检查服务状态或联系管理员");
        }

        Map<String, Object> response = new HashMap<>();
        response.put ("success", true);
        response.put ("data", healthInfo);
        response.put ("timestamp", LocalDateTime.now ());
        return ResponseEntity.ok (response);
    }

    // ============================================================
    // 系统降级信息
    // ============================================================

    /**
     * 获取系统降级总览
     */
    @Observed(name = "fallback.getDegradationOverview", contextualName = "fallback-degradation-overview")
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDegradationOverview () {
        Map<String, Object> overview = new HashMap<>();

        // 系统状态
        overview.put ("systemStatus", "DEGRADED");
        overview.put ("timestamp", LocalDateTime.now ());

        // 受影响的服务
        Map<String, String> affectedServices = new HashMap<>();
        affectedServices.put ("device", "设备通讯服务 - 离线模式");
        affectedServices.put ("video", "视频服务 - 录像回放模式");

        overview.put ("affectedServices", affectedServices);

        // 可用功能
        Map<String, Boolean> availableFunctions = new HashMap<>();
        availableFunctions.put ("access", true); // 门禁基础功能可用
        availableFunctions.put ("consume", true); // 消费离线模式可用
        availableFunctions.put ("attendance", false);
        availableFunctions.put ("visitor", false);

        overview.put ("availableFunctions", availableFunctions);

        // 预计恢复时间
        overview.put ("estimatedRecovery", "15分钟内");

        // 联系信息
        overview.put ("contactInfo",
                Map.of ("admin", "admin@ioedream.com", "hotline", "400-123-4567", "emergency", "138-0000-0000"));

        Map<String, Object> response = new HashMap<>();
        response.put ("success", true);
        response.put ("data", overview);
        response.put ("timestamp", LocalDateTime.now ());
        return ResponseEntity.ok (response);
    }

    // ============================================================
    // 私有辅助方法
    // ============================================================

    /**
     * 创建标准降级响应
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse (String errorCode, String message,
            String suggestion) {
        return createFallbackResponse (errorCode, message, suggestion, null);
    }

    /**
     * 创建带数据的降级响应
     */
    private ResponseEntity<Map<String, Object>> createFallbackResponse (String errorCode, String message,
            String suggestion, Object data) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put ("errorCode", errorCode);
        responseMap.put ("message", message);
        responseMap.put ("suggestion", suggestion);
        responseMap.put ("timestamp", LocalDateTime.now ());
        responseMap.put ("fallbackMode", true);

        if (data != null) {
            responseMap.put ("data", data);
        }

        // 根据错误类型决定HTTP状态码
        HttpStatus httpStatus = determineHttpStatus (errorCode);
        // httpStatus由determineHttpStatus保证非空，使用Objects.requireNonNull确保null安全
        org.springframework.http.HttpStatusCode statusCode = Objects.requireNonNull (httpStatus, "httpStatus不能为null");

        return ResponseEntity.status (statusCode).body (responseMap);
    }

    /**
     * 根据错误码确定HTTP状态码
     */
    private HttpStatus determineHttpStatus (String errorCode) {
        if (errorCode.contains ("DEGRADED")) {
            return HttpStatus.OK; // 降级模式返回200，但包含降级信息
        } else if (errorCode.contains ("UNAVAILABLE")) {
            return HttpStatus.SERVICE_UNAVAILABLE; // 503
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }
    }

    /**
     * 检查服务健康状态（模拟实现）
     */
    private boolean checkServiceHealth (String serviceName) {
        // 实际项目中应该调用实际的健康检查接口
        // 这里只是模拟实现

        // 根据服务名称模拟不同的健康状态
        switch (serviceName.toLowerCase ()) {
        case "access" :
        case "consume" :
            return true; // 关键服务认为健康（可能有降级）
        case "device" :
        case "video" :
            return false; // 非关键服务认为不健康
        default :
            return Math.random () > 0.5; // 其他服务随机
        }
    }
}
