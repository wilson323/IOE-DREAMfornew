package net.lab1024.sa.infrastructure.config.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.infrastructure.config.domain.vo.ConfigHistoryVO;
import net.lab1024.sa.infrastructure.config.domain.vo.ConfigItemVO;
import net.lab1024.sa.infrastructure.config.service.ConfigManagementService;

/**
 * 配置管理控制器
 * 提供配置的增删改查、版本管理、环境隔离等功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@Validated
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigManagementService configManagementService;

    /**
     * 获取所有配置项
     *
     * @param application 应用名称
     * @param profile     环境名称
     * @param label       标签
     * @return 配置项列表
     */
    @GetMapping("/items")
    public ResponseDTO<List<ConfigItemVO>> getAllConfigs(
            @RequestParam(required = false) String application,
            @RequestParam(required = false) String profile,
            @RequestParam(required = false) String label) {

        log.info("获取配置列表，应用: {}, 环境: {}, 标签: {}", application, profile, label);

        List<ConfigItemVO> configs = configManagementService.getAllConfigs(application, profile, label);

        log.info("配置列表获取完成，数量: {}", configs.size());

        return ResponseDTO.ok(configs);
    }

    /**
     * 根据应用和环境获取配置
     *
     * @param application 应用名称
     * @param profile     环境名称
     * @param label       标签
     * @return 配置信息
     */
    @GetMapping("/{application}/{profile}")
    public ResponseDTO<Map<String, Object>> getConfigByApplicationAndProfile(
            @PathVariable @NotBlank String application,
            @PathVariable @NotBlank String profile,
            @RequestParam(defaultValue = "master") String label) {

        log.info("获取配置，应用: {}, 环境: {}, 标签: {}", application, profile, label);

        Map<String, Object> config = configManagementService.getConfig(application, profile, label);

        return ResponseDTO.ok(config);
    }

    /**
     * 获取单个配置项
     *
     * @param configId 配置项ID
     * @return 配置项详情
     */
    @GetMapping("/item/{configId}")
    public ResponseDTO<ConfigItemVO> getConfigById(@PathVariable @NotNull Long configId) {
        log.info("获取配置项，ID: {}", configId);

        ConfigItemVO config = configManagementService.getConfigById(configId);

        return ResponseDTO.ok(config);
    }

    /**
     * 创建或更新配置项
     *
     * @param configVO 配置项信息
     * @return 配置项ID
     */
    @PostMapping("/item")
    public ResponseDTO<Long> createOrUpdateConfig(@Valid @RequestBody ConfigItemVO configVO) {
        log.info("保存配置项，应用: {}, 键: {}", configVO.getApplication(), configVO.getConfigKey());

        Long configId = configManagementService.saveConfig(configVO);

        log.info("配置项保存成功，ID: {}", configId);

        return ResponseDTO.ok(configId);
    }

    /**
     * 删除配置项
     *
     * @param configId 配置项ID
     * @return 响应结果
     */
    @DeleteMapping("/item/{configId}")
    public ResponseDTO<Void> deleteConfig(@PathVariable @NotNull Long configId) {
        log.info("删除配置项，ID: {}", configId);

        configManagementService.deleteConfig(configId);

        log.info("配置项删除成功");

        return ResponseDTO.ok();
    }

    /**
     * 批量发布配置
     *
     * @param application 应用名称
     * @param profile     环境名称
     * @param configIds   配置项ID列表
     * @return 响应结果
     */
    @PostMapping("/publish")
    public ResponseDTO<Void> publishConfigs(
            @RequestParam @NotBlank String application,
            @RequestParam @NotBlank String profile,
            @RequestBody List<Long> configIds) {

        log.info("发布配置，应用: {}, 环境: {}, 配置数量: {}", application, profile, configIds.size());

        configManagementService.publishConfigs(application, profile, configIds);

        log.info("配置发布成功");

        return ResponseDTO.ok();
    }

    /**
     * 回滚配置到指定版本
     *
     * @param configId 配置项ID
     * @param version  版本号
     * @return 响应结果
     */
    @PostMapping("/rollback")
    public ResponseDTO<Void> rollbackConfig(
            @RequestParam @NotNull Long configId,
            @RequestParam @NotNull Long version) {

        log.info("回滚配置，配置ID: {}, 版本: {}", configId, version);

        configManagementService.rollbackConfig(configId, version);

        log.info("配置回滚成功");

        return ResponseDTO.ok();
    }

    /**
     * 获取配置变更历史
     *
     * @param configId 配置项ID
     * @return 配置历史列表
     */
    @GetMapping("/history/{configId}")
    public ResponseDTO<List<ConfigHistoryVO>> getConfigHistory(@PathVariable @NotNull Long configId) {
        log.info("获取配置历史，配置ID: {}", configId);

        List<ConfigHistoryVO> history = configManagementService.getConfigHistory(configId);

        return ResponseDTO.ok(history);
    }

    /**
     * 比较配置版本
     *
     * @param configId 配置项ID
     * @param version1 版本1
     * @param version2 版本2
     * @return 版本比较结果
     */
    @GetMapping("/compare")
    public ResponseDTO<Map<String, Object>> compareConfigVersions(
            @RequestParam @NotNull Long configId,
            @RequestParam @NotNull Long version1,
            @RequestParam @NotNull Long version2) {

        log.info("比较配置版本，配置ID: {}, 版本1: {}, 版本2: {}", configId, version1, version2);

        Map<String, Object> comparison = configManagementService.compareConfigVersions(configId, version1, version2);

        return ResponseDTO.ok(comparison);
    }

    /**
     * 配置健康检查
     *
     * @return 健康检查结果
     */
    @GetMapping("/health")
    public ResponseDTO<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "ioedream-config-service",
                "port", 8888,
                "configServer", "ENABLED",
                "totalConfigs", configManagementService.getTotalConfigCount(),
                "activeProfiles", configManagementService.getActiveProfiles());

        return ResponseDTO.ok(health);
    }

    /**
     * 获取环境列表
     *
     * @return 环境列表
     */
    @GetMapping("/environments")
    public ResponseDTO<List<String>> getEnvironments() {
        List<String> environments = configManagementService.getEnvironments();
        return ResponseDTO.ok(environments);
    }

    /**
     * 获取应用列表
     *
     * @return 应用列表
     */
    @GetMapping("/applications")
    public ResponseDTO<List<String>> getApplications() {
        List<String> applications = configManagementService.getApplications();
        return ResponseDTO.ok(applications);
    }

    /**
     * 配置搜索
     *
     * @param keyword     搜索关键词
     * @param application 应用名称
     * @param profile     环境名称
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseDTO<List<ConfigItemVO>> searchConfigs(
            @RequestParam @NotBlank String keyword,
            @RequestParam(required = false) String application,
            @RequestParam(required = false) String profile) {

        log.info("搜索配置，关键词: {}, 应用: {}, 环境: {}", keyword, application, profile);

        List<ConfigItemVO> results = configManagementService.searchConfigs(keyword, application, profile);

        log.info("配置搜索完成，结果数量: {}", results.size());

        return ResponseDTO.ok(results);
    }
}