package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AntiPassbackConfigForm;
import net.lab1024.sa.access.domain.form.AntiPassbackDetectForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackConfigVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackDetectResultVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackRecordVO;
import net.lab1024.sa.access.service.AntiPassbackService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 门禁反潜回控制器
 * <p>
 * 提供10个REST API端点：
 * - 反潜回检测（核心功能）
 * - 批量检测
 * - 配置管理（CRUD）
 * - 记录查询（分页）
 * - 记录处理
 * - 缓存清理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/anti-passback")
@Tag(name = "门禁反潜回管理", description = "门禁反潜回管理接口")
public class AntiPassbackController {

    @Resource
    private AntiPassbackService antiPassbackService;

    /**
     * 反潜回检测（核心API）
     */
    @PostMapping("/detect")
    @Operation(summary = "反潜回检测", description = "检测用户通行是否存在反潜回违规，返回是否允许通行")
    public ResponseDTO<AntiPassbackDetectResultVO> detect(
            @Valid @RequestBody AntiPassbackDetectForm detectForm) {
        log.info("[反潜回API] 检测请求: userId={}, deviceId={}, areaId={}",
                detectForm.getUserId(), detectForm.getDeviceId(), detectForm.getAreaId());

        try {
            return antiPassbackService.detect(detectForm);
        } catch (Exception e) {
            log.error("[反潜回API] 检测异常: userId={}, error={}",
                    detectForm.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("ANTI_PASSBACK_DETECT_ERROR", "反潜回检测失败: " + e.getMessage());
        }
    }

    /**
     * 批量反潜回检测
     */
    @PostMapping("/batch-detect")
    @Operation(summary = "批量反潜回检测", description = "批量检测多个用户通行是否存在反潜回违规")
    public ResponseDTO<List<AntiPassbackDetectResultVO>> batchDetect(
            @Valid @RequestBody List<AntiPassbackDetectForm> detectForms) {
        log.info("[反潜回API] 批量检测: count={}", detectForms.size());

        try {
            return antiPassbackService.batchDetect(detectForms);
        } catch (Exception e) {
            log.error("[反潜回API] 批量检测异常: count={}, error={}",
                    detectForms.size(), e.getMessage(), e);
            return ResponseDTO.error("BATCH_DETECT_ERROR", "批量检测失败: " + e.getMessage());
        }
    }

    /**
     * 创建反潜回配置
     */
    @PostMapping("/config")
    @Operation(summary = "创建反潜回配置", description = "创建新的反潜回配置")
    public ResponseDTO<Long> createConfig(
            @Valid @RequestBody AntiPassbackConfigForm configForm) {
        log.info("[反潜回API] 创建配置: mode={}, areaId={}, timeWindow={}",
                configForm.getMode(), configForm.getAreaId(), configForm.getTimeWindow());

        try {
            return antiPassbackService.createConfig(configForm);
        } catch (Exception e) {
            log.error("[反潜回API] 创建配置异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("CREATE_CONFIG_ERROR", "创建配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新反潜回配置
     */
    @PutMapping("/config")
    @Operation(summary = "更新反潜回配置", description = "更新反潜回配置")
    public ResponseDTO<Void> updateConfig(
            @Valid @RequestBody AntiPassbackConfigForm configForm) {
        log.info("[反潜回API] 更新配置: configId={}", configForm.getConfigId());

        try {
            return antiPassbackService.updateConfig(configForm);
        } catch (Exception e) {
            log.error("[反潜回API] 更新配置异常: configId={}, error={}",
                    configForm.getConfigId(), e.getMessage(), e);
            return ResponseDTO.error("UPDATE_CONFIG_ERROR", "更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 删除反潜回配置
     */
    @DeleteMapping("/config/{configId}")
    @Operation(summary = "删除反潜回配置", description = "删除反潜回配置")
    public ResponseDTO<Void> deleteConfig(
            @Parameter(description = "配置ID", required = true)
            @PathVariable Long configId) {
        log.info("[反潜回API] 删除配置: configId={}", configId);

        try {
            return antiPassbackService.deleteConfig(configId);
        } catch (Exception e) {
            log.error("[反潜回API] 删除配置异常: configId={}, error={}",
                    configId, e.getMessage(), e);
            return ResponseDTO.error("DELETE_CONFIG_ERROR", "删除配置失败: " + e.getMessage());
        }
    }

    /**
     * 查询反潜回配置详情
     */
    @GetMapping("/config/{configId}")
    @Operation(summary = "查询反潜回配置", description = "查询单个反潜回配置详情")
    public ResponseDTO<AntiPassbackConfigVO> getConfig(
            @Parameter(description = "配置ID", required = true)
            @PathVariable Long configId) {
        log.info("[反潜回API] 查询配置: configId={}", configId);

        try {
            return antiPassbackService.getConfig(configId);
        } catch (Exception e) {
            log.error("[反潜回API] 查询配置异常: configId={}, error={}",
                    configId, e.getMessage(), e);
            return ResponseDTO.error("GET_CONFIG_ERROR", "查询配置失败: " + e.getMessage());
        }
    }

    /**
     * 查询反潜回配置列表
     */
    @GetMapping("/config/list")
    @Operation(summary = "查询反潜回配置列表", description = "查询反潜回配置列表，支持按模式和区域筛选")
    public ResponseDTO<List<AntiPassbackConfigVO>> listConfigs(
            @Parameter(description = "反潜回模式（1-全局 2-区域 3-软 4-硬）")
            @RequestParam(required = false) Integer mode,
            @Parameter(description = "启用状态（0-禁用 1-启用）")
            @RequestParam(required = false) Integer enabled,
            @Parameter(description = "区域ID")
            @RequestParam(required = false) Long areaId) {
        log.info("[反潜回API] 查询配置列表: mode={}, enabled={}, areaId={}",
                mode, enabled, areaId);

        try {
            return antiPassbackService.listConfigs(mode, enabled, areaId);
        } catch (Exception e) {
            log.error("[反潜回API] 查询配置列表异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("LIST_CONFIGS_ERROR", "查询配置列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询反潜回检测记录（分页）
     */
    @GetMapping("/records")
    @Operation(summary = "查询反潜回记录", description = "分页查询反潜回检测记录")
    public ResponseDTO<PageResult<AntiPassbackRecordVO>> queryRecords(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "检测结果（1-正常 2-软反潜回 3-硬反潜回）")
            @RequestParam(required = false) Integer result,
            @Parameter(description = "处理状态（0-未处理 1-已处理 2-已忽略）")
            @RequestParam(required = false) Integer handled,
            @Parameter(description = "页码", required = true) @RequestParam Integer pageNum,
            @Parameter(description = "页大小", required = true) @RequestParam Integer pageSize) {
        log.info("[反潜回API] 查询记录: userId={}, deviceId={}, areaId={}, result={}, handled={}, pageNum={}, pageSize={}",
                userId, deviceId, areaId, result, handled, pageNum, pageSize);

        try {
            return antiPassbackService.queryRecords(userId, deviceId, areaId, result, handled, pageNum, pageSize);
        } catch (Exception e) {
            log.error("[反潜回API] 查询记录异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_RECORDS_ERROR", "查询记录失败: " + e.getMessage());
        }
    }

    /**
     * 处理反潜回记录
     */
    @PutMapping("/records/{recordId}/handle")
    @Operation(summary = "处理反潜回记录", description = "处理反潜回检测记录")
    public ResponseDTO<Void> handleRecord(
            @Parameter(description = "记录ID", required = true)
            @PathVariable Long recordId,
            @Parameter(description = "处理备注", required = true)
            @RequestParam String handleRemark) {
        log.info("[反潜回API] 处理记录: recordId={}, handleRemark={}", recordId, handleRemark);

        try {
            return antiPassbackService.handleRecord(recordId, handleRemark);
        } catch (Exception e) {
            log.error("[反潜回API] 处理记录异常: recordId={}, error={}",
                    recordId, e.getMessage(), e);
            return ResponseDTO.error("HANDLE_RECORD_ERROR", "处理记录失败: " + e.getMessage());
        }
    }

    /**
     * 清除用户反潜回缓存
     */
    @DeleteMapping("/cache/user/{userId}")
    @Operation(summary = "清除用户缓存", description = "清除指定用户的反潜回缓存，用于用户权限变更或离职")
    public ResponseDTO<Integer> clearUserCache(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("[反潜回API] 清除用户缓存: userId={}", userId);

        try {
            return antiPassbackService.clearUserCache(userId);
        } catch (Exception e) {
            log.error("[反潜回API] 清除用户缓存异常: userId={}, error={}",
                    userId, e.getMessage(), e);
            return ResponseDTO.error("CLEAR_USER_CACHE_ERROR", "清除用户缓存失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有反潜回缓存
     */
    @DeleteMapping("/cache/all")
    @Operation(summary = "清除所有缓存", description = "清除所有反潜回缓存，用于配置变更")
    public ResponseDTO<Integer> clearAllCache() {
        log.info("[反潜回API] 清除所有缓存");

        try {
            return antiPassbackService.clearAllCache();
        } catch (Exception e) {
            log.error("[反潜回API] 清除所有缓存异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("CLEAR_ALL_CACHE_ERROR", "清除所有缓存失败: " + e.getMessage());
        }
    }
}
