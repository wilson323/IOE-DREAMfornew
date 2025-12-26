package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionVO;

/**
 * 消费交易服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 定义交易相关的业务服务接口
 * - 包含交易执行、查询、统计等功能
 * - 支持事务管理和并发控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface ConsumeTransactionService {

    /**
     * 执行消费交易
     * <p>
     * 核心业务方法，包含完整的事务管理和并发控制：
     * - 参数验证
     * - 账户状态检查
     * - 余额验证
     * - 分布式锁保护
     * - 幂等性检查
     * - 交易记录创建
     * - 监控日志记录
     * </p>
     *
     * @param userId   用户ID
     * @param amount   交易金额
     * @param deviceId 设备ID
     * @param mealId   餐次ID
     * @return 交易结果
     */
    ConsumeTransactionVO executeTransaction(Long userId, BigDecimal amount, String deviceId, String mealId);

    /**
     * 交易撤销
     * <p>
     * 撤销已完成的交易，包含：
     * - 原交易状态验证
     * - 账户余额恢复
     * - 撤销记录创建
     * - 审计日志记录
     * </p>
     *
     * @param transactionId 交易ID
     * @param reason        撤销原因
     * @return 撤销结果
     */
    Boolean cancelTransaction(String transactionId, String reason);

    /**
     * 分页查询交易记录
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeTransactionVO> queryPage(ConsumeTransactionQueryForm queryForm);

    /**
     * 根据ID查询交易详情
     *
     * @param transactionId 交易ID
     * @return 交易详情
     */
    ConsumeTransactionVO getById(String transactionId);

    /**
     * 获取交易统计信息
     * <p>
     * 返回多维度的交易统计数据：
     * - 今日统计
     * - 历史统计
     * - 按餐次统计
     * - 按设备统计
     * - 按小时统计
     * </p>
     *
     * @param userId   用户ID（可选）
     * @param deviceId 设备ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 统计信息
     */
    ConsumeTransactionStatisticsVO getStatistics(Long userId, String deviceId, String startDate, String endDate);

    /**
     * 获取交易趋势数据
     *
     * @param userId   用户ID（可选）
     * @param deviceId 设备ID（可选）
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param type     趋势类型（day/hour/meal）
     * @return 趋势数据
     */
    Map<String, Object> getTransactionTrend(Long userId, String deviceId, String startDate, String endDate, String type);

    /**
     * 根据用户ID查询交易记录
     *
     * @param userId 用户ID
     * @param limit  限制条数
     * @return 交易记录列表
     */
    java.util.List<ConsumeTransactionVO> getByUserId(Long userId, Integer limit);

    /**
     * 根据设备ID查询交易记录
     *
     * @param deviceId 设备ID
     * @param limit    限制条数
     * @return 交易记录列表
     */
    java.util.List<ConsumeTransactionVO> getByDeviceId(String deviceId, Integer limit);

    /**
     * 获取指定日期的交易汇总信息
     *
     * @param date 日期（格式：yyyy-MM-dd）
     * @return 汇总信息
     */
    Map<String, Object> getDailySummary(String date);

    /**
     * 查询异常交易记录
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 异常交易记录
     */
    PageResult<ConsumeTransactionVO> getAbnormalTransactions(Integer pageNum, Integer pageSize);

    /**
     * 重试失败的交易
     *
     * @param transactionId 交易ID
     * @return 重试结果
     */
    ConsumeTransactionVO retryTransaction(String transactionId);

    /**
     * 导出交易记录
     *
     * @param queryForm 查询条件
     * @return 导出结果
     */
    String exportTransactions(ConsumeTransactionQueryForm queryForm);

    // ==================== 高级功能方法 ====================

    /**
     * 批量执行交易（用于批量扣费场景）
     *
     * @param transactions 交易列表
     * @return 批量处理结果
     */
    java.util.Map<String, Object> batchExecuteTransactions(java.util.List<Map<String, Object>> transactions);

    /**
     * 获取实时交易监控数据
     *
     * @return 监控数据
     */
    Map<String, Object> getRealtimeMonitoringData();

    /**
     * 生成交易报表
     *
     * @param reportType 报表类型（daily/weekly/monthly）
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 报表数据
     */
    Map<String, Object> generateReport(String reportType, String startDate, String endDate);

    /**
     * 验证交易完整性
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 验证结果
     */
    Map<String, Object> validateTransactionIntegrity(String startDate, String endDate);

    /**
     * 获取账户交易历史
     *
     * @param accountId 账户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 交易历史
     */
    java.util.List<ConsumeTransactionVO> getAccountTransactionHistory(Long accountId, String startDate, String endDate);

    /**
     * 获取今日交易记录
     *
     * @param deviceId 设备ID（可选）
     * @return 今日交易记录
     */
    List<ConsumeTransactionVO> getTodayTransactions(String deviceId);

    /**
     * 获取交易汇总信息
     *
     * @param date 日期（yyyy-MM-dd格式）
     * @return 汇总信息
     */
    Map<String, Object> getTransactionSummary(String date);

    /**
     * 交易记录对账
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 对账结果
     */
    Map<String, Object> reconcileTransactions(String startDate, String endDate);

    /**
     * 重新处理交易
     *
     * @param transactionId 交易ID
     * @param reason 处理原因
     * @return 处理结果
     */
    Boolean reprocessTransaction(String transactionId, String reason);
}