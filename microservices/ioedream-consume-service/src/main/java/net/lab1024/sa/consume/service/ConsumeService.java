package net.lab1024.sa.consume.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.CursorPagination;
import net.lab1024.sa.consume.domain.dto.ConsumeQueryDTO;

import java.time.LocalDateTime;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.consume.domain.dto.RechargeRequestDTO;
import net.lab1024.sa.consume.domain.form.ConsumeTransactionForm;
import net.lab1024.sa.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceStatisticsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceVO;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
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

    /**
     * 执行消费（兼容方法）
     *
     * @param request 消费请求DTO
     * @return 消费结果
     */
    ResponseDTO<ConsumeTransactionResultVO> consume(ConsumeRequestDTO request);

    /**
     * 分页查询消费记录（兼容方法，传统分页）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    ResponseDTO<IPage<ConsumeRecordVO>> queryConsumeRecordPage(ConsumeQueryDTO queryDTO);

    /**
     * 游标分页查询消费记录（推荐用于深度分页）
     * <p>
     * 适用于需要查询大量数据的场景，性能优于传统分页
     * </p>
     *
     * @param pageSize 每页大小（默认20，最大100）
     * @param lastTime 上一页最后一条记录的创建时间（首次查询传null）
     * @param userId 用户ID（可选）
     * @param areaId 区域ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param consumeType 消费类型（可选）
     * @param status 状态（可选）
     * @return 游标分页结果
     */
    ResponseDTO<CursorPagination.CursorPageResult<ConsumeRecordVO>> cursorPageConsumeRecords(
            Integer pageSize, LocalDateTime lastTime,
            Long userId, Long areaId, LocalDateTime startTime, LocalDateTime endTime,
            String consumeType, Integer status);

    /**
     * 账户充值（兼容方法）
     *
     * @param request 充值请求DTO
     * @return 充值结果
     */
    ResponseDTO<Void> recharge(RechargeRequestDTO request);
}



