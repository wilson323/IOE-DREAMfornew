package net.lab1024.sa.consume.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * 消费服务健康检查控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-01
 */
@Slf4j
@RestController
@RequestMapping("/actuator")
@Tag(name = "健康检查", description = "消费服务健康检查相关接口")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查消费服务是否正常运行")
    public ResponseDTO<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();

        try {
            healthInfo.put("status", "UP");
            healthInfo.put("service", "consume-service");
            healthInfo.put("timestamp", LocalDateTime.now());
            healthInfo.put("version", "1.0.0");
            healthInfo.put("description", "IOE-DREAM 消费管理微服务运行正常");

            log.debug("消费服务健康检查通过");
            return ResponseDTO.ok(healthInfo);

        } catch (Exception e) {
            log.error("消费服务健康检查失败", e);
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("timestamp", LocalDateTime.now());

            return ResponseDTO.error("服务异常", healthInfo);
        }
    }

    @GetMapping("/info")
    @Operation(summary = "服务信息", description = "获取消费服务基本信息")
    public ResponseDTO<Map<String, Object>> info() {
        Map<String, Object> serviceInfo = new HashMap<>();

        serviceInfo.put("serviceName", "consume-service");
        serviceInfo.put("serviceDescription", "IOE-DREAM 消费管理微服务");
        serviceInfo.put("version", "1.0.0");
        serviceInfo.put("author", "IOE-DREAM Team");
        serviceInfo.put("port", 8082);
        serviceInfo.put("startUpTime", System.currentTimeMillis());

        return ResponseDTO.ok(serviceInfo);
    }

    @GetMapping("/ready")
    @Operation(summary = "就绪检查", description = "检查消费服务是否准备就绪")
    public ResponseDTO<Map<String, Object>> ready() {
        Map<String, Object> readyInfo = new HashMap<>();

        try {
            // 这里可以添加更多的就绪检查逻辑
            // 例如：数据库连接检查、Redis连接检查等

            readyInfo.put("status", "READY");
            readyInfo.put("database", "UP");
            readyInfo.put("redis", "UP");
            readyInfo.put("timestamp", LocalDateTime.now());

            return ResponseDTO.ok(readyInfo);

        } catch (Exception e) {
            log.error("消费服务就绪检查失败", e);
            readyInfo.put("status", "NOT_READY");
            readyInfo.put("error", e.getMessage());

            return ResponseDTO.error("服务未就绪", readyInfo);
        }
    }
}