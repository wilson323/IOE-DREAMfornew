package net.lab1024.sa.consume.manager;

import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 消费执行管理Manager接口
 * <p>
 * 用于消费执行相关的复杂业务逻辑编排
 * 严格遵循CLAUDE.md规范：
 * - Manager类在microservices-common中不使用Spring注解
 * - Manager类通过构造函数注入依赖
 * - 保持为纯Java类
 * </p>
 * <p>
 * 业务场景：
 * - 消费流程执行
 * - 消费权限验证
 * - 消费金额计算
 * - 消费记录生成
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeExecutionManager {

    /**
     * 执行消费流程
     *
     * @param request 消费请求
     * @return 消费结果
     */
    ResponseDTO<?> executeConsumption(Object request);

    /**
     * 验证消费权限
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param consumeMode 消费模式
     * @return 是否有权限
     */
    boolean validateConsumePermission(Long accountId, String areaId, String consumeMode);

    /**
     * 计算消费金额
     *
     * @param accountId 账户ID
     * @param areaId 区域ID
     * @param consumeMode 消费模式
     * @param consumeAmount 消费金额（如果是金额模式）
     * @param request 消费请求对象（可选，商品模式时需要传递以获取商品信息）
     * @return 实际消费金额
     */
    java.math.BigDecimal calculateConsumeAmount(Long accountId, String areaId, String consumeMode, java.math.BigDecimal consumeAmount, Object request);
}
