package net.lab1024.sa.access.config.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.config.service.SystemConfigService;
import net.lab1024.sa.access.config.service.UserPermissionService;
import net.lab1024.sa.access.config.service.LicenseService;
import net.lab1024.sa.access.config.service.BackupService;
import net.lab1024.sa.access.config.domain.form.SystemConfigForm;
import net.lab1024.sa.access.config.domain.form.UserPermissionForm;
import net.lab1024.sa.access.config.domain.vo.ConfigItemVO;
import net.lab1024.sa.access.config.domain.vo.PermissionTreeVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 严格遵循四层架构规范：
 * - Controller层只负责参数验证和调用Service
 * - 使用统一响应格式ResponseDTO
 * - 权限控制注解@SaCheckPermission
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/config")
@Slf4j
public class SystemConfigController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private UserPermissionService userPermissionService;

    @Resource
    private LicenseService licenseService;

    @Resource
    private BackupService backupService;

    /**
     * 获取系统配置列表
     *
     * @param configType 配置类型
     * @return 配置列表
     */
    @GetMapping("/list")
    @SaCheckPermission("access:config:list")
    public ResponseDTO<List<ConfigItemVO>> getConfigList(@RequestParam(required = false) String configType) {
        log.debug("[SystemConfigController] 获取系统配置列表: configType={}", configType);

        List<ConfigItemVO> configs = systemConfigService.getConfigList(configType);
        return ResponseDTO.userOk(configs);
    }

    /**
     * 获取单个配置
     *
     * @param configKey 配置键
     * @return 配置信息
     */
    @GetMapping("/item/{configKey}")
    @SaCheckPermission("access:config:get")
    public ResponseDTO<ConfigItemVO> getConfigItem(@PathVariable String configKey) {
        log.debug("[SystemConfigController] 获取配置项: configKey={}", configKey);

        ConfigItemVO config = systemConfigService.getConfigItem(configKey);
        return ResponseDTO.userOk(config);
    }

    /**
     * 创建或更新系统配置
     *
     * @param form 配置表单
     * @return 操作结果
     */
    @PostMapping("/item")
    @SaCheckPermission("access:config:save")
    public ResponseDTO<String> saveConfigItem(@Valid @RequestBody SystemConfigForm form) {
        log.info("[SystemConfigController] 保存配置项: configKey={}, configType={}",
                form.getConfigKey(), form.getConfigType());

        String result = systemConfigService.saveConfigItem(form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 删除配置项
     *
     * @param configKey 配置键
     * @return 删除结果
     */
    @DeleteMapping("/item/{configKey}")
    @SaCheckPermission("access:config:delete")
    public ResponseDTO<String> deleteConfigItem(@PathVariable String configKey) {
        log.info("[SystemConfigController] 删除配置项: configKey={}", configKey);

        String result = systemConfigService.deleteConfigItem(configKey);
        return ResponseDTO.userOk(result);
    }

    /**
     * 批量更新配置
     *
     * @param configs 配置列表
     * @return 更新结果
     */
    @PutMapping("/batch")
    @SaCheckPermission("access:config:batch")
    public ResponseDTO<String> batchUpdateConfig(@Valid @RequestBody List<SystemConfigForm> configs) {
        log.info("[SystemConfigController] 批量更新配置: count={}", configs.size());

        String result = systemConfigService.batchUpdateConfig(configs);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取用户权限树
     *
     * @param userId 用户ID
     * @return 权限树
     */
    @GetMapping("/permission/user/{userId}")
    @SaCheckPermission("access:permission:user:list")
    public ResponseDTO<PermissionTreeVO> getUserPermissionTree(@PathVariable Long userId) {
        log.debug("[SystemConfigController] 获取用户权限树: userId={}", userId);

        PermissionTreeVO permissionTree = userPermissionService.getUserPermissionTree(userId);
        return ResponseDTO.userOk(permissionTree);
    }

    /**
     * 分配用户权限
     *
     * @param form 权限分配表单
     * @return 分配结果
     */
    @PostMapping("/permission/assign")
    @SaCheckPermission("access:permission:assign")
    public ResponseDTO<String> assignUserPermission(@Valid @RequestBody UserPermissionForm form) {
        log.info("[SystemConfigController] 分配用户权限: userId={}, resourceCount={}",
                form.getUserId(), form.getResourcePermissions().size());

        String result = userPermissionService.assignUserPermission(form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 撤销用户权限
     *
     * @param userId 用户ID
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @return 撤销结果
     */
    @DeleteMapping("/permission/user/{userId}/{resourceType}/{resourceId}")
    @SaCheckPermission("access:permission:revoke")
    public ResponseDTO<String> revokeUserPermission(@PathVariable Long userId,
                                                    @PathVariable String resourceType,
                                                    @PathVariable Long resourceId) {
        log.info("[SystemConfigController] 撤销用户权限: userId={}, resourceType={}, resourceId={}",
                userId, resourceType, resourceId);

        String result = userPermissionService.revokeUserPermission(userId, resourceType, resourceId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取许可证信息
     *
     * @return 许可证信息
     */
    @GetMapping("/license/info")
    @SaCheckPermission("access:license:info")
    public ResponseDTO<Map<String, Object>> getLicenseInfo() {
        log.debug("[SystemConfigController] 获取许可证信息");

        Map<String, Object> licenseInfo = licenseService.getLicenseInfo();
        return ResponseDTO.userOk(licenseInfo);
    }

    /**
     * 验证许可证
     *
     * @return 验证结果
     */
    @PostMapping("/license/validate")
    @SaCheckPermission("access:license:validate")
    public ResponseDTO<Boolean> validateLicense() {
        log.debug("[SystemConfigController] 验证许可证");

        Boolean isValid = licenseService.validateLicense();
        return ResponseDTO.userOk(isValid);
    }

    /**
     * 创建系统备份
     *
     * @param backupType 备份类型
     * @param description 备份描述
     * @return 备份结果
     */
    @PostMapping("/backup/create")
    @SaCheckPermission("access:backup:create")
    public ResponseDTO<Map<String, Object>> createBackup(@RequestParam String backupType,
                                                        @RequestParam(required = false) String description) {
        log.info("[SystemConfigController] 创建系统备份: backupType={}", backupType);

        Map<String, Object> backupResult = backupService.createBackup(backupType, description);
        return ResponseDTO.userOk(backupResult);
    }

    /**
     * 恢复系统备份
     *
     * @param backupId 备份ID
     * @return 恢复结果
     */
    @PostMapping("/backup/restore/{backupId}")
    @SaCheckPermission("access:backup:restore")
    public ResponseDTO<String> restoreBackup(@PathVariable Long backupId) {
        log.warn("[SystemConfigController] 恢复系统备份: backupId={}", backupId);

        String result = backupService.restoreBackup(backupId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取备份列表
     *
     * @return 备份列表
     */
    @GetMapping("/backup/list")
    @SaCheckPermission("access:backup:list")
    public ResponseDTO<List<Map<String, Object>>> getBackupList() {
        log.debug("[SystemConfigController] 获取备份列表");

        List<Map<String, Object>> backupList = backupService.getBackupList();
        return ResponseDTO.userOk(backupList);
    }

    /**
     * 删除备份
     *
     * @param backupId 备份ID
     * @return 删除结果
     */
    @DeleteMapping("/backup/{backupId}")
    @SaCheckPermission("access:backup:delete")
    public ResponseDTO<String> deleteBackup(@PathVariable Long backupId) {
        log.info("[SystemConfigController] 删除备份: backupId={}", backupId);

        String result = backupService.deleteBackup(backupId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取系统配置统计
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @SaCheckPermission("access:config:statistics")
    public ResponseDTO<Map<String, Object>> getSystemStatistics() {
        log.debug("[SystemConfigController] 获取系统配置统计");

        Map<String, Object> statistics = systemConfigService.getSystemStatistics();
        return ResponseDTO.userOk(statistics);
    }

    /**
     * 重置系统配置为默认值
     *
     * @param configType 配置类型
     * @return 重置结果
     */
    @PostMapping("/reset/{configType}")
    @SaCheckPermission("access:config:reset")
    public ResponseDTO<String> resetConfigToDefault(@PathVariable String configType) {
        log.warn("[SystemConfigController] 重置系统配置: configType={}", configType);

        String result = systemConfigService.resetConfigToDefault(configType);
        return ResponseDTO.userOk(result);
    }

    /**
     * 导出配置
     *
     * @param configType 配置类型
     * @return 导出结果
     */
    @PostMapping("/export")
    @SaCheckPermission("access:config:export")
    public ResponseDTO<Map<String, Object>> exportConfig(@RequestParam(required = false) String configType) {
        log.info("[SystemConfigController] 导出配置: configType={}", configType);

        Map<String, Object> exportResult = systemConfigService.exportConfig(configType);
        return ResponseDTO.userOk(exportResult);
    }

    /**
     * 导入配置
     *
     * @param importData 导入数据
     * @param overwrite 是否覆盖
     * @return 导入结果
     */
    @PostMapping("/import")
    @SaCheckPermission("access:config:import")
    public ResponseDTO<String> importConfig(@RequestBody Map<String, Object> importData,
                                          @RequestParam(defaultValue = "false") Boolean overwrite) {
        log.info("[SystemConfigController] 导入配置: overwrite={}, itemCount={}", overwrite, importData.size());

        String result = systemConfigService.importConfig(importData, overwrite);
        return ResponseDTO.userOk(result);
    }
}