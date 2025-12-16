package net.lab1024.sa.consume.domain.vo;

import lombok.Data;

/**
 * 消费设备统计VO
 * <p>
 * 用于返回设备统计信息
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
public class ConsumeDeviceStatisticsVO {

    /**
     * 总设备数
     */
    private Integer totalDevices;

    /**
     * 在线设备数
     */
    private Integer onlineDevices;

    /**
     * 离线设备数
     */
    private Integer offlineDevices;

    /**
     * 故障设备数
     */
    private Integer faultDevices;

    /**
     * 今日消费笔数
     */
    private Long todayTransactionCount;

    /**
     * 今日消费金额
     */
    private java.math.BigDecimal todayTransactionAmount;
}



