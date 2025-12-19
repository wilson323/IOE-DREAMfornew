package net.lab1024.sa.access.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.domain.enumeration.VerifyTypeEnum;
import net.lab1024.sa.access.strategy.MultiModalAuthenticationStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 多模态认证管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在配置类中注册为Bean
 * </p>
 * <p>
 * 核心职责：
 * - 统一管理9种认证策略
 * - 根据认证方式自动选择策略
 * - 支持多因子认证（融合认证）
 * - 提供认证方式统计和分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class MultiModalAuthenticationManager {

    /**
     * 认证策略映射（按认证方式代码索引）
     */
    private final Map<Integer, MultiModalAuthenticationStrategy> strategyMap;

    /**
     * 构造函数注入依赖
     *
     * @param strategyList 认证策略列表（Spring自动注入所有实现类）
     */
    public MultiModalAuthenticationManager(List<MultiModalAuthenticationStrategy> strategyList) {
        // 初始化策略映射
        this.strategyMap = strategyList.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getVerifyType().getCode(),
                        strategy -> strategy,
                        (existing, replacement) -> {
                            log.warn("[多模态认证] 策略重复注册，保留现有策略: {}",
                                    existing.getStrategyName());
                            return existing;
                        }
                ));

        log.info("[多模态认证] 策略映射初始化完成: 共{}种认证方式", strategyMap.size());
    }

    /**
     * 执行认证
     * <p>
     * 根据请求中的verifyType自动选择对应的认证策略
     * </p>
     *
     * @param request 验证请求
     * @return 认证结果
     */
    public VerificationResult authenticate(AccessVerificationRequest request) {
        if (request == null || request.getVerifyType() == null) {
            log.warn("[多模态认证] 认证请求或认证方式为空");
            return VerificationResult.failed("INVALID_REQUEST", "认证请求或认证方式为空");
        }

        // 获取对应的认证策略
        MultiModalAuthenticationStrategy strategy = strategyMap.get(request.getVerifyType());
        if (strategy == null) {
            log.warn("[多模态认证] 不支持的认证方式: verifyType={}", request.getVerifyType());
            return VerificationResult.failed("UNSUPPORTED_VERIFY_TYPE",
                    "不支持的认证方式: " + request.getVerifyType());
        }

        // 执行认证
        return strategy.authenticate(request);
    }

    /**
     * 获取认证策略
     *
     * @param verifyType 认证方式代码
     * @return 认证策略，如果不存在则返回null
     */
    public MultiModalAuthenticationStrategy getStrategy(Integer verifyType) {
        return strategyMap.get(verifyType);
    }

    /**
     * 获取所有支持的认证方式
     *
     * @return 认证方式枚举列表
     */
    public List<VerifyTypeEnum> getSupportedVerifyTypes() {
        return strategyMap.values().stream()
                .map(MultiModalAuthenticationStrategy::getVerifyType)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 检查是否支持指定的认证方式
     *
     * @param verifyType 认证方式代码
     * @return 是否支持
     */
    public boolean supports(Integer verifyType) {
        return strategyMap.containsKey(verifyType);
    }

    /**
     * 获取认证方式描述
     *
     * @param verifyType 认证方式代码
     * @return 认证方式描述，如果不存在则返回"未知"
     */
    public String getVerifyTypeDescription(Integer verifyType) {
        VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.getByCode(verifyType);
        return verifyTypeEnum != null ? verifyTypeEnum.getDescription() : "未知";
    }
}
