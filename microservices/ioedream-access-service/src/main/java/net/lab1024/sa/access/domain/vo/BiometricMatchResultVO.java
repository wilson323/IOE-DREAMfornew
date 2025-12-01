package net.lab1024.sa.access.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 生物识别匹配结果VO
 * <p>
 * 用于返回生物识别验证的结果信息
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
public class BiometricMatchResultVO {

    /**
     * 是否匹配成功
     */
    private Boolean matched;

    /**
     * 匹配的用户ID
     */
    private Long userId;

    /**
     * 匹配的用户姓名
     */
    private String userName;

    /**
     * 匹配的用户编号
     */
    private String userCode;

    /**
     * 匹配的模板ID
     */
    private Long templateId;

    /**
     * 匹配分数
     */
    private BigDecimal matchScore;

    /**
     * 匹配阈值
     */
    private BigDecimal matchThreshold;

    /**
     * 生物特征类型
     */
    private String biometricType;

    /**
     * 匹配时间
     */
    private LocalDateTime matchTime;

    /**
     * 匹配耗时（毫秒）
     */
    private Long matchDuration;

    /**
     * 匹配设备ID
     */
    private String deviceId;

    /**
     * 匹配设备名称
     */
    private String deviceName;

    /**
     * 匹配结果详情（JSON格式）
     */
    private String matchDetails;

    /**
     * 匹配状态码
     * 0-成功, 1-失败, 2-超时, 3-质量不足
     */
    private Integer statusCode;

    /**
     * 匹配状态描述
     */
    private String statusMessage;
}
