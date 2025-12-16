package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 行为检测管理器
 * <p>
 * 负责异常行为检测，包括徘徊、聚集、跌倒等
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
public class BehaviorDetectionManager {

    private final Map<String, PersonTrack> personTracks = new ConcurrentHashMap<>();

    // 配置参数
    private static final int LOITERING_THRESHOLD_SECONDS = 300; // 徘徊阈值：5分钟
    private static final int GATHERING_THRESHOLD_COUNT = 5; // 聚集阈值：5人

    /**
     * 跌倒检测置信度阈值
     * <p>
     * 预留配置参数，用于未来AI模型集成：
     * - 当集成跌倒检测AI模型时，将使用此阈值判断检测结果
     * - 当前未使用，待AI模型集成后启用
     * </p>
     */
    @SuppressWarnings("unused")
    private static final double FALL_DETECTION_THRESHOLD = 0.8; // 跌倒检测置信度阈值

    /**
     * 徘徊检测
     *
     * @param cameraId 摄像头ID
     * @param personId 人员标识
     * @param x 位置X
     * @param y 位置Y
     * @param timestamp 时间戳
     * @return 是否检测到徘徊
     */
    public LoiteringResult detectLoitering(String cameraId, String personId, int x, int y, LocalDateTime timestamp) {
        String trackKey = cameraId + "_" + personId;
        PersonTrack track = personTracks.computeIfAbsent(trackKey, k -> new PersonTrack(personId, timestamp));

        track.addPosition(x, y, timestamp);

        // 检查是否在同一区域停留超过阈值
        if (track.getDurationSeconds() > LOITERING_THRESHOLD_SECONDS && track.isInSameArea()) {
            log.warn("[行为检测] 检测到徘徊行为，cameraId={}, personId={}, duration={}s",
                    cameraId, personId, track.getDurationSeconds());
            return new LoiteringResult(true, personId, track.getDurationSeconds(), x, y);
        }

        return new LoiteringResult(false, personId, track.getDurationSeconds(), x, y);
    }

    /**
     * 聚集检测
     *
     * @param cameraId 摄像头ID
     * @param personPositions 人员位置列表
     * @return 聚集检测结果
     */
    public GatheringResult detectGathering(String cameraId, List<PersonPosition> personPositions) {
        if (personPositions.size() < GATHERING_THRESHOLD_COUNT) {
            return new GatheringResult(false, 0, 0, 0, personPositions.size());
        }

        // 简化实现：检查是否有多人在同一区域
        // TODO: 实现基于密度的聚类算法（如DBSCAN）
        // 说明：当前使用简单的平均值计算，未来将集成DBSCAN等聚类算法
        // 以更准确地识别人员聚集区域和聚集半径
        int avgX = (int) personPositions.stream().mapToInt(PersonPosition::x).average().orElse(0);
        int avgY = (int) personPositions.stream().mapToInt(PersonPosition::y).average().orElse(0);

        log.warn("[行为检测] 检测到聚集行为，cameraId={}, count={}", cameraId, personPositions.size());
        return new GatheringResult(true, avgX, avgY, 100, personPositions.size());
    }

    /**
     * 跌倒检测
     * <p>
     * 当前实现：返回默认结果（未检测到跌倒）
     * 未来扩展：集成跌倒检测AI模型
     * - 使用FALL_DETECTION_THRESHOLD作为置信度阈值
     * - 支持实时视频流分析和批量图片分析
     * - 返回跌倒位置坐标和置信度
     * </p>
     *
     * @param cameraId 摄像头ID
     * @param frameData 视频帧数据
     * @return 跌倒检测结果
     */
    public FallDetectionResult detectFall(String cameraId, byte[] frameData) {
        log.info("[行为检测] 跌倒检测，cameraId={}", cameraId);
        // TODO: 集成跌倒检测AI模型
        // 说明：待AI模型集成后，将使用FALL_DETECTION_THRESHOLD判断检测结果
        // 当前返回默认结果，表示未检测到跌倒
        return new FallDetectionResult(false, 0.0, 0, 0);
    }

    /**
     * 异常行为检测（综合）
     * <p>
     * 当前实现：返回空列表（未检测到异常行为）
     * 未来扩展：集成异常行为检测AI模型
     * - 支持多种异常行为类型检测（打架、奔跑、攀爬等）
     * - 返回异常行为类型、置信度、位置坐标和描述信息
     * - 支持自定义异常行为规则配置
     * </p>
     *
     * @param cameraId 摄像头ID
     * @param frameData 视频帧数据
     * @return 异常行为列表
     */
    public List<AbnormalBehavior> detectAbnormalBehaviors(String cameraId, byte[] frameData) {
        log.info("[行为检测] 综合异常行为检测，cameraId={}", cameraId);
        // TODO: 集成异常行为检测AI模型
        // 说明：待AI模型集成后，将返回检测到的异常行为列表
        // 当前返回空列表，表示未检测到异常行为
        return List.of();
    }

    /**
     * 清理过期轨迹
     */
    public void cleanExpiredTracks(int expireMinutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(expireMinutes);
        personTracks.entrySet().removeIf(entry -> entry.getValue().getLastUpdateTime().isBefore(threshold));
    }

    // 内部类
    private static class PersonTrack {
        /**
         * 人员标识
         * <p>
         * 用于标识被跟踪的人员，在构造函数中初始化
         * 未来扩展：支持人员身份识别和关联，用于生成人员行为分析报告
         * </p>
         */
        private final String personId;

        private final LocalDateTime startTime;
        private LocalDateTime lastUpdateTime;

        /**
         * 最后位置X坐标
         * <p>
         * 在addPosition方法中更新，当前未读取
         * 预留字段，用于未来扩展：
         * - 支持轨迹回放功能
         * - 用于生成人员移动路径可视化
         * </p>
         */
        @SuppressWarnings("unused")
        private int lastX;

        /**
         * 最后位置Y坐标
         * <p>
         * 在addPosition方法中更新，当前未读取
         * 预留字段，用于未来扩展：
         * - 支持轨迹回放功能
         * - 用于生成人员移动路径可视化
         * </p>
         */
        @SuppressWarnings("unused")
        private int lastY;

        private int minX;
        private int maxX;
        private int minY;
        private int maxY;

        PersonTrack(String personId, LocalDateTime startTime) {
            this.personId = personId;
            this.startTime = startTime;
            this.lastUpdateTime = startTime;
            this.minX = this.maxX = 0;
            this.minY = this.maxY = 0;
        }

        void addPosition(int x, int y, LocalDateTime timestamp) {
            this.lastX = x;
            this.lastY = y;
            this.lastUpdateTime = timestamp;
            this.minX = Math.min(minX, x);
            this.maxX = Math.max(maxX, x);
            this.minY = Math.min(minY, y);
            this.maxY = Math.max(maxY, y);
        }

        long getDurationSeconds() {
            return java.time.Duration.between(startTime, lastUpdateTime).getSeconds();
        }

        boolean isInSameArea() {
            // 简化判断：移动范围小于100像素认为在同一区域
            return (maxX - minX) < 100 && (maxY - minY) < 100;
        }

        LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }
    }

    // 记录类型
    public record PersonPosition(String personId, int x, int y) {}
    public record LoiteringResult(boolean detected, String personId, long durationSeconds, int x, int y) {}
    public record GatheringResult(boolean detected, int centerX, int centerY, int radius, int personCount) {}
    public record FallDetectionResult(boolean detected, double confidence, int x, int y) {}
    public record AbnormalBehavior(String type, double confidence, int x, int y, String description) {}
}
