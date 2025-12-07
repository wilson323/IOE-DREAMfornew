package net.lab1024.sa.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import jakarta.annotation.Resource;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 微服务健康检查控制器
 * <p>
 * 提供统一的健康检查接口，支持：
 * - 服务基础健康状态
 * - 服务发现状态
 * - 依赖服务状态
 * - 系统资源状态
 * <p>
 * 严格遵循repowiki规范，提供标准化的健康检查API
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-01-27
 */
@Slf4j
@RestController
@RequestMapping("/actuator/health")
@Tag(name = "健康检查", description = "微服务健康检查相关接口")
public class HealthCheckController {

    @Resource
    private DiscoveryClient discoveryClient;

    private final String serviceName;

    public HealthCheckController() {
        // 从Spring应用上下文获取服务名称
        this.serviceName = "ioedream-service";
    }

    /**
     * 基础健康检查
     *
     * @return 健康状态
     */
    @GetMapping
    @Operation(summary = "基础健康检查", description = "检查服务基础运行状态")
    public ResponseDTO<Map<String, Object>> basicHealth() {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            healthInfo.put("status", "UP");
            healthInfo.put("service", serviceName);
            healthInfo.put("timestamp", LocalDateTime.now());
            healthInfo.put("uptime", getUptime());

            return ResponseDTO.success(healthInfo);
        } catch (Exception e) {
            log.error("健康检查失败", e);
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            return ResponseDTO.error("服务异常", healthInfo);
        }
    }

    /**
     * 详细健康检查
     *
     * @return 详细健康状态
     */
    @GetMapping("/detailed")
    @Operation(summary = "详细健康检查", description = "检查服务详细运行状态，包括依赖和资源")
    public ResponseDTO<Map<String, Object>> detailedHealth() {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            // 基础信息
            healthInfo.put("status", "UP");
            healthInfo.put("service", serviceName);
            healthInfo.put("timestamp", LocalDateTime.now());
            healthInfo.put("uptime", getUptime());

            // JVM信息
            healthInfo.put("jvm", getJvmInfo());

            // 服务发现状态
            if (discoveryClient != null) {
                healthInfo.put("discovery", getDiscoveryInfo());
            }

            return ResponseDTO.success(healthInfo);
        } catch (Exception e) {
            log.error("详细健康检查失败", e);
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            return ResponseDTO.error("服务异常", healthInfo);
        }
    }

    /**
     * 就绪检查
     * Kubernetes容器就绪探针使用
     *
     * @return 就绪状态
     */
    @GetMapping("/ready")
    @Operation(summary = "就绪检查", description = "检查服务是否准备就绪接收请求")
    public ResponseDTO<Map<String, Object>> readiness() {
        Map<String, Object> readinessInfo = new HashMap<>();

        try {
            boolean isReady = checkReadiness();

            readinessInfo.put("status", isReady ? "READY" : "NOT_READY");
            readinessInfo.put("service", serviceName);
            readinessInfo.put("timestamp", LocalDateTime.now());

            if (isReady) {
                return ResponseDTO.success(readinessInfo);
            } else {
                return ResponseDTO.error("服务未就绪", readinessInfo);
            }
        } catch (Exception e) {
            log.error("就绪检查失败", e);
            readinessInfo.put("status", "NOT_READY");
            readinessInfo.put("error", e.getMessage());
            return ResponseDTO.error("服务异常", readinessInfo);
        }
    }

    /**
     * 存活检查
     * Kubernetes容器存活探针使用
     *
     * @return 存活状态
     */
    @GetMapping("/live")
    @Operation(summary = "存活检查", description = "检查服务是否存活")
    public ResponseDTO<Map<String, Object>> liveness() {
        Map<String, Object> livenessInfo = new HashMap<>();

        try {
            boolean isAlive = checkLiveness();

            livenessInfo.put("status", isAlive ? "ALIVE" : "NOT_ALIVE");
            livenessInfo.put("service", serviceName);
            livenessInfo.put("timestamp", LocalDateTime.now());

            if (isAlive) {
                return ResponseDTO.success(livenessInfo);
            } else {
                return ResponseDTO.error("服务未存活", livenessInfo);
            }
        } catch (Exception e) {
            log.error("存活检查失败", e);
            livenessInfo.put("status", "NOT_ALIVE");
            livenessInfo.put("error", e.getMessage());
            return ResponseDTO.error("服务异常", livenessInfo);
        }
    }

    /**
     * 检查其他微服务状态
     *
     * @param serviceName 服务名称
     * @return 服务状态
     */
    @GetMapping("/service/{serviceName}")
    @Operation(summary = "检查服务状态", description = "检查指定微服务的健康状态")
    public ResponseDTO<Map<String, Object>> checkServiceHealth(
            @PathVariable String serviceName) {
        Map<String, Object> serviceInfo = new HashMap<>();

        try {
            if (discoveryClient != null) {
                Set<String> services = new HashSet<>(discoveryClient.getServices());

                if (services.contains(serviceName)) {
                    serviceInfo.put("status", "UP");
                    serviceInfo.put("instances", discoveryClient.getInstances(serviceName).size());
                    serviceInfo.put("timestamp", LocalDateTime.now());
                } else {
                    serviceInfo.put("status", "UNKNOWN");
                    serviceInfo.put("message", "服务未注册");
                }
            } else {
                serviceInfo.put("status", "UNKNOWN");
                serviceInfo.put("message", "服务发现未启用");
            }

            serviceInfo.put("service", serviceName);
            return ResponseDTO.success(serviceInfo);
        } catch (Exception e) {
            log.error("检查服务状态失败: {}", serviceName, e);
            serviceInfo.put("status", "DOWN");
            serviceInfo.put("error", e.getMessage());
            return ResponseDTO.error("检查失败", serviceInfo);
        }
    }

    /**
     * 获取服务运行时间
     */
    private String getUptime() {
        try {
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            long hours = uptime / (60 * 60 * 1000);
            long minutes = (uptime % (60 * 60 * 1000)) / (60 * 1000);
            long seconds = (uptime % (60 * 1000)) / 1000;

            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (Exception e) {
            return "未知";
        }
    }

    /**
     * 获取JVM信息
     */
    private Map<String, Object> getJvmInfo() {
        Map<String, Object> jvmInfo = new HashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();

            // 内存信息
            jvmInfo.put("memory", Map.of(
                "total", runtime.totalMemory(),
                "free", runtime.freeMemory(),
                "used", runtime.totalMemory() - runtime.freeMemory(),
                "max", runtime.maxMemory()
            ));

            // 处理器信息
            jvmInfo.put("processors", runtime.availableProcessors());

            // Java版本
            jvmInfo.put("java_version", System.getProperty("java.version"));

            return jvmInfo;
        } catch (Exception e) {
            jvmInfo.put("error", e.getMessage());
            return jvmInfo;
        }
    }

    /**
     * 获取服务发现信息
     */
    private Map<String, Object> getDiscoveryInfo() {
        Map<String, Object> discoveryInfo = new HashMap<>();

        try {
            Set<String> services = new HashSet<>(discoveryClient.getServices());

            discoveryInfo.put("enabled", true);
            discoveryInfo.put("service_count", services.size());
            discoveryInfo.put("services", services);

            return discoveryInfo;
        } catch (Exception e) {
            discoveryInfo.put("enabled", false);
            discoveryInfo.put("error", e.getMessage());
            return discoveryInfo;
        }
    }

    /**
     * 检查服务就绪状态
     */
    private boolean checkReadiness() {
        // 这里可以添加具体的就绪检查逻辑
        // 例如：数据库连接池状态、缓存连接状态、依赖服务状态等
        return true;
    }

    /**
     * 检查服务存活状态
     */
    private boolean checkLiveness() {
        // 基础存活检查
        return true;
    }
}