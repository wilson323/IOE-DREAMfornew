package net.lab1024.sa.access.service.impl;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessVerificationRequest;
import net.lab1024.sa.access.domain.dto.VerificationResult;
import net.lab1024.sa.access.monitor.AccessVerificationMetrics;
import net.lab1024.sa.access.service.AccessVerificationService;
import net.lab1024.sa.access.strategy.VerificationModeStrategy;
import net.lab1024.sa.common.organization.dao.AreaAccessExtDao;
import net.lab1024.sa.common.organization.entity.AreaAccessExtEntity;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 门禁验证服务实现
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Resource依赖注入
 * - 实现AccessVerificationService接口
 * </p>
 * <p>
 * 核心职责：
 * - 统一验证入口
 * - 根据区域配置自动选择验证策略
 * - 异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class AccessVerificationServiceImpl implements AccessVerificationService {

    @Resource
    private AreaAccessExtDao areaAccessExtDao;

    @Resource
    private List<VerificationModeStrategy> strategyList;

    @Resource
    private AccessVerificationMetrics verificationMetrics;

    /**
     * 策略映射缓存（按模式名称索引）
     */
    private Map<String, VerificationModeStrategy> strategyMap;

    /**
     * 初始化策略映射
     */
    private void initStrategyMap() {
        if (strategyMap == null) {
            strategyMap = strategyList.stream()
                    .collect(Collectors.toMap(
                            strategy -> {
                                // 根据策略类名推断模式
                                if (strategy.supports("edge")) {
                                    return "edge";
                                } else if (strategy.supports("backend")) {
                                    return "backend";
                                }
                                return "unknown";
                            },
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));
            log.info("[验证服务] 策略映射初始化完成: {}", strategyMap.keySet());
        }
    }

    @Override
    @Timed(value = "access.verification.duration", description = "门禁验证耗时", 
           percentiles = {0.5, 0.9, 0.95, 0.99})
    @Counted(value = "access.verification.count", description = "门禁验证次数")
    public VerificationResult verifyAccess(AccessVerificationRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("[验证服务] 开始验证: userId={}, deviceId={}, areaId={}",
                request.getUserId(), request.getDeviceId(), request.getAreaId());

        try {
            // 记录验证总数
            verificationMetrics.recordVerificationTotal();

            // 1. 获取区域验证模式
            String mode = getVerificationMode(request.getAreaId());
            log.info("[验证服务] 区域验证模式: areaId={}, mode={}", request.getAreaId(), mode);

            // 2. 初始化策略映射
            initStrategyMap();

            // 3. 选择验证策略
            VerificationModeStrategy strategy = strategyMap.get(mode);
            if (strategy == null) {
                log.error("[验证服务] 不支持的验证模式: areaId={}, mode={}", request.getAreaId(), mode);
                verificationMetrics.recordVerificationFailed();
                return VerificationResult.failed("UNSUPPORTED_MODE", "不支持的验证模式: " + mode);
            }

            // 4. 执行验证
            VerificationResult result = strategy.verify(request);

            // 5. 记录验证结果
            if (result.isSuccess()) {
                verificationMetrics.recordVerificationSuccess();
            } else {
                verificationMetrics.recordVerificationFailed();
                // 根据错误码记录具体失败原因
                if (result.getErrorCode() != null) {
                    switch (result.getErrorCode()) {
                        case "ANTI_PASSBACK_VIOLATION":
                            verificationMetrics.recordAntiPassbackViolation();
                            break;
                        case "INTERLOCK_VIOLATION":
                            verificationMetrics.recordInterlockViolation();
                            break;
                        case "MULTI_PERSON_WAITING":
                            verificationMetrics.recordMultiPersonWaiting();
                            break;
                        case "BLACKLIST_REJECTION":
                            verificationMetrics.recordBlacklistRejection();
                            break;
                        case "TIME_PERIOD_REJECTION":
                            verificationMetrics.recordTimePeriodRejection();
                            break;
                    }
                }
            }

            // 6. 记录验证耗时
            long duration = System.currentTimeMillis() - startTime;
            verificationMetrics.recordVerificationDuration(duration);

            log.info("[验证服务] 验证完成: userId={}, mode={}, result={}, duration={}ms",
                    request.getUserId(), mode, result.getAuthStatus(), duration);

            return result;

        } catch (Exception e) {
            log.error("[验证服务] 验证异常: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            verificationMetrics.recordVerificationFailed();
            long duration = System.currentTimeMillis() - startTime;
            verificationMetrics.recordVerificationDuration(duration);
            return VerificationResult.failed("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    public String getVerificationMode(Long areaId) {
        if (areaId == null) {
            log.warn("[验证服务] 区域ID为空，使用默认模式: edge");
            return "edge";
        }

        try {
            AreaAccessExtEntity areaExt = areaAccessExtDao.selectByAreaId(areaId);
            if (areaExt == null) {
                log.warn("[验证服务] 区域扩展信息不存在: areaId={}, 使用默认模式: edge", areaId);
                return "edge";
            }

            String mode = areaExt.getVerificationMode();
            if (mode == null || mode.trim().isEmpty()) {
                log.warn("[验证服务] 验证模式未配置: areaId={}, 使用默认模式: edge", areaId);
                return "edge";
            }

            return mode.trim().toLowerCase();

        } catch (Exception e) {
            log.error("[验证服务] 获取验证模式异常: areaId={}, error={}", areaId, e.getMessage(), e);
            return "edge"; // 异常时使用默认模式
        }
    }
}
