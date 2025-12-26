package net.lab1024.sa.video.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.video.detection.DBSCANKMeans;

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
    private static final int DBSCAN_MIN_PTS = 3; // DBSCAN最小邻居点数
    private static final int DBSCAN_EPSILON = 150; // DBSCAN邻域半径（像素）

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

    // DBSCAN聚类算法实例
    private final DBSCANKMeans dbscan = new DBSCANKMeans(DBSCAN_MIN_PTS, DBSCAN_EPSILON);

    /**
     * 徘徊检测
     *
     * @param cameraId  摄像头ID
     * @param personId  人员标识
     * @param x         位置X
     * @param y         位置Y
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
     * <p>
     * 使用DBSCAN密度聚类算法检测人员聚集
     * - 自动发现聚集区域
     * - 计算聚集中心点和半径
     * - 支持多区域同时检测
     * </p>
     *
     * @param cameraId        摄像头ID
     * @param personPositions 人员位置列表
     * @return 聚集检测结果（返回最大的聚集区域）
     */
    public GatheringResult detectGathering(String cameraId, List<PersonPosition> personPositions) {
        if (personPositions.size() < GATHERING_THRESHOLD_COUNT) {
            log.debug("[行为检测] 人员数量不足，跳过聚集检测: count={}", personPositions.size());
            return new GatheringResult(false, 0, 0, 0, personPositions.size());
        }

        try {
            // 转换为PersonPoint列表
            List<DBSCANKMeans.PersonPoint> points = personPositions.stream()
                    .map(pos -> new DBSCANKMeans.PersonPoint(pos.personId(), pos.x(), pos.y()))
                    .collect(Collectors.toList());

            // 执行DBSCAN聚类
            List<List<DBSCANKMeans.PersonPoint>> clusters = dbscan.cluster(points);

            if (clusters.isEmpty()) {
                log.debug("[行为检测] 未检测到聚集: cameraId={}", cameraId);
                return new GatheringResult(false, 0, 0, 0, personPositions.size());
            }

            // 找到最大的聚类
            List<DBSCANKMeans.PersonPoint> largestCluster = clusters.stream()
                    .max((c1, c2) -> Integer.compare(c1.size(), c2.size()))
                    .orElse(null);

            if (largestCluster == null || largestCluster.size() < GATHERING_THRESHOLD_COUNT) {
                log.debug("[行为检测] 最大聚类人数不足: maxSize={}", largestCluster != null ? largestCluster.size() : 0);
                return new GatheringResult(false, 0, 0, 0, personPositions.size());
            }

            // 计算聚类信息
            DBSCANKMeans.ClusterInfo clusterInfo = dbscan.calculateClusterInfo(largestCluster);

            log.warn("[行为检测] 检测到聚集行为: cameraId={}, clusterCount={}, maxClusterSize={}, center=({},{})",
                    cameraId, clusters.size(), largestCluster.size(),
                    clusterInfo.centerX(), clusterInfo.centerY());

            return new GatheringResult(
                    true,
                    clusterInfo.centerX(),
                    clusterInfo.centerY(),
                    clusterInfo.radius(),
                    clusterInfo.pointCount()
            );

        } catch (Exception e) {
            log.error("[行为检测] 聚集检测失败: cameraId={}, error={}", cameraId, e.getMessage(), e);
            return new GatheringResult(false, 0, 0, 0, personPositions.size());
        }
    }

    // ============================================================
    // ⚠️ 架构违规修复（2025-01-30）
    // ============================================================
    // 以下方法已被删除，违反边缘计算架构原则：
    // - detectFall(String cameraId, byte[] frameData)
    // - detectAbnormalBehaviors(String cameraId, byte[] frameData)
    //
    // 正确架构：
    // 设备端完成AI分析，服务器通过 DeviceAIEventReceiver 接收结构化事件。
   //
    // 参考文档：
    // - openspec/changes/refactor-video-edge-ai-architecture/proposal.md
    // - CLAUDE.md (Mode 5: 边缘AI计算)
    // ============================================================


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
    public record PersonPosition(String personId, int x, int y) {
    }

    public record LoiteringResult(boolean detected, String personId, long durationSeconds, int x, int y) {
    }

    public record GatheringResult(boolean detected, int centerX, int centerY, int radius, int personCount) {
    }

    // ============================================================
    // ⚠️ 以下record已被删除（2025-01-30）
    // ============================================================
    // - FallDetectionResult: 用于服务器端AI分析，已违反架构
    // - AbnormalBehavior: 用于服务器端AI分析，已违反架构
    //
    // 替代方案：
    // 使用 DeviceAIEvent 实体类接收设备上报的结构化事件
    // ============================================================
}
