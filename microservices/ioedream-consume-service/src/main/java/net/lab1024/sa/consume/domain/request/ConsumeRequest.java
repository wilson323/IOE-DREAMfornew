package net.lab1024.sa.consume.domain.request;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 消费请求对象
 * <p>
 * 用于接收消费请求参数
 * 严格遵循CLAUDE.md规范：
 * - 使用Request后缀命名
 * - 包含完整的业务字段
 * - 符合企业级Request设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeRequest {

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
     * 消费金额（金额模式时使用）
     */
    private BigDecimal amount;

    /**
     * 消费模式
     * <p>
     * FIXED-定值
     * AMOUNT-金额
     * PRODUCT-商品
     * COUNT-计次
     * </p>
     */
    private String consumeMode;

    /**
     * 商品ID（商品模式时使用）
     */
    private String productId;

    /**
     * 商品数量（商品模式时使用）
     */
    private Integer quantity;
}



