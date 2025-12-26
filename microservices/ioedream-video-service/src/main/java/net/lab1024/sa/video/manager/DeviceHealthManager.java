package net.lab1024.sa.video.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.DeviceHealthDao;
import net.lab1024.sa.video.entity.DeviceHealthEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备健康管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 负责健康度评分算法和告警判断
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
public class DeviceHealthManager {

    private final DeviceHealthDao deviceHealthDao;

    /**
     * 构造函数注入依赖
     *
     * @param deviceHealthDao 设备健康数据访问对象
     */
    public DeviceHealthManager(DeviceHealthDao deviceHealthDao) {
        this.deviceHealthDao = deviceHealthDao;
    }

    /**
     * 计算设备健康度评分
     * <p>
     * 评分标准（总分100）：
     * - CPU使用率（20分）：0-50%得20分，50-80%得10分，>80%得0分
     * - 内存使用率（20分）：0-70%得20分，70-90%得10分，>90%得0分
     * - 磁盘使用率（15分）：0-80%得15分，80-90%得10分，>90%得0分
     * - 网络延迟（15分）：0-50ms得15分，50-100ms得10分，>100ms得5分，>200ms得0分
     * - 网络丢包率（10分）：0-0.1%得10分，0.1-1%得5分，>1%得0分
     * - 帧率（10分）：>=25fps得10分，15-25fps得5分，<15fps得0分
     * - 运行时间（10分）：>=720h得10分，<720h得5分
     * </p>
     *
     * @param deviceId 设备ID
     * @param deviceCode 设备编号
     * @param deviceName 设备名称
     * @param metrics 健康指标数据
     * @return 健康度评分实体
     */
    public DeviceHealthEntity calculateHealthScore(Long deviceId, String deviceCode, String deviceName,
                                                    Map<String, Object> metrics) {
        log.debug("[设备健康] 开始计算健康度评分: deviceId={}, deviceCode={}", deviceId, deviceCode);

        int score = 0;
        StringBuilder alarmMessage = new StringBuilder();
        int alarmLevel = 0;

        // 1. CPU使用率评分（20分）
        BigDecimal cpuUsage = new BigDecimal(metrics.getOrDefault("cpuUsage", "0").toString());
        int cpuScore = calculateCpuScore(cpuUsage);
        score += cpuScore;
        if (cpuUsage.compareTo(new BigDecimal("80")) > 0) {
            alarmMessage.append("CPU使用率过高(").append(cpuUsage).append("%) ");
            alarmLevel = Math.max(alarmLevel, 2);
        }

        // 2. 内存使用率评分（20分）
        BigDecimal memoryUsage = new BigDecimal(metrics.getOrDefault("memoryUsage", "0").toString());
        int memoryScore = calculateMemoryScore(memoryUsage);
        score += memoryScore;
        if (memoryUsage.compareTo(new BigDecimal("90")) > 0) {
            alarmMessage.append("内存使用率过高(").append(memoryUsage).append("%) ");
            alarmLevel = Math.max(alarmLevel, 2);
        }

        // 3. 磁盘使用率评分（15分）
        BigDecimal diskUsage = new BigDecimal(metrics.getOrDefault("diskUsage", "0").toString());
        int diskScore = calculateDiskScore(diskUsage);
        score += diskScore;
        if (diskUsage.compareTo(new BigDecimal("90")) > 0) {
            alarmMessage.append("磁盘使用率过高(").append(diskUsage).append("%) ");
            alarmLevel = Math.max(alarmLevel, 1);
        }

        // 4. 网络延迟评分（15分）
        Integer networkLatency = Integer.parseInt(metrics.getOrDefault("networkLatency", "0").toString());
        int networkScore = calculateNetworkScore(networkLatency);
        score += networkScore;
        if (networkLatency > 200) {
            alarmMessage.append("网络延迟过高(").append(networkLatency).append("ms) ");
            alarmLevel = Math.max(alarmLevel, 2);
        }

        // 5. 网络丢包率评分（10分）
        BigDecimal packetLoss = new BigDecimal(metrics.getOrDefault("packetLoss", "0").toString());
        int packetScore = calculatePacketLossScore(packetLoss);
        score += packetScore;
        if (packetLoss.compareTo(new BigDecimal("1")) > 0) {
            alarmMessage.append("网络丢包率过高(").append(packetLoss).append("%) ");
            alarmLevel = Math.max(alarmLevel, 3);
        }

        // 6. 帧率评分（10分）
        Integer frameRate = Integer.parseInt(metrics.getOrDefault("frameRate", "0").toString());
        int frameScore = calculateFrameRateScore(frameRate);
        score += frameScore;
        if (frameRate > 0 && frameRate < 15) {
            alarmMessage.append("帧率过低(").append(frameRate).append("fps) ");
            alarmLevel = Math.max(alarmLevel, 2);
        }

        // 7. 运行时间评分（10分）
        Long uptime = Long.parseLong(metrics.getOrDefault("uptime", "0").toString());
        int uptimeScore = calculateUptimeScore(uptime);
        score += uptimeScore;

        // 8. 温度检查
        Integer temperature = Integer.parseInt(metrics.getOrDefault("temperature", "0").toString());
        if (temperature > 70) {
            alarmMessage.append("设备温度过高(").append(temperature).append("℃) ");
            alarmLevel = Math.max(alarmLevel, 3);
        }

        // 判断健康状态
        int healthStatus;
        if (score >= 80) {
            healthStatus = 1; // 健康
        } else if (score >= 60) {
            healthStatus = 2; // 亚健康
        } else {
            healthStatus = 3; // 不健康
        }

        // 如果离线
        if (metrics.containsKey("offline") && Boolean.parseBoolean(metrics.get("offline").toString())) {
            healthStatus = 4; // 离线
            score = 0;
            alarmLevel = 3;
            alarmMessage.insert(0, "设备离线 ");
        }

        // 构建健康记录实体
        DeviceHealthEntity health = DeviceHealthEntity.builder()
                .deviceId(deviceId)
                .deviceCode(deviceCode)
                .deviceName(deviceName)
                .healthScore(score)
                .healthStatus(healthStatus)
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .diskUsage(diskUsage)
                .networkLatency(networkLatency)
                .packetLoss(packetLoss)
                .frameRate(frameRate)
                .bitRate(Integer.parseInt(metrics.getOrDefault("bitRate", "0").toString()))
                .uptime(uptime)
                .temperature(temperature)
                .alarmLevel(alarmLevel)
                .alarmMessage(alarmMessage.length() > 0 ? alarmMessage.toString() : null)
                .checkTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();

        log.debug("[设备健康] 健康度评分完成: deviceId={}, score={}, status={}, alarmLevel={}",
                deviceId, score, healthStatus, alarmLevel);

        return health;
    }

