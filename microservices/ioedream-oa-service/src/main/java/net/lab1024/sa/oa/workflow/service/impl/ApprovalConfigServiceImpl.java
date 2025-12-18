package net.lab1024.sa.oa.workflow.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.workflow.dao.ApprovalConfigDao;
import net.lab1024.sa.oa.workflow.domain.form.ApprovalConfigForm;
import net.lab1024.sa.common.workflow.entity.ApprovalConfigEntity;
import net.lab1024.sa.oa.workflow.service.ApprovalConfigService;

/**
 * 审批配置服务实现类
 * <p>
 * 提供审批配置的CRUD操作
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-oa-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ApprovalConfigServiceImpl implements ApprovalConfigService {

    @Resource
    private ApprovalConfigDao approvalConfigDao;

    @Override
    @Observed(name = "approval.config.pageConfigs", contextualName = "approval-config-page")
    public ResponseDTO<PageResult<ApprovalConfigEntity>> pageConfigs(
            PageParam pageParam, String businessType, String module, String status) {
        log.info("分页查询审批配置，businessType={}, module={}, status={}", businessType, module, status);

        try {
            LambdaQueryWrapper<ApprovalConfigEntity> wrapper = new LambdaQueryWrapper<>();
            if (businessType != null && !businessType.trim().isEmpty()) {
                wrapper.eq(ApprovalConfigEntity::getBusinessType, businessType);
            }
            if (module != null && !module.trim().isEmpty()) {
                wrapper.eq(ApprovalConfigEntity::getModule, module);
            }
            if (status != null && !status.trim().isEmpty()) {
                wrapper.eq(ApprovalConfigEntity::getStatus, status);
            }
            wrapper.orderByDesc(ApprovalConfigEntity::getSortOrder);
            wrapper.orderByDesc(ApprovalConfigEntity::getCreateTime);

            Page<ApprovalConfigEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            IPage<ApprovalConfigEntity> pageResult = approvalConfigDao.selectPage(page, wrapper);

            PageResult<ApprovalConfigEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum(pageParam.getPageNum());
            result.setPageSize(pageParam.getPageSize());

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批配置] 分页查询审批配置参数错误: {}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批配置] 分页查询审批配置业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批配置] 分页查询审批配置系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("PAGE_CONFIGS_SYSTEM_ERROR", "分页查询审批配置失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批配置] 分页查询审批配置未知异常", e);
            throw new SystemException("PAGE_CONFIGS_SYSTEM_ERROR", "分页查询审批配置失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "approval.config.getConfig", contextualName = "approval-config-get")
    public ResponseDTO<ApprovalConfigEntity> getConfig(Long id) {
        log.info("查询审批配置，id={}", id);

        try {
            ApprovalConfigEntity config = approvalConfigDao.selectById(id);
            if (config == null) {
                return ResponseDTO.error("审批配置不存在");
            }
            return ResponseDTO.ok(config);
        } catch (Exception e) {
            log.error("查询审批配置异常，id={}", id, e);
            return ResponseDTO.error("查询审批配置失败: " + e.getMessage());
        }
    }

    @Override
    @Observed(name = "approval.config.getByBusinessType", contextualName = "approval-config-get-by-type")
    public ResponseDTO<ApprovalConfigEntity> getConfigByBusinessType(String businessType) {
        log.info("根据业务类型查询审批配置，businessType={}", businessType);

        try {
            ApprovalConfigEntity config = approvalConfigDao.selectByBusinessType(businessType);
            if (config == null) {
                return ResponseDTO.error("未找到业务类型对应的审批配置: " + businessType);
            }
            return ResponseDTO.ok(config);
        } catch (Exception e) {
            log.error("根据业务类型查询审批配置异常，businessType={}", businessType, e);
            return ResponseDTO.error("查询审批配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Observed(name = "approval.config.createConfig", contextualName = "approval-config-create")
    public ResponseDTO<ApprovalConfigEntity> createConfig(ApprovalConfigForm form) {
        log.info("创建审批配置，businessType={}", form.getBusinessType());

        try {
            // 检查业务类型是否已存在
            int exists = approvalConfigDao.existsByBusinessType(form.getBusinessType());
            if (exists > 0) {
                return ResponseDTO.error("业务类型已存在: " + form.getBusinessType());
            }

            ApprovalConfigEntity entity = new ApprovalConfigEntity();
            entity.setBusinessType(form.getBusinessType());
            entity.setBusinessTypeName(form.getBusinessTypeName());
            entity.setModule(form.getModule());
            entity.setDefinitionId(form.getDefinitionId());
            entity.setProcessKey(form.getProcessKey());
            entity.setApprovalRules(form.getApprovalRules());
            entity.setPostApprovalHandler(form.getPostApprovalHandler());
            entity.setTimeoutConfig(form.getTimeoutConfig());
            entity.setNotificationConfig(form.getNotificationConfig());
            entity.setApplicableScope(form.getApplicableScope());
            entity.setStatus(form.getStatus() != null ? form.getStatus() : "ENABLED");
            entity.setSortOrder(form.getSortOrder() != null ? form.getSortOrder() : 0);
            entity.setRemark(form.getRemark());
            entity.setEffectiveTime(form.getEffectiveTime() != null ? form.getEffectiveTime() : LocalDateTime.now());
            entity.setExpireTime(form.getExpireTime());

            approvalConfigDao.insert(entity);
            log.info("创建审批配置成功，id={}, businessType={}", entity.getId(), entity.getBusinessType());
            return ResponseDTO.ok(entity);
        } catch (Exception e) {
            log.error("创建审批配置异常，businessType={}", form.getBusinessType(), e);
            return ResponseDTO.error("创建审批配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Observed(name = "approval.config.updateConfig", contextualName = "approval-config-update")
    public ResponseDTO<ApprovalConfigEntity> updateConfig(Long id, ApprovalConfigForm form) {
        log.info("更新审批配置，id={}, businessType={}", id, form.getBusinessType());

        try {
            ApprovalConfigEntity entity = approvalConfigDao.selectById(id);
            if (entity == null) {
                return ResponseDTO.error("审批配置不存在");
            }

            // 如果业务类型变更，检查新业务类型是否已存在
            if (!entity.getBusinessType().equals(form.getBusinessType())) {
                int exists = approvalConfigDao.existsByBusinessType(form.getBusinessType());
                if (exists > 0) {
                    return ResponseDTO.error("业务类型已存在: " + form.getBusinessType());
                }
            }

            entity.setBusinessType(form.getBusinessType());
            entity.setBusinessTypeName(form.getBusinessTypeName());
            entity.setModule(form.getModule());
            entity.setDefinitionId(form.getDefinitionId());
            entity.setProcessKey(form.getProcessKey());
            entity.setApprovalRules(form.getApprovalRules());
            entity.setPostApprovalHandler(form.getPostApprovalHandler());
            entity.setTimeoutConfig(form.getTimeoutConfig());
            entity.setNotificationConfig(form.getNotificationConfig());
            entity.setApplicableScope(form.getApplicableScope());
            if (form.getStatus() != null) {
                entity.setStatus(form.getStatus());
            }
            if (form.getSortOrder() != null) {
                entity.setSortOrder(form.getSortOrder());
            }
            entity.setRemark(form.getRemark());
            if (form.getEffectiveTime() != null) {
                entity.setEffectiveTime(form.getEffectiveTime());
            }
            entity.setExpireTime(form.getExpireTime());

            approvalConfigDao.updateById(entity);
            log.info("更新审批配置成功，id={}, businessType={}", id, entity.getBusinessType());
            return ResponseDTO.ok(entity);
        } catch (Exception e) {
            log.error("更新审批配置异常，id={}", id, e);
            return ResponseDTO.error("更新审批配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Observed(name = "approval.config.deleteConfig", contextualName = "approval-config-delete")
    public ResponseDTO<Void> deleteConfig(Long id) {
        log.info("删除审批配置，id={}", id);

        try {
            ApprovalConfigEntity entity = approvalConfigDao.selectById(id);
            if (entity == null) {
                return ResponseDTO.error("审批配置不存在");
            }

            approvalConfigDao.deleteById(id);
            log.info("删除审批配置成功，id={}", id);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("删除审批配置异常，id={}", id, e);
            return ResponseDTO.error("删除审批配置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Observed(name = "approval.config.enableConfig", contextualName = "approval-config-enable")
    public ResponseDTO<Void> enableConfig(Long id) {
        log.info("启用审批配置，id={}", id);

        try {
            ApprovalConfigEntity entity = approvalConfigDao.selectById(id);
            if (entity == null) {
                throw new BusinessException("CONFIG_NOT_FOUND", "审批配置不存在");
            }

            entity.setStatus("ENABLED");
            approvalConfigDao.updateById(entity);
            log.info("启用审批配置成功，id={}", id);
            return ResponseDTO.ok();
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批配置] 启用审批配置参数错误，id={}, error={}", id, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批配置] 启用审批配置业务异常，id={}, code={}, message={}", id, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批配置] 启用审批配置系统异常，id={}, code={}, message={}", id, e.getCode(), e.getMessage(), e);
            throw new SystemException("ENABLE_CONFIG_SYSTEM_ERROR", "启用审批配置失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批配置] 启用审批配置未知异常，id={}", id, e);
            throw new SystemException("ENABLE_CONFIG_SYSTEM_ERROR", "启用审批配置失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> disableConfig(Long id) {
        log.info("禁用审批配置，id={}", id);

        try {
            ApprovalConfigEntity entity = approvalConfigDao.selectById(id);
            if (entity == null) {
                throw new BusinessException("CONFIG_NOT_FOUND", "审批配置不存在");
            }

            entity.setStatus("DISABLED");
            approvalConfigDao.updateById(entity);
            log.info("禁用审批配置成功，id={}", id);
            return ResponseDTO.ok();
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[审批配置] 禁用审批配置参数错误，id={}, error={}", id, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[审批配置] 禁用审批配置业务异常，id={}, code={}, message={}", id, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[审批配置] 禁用审批配置系统异常，id={}, code={}, message={}", id, e.getCode(), e.getMessage(), e);
            throw new SystemException("DISABLE_CONFIG_SYSTEM_ERROR", "禁用审批配置失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[审批配置] 禁用审批配置未知异常，id={}", id, e);
            throw new SystemException("DISABLE_CONFIG_SYSTEM_ERROR", "禁用审批配置失败：" + e.getMessage(), e);
        }
    }
}





