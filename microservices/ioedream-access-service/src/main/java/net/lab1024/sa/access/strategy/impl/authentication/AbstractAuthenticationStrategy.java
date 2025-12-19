package net.lab1024.sa.access.strategy.impl.authentication;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import net.lab1024.sa.access.strategy.MultiModalAuthenticationStrategy;

/**
 * 多模态认证策略抽象基类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用抽象类封装通用逻辑
 * - 子类实现具体的认证逻辑
 * - 使用模板方法模式
 * </p>
 * <p>
 * 核心职责：
 * - 提供通用的认证流程框架
 * - 处理通用的参数验证和异常处理
 * - 记录认证日志和指标
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public abstract class AbstractAuthenticationStrategy implements MultiModalAuthenticationStrategy {

    /**
     * 执行认证（模板方法）
     * <p>
     * 定义统一的认证流程：
     * 1. 参数验证
     * 2. 执行具体认证逻辑（由子类实现）
     * 3. 记录认证结果
     * </p>
     *
     * @param request 验证请求
     * @return 认证结果
     */
    @Override
    public VerificationResult authenticate(AccessVerificationRequest request) {
        log.debug("[{}] 开始认证: userId={}, deviceId={}, verifyType={}",
                getStrategyName(), request.getUserId(), request.getDeviceId(), request.getVerifyType());

        try {
            // 1. 参数验证
            if (!validateRequest(request)) {
                return VerificationResult.failed("INVALID_REQUEST", "认证请求参数无效");
            }

            // 2. 执行具体认证逻辑（由子类实现）
            VerificationResult result = doAuthenticate(request);

            // 3. 记录认证结果
            logAuthenticationResult(request, result);

            return result;

        } catch (Exception e) {
            log.error("[{}] 认证异常: userId={}, error={}", getStrategyName(), request.getUserId(), e.getMessage(), e);
            return VerificationResult.failed("AUTHENTICATION_ERROR", "认证异常: " + e.getMessage());
        }
    }

    /**
     * 验证请求参数
     * <p>
     * 子类可以重写此方法实现自定义验证逻辑
     * </p>
     *
     * @param request 验证请求
     * @return 是否有效
     */
    protected boolean validateRequest(AccessVerificationRequest request) {
        if (request == null) {
            log.warn("[{}] 认证请求为空", getStrategyName());
            return false;
        }

        if (request.getUserId() == null) {
            log.warn("[{}] 用户ID为空", getStrategyName());
            return false;
        }

        if (request.getDeviceId() == null) {
            log.warn("[{}] 设备ID为空", getStrategyName());
            return false;
        }

        return true;
    }

    /**
     * 执行具体认证逻辑
     * <p>
     * 由子类实现具体的认证逻辑
     * </p>
     *
     * @param request 验证请求
     * @return 认证结果
     */
    protected abstract VerificationResult doAuthenticate(AccessVerificationRequest request);

    /**
     * 记录认证结果
     * <p>
     * 子类可以重写此方法实现自定义日志记录
     * </p>
     *
     * @param request 验证请求
     * @param result  认证结果
     */
    protected void logAuthenticationResult(AccessVerificationRequest request, VerificationResult result) {
        if (result.isSuccess()) {
            log.info("[{}] 认证成功: userId={}, deviceId={}", getStrategyName(), request.getUserId(), request.getDeviceId());
        } else {
            log.warn("[{}] 认证失败: userId={}, deviceId={}, errorCode={}, errorMessage={}",
                    getStrategyName(), request.getUserId(), request.getDeviceId(),
                    result.getErrorCode(), result.getErrorMessage());
        }
    }

    /**
     * 是否支持该认证方式
     * <p>
     * 默认实现：检查请求的verifyType是否匹配当前策略的认证方式
     * </p>
     *
     * @param verifyType 认证方式代码
     * @return 是否支持
     */
    @Override
    public boolean supports(Integer verifyType) {
        VerifyTypeEnum verifyTypeEnum = getVerifyType();
        return verifyTypeEnum != null && verifyTypeEnum.getCode().equals(verifyType);
    }
}
