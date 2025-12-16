package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 消费交易详情VO
 * <p>
 * 用于返回消费交易详细信息
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的业务字段
 * - 符合企业级VO设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeTransactionDetailVO {

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 账户ID
     */
    private String accountId;

    /**
     * 区域ID
     */
    private String areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 消费模式
     */
    private String consumeMode;

    /**
     * 消费类型
     */
    private String consumeType;

    /**
     * 交易状态
     */
    private Integer transactionStatus;
}



