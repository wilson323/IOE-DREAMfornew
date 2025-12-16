package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 消费交易表单对象
 * <p>
 * 用于接收消费交易表单参数
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 包含完整的业务字段
 * - 符合企业级Form设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeTransactionForm {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费模式
     */
    private String consumeMode;

    /**
     * 消费类型
     */
    private String consumeType;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 商品ID（商品模式时使用）
     */
    private String productId;

    /**
     * 商品数量（商品模式时使用）
     */
    private Integer quantity;

    /**
     * 商品ID列表（多商品模式时使用）
     */
    private java.util.List<String> productIds;

    /**
     * 商品数量Map（key=productId, value=quantity）
     */
    private java.util.Map<String, Integer> productQuantities;
}



