package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.BiometricTemplateDao;
import net.lab1024.sa.access.service.BiometricAuthService;
import net.lab1024.sa.access.manager.BiometricTemplateManager;
import net.lab1024.sa.access.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.access.domain.form.BiometricRegisterForm;
import net.lab1024.sa.access.domain.form.BiometricAuthForm;
import net.lab1024.sa.access.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.access.domain.vo.BiometricAuthResultVO;
import net.lab1024.sa.common.core.domain.ResponseDTO;
import net.lab1024.sa.common.core.exception.BusinessException;
import net.lab1024.sa.common.core.util.SmartBeanUtil;
import net.lab1024.sa.common.core.util.SmartBase64Util;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生物识别认证服务实现
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BiometricAuthServiceImpl implements BiometricAuthService {

    @Resource
    private BiometricTemplateManager biometricTemplateManager;

    @Resource
    private BiometricTemplateDao biometricTemplateDao;

    @Override
    public ResponseDTO<BiometricTemplateVO> registerTemplate(BiometricRegisterForm registerForm) {
        try {
            log.info("[生物识别服务] 注册模板 userId={}, biometricType={}",
                    registerForm.getUserId(), registerForm.getBiometricType());

            // 1. 验证表单数据
            validateRegisterForm(registerForm);

            // 2. 解码特征数据
            byte[] featureData = SmartBase64Util.decodeToBytes(registerForm.getFeatureData());

            // 3. 调用Manager注册模板
            BiometricTemplateEntity template = biometricTemplateManager.registerTemplate(
                    registerForm.getUserId(),
                    registerForm.getBiometricType(),
                    featureData,
                    registerForm.getDeviceId()
            );

            // 4. 转换为VO
            BiometricTemplateVO templateVO = convertToTemplateVO(template);

            log.info("[生物识别服务] 模板注册成功 templateId={}", template.getTemplateId());
            return ResponseDTO.ok(templateVO);

        } catch (Exception e) {
            log.error("[生物识别服务] 模板注册失败 userId={}, biometricType={}",
                    registerForm.getUserId(), registerForm.getBiometricType(), e);
            throw new BusinessException("BIOMETRIC_REGISTER_ERROR", "模板注册失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<BiometricAuthResultVO> authenticate(BiometricAuthForm authForm) {
        try {
            log.info("[生物识别服务] 1:1验证 userId={}, biometricType={}",
                    authForm.getUserId(), authForm.getBiometricType());

            // 1. 验证表单数据
            validateAuthForm(authForm);

            // 2. 解码特征数据
            byte[] featureData = SmartBase64Util.decodeToBytes(authForm.getFeatureData());

            // 3. 调用Manager进行验证
            BiometricAuthResult authResult = biometricTemplateManager.authenticate(
                    authForm.getUserId(),
                    authForm.getBiometricType(),
                    featureData,
                    authForm.getDeviceId()
            );

            // 4. 转换为VO
            BiometricAuthResultVO resultVO = convertToAuthResultVO(authResult);

            log.info("[生物识别服务] 1:1验证完成 userId={}, success={}, matchScore={}",
                    authForm.getUserId(), authResult.isSuccess(), authResult.getMatchScore());
            return ResponseDTO.ok(resultVO);

        } catch (Exception e) {
            log.error("[生物识别服务] 1:1验证失败 userId={}, biometricType={}",
                    authForm.getUserId(), authForm.getBiometricType(), e);
            throw new BusinessException("BIOMETRIC_AUTH_ERROR", "生物识别验证失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<BiometricAuthResultVO>> identify(BiometricAuthForm authForm) {
        try {
            log.info("[生物识别服务] 1:N识别 biometricType={}", authForm.getBiometricType());

            // 1. 验证表单数据
            validateIdentifyForm(authForm);

            // 2. 解码特征数据
            byte[] featureData = SmartBase64Util.decodeToBytes(authForm.getFeatureData());

            // 3. 调用Manager进行识别
            List<BiometricTemplateManager.BiometricAuthResult> authResults = biometricTemplateManager.identify(
                    authForm.getBiometricType(),
                    featureData,
                    authForm.getDeviceId(),
                    authForm.getLimit()
            );

            // 4. 转换为VO列表
            List<BiometricAuthResultVO> resultVOs = authResults.stream()
                    .map(this::convertToAuthResultVO)
                    .collect(Collectors.toList());

            log.info("[生物识别服务] 1:N识别完成 biometricType={}, resultCount={}",
                    authForm.getBiometricType(), resultVOs.size());
            return ResponseDTO.ok(resultVOs);

        } catch (Exception e) {
            log.error("[生物识别服务] 1:N识别失败 biometricType={}", authForm.getBiometricType(), e);
            throw new BusinessException("BIOMETRIC_IDENTIFY_ERROR", "生物识别失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<BiometricTemplateVO>> getUserTemplates(Long userId) {
        try {
            log.info("[生物识别服务] 查询用户模板 userId={}", userId);

            // 1. 查询用户所有模板
            List<BiometricTemplateEntity> templates = biometricTemplateDao.selectByUserId(userId);

            // 2. 转换为VO列表
            List<BiometricTemplateVO> templateVOs = templates.stream()
                    .map(this::convertToTemplateVO)
                    .collect(Collectors.toList());

            log.info("[生物识别服务] 用户模板查询完成 userId={}, templateCount={}",
                    userId, templateVOs.size());
            return ResponseDTO.ok(templateVOs);

        } catch (Exception e) {
            log.error("[生物识别服务] 用户模板查询失败 userId={}", userId, e);
            throw new BusinessException("BIOMETRIC_QUERY_ERROR", "查询用户模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> updateTemplateStatus(Long templateId, Integer status) {
        try {
            log.info("[生物识别服务] 更新模板状态 templateId={}, status={}", templateId, status);

            // 1. 验证模板ID
            BiometricTemplateEntity template = biometricTemplateDao.selectById(templateId);
            if (template == null) {
                throw new BusinessException("TEMPLATE_NOT_FOUND", "模板不存在");
            }

            // 2. 调用Manager更新状态
            biometricTemplateManager.updateTemplateStatus(templateId, status);

            log.info("[生物识别服务] 模板状态更新成功 templateId={}", templateId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物识别服务] 更新模板状态失败 templateId={}, status={}", templateId, status, e);
            throw new BusinessException("TEMPLATE_UPDATE_ERROR", "更新模板状态失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Void> deleteUserTemplate(Long userId, Integer biometricType) {
        try {
            log.info("[生物识别服务] 删除用户模板 userId={}, biometricType={}", userId, biometricType);

            // 1. 调用Manager删除模板
            biometricTemplateManager.deleteUserTemplates(userId, biometricType);

            log.info("[生物识别服务] 用户模板删除成功 userId={}, biometricType={}", userId, biometricType);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[生物识别服务] 删除用户模板失败 userId={}, biometricType={}", userId, biometricType, e);
            throw new BusinessException("TEMPLATE_DELETE_ERROR", "删除用户模板失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<BiometricTemplateVO> getUserBiometricStats(Long userId) {
        try {
            log.info("[生物识别服务] 查询用户生物识别统计 userId={}", userId);

            // 1. 查询用户所有激活模板
            List<BiometricTemplateEntity> templates = biometricTemplateDao
                    .selectByUserId(userId)
                    .stream()
                    .filter(t -> t.getTemplateStatus().equals(BiometricTemplateEntity.TemplateStatus.ACTIVE.getCode()))
                    .collect(Collectors.toList());

            // 2. 计算统计信息
            int totalTemplates = templates.size();
            int totalUseCount = templates.stream().mapToInt(t -> t.getUseCount() != null ? t.getUseCount() : 0).sum();
            int totalSuccessCount = templates.stream().mapToInt(t -> t.getSuccessCount() != null ? t.getSuccessCount() : 0).sum();
            int totalFailCount = templates.stream().mapToInt(t -> t.getFailCount() != null ? t.getFailCount() : 0).sum();

            double successRate = totalUseCount > 0 ? (double) totalSuccessCount / totalUseCount * 100 : 0;

            // 3. 构建统计VO
            BiometricTemplateVO statsVO = new BiometricTemplateVO();
            statsVO.setUserId(userId);
            statsVO.setTotalTemplates(totalTemplates);
            statsVO.setTotalUseCount(totalUseCount);
            statsVO.setTotalSuccessCount(totalSuccessCount);
            statsVO.setTotalFailCount(totalFailCount);
            statsVO.setSuccessRate(String.format("%.2f%%", successRate));

            log.info("[生物识别服务] 用户生物识别统计查询完成 userId={}, templateCount={}, successRate={}",
                    userId, totalTemplates, successRate);
            return ResponseDTO.ok(statsVO);

        } catch (Exception e) {
            log.error("[生物识别服务] 查询用户生物识别统计失败 userId={}", userId, e);
            throw new BusinessException("BIOMETRIC_STATS_ERROR", "查询生物识别统计失败: " + e.getMessage());
        }
    }

    // ========== 私有方法 ==========

    /**
     * 验证注册表单
     */
    private void validateRegisterForm(BiometricRegisterForm form) {
        if (form.getUserId() == null) {
            throw new BusinessException("PARAM_ERROR", "用户ID不能为空");
        }
        if (form.getBiometricType() == null) {
            throw new BusinessException("PARAM_ERROR", "生物识别类型不能为空");
        }
        if (form.getFeatureData() == null || form.getFeatureData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "特征数据不能为空");
        }
        if (form.getDeviceId() == null || form.getDeviceId().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "设备ID不能为空");
        }
    }

    /**
     * 验证认证表单
     */
    private void validateAuthForm(BiometricAuthForm form) {
        if (form.getUserId() == null) {
            throw new BusinessException("PARAM_ERROR", "用户ID不能为空");
        }
        if (form.getBiometricType() == null) {
            throw new BusinessException("PARAM_ERROR", "生物识别类型不能为空");
        }
        if (form.getFeatureData() == null || form.getFeatureData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "特征数据不能为空");
        }
        if (form.getDeviceId() == null || form.getDeviceId().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "设备ID不能为空");
        }
    }

    /**
     * 验证识别表单
     */
    private void validateIdentifyForm(BiometricAuthForm form) {
        if (form.getBiometricType() == null) {
            throw new BusinessException("PARAM_ERROR", "生物识别类型不能为空");
        }
        if (form.getFeatureData() == null || form.getFeatureData().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "特征数据不能为空");
        }
        if (form.getDeviceId() == null || form.getDeviceId().isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "设备ID不能为空");
        }
        // limit是可选的，如果不设置则使用默认值
        if (form.getLimit() == null) {
            form.setLimit(10);
        }
    }

    /**
     * 转换模板实体为VO
     */
    private BiometricTemplateVO convertToTemplateVO(BiometricTemplateEntity entity) {
        BiometricTemplateVO vo = SmartBeanUtil.copy(entity, BiometricTemplateVO.class);

        // 设置枚举描述
        if (entity.getBiometricType() != null) {
            BiometricTemplateEntity.BiometricType type = BiometricTemplateEntity.BiometricType.fromCode(entity.getBiometricType());
            vo.setBiometricTypeDesc(type.getDescription());
        }

        if (entity.getTemplateStatus() != null) {
            BiometricTemplateEntity.TemplateStatus status = BiometricTemplateEntity.TemplateStatus.fromCode(entity.getTemplateStatus());
            vo.setTemplateStatusDesc(status.getDescription());
        }

        // 计算成功率
        if (entity.getUseCount() != null && entity.getUseCount() > 0) {
            int successCount = entity.getSuccessCount() != null ? entity.getSuccessCount() : 0;
            double successRate = (double) successCount / entity.getUseCount() * 100;
            vo.setSuccessRate(String.format("%.2f%%", successRate));
        } else {
            vo.setSuccessRate("0.00%");
        }

        // 不返回敏感的特征数据
        vo.setFeatureData(null);
        vo.setFeatureVector(null);

        return vo;
    }

    /**
     * 转换认证结果为VO
     */
    private BiometricAuthResultVO convertToAuthResultVO(BiometricTemplateManager.BiometricAuthResult authResult) {
        BiometricAuthResultVO vo = new BiometricAuthResultVO();
        vo.setSuccess(authResult.isSuccess());
        vo.setMatchScore(authResult.getMatchScore());
        vo.setLivenessPassed(authResult.getLivenessResult().isPassed());
        vo.setLivenessScore(authResult.getLivenessResult().getScore());
        vo.setMessage(authResult.getMessage());
        vo.setDuration(authResult.getDuration());

        if (authResult.getMatchedTemplate() != null) {
            vo.setTemplateId(authResult.getMatchedTemplate().getTemplateId());
            vo.setUserId(authResult.getMatchedTemplate().getUserId());
            vo.setTemplateName(authResult.getMatchedTemplate().getTemplateName());

            if (authResult.getMatchedTemplate().getBiometricType() != null) {
                BiometricTemplateEntity.BiometricType type = BiometricTemplateEntity.BiometricType.fromCode(
                        authResult.getMatchedTemplate().getBiometricType());
                vo.setBiometricTypeDesc(type.getDescription());
            }
        }

        return vo;
    }
}