package net.lab1024.sa.system.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.system.config.dao.ConfigDao;
import net.lab1024.sa.system.config.domain.entity.ConfigEntity;
import net.lab1024.sa.system.dict.dao.DictDataDao;
import net.lab1024.sa.system.dict.dao.DictTypeDao;
import net.lab1024.sa.system.dict.domain.entity.DictDataEntity;
import net.lab1024.sa.system.dict.domain.entity.DictTypeEntity;

/**
 * 系统配置管理服务
 * 支持系统参数配置、字典管理、系统监控配置和配置版本管理
 *
 * @author IOE-DREAM
 * @since 2025-11-28
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class SystemConfigurationService {

    @Resource
    private ConfigDao configDao;

    @Resource
    private DictTypeDao dictTypeDao;

    @Resource
    private DictDataDao dictDataDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        SYSTEM("系统配置", "系统基础参数配置"),
        BUSINESS("业务配置", "业务相关参数配置"),
        SECURITY("安全配置", "安全相关参数配置"),
        INTEGRATION("集成配置", "第三方系统集成配置"),
        MONITORING("监控配置", "系统监控相关配置"),
        NOTIFICATION("通知配置", "消息通知相关配置"),
        STORAGE("存储配置", "文件存储相关配置"),
        PERFORMANCE("性能配置", "性能优化相关配置");

        private final String description;
        private final String comment;

        ConfigType(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 配置值类型枚举
     */
    public enum ValueType {
        STRING("字符串", "文本类型"),
        INTEGER("整数", "整数类型"),
        DECIMAL("小数", "浮点数类型"),
        BOOLEAN("布尔值", "true/false"),
        JSON("JSON", "JSON格式字符串"),
        YAML("YAML", "YAML格式字符串"),
        PROPERTIES("属性", "Properties格式字符串");

        private final String description;
        private final String comment;

        ValueType(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 配置项信息
     */
    public static class Configuration {
        private String configId;
        private String configKey;
        private String configName;
        private String description;
        private ConfigType configType;
        private ValueType valueType;
        private String configValue;
        private String defaultValue;
        private boolean isRequired;
        private boolean isEncrypted;
        private boolean isReadonly;
        private String validationRule;
        private String remark;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private String createUserId;
        private String updateUserId;
        private Integer sortOrder;
        private String category;

        // 构造函数
        public Configuration(String configId, String configKey, String configName,
                ConfigType configType, ValueType valueType) {
            this.configId = configId;
            this.configKey = configKey;
            this.configName = configName;
            this.configType = configType;
            this.valueType = valueType;
            this.createTime = LocalDateTime.now();
            this.updateTime = LocalDateTime.now();
            this.isRequired = false;
            this.isEncrypted = false;
            this.isReadonly = false;
            this.sortOrder = 0;
        }

        // Getter和Setter方法
        public String getConfigId() {
            return configId;
        }

        public void setConfigId(String configId) {
            this.configId = configId;
        }

        public String getConfigKey() {
            return configKey;
        }

        public void setConfigKey(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigName() {
            return configName;
        }

        public void setConfigName(String configName) {
            this.configName = configName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ConfigType getConfigType() {
            return configType;
        }

        public void setConfigType(ConfigType configType) {
            this.configType = configType;
        }

        public ValueType getValueType() {
            return valueType;
        }

        public void setValueType(ValueType valueType) {
            this.valueType = valueType;
        }

        public String getConfigValue() {
            return configValue;
        }

        public void setConfigValue(String configValue) {
            this.configValue = configValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public boolean isRequired() {
            return isRequired;
        }

        public void setRequired(boolean required) {
            isRequired = required;
        }

        public boolean isEncrypted() {
            return isEncrypted;
        }

        public void setEncrypted(boolean encrypted) {
            isEncrypted = encrypted;
        }

        public boolean isReadonly() {
            return isReadonly;
        }

        public void setReadonly(boolean readonly) {
            isReadonly = readonly;
        }

        public String getValidationRule() {
            return validationRule;
        }

        public void setValidationRule(String validationRule) {
            this.validationRule = validationRule;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }

        public String getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(String createUserId) {
            this.createUserId = createUserId;
        }

        public String getUpdateUserId() {
            return updateUserId;
        }

        public void setUpdateUserId(String updateUserId) {
            this.updateUserId = updateUserId;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    /**
     * 配置版本信息
     */
    public static class ConfigurationVersion {
        private String versionId;
        private String versionName;
        private String description;
        private String configSnapshot; // 配置快照（JSON格式）
        private LocalDateTime createTime;
        private String createUserId;
        private boolean isActive;
        private String remark;

        // 构造函数
        public ConfigurationVersion(String versionId, String versionName, String description) {
            this.versionId = versionId;
            this.versionName = versionName;
            this.description = description;
            this.createTime = LocalDateTime.now();
            this.isActive = false;
        }

        // Getter和Setter方法
        public String getVersionId() {
            return versionId;
        }

        public void setVersionId(String versionId) {
            this.versionId = versionId;
        }

        public String getVersionName() {
            return versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getConfigSnapshot() {
            return configSnapshot;
        }

        public void setConfigSnapshot(String configSnapshot) {
            this.configSnapshot = configSnapshot;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public String getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(String createUserId) {
            this.createUserId = createUserId;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * 字典信息
     */
    public static class Dictionary {
        private String dictId;
        private String dictCode;
        private String dictName;
        private String description;
        private String remark;
        private LocalDateTime createTime;
        private String createUserId;

        // 构造函数
        public Dictionary(String dictId, String dictCode, String dictName) {
            this.dictId = dictId;
            this.dictCode = dictCode;
            this.dictName = dictName;
            this.createTime = LocalDateTime.now();
        }

        // Getter和Setter方法
        public String getDictId() {
            return dictId;
        }

        public void setDictId(String dictId) {
            this.dictId = dictId;
        }

        public String getDictCode() {
            return dictCode;
        }

        public void setDictCode(String dictCode) {
            this.dictCode = dictCode;
        }

        public String getDictName() {
            return dictName;
        }

        public void setDictName(String dictName) {
            this.dictName = dictName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public String getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(String createUserId) {
            this.createUserId = createUserId;
        }
    }

    /**
     * 字典项信息
     */
    public static class DictionaryItem {
        private String itemId;
        private String dictId;
        private String itemCode;
        private String itemName;
        private String itemValue;
        private Integer sortOrder;
        private String description;
        private boolean isActive;
        private String remark;
        private LocalDateTime createTime;
        private String createUserId;

        // 构造函数
        public DictionaryItem(String itemId, String dictId, String itemCode, String itemName) {
            this.itemId = itemId;
            this.dictId = dictId;
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.isActive = true;
            this.createTime = LocalDateTime.now();
            this.sortOrder = 0;
        }

        // Getter和Setter方法
        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getDictId() {
            return dictId;
        }

        public void setDictId(String dictId) {
            this.dictId = dictId;
        }

        public String getItemCode() {
            return itemCode;
        }

        public void setItemCode(String itemCode) {
            this.itemCode = itemCode;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getItemValue() {
            return itemValue;
        }

        public void setItemValue(String itemValue) {
            this.itemValue = itemValue;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public String getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(String createUserId) {
            this.createUserId = createUserId;
        }
    }

    /**
     * 创建配置项
     */
    public Configuration createConfiguration(String configKey, String configName,
            ConfigType configType, ValueType valueType,
            String configValue, String defaultValue,
            String description, String createUserId) {
        log.info("创建配置项: key={}, name={}, type={}", configKey, configName, configType);

        // 检查配置键是否已存在
        if (isConfigKeyExists(configKey)) {
            log.error("配置键已存在: {}", configKey);
            return null;
        }

        Configuration config = new Configuration(
                "CONFIG_" + System.currentTimeMillis(),
                configKey,
                configName,
                configType,
                valueType);

        config.setConfigValue(configValue);
        config.setDefaultValue(defaultValue);
        config.setDescription(description);
        config.setCreateUserId(createUserId);

        // 验证配置值
        if (!validateConfigValue(config)) {
            log.error("配置值验证失败: {}", configKey);
            return null;
        }

        log.info("配置项创建成功: configId={}, key={}", config.getConfigId(), configKey);
        return config;
    }

    /**
     * 更新配置项
     */
    @CacheEvict(value = "configurations", key = "#configId")
    public boolean updateConfiguration(String configId, String configValue,
            String description, String updateUserId) {
        log.info("更新配置项: configId={}, value={}", configId, configValue);

        Configuration config = getConfiguration(configId);
        if (config == null) {
            log.error("配置项不存在: {}", configId);
            return false;
        }

        if (config.isReadonly()) {
            log.error("配置项为只读，不允许修改: {}", configId);
            return false;
        }

        String oldValue = config.getConfigValue();
        config.setConfigValue(configValue);
        config.setDescription(description);
        config.setUpdateTime(LocalDateTime.now());
        config.setUpdateUserId(updateUserId);

        // 验证配置值
        if (!validateConfigValue(config)) {
            log.error("配置值验证失败，恢复原值: {}", configId);
            config.setConfigValue(oldValue);
            return false;
        }

        log.info("配置项更新成功: configId={}, old={}, new={}", configId, oldValue, configValue);
        return true;
    }

    /**
     * 批量更新配置
     */
    @CacheEvict(value = "configurations", allEntries = true)
    public Map<String, Boolean> batchUpdateConfigurations(Map<String, String> configValues,
            String updateUserId) {
        log.info("批量更新配置: count={}, operator={}", configValues.size(), updateUserId);

        Map<String, Boolean> results = new HashMap<>();

        for (Map.Entry<String, String> entry : configValues.entrySet()) {
            String configKey = entry.getKey();
            String configValue = entry.getValue();
            boolean success = false;

            try {
                Configuration config = getConfigurationByKey(configKey);
                if (config != null && !config.isReadonly()) {
                    success = updateConfiguration(config.getConfigId(), configValue,
                            config.getDescription(), updateUserId);
                }
            } catch (Exception e) {
                log.error("批量更新配置失败: key={}", configKey, e);
                success = false;
            }

            results.put(configKey, success);
        }

        long successCount = results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        log.info("批量更新配置完成: 总数={}, 成功={}", configValues.size(), successCount);

        return results;
    }

    /**
     * 删除配置项
     */
    @CacheEvict(value = "configurations", key = "#configId")
    public boolean deleteConfiguration(String configId, String operatorId) {
        log.info("删除配置项: configId={}, operator={}", configId, operatorId);

        Configuration config = getConfiguration(configId);
        if (config == null) {
            log.error("配置项不存在: {}", configId);
            return false;
        }

        if (config.isRequired()) {
            log.error("必需配置项不允许删除: {}", configId);
            return false;
        }

        // 从数据库删除
        try {
            Long id = Long.parseLong(configId.replace("CONFIG_", ""));
            int result = configDao.deleteById(id);
            if (result > 0) {
                log.info("配置项删除成功: configId={}", configId);
                return true;
            } else {
                log.error("配置项删除失败: configId={}", configId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除配置项异常: configId={}", configId, e);
            return false;
        }
    }

    /**
     * 创建配置版本
     */
    public ConfigurationVersion createConfigurationVersion(String versionName, String description,
            String createUserId) {
        log.info("创建配置版本: name={}, creator={}", versionName, createUserId);

        // 获取当前所有配置作为快照
        List<Configuration> allConfigs = getAllConfigurations();
        String configSnapshot = serializeConfigurations(allConfigs);

        ConfigurationVersion version = new ConfigurationVersion(
                "VERSION_" + System.currentTimeMillis(),
                versionName,
                description);

        version.setConfigSnapshot(configSnapshot);
        version.setCreateUserId(createUserId);

        log.info("配置版本创建成功: versionId={}", version.getVersionId());
        return version;
    }

    /**
     * 恢复配置版本
     */
    @CacheEvict(value = "configurations", allEntries = true)
    public boolean restoreConfigurationVersion(String versionId, String operatorId) {
        log.info("恢复配置版本: versionId={}, operator={}", versionId, operatorId);

        ConfigurationVersion version = getConfigurationVersion(versionId);
        if (version == null) {
            log.error("配置版本不存在: {}", versionId);
            return false;
        }

        // 解析配置快照
        List<Configuration> configs = deserializeConfigurations(version.getConfigSnapshot());
        if (configs == null) {
            log.error("配置快照解析失败: {}", versionId);
            return false;
        }

        // 恢复配置
        for (Configuration config : configs) {
            updateConfiguration(config.getConfigId(), config.getConfigValue(),
                    config.getDescription(), operatorId);
        }

        log.info("配置版本恢复成功: versionId={}", versionId);
        return true;
    }

    /**
     * 获取配置项
     */
    @Cacheable(value = "configurations", key = "#configId")
    public Configuration getConfiguration(String configId) {
        log.info("获取配置项: configId={}", configId);

        try {
            Long id = Long.parseLong(configId.replace("CONFIG_", ""));
            ConfigEntity entity = configDao.selectById(id);
            if (entity == null) {
                log.warn("配置项不存在: configId={}", configId);
                return null;
            }
            return convertToConfiguration(entity);
        } catch (Exception e) {
            log.error("获取配置项异常: configId={}", configId, e);
            return null;
        }
    }

    /**
     * 根据配置键获取配置项
     */
    @Cacheable(value = "configurations", key = "'key:' + #configKey")
    public Configuration getConfigurationByKey(String configKey) {
        log.info("根据配置键获取配置项: key={}", configKey);

        try {
            ConfigEntity entity = configDao.selectByConfigKey(configKey);
            if (entity == null) {
                log.warn("配置项不存在: configKey={}", configKey);
                return null;
            }
            return convertToConfiguration(entity);
        } catch (Exception e) {
            log.error("根据配置键获取配置项异常: configKey={}", configKey, e);
            return null;
        }
    }

    /**
     * 获取配置值
     */
    @Cacheable(value = "configValues", key = "#configKey")
    public String getConfigValue(String configKey) {
        log.info("获取配置值: key={}", configKey);

        Configuration config = getConfigurationByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    /**
     * 获取配置列表
     */
    @Cacheable(value = "configurationList", key = "#configType + '_' + #category")
    public List<Configuration> getConfigurationList(ConfigType configType, String category) {
        log.info("获取配置列表: type={}, category={}", configType, category);

        try {
            LambdaQueryWrapper<ConfigEntity> wrapper = new LambdaQueryWrapper<>();
            if (configType != null) {
                wrapper.eq(ConfigEntity::getConfigGroup, configType.name());
            }
            if (category != null && !category.isEmpty()) {
                wrapper.eq(ConfigEntity::getConfigGroup, category);
            }
            wrapper.eq(ConfigEntity::getDeletedFlag, 0);
            wrapper.orderByAsc(ConfigEntity::getSortOrder, ConfigEntity::getConfigId);

            List<ConfigEntity> entities = configDao.selectList(wrapper);
            return entities.stream()
                    .map(this::convertToConfiguration)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取配置列表异常: type={}, category={}", configType, category, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取所有配置
     */
    public List<Configuration> getAllConfigurations() {
        log.info("获取所有配置");

        try {
            List<ConfigEntity> entities = configDao.selectAllConfigs();
            return entities.stream()
                    .map(this::convertToConfiguration)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取所有配置异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 创建字典
     */
    public Dictionary createDictionary(String dictCode, String dictName, String description,
            String createUserId) {
        log.info("创建字典: code={}, name={}", dictCode, dictName);

        // 检查字典代码是否已存在
        if (isDictCodeExists(dictCode)) {
            log.error("字典代码已存在: {}", dictCode);
            return null;
        }

        Dictionary dictionary = new Dictionary(
                "DICT_" + System.currentTimeMillis(),
                dictCode,
                dictName);

        dictionary.setDescription(description);
        dictionary.setCreateUserId(createUserId);

        log.info("字典创建成功: dictId={}, code={}", dictionary.getDictId(), dictCode);
        return dictionary;
    }

    /**
     * 创建字典项
     */
    public DictionaryItem createDictionaryItem(String dictId, String itemCode, String itemName,
            String itemValue, Integer sortOrder, String createUserId) {
        log.info("创建字典项: dictId={}, code={}, name={}", dictId, itemCode, itemName);

        // 检查字典是否存在
        Dictionary dictionary = getDictionary(dictId);
        if (dictionary == null) {
            log.error("字典不存在: {}", dictId);
            return null;
        }

        DictionaryItem item = new DictionaryItem(
                "ITEM_" + System.currentTimeMillis(),
                dictId,
                itemCode,
                itemName);

        item.setItemValue(itemValue);
        item.setSortOrder(sortOrder != null ? sortOrder : 0);
        item.setCreateUserId(createUserId);

        log.info("字典项创建成功: itemId={}, code={}", item.getItemId(), itemCode);
        return item;
    }

    /**
     * 获取字典项列表
     */
    @Cacheable(value = "dictionaryItems", key = "#dictCode")
    public List<DictionaryItem> getDictionaryItems(String dictCode) {
        log.info("获取字典项列表: dictCode={}", dictCode);

        try {
            List<DictDataEntity> entities = dictDataDao.selectEnabledByDictTypeCode(dictCode);
            return entities.stream()
                    .map(this::convertToDictionaryItem)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取字典项列表异常: dictCode={}", dictCode, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取配置统计信息
     */
    public Map<String, Object> getConfigurationStatistics() {
        log.info("获取配置统计信息");

        try {
            Map<String, Object> statistics = configDao.getConfigStatistics();
            if (statistics == null) {
                statistics = new HashMap<>();
            }
            return statistics;
        } catch (Exception e) {
            log.error("获取配置统计信息异常", e);
            return new HashMap<>();
        }
    }

    // 私有辅助方法

    /**
     * 验证配置值
     */
    private boolean validateConfigValue(Configuration config) {
        if (config.isRequired() && (config.getConfigValue() == null || config.getConfigValue().trim().isEmpty())) {
            log.error("必需配置项值为空: {}", config.getConfigKey());
            return false;
        }

        // 根据值类型验证
        switch (config.getValueType()) {
            case INTEGER:
                try {
                    Integer.parseInt(config.getConfigValue());
                } catch (NumberFormatException e) {
                    log.error("整数配置值格式错误: {}", config.getConfigKey());
                    return false;
                }
                break;
            case DECIMAL:
                try {
                    Double.parseDouble(config.getConfigValue());
                } catch (NumberFormatException e) {
                    log.error("小数配置值格式错误: {}", config.getConfigKey());
                    return false;
                }
                break;
            case BOOLEAN:
                if (!"true".equalsIgnoreCase(config.getConfigValue()) &&
                        !"false".equalsIgnoreCase(config.getConfigValue())) {
                    log.error("布尔配置值格式错误: {}", config.getConfigKey());
                    return false;
                }
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * 检查配置键是否存在
     */
    private boolean isConfigKeyExists(String configKey) {
        try {
            int count = configDao.checkConfigKeyUnique(configKey, null);
            return count > 0;
        } catch (Exception e) {
            log.error("检查配置键是否存在异常: configKey={}", configKey, e);
            return false;
        }
    }

    /**
     * 检查字典代码是否存在
     */
    private boolean isDictCodeExists(String dictCode) {
        try {
            int count = dictTypeDao.checkDictTypeCodeUnique(dictCode, null);
            return count > 0;
        } catch (Exception e) {
            log.error("检查字典代码是否存在异常: dictCode={}", dictCode, e);
            return false;
        }
    }

    /**
     * 序列化配置列表
     */
    private String serializeConfigurations(List<Configuration> configurations) {
        try {
            return objectMapper.writeValueAsString(configurations);
        } catch (Exception e) {
            log.error("序列化配置列表异常", e);
            return "[]";
        }
    }

    /**
     * 反序列化配置列表
     */
    private List<Configuration> deserializeConfigurations(String snapshot) {
        try {
            return objectMapper.readValue(snapshot,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Configuration.class));
        } catch (Exception e) {
            log.error("反序列化配置列表异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取配置版本
     */
    private ConfigurationVersion getConfigurationVersion(String versionId) {
        // TODO: 需要创建配置版本表和相关DAO
        log.warn("配置版本功能暂未实现: versionId={}", versionId);
        return null;
    }

    /**
     * 获取字典信息
     */
    private Dictionary getDictionary(String dictId) {
        try {
            Long id = Long.parseLong(dictId.replace("DICT_", ""));
            DictTypeEntity entity = dictTypeDao.selectById(id);
            if (entity == null) {
                log.warn("字典不存在: dictId={}", dictId);
                return null;
            }
            return convertToDictionary(entity);
        } catch (Exception e) {
            log.error("获取字典信息异常: dictId={}", dictId, e);
            return null;
        }
    }

    /**
     * 将ConfigEntity转换为Configuration
     */
    private Configuration convertToConfiguration(ConfigEntity entity) {
        if (entity == null) {
            return null;
        }

        ConfigType configType = ConfigType.SYSTEM;
        try {
            if (entity.getConfigGroup() != null) {
                configType = ConfigType.valueOf(entity.getConfigGroup());
            }
        } catch (Exception e) {
            log.warn("配置类型转换失败: {}", entity.getConfigGroup());
        }

        ValueType valueType = ValueType.STRING;
        try {
            if (entity.getConfigType() != null) {
                valueType = ValueType.valueOf(entity.getConfigType());
            }
        } catch (Exception e) {
            log.warn("值类型转换失败: {}", entity.getConfigType());
        }

        Configuration config = new Configuration(
                String.valueOf(entity.getConfigId()),
                entity.getConfigKey(),
                entity.getConfigName(),
                configType,
                valueType);

        config.setConfigValue(entity.getConfigValue());
        config.setDefaultValue(entity.getDefaultValue());
        config.setDescription(entity.getDescription());
        config.setRemark(entity.getRemark());
        config.setValidationRule(entity.getValidationRule());
        config.setSortOrder(entity.getSortOrder());
        config.setCategory(entity.getConfigGroup());
        config.setRequired(entity.getIsSystem() != null && entity.getIsSystem() == 1);
        config.setEncrypted(entity.getIsEncrypt() != null && entity.getIsEncrypt() == 1);
        config.setReadonly(entity.getIsReadonly() != null && entity.getIsReadonly() == 1);

        if (entity.getCreateTime() != null) {
            config.setCreateTime(entity.getCreateTime());
        }
        if (entity.getUpdateTime() != null) {
            config.setUpdateTime(entity.getUpdateTime());
        }
        if (entity.getCreateUserId() != null) {
            config.setCreateUserId(String.valueOf(entity.getCreateUserId()));
        }
        if (entity.getUpdateUserId() != null) {
            config.setUpdateUserId(String.valueOf(entity.getUpdateUserId()));
        }

        return config;
    }

    /**
     * 将DictTypeEntity转换为Dictionary
     */
    private Dictionary convertToDictionary(DictTypeEntity entity) {
        if (entity == null) {
            return null;
        }

        Dictionary dictionary = new Dictionary(
                String.valueOf(entity.getDictTypeId()),
                entity.getDictTypeCode(),
                entity.getDictTypeName());

        dictionary.setDescription(entity.getDescription());
        dictionary.setRemark(entity.getRemark());
        if (entity.getCreateTime() != null) {
            dictionary.setCreateTime(entity.getCreateTime());
        }
        if (entity.getCreateUserId() != null) {
            dictionary.setCreateUserId(String.valueOf(entity.getCreateUserId()));
        }

        return dictionary;
    }

    /**
     * 将DictDataEntity转换为DictionaryItem
     */
    private DictionaryItem convertToDictionaryItem(DictDataEntity entity) {
        if (entity == null) {
            return null;
        }

        DictionaryItem item = new DictionaryItem(
                String.valueOf(entity.getDictDataId()),
                String.valueOf(entity.getDictTypeId()),
                entity.getDictValue(),
                entity.getDictLabel());

        item.setItemValue(entity.getDictValue());
        item.setSortOrder(entity.getSortOrder());
        item.setRemark(entity.getRemark());
        item.setActive(entity.getStatus() != null && entity.getStatus() == 1);
        if (entity.getCreateTime() != null) {
            item.setCreateTime(entity.getCreateTime());
        }
        if (entity.getCreateUserId() != null) {
            item.setCreateUserId(String.valueOf(entity.getCreateUserId()));
        }

        return item;
    }

    /**
     * 获取配置类型列表
     */
    public List<Map<String, Object>> getConfigTypes() {
        return Arrays.stream(ConfigType.values())
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("code", type.name());
                    typeInfo.put("description", type.getDescription());
                    typeInfo.put("comment", type.getComment());
                    return typeInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取值类型列表
     */
    public List<Map<String, Object>> getValueTypes() {
        return Arrays.stream(ValueType.values())
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("code", type.name());
                    typeInfo.put("description", type.getDescription());
                    typeInfo.put("comment", type.getComment());
                    return typeInfo;
                })
                .collect(Collectors.toList());
    }
}
