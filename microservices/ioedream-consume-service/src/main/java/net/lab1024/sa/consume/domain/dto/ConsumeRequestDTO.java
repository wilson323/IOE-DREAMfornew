package net.lab1024.sa.consume.domain.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 消费请求DTO
 * <p>
 * 用于消费请求数据传输
 * 严格遵循CLAUDE.md规范：
 * - 使用DTO后缀命名
 * - 包含完整的业务字段
 * - 符合企业级DTO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeRequestDTO {

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
    private Long areaId;

    /**
     * 消费金额（金额模式时使用）
     */
    private BigDecimal amount;

    /**
     * 消费模式
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
