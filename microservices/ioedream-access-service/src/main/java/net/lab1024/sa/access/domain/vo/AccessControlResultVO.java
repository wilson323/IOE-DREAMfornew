package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 门禁控制结果VO
 * <p>
 * 用于返回门禁验证和控制的结果信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class AccessControlResultVO {

    /**
     * 是否允许通过
     */
    private Boolean allowed;

    /**
     * 结果码
     */
    private Integer resultCode;

    /**
     * 结果消息
     */
    private String resultMessage;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 验证方式
     */
    private String verifyMethod;

    /**
     * 验证分数
     */
    private BigDecimal verifyScore;

    /**
     * 验证时间
     */
    private LocalDateTime verifyTime;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 风险分数
     */
    private BigDecimal riskScore;

    /**
     * 处理耗时（毫秒）
     */
    private Long processDuration;

    /**
     * 详细信息（JSON格式）
     */
    private String details;
}
