package net.lab1024.sa.consume.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.consume.engine.mode.ConsumptionModeEngine;
import net.lab1024.sa.consume.engine.mode.ConsumptionModeEngineInitializer;
import net.lab1024.sa.consume.engine.mode.config.ConsumptionModeConfig;

/**
 * 消费模式管理控制器
 * 提供消费模式的管理、配置、监控等功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@RestController
@RequestMapping("/api/consume/mode")
@Tag(name = "消费模式管理", description = "消费模式相关管理接口")
public class ConsumptionModeController {

    @Resource
    private ConsumptionModeEngine modeEngine;

    @Resource
    private ConsumptionModeEngineInitializer engineInitializer;

    /**
     * 获取所有支持的消费模式
     */
    @GetMapping("/supported")
    @Operation(summary = "获取所有支持的消费模式", description = "返回所有已注册的消费模式列表")
    @SaCheckPermission("consume:mode:view")
    public ResponseDTO<Set<String>> getSupportedModes() {
        try {
            Set<String> supportedModes = modeEngine.getAllSupportedModes();
            return ResponseDTO.ok(supportedModes, "获取支持的消费模式成功");
        } catch (Exception e) {
            log.error("获取支持的消费模式异常", e);
            return ResponseDTO.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 检查消费模式是否支持
     */
    @GetMapping("/supported/{modeCode}")
    @Operation(summary = "检查消费模式是否支持", description = "检查指定的消费模式是否已注册和支持")
    @SaCheckPermission("consume:mode:view")
    public ResponseDTO<Map<String, Object>> checkModeSupported(@PathVariable String modeCode) {
        try {
            boolean isSupported = modeEngine.isModeSupported(modeCode);

            Map<String, Object> result = new HashMap<>();
            result.put("modeCode", modeCode);
            result.put("isSupported", isSupported);
            result.put("strategy", isSupported ? modeEngine.getStrategy(modeCode).getClass().getSimpleName() : null);

            return ResponseDTO.ok(result, isSupported ? "消费模式支持" : "消费模式不支持");
        } catch (Exception e) {
            log.error("检查消费模式支持状态异常: modeCode={}", modeCode, e);
            return ResponseDTO.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取消费模式配置
     */
    @GetMapping("/config/{modeCode}")
    @Operation(summary = "获取消费模式配置", description = "获取指定消费模式的配置信息")
    @SaCheckPermission("consume:mode:view")
    public ResponseDTO<ConsumptionModeConfig> getModeConfig(@PathVariable String modeCode) {
        try {
            ConsumptionModeConfig config = modeEngine.getModeConfig(modeCode);
            if (config == null) {
                return ResponseDTO.error("消费模式配置不存在: " + modeCode);
            }
            return ResponseDTO.ok(config, "获取消费模式配置成功");
        } catch (Exception e) {
            log.error("获取消费模式配置异常: modeCode={}", modeCode, e);
            return ResponseDTO.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有消费模式配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取所有消费模式配置", description = "返回所有已缓存的消费模式配置")
    @SaCheckPermission("consume:mode:view")
    public ResponseDTO<Map<String, ConsumptionModeConfig>> getAllConfigs() {
        try {
            Map<String, ConsumptionModeConfig> configs = modeEngine.getAllConfigs();
            return ResponseDTO.ok(configs, "获取所有消费模式配置成功");
        } catch (Exception e) {
            log.error("获取所有消费模式配置异常", e);
            return ResponseDTO.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 解析并缓存消费模式配置
     */
    @PostMapping("/config/parse")
    @Operation(summary = "解析并缓存消费模式配置", description = "解析JSON格式的消费模式配置并缓存")
    @SaCheckPermission("consume:mode:manage")
    public ResponseDTO<ConsumptionModeConfig> parseAndCacheConfig(@RequestBody Map<String, Object> request) {
        try {
            String configJson = request.get("configJson").toString();
            ConsumptionModeConfig config = modeEngine.parseAndCacheConfig(configJson);
            return ResponseDTO.ok(config, "消费模式配置解析并缓存成功");
        } catch (Exception e) {
            log.error("解析并缓存消费模式配置异常", e);
            return ResponseDTO.error("解析失败: " + e.getMessage());
        }
    }

    /**
     * 重新加载消费模式配置
     */
    @PostMapping("/config/{modeCode}/reload")
    @Operation(summary = "重新加载消费模式配置", description = "重新加载指定消费模式的配置")
    @SaCheckPermission("consume:mode:manage")
    public ResponseDTO<String> reloadConfig(@PathVariable String modeCode, @RequestBody Map<String, Object> request) {
        try {
            String configJson = request.get("configJson").toString();
            modeEngine.reloadConfig(modeCode, configJson);
            return ResponseDTO.ok("消费模式配置重新加载成功");
        } catch (Exception e) {
            log.error("重新加载消费模式配置异常: modeCode={}", modeCode, e);
            return ResponseDTO.error("重新加载失败: " + e.getMessage());
        }
    }

    /**
     * 清理配置缓存
     */
    @DeleteMapping("/config/cache")
    @Operation(summary = "清理配置缓存", description = "清理消费模式配置缓存，可指定模式或清理所有")
    @SaCheckPermission("consume:mode:manage")
    public ResponseDTO<String> clearConfigCache(@RequestParam(required = false) String modeCode) {
        try {
            modeEngine.clearConfigCache(modeCode);
            String message = modeCode != null ? "消费模式配置缓存清理成功: " + modeCode : "所有消费模式配置缓存清理成功";
            return ResponseDTO.ok(message);
        } catch (Exception e) {
            log.error("清理消费模式配置缓存异常: modeCode={}", modeCode, e);
            return ResponseDTO.error("清理失败: " + e.getMessage());
        }
    }

    /**
     * 检查消费模式引擎健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "检查消费模式引擎健康状态", description = "检查消费模式引擎的健康状态和运行指标")
    @SaCheckPermission("consume:mode:view")
    public ResponseDTO<ConsumptionModeEngine.EngineHealthResult> checkEngineHealth() {
        try {
            ConsumptionModeEngine.EngineHealthResult healthResult = modeEngine.checkHealth();
            return ResponseDTO.ok(healthResult, "消费模式引擎健康状态检查完成");
        } catch (Exception e) {
            log.error("检查消费模式引擎健康状态异常", e);
            return ResponseDTO.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取引擎初始化摘要
     */
    @GetMapping("/init-summary")
    @Operation(summary = "获取引擎初始化摘要", description = "获取消费模式引擎的初始化摘要信息")
    @SaCheckPermission("consume:mode:view")
    public ResponseDTO<Map<String, Object>> getInitializationSummary() {
        try {
            String summary = engineInitializer.getInitializationSummary();

            Map<String, Object> result = new HashMap<>();
            result.put("summary", summary);
            result.put("supportedModes", modeEngine.getAllSupportedModes());
            result.put("configCount", modeEngine.getAllConfigs().size());
            result.put("timestamp", System.currentTimeMillis());

            return ResponseDTO.ok(result, "获取引擎初始化摘要成功");
        } catch (Exception e) {
            log.error("获取引擎初始化摘要异常", e);
            return ResponseDTO.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建默认配置模板
     */
    @PostMapping("/template/{modeCode}")
    @Operation(summary = "创建默认配置模板", description = "为指定的消费模式创建默认配置模板")
    @SaCheckPermission("consume:mode:manage")
    public ResponseDTO<Map<String, Object>> createDefaultTemplate(@PathVariable String modeCode) {
        try {
            // 这里可以调用ConfigurationParser来创建模板
            // 简化实现，返回基本模板

            Map<String, Object> template = new HashMap<>();
            template.put("modeCode", modeCode.toUpperCase());
            template.put("modeName", getDefaultModeName(modeCode));
            template.put("description", "自动生成的默认配置模板");
            template.put("enabled", true);
            template.put("priority", 100);
            template.put("maxRetryCount", 3);
            template.put("timeoutSeconds", 30);
            template.put("supportsFallback", true);

            return ResponseDTO.ok(template, "创建默认配置模板成功");
        } catch (Exception e) {
            log.error("创建默认配置模板异常: modeCode={}", modeCode, e);
            return ResponseDTO.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取消费模式统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取消费模式统计信息", description = "获取消费模式的使用统计和性能指标")
    @SaCheckPermission("consume:mode:view")
    public ResponseDTO<Map<String, Object>> getModeStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 基础统计
            statistics.put("totalModes", modeEngine.getAllSupportedModes().size());
            statistics.put("totalConfigs", modeEngine.getAllConfigs().size());
            statistics.put("timestamp", System.currentTimeMillis());

            // 健康状态
            ConsumptionModeEngine.EngineHealthResult healthResult = modeEngine.checkHealth();
            statistics.put("healthy", healthResult.isHealthy());
            statistics.put("fallbackAvailable", healthResult.isFallbackAvailable());

            // 模式详情
            Map<String, Object> modeDetails = new HashMap<>();
            for (String modeCode : modeEngine.getAllSupportedModes()) {
                Map<String, Object> modeInfo = new HashMap<>();
                modeInfo.put("supported", true);
                modeInfo.put("hasConfig", modeEngine.getModeConfig(modeCode) != null);
                modeInfo.put("strategy", modeEngine.getStrategy(modeCode).getClass().getSimpleName());
                modeDetails.put(modeCode, modeInfo);
            }
            statistics.put("modeDetails", modeDetails);

            return ResponseDTO.ok(statistics, "获取消费模式统计信息成功");
        } catch (Exception e) {
            log.error("获取消费模式统计信息异常", e);
            return ResponseDTO.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认模式名称
     */
    private String getDefaultModeName(String modeCode) {
        switch (modeCode.toUpperCase()) {
            case "STANDARD":
                return "标准消费模式";
            case "FIXED_AMOUNT":
                return "固定金额模式";
            case "SUBSIDY":
                return "补贴消费模式";
            case "PER_COUNT":
                return "按次消费模式";
            default:
                return "自定义消费模式";
        }
    }
}
