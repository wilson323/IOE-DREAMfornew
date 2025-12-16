package net.lab1024.sa.consume.service.engine;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.form.consume.ConsumeRequestForm;
import net.lab1024.sa.consume.domain.vo.consume.RiskAssessmentVO;
import net.lab1024.sa.consume.service.risk.RiskRuleEngine;
import net.lab1024.sa.consume.service.risk.model.RiskFactor;
import net.lab1024.sa.consume.service.risk.model.RiskLevel;
import net.lab1024.sa.consume.service.risk.model.RiskAssessmentResult;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 风险评估引擎
 * 实现P0级实时风险检测和异常分析
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Service
public class RiskAssessmentEngine {

    private final RiskRuleEngine ruleEngine;

    @Resource
    public RiskAssessmentEngine(RiskRuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    /**
     * 评估支付风险 - P0级核心功能
     * 实时风险评估和异常检测
     *
     * @param request 支付请求
     * @return 风险评估结果
     */
    public CompletableFuture<RiskAssessmentResult> assessPaymentRisk(ConsumeRequestForm request) {
        log.debug("[风险评估] 开始评估支付风险: userId={}, amount={}", request.getUserId(), request.getAmount());

        return CompletableFuture.supplyAsync(() -> {
            try {
                RiskAssessmentResult result = new RiskAssessmentResult();
                List<RiskFactor> riskFactors = new ArrayList<>();

                // 1. 金额风险检查
                assessAmountRisk(request, riskFactors);

                // 2. 频率风险检查
                assessFrequencyRisk(request, riskFactors);

                // 3. 设备风险检查
                assessDeviceRisk(request, riskFactors);

                // 4. 时间风险检查
                assessTimeRisk(request, riskFactors);

                // 5. 用户行为风险检查
                assessUserBehaviorRisk(request, riskFactors);

                // 6. 地理位置风险检查
                assessLocationRisk(request, riskFactors);

                // 7. 商户风险检查
                assessMerchantRisk(request, riskFactors);

                result.setRiskFactors(riskFactors);
                result.setRiskLevel(calculateRiskLevel(riskFactors));
                result.setRiskScore(calculateRiskScore(riskFactors));
                result.setRecommendation(generateRecommendation(result));
                result.setAssessmentTime(System.currentTimeMillis());

                log.debug("[风险评估] 风险评估完成: level={}, score={}, factors={}",
                        result.getRiskLevel(), result.getRiskScore(), riskFactors.size());

                return result;

            } catch (Exception e) {
                log.error("[风险评估] 评估异常", e);
                // 异常情况下返回高风险
                return RiskAssessmentResult.highRisk("风险评估异常: " + e.getMessage());
            }
        });
    }

    /**
     * 实时风险监控
     *
     * @param request 支付请求
     * @return 是否高风险
     */
    public boolean isHighRisk(ConsumeRequestForm request) {
        try {
            RiskAssessmentResult result = assessPaymentRisk(request).get();
            return result.getRiskLevel() == RiskLevel.HIGH;
        } catch (Exception e) {
            log.error("[风险评估] 实时风险检查异常", e);
            return true; // 异常情况下视为高风险
        }
    }

    /**
     * 更新风险模型
     *
     * @param transactionResult 交易结果
     */
    public void updateRiskModel(PaymentResult transactionResult) {
        try {
            // 根据交易结果更新风险模型
            ruleEngine.updateRiskModel(transactionResult);
            log.debug("[风险评估] 风险模型更新完成: transactionId={}", transactionResult.getTransactionId());
        } catch (Exception e) {
            log.error("[风险评估] 更新风险模型异常", e);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 评估金额风险
     */
    private void assessAmountRisk(ConsumeRequestForm request, List<RiskFactor> riskFactors) {
        BigDecimal amount = request.getAmount();

        // 大额交易风险
        if (amount.compareTo(new BigDecimal("1000")) >= 0) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("AMOUNT_HIGH")
                    .description("大额交易风险")
                    .riskScore(30)
                    .severity("HIGH")
                    .build());
        }

        // 异常金额交易风险
        if (amount.compareTo(new BigDecimal("0.01")) <= 0) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("AMOUNT_ANOMALOUS")
                    .description("异常金额交易")
                    .riskScore(50)
                    .severity("HIGH")
                    .build());
        }

        // 精确金额风险（可能是测试或攻击）
        if (isRoundAmount(amount)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("AMOUNT_ROUND")
                    .description("精确金额风险")
                    .riskScore(20)
                    .severity("MEDIUM")
                    .build());
        }
    }

    /**
     * 评估频率风险
     */
    private void assessFrequencyRisk(ConsumeRequestForm request, List<RiskFactor> riskFactors) {
        // 这里应该实现基于用户历史交易数据的频率分析
        // 简化实现，实际应该查询数据库

        // 短时间内多笔交易风险
        int recentTransactionCount = getRecentTransactionCount(request.getUserId(), 60); // 最近1分钟

        if (recentTransactionCount >= 5) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("FREQUENCY_HIGH")
                    .description("高频交易风险")
                    .riskScore(40)
                    .severity("HIGH")
                    .build());
        }
    }

    /**
     * 评估设备风险
     */
    private void assessDeviceRisk(ConsumeRequestForm request, List<RiskFactor> riskFactors) {
        String deviceId = request.getDeviceId();

        // 设备白名单检查
        if (!isDeviceInWhitelist(deviceId)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("DEVICE_UNKNOWN")
                    .description("未知设备风险")
                    .riskScore(25)
                    .severity("MEDIUM")
                    .build());
        }

        // 设备异常行为检查
        if (hasDeviceAnomalousBehavior(deviceId)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("DEVICE_ANOMALY")
                    .description("设备异常行为")
                    .riskScore(35)
                    .severity("HIGH")
                    .build());
        }
    }

    /**
     * 评估时间风险
     */
    private void assessTimeRisk(ConsumeRequestForm request, List<RiskFactor> riskFactors) {
        long currentTime = System.currentTimeMillis();
        long hourInMillis = 60 * 60 * 1000;
        int currentHour = (int) ((currentTime / hourInMillis) % 24);

        // 深夜时段风险
        if (currentHour >= 23 || currentHour <= 5) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("TIME_LATE_NIGHT")
                    .description("深夜时段交易")
                    .riskScore(20)
                    .severity("LOW")
                    .build());
        }

        // 异常时间模式风险
        if (hasAnomalousTimePattern(request.getUserId(), currentTime)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("TIME_PATTERN_ANOMALY")
                    .description("异常时间模式")
                    .riskScore(30)
                    .severity("MEDIUM")
                    .build());
        }
    }

    /**
     * 评估用户行为风险
     */
    private void assessUserBehaviorRisk(ConsumeRequestForm request, List<RiskFactor> riskFactors) {
        Long userId = request.getUserId();

        // 新用户风险
        if (isNewUser(userId)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("USER_NEW")
                    .description("新用户风险")
                    .riskScore(15)
                    .severity("LOW")
                    .build());
        }

        // 用户行为异常
        if (hasAnomalousUserBehavior(userId)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("USER_BEHAVIOR_ANOMALY")
                    .description("用户行为异常")
                    .riskScore(45)
                    .severity("HIGH")
                    .build());
        }
    }

    /**
     * 评估地理位置风险
     */
    private void assessLocationRisk(ConsumeRequestForm request, List<RiskFactor> riskFactors) {
        String locationId = request.getLocationId();

        // 异常地点交易
        if (!isUserUsualLocation(request.getUserId(), locationId)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("LOCATION_ANOMALOUS")
                    .description("异常地点交易")
                    .riskScore(25)
                    .severity("MEDIUM")
                    .build());
        }

        // 高风险地区交易
        if (isHighRiskLocation(locationId)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("LOCATION_HIGH_RISK")
                    .description("高风险地区交易")
                    .riskScore(50)
                    .severity("HIGH")
                    .build());
        }
    }

    /**
     * 评估商户风险
     */
    private void assessMerchantRisk(ConsumeRequestForm request, List<RiskFactor> riskFactors) {
        Long merchantId = request.getMerchantId();

        // 新商户风险
        if (isNewMerchant(merchantId)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("MERCHANT_NEW")
                    .description("新商户风险")
                    .riskScore(10)
                    .severity("LOW")
                    .build());
        }

        // 高风险商户
        if (isHighRiskMerchant(merchantId)) {
            riskFactors.add(RiskFactor.builder()
                    .factorType("MERCHANT_HIGH_RISK")
                    .description("高风险商户")
                    .riskScore(40)
                    .severity("HIGH")
                    .build());
        }
    }

    /**
     * 计算风险等级
     */
    private RiskLevel calculateRiskLevel(List<RiskFactor> riskFactors) {
        if (riskFactors.isEmpty()) {
            return RiskLevel.LOW;
        }

        int highRiskCount = 0;
        int mediumRiskCount = 0;

        for (RiskFactor factor : riskFactors) {
            if ("HIGH".equals(factor.getSeverity())) {
                highRiskCount++;
            } else if ("MEDIUM".equals(factor.getSeverity())) {
                mediumRiskCount++;
            }
        }

        // 有任何高风险因素或3个以上中风险因素
        if (highRiskCount > 0 || mediumRiskCount >= 3) {
            return RiskLevel.HIGH;
        }

        // 有1-2个中风险因素
        if (mediumRiskCount >= 1) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.LOW;
    }

    /**
     * 计算风险分数
     */
    private int calculateRiskScore(List<RiskFactor> riskFactors) {
        return riskFactors.stream()
                .mapToInt(RiskFactor::getRiskScore)
                .sum();
    }

    /**
     * 生成建议
     */
    private String generateRecommendation(RiskAssessmentResult result) {
        switch (result.getRiskLevel()) {
            case HIGH:
                return "建议拒绝交易并进行人工审核";
            case MEDIUM:
                return "建议要求额外验证或降低交易限额";
            case LOW:
                return "交易风险较低，可以正常处理";
            default:
                return "请进行进一步评估";
        }
    }

    // ==================== 辅助方法 ====================

    private boolean isRoundAmount(BigDecimal amount) {
        return amount.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }

    private int getRecentTransactionCount(Long userId, int seconds) {
        // 实际实现应该查询数据库获取指定时间内的交易次数
        // 这里简化返回0
        return 0;
    }

    private boolean isDeviceInWhitelist(String deviceId) {
        // 实际实现应该查询设备白名单
        return false;
    }

    private boolean hasDeviceAnomalousBehavior(String deviceId) {
        // 实际实现应该分析设备行为模式
        return false;
    }

    private boolean hasAnomalousTimePattern(Long userId, long currentTime) {
        // 实现应该分析用户交易时间模式
        return false;
    }

    private boolean isNewUser(Long userId) {
        // 实际实现应该检查用户创建时间和交易历史
        return false;
    }

    private boolean hasAnomalousUserBehavior(Long userId) {
        // 实际实现应该分析用户行为模式
        return false;
    }

    private boolean isUserUsualLocation(Long userId, String locationId) {
        // 实际实现应该分析用户常用地点
        return false;
    }

    private boolean isHighRiskLocation(String locationId) {
        // 实际实现应该查询高风险地区列表
        return false;
    }

    private boolean isNewMerchant(Long merchantId) {
        // 实际实现应该检查商户注册时间和交易历史
        return false;
    }

    private boolean isHighRiskMerchant(Long merchantId) {
        // 实现实现应该查询高风险商户名单
        return false;
    }
}