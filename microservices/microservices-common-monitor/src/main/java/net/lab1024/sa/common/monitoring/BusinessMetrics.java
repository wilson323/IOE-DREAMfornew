package net.lab1024.sa.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 业务指标收集器
 * <p>
 * 基于Micrometer的业务指标监控
 * 严格遵循CLAUDE.md规范：
 * - 使用Micrometer标准指标收集
 * - 支持Prometheus导出
 * - 提供业务特定指标
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Component
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * 构造函数注入MeterRegistry
     */
    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * 记录通行事件
     * <p>
     * 记录门禁通行成功/失败事件
     * </p>
     *
     * @param result 通行结果（SUCCESS/FAILURE）
     */
    public void recordAccessEvent(String result) {
        Counter.builder("access.event")
                .tag("result", result)
                .description("门禁通行事件统计")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 记录API响应时间
     * <p>
     * 记录API接口的响应时间，用于性能监控
     * </p>
     *
     * @param api API路径
     * @param duration 响应时间（毫秒）
     */
    public void recordResponseTime(String api, long duration) {
        Timer.builder("api.response.time")
                .tag("api", api)
                .description("API响应时间")
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录生物识别性能
     * <p>
     * 记录生物识别操作的耗时和成功率
     * </p>
     *
     * @param type 生物识别类型（FACE/FINGERPRINT/IRIS等）
     * @param duration 识别耗时（毫秒）
     * @param success 是否成功
     */
    public void recordBiometricPerformance(String type, long duration, boolean success) {
        Timer.builder("biometric.recognition.time")
                .tag("type", type)
                .tag("success", String.valueOf(success))
                .description("生物识别性能指标")
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录消费事件
     * <p>
     * 记录消费成功/失败事件
     * </p>
     *
     * @param result 消费结果（SUCCESS/FAILURE）
     * @param amount 消费金额
     */
    public void recordConsumeEvent(String result, double amount) {
        Counter.builder("consume.event")
                .tag("result", result)
                .description("消费事件统计")
                .register(meterRegistry)
                .increment();

        if ("SUCCESS".equals(result)) {
            meterRegistry.gauge("consume.amount", amount);
        }
    }

    /**
     * 记录考勤打卡事件
     * <p>
     * 记录考勤打卡成功/失败事件
     * </p>
     *
     * @param result 打卡结果（SUCCESS/FAILURE）
     */
    public void recordAttendanceEvent(String result) {
        Counter.builder("attendance.event")
                .tag("result", result)
                .description("考勤打卡事件统计")
                .register(meterRegistry)
                .increment();
    }
}
