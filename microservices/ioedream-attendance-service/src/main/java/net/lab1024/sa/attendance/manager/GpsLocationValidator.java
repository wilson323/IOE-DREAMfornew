package net.lab1024.sa.attendance.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * GPS位置验证管理器
 * <p>
 * 验证打卡位置是否在有效考勤区域内
 * 支持多种区域类型：圆形、多边形
 * 支持距离计算和偏差容忍
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class GpsLocationValidator {

    /**
     * 验证打卡位置是否在有效区域内
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param attendanceAreaId 考勤区域ID
     * @return 验证结果
     */
    public ValidationResult validateLocation(Double latitude, Double longitude, Long attendanceAreaId) {
        log.info("[GPS位置验证] 验证打卡位置: lat={}, lng={}, areaId={}", latitude, longitude, attendanceAreaId);

        try {
            // 1. 参数验证
            if (latitude == null || longitude == null) {
                log.warn("[GPS位置验证] GPS坐标为空");
                return ValidationResult.failure("GPS坐标不能为空");
            }

            // 检查坐标有效性
            if (!isValidCoordinate(latitude, longitude)) {
                log.warn("[GPS位置验证] GPS坐标无效: lat={}, lng={}", latitude, longitude);
                return ValidationResult.failure("GPS坐标无效");
            }

            // 2. 获取考勤区域配置
            AttendanceAreaConfig areaConfig = getAttendanceAreaConfig(attendanceAreaId);
            if (areaConfig == null) {
                log.warn("[GPS位置验证] 考勤区域不存在: areaId={}", attendanceAreaId);
                return ValidationResult.failure("考勤区域不存在");
            }

            // 3. 根据区域类型验证
            boolean isInRange;
            double distance = 0.0;

            if ("CIRCLE".equals(areaConfig.getAreaType())) {
                // 圆形区域验证
                CircleArea circleArea = (CircleArea) areaConfig;
                distance = calculateDistance(latitude, longitude,
                        circleArea.getCenterLatitude(), circleArea.getCenterLongitude());
                isInRange = distance <= circleArea.getRadius();

                log.info("[GPS位置验证] 圆形区域验证: distance={}m, radius={}m, inRange={}",
                        distance, circleArea.getRadius(), isInRange);

            } else if ("POLYGON".equals(areaConfig.getAreaType())) {
                // 多边形区域验证
                PolygonArea polygonArea = (PolygonArea) areaConfig;
                isInRange = isPointInPolygon(latitude, longitude, polygonArea.getPoints());

                log.info("[GPS位置验证] 多边形区域验证: inRange={}", isInRange);

            } else {
                log.warn("[GPS位置验证] 不支持的区域类型: {}", areaConfig.getAreaType());
                return ValidationResult.failure("不支持的区域类型");
            }

            // 4. 返回验证结果
            if (isInRange) {
                log.info("[GPS位置验证] 位置验证通过");
                return ValidationResult.success();
            } else {
                log.warn("[GPS位置验证] 位置超出有效范围: distance={}m", distance);
                return ValidationResult.failure("位置超出有效范围，距离" + (int) distance + "米");
            }

        } catch (Exception e) {
            log.error("[GPS位置验证] 验证异常: lat={}, lng={}, error={}", latitude, longitude, e.getMessage(), e);
            return ValidationResult.failure("位置验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证打卡位置（带偏差容忍）
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param attendanceAreaId 考勤区域ID
     * @param toleranceMeters 容忍偏差（米）
     * @return 验证结果
     */
    public ValidationResult validateLocationWithTolerance(Double latitude, Double longitude,
                                                         Long attendanceAreaId, Double toleranceMeters) {
        log.info("[GPS位置验证] 验证打卡位置（带容忍）: lat={}, lng={}, areaId={}, tolerance={}m",
                latitude, longitude, attendanceAreaId, toleranceMeters);

        try {
            // 获取考勤区域配置
            AttendanceAreaConfig areaConfig = getAttendanceAreaConfig(attendanceAreaId);
            if (areaConfig == null || !"CIRCLE".equals(areaConfig.getAreaType())) {
                // 容忍偏差仅支持圆形区域
                return validateLocation(latitude, longitude, attendanceAreaId);
            }

            CircleArea circleArea = (CircleArea) areaConfig;
            double distance = calculateDistance(latitude, longitude,
                    circleArea.getCenterLatitude(), circleArea.getCenterLongitude());

            // 有效半径 = 原始半径 + 容忍偏差
            double effectiveRadius = circleArea.getRadius() + (toleranceMeters != null ? toleranceMeters : 0.0);
            boolean isInRange = distance <= effectiveRadius;

            log.info("[GPS位置验证] 带容忍验证: distance={}m, effectiveRadius={}m, inRange={}",
                    distance, effectiveRadius, isInRange);

            if (isInRange) {
                return ValidationResult.success();
            } else {
                return ValidationResult.failure("位置超出有效范围，距离" + (int) distance + "米（含容忍偏差）");
            }

        } catch (Exception e) {
            log.error("[GPS位置验证] 验证异常: error={}", e.getMessage(), e);
            return ValidationResult.failure("位置验证失败: " + e.getMessage());
        }
    }

    /**
     * 计算两点之间的距离（米）
     * 使用Haversine公式
     *
     * @param lat1 点1纬度
     * @param lon1 点1经度
     * @param lat2 点2纬度
     * @param lon2 点2经度
     * @return 距离（米）
     */
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final double EARTH_RADIUS = 6371000; // 地球半径（米）

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 判断点是否在多边形内
     * 使用射线法（Ray Casting Algorithm）
     *
     * @param latitude 点纬度
     * @param longitude 点经度
     * @param points 多边形顶点列表
     * @return 是否在多边形内
     */
    public boolean isPointInPolygon(Double latitude, Double longitude, List<GpsPoint> points) {
        if (points == null || points.size() < 3) {
            return false;
        }

        int crossings = 0;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            GpsPoint p1 = points.get(i);
            GpsPoint p2 = points.get((i + 1) % n);

            // 检查射线是否与边相交
            if (isRayIntersectEdge(latitude, longitude, p1, p2)) {
                crossings++;
            }
        }

        // 奇数次相交表示在多边形内
        return (crossings % 2) == 1;
    }

    /**
     * 检查射线是否与边相交
     */
    private boolean isRayIntersectEdge(Double lat, Double lng, GpsPoint p1, GpsPoint p2) {
        // 射线向右发射
        if (p1.getLatitude() > p2.getLatitude()) {
            GpsPoint temp = p1;
            p1 = p2;
            p2 = temp;
        }

        // 点在边的下方或上方
        if (lat < p1.getLatitude() || lat > p2.getLatitude()) {
            return false;
        }

        // 边是水平的
        if (p1.getLatitude().equals(p2.getLatitude())) {
            return false;
        }

        // 计算交点的经度
        double xIntersection = (lat - p1.getLatitude()) * (p2.getLongitude() - p1.getLongitude()) /
                (p2.getLatitude() - p1.getLatitude()) + p1.getLongitude();

        // 交点在点右侧
        return lng < xIntersection;
    }

    /**
     * 验证GPS坐标有效性
     */
    private boolean isValidCoordinate(Double latitude, Double longitude) {
        return latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    /**
     * 获取考勤区域配置
     */
    private AttendanceAreaConfig getAttendanceAreaConfig(Long areaId) {
        // TODO: 从数据库获取考勤区域配置
        // 这里返回模拟数据用于演示

        if (areaId == null) {
            return null;
        }

        // 模拟圆形区域（公司总部）
        CircleArea circleArea = new CircleArea();
        circleArea.setAreaId(areaId);
        circleArea.setAreaName("公司总部");
        circleArea.setAreaType("CIRCLE");
        circleArea.setCenterLatitude(39.9042);
        circleArea.setCenterLongitude(116.4074);
        circleArea.setRadius(500.0); // 500米

        return circleArea;
    }

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private boolean valid;
        private String errorMessage;

        public static ValidationResult success() {
            ValidationResult result = new ValidationResult();
            result.valid = true;
            return result;
        }

        public static ValidationResult failure(String errorMessage) {
            ValidationResult result = new ValidationResult();
            result.valid = false;
            result.errorMessage = errorMessage;
            return result;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 考勤区域配置基类
     */
    public static class AttendanceAreaConfig {
        private Long areaId;
        private String areaName;
        private String areaType; // CIRCLE-圆形, POLYGON-多边形

        public Long getAreaId() {
            return areaId;
        }

        public void setAreaId(Long areaId) {
            this.areaId = areaId;
        }

        public String getAreaName() {
            return areaName;
        }

        public void setAreaName(String areaName) {
            this.areaName = areaName;
        }

        public String getAreaType() {
            return areaType;
        }

        public void setAreaType(String areaType) {
            this.areaType = areaType;
        }
    }

    /**
     * 圆形区域
     */
    public static class CircleArea extends AttendanceAreaConfig {
        private Double centerLatitude;
        private Double centerLongitude;
        private Double radius; // 半径（米）

        public Double getCenterLatitude() {
            return centerLatitude;
        }

        public void setCenterLatitude(Double centerLatitude) {
            this.centerLatitude = centerLatitude;
        }

        public Double getCenterLongitude() {
            return centerLongitude;
        }

        public void setCenterLongitude(Double centerLongitude) {
            this.centerLongitude = centerLongitude;
        }

        public Double getRadius() {
            return radius;
        }

        public void setRadius(Double radius) {
            this.radius = radius;
        }
    }

    /**
     * 多边形区域
     */
    public static class PolygonArea extends AttendanceAreaConfig {
        private List<GpsPoint> points;

        public List<GpsPoint> getPoints() {
            return points;
        }

        public void setPoints(List<GpsPoint> points) {
            this.points = points;
        }
    }

    /**
     * GPS坐标点
     */
    public static class GpsPoint {
        private Double latitude;
        private Double longitude;

        public GpsPoint(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }
    }
}
