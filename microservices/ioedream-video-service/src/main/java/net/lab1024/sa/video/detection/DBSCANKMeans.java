package net.lab1024.sa.video.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * DBSCAN密度聚类算法实现
 * <p>
 * 用于行为检测中的人员聚集识别
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class DBSCANKMeans {

    /** 最小邻居点数（聚类最小点数） */
    private final int minPts;

    /** 邻域半径（像素距离） */
    private final int epsilon;

    /** 点的访问状态 */
    private enum PointStatus {
        UNVISITED,   // 未访问
        VISITED,     // 已访问
        NOISE        // 噪声点
    }

    /**
     * 构造函数
     *
     * @param minPts 最小邻居点数
     * @param epsilon 邻域半径
     */
    public DBSCANKMeans(int minPts, int epsilon) {
        this.minPts = minPts;
        this.epsilon = epsilon;
    }

    /**
     * 执行DBSCAN聚类
     *
     * @param points 点集
     * @return 聚类结果列表，每个列表表示一个聚类
     */
    public List<List<PersonPoint>> cluster(List<PersonPoint> points) {
        log.info("[DBSCAN] 开始聚类: pointCount={}, minPts={}, epsilon={}",
                points.size(), minPts, epsilon);

        if (points.isEmpty()) {
            return List.of();
        }

        // 1. 初始化所有点的状态为未访问
        Map<PersonPoint, PointStatus> pointStatus = new HashMap<>();
        for (PersonPoint point : points) {
            pointStatus.put(point, PointStatus.UNVISITED);
        }

        // 2. 存储聚类结果
        List<List<PersonPoint>> clusters = new ArrayList<>();
        int clusterId = 0;

        // 3. 遍历所有点
        for (PersonPoint point : points) {
            if (pointStatus.get(point) != PointStatus.UNVISITED) {
                continue;
            }

            // 标记为已访问
            pointStatus.put(point, PointStatus.VISITED);

            // 获取邻域点
            List<PersonPoint> neighbors = getNeighbors(point, points);

            // 如果邻域点数小于minPts，标记为噪声点
            if (neighbors.size() < minPts) {
                pointStatus.put(point, PointStatus.NOISE);
                log.debug("[DBSCAN] 点被标记为噪声: personId=({},{})", point.x(), point.y());
            } else {
                // 创建新聚类
                log.info("[DBSCAN] 发现新聚类 #{}", clusterId);
                List<PersonPoint> cluster = expandCluster(point, neighbors, points, pointStatus, clusterId);
                clusters.add(cluster);
                clusterId++;
            }
        }

        log.info("[DBSCAN] 聚类完成: clusterCount={}, noiseCount={}",
                clusterId,
                pointStatus.values().stream().filter(status -> status == PointStatus.NOISE).count());

        return clusters;
    }

    /**
     * 扩展聚类
     */
    private List<PersonPoint> expandCluster(
            PersonPoint point,
            List<PersonPoint> neighbors,
            List<PersonPoint> allPoints,
            Map<PersonPoint, PointStatus> pointStatus,
            int clusterId) {

        List<PersonPoint> cluster = new ArrayList<>();
        cluster.add(point);

        // 使用索引队列避免递归深度过大
        List<Integer> neighborIndices = new ArrayList<>();
        for (PersonPoint neighbor : neighbors) {
            int idx = allPoints.indexOf(neighbor);
            if (idx >= 0) {
                neighborIndices.add(idx);
            }
        }

        int i = 0;
        while (i < neighborIndices.size()) {
            PersonPoint currentNeighbor = allPoints.get(neighborIndices.get(i));

            if (pointStatus.get(currentNeighbor) == PointStatus.UNVISITED) {
                pointStatus.put(currentNeighbor, PointStatus.VISITED);
                List<PersonPoint> currentNeighbors = getNeighbors(currentNeighbor, allPoints);

                if (currentNeighbors.size() >= minPts) {
                    // 添加新的邻居点到队列
                    for (PersonPoint newNeighbor : currentNeighbors) {
                        int newIdx = allPoints.indexOf(newNeighbor);
                        if (newIdx >= 0 && !neighborIndices.contains(newIdx)) {
                            neighborIndices.add(newIdx);
                        }
                    }
                }
            }

            // 如果点未被分配到任何聚类，添加到当前聚类
            if (!cluster.contains(currentNeighbor)) {
                cluster.add(currentNeighbor);
            }

            i++;
        }

        log.debug("[DBSCAN] 聚类 #{} 扩展完成: pointCount={}", clusterId, cluster.size());
        return cluster;
    }

    /**
     * 获取邻域点
     */
    private List<PersonPoint> getNeighbors(PersonPoint point, List<PersonPoint> allPoints) {
        List<PersonPoint> neighbors = new ArrayList<>();

        for (PersonPoint other : allPoints) {
            if (calculateDistance(point, other) <= epsilon) {
                neighbors.add(other);
            }
        }

        return neighbors;
    }

    /**
     * 计算两点之间的欧氏距离
     */
    private double calculateDistance(PersonPoint p1, PersonPoint p2) {
        int dx = p1.x() - p2.x();
        int dy = p1.y() - p2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 计算聚类的中心点和半径
     */
    public ClusterInfo calculateClusterInfo(List<PersonPoint> cluster) {
        if (cluster.isEmpty()) {
            return new ClusterInfo(0, 0, 0, 0);
        }

        // 计算中心点
        double avgX = cluster.stream().mapToInt(PersonPoint::x).average().orElse(0);
        double avgY = cluster.stream().mapToInt(PersonPoint::y).average().orElse(0);

        // 计算最大半径（距离中心点最远点的距离）
        double maxRadius = 0;
        for (PersonPoint point : cluster) {
            double distance = Math.sqrt(Math.pow(point.x() - avgX, 2) + Math.pow(point.y() - avgY, 2));
            maxRadius = Math.max(maxRadius, distance);
        }

        return new ClusterInfo((int) avgX, (int) avgY, (int) maxRadius, cluster.size());
    }

    /**
     * 聚类信息
     */
    public record ClusterInfo(int centerX, int centerY, int radius, int pointCount) {
    }

    /**
     * 人员点
     */
    public record PersonPoint(String personId, int x, int y) {
    }
}
