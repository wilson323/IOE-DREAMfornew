package net.lab1024.sa.common.notification.service.impl;

import lombok.extern.slf4j.Slf4j;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitoring.AlertManager;
import net.lab1024.sa.common.notification.dao.NotificationConfigDao;
import net.lab1024.sa.common.notification.domain.entity.NotificationConfigEntity;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigAddForm;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigQueryForm;
import net.lab1024.sa.common.notification.domain.form.NotificationConfigUpdateForm;
import net.lab1024.sa.common.notification.domain.vo.NotificationConfigVO;
import net.lab1024.sa.common.notification.manager.NotificationConfigManager;
import net.lab1024.sa.common.notification.service.NotificationConfigService;
import net.lab1024.sa.common.util.AESUtil;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;

/**
 * 通知配置服务实现类
 * <p>
 * 提供通知配置的CRUD操作和配置管理功能
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解标识服务实现
 * - 使用@Resource依赖注入（符合架构规范）
 * - 使用@Transactional管理事务
 * - 完整的异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class NotificationConfigServiceImpl implements NotificationConfigService {


    @Resource
    private NotificationConfigDao notificationConfigDao;

    @Resource
    private NotificationConfigManager notificationConfigManager;

    @Resource
    private AESUtil aesUtil;

    /**
     * AlertManager（可选依赖）
     * <p>
     * 如果AlertManager未配置，则alertManager为null
     * 在调用前需要检查是否为null
     * 使用@Resource注入，如果不存在则保持为null（不会抛出异常）
     * </p>
     */
    @Resource
    private AlertManager alertManager;

    /**
     * 新增通知配置
     * <p>
     * 功能说明：
     * 1. 验证配置键唯一性
     * 2. 如果isEncrypted=1，加密配置值
     * 3. 保存到数据库
     * 4. 清除相关缓存（使用@CacheEvict注解）
     * 5. 触发AlertManager配置刷新（如果是告警配置）
     * </p>
     *
     * @param form 新增表单
     * @return 响应结果
     */
    @Override
    @Observed(name = "notification.config.add", contextualName = "notification-config-add")
    @CacheEvict(value = {"notification:config:value", "notification:config:type"}, allEntries = true)
    public ResponseDTO<Long> add(NotificationConfigAddForm form) {
        log.info("[通知配置] 新增通知配置，configKey={}", form.getConfigKey());

        try {
            // 1. 验证配置键唯一性
            NotificationConfigEntity existingConfig = notificationConfigDao.selectByConfigKey(form.getConfigKey());
            if (existingConfig != null) {
                log.warn("[通知配置] 配置键已存在，configKey={}", form.getConfigKey());
                return ResponseDTO.error("CONFIG_KEY_EXISTS", "配置键已存在");
            }

            // 2. 构建实体对象
            NotificationConfigEntity entity = new NotificationConfigEntity();
            entity.setConfigKey(form.getConfigKey());
            entity.setConfigType(form.getConfigType());
            entity.setConfigDesc(form.getConfigDesc());
            entity.setIsEncrypted(form.getIsEncrypted());
            entity.setStatus(form.getStatus());

            // 3. 如果isEncrypted=1，加密配置值
            String configValue = form.getConfigValue();
            if (form.getIsEncrypted() != null && form.getIsEncrypted() == 1) {
                try {
                    configValue = aesUtil.encrypt(configValue);
                    log.debug("[通知配置] 配置值已加密，configKey={}", form.getConfigKey());
                } catch (IllegalArgumentException | ParamException e) {
                    log.warn("[通知配置] 配置值加密参数异常，configKey={}, error={}", form.getConfigKey(), e.getMessage());
                    return ResponseDTO.error("ENCRYPT_PARAM_ERROR", "配置值加密参数错误: " + e.getMessage());
                } catch (Exception e) {
                    log.error("[通知配置] 配置值加密系统异常，configKey={}", form.getConfigKey(), e);
                    return ResponseDTO.error("ENCRYPT_FAILED", "配置值加密失败，请稍后重试");
                }
            }
            entity.setConfigValue(configValue);

            // 4. 保存到数据库
            int result = notificationConfigDao.insert(entity);
            if (result <= 0) {
                log.error("[通知配置] 保存配置失败，configKey={}", form.getConfigKey());
                return ResponseDTO.error("SAVE_FAILED", "保存配置失败");
            }

            // 5. 刷新AlertManager配置（如果需要）
            // 注意：缓存清除由@CacheEvict注解自动处理
            refreshAlertConfigIfNeeded(form.getConfigKey());

            log.info("[通知配置] 新增通知配置成功，configId={}, configKey={}", entity.getId(), form.getConfigKey());
            return ResponseDTO.ok(entity.getId());

        } catch (BusinessException e) {
            log.warn("[通知配置] 新增通知配置业务异常，configKey={}, error={}", form.getConfigKey(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[通知配置] 新增通知配置参数异常，configKey={}, error={}", form.getConfigKey(), e.getMessage());
            return ResponseDTO.error("NOTIFICATION_CONFIG_ADD_PARAM_ERROR", "新增配置参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[通知配置] 新增通知配置系统异常，configKey={}", form.getConfigKey(), e);
            return ResponseDTO.error("NOTIFICATION_CONFIG_ADD_ERROR", "新增配置失败，请稍后重试");
        }
    }

    /**
     * 更新通知配置
     * <p>
     * 功能说明：
     * 1. 验证配置是否存在
     * 2. 如果isEncrypted=1，加密配置值
     * 3. 更新到数据库
     * 4. 清除相关缓存（使用@CacheEvict注解）
     * 5. 触发AlertManager配置刷新（如果是告警配置）
     * </p>
     *
     * @param form 更新表单
     * @return 响应结果
     */
    @Override
    @Observed(name = "notification.config.update", contextualName = "notification-config-update")
    @CacheEvict(value = {"notification:config:value", "notification:config:type"}, allEntries = true)
    public ResponseDTO<Void> update(NotificationConfigUpdateForm form) {
        log.info("[通知配置] 更新通知配置，configId={}", form.getConfigId());

        try {
            // 1. 验证配置是否存在
            NotificationConfigEntity entity = notificationConfigDao.selectById(form.getConfigId());
            if (entity == null) {
                log.warn("[通知配置] 配置不存在，configId={}", form.getConfigId());
                return ResponseDTO.error("CONFIG_NOT_FOUND", "配置不存在");
            }

            // 2. 更新配置值（如果提供）
            if (form.getConfigValue() != null) {
                String configValue = form.getConfigValue();
                // 如果配置需要加密，先加密
                if (entity.getIsEncrypted() != null && entity.getIsEncrypted() == 1) {
                    try {
                        configValue = aesUtil.encrypt(configValue);
                        log.debug("[通知配置] 配置值已加密，configId={}", form.getConfigId());
                    } catch (IllegalArgumentException | ParamException e) {
                        log.warn("[通知配置] 配置值加密参数异常，configId={}, error={}", form.getConfigId(), e.getMessage());
                        return ResponseDTO.error("ENCRYPT_PARAM_ERROR", "配置值加密参数错误: " + e.getMessage());
                    } catch (Exception e) {
                        log.error("[通知配置] 配置值加密系统异常，configId={}", form.getConfigId(), e);
                        return ResponseDTO.error("ENCRYPT_FAILED", "配置值加密失败，请稍后重试");
                    }
                }
                int result = notificationConfigDao.updateConfigValue(entity.getId(), configValue);
                if (result <= 0) {
                    log.error("[通知配置] 更新配置值失败，configId={}", form.getConfigId());
                    return ResponseDTO.error("UPDATE_FAILED", "更新配置值失败");
                }
            }

            // 3. 更新配置描述（如果提供）
            if (form.getConfigDesc() != null) {
                entity.setConfigDesc(form.getConfigDesc());
            }

            // 4. 更新配置状态（如果提供）
            if (form.getStatus() != null) {
                int result = notificationConfigDao.updateConfigStatus(entity.getId(), form.getStatus());
                if (result <= 0) {
                    log.error("[通知配置] 更新配置状态失败，configId={}", form.getConfigId());
                    return ResponseDTO.error("UPDATE_STATUS_FAILED", "更新配置状态失败");
                }
            }

            // 5. 刷新AlertManager配置（如果需要）
            // 注意：缓存清除由@CacheEvict注解自动处理
            refreshAlertConfigIfNeeded(entity.getConfigKey());

            log.info("[通知配置] 更新通知配置成功，configId={}", form.getConfigId());
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[通知配置] 更新通知配置业务异常，configId={}, error={}", form.getConfigId(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[通知配置] 更新通知配置参数异常，configId={}, error={}", form.getConfigId(), e.getMessage());
            return ResponseDTO.error("NOTIFICATION_CONFIG_UPDATE_PARAM_ERROR", "更新配置参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[通知配置] 更新通知配置系统异常，configId={}", form.getConfigId(), e);
            return ResponseDTO.error("NOTIFICATION_CONFIG_UPDATE_ERROR", "更新配置失败，请稍后重试");
        }
    }

    /**
     * 删除通知配置（软删除）
     * <p>
     * 功能说明：
     * 1. 验证配置是否存在
     * 2. 执行软删除（deleted_flag=1）
     * 3. 清除相关缓存（使用@CacheEvict注解）
     * 4. 触发AlertManager配置刷新（如果是告警配置）
     * </p>
     *
     * @param configId 配置ID
     * @return 响应结果
     */
    @Override
    @Observed(name = "notification.config.delete", contextualName = "notification-config-delete")
    @CacheEvict(value = {"notification:config:value", "notification:config:type"}, allEntries = true)
    public ResponseDTO<Void> delete(Long configId) {
        log.info("[通知配置] 删除通知配置，configId={}", configId);

        try {
            // 1. 验证配置是否存在
            NotificationConfigEntity entity = notificationConfigDao.selectById(configId);
            if (entity == null) {
                log.warn("[通知配置] 配置不存在，configId={}", configId);
                return ResponseDTO.error("CONFIG_NOT_FOUND", "配置不存在");
            }

            // 2. 执行软删除
            int result = notificationConfigDao.deleteById(configId);
            if (result <= 0) {
                log.error("[通知配置] 删除配置失败，configId={}", configId);
                return ResponseDTO.error("DELETE_FAILED", "删除配置失败");
            }

            // 3. 刷新AlertManager配置（如果需要）
            // 注意：缓存清除由@CacheEvict注解自动处理
            refreshAlertConfigIfNeeded(entity.getConfigKey());

            log.info("[通知配置] 删除通知配置成功，configId={}", configId);
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[通知配置] 删除通知配置业务异常，configId={}, error={}", configId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[通知配置] 删除通知配置参数异常，configId={}, error={}", configId, e.getMessage());
            return ResponseDTO.error("NOTIFICATION_CONFIG_DELETE_PARAM_ERROR", "删除配置参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[通知配置] 删除通知配置系统异常，configId={}", configId, e);
            return ResponseDTO.error("NOTIFICATION_CONFIG_DELETE_ERROR", "删除配置失败，请稍后重试");
        }
    }

    /**
     * 根据ID查询通知配置
     * <p>
     * 功能说明：
     * 1. 从数据库查询配置
     * 2. 如果isEncrypted=1，解密配置值
     * 3. 敏感信息脱敏处理
     * 4. 转换为VO返回
     * </p>
     *
     * @param configId 配置ID
     * @return 响应结果
     */
    @Override
    @Observed(name = "notification.config.getById", contextualName = "notification-config-get-by-id")
    @Transactional(readOnly = true)
    public ResponseDTO<NotificationConfigVO> getById(Long configId) {
        log.debug("[通知配置] 根据ID查询通知配置，configId={}", configId);

        try {
            // 1. 从数据库查询配置
            NotificationConfigEntity entity = notificationConfigDao.selectById(configId);
            if (entity == null) {
                log.warn("[通知配置] 配置不存在，configId={}", configId);
                return ResponseDTO.error("CONFIG_NOT_FOUND", "配置不存在");
            }

            // 2. 转换为VO
            NotificationConfigVO vo = convertToVO(entity);

            log.debug("[通知配置] 根据ID查询通知配置成功，configId={}", configId);
            return ResponseDTO.ok(vo);

        } catch (BusinessException e) {
            log.warn("[通知配置] 根据ID查询通知配置业务异常，configId={}, error={}", configId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[通知配置] 根据ID查询通知配置参数异常，configId={}, error={}", configId, e.getMessage());
            return ResponseDTO.error("NOTIFICATION_CONFIG_QUERY_PARAM_ERROR", "查询配置参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[通知配置] 根据ID查询通知配置系统异常，configId={}", configId, e);
            return ResponseDTO.error("NOTIFICATION_CONFIG_QUERY_ERROR", "查询配置失败，请稍后重试");
        }
    }

    /**
     * 根据配置键查询通知配置
     * <p>
     * 功能说明：
     * 1. 从缓存或数据库查询配置
     * 2. 如果isEncrypted=1，解密配置值
     * 3. 敏感信息脱敏处理
     * 4. 转换为VO返回
     * </p>
     *
     * @param configKey 配置键
     * @return 响应结果
     */
    @Override
    @Observed(name = "notification.config.getByConfigKey", contextualName = "notification-config-get-by-key")
    @Transactional(readOnly = true)
    public ResponseDTO<NotificationConfigVO> getByConfigKey(String configKey) {
        log.debug("[通知配置] 根据配置键查询通知配置，configKey={}", configKey);

        try {
            // 1. 从数据库查询配置
            NotificationConfigEntity entity = notificationConfigDao.selectByConfigKey(configKey);
            if (entity == null) {
                log.warn("[通知配置] 配置不存在，configKey={}", configKey);
                return ResponseDTO.error("CONFIG_NOT_FOUND", "配置不存在");
            }

            // 2. 转换为VO
            NotificationConfigVO vo = convertToVO(entity);

            log.debug("[通知配置] 根据配置键查询通知配置成功，configKey={}", configKey);
            return ResponseDTO.ok(vo);

        } catch (BusinessException e) {
            log.warn("[通知配置] 根据配置键查询通知配置业务异常，configKey={}, error={}", configKey, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[通知配置] 根据配置键查询通知配置参数异常，configKey={}, error={}", configKey, e.getMessage());
            return ResponseDTO.error("NOTIFICATION_CONFIG_QUERY_PARAM_ERROR", "查询配置参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[通知配置] 根据配置键查询通知配置系统异常，configKey={}", configKey, e);
            return ResponseDTO.error("NOTIFICATION_CONFIG_QUERY_ERROR", "查询配置失败，请稍后重试");
        }
    }

    /**
     * 分页查询通知配置
     * <p>
     * 功能说明：
     * 1. 根据查询条件构建查询条件
     * 2. 执行分页查询
     * 3. 如果isEncrypted=1，解密配置值
     * 4. 敏感信息脱敏处理
     * 5. 转换为VO列表返回
     * </p>
     *
     * @param form 查询表单
     * @return 响应结果
     */
    @Override
    @Observed(name = "notification.config.queryPage", contextualName = "notification-config-query-page")
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<NotificationConfigVO>> queryPage(NotificationConfigQueryForm form) {
        log.info("[通知配置] 分页查询通知配置，pageNum={}, pageSize={}", form.getPageNum(), form.getPageSize());

        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<NotificationConfigEntity> wrapper = new LambdaQueryWrapper<>();

            // 配置键模糊查询
            if (StringUtils.hasText(form.getConfigKey())) {
                wrapper.like(NotificationConfigEntity::getConfigKey, form.getConfigKey().trim());
            }

            // 配置类型查询
            if (StringUtils.hasText(form.getConfigType())) {
                wrapper.eq(NotificationConfigEntity::getConfigType, form.getConfigType().trim());
            }

            // 状态查询
            if (form.getStatus() != null) {
                wrapper.eq(NotificationConfigEntity::getStatus, form.getStatus());
            }

            // 软删除过滤
            wrapper.eq(NotificationConfigEntity::getDeletedFlag, 0);

            // 排序：按创建时间倒序
            wrapper.orderByDesc(NotificationConfigEntity::getCreateTime);

            // 2. 执行分页查询
            Page<NotificationConfigEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
            Page<NotificationConfigEntity> pageResult = notificationConfigDao.selectPage(page, wrapper);

            // 3. 转换为VO列表
            List<NotificationConfigVO> voList = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 4. 构建分页结果
            PageResult<NotificationConfigVO> result = PageResult.of(
                    voList,
                    pageResult.getTotal(),
                    form.getPageNum(),
                    form.getPageSize()
            );

            log.info("[通知配置] 分页查询通知配置成功，记录数={}", voList.size());
            return ResponseDTO.ok(result);

        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[通知配置] 分页查询通知配置参数异常，error={}", e.getMessage());
            return ResponseDTO.error("NOTIFICATION_CONFIG_QUERY_PARAM_ERROR", "查询配置参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[通知配置] 分页查询通知配置系统异常", e);
            return ResponseDTO.error("NOTIFICATION_CONFIG_QUERY_ERROR", "查询配置失败，请稍后重试");
        }
    }

    /**
     * 更新配置状态（启用/禁用）
     * <p>
     * 功能说明：
     * 1. 验证配置是否存在
     * 2. 更新配置状态
     * 3. 清除相关缓存（使用@CacheEvict注解）
     * 4. 触发AlertManager配置刷新（如果是告警配置）
     * </p>
     *
     * @param configId 配置ID
     * @param status   状态（1-启用 2-禁用）
     * @return 响应结果
     */
    @Override
    @Observed(name = "notification.config.updateStatus", contextualName = "notification-config-update-status")
    @CacheEvict(value = {"notification:config:value", "notification:config:type"}, allEntries = true)
    public ResponseDTO<Void> updateStatus(Long configId, Integer status) {
        log.info("[通知配置] 更新配置状态，configId={}, status={}", configId, status);

        try {
            // 1. 验证配置是否存在
            NotificationConfigEntity entity = notificationConfigDao.selectById(configId);
            if (entity == null) {
                log.warn("[通知配置] 配置不存在，configId={}", configId);
                return ResponseDTO.error("CONFIG_NOT_FOUND", "配置不存在");
            }

            // 2. 验证状态值
            if (status == null || (status != 1 && status != 2)) {
                log.warn("[通知配置] 状态值无效，configId={}, status={}", configId, status);
                return ResponseDTO.error("INVALID_STATUS", "状态值无效，必须为1（启用）或2（禁用）");
            }

            // 3. 更新配置状态
            int result = notificationConfigDao.updateConfigStatus(configId, status);
            if (result <= 0) {
                log.error("[通知配置] 更新配置状态失败，configId={}, status={}", configId, status);
                return ResponseDTO.error("UPDATE_STATUS_FAILED", "更新配置状态失败");
            }

            // 4. 刷新AlertManager配置（如果需要）
            // 注意：缓存清除由@CacheEvict注解自动处理
            refreshAlertConfigIfNeeded(entity.getConfigKey());

            log.info("[通知配置] 更新配置状态成功，configId={}, status={}", configId, status);
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[通知配置] 更新配置状态业务异常，configId={}, status={}, error={}", configId, status, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[通知配置] 更新配置状态参数异常，configId={}, status={}, error={}", configId, status, e.getMessage());
            return ResponseDTO.error("NOTIFICATION_CONFIG_UPDATE_STATUS_PARAM_ERROR", "更新配置状态参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[通知配置] 更新配置状态系统异常，configId={}, status={}", configId, status, e);
            return ResponseDTO.error("NOTIFICATION_CONFIG_UPDATE_STATUS_ERROR", "更新配置状态失败，请稍后重试");
        }
    }

    /**
     * 刷新AlertManager配置
     * <p>
     * 功能说明：
     * 1. 调用AlertManager的refreshAlertConfig方法
     * 2. 使配置修改立即生效，无需重启服务
     * </p>
     *
     * @return 响应结果
     */
    @Override
    @Observed(name = "notification.config.refreshAlertConfig", contextualName = "notification-config-refresh-alert")
    public ResponseDTO<Void> refreshAlertConfig() {
        log.info("[通知配置] 刷新AlertManager配置");

        try {
            if (alertManager == null) {
                log.warn("[通知配置] AlertManager未配置，无法刷新配置");
                return ResponseDTO.error("ALERT_MANAGER_NOT_CONFIGURED", "AlertManager未配置");
            }

            // 调用AlertManager的refreshAlertConfig方法
            alertManager.refreshAlertConfig();

            log.info("[通知配置] AlertManager配置刷新成功");
            return ResponseDTO.ok();

        } catch (BusinessException e) {
            log.warn("[通知配置] AlertManager配置刷新业务异常，error={}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[通知配置] AlertManager配置刷新系统异常", e);
            return ResponseDTO.error("NOTIFICATION_CONFIG_REFRESH_ERROR", "刷新配置失败，请稍后重试");
        }
    }

    /**
     * 将Entity转换为VO
     * <p>
     * 功能说明：
     * 1. 如果isEncrypted=1，解密配置值
     * 2. 敏感信息脱敏处理
     * 3. 转换为VO对象
     * </p>
     *
     * @param entity 配置实体
     * @return 配置VO
     */
    private NotificationConfigVO convertToVO(NotificationConfigEntity entity) {
        NotificationConfigVO vo = new NotificationConfigVO();
        vo.setConfigId(entity.getId());
        vo.setConfigKey(entity.getConfigKey());
        vo.setConfigType(entity.getConfigType());
        vo.setConfigDesc(entity.getConfigDesc());
        vo.setIsEncrypted(entity.getIsEncrypted());
        vo.setStatus(entity.getStatus());

        // 如果isEncrypted=1，解密配置值
        String configValue = entity.getConfigValue();
        if (entity.getIsEncrypted() != null && entity.getIsEncrypted() == 1) {
            try {
                configValue = aesUtil.decrypt(configValue);
                log.debug("[通知配置] 配置值已解密，configKey={}", entity.getConfigKey());
            } catch (IllegalArgumentException | ParamException e) {
                log.warn("[通知配置] 配置值解密参数异常，configKey={}, error={}", entity.getConfigKey(), e.getMessage());
                configValue = "***解密失败***";
            } catch (Exception e) {
                log.error("[通知配置] 配置值解密系统异常，configKey={}", entity.getConfigKey(), e);
                configValue = "***解密失败***";
            }
        }

        // 敏感信息脱敏处理（密码、密钥等）
        configValue = maskSensitiveValue(entity.getConfigKey(), configValue);
        vo.setConfigValue(configValue);

        return vo;
    }

    /**
     * 敏感信息脱敏处理
     * <p>
     * 功能说明：
     * 1. 识别敏感配置键（包含PASSWORD、SECRET、KEY等关键词）
     * 2. 对敏感值进行脱敏处理（只显示前2位和后2位，中间用*代替）
     * </p>
     *
     * @param configKey   配置键
     * @param configValue 配置值
     * @return 脱敏后的配置值
     */
    private String maskSensitiveValue(String configKey, String configValue) {
        if (configValue == null || configValue.isEmpty()) {
            return configValue;
        }

        // 识别敏感配置键
        String upperKey = configKey.toUpperCase();
        boolean isSensitive = upperKey.contains("PASSWORD") ||
                upperKey.contains("SECRET") ||
                upperKey.contains("KEY") ||
                upperKey.contains("TOKEN");

        if (!isSensitive) {
            return configValue;
        }

        // 脱敏处理：只显示前2位和后2位，中间用*代替
        if (configValue.length() <= 4) {
            return "****";
        }

        return configValue.substring(0, 2) + "****" + configValue.substring(configValue.length() - 2);
    }

    /**
     * 根据配置键获取配置值（带缓存）
     * <p>
     * 使用Spring Cache的@Cacheable注解管理缓存
     * 缓存策略：缓存名称=notification:config:value，缓存键=configKey
     * </p>
     *
     * @param configKey 配置键
     * @return 配置值（已解密），如果配置不存在或已禁用则返回null
     */
    @Override
    @Observed(name = "notification.config.getConfigValue", contextualName = "notification-config-get-value")
    @Cacheable(value = "notification:config:value", key = "#configKey", unless = "#result == null")
    @Transactional(readOnly = true)
    public String getConfigValue(String configKey) {
        return notificationConfigManager.getConfigValue(configKey);
    }

    /**
     * 根据配置类型获取所有配置（带缓存）
     * <p>
     * 使用Spring Cache的@Cacheable注解管理缓存
     * 缓存策略：缓存名称=notification:config:type，缓存键=configType
     * </p>
     *
     * @param configType 配置类型
     * @return 配置Map（key为configKey，value为configValue）
     */
    @Override
    @Observed(name = "notification.config.getConfigsByType", contextualName = "notification-config-get-by-type")
    @Cacheable(value = "notification:config:type", key = "#configType", unless = "#result == null || #result.isEmpty()")
    @Transactional(readOnly = true)
    public java.util.Map<String, String> getConfigsByType(String configType) {
        return notificationConfigManager.getConfigsByType(configType);
    }

    /**
     * 刷新AlertManager配置（如果需要）
     * <p>
     * 功能说明：
     * 1. 如果配置键以"ALERT."开头且AlertManager已配置，则刷新AlertManager配置
     * 2. 配置刷新失败不影响主流程
     * </p>
     * <p>
     * 注意：缓存清除由@CacheEvict注解自动处理，此方法只处理AlertManager配置刷新
     * </p>
     *
     * @param configKey 配置键
     */
    private void refreshAlertConfigIfNeeded(String configKey) {
        // 如果是告警配置，触发AlertManager配置刷新
        if (configKey != null && configKey.startsWith("ALERT.") && alertManager != null) {
            try {
                alertManager.refreshAlertConfig();
                log.info("[通知配置] AlertManager配置已刷新，configKey={}", configKey);
            } catch (BusinessException e) {
                log.warn("[通知配置] AlertManager配置刷新业务异常，configKey={}, error={}", configKey, e.getMessage());
                // 配置刷新失败不影响主流程
            } catch (Exception e) {
                log.warn("[通知配置] AlertManager配置刷新系统异常，configKey={}", configKey, e);
                // 配置刷新失败不影响主流程
            }
        }
    }
}

