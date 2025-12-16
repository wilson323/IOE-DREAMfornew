package net.lab1024.sa.oa.workflow.manager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.oa.workflow.dao.ApprovalConfigDao;
import net.lab1024.sa.oa.workflow.entity.ApprovalConfigEntity;

/**
 * 审批配置管理Manager
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class ApprovalConfigManager {

    private final ApprovalConfigDao approvalConfigDao;

    public ApprovalConfigManager(ApprovalConfigDao approvalConfigDao) {
        this.approvalConfigDao = approvalConfigDao;
    }

    public ApprovalConfigEntity getApprovalConfig(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            log.warn("[审批配置] 业务类型为空，无法查询审批配置");
            return null;
        }

        try {
            ApprovalConfigEntity config = approvalConfigDao.selectByBusinessType(businessType);
            if (config != null) {
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

    public Long getDefinitionId(String businessType) {
        ApprovalConfigEntity config = getApprovalConfig(businessType);
        if (config != null && config.getDefinitionId() != null) {
            return config.getDefinitionId();
        }
        return null;
    }

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

    public boolean isConfigValid(ApprovalConfigEntity config) {
        if (config == null) {
            return false;
        }

        if (!"ENABLED".equals(config.getStatus())) {
            log.debug("[审批配置] 审批配置未启用，businessType={}", config.getBusinessType());
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (config.getEffectiveTime() != null && now.isBefore(config.getEffectiveTime())) {
            log.debug("[审批配置] 审批配置未生效，businessType={}, effectiveTime={}", config.getBusinessType(), config.getEffectiveTime());
            return false;
        }

        if (config.getExpireTime() != null && now.isAfter(config.getExpireTime())) {
            log.debug("[审批配置] 审批配置已失效，businessType={}, expireTime={}", config.getBusinessType(), config.getExpireTime());
            return false;
        }

        return true;
    }

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

    public Map<String, Object> parseApprovalRules(ApprovalConfigEntity config) {
        Map<String, Object> rules = new HashMap<>();
        if (config == null || config.getApprovalRules() == null || config.getApprovalRules().trim().isEmpty()) {
            return rules;
        }

        try {
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




