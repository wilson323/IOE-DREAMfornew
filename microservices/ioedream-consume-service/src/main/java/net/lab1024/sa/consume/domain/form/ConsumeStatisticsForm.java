package net.lab1024.sa.consume.domain.form;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

/**
 * 消费统计查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeStatisticsForm {

    /**
     * 开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 开始时间（LocalDateTime）
     */
    private LocalDateTime startTime;

    /**
     * 结束时间（LocalDateTime）
     */
    private LocalDateTime endTime;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 消费模式
     */
    private String consumeMode;

    /**
     * 统计维度
     * <p>
     * DAY-按天
     * WEEK-按周
     * MONTH-按月
     * </p>
     */
    private String dimension;

    /**
     * 统计类型
     */
    private String statisticsType;

    /**
     * 时间范围类型
     * <p>
     * TODAY-今天
     * WEEK-本周
     * MONTH-本月
     * YEAR-本年
     * </p>
     */
    private String timeRangeType;

    /**
     * 排名类型
     * <p>
     * USER-用户排名
     * AREA-区域排名
     * DEVICE-设备排名
     * </p>
     */
    private String rankingType;

    /**
     * 排名限制（返回前N名）
     */
    private Integer rankingLimit;

    /**
     * 数据粒度
     * <p>
     * HOUR-按小时
     * DAY-按天
     * WEEK-按周
     * MONTH-按月
     * </p>
     */
    private String dataGranularity;

    /**
     * 用户ID列表（批量查询）
     */
    private List<Long> userIds;

    /**
     * 区域ID列表（批量查询）
     */
    private List<Long> regionIds;

    /**
     * 分组维度
     * <p>
     * TIME-按时间
     * REGION-按区域
     * USER-按用户
     * CATEGORY-按类别
     * </p>
     */
    private String groupByDimension;
}



