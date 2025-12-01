package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.consume.domain.dto.ConsumeResultDTO;

/**
 * 消费服务接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsumeService {

    /**
     * 处理消费请求
     *
     * @param consumeRequest 消费请求
     * @return 消费结果
     */
    ResponseDTO<ConsumeResultDTO> processConsume(ConsumeRequestDTO consumeRequest);

    /**
     * 查询消费记录
     *
     * @param consumeId 消费记录ID
     * @return 消费记录
     */
    ResponseDTO<Object> getConsumeRecord(Long consumeId);

    /**
     * 查询用户消费记录
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 消费记录列表
     */
    ResponseDTO<Object> getUserConsumeRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取消费统计
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    ResponseDTO<Object> getConsumeStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 退款消费
     *
     * @param consumeId 消费记录ID
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     */
    ResponseDTO<String> refundConsume(Long consumeId, BigDecimal refundAmount, String refundReason);
}