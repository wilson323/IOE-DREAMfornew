package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 消费统计数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeStatisticsVO {

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 生成时间
     */
    private LocalDateTime generateTime;

    /**
     * 总消费金额
     */
    private BigDecimal totalAmount;

    /**
     * 总消费笔数
     */
    private Long totalCount;

    /**
     * 消费人数
     */
    private Integer userCount;

    /**
     * 平均消费金额
     */
    private BigDecimal averageAmount;

    /**
     * 最高消费金额
     */
    private BigDecimal maxAmount;

    /**
     * 最低消费金额
     */
    private BigDecimal minAmount;

    /**
     * 活跃用户数
     */
    private Integer activeUserCount;

    /**
     * 新用户数
     */
    private Integer newUserCount;

    /**
     * 时间序列数据
     */
    private List<Map<String, Object>> timeSeriesData;

    /**
     * 类别数据
     */
    private List<Map<String, Object>> categoryData;

    /**
     * 区域数据
     */
    private List<Map<String, Object>> regionData;
}



