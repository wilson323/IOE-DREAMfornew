package net.lab1024.sa.common.system.controller;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.system.domain.dto.ConfigCreateDTO;
import net.lab1024.sa.common.system.domain.dto.ConfigUpdateDTO;
import net.lab1024.sa.common.system.domain.dto.DictCreateDTO;
import net.lab1024.sa.common.system.domain.vo.DictVO;
import net.lab1024.sa.common.system.service.SystemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统控制器
 * 整合自ioedream-system-service
 *
 * 符合CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "系统管理", description = "系统配置和字典相关接口")
public class SystemController {

    @Resource
    private SystemService systemService;

    // ==================== 系统配置管理 ====================

    @PostMapping("/config")
    @Observed(name = "system.createConfig", contextualName = "system-create-config")
    @Operation(summary = "创建配置", description = "创建新的系统配置")
    public ResponseDTO<Long> createConfig(@Valid @RequestBody ConfigCreateDTO dto) {
        return systemService.createConfig(dto);
    }

    @PutMapping("/config/{configId}")
    @Observed(name = "system.updateConfig", contextualName = "system-update-config")
    @Operation(summary = "更新配置", description = "更新系统配置")
    public ResponseDTO<Void> updateConfig(@PathVariable Long configId, @Valid @RequestBody ConfigUpdateDTO dto) {
        return systemService.updateConfig(configId, dto);
    }

    @DeleteMapping("/config/{configId}")
    @Observed(name = "system.deleteConfig", contextualName = "system-delete-config")
    @Operation(summary = "删除配置", description = "删除系统配置")
    public ResponseDTO<Void> deleteConfig(@PathVariable Long configId) {
        return systemService.deleteConfig(configId);
    }

    @GetMapping("/config/{configKey}")
    @Observed(name = "system.getConfigValue", contextualName = "system-get-config-value")
    @Operation(summary = "获取配置值", description = "根据配置键获取配置值")
    public ResponseDTO<String> getConfigValue(@PathVariable String configKey) {
        String value = systemService.getConfigValue(configKey);
        return ResponseDTO.ok(value);
    }

    @PostMapping("/config/refresh")
    @Observed(name = "system.refreshConfigCache", contextualName = "system-refresh-config-cache")
    @Operation(summary = "刷新配置缓存", description = "刷新所有配置缓存")
    public ResponseDTO<Void> refreshConfigCache() {
        return systemService.refreshConfigCache();
    }

    // ==================== 数据字典管理 ====================

    @PostMapping("/dict")
    @Observed(name = "system.createDict", contextualName = "system-create-dict")
    @Operation(summary = "创建字典", description = "创建新的字典数据")
    public ResponseDTO<Long> createDict(@Valid @RequestBody DictCreateDTO dto) {
        return systemService.createDict(dto);
    }

    @GetMapping("/dict/list/{dictType}")
    @Observed(name = "system.getDictList", contextualName = "system-get-dict-list")
    @Operation(summary = "获取字典列表", description = "获取指定类型的字典列表")
    public ResponseDTO<List<DictVO>> getDictList(@PathVariable String dictType) {
        return systemService.getDictList(dictType);
    }

    @GetMapping("/dict/tree/{dictType}")
    @Observed(name = "system.getDictTree", contextualName = "system-get-dict-tree")
    @Operation(summary = "获取字典树", description = "获取指定类型的字典树形结构")
    public ResponseDTO<List<DictVO>> getDictTree(@PathVariable String dictType) {
        return systemService.getDictTree(dictType);
    }

    @PostMapping("/dict/refresh")
    @Observed(name = "system.refreshDictCache", contextualName = "system-refresh-dict-cache")
    @Operation(summary = "刷新字典缓存", description = "刷新所有字典缓存")
    public ResponseDTO<Void> refreshDictCache() {
        return systemService.refreshDictCache();
    }

    // ==================== 系统信息查询 ====================

    @GetMapping("/info")
    @Observed(name = "system.getSystemInfo", contextualName = "system-get-system-info")
    @Operation(summary = "获取系统信息", description = "获取JVM、操作系统、应用等系统信息")
    public ResponseDTO<Map<String, Object>> getSystemInfo() {
        return systemService.getSystemInfo();
    }

    @GetMapping("/statistics")
    @Observed(name = "system.getSystemStatistics", contextualName = "system-get-system-statistics")
    @Operation(summary = "获取系统统计", description = "获取配置、字典等统计信息")
    public ResponseDTO<Map<String, Object>> getSystemStatistics() {
        return systemService.getSystemStatistics();
    }
}

