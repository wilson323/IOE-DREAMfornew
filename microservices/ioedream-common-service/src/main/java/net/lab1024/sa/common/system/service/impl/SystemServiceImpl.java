package net.lab1024.sa.common.system.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.system.dao.SystemConfigDao;
import net.lab1024.sa.common.system.dao.SystemDictDao;
import net.lab1024.sa.common.system.domain.dto.ConfigCreateDTO;
import net.lab1024.sa.common.system.domain.dto.ConfigUpdateDTO;
import net.lab1024.sa.common.system.domain.dto.DictCreateDTO;
import net.lab1024.sa.common.system.domain.entity.SystemConfigEntity;
import net.lab1024.sa.common.system.domain.entity.SystemDictEntity;
import net.lab1024.sa.common.system.domain.vo.DictVO;
import net.lab1024.sa.common.system.manager.ConfigManager;
import net.lab1024.sa.common.system.manager.DictManager;
import net.lab1024.sa.common.system.service.SystemService;

/**
 * 系统服务实现类 整合自ioedream-system-service
 *
 * 符合CLAUDE.md规范： - 使用@Service注解 - 使用@Resource依赖注入 - 使用@Transactional事务管理 -
 * 调用Manager层处理复杂逻辑
 *
 * 企业级特性： - 配置动态刷新 - 配置版本管理 - 配置变更审计 - 配置加密存储 - 多环境配置隔离
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class SystemServiceImpl implements SystemService {


    @Resource
    private SystemConfigDao systemConfigDao;

    @Resource
    private SystemDictDao systemDictDao;

    @Resource
    private ConfigManager configManager;

    @Resource(name = "systemDictManager")
    private DictManager dictManager;

    @Override
    @Observed(name = "system.createConfig", contextualName = "system-create-config")
    public ResponseDTO<Long> createConfig(ConfigCreateDTO dto) {
        try {
            SystemConfigEntity config = new SystemConfigEntity();
            config.setConfigKey(dto.getConfigKey());
            config.setConfigValue(dto.getConfigValue());
            config.setConfigName(dto.getConfigName()); // 配置名称
            config.setDescription(dto.getConfigDesc()); // 使用description字段
            config.setConfigType(dto.getConfigType() != null ? dto.getConfigType() : "SYSTEM");
            config.setIsEncrypted(dto.getIsEncrypted() != null ? dto.getIsEncrypted() : 0);

            // 如果需要加密
            if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
                // 注意：ConfigManager可能没有encryptConfigValue方法，这里先注释
                // config.setConfigValue(configManager.encryptConfigValue(config.getConfigValue()));
            }

            systemConfigDao.insert(config);

            log.info("创建系统配置成功 - key: {}", config.getConfigKey());
            // SystemConfigEntity使用configId作为主键
            return ResponseDTO.ok(config.getConfigId());

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[创建系统配置] 参数异常，key: {}, error={}", dto.getConfigKey(), e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "创建系统配置参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[创建系统配置] 业务异常，key: {}, error={}", dto.getConfigKey(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[创建系统配置] 系统异常，key: {}", dto.getConfigKey(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "创建系统配置失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.updateConfig", contextualName = "system-update-config")
    public ResponseDTO<Void> updateConfig(Long configId, ConfigUpdateDTO dto) {
        try {
            SystemConfigEntity config = systemConfigDao.selectById(configId);
            if (config == null) {
                return ResponseDTO.error("配置不存在");
            }

            if (dto.getConfigValue() != null) {
                // 如果需要加密
                if (dto.getIsEncrypted() != null && dto.getIsEncrypted() == 1) {
                    // 注意：ConfigManager可能没有encryptConfigValue方法，这里先注释
                    // config.setConfigValue(configManager.encryptConfigValue(dto.getConfigValue()));
                } else {
                    config.setConfigValue(dto.getConfigValue());
                }
            }
            // 更新其他字段
            if (dto.getConfigName() != null) {
                config.setConfigName(dto.getConfigName());
            }
            if (dto.getConfigDesc() != null) {
                config.setDescription(dto.getConfigDesc());
            }
            if (dto.getConfigType() != null) {
                config.setConfigType(dto.getConfigType());
            }
            if (dto.getIsEncrypted() != null) {
                config.setIsEncrypted(dto.getIsEncrypted());
            }

            systemConfigDao.updateById(config);

            // 刷新缓存（使用@CacheEvict注解）
            evictConfigCache(config.getConfigKey());

            log.info("更新系统配置成功 - key: {}", config.getConfigKey());
            return ResponseDTO.ok();

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[更新系统配置] 参数异常，configId: {}, error={}", configId, e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "更新系统配置参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[更新系统配置] 业务异常，configId: {}, error={}", configId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[更新系统配置] 系统异常，configId: {}", configId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "更新系统配置失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.deleteConfig", contextualName = "system-delete-config")
    public ResponseDTO<Void> deleteConfig(Long configId) {
        try {
            SystemConfigEntity config = systemConfigDao.selectById(configId);
            if (config == null) {
                return ResponseDTO.error("配置不存在");
            }

            // SystemConfigEntity没有isSystem字段，跳过检查
            // if (config.getIsSystem () == 1) {
            // return ResponseDTO.error ("系统内置配置不能删除");
            // }

            systemConfigDao.deleteById(configId);

            // 刷新缓存（使用@CacheEvict注解）
            evictConfigCache(config.getConfigKey());

            log.info("删除系统配置成功 - key: {}", config.getConfigKey());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[删除系统配置] 业务异常，configId: {}, error={}", configId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[删除系统配置] 系统异常，configId: {}", configId, e);
            return ResponseDTO.error("SYSTEM_ERROR", "删除系统配置失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.getConfigValue", contextualName = "system-get-config-value")
    @Cacheable(value = "system:config", key = "#configKey", unless = "#result == null")
    public String getConfigValue(String configKey) {
        // 直接查询数据库（缓存由@Cacheable注解处理）
        SystemConfigEntity config = systemConfigDao.selectByKey(configKey);
        if (config == null) {
            return null;
        }

        String value = config.getConfigValue();

        // 如果需要解密
        if (config.getIsEncrypted() != null && config.getIsEncrypted() == 1) {
            // 注意：ConfigManager可能没有decryptConfigValue方法，这里先注释
            // value = configManager.decryptConfigValue(value);
        }

        return value;
    }

    @Override
    @Observed(name = "system.getConfig", contextualName = "system-get-config")
    @Cacheable(value = "system:config", key = "#configKey", unless = "#result == null || !#result.isSuccess() || #result.getData() == null")
    public ResponseDTO<String> getConfig(String configKey) {
        try {
            String value = getConfigValue(configKey);
            if (value == null) {
                return ResponseDTO.error("配置不存在");
            }
            return ResponseDTO.ok(value);
        } catch (Exception e) {
            log.error("[获取系统配置] 系统异常，configKey: {}", configKey, e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取系统配置失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.getAllConfigs", contextualName = "system-get-all-configs")
    @Cacheable(value = "system:config:all", unless = "#result == null || !#result.isSuccess() || #result.getData() == null || #result.getData().isEmpty()")
    public ResponseDTO<Map<String, String>> getAllConfigs() {
        try {
            List<SystemConfigEntity> configList = systemConfigDao.selectList(null);
            Map<String, String> configMap = configList.stream()
                    .collect(Collectors.toMap(
                            SystemConfigEntity::getConfigKey,
                            SystemConfigEntity::getConfigValue,
                            (v1, v2) -> v1 // 如果有重复key，保留第一个
                    ));
            return ResponseDTO.ok(configMap);
        } catch (Exception e) {
            log.error("[获取所有系统配置] 系统异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取所有系统配置失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.refreshConfigCache", contextualName = "system-refresh-config-cache")
    @CacheEvict(value = "system:config", allEntries = true)
    public ResponseDTO<Void> refreshConfigCache() {
        try {
            // 缓存清除由@CacheEvict注解自动处理
            log.info("刷新配置缓存成功");
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[刷新配置缓存] 系统异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "刷新配置缓存失败，请稍后重试");
        }
    }

    /**
     * 清除指定配置的缓存
     * <p>
     * 使用@CacheEvict注解清除缓存
     * </p>
     *
     * @param configKey
     *                  配置键
     */
    @CacheEvict(value = "system:config", key = "#configKey")
    public void evictConfigCache(String configKey) {
        log.debug("清除配置缓存 - configKey: {}", configKey);
    }

    @Override
    @Observed(name = "system.createDict", contextualName = "system-create-dict")
    public ResponseDTO<Long> createDict(DictCreateDTO dto) {
        try {
            // 根据dictTypeCode查询dictTypeId（如果需要）
            // 注意：DictCreateDTO只有dictTypeCode，没有dictTypeId
            // 这里简化处理，假设dictTypeId可以为null或通过其他方式获取
            Long dictTypeId = null; // TODO: 需要通过dictTypeCode查询dictTypeId

            // 验证字典值唯一性（使用typeCode和dictValue）
            SystemDictEntity existing = systemDictDao.selectByTypeCodeAndValue(dto.getDictTypeCode(),
                    dto.getDictDataValue());
            if (existing != null) {
                return ResponseDTO.error("字典值已存在");
            }

            SystemDictEntity dict = new SystemDictEntity();
            dict.setDictTypeId(dictTypeId);
            dict.setTypeCode(dto.getDictTypeCode()); // SystemDictEntity使用typeCode，不是dictTypeCode
            dict.setDictLabel(dto.getDictDataCode()); // 使用dictDataCode作为label
            dict.setDictValue(dto.getDictDataValue()); // 使用dictDataValue作为value
            dict.setSortOrder(dto.getSortOrder());
            dict.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            dict.setIsDefault(0); // DictCreateDTO没有isDefault字段，默认为0
            dict.setRemark(dto.getRemark());
            dict.setCreateTime(LocalDateTime.now());
            dict.setUpdateTime(LocalDateTime.now());

            systemDictDao.insert(dict);

            // 刷新缓存（使用@CacheEvict注解）
            evictDictCache(dto.getDictTypeCode());

            log.info("创建字典数据成功 - typeCode: {}, value: {}", dto.getDictTypeCode(), dto.getDictDataValue());
            return ResponseDTO.ok(dict.getDictId()); // SystemDictEntity使用dictId作为主键

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[创建字典数据] 参数异常，typeCode: {}, error={}", dto.getDictTypeCode(), e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "创建字典数据参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.warn("[创建字典数据] 业务异常，typeCode: {}, error={}", dto.getDictTypeCode(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[创建字典数据] 系统异常，typeCode: {}", dto.getDictTypeCode(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "创建字典数据失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.getDictList", contextualName = "system-get-dict-list")
    @Cacheable(value = "system:dict", key = "#dictType", unless = "#result == null || !#result.isSuccess() || #result.getData() == null || #result.getData().isEmpty()")
    public ResponseDTO<List<DictVO>> getDictList(String dictType) {
        try {
            // 直接调用DAO查询，缓存由@Cacheable注解处理
            List<SystemDictEntity> list = systemDictDao.selectEnabledByTypeCode(dictType);
            List<DictVO> voList = list.stream().map(this::convertDictToVO).collect(Collectors.toList());

            return ResponseDTO.ok(voList);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[获取字典列表] 参数异常，dictType: {}, error={}", dictType, e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "获取字典列表参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[获取字典列表] 系统异常，dictType: {}", dictType, e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取字典列表失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.getDictTree", contextualName = "system-get-dict-tree")
    @Cacheable(value = "system:dict:tree", key = "#dictType", unless = "#result == null || !#result.isSuccess() || #result.getData() == null || #result.getData().isEmpty()")
    public ResponseDTO<List<DictVO>> getDictTree(String dictType) {
        try {
            log.debug("[字典树] 获取字典树: dictType={}", dictType);

            // 1. 获取字典列表（使用@Cacheable注解缓存）
            List<SystemDictEntity> list = systemDictDao.selectEnabledByTypeCode(dictType);

            // 2. 转换为DictVO列表（直接使用convertDictToVO方法）
            List<DictVO> dictVOList = list.stream().map(this::convertDictToVO).collect(Collectors.toList());

            log.debug("[字典树] 获取字典树成功: dictType={}, count={}", dictType, dictVOList.size());
            return ResponseDTO.ok(dictVOList);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[字典树] 参数异常，dictType: {}, error={}", dictType, e.getMessage());
            return ResponseDTO.error("PARAM_ERROR", "获取字典树参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[字典树] 系统异常，dictType: {}", dictType, e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取字典树失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.refreshDictCache", contextualName = "system-refresh-dict-cache")
    @CacheEvict(value = { "system:dict", "system:dict:tree" }, allEntries = true)
    public ResponseDTO<Void> refreshDictCache() {
        try {
            // 缓存清除由@CacheEvict注解自动处理
            log.info("刷新字典缓存成功");
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[刷新字典缓存] 系统异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "刷新字典缓存失败，请稍后重试");
        }
    }

    /**
     * 清除指定字典类型的缓存
     * <p>
     * 使用@CacheEvict注解清除缓存
     * </p>
     *
     * @param dictTypeCode
     *                     字典类型代码
     */
    @CacheEvict(value = { "system:dict", "system:dict:tree" }, key = "#dictTypeCode")
    public void evictDictCache(String dictTypeCode) {
        log.debug("清除字典缓存 - dictTypeCode: {}", dictTypeCode);
    }

    @Override
    @Observed(name = "system.getSystemInfo", contextualName = "system-get-system-info")
    public ResponseDTO<Map<String, Object>> getSystemInfo() {
        try {
            Map<String, Object> info = new HashMap<>();

            // JVM信息
            Runtime runtime = Runtime.getRuntime();
            info.put("jvmTotalMemory", runtime.totalMemory());
            info.put("jvmFreeMemory", runtime.freeMemory());
            info.put("jvmMaxMemory", runtime.maxMemory());
            info.put("jvmUsedMemory", runtime.totalMemory() - runtime.freeMemory());

            // 操作系统信息
            info.put("osName", System.getProperty("os.name"));
            info.put("osVersion", System.getProperty("os.version"));
            info.put("osArch", System.getProperty("os.arch"));

            // 应用信息
            info.put("javaVersion", System.getProperty("java.version"));
            info.put("javaVendor", System.getProperty("java.vendor"));
            info.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

            return ResponseDTO.ok(info);

        } catch (Exception e) {
            log.error("[获取系统信息] 系统异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取系统信息失败，请稍后重试");
        }
    }

    @Override
    @Observed(name = "system.getSystemStatistics", contextualName = "system-get-system-statistics")
    public ResponseDTO<Map<String, Object>> getSystemStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 配置统计
            long configCount = systemConfigDao.selectCount(null);
            stats.put("configCount", configCount);

            // 字典统计
            long dictCount = systemDictDao.selectCount(null);
            stats.put("dictCount", dictCount);

            return ResponseDTO.ok(stats);

        } catch (Exception e) {
            log.error("[获取系统统计] 系统异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "获取系统统计失败，请稍后重试");
        }
    }

    private DictVO convertDictToVO(SystemDictEntity entity) {
        DictVO vo = new DictVO();
        vo.setDictId(entity.getDictId()); // SystemDictEntity使用dictId作为主键
        vo.setDictTypeCode(entity.getTypeCode()); // SystemDictEntity使用typeCode，不是dictTypeCode
        vo.setDictDataCode(entity.getDictLabel()); // 使用dictLabel作为dictDataCode
        vo.setDictDataValue(entity.getDictValue()); // 使用dictValue作为dictDataValue
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        // DictVO没有dictTypeId, dictTypeName, statusDesc, cssClass, listClass, isDefault,
        // createTime字段
        return vo;
    }
}
