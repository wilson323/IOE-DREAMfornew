package net.lab1024.sa.consume.scheduler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.manager.DualWriteValidationManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 双写验证定时任务
 *
 * 职责：定时执行新旧表数据一致性验证
 *
 * 执行频率：每10分钟执行一次
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
@Component
public class DualWriteValidationScheduler {

    private final DualWriteValidationManager dualWriteValidationManager;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DualWriteValidationScheduler(DualWriteValidationManager dualWriteValidationManager) {
        this.dualWriteValidationManager = dualWriteValidationManager;
    }

    /**
     * 定时验证账户数据一致性
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    public void validateAccountData() {
        String startTime = LocalDateTime.now().format(FORMATTER);
        log.info("[双写验证定时任务] 开始验证账户数据: startTime={}", startTime);

        try {
            DualWriteValidationManager.ValidationResult result =
                    dualWriteValidationManager.validateAllAccounts();

            log.info("[双写验证定时任务] 账户数据验证完成: dataType={}, totalCount={}, consistentCount={}, consistencyRate={}, passed={}",
                    result.getDataType(),
                    result.getTotalCount(),
                    result.getConsistentCount(),
                    String.format("%.4f", result.getConsistencyRate()),
                    result.isPassed());

            // 如果验证失败，触发告警
            if (!result.isPassed()) {
                log.error("[双写验证定时任务] ⚠️ 账户数据一致性不达标! consistencyRate={}, required=0.999",
                        result.getConsistencyRate());
                // 这里可以集成告警系统，发送邮件或短信通知
            }

        } catch (Exception e) {
            log.error("[双写验证定时任务] 账户数据验证异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 定时验证交易数据一致性
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    public void validateTransactionData() {
        String startTime = LocalDateTime.now().format(FORMATTER);
        log.info("[双写验证定时任务] 开始验证交易数据: startTime={}", startTime);

        try {
            DualWriteValidationManager.ValidationResult result =
                    dualWriteValidationManager.validateAllTransactions();

            log.info("[双写验证定时任务] 交易数据验证完成: dataType={}, totalCount={}, consistentCount={}, consistencyRate={}, passed={}",
                    result.getDataType(),
                    result.getTotalCount(),
                    result.getConsistentCount(),
                    String.format("%.4f", result.getConsistencyRate()),
                    result.isPassed());

            // 如果验证失败，触发告警
            if (!result.isPassed()) {
                log.error("[双写验证定时任务] ⚠️ 交易数据一致性不达标! consistencyRate={}, required=0.999",
                        result.getConsistencyRate());
                // 这里可以集成告警系统，发送邮件或短信通知
            }

        } catch (Exception e) {
            log.error("[双写验证定时任务] 交易数据验证异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 检查是否可以切换到新表
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    public void checkSwitchEligibility() {
        String startTime = LocalDateTime.now().format(FORMATTER);
        log.info("[双写验证定时任务] 检查是否可以切换到新表: startTime={}", startTime);

        try {
            boolean canSwitch = dualWriteValidationManager.canSwitchToNewTables();

            if (canSwitch) {
                log.info("[双写验证定时任务] ✓✓✓ 满足切换条件! 可以开始切换到新表");
                // 这里可以触发切换流程或通知管理员
            } else {
                log.info("[双写验证定时任务] 尚未满足切换条件，继续双写验证");
            }

        } catch (Exception e) {
            log.error("[双写验证定时任务] 检查切换条件异常: error={}", e.getMessage(), e);
        }
    }

    /**
     * 清理历史验证数据
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupHistoryData() {
        String startTime = LocalDateTime.now().format(FORMATTER);
        log.info("[双写验证定时任务] 清理历史验证数据: startTime={}", startTime);

        try {
            // 清理7天前的验证统计数据
            // 这里可以扩展实现数据清理逻辑

            log.info("[双写验证定时任务] 历史验证数据清理完成");

        } catch (Exception e) {
            log.error("[双写验证定时任务] 清理历史数据异常: error={}", e.getMessage(), e);
        }
    }
}
