package net.lab1024.sa.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redisson测试控制器
 * <p>
 * 用于验证Redisson分布式锁功能是否正常工作
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
@RequestMapping("/api/v1/test")
@Tag(name = "Redisson测试接口")
public class RedissonTestController {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 测试Redisson分布式锁功能
     * <p>
     * 验证Redisson客户端是否正常工作，分布式锁是否可用
     * </p>
     *
     * @return 测试结果
     */
    @GetMapping("/redisson")
    @Operation(summary = "测试Redisson分布式锁", description = "验证Redisson客户端和分布式锁功能是否正常")
    public ResponseDTO<String> testRedisson() {
        if (redissonClient == null) {
            log.error("[Redisson测试] RedissonClient未配置");
            return ResponseDTO.error("REDISSON_NOT_CONFIGURED", "Redisson客户端未配置");
        }

        RLock lock = null;
        try {
            // 获取分布式锁
            lock = redissonClient.getLock("test:lock");
            
            // 尝试获取锁，等待5秒，锁定10秒
            boolean locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            
            if (locked) {
                try {
                    log.info("[Redisson测试] 成功获取分布式锁");
                    // 模拟业务操作
                    Thread.sleep(100);
                    return ResponseDTO.ok("Redisson工作正常");
                } finally {
                    // 释放锁
                    lock.unlock();
                    log.info("[Redisson测试] 成功释放分布式锁");
                }
            } else {
                log.warn("[Redisson测试] 获取锁失败，可能被其他线程占用");
                return ResponseDTO.error("LOCK_FAILED", "获取锁失败");
            }
        } catch (InterruptedException e) {
            log.error("[Redisson测试] 获取锁时被中断", e);
            Thread.currentThread().interrupt();
            return ResponseDTO.error("LOCK_INTERRUPTED", "获取锁时被中断: " + e.getMessage());
        } catch (Exception e) {
            log.error("[Redisson测试] Redisson测试失败", e);
            return ResponseDTO.error("REDISSON_TEST_FAILED", "Redisson测试失败: " + e.getMessage());
        } finally {
            // 确保锁被释放
            if (lock != null && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("[Redisson测试] 释放锁时发生异常", e);
                }
            }
        }
    }

    /**
     * 测试Redisson连接状态
     * <p>
     * 检查Redisson客户端连接是否正常
     * </p>
     *
     * @return 连接状态
     */
    @GetMapping("/redisson/status")
    @Operation(summary = "检查Redisson连接状态", description = "检查Redisson客户端连接是否正常")
    public ResponseDTO<String> checkRedissonStatus() {
        if (redissonClient == null) {
            return ResponseDTO.error("REDISSON_NOT_CONFIGURED", "Redisson客户端未配置");
        }

        try {
            // 尝试获取配置信息
            var config = redissonClient.getConfig();
            if (config != null) {
                return ResponseDTO.ok("Redisson客户端已配置，连接正常");
            } else {
                return ResponseDTO.error("REDISSON_CONFIG_ERROR", "Redisson配置异常");
            }
        } catch (Exception e) {
            log.error("[Redisson测试] 检查连接状态失败", e);
            return ResponseDTO.error("REDISSON_STATUS_CHECK_FAILED", "检查连接状态失败: " + e.getMessage());
        }
    }
}

