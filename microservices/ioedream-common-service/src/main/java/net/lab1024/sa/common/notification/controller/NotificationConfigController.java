package net.lab1024.sa.common.notification.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.notification.domain.vo.NotificationConfigVO;
import net.lab1024.sa.common.notification.service.NotificationConfigService;

/**
 * 通知配置控制器
 * <p>
 * 提供通知配置管理的REST API
 * 严格遵循CLAUDE.md规范:
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数验证
 * - 返回统一ResponseDTO格式
 * - 完整的Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notification/config")
@Tag(name = "通知配置管理", description = "通知渠道配置管理接口")
public class NotificationConfigController {

    @Resource
    private NotificationConfigService notificationConfigService;

    /**
     * 根据配置键获取配置值
     * <p>
     * 自动解密加密配置
     * </p>
     *
     * @param configKey 配置键
     * @return 配置值（已解密）
     */
    @GetMapping("/value/{configKey}")
    @Operation(summary = "获取配置值", description = "根据配置键获取配置值，自动解密加密配置")
    public ResponseDTO<String> getConfigValue(@PathVariable String configKey) {
        log.info("[通知配置] 获取配置值，configKey：{}", configKey);
        String value = notificationConfigService.getConfigValue(configKey);
        return ResponseDTO.ok(value);
    }

    /**
     * 根据配置类型获取所有配置
     *
     * @param configType 配置类型
     * @return 配置Map
     */
    @GetMapping("/type/{configType}")
    @Operation(summary = "获取配置列表（按类型）", description = "根据配置类型获取所有启用的配置")
    public ResponseDTO<Map<String, String>> getConfigsByType(@PathVariable String configType) {
        log.info("[通知配置] 获取配置列表（按类型），configType：{}", configType);
        Map<String, String> configs = notificationConfigService.getConfigsByType(configType);
        return ResponseDTO.ok(configs);
    }

    /**
     * 更新配置值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 是否更新成功
     */
    @PutMapping("/value/{configKey}")
    @Operation(summary = "更新配置值", description = "更新指定配置的值，支持加密配置")
    public ResponseDTO<Boolean> updateConfigValue(
            @PathVariable String configKey,
            @Valid @RequestBody Map<String, String> request) {
        log.info("[通知配置] 更新配置值，configKey：{}", configKey);
        String configValue = request.get("configValue");
        boolean result = notificationConfigService.updateConfigValue(configKey, configValue);
        return ResponseDTO.ok(result);
    }

    /**
     * 更新配置状态
     *
     * @param configKey 配置键
     * @param status    状态（1-启用 2-禁用）
     * @return 是否更新成功
     */
    @PutMapping("/status/{configKey}")
    @Operation(summary = "更新配置状态", description = "启用或禁用指定配置")
    public ResponseDTO<Boolean> updateConfigStatus(
            @PathVariable String configKey,
            @RequestParam Integer status) {
        log.info("[通知配置] 更新配置状态，configKey：{}，status：{}", configKey, status);
        boolean result = notificationConfigService.updateConfigStatus(configKey, status);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取配置列表（按类型）
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    @GetMapping("/list/{configType}")
    @Operation(summary = "获取配置列表", description = "根据配置类型获取配置列表（包含详细信息）")
    public ResponseDTO<List<NotificationConfigVO>> getConfigListByType(@PathVariable String configType) {
        log.info("[通知配置] 获取配置列表，configType：{}", configType);
        List<NotificationConfigVO> configs = notificationConfigService.getConfigListByType(configType);
        return ResponseDTO.ok(configs);
    }

    /**
     * 清除配置缓存
     *
     * @param configKey 配置键
     * @return 操作结果
     */
    @PutMapping("/cache/evict/{configKey}")
    @Operation(summary = "清除配置缓存", description = "清除指定配置的缓存，用于配置热更新")
    public ResponseDTO<Void> evictCache(@PathVariable String configKey) {
        log.info("[通知配置] 清除配置缓存，configKey：{}", configKey);
        notificationConfigService.evictCache(configKey);
        return ResponseDTO.ok();
    }
}
