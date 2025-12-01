package net.lab1024.sa.admin.module.consume.service.consistency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import net.lab1024.sa.base.common.domain.PageResult;

/**
 * 对账服务接口
 * 负责消费模块的数据对账、一致性检查、差异修复等功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ReconciliationService {

    /**
     * 执行日终对账
     *
     * @param reconcileDate 对账日期
     * @return 对账结果
     */
    ReconciliationResult performDailyReconciliation(LocalDate reconcileDate);

    /**
     * 修复账户余额差异
     *
     * @param discrepancy 差异信息
     * @param adjustType 调整类型（SYSTEM/RECORD）
     * @param operatorId 操作人ID
     * @return 修复结果
     */
    boolean fixAccountBalanceDiscrepancy(AccountDiscrepancy discrepancy, String adjustType, Long operatorId);

    /**
     * 查询对账历史
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 对账历史分页结果
     */
    PageResult<ReconciliationResult> queryReconciliationHistory(LocalDate startDate, LocalDate endDate,
                                                               int pageNum, int pageSize);

    // 内部接口定义
    interface ReconciliationResult {
        LocalDate getReconcileDate();
        boolean isSuccess();
        String getErrorMessage();
        boolean hasDiscrepancies();
        int getTotalDiscrepancyCount();
        String getSummary();
    }

    interface AccountDiscrepancy {
        Long getAccountId();
        Long getPersonId();
        String getPersonName();
        BigDecimal getSystemBalance();
        BigDecimal getCalculatedBalance();
        BigDecimal getDifference();
        String getDiscrepancyType();
        LocalDate getReconcileDate();
    }
}