package net.lab1024.sa.config.service;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.config.domain.vo.ConfigItemVO;
import net.lab1024.sa.config.domain.vo.ConfigHistoryVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 配置管理服务
 * 提供配置的增删改查、版本管理、环境隔离等功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class ConfigManagementService {

    private final Map<String, Map<String, Object>> configStore = new HashMap<>();
    private final Map<Long, ConfigItemVO> configItems = new HashMap<>();
    private final List<ConfigHistoryVO> configHistory = new ArrayList<>();
    private Long configIdCounter = 1L;
    private Long historyIdCounter = 1L;

    // 初始化一些示例配置
    public ConfigManagementService() {
        initializeSampleConfigs();
    }

    /**
     * 获取所有配置项
     */
    public List<ConfigItemVO> getAllConfigs(String application, String profile, String label) {
        List<ConfigItemVO> result = new ArrayList<>();

        for (ConfigItemVO config : configItems.values()) {
            boolean matches = true;

            if (application != null && !application.isEmpty() && !application.equals(config.getApplication())) {
                matches = false;
            }
            if (profile != null && !profile.isEmpty() && !profile.equals(config.getProfile())) {
                matches = false;
            }
            if (label != null && !label.isEmpty() && !label.equals(config.getLabel())) {
                matches = false;
            }

            if (matches) {
                result.add(config);
            }
        }

        return result;
    }

    /**
     * 根据应用和环境获取配置
     */
    public Map<String, Object> getConfig(String application, String profile, String label) {
        String key = application + "-" + profile + "-" + label;
        return configStore.getOrDefault(key, new HashMap<>());
    }

    /**
     * 获取单个配置项
     */
    public ConfigItemVO getConfigById(Long configId) {
        return configItems.get(configId);
    }

    /**
     * 保存配置项
     */
    public synchronized Long saveConfig(ConfigItemVO configVO) {
        // 检查是否为更新
        ConfigItemVO existingConfig = configItems.get(configVO.getConfigId());

        if (existingConfig != null) {
            // 记录历史
            ConfigHistoryVO history = new ConfigHistoryVO();
            history.setHistoryId(historyIdCounter++);
            history.setConfigId(configVO.getConfigId());
            history.setConfigKey(configVO.getConfigKey());
            history.setOldValue(existingConfig.getConfigValue());
            history.setNewValue(configVO.getConfigValue());
            history.setChangeType("UPDATE");
            history.setChangedBy("admin");
            history.setChangeTime(LocalDateTime.now());
            history.setVersion(existingConfig.getVersion() + 1);
            configHistory.add(history);

            // 更新现有配置
            existingConfig.setConfigValue(configVO.getConfigValue());
            existingConfig.setDescription(configVO.getDescription());
            existingConfig.setUpdatedTime(LocalDateTime.now());
            existingConfig.setVersion(existingConfig.getVersion() + 1);

            // 更新配置存储
            updateConfigStore(existingConfig);
        } else {
            // 创建新配置
            configVO.setConfigId(configIdCounter++);
            configVO.setCreatedTime(LocalDateTime.now());
            configVO.setUpdatedTime(LocalDateTime.now());
            configVO.setVersion(1L);

            // 记录创建历史
            ConfigHistoryVO history = new ConfigHistoryVO();
            history.setHistoryId(historyIdCounter++);
            history.setConfigId(configVO.getConfigId());
            history.setConfigKey(configVO.getConfigKey());
            history.setOldValue(null);
            history.setNewValue(configVO.getConfigValue());
            history.setChangeType("CREATE");
            history.setChangedBy("admin");
            history.setChangeTime(LocalDateTime.now());
            history.setVersion(1L);
            configHistory.add(history);

            configItems.put(configVO.getConfigId(), configVO);
            addToConfigStore(configVO);
        }

        return configVO.getConfigId();
    }

    /**
     * 删除配置项
     */
    public synchronized void deleteConfig(Long configId) {
        ConfigItemVO config = configItems.get(configId);
        if (config != null) {
            // 记录删除历史
            ConfigHistoryVO history = new ConfigHistoryVO();
            history.setHistoryId(historyIdCounter++);
            history.setConfigId(configId);
            history.setConfigKey(config.getConfigKey());
            history.setOldValue(config.getConfigValue());
            history.setNewValue(null);
            history.setChangeType("DELETE");
            history.setChangedBy("admin");
            history.setChangeTime(LocalDateTime.now());
            history.setVersion(config.getVersion());
            configHistory.add(history);

            configItems.remove(configId);
            removeFromConfigStore(config);
        }
    }

    /**
     * 发布配置
     */
    public void publishConfigs(String application, String profile, List<Long> configIds) {
        log.info("发布配置到应用: {}, 环境: {}, 配置数量: {}", application, profile, configIds.size());

        // 这里应该触发配置推送到各个应用实例
        // 简化实现，只是记录发布日志
    }

    /**
     * 回滚配置
     */
    public void rollbackConfig(Long configId, Long targetVersion) {
        log.info("回滚配置 ID: {} 到版本: {}", configId, targetVersion);

        // 查找指定版本的历史记录
        ConfigHistoryVO targetHistory = null;
        for (ConfigHistoryVO history : configHistory) {
            if (history.getConfigId().equals(configId) &&
                history.getVersion().equals(targetVersion) &&
                "UPDATE".equals(history.getChangeType())) {
                targetHistory = history;
                break;
            }
        }

        if (targetHistory != null) {
            ConfigItemVO config = configItems.get(configId);
            if (config != null) {
                config.setConfigValue(targetHistory.getOldValue());
                config.setUpdatedTime(LocalDateTime.now());
                config.setVersion(config.getVersion() + 1);

                // 记录回滚历史
                ConfigHistoryVO rollbackHistory = new ConfigHistoryVO();
                rollbackHistory.setHistoryId(historyIdCounter++);
                rollbackHistory.setConfigId(configId);
                rollbackHistory.setConfigKey(config.getConfigKey());
                rollbackHistory.setOldValue(config.getConfigValue());
                rollbackHistory.setNewValue(targetHistory.getOldValue());
                rollbackHistory.setChangeType("ROLLBACK");
                rollbackHistory.setChangedBy("admin");
                rollbackHistory.setChangeTime(LocalDateTime.now());
                rollbackHistory.setVersion(config.getVersion());
                configHistory.add(rollbackHistory);

                updateConfigStore(config);
            }
        }
    }

    /**
     * 获取配置历史
     */
    public List<ConfigHistoryVO> getConfigHistory(Long configId) {
        List<ConfigHistoryVO> result = new ArrayList<>();
        for (ConfigHistoryVO history : configHistory) {
            if (history.getConfigId().equals(configId)) {
                result.add(history);
            }
        }
        return result;
    }

    /**
     * 比较配置版本
     */
    public Map<String, Object> compareConfigVersions(Long configId, Long version1, Long version2) {
        ConfigItemVO config = configItems.get(configId);
        if (config == null) {
            return Map.of("error", "配置不存在");
        }

        ConfigHistoryVO hist1 = null, hist2 = null;
        for (ConfigHistoryVO history : configHistory) {
            if (history.getConfigId().equals(configId)) {
                if (history.getVersion().equals(version1)) hist1 = history;
                if (history.getVersion().equals(version2)) hist2 = history;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("configId", configId);
        result.put("configKey", config.getConfigKey());
        result.put("version1", Map.of("version", version1, "value", hist1 != null ? hist1.getNewValue() : null));
        result.put("version2", Map.of("version", version2, "value", hist2 != null ? hist2.getNewValue() : null));
        result.put("different", !Objects.equals(
            hist1 != null ? hist1.getNewValue() : null,
            hist2 != null ? hist2.getNewValue() : null
        ));

        return result;
    }

    /**
     * 获取环境列表
     */
    public List<String> getEnvironments() {
        return Arrays.asList("dev", "test", "staging", "prod");
    }

    /**
     * 获取应用列表
     */
    public List<String> getApplications() {
        Set<String> applications = new HashSet<>();
        for (ConfigItemVO config : configItems.values()) {
            applications.add(config.getApplication());
        }
        return new ArrayList<>(applications);
    }

    /**
     * 搜索配置
     */
    public List<ConfigItemVO> searchConfigs(String keyword, String application, String profile) {
        List<ConfigItemVO> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (ConfigItemVO config : configItems.values()) {
            boolean matches = true;

            // 关键词匹配
            if (!config.getConfigKey().toLowerCase().contains(lowerKeyword) &&
                !config.getDescription().toLowerCase().contains(lowerKeyword)) {
                matches = false;
            }

            // 应用过滤
            if (application != null && !application.isEmpty() && !application.equals(config.getApplication())) {
                matches = false;
            }

            // 环境过滤
            if (profile != null && !profile.isEmpty() && !profile.equals(config.getProfile())) {
                matches = false;
            }

            if (matches) {
                result.add(config);
            }
        }

        return result;
    }

    /**
     * 获取总配置数量
     */
    public Integer getTotalConfigCount() {
        return configItems.size();
    }

    /**
     * 获取活跃环境
     */
    public List<String> getActiveProfiles() {
        return Arrays.asList("dev", "test", "prod");
    }

    /**
     * 初始化示例配置
     */
    private void initializeSampleConfigs() {
        String[] applications = {"ioedream-auth-service", "ioedream-access-service", "ioedream-consume-service"};
        String[] profiles = {"dev", "test", "prod"};
        String[] keys = {"database.url", "redis.host", "cache.ttl", "log.level"};

        for (String app : applications) {
            for (String profile : profiles) {
                for (String key : keys) {
                    ConfigItemVO config = new ConfigItemVO();
                    config.setConfigId(configIdCounter++);
                    config.setApplication(app);
                    config.setProfile(profile);
                    config.setLabel("master");
                    config.setConfigKey(key);
                    config.setConfigValue(generateConfigValue(key, profile));
                    config.setDescription("自动生成的" + key + "配置");
                    config.setCreatedTime(LocalDateTime.now());
                    config.setUpdatedTime(LocalDateTime.now());
                    config.setVersion(1L);

                    configItems.put(config.getConfigId(), config);
                    addToConfigStore(config);
                }
            }
        }
    }

    /**
     * 生成配置值
     */
    private String generateConfigValue(String key, String profile) {
        switch (key) {
            case "database.url":
                return "jdbc:mysql://localhost:3306/ioedream_" + profile + "?useUnicode=true&characterEncoding=utf8";
            case "redis.host":
                return "localhost";
            case "cache.ttl":
                return profile.equals("prod") ? "3600" : "600";
            case "log.level":
                return profile.equals("prod") ? "WARN" : "DEBUG";
            default:
                return "default-value";
        }
    }

    /**
     * 添加到配置存储
     */
    private void addToConfigStore(ConfigItemVO config) {
        String key = config.getApplication() + "-" + config.getProfile() + "-" + config.getLabel();
        configStore.computeIfAbsent(key, k -> new HashMap<>()).put(config.getConfigKey(), config.getConfigValue());
    }

    /**
     * 更新配置存储
     */
    private void updateConfigStore(ConfigItemVO config) {
        String key = config.getApplication() + "-" + config.getProfile() + "-" + config.getLabel();
        Map<String, Object> appConfig = configStore.get(key);
        if (appConfig != null) {
            appConfig.put(config.getConfigKey(), config.getConfigValue());
        }
    }

    /**
     * 从配置存储移除
     */
    private void removeFromConfigStore(ConfigItemVO config) {
        String key = config.getApplication() + "-" + config.getProfile() + "-" + config.getLabel();
        Map<String, Object> appConfig = configStore.get(key);
        if (appConfig != null) {
            appConfig.remove(config.getConfigKey());
            if (appConfig.isEmpty()) {
                configStore.remove(key);
            }
        }
    }
}