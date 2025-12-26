package net.lab1024.sa.common.notification.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.common.notification.dao.NotificationConfigDao;
import net.lab1024.sa.common.notification.domain.entity.NotificationConfigEntity;
import net.lab1024.sa.common.util.AESUtil;

public class NotificationConfigManager {

    private final NotificationConfigDao notificationConfigDao;
    private final ObjectMapper objectMapper;
    private final AESUtil aesUtil;

    public NotificationConfigManager(NotificationConfigDao dao, ObjectMapper mapper, AESUtil util) {
        this.notificationConfigDao = dao;
        this.objectMapper = mapper;
        this.aesUtil = util;
    }

    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }

    public String getConfigValue(String configKey, String defaultValue) {
        String value = resolveValue(notificationConfigDao.selectByConfigKey(configKey));
        return value != null ? value : defaultValue;
    }

    public Map<String, String> getConfigsByType(String configType) {
        List<NotificationConfigEntity> entities = notificationConfigDao.selectByConfigType(configType);
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (NotificationConfigEntity entity : entities) {
            result.put(entity.getConfigKey(), resolveValue(entity));
        }
        return result;
    }

    public <T> T getConfigObject(String configKey, Class<T> targetClass) {
        String value = getConfigValue(configKey);
        if (value == null || objectMapper == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, targetClass);
        } catch (Exception ex) {
            throw new IllegalArgumentException("解析配置失败: " + configKey, ex);
        }
    }

    public <T> T getConfigObject(String configKey, TypeReference<T> typeReference) {
        String value = getConfigValue(configKey);
        if (value == null || objectMapper == null) {
            return null;
        }
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (Exception ex) {
            throw new IllegalArgumentException("解析配置失败: " + configKey, ex);
        }
    }

    public boolean updateConfigValue(String configKey, String configValue) {
        NotificationConfigEntity entity = notificationConfigDao.selectByConfigKey(configKey);
        if (entity == null) {
            return false;
        }
        String value = resolveStoreValue(entity, configValue);
        return notificationConfigDao.updateConfigValue(entity.getId(), value) > 0;
    }

    public NotificationConfigEntity getConfigEntity(String configKey) {
        return notificationConfigDao.selectByConfigKey(configKey);
    }

    public List<NotificationConfigEntity> getConfigEntitiesByType(String configType) {
        return notificationConfigDao.selectByConfigType(configType);
    }

    private String resolveValue(NotificationConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        String value = entity.getConfigValue();
        Integer encrypted = entity.getIsEncrypted();
        if (encrypted != null && encrypted == 1 && value != null) {
            return AESUtil.decrypt(value);
        }
        return value;
    }

    private String resolveStoreValue(NotificationConfigEntity entity, String value) {
        Integer encrypted = entity.getIsEncrypted();
        if (encrypted != null && encrypted == 1 && value != null) {
            return AESUtil.encrypt(value);
        }
        return value;
    }
}
