package net.lab1024.sa.access.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 门禁验证监控指标收集器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Component注解注册为Spring Bean
 * - 使用Micrometer收集业务指标
 * - 提供完整的监控指标
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AccessVerificationMetrics {

    @Resource
    private MeterRegistry meterRegistry;

    private Counter verificationTotalCounter;
    private Counter verificationSuccessCounter;
    private Counter verificationFailedCounter;
    private Counter antiPassbackViolationCounter;
    private Counter interlockViolationCounter;
    private Counter multiPersonWaitingCounter;
    private Counter blacklistRejectionCounter;
    private Counter timePeriodRejectionCounter;
    private Timer verificationTimer;

    /**
     * 初始化监控指标
     */
    @PostConstruct
    public void initMetrics() {
        // 验证总数
        verificationTotalCounter = Counter.builder("access.verification.total")
                .description("门禁验证总次数")
                .tag("service", "access-service")
                .register(meterRegistry);

        // 验证成功数
        verificationSuccessCounter = Counter.builder("access.verification.success")
                .description("门禁验证成功次数")
                .tag("service", "access-service")
                .register(meterRegistry);

        // 验证失败数
        verificationFailedCounter = Counter.builder("access.verification.failed")
                .description("门禁验证失败次数")
                .tag("service", "access-service")
                .register(meterRegistry);

        // 反潜违规数
        antiPassbackViolationCounter = Counter.builder("access.anti_passback.violation")
                .description("反潜违规次数")
                .tag("service", "access-service")
                .register(meterRegistry);

        // 互锁违规数
        interlockViolationCounter = Counter.builder("access.interlock.violation")
                .description("互锁违规次数")
                .tag("service", "access-service")
                .register(meterRegistry);

        // 多人验证等待数
        multiPersonWaitingCounter = Counter.builder("access.multi_person.waiting")
                .description("多人验证等待次数")
                .tag("service", "access-service")
                .register(meterRegistry);

        // 黑名单拒绝数
        blacklistRejectionCounter = Counter.builder("access.blacklist.rejection")
                .description("黑名单拒绝次数")
                .tag("service", "access-service")
                .register(meterRegistry);

        // 时间段拒绝数
        timePeriodRejectionCounter = Counter.builder("access.time_period.rejection")
                .description("时间段拒绝次数")
                .tag("service", "access-service")
                .register(meterRegistry);

        // 验证耗时
        verificationTimer = Timer.builder("access.verification.duration")
                .description("门禁验证耗时")
                .tag("service", "access-service")
                .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                .register(meterRegistry);

        log.info("[监控指标] 门禁验证监控指标初始化完成");
    }

    /**
     * 记录验证总数
     */
    public void recordVerificationTotal() {
        verificationTotalCounter.increment();
    }

    /**
     * 记录验证成功
     */
    public void recordVerificationSuccess() {
        verificationSuccessCounter.increment();
    }

    /**
     * 记录验证失败
     */
    public void recordVerificationFailed() {
        verificationFailedCounter.increment();
    }

    /**
     * 记录反潜违规
     */
    public void recordAntiPassbackViolation() {
        antiPassbackViolationCounter.increment();
    }

    /**
     * 记录互锁违规
     */
    public void recordInterlockViolation() {
        interlockViolationCounter.increment();
    }

    /**
     * 记录多人验证等待
     */
    public void recordMultiPersonWaiting() {
        multiPersonWaitingCounter.increment();
    }

    /**
     * 记录黑名单拒绝
     */
    public void recordBlacklistRejection() {
        blacklistRejectionCounter.increment();
    }

    /**
     * 记录时间段拒绝
     */
    public void recordTimePeriodRejection() {
        timePeriodRejectionCounter.increment();
    }

    /**
     * 记录验证耗时
     *
     * @param duration 耗时（毫秒）
     */
    public void recordVerificationDuration(long duration) {
        verificationTimer.record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取Timer实例（用于手动计时）
     *
     * @return Timer实例
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 停止计时并记录
     *
     * @param sample Timer样本
     */
    public void stopTimer(Timer.Sample sample) {
        sample.stop(verificationTimer);
    }
}
