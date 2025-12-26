package net.lab1024.sa.consume.monitor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import net.lab1024.sa.consume.domain.vo.ConsumeTransactionVO;

/**
 * 消费交易监控组件（简化版）
 * <p>
 * 临时替代版本，解决Lombok配置问题
 * 核心功能：交易监控和统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0-simplified
 * @since 2025-12-22
 */
@Component
@Slf4j
public class ConsumeTransactionMonitorSimple {


    private final MeterRegistry meterRegistry;

    // 交易计数器
    private final Counter transactionSuccessCounter;
    private final Counter transactionFailureCounter;

    // 交易计时器
    private final Timer transactionTimer;

    // 统计数据
    private final AtomicLong totalTransactionAmount = new AtomicLong(0);
    private final AtomicLong todayTransactionAmount = new AtomicLong(0);
    private final AtomicLong todayTransactionCount = new AtomicLong(0);

    public ConsumeTransactionMonitorSimple(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.transactionSuccessCounter = Counter.builder("consume_transaction_success")
                .description("成功交易计数")
                .register(meterRegistry);

        this.transactionFailureCounter = Counter.builder("consume_transaction_failure")
                .description("失败交易计数")
                .register(meterRegistry);

        this.transactionTimer = Timer.builder("consume_transaction_duration")
                .description("交易处理时间")
                .register(meterRegistry);
    }

    /**
     * 记录成功交易
     */
    public void recordSuccessTransaction(ConsumeTransactionVO transaction) {
        transactionSuccessCounter.increment();

        // 累计金额（转换为分）
        long amountInCents = transaction.getAmount().multiply(new BigDecimal("100")).longValue();
        totalTransactionAmount.addAndGet(amountInCents);
        todayTransactionAmount.addAndGet(amountInCents);
        todayTransactionCount.incrementAndGet();

        log.info("[监控] 交易成功记录完成");
    }

    /**
     * 记录失败交易
     */
    public void recordFailureTransaction(ConsumeTransactionVO transaction, String reason) {
        transactionFailureCounter.increment();

        log.warn("[监控] 交易失败: reason={}", reason);
    }

    /**
     * 记录交易处理时间
     */
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 简化的统计信息
     */
    public String getStatisticsSummary() {
        return String.format("今日交易: %d笔, 总额: %.2f元",
            todayTransactionCount.get(),
            todayTransactionAmount.get() / 100.0);
    }

    /**
     * 重置今日统计
     */
    public void resetTodayStatistics() {
        todayTransactionCount.set(0);
        todayTransactionAmount.set(0);
        log.info("[监控] 今日统计数据已重置");
    }
}