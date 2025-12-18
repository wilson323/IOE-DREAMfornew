package net.lab1024.sa.access.strategy;

import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;

/**
 * 验证模式策略接口
 * <p>
 * 严格遵循策略模式设计：
 * - 定义统一的验证接口
 * - 支持多种验证模式实现
 * - 可扩展新的验证模式
 * </p>
 * <p>
 * 实现类：
 * - EdgeVerificationStrategy - 设备端验证策略
 * - BackendVerificationStrategy - 后台验证策略
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VerificationModeStrategy {

    /**
     * 执行验证
     * <p>
     * 根据不同的验证模式执行相应的验证逻辑
     * </p>
     *
     * @param request 验证请求
     * @return 验证结果
     */
    VerificationResult verify(AccessVerificationRequest request);

    /**
     * 是否支持该验证模式
     * <p>
     * 用于策略选择，判断当前策略是否支持指定的验证模式
     * </p>
     *
     * @param mode 验证模式（edge/backend/hybrid）
     * @return 是否支持
     */
    boolean supports(String mode);

    /**
     * 获取策略名称
     * <p>
     * 用于日志记录和调试
     * </p>
     *
     * @return 策略名称
     */
    String getStrategyName();
}
