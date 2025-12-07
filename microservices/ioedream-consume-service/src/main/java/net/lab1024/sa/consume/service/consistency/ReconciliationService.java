package net.lab1024.sa.consume.service.consistency;

import java.time.LocalDate;

import lombok.Data;

/**
 * 对账服务接口
 * <p>
 * 提供消费对账相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回业务对象，不返回ResponseDTO
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
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
     * 执行实时对账
     *
     * @param accountId 账户ID
     * @return 对账结果
     */
    ReconciliationResult performRealTimeReconciliation(Long accountId);

    /**
     * 查询对账历史
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 对账历史列表
     */
    ReconciliationHistoryResult queryReconciliationHistory(
            LocalDate startDate, 
            LocalDate endDate, 
            Integer pageNum, 
            Integer pageSize);

    /**
     * 对账结果
     */
    @Data
    class ReconciliationResult {
        /**
         * 对账日期
         */
        private LocalDate reconcileDate;

        /**
         * 对账账户数
         */
        private Integer accountCount;

        /**
         * 一致账户数
         */
        private Integer matchedCount;

        /**
         * 不一致账户数
         */
        private Integer unmatchedCount;

        /**
         * 对账状态
         */
        private String status;

        /**
         * 对账消息
         */
        private String message;
    }

    /**
     * 对账历史结果
     */
    @Data
    class ReconciliationHistoryResult {
        /**
         * 对账记录列表
         */
        private java.util.List<ReconciliationRecord> records;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 当前页码
         */
        private Integer pageNum;

        /**
         * 每页大小
         */
        private Integer pageSize;
    }

    /**
     * 对账记录
     */
    @Data
    class ReconciliationRecord {
        /**
         * 对账ID
         */
        private Long id;

        /**
         * 对账日期
         */
        private LocalDate reconcileDate;

        /**
         * 账户ID
         */
        private Long accountId;

        /**
         * 对账状态
         */
        private String status;

        /**
         * 对账时间
         */
        private java.time.LocalDateTime reconcileTime;
    }
}
