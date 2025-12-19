package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;

/**
 * 门禁验证服务接口
 * <p>
 * 统一验证入口，根据配置选择验证模式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessVerificationService {

    /**
     * 统一验证入口
     * <p>
     * 根据区域配置的验证模式，自动路由到相应的验证策略
     * </p>
     *
     * @param request 验证请求
     * @return 验证结果
     */
    VerificationResult verifyAccess(AccessVerificationRequest request);

    /**
     * 获取区域验证模式
     *
     * @param areaId 区域ID
     * @return 验证模式（edge/backend/hybrid）
     */
    String getVerificationMode(Long areaId);
}
