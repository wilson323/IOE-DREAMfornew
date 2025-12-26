package net.lab1024.sa.common.biometric.strategy.impl;

import lombok.extern.slf4j.Slf4j;


import net.lab1024.sa.common.biometric.domain.dto.VerificationRequest;
import net.lab1024.sa.common.biometric.domain.dto.VerificationResult;
import net.lab1024.sa.common.biometric.domain.enumeration.VerifyTypeEnum;
import net.lab1024.sa.common.biometric.strategy.MultiModalAuthenticationStrategy;

/**
 * 多模态认证策略抽象基类
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用抽象类封装通用逻辑
 * - 子类实现具体的认证逻辑
 * - 使用模板方法模式
 * </p>
 * <p>
 * ⚠️ 重要说明：多模态认证的作用
 * </p>
 * <p>
 * <strong>不是进行人员识别</strong>：
 * - 设备端已完成人员识别（人脸、指纹、卡片等）
 * - 设备端已识别出人员编号（pin字段）
 * - 软件端接收的是人员编号（pin），不是生物特征数据
 * </p>
 * <p>
 * <strong>核心职责是记录认证方式用于统计和审计</strong>：
 * - ✅ 记录认证方式（verifytype）用于统计和审计
 * - ✅ 提供认证方式枚举（VerifyTypeEnum）统一管理
 * - ✅ 转换认证方式（verifytype ↔ verifyMethod）用于数据存储和展示
 * - ❌ 不进行人员识别（设备端已完成）
 * - ❌ 不验证认证方式是否允许（设备端已完成，如果设备不支持该认证方式，设备端不会识别成功）
 * </p>
 * <p>
 * 两种验证模式的区别：
 * </p>
 * <ul>
 * <li><strong>边缘验证模式（Edge）</strong>：设备端已完成验证，软件端只记录认证方式</li>
 * <li><strong>后台验证模式（Backend）</strong>：设备端已识别人员和验证认证方式，软件端只记录认证方式</li>
 * </ul>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public abstract class AbstractAuthenticationStrategy implements MultiModalAuthenticationStrategy {

    /**
     * 记录认证方式（模板方法）
     * <p>
     * ⚠️ 注意：不是进行人员识别，也不是验证认证方式是否允许
     * </p>
     * <p>
     * 设备端已完成：
     * - 人员识别（1:N比对，识别出pin）
     * - 认证方式验证（如果设备不支持该认证方式，设备端不会识别成功）
     * </p>
     * <p>
     * 定义统一的记录流程：
     * 1. 参数验证
     * 2. 执行具体认证方式记录逻辑（由子类实现，默认返回成功）
     * 3. 记录认证结果
     * </p>
     * <p>
     * 核心职责：
     * - 记录认证方式（verifytype）用于统计和审计
     * - 提供认证方式枚举（VerifyTypeEnum）统一管理
     * - 转换认证方式（verifytype ↔ verifyMethod）用于数据存储和展示
     * </p>
     *
     * @param request 验证请求（包含userId、verifyType等，设备端已识别出人员编号）
     * @return 记录结果（始终成功，因为只是记录）
     */
    @Override
    public VerificationResult authenticate(VerificationRequest request) {
        log.debug("[{}] 开始认证: userId={}, deviceId={}, verifyType={}",
                getStrategyName(), request.getUserId(), request.getDeviceId(), request.getVerifyType());

        try {
            // 1. 参数验证
            if (!validateRequest(request)) {
                return VerificationResult.failed("INVALID_REQUEST", "认证请求参数无效");
            }

            // 2. 执行具体认证方式记录逻辑（由子类实现，默认返回成功）
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
    protected boolean validateRequest(VerificationRequest request) {
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
     * 执行具体认证方式记录逻辑（由子类实现）
     * <p>
     * ⚠️ 注意：不是进行人员识别，也不是验证认证方式是否允许
     * </p>
     * <p>
     * 设备端已完成：
     * - 人员识别（1:N比对，识别出pin）
     * - 认证方式验证（如果设备不支持该认证方式，设备端不会识别成功）
     * </p>
     * <p>
     * 子类可以重写此方法实现更具体的记录逻辑（例如：记录认证方式的使用次数）
     * 默认实现：返回成功，表示记录成功
     * </p>
     *
     * @param request 验证请求（包含userId、verifyType等，设备端已识别出人员编号）
     * @return 记录结果（默认返回成功）
     */
    protected VerificationResult doAuthenticate(VerificationRequest request) {
        // 默认实现：记录认证方式（用于统计和审计）
        // 子类可以重写此方法实现更具体的记录逻辑（例如：统计各认证方式的使用次数）
        log.debug("[{}] 认证方式记录: userId={}, verifyType={}",
                getStrategyName(), request.getUserId(), request.getVerifyType());
        return VerificationResult.success("认证方式记录成功", null, getVerifyType().getName().toLowerCase());
    }

    /**
     * 记录认证结果
     * <p>
     * 子类可以重写此方法实现自定义日志记录
     * </p>
     *
     * @param request 验证请求
     * @param result  认证结果
     */
    protected void logAuthenticationResult(VerificationRequest request, VerificationResult result) {
        if (result.isSuccess()) {
            log.info("[{}] 认证成功: userId={}, deviceId={}", getStrategyName(), request.getUserId(),
                    request.getDeviceId());
        } else {
            log.warn("[{}] 认证失败: userId={}, deviceId={}, errorCode={}, errorMessage={}",
                    getStrategyName(), request.getUserId(), request.getDeviceId(),
                    result.getErrorCode(), result.getMessage());
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
