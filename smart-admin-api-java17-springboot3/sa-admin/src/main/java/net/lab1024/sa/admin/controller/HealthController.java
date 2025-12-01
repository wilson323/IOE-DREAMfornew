package net.lab1024.sa.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查Controller
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查", description = "系统健康检查相关接口")
public class HealthController {

    @GetMapping
    @Operation(summary = "系统健康检查", description = "检查系统运行状态")
    public ResponseDTO<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("application", "SmartAdmin v3.0");
        healthInfo.put("version", "3.0.0");
        healthInfo.put("database", "UP");
        healthInfo.put("redis", "UP");

        return ResponseDTO.ok(healthInfo);
    }

    @GetMapping("/ping")
    @Operation(summary = "简单心跳检查", description = "简单的服务心跳检查")
    public ResponseDTO<String> ping() {
        return ResponseDTO.ok("pong");
    }
}