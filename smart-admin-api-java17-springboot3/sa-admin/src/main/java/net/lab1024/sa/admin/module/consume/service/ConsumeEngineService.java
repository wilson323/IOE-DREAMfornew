package net.lab1024.sa.admin.module.consume.service;

import net.lab1024.sa.admin.module.consume.domain.request.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.domain.response.ConsumeResult;

import jakarta.validation.Valid;

/**
 * 核心消费引擎服务
 * 负责处理所有消费交易的核心逻辑
 * 确保资金安全和数据一致性
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
public interface ConsumeEngineService {

    /**
     * 核心消费处理方法
     * 这是消费模块的核心方法，负责处理所有的消费交易
     * 确保原子性、一致性、隔离性、持久性
     *
     * @param request 消费请求对象
     * @return 消费结果对象
     */
    ConsumeResult processConsume(@Valid ConsumeRequest request);

    /**
     * 查询消费结果
     * 根据订单号查询消费结果
     *
     * @param orderNo 订单号
     * @return 消费结果对象
     */
    ConsumeResult queryConsumeResult(String orderNo);

    /**
     * 检查消费权限
     * 验证用户是否有权限在指定设备进行消费
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param regionId 区域ID
     * @return 是否有权限
     */
    boolean checkConsumePermission(Long personId, Long deviceId, String regionId);

    /**
     * 验证消费限额
     * 检查用户是否超出消费限额
     *
     * @param personId 人员ID
     * @param amount 消费金额
     * @return 验证结果
     */
    boolean validateConsumeLimit(Long personId, java.math.BigDecimal amount);
}