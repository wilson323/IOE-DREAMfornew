package net.lab1024.sa.biometric.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.biometric.dao.BiometricTemplateDao;
import net.lab1024.sa.biometric.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.biometric.domain.entity.BiometricType;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateAddForm;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateQueryForm;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateUpdateForm;
import net.lab1024.sa.biometric.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.biometric.manager.BiometricTemplateManager;
import net.lab1024.sa.biometric.service.BiometricTemplateService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.SmartBeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生物模板管理服务实现
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 使用@Transactional事务管理
 * - 调用Manager层进行复杂业务编排
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BiometricTemplateServiceImpl implements BiometricTemplateService {

    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Resource
    private BiometricTemplateManager biometricTemplateManager;

    @Override
    public ResponseDTO<BiometricTemplateVO> addTemplate(BiometricTemplateAddForm addForm) {
        try {
            log.info("[生物模板管理] 添加模板开始 userId={}, biometricType={}",
                    addForm.getUserId(), addForm.getBiometricType());

            // 1. 验证表单数据
            validateAddForm(addForm);

            // 2. 调用Manager层注册模板
            BiometricTemplateEntity template = biometricTemplateManager.registerTemplate(
                    addForm.getUserId(),
                    addForm.getBiometricType(),
                    addForm.getFeatureData(),
                    addForm.getQualityScore()
            );

            // 3. 转换为VO
            BiometricTemplateVO templateVO = convertToVO(template);

            log.info("[生物模板管理] 添加模板成功 templateId={}", template.getTemplateId());
            return ResponseDTO.ok(templateVO);

        } catch (Exception e) {
            log.error("[生物模板管理] 添加模板失败 userId={}, biometricType={}",
                    addForm.getUserId(), addForm.getBiometricType(), e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_ADD_ERROR", "添加模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateTemplate(BiometricTemplateUpdateForm updateForm) {
        try {
            log.info("[生物模板管理] 更新模板开始 templateId={}", updateForm.getTemplateId());

            // 1. 查询模板
            BiometricTemplateEntity template = biometricTemplateDao.selectById(updateForm.getTemplateId());
            if (template == null) {
                throw new BusinessException("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }

            // 2. 更新字段
            if (updateForm.getTemplateName() != null) {
                template.setTemplateName(updateForm.getTemplateName());
            }
            if (updateForm.getTemplateStatus() != null) {
                template.setTemplateStatus(updateForm.getTemplateStatus());
            }
            if (updateForm.getMatchThreshold() != null) {
                template.setMatchThreshold(updateForm.getMatchThreshold());
            }

            // 3. 保存
            biometricTemplateDao.updateById(template);

            log.info("[生物模板管理] 更新模板成功 templateId={}", updateForm.getTemplateId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 更新模板失败 templateId={}", updateForm.getTemplateId(), e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_UPDATE_ERROR", "更新模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteTemplate(Long templateId) {
        try {
            log.info("[生物模板管理] 删除模板开始 templateId={}", templateId);

            // 1. 查询模板
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                throw new BusinessException("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }

            // 2. 调用Manager层删除模板（会从所有设备删除）
            biometricTemplateManager.deleteTemplate(template.getUserId(), template.getBiometricType());

            log.info("[生物模板管理] 删除模板成功 templateId={}", templateId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 删除模板失败 templateId={}", templateId, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_DELETE_ERROR", "删除模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteTemplateByUserAndType(Long userId, Integer biometricType) {
        try {
            log.info("[生物模板管理] 删除用户模板开始 userId={}, biometricType={}", userId, biometricType);

            // 调用Manager层删除模板
            biometricTemplateManager.deleteTemplate(userId, biometricType);

            log.info("[生物模板管理] 删除用户模板成功 userId={}, biometricType={}", userId, biometricType);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 删除用户模板失败 userId={}, biometricType={}", userId, biometricType, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_DELETE_ERROR", "删除模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<BiometricTemplateVO> getTemplateById(Long templateId) {
        try {
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                return ResponseDTO.error("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }

            BiometricTemplateVO templateVO = convertToVO(template);
            return ResponseDTO.ok(templateVO);

        } catch (Exception e) {
            log.error("[生物模板管理] 查询模板失败 templateId={}", templateId, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_QUERY_ERROR", "查询模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<BiometricTemplateVO>> getTemplatesByUserId(Long userId) {
        try {
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserId(userId);
            List<BiometricTemplateVO> templateVOs = templates.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return ResponseDTO.ok(templateVOs);

        } catch (Exception e) {
            log.error("[生物模板管理] 查询用户模板失败 userId={}", userId, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_QUERY_ERROR", "查询模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<BiometricTemplateVO> getTemplateByUserAndType(Long userId, Integer biometricType) {
        try {
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserIdAndType(userId, biometricType);
            if (templates.isEmpty()) {
                return ResponseDTO.error("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }

            BiometricTemplateVO templateVO = convertToVO(templates.get(0));
            return ResponseDTO.ok(templateVO);

        } catch (Exception e) {
            log.error("[生物模板管理] 查询用户模板失败 userId={}, biometricType={}", userId, biometricType, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_QUERY_ERROR", "查询模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<BiometricTemplateVO>> pageTemplate(BiometricTemplateQueryForm queryForm) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<BiometricTemplateEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(queryForm.getUserId() != null, BiometricTemplateEntity::getUserId, queryForm.getUserId())
                    .eq(queryForm.getBiometricType() != null, BiometricTemplateEntity::getBiometricType, queryForm.getBiometricType())
                    .eq(queryForm.getTemplateStatus() != null, BiometricTemplateEntity::getTemplateStatus, queryForm.getTemplateStatus())
                    .eq(queryForm.getDeviceId() != null, BiometricTemplateEntity::getDeviceId, queryForm.getDeviceId())
                    .like(queryForm.getTemplateName() != null, BiometricTemplateEntity::getTemplateName, queryForm.getTemplateName())
                    .orderByDesc(BiometricTemplateEntity::getCreateTime);

            // 分页查询
            Page<BiometricTemplateEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
            Page<BiometricTemplateEntity> pageResult = biometricTemplateDao.selectPage(page, queryWrapper);

            // 转换为VO
            List<BiometricTemplateVO> templateVOs = pageResult.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            // 构建分页结果
            PageResult<BiometricTemplateVO> result = PageResult.of(
                    templateVOs,
                    pageResult.getTotal(),
                    queryForm.getPageNum(),
                    queryForm.getPageSize()
            );

            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[生物模板管理] 分页查询模板失败", e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_QUERY_ERROR", "查询模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateTemplateStatus(Long templateId, Integer templateStatus) {
        try {
            int rows = biometricTemplateDao.updateTemplateStatus(templateId, templateStatus);
            if (rows == 0) {
                throw new BusinessException("BIOMETRIC_TEMPLATE_NOT_FOUND", "模板不存在");
            }
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 更新模板状态失败 templateId={}, status={}", templateId, templateStatus, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_UPDATE_ERROR", "更新模板状态失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> batchUpdateTemplateStatus(List<Long> templateIds, Integer templateStatus) {
        try {
            for (Long templateId : templateIds) {
                biometricTemplateDao.updateTemplateStatus(templateId, templateStatus);
            }
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物模板管理] 批量更新模板状态失败 templateIds={}, status={}", templateIds, templateStatus, e);
            throw new BusinessException("BIOMETRIC_TEMPLATE_UPDATE_ERROR", "批量更新模板状态失败: " + e.getMessage());
        }
    }

    /**
     * 验证添加表单
     */
    private void validateAddForm(BiometricTemplateAddForm addForm) {
        if (addForm.getUserId() == null) {
            throw new BusinessException("PARAM_ERROR", "用户ID不能为空");
        }
        if (addForm.getBiometricType() == null) {
            throw new BusinessException("PARAM_ERROR", "生物识别类型不能为空");
        }
        if (addForm.getFeatureData() == null || addForm.getFeatureData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "特征数据不能为空");
        }
    }

    /**
     * 转换为VO
     */
    private BiometricTemplateVO convertToVO(BiometricTemplateEntity template) {
        BiometricTemplateVO vo = SmartBeanUtil.copy(template, BiometricTemplateVO.class);
        
        // 设置描述字段
        if (template.getBiometricType() != null) {
            BiometricType type = BiometricType.fromCode(template.getBiometricType());
            vo.setBiometricTypeDesc(type.getName());
        }
        if (template.getTemplateStatus() != null) {
            BiometricTemplateEntity.TemplateStatus status = BiometricTemplateEntity.TemplateStatus.fromCode(template.getTemplateStatus());
            vo.setTemplateStatusDesc(status.getDescription());
        }
        
        // 计算成功率
        if (template.getUseCount() != null && template.getUseCount() > 0) {
            double successRate = (double) template.getSuccessCount() / template.getUseCount() * 100;
            vo.setSuccessRate(String.format("%.2f%%", successRate));
        }

        return vo;
    }
}