    /**
     * 保存健康检查记录
     *
     * @param health 健康记录实体
     */
    public void saveHealthRecord(DeviceHealthEntity health) {
        deviceHealthDao.insert(health);
        log.debug("[设备健康] 健康记录已保存: healthId={}", health.getHealthId());
    }

    /**
     * 获取设备最新健康记录
     *
     * @param deviceId 设备ID
     * @return 健康记录实体
     */
    public DeviceHealthEntity getLatestHealth(Long deviceId) {
        return deviceHealthDao.selectLatestByDeviceId(deviceId);
    }

    /**
     * 获取设备健康历史
     *
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 健康记录列表
     */
    public List<DeviceHealthEntity> getHealthHistory(Long deviceId, int limit) {
        return deviceHealthDao.selectHealthHistory(deviceId, limit);
    }

    /**
     * 获取告警记录
     *
     * @param alarmLevel 告警级别
     * @return 告警记录列表
     */
    public List<DeviceHealthEntity> getAlarmRecords(int alarmLevel) {
        return deviceHealthDao.selectAlarmRecords(alarmLevel);
    }

    /**
     * 获取健康统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getHealthStatistics() {
        List<DeviceHealthDao.HealthStatusCount> statusCounts =
                deviceHealthDao.selectHealthStatusStatistics();

        Map<String, Object> stats = new HashMap<>();
        long totalCount = 0;
        long healthyCount = 0;
        long subHealthyCount = 0;
        long unhealthyCount = 0;
        long offlineCount = 0;

        for (DeviceHealthDao.HealthStatusCount count : statusCounts) {
            totalCount += count.count;
            switch (count.healthStatus) {
                case 1: healthyCount = count.count; break;
                case 2: subHealthyCount = count.count; break;
                case 3: unhealthyCount = count.count; break;
                case 4: offlineCount = count.count; break;
            }
        }

        stats.put("totalCount", totalCount);
        stats.put("healthyCount", healthyCount);
        stats.put("subHealthyCount", subHealthyCount);
        stats.put("unhealthyCount", unhealthyCount);
        stats.put("offlineCount", offlineCount);
        stats.put("healthyRate", totalCount > 0 ?
                new BigDecimal(healthyCount).divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) :
                BigDecimal.ZERO);

        return stats;
    }

    // ==================== 评分算法 ====================

    private int calculateCpuScore(BigDecimal usage) {
        if (usage.compareTo(new BigDecimal("50")) <= 0) return 20;
        if (usage.compareTo(new BigDecimal("80")) <= 0) return 10;
        return 0;
    }

    private int calculateMemoryScore(BigDecimal usage) {
        if (usage.compareTo(new BigDecimal("70")) <= 0) return 20;
        if (usage.compareTo(new BigDecimal("90")) <= 0) return 10;
        return 0;
    }

    private int calculateDiskScore(BigDecimal usage) {
        if (usage.compareTo(new BigDecimal("80")) <= 0) return 15;
        if (usage.compareTo(new BigDecimal("90")) <= 0) return 10;
        return 0;
    }

    private int calculateNetworkScore(Integer latency) {
        if (latency <= 50) return 15;
        if (latency <= 100) return 10;
        if (latency <= 200) return 5;
        return 0;
    }

    private int calculatePacketLossScore(BigDecimal loss) {
        if (loss.compareTo(new BigDecimal("0.1")) <= 0) return 10;
        if (loss.compareTo(new BigDecimal("1")) <= 0) return 5;
        return 0;
    }

    private int calculateFrameRateScore(Integer frameRate) {
        if (frameRate == 0) return 10; // 无数据时不扣分
        if (frameRate >= 25) return 10;
        if (frameRate >= 15) return 5;
        return 0;
    }

    private int calculateUptimeScore(Long uptime) {
        if (uptime >= 720) return 10; // >= 30天
        return 5;
    }
}
