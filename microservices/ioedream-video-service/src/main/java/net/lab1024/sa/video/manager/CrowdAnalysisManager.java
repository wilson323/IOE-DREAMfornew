package net.lab1024.sa.video.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.video.sdk.AiSdkProvider;

/**
 * 人群分析管理器
 * <p>
 * 负责人流密度统计、热力图生成、拥挤预警等
 * 严格遵循CLAUDE.md规范：
 * - Manager类通过构造函数注入依赖，保持为纯Java类
 * - 不使用Spring注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
public class CrowdAnalysisManager {

    private final AiSdkProvider aiSdkProvider;
    private final Map<String, DensityHistory> densityHistories = new ConcurrentHashMap<>();
    private final Map<String, TrajectoryHistory> trajectoryHistories = new ConcurrentHashMap<>();

    // 配置参数
    private static final int CROWD_WARNING_THRESHOLD = 50; // 拥挤预警阈值
    private static final int CROWD_ALARM_THRESHOLD = 100; // 拥挤告警阈值
    private static final double DEFAULT_AREA_SQUARE_METERS = 100.0; // 默认区域面积（平方米）

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param aiSdkProvider AI SDK提供者
     */
    public CrowdAnalysisManager(AiSdkProvider aiSdkProvider) {
        this.aiSdkProvider = aiSdkProvider;
    }

    /**
     * 统计人流密度
     * <p>
     * 实现步骤：
     * 1. 调用AI SDK的人群计数模型
     * 2. 计算人流密度（人数/区域面积）
     * 3. 记录历史数据
     * 4. 判断拥挤等级
     * </p>
     *
     * @param cameraId  摄像头ID
     * @param frameData 视频帧数据
     * @return 人流密度结果
     */
    public DensityResult calculateDensity(String cameraId, byte[] frameData) {
        log.info("[人群分析] 计算人流密度，cameraId={}", cameraId);

        int personCount = 0;
        double density = 0.0;

        try {
            // 调用AI SDK的人群计数模型
            if (aiSdkProvider != null && aiSdkProvider.isAvailable()) {
                personCount = aiSdkProvider.countPeople(frameData);
                log.debug("[人群分析] AI模型计数结果，cameraId={}, personCount={}", cameraId, personCount);
            } else {
                log.warn("[人群分析] AI SDK不可用，使用默认值，cameraId={}", cameraId);
                personCount = 0;
            }

            // 计算人流密度（人数/区域面积）
            // 注意：实际应用中应从摄像头配置中获取区域面积
            density = personCount / DEFAULT_AREA_SQUARE_METERS;

            // 记录历史
            DensityHistory history = densityHistories.computeIfAbsent(cameraId, k -> new DensityHistory());
            history.addRecord(personCount, LocalDateTime.now());

        } catch (Exception e) {
            log.error("[人群分析] 计算人流密度失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
            personCount = 0;
            density = 0.0;
        }

        // 判断拥挤等级
        CrowdLevel level = CrowdLevel.NORMAL;
        if (personCount >= CROWD_ALARM_THRESHOLD) {
            level = CrowdLevel.ALARM;
        } else if (personCount >= CROWD_WARNING_THRESHOLD) {
            level = CrowdLevel.WARNING;
        }

        return new DensityResult(cameraId, personCount, density, level, LocalDateTime.now());
    }

    /**
     * 生成热力图数据
     * <p>
     * 实现步骤：
     * 1. 获取历史轨迹数据（最近30分钟）
     * 2. 将轨迹点映射到网格
     * 3. 计算每个网格的密度值
     * 4. 生成热力图数据
     * </p>
     *
     * @param cameraId 摄像头ID
     * @param width    宽度
     * @param height   高度
     * @param gridSize 网格大小
     * @return 热力图数据
     */
    public HeatmapData generateHeatmap(String cameraId, int width, int height, int gridSize) {
        log.info("[人群分析] 生成热力图，cameraId={}, size={}x{}, gridSize={}", cameraId, width, height, gridSize);

        int cols = width / gridSize;
        int rows = height / gridSize;
        int[][] heatmapGrid = new int[rows][cols];

        try {
            // 获取历史轨迹数据（最近30分钟）
            TrajectoryHistory trajectoryHistory = trajectoryHistories.get(cameraId);
            if (trajectoryHistory != null) {
                Map<String, Integer> trajectoryPoints = trajectoryHistory.getTrajectoryPoints(30);

                // 将轨迹点映射到网格
                for (Map.Entry<String, Integer> entry : trajectoryPoints.entrySet()) {
                    String[] coords = entry.getKey().split(",");
                    if (coords.length == 2) {
                        try {
                            int x = Integer.parseInt(coords[0]);
                            int y = Integer.parseInt(coords[1]);
                            int count = entry.getValue();

                            // 计算网格坐标
                            int gridX = Math.min(x / gridSize, cols - 1);
                            int gridY = Math.min(y / gridSize, rows - 1);

                            // 累加密度值
                            if (gridX >= 0 && gridX < cols && gridY >= 0 && gridY < rows) {
                                heatmapGrid[gridY][gridX] += count;
                            }
                        } catch (NumberFormatException e) {
                            log.warn("[人群分析] 解析轨迹点坐标失败，key={}", entry.getKey());
                        }
                    }
                }
            } else {
                log.debug("[人群分析] 无历史轨迹数据，返回空热力图，cameraId={}", cameraId);
            }

        } catch (Exception e) {
            log.error("[人群分析] 生成热力图失败，cameraId={}, error={}", cameraId, e.getMessage(), e);
        }

        return new HeatmapData(cameraId, heatmapGrid, gridSize, LocalDateTime.now());
    }

    /**
     * 添加轨迹点（由视频分析服务调用）
     *
     * @param cameraId 摄像头ID
     * @param x        X坐标
     * @param y        Y坐标
     */
    public void addTrajectoryPoint(String cameraId, int x, int y) {
        TrajectoryHistory history = trajectoryHistories.computeIfAbsent(cameraId, k -> new TrajectoryHistory());
        history.addPoint(x, y, LocalDateTime.now());
    }

    /**
     * 拥挤预警检查
     *
     * @param cameraId 摄像头ID
     * @return 预警结果
     */
    public CrowdWarning checkCrowdWarning(String cameraId) {
        DensityHistory history = densityHistories.get(cameraId);
        if (history == null) {
            return new CrowdWarning(false, CrowdLevel.NORMAL, 0, "无数据");
        }

        int avgCount = history.getAverageCount(5); // 最近5分钟平均
        CrowdLevel level = CrowdLevel.NORMAL;
        String message = "正常";

        if (avgCount >= CROWD_ALARM_THRESHOLD) {
            level = CrowdLevel.ALARM;
            message = String.format("人流密度过高，当前%d人，请立即疏散", avgCount);
            log.error("[人群分析] 拥挤告警，cameraId={}, count={}", cameraId, avgCount);
        } else if (avgCount >= CROWD_WARNING_THRESHOLD) {
            level = CrowdLevel.WARNING;
            message = String.format("人流密度较高，当前%d人，请注意", avgCount);
            log.warn("[人群分析] 拥挤预警，cameraId={}, count={}", cameraId, avgCount);
        }

        return new CrowdWarning(level != CrowdLevel.NORMAL, level, avgCount, message);
    }

    /**
     * 获取人流趋势
     *
     * @param cameraId 摄像头ID
     * @param minutes  时间范围（分钟）
     * @return 趋势数据
     */
    public Map<LocalDateTime, Integer> getFlowTrend(String cameraId, int minutes) {
        DensityHistory history = densityHistories.get(cameraId);
        if (history == null) {
            return new HashMap<>();
        }
        return history.getRecords(minutes);
    }

    // 内部类：密度历史记录
    private static class DensityHistory {
        private final Map<LocalDateTime, Integer> records = new HashMap<>();

        void addRecord(int count, LocalDateTime time) {
            records.put(time, count);
            // 清理超过1小时的数据
            LocalDateTime threshold = LocalDateTime.now().minusHours(1);
            records.entrySet().removeIf(e -> e.getKey().isBefore(threshold));
        }

        int getAverageCount(int minutes) {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
            return (int) records.entrySet().stream()
                    .filter(e -> e.getKey().isAfter(threshold))
                    .mapToInt(Map.Entry::getValue)
                    .average()
                    .orElse(0);
        }

        Map<LocalDateTime, Integer> getRecords(int minutes) {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
            Map<LocalDateTime, Integer> result = new HashMap<>();
            records.entrySet().stream()
                    .filter(e -> e.getKey().isAfter(threshold))
                    .forEach(e -> result.put(e.getKey(), e.getValue()));
            return result;
        }
    }

    // 内部类：轨迹历史记录
    private static class TrajectoryHistory {
        private final Map<String, TrajectoryPoint> trajectoryPoints = new HashMap<>();

        void addPoint(int x, int y, LocalDateTime time) {
            String key = x + "," + y;
            TrajectoryPoint point = trajectoryPoints.computeIfAbsent(key, k -> new TrajectoryPoint(x, y));
            point.incrementCount();
            point.setLastUpdateTime(time);

            // 清理超过30分钟的数据
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
            trajectoryPoints.entrySet().removeIf(e -> e.getValue().getLastUpdateTime().isBefore(threshold));
        }

        Map<String, Integer> getTrajectoryPoints(int minutes) {
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
            Map<String, Integer> result = new HashMap<>();
            trajectoryPoints.entrySet().stream()
                    .filter(e -> e.getValue().getLastUpdateTime().isAfter(threshold))
                    .forEach(e -> result.put(e.getKey(), e.getValue().getCount()));
            return result;
        }
    }

    // 轨迹点内部类
    private static class TrajectoryPoint {
        private final int x;
        private final int y;
        private int count = 0;
        private LocalDateTime lastUpdateTime;

        TrajectoryPoint(int x, int y) {
            this.x = x;
            this.y = y;
            this.lastUpdateTime = LocalDateTime.now();
        }

        int getX() {
            return x;
        }

        int getY() {
            return y;
        }

        void incrementCount() {
            this.count++;
        }

        int getCount() {
            return count;
        }

        LocalDateTime getLastUpdateTime() {
            return lastUpdateTime;
        }

        void setLastUpdateTime(LocalDateTime lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }
    }

    // 记录类型
    public enum CrowdLevel {
        NORMAL, WARNING, ALARM
    }

    public record DensityResult(String cameraId, int personCount, double density, CrowdLevel level,
            LocalDateTime timestamp) {
    }

    public record HeatmapData(String cameraId, int[][] grid, int gridSize, LocalDateTime timestamp) {
    }

    public record CrowdWarning(boolean warning, CrowdLevel level, int personCount, String message) {
    }
}
