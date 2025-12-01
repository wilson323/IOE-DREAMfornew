package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 轨迹分析结果VO
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
public class TrajectoryAnalysisVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 目标ID
     */
    private String targetId;

    /**
     * 目标类型
     * PERSON - 人员
     * VEHICLE - 车辆
     * OBJECT - 物体
     */
    private String targetType;

    /**
     * 分析开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 分析结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 轨迹点列表
     */
    private List<TrajectoryPoint> trajectoryPoints;

    /**
     * 统计信息
     */
    private TrajectoryStatistics statistics;

    /**
     * 异常事件列表
     */
    private List<AnomalyEvent> anomalyEvents;

    /**
     * 预测轨迹
     */
    private List<TrajectoryPoint> predictedTrajectory;

    /**
     * 轨迹点
     */
    @Data
    public static class TrajectoryPoint {
        /**
         * 点ID
         */
        private String pointId;

        /**
         * 时间戳
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;

        /**
         * X坐标
         */
        private Double x;

        /**
         * Y坐标
         */
        private Double y;

        /**
         * 置信度
         */
        private Double confidence;

        /**
         * 速度 (像素/秒)
         */
        private Double speed;

        /**
         * 方向角度 (度)
         */
        private Double direction;

        /**
         * 区域信息
         */
        private String areaInfo;

        /**
         * 附加属性
         */
        private Map<String, Object> attributes;
    }

    /**
     * 轨迹统计
     */
    @Data
    public static class TrajectoryStatistics {
        /**
         * 总轨迹点数
         */
        private Integer totalPoints;

        /**
         * 总距离 (像素)
         */
        private Double totalDistance;

        /**
         * 平均速度
         */
        private Double averageSpeed;

        /**
         * 最大速度
         */
        private Double maxSpeed;

        /**
         * 最小速度
         */
        private Double minSpeed;

        /**
         * 停留时间 (秒)
         */
        private Long dwellTime;

        /**
         * 活动区域数量
         */
        private Integer areaCount;

        /**
         * 主要活动区域
         */
        private String primaryArea;

        /**
         * 轨迹模式
         * CIRCULAR - 环形
         * LINEAR - 线性
         * RANDOM - 随机
         * STATIONARY - 静止
         */
        private String movementPattern;
    }

    /**
     * 异常事件
     */
    @Data
    public static class AnomalyEvent {
        /**
         * 事件ID
         */
        private String eventId;

        /**
         * 事件类型
         * SPEED_ABNORMAL - 速度异常
         * DIRECTION_CHANGE - 方向突变
         * AREA_VIOLATION - 区域闯入
         * LOITERING - 逗留
         * DISAPPEARANCE - 消失
         * APPEARANCE - 出现
         */
        private String eventType;

        /**
         * 事件时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime eventTime;

        /**
         * 事件位置
         */
        private TrajectoryPoint location;

        /**
         * 事件描述
         */
        private String description;

        /**
         * 严重程度
         * LOW - 低
         * MEDIUM - 中
         * HIGH - 高
         * CRITICAL - 严重
         */
        private String severity;

        /**
         * 置信度
         */
        private Double confidence;

        /**
         * 处理状态
         * PENDING - 待处理
         * PROCESSED - 已处理
         * IGNORED - 已忽略
         */
        private String status;
    }
}