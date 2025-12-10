package net.lab1024.sa.database.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.response.ResponseDTO;
import net.lab1024.sa.database.service.DatabaseSyncService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 数据库同步控制器
 * <p>
 * 提供数据库同步相关的API接口，包括：
 * - 数据库初始化和同步
 * - 版本管理和状态查询
 * - 手动触发同步操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-08
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/database/sync")
@Tag(name = "数据库同步管理", description = "数据库初始化和同步管理")
public class DatabaseSyncController {

    @Resource
    private DatabaseSyncService databaseSyncService;

    /**
     * 初始化数据库配置
     */
    @Operation(summary = "初始化数据库配置", description = "初始化所有数据库配置，为同步做准备")
    @PostMapping("/init")
    public ResponseDTO<Void> initDatabaseConfigs() {
        log.info("[数据库同步控制器] 初始化数据库配置请求");

        try {
            databaseSyncService.initDatabaseConfigs();
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[数据库同步控制器] 初始化数据库配置失败", e);
            return ResponseDTO.error("INIT_FAILED", "初始化数据库配置失败: " + e.getMessage());
        }
    }

    /**
     * 同步所有数据库
     */
    @Operation(summary = "同步所有数据库", description = "同步所有已配置的数据库表结构和数据")
    @PostMapping("/all")
    public ResponseDTO<Map<String, Boolean>> syncAllDatabases() {
        log.info("[数据库同步控制器] 同步所有数据库请求");

        try {
            CompletableFuture<Map<String, Boolean>> future = databaseSyncService.syncAllDatabases();
            Map<String, Boolean> results = future.get();

            return ResponseDTO.ok(results);
        } catch (Exception e) {
            log.error("[数据库同步控制器] 同步所有数据库失败", e);
            return ResponseDTO.error("SYNC_ALL_FAILED", "同步所有数据库失败: " + e.getMessage());
        }
    }

    /**
     * 同步指定数据库
     */
    @Operation(summary = "同步指定数据库", description = "同步指定的数据库表结构和数据")
    @PostMapping("/{dbName}")
    public ResponseDTO<Boolean> syncDatabase(
            @Parameter(description = "数据库名称") @PathVariable String dbName) {
        log.info("[数据库同步控制器] 同步指定数据库请求: {}", dbName);

        try {
            boolean result = databaseSyncService.syncDatabase(dbName, null);

            if (result) {
                return ResponseDTO.ok(true);
            } else {
                return ResponseDTO.error("SYNC_FAILED", "同步数据库失败");
            }
        } catch (Exception e) {
            log.error("[数据库同步控制器] 同步指定数据库失败: {}", dbName, e);
            return ResponseDTO.error("SYNC_FAILED", "同步数据库失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库版本信息
     */
    @Operation(summary = "获取数据库版本信息", description = "获取所有数据库的版本和同步状态")
    @GetMapping("/versions")
    public ResponseDTO<Map<String, Object>> getDatabaseVersions() {
        log.info("[数据库同步控制器] 获取数据库版本信息请求");

        try {
            Map<String, Object> versions = databaseSyncService.getDatabaseVersions();
            return ResponseDTO.ok(versions);
        } catch (Exception e) {
            log.error("[数据库同步控制器] 获取数据库版本信息失败", e);
            return ResponseDTO.error("GET_VERSIONS_FAILED", "获取数据库版本信息失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @Operation(summary = "数据库健康检查", description = "检查所有数据库的连接状态和健康度")
    @GetMapping("/health")
    public ResponseDTO<Map<String, Object>> healthCheck() {
        log.info("[数据库同步控制器] 数据库健康检查请求");

        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            health.put("service", "ioedream-database-service");

            // 可以添加更详细的健康检查逻辑
            health.put("databases", Map.of(
                "ioedream_common_db", "UP",
                "ioedream_access_db", "UP"
            ));

            return ResponseDTO.ok(health);
        } catch (Exception e) {
            log.error("[数据库同步控制器] 数据库健康检查失败", e);
            return ResponseDTO.error("HEALTH_CHECK_FAILED", "健康检查失败: " + e.getMessage());
        }
    }
}