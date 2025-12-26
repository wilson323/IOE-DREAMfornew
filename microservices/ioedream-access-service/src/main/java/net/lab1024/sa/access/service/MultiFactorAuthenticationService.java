package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.MultiFactorAuthenticationForm;
import net.lab1024.sa.access.domain.vo.MultiFactorAuthenticationResultVO;

/**
 * 多因子认证服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义
 * - 使用标准返回类型（Controller层包装ResponseDTO）
 * </p>
 * <p>
 * 核心职责：
 * - 提供多因子认证功能
 * - 支持双重生物识别认证（人脸+指纹）
 * - 支持生物识别+IC卡组合认证
 * - 提供认证策略配置
 * </p>
 * <p>
 * 认证模式：
 * - STRICT: 严格模式（所有因子必须通过）
 * - RELAXED: 宽松模式（至少一个因子通过）
 * - PRIORITY: 优先模式（按优先级依次验证）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface MultiFactorAuthenticationService {

    /**
     * 执行多因子认证
     * <p>
     * 根据配置的认证策略，依次验证多个认证因子
     * </p>
     *
     * @param form 多因子认证请求表单
     * @return 认证结果（包含通过状态、详细日志、失败原因）
     */
    MultiFactorAuthenticationResultVO authenticate(MultiFactorAuthenticationForm form);

    /**
     * 验证人脸特征
     * <p>
     * 提取人脸特征并与模板比对
     * </p>
     *
     * @param userId 用户ID
     * @param faceImageData 人脸图像数据（Base64编码）
     * @return 人脸验证结果
     */
    boolean verifyFace(Long userId, String faceImageData);

    /**
     * 验证指纹特征
     * <p>
     * 提取指纹特征并与模板比对
     * </p>
     *
     * @param userId 用户ID
     * @param fingerprintData 指纹特征数据
     * @return 指纹验证结果
     */
    boolean verifyFingerprint(Long userId, byte[] fingerprintData);

    /**
     * 获取用户支持的多因子认证配置
     *
     * @param userId 用户ID
     * @return 多因子认证配置
     */
    Object getUserMultiFactorConfig(Long userId);
}
