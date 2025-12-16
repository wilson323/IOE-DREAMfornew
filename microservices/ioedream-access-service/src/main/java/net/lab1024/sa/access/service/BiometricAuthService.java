package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.BiometricRegisterForm;
import net.lab1024.sa.access.domain.form.BiometricAuthForm;
import net.lab1024.sa.access.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.access.domain.vo.BiometricAuthResultVO;
import net.lab1024.sa.access.manager.BiometricTemplateManager;
import net.lab1024.sa.access.domain.entity.BiometricTemplateEntity;
import net.lab1024.sa.access.manager.BiometricTemplateManager.BiometricAuthResult;
import net.lab1024.sa.common.core.domain.ResponseDTO;

import java.util.List;

/**
 * 生物识别认证服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface BiometricAuthService {

    /**
     * 注册生物识别模板
     *
     * @param registerForm 注册表单
     * @return 注册结果
     */
    ResponseDTO<BiometricTemplateVO> registerTemplate(BiometricRegisterForm registerForm);

    /**
     * 生物识别验证（1:1）
     *
     * @param authForm 验证表单
     * @return 验证结果
     */
    ResponseDTO<BiometricAuthResultVO> authenticate(BiometricAuthForm authForm);

    /**
     * 生物识别识别（1:N）
     *
     * @param authForm 验证表单
     * @return 识别结果列表
     */
    ResponseDTO<List<BiometricAuthResultVO>> identify(BiometricAuthForm authForm);

    /**
     * 获取用户生物识别模板列表
     *
     * @param userId 用户ID
     * @return 模板列表
     */
    ResponseDTO<List<BiometricTemplateVO>> getUserTemplates(Long userId);

    /**
     * 更新模板状态
     *
     * @param templateId 模板ID
     * @param status 状态
     * @return 操作结果
     */
    ResponseDTO<Void> updateTemplateStatus(Long templateId, Integer status);

    /**
     * 删除用户模板
     *
     * @param userId 用户ID
     * @param biometricType 生物识别类型
     * @return 操作结果
     */
    ResponseDTO<Void> deleteUserTemplate(Long userId, Integer biometricType);

    /**
     * 获取用户生物识别统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    ResponseDTO<BiometricTemplateVO> getUserBiometricStats(Long userId);
}