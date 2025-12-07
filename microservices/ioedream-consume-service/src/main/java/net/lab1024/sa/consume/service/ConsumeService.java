package net.lab1024.sa.consume.service;

import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionDetailVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO;

/**
 * 消费服务接口
 * <p>
 * 提供消费相关的核心业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口在ioedream-consume-service中
 * - 提供统一的业务接口
 * - 支持多种消费模式
 * </p>
 * <p>
 * 业务场景：
 * - 消费交易执行
 * - 设备管理
 * - 实时统计
 * - 消费记录查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeService {

    /**
     * 执行消费交易
     *
     * @param form 消费交易表单
     * @return 交易结果
     */
    ConsumeTransactionResultVO executeTransaction(ConsumeTransactionForm form);

    /**
     * 执行消费请求
     *
     * @param request 消费请求
     * @return 交易详情
     */
    ConsumeTransactionDetailVO executeConsume(ConsumeRequest request);

    /**
     * 获取设备详情
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    ConsumeDeviceVO getDeviceDetail(Long deviceId);

    /**
     * 获取设备状态统计
     *
     * @param areaId 区域ID
     * @return 设备统计信息
     */
    ConsumeDeviceStatisticsVO getDeviceStatistics(String areaId);

    /**
     * 获取实时统计
     *
     * @param areaId 区域ID
     * @return 实时统计数据
     */
    net.lab1024.sa.consume.domain.vo.ConsumeRealtimeStatisticsVO getRealtimeStatistics(String areaId);

    /**
     * 获取交易详情
     *
     * @param transactionNo 交易流水号
     * @return 交易详情
     */
    ConsumeTransactionDetailVO getTransactionDetail(String transactionNo);

    /**
     * 分页查询消费交易记录
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    net.lab1024.sa.common.domain.PageResult<ConsumeTransactionDetailVO> queryTransactions(
            net.lab1024.sa.consume.domain.form.ConsumeTransactionQueryForm queryForm);
}
