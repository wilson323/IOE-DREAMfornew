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
 * ⚠️ 重要说明：多模态认证的作用
 * </p>
 * <p>
 * <strong>不是进行人员识别</strong>：
 * - 设备端已完成人员识别（人脸、指纹、卡片等）
 * - 设备端已识别出人员编号（pin字段）
 * - 软件端接收的是人员编号（pin），不是生物特征数据
 * </p>
 * <p>
 * <strong>核心职责是验证认证方式是否允许</strong>：
 * - 验证用户是否允许使用该认证方式（例如：某些区域只允许人脸，不允许密码）
 * - 验证区域配置中是否允许该认证方式
 * - 验证设备配置中是否支持该认证方式
 * - 记录认证方式（用于统计和审计）
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
public class MultiModalAuthenticationManager {

    // 显式添加logger声明以确保编译通过

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
     * 记录认证方式（用于统计和审计）
     * <p>
     * ⚠️ 注意：不是进行人员识别，也不是验证认证方式是否允许
     * </p>
     * <p>
     * 设备端已完成：
     * - 人员识别（1:N比对，识别出pin）
     * - 认证方式验证（如果设备不支持该认证方式，设备端不会识别成功）
     * </p>
     * <p>
     * 软件端只记录认证方式（verifytype）用于统计和审计
     * </p>
     * <p>
     * 根据请求中的verifyType自动选择对应的认证策略进行记录
     * </p>
     *
     * @param request 验证请求（包含userId、verifyType等，设备端已识别出人员编号）
     * @return 记录结果（始终成功，因为只是记录）
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

    /**
     * 计算认证方式统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息，包含各认证方式的使用次数
     */
    public Map<String, Object> calculateVerifyTypeStatistics(String startTime, String endTime) {
        log.debug("[多模态认证] 计算认证方式统计: startTime={}, endTime={}", startTime, endTime);

        Map<String, Object> statistics = new java.util.HashMap<>();

        // 初始化各认证方式的计数
        Map<Integer, Long> verifyTypeCount = new java.util.HashMap<>();
        for (VerifyTypeEnum verifyType : VerifyTypeEnum.values()) {
            verifyTypeCount.put(verifyType.getCode(), 0L);
        }

        // TODO: 实现实际的统计逻辑
        // 这里需要从数据库或缓存中查询指定时间范围内的认证记录
        // 按认证方式分组统计

        // 示例：设置默认值（实际应从数据库查询）
        statistics.put("totalCount", 0);
        statistics.put("faceCount", 0);
        statistics.put("fingerprintCount", 0);
        statistics.put("cardCount", 0);
        statistics.put("passwordCount", 0);
        statistics.put("qrcodeCount", 0);
        statistics.put("verifyTypeCounts", verifyTypeCount);
        statistics.put("startTime", startTime);
        statistics.put("endTime", endTime);
        statistics.put("statisticsTime", java.time.LocalDateTime.now().toString());

        log.info("[多模态认证] 认证方式统计完成: totalRecords={}", statistics.get("totalCount"));
        return statistics;
    }
}
