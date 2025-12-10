package net.lab1024.sa.common.workflow.manager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.workflow.dao.ApprovalConfigDao;
import net.lab1024.sa.common.workflow.entity.ApprovalConfigEntity;

/**
 * 审批配置管理Manager
 * <p>
 * 负责审批配置的查询和管理
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 根据业务类型查询审批配置
 * - 获取流程定义ID（支持动态配置）
 * - 验证审批规则
 * - 获取审批后处理配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class ApprovalConfigManager {

    private final ApprovalConfigDao approvalConfigDao;

    /**
     * 构造函数注入依赖
     *
     * @param approvalConfigDao 审批配置DAO
     */
    public ApprovalConfigManager(ApprovalConfigDao approvalConfigDao) {
        this.approvalConfigDao = approvalConfigDao;
    }

    /**
     * 根据业务类型获取审批配置
     * <p>
     * 支持自定义业务类型，不局限于枚举
     * </p>
     *
     * @param businessType 业务类型
     * @return 审批配置实体，如果不存在则返回null
     */
    public ApprovalConfigEntity getApprovalConfig(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            log.warn("[审批配置] 业务类型为空，无法查询审批配置");
            return null;
        }

        try {
            ApprovalConfigEntity config = approvalConfigDao.selectByBusinessType(businessType);
            if (config != null) {
                // 检查配置是否在有效期内
                if (isConfigValid(config)) {
                    log.debug("[审批配置] 查询审批配置成功，businessType={}, definitionId={}", businessType, config.getDefinitionId());
                    return config;
                } else {
                    log.warn("[审批配置] 审批配置已过期或未生效，businessType={}", businessType);
                    return null;
                }
            } else {
                log.debug("[审批配置] 未找到审批配置，businessType={}，将使用默认配置", businessType);
                return null;
            }
        } catch (Exception e) {
            log.error("[审批配置] 查询审批配置异常，businessType={}", businessType, e);
            return null;
        }
    }

    /**
     * 根据业务类型获取流程定义ID
     * <p>
     * 优先从审批配置中获取，如果配置不存在则返回null（由调用方决定使用默认流程）
     * </p>
     *
     * @param businessType 业务类型
     * @return 流程定义ID，如果不存在则返回null
     */
    public Long getDefinitionId(String businessType) {
        ApprovalConfigEntity config = getApprovalConfig(businessType);
        if (config != null && config.getDefinitionId() != null) {
            return config.getDefinitionId();
        }
        return null;
    }

    /**
     * 根据业务类型和模块获取审批配置
     *
     * @param businessType 业务类型
     * @param module 所属模块
     * @return 审批配置实体
     */
    public ApprovalConfigEntity getApprovalConfig(String businessType, String module) {
        if (businessType == null || businessType.trim().isEmpty()) {
            log.warn("[审批配置] 业务类型为空，无法查询审批配置");
            return null;
        }

        try {
            ApprovalConfigEntity config = approvalConfigDao.selectByBusinessTypeAndModule(businessType, module);
            if (config != null && isConfigValid(config)) {
                log.debug("[审批配置] 查询审批配置成功，businessType={}, module={}, definitionId={}",
                        businessType, module, config.getDefinitionId());
                return config;
            } else {
                log.debug("[审批配置] 未找到审批配置，businessType={}, module={}", businessType, module);
                return null;
            }
        } catch (Exception e) {
            log.error("[审批配置] 查询审批配置异常，businessType={}, module={}", businessType, module, e);
            return null;
        }
    }

    /**
     * 检查审批配置是否有效
     * <p>
     * 检查配置状态、生效时间、失效时间
     * </p>
     *
     * @param config 审批配置实体
     * @return 有效返回true，否则返回false
     */
    public boolean isConfigValid(ApprovalConfigEntity config) {
        if (config == null) {
            return false;
        }

        // 检查状态
        if (!"ENABLED".equals(config.getStatus())) {
            log.debug("[审批配置] 审批配置未启用，businessType={}", config.getBusinessType());
            return false;
        }

        // 检查生效时间
        LocalDateTime now = LocalDateTime.now();
        if (config.getEffectiveTime() != null && now.isBefore(config.getEffectiveTime())) {
            log.debug("[审批配置] 审批配置未生效，businessType={}, effectiveTime={}", config.getBusinessType(), config.getEffectiveTime());
            return false;
        }

        // 检查失效时间
        if (config.getExpireTime() != null && now.isAfter(config.getExpireTime())) {
            log.debug("[审批配置] 审批配置已失效，businessType={}, expireTime={}", config.getBusinessType(), config.getExpireTime());
            return false;
        }

        return true;
    }

    /**
     * 检查业务类型是否存在配置
     *
     * @param businessType 业务类型
     * @return 存在返回true，否则返回false
     */
    public boolean existsConfig(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return false;
        }

        try {
            int count = approvalConfigDao.existsByBusinessType(businessType);
            return count > 0;
        } catch (Exception e) {
            log.error("[审批配置] 检查业务类型配置异常，businessType={}", businessType, e);
            return false;
        }
    }

    /**
     * 解析审批规则
     * <p>
     * 将JSON格式的审批规则解析为Map
     * </p>
     *
     * @param config 审批配置实体
     * @return 审批规则Map，如果解析失败则返回空Map
     */
    public Map<String, Object> parseApprovalRules(ApprovalConfigEntity config) {
        Map<String, Object> rules = new HashMap<>();
        if (config == null || config.getApprovalRules() == null || config.getApprovalRules().trim().isEmpty()) {
            return rules;
        }

        try {
            // 使用Jackson解析JSON
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsedRules = objectMapper.readValue(config.getApprovalRules(), Map.class);
            rules = parsedRules;
            log.debug("[审批配置] 解析审批规则成功，businessType={}", config.getBusinessType());
        } catch (Exception e) {
            log.error("[审批配置] 解析审批规则异常，businessType={}", config.getBusinessType(), e);
        }

        return rules;
    }

    /**
     * 解析审批后处理配置
     *
     * @param config 审批配置实体
     * @return 审批后处理配置Map
     */
    public Map<String, Object> parsePostApprovalHandler(ApprovalConfigEntity config) {
        Map<String, Object> handler = new HashMap<>();
        if (config == null || config.getPostApprovalHandler() == null || config.getPostApprovalHandler().trim().isEmpty()) {
            return handler;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsedHandler = objectMapper.readValue(config.getPostApprovalHandler(), Map.class);
            handler = parsedHandler;
            log.debug("[审批配置] 解析审批后处理配置成功，businessType={}", config.getBusinessType());
        } catch (Exception e) {
            log.error("[审批配置] 解析审批后处理配置异常，businessType={}", config.getBusinessType(), e);
        }

        return handler;
    }

    /**
     * 解析超时配置
     *
     * @param config 审批配置实体
     * @return 超时配置Map
     */
    public Map<String, Object> parseTimeoutConfig(ApprovalConfigEntity config) {
        Map<String, Object> timeoutConfig = new HashMap<>();
        if (config == null || config.getTimeoutConfig() == null || config.getTimeoutConfig().trim().isEmpty()) {
            return timeoutConfig;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsedTimeoutConfig = objectMapper.readValue(config.getTimeoutConfig(), Map.class);
            timeoutConfig = parsedTimeoutConfig;
            log.debug("[审批配置] 解析超时配置成功，businessType={}", config.getBusinessType());
        } catch (Exception e) {
            log.error("[审批配置] 解析超时配置异常，businessType={}", config.getBusinessType(), e);
        }

        return timeoutConfig;
    }
}

