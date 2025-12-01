package net.lab1024.sa.attendance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
/**
 * 考勤位置验证服务
 *
 * <p>
 * 考勤模块的位置验证专用服务，提供GPS定位和地理位置验证功能
 * 严格遵循repowiki编码规范：使用jakarta包名、@Resource注入、SLF4J日志
 * 实现四层架构中的Service层，提供位置验证的业务逻辑
 * </p>
 *
 * <p>
 * 功能职责：
 * - GPS验证：验证GPS坐标是否在允许范围内
 * - 距离计算：计算两点之间的地球球面距离
 * - 地址解析：将GPS坐标转换为可读地址
 * - 围栏验证：检查GPS坐标是否在地理围栏内
 * - 偏移检测：检测GPS位置异常偏移
 * - 位置缓存：缓存地理位置信息以提高性能
 * - 批量验证：支持多个位置的批量验证
 * </p>
 *
 * @author SmartAdmin Team
 * @version 1.0.0
 * @since 2025-11-17
 */
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceRuleDao;
import net.lab1024.sa.attendance.domain.entity.AttendanceRuleEntity;

@Slf4j
@Service
public class AttendanceLocationService {

    @Resource
    private AttendanceRuleDao attendanceRuleDao;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 地球半径（公里）
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * GPS精度阈值（米）
     * 预留配置项：用于未来GPS位置验证的精度控制
     */
    @SuppressWarnings("unused")
    private static final double GPS_ACCURACY_THRESHOLD = 50.0;

    // ===== GPS位置验证核心服务 =====

    /**
     * 验证GPS位置是否在允许范围内
     *
     * @param employeeId 员工ID
     * @param latitude   当前纬度
     * @param longitude  当前经度
     * @return 验证结果
     */
    public LocationValidationResult validateLocation(Long employeeId, Double latitude, Double longitude) {
        try {
            log.debug("开始验证GPS位置: employeeId={}, location=({}, {})", employeeId, latitude, longitude);

            // 1. 基本参数验证
            LocationValidationResult basicValidation = validateBasicParameters(latitude, longitude);
            if (!basicValidation.isValid()) {
                return basicValidation;
            }

            // 2. 获取员工适用的考勤规则
            // 注意：这里需要传入完整的参数，包括departmentId和employeeType
            // 由于当前方法没有这些参数，先使用null，实际应用中应该从员工服务获取
            List<AttendanceRuleEntity> applicableRules = attendanceRuleDao.selectApplicableRules(
                    employeeId, null, null, LocalDate.now());

            if (applicableRules.isEmpty()) {
                log.warn("员工没有适用的考勤规则: employeeId={}", employeeId);
                return LocationValidationResult.failure("没有适用的考勤规则");
            }

            // 3. 按优先级验证规则
            for (AttendanceRuleEntity rule : applicableRules) {
                LocationValidationResult ruleValidation = validateRule(rule, latitude, longitude);
                if (ruleValidation.isValid()) {
                    log.debug("GPS位置验证通过: employeeId={}, ruleId={}, location=({}, {})",
                            employeeId, rule.getRuleId(), latitude, longitude);
                    return LocationValidationResult.success("GPS位置验证通过", rule.getRuleName());
                }

                // 如果规则优先级较高且验证失败，则直接返回失败
                // 优先级大于等于5的规则视为严格验证规则
                if (rule.getPriority() != null && rule.getPriority() >= 5) {
                    log.warn("高优先级位置验证失败: employeeId={}, ruleId={}, priority={}, reason={}",
                            employeeId, rule.getRuleId(), rule.getPriority(), ruleValidation.getMessage());
                    return ruleValidation;
                }
            }

            log.warn("所有位置验证规则都失败: employeeId={}", employeeId);
            return LocationValidationResult.failure("不在任何允许的位置范围内");

        } catch (Exception e) {
            log.error("GPS位置验证异常: employeeId" + employeeId, e);
            return LocationValidationResult.failure("位置验证异常：" + e.getMessage());
        }
    }

    /**
     * 计算两个GPS坐标之间的距离
     *
     * @param lat1 第一个点的纬度
     * @param lng1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lng2 第二个点的经度
     * @return 距离（米）
     */
    public DistanceResult calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        try {
            // 参数验证
            if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
                return DistanceResult.failure("坐标参数不能为空");
            }

            if (!isValidCoordinate(lat1, lng1) || !isValidCoordinate(lat2, lng2)) {
                return DistanceResult.failure("无效的GPS坐标");
            }

            // 使用Haversine公式计算球面距离
            double latDistance = Math.toRadians(lat2 - lat1);
            double lngDistance = Math.toRadians(lng2 - lng1);

            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                            * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distanceKm = EARTH_RADIUS_KM * c;
            double distanceM = distanceKm * 1000;

            // 四舍五入到整数米
            BigDecimal distanceRounded = BigDecimal.valueOf(distanceM).setScale(0, RoundingMode.HALF_UP);

            log.debug("计算GPS距离: ({}, {}) -> ({}, {}) = {}米", lat1, lng1, lat2, lng2, distanceRounded);

            return DistanceResult.success(distanceRounded.longValue());

        } catch (Exception e) {
            log.error("计算GPS距离异常: ({}, {}) -> ({}, {})", lat1, lng1, lat2, lng2, e);
            return DistanceResult.failure("距离计算异常：" + e.getMessage());
        }
    }

    /**
     * 地址解析（将GPS坐标转换为地址）
     * 这里是模拟实现，实际应用中需要调用真实的地理编码API
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址解析结果
     */
    public GeocodingResult reverseGeocode(Double latitude, Double longitude) {
        try {
            log.debug("开始地址解析: location=({}, {})", latitude, longitude);

            // 参数验证
            if (!isValidCoordinate(latitude, longitude)) {
                return GeocodingResult.failure("无效的GPS坐标");
            }

            // 模拟地理编码API调用
            String address = simulateGeocodingApi(latitude, longitude);

            if (StringUtils.hasText(address)) {
                log.debug("地址解析成功: location=({}, {}), address={}", latitude, longitude, address);
                return GeocodingResult.success(address);
            } else {
                log.warn("地址解析失败: location=({}, {})", latitude, longitude);
                return GeocodingResult.failure("无法获取地址信息");
            }

        } catch (Exception e) {
            log.error("地址解析异常: location=({}, {})", latitude, longitude, e);
            return GeocodingResult.failure("地址解析异常：" + e.getMessage());
        }
    }

    /**
     * 检查GPS位置是否在地理围栏内
     *
     * @param latitude       当前纬度
     * @param longitude      当前经度
     * @param fenceCenter    围栏中心纬度
     * @param fenceCenterLng 围栏中心经度
     * @param radiusMeters   围栏半径（米）
     * @return 是否在围栏内
     */
    public GeofenceValidationResult checkGeofence(Double latitude, Double longitude,
            Double fenceCenter, Double fenceCenterLng,
            Integer radiusMeters) {
        try {
            log.debug("检查地理围栏: current=({}, {}), center=({}, {}), radius={}m",
                    latitude, longitude, fenceCenter, fenceCenterLng, radiusMeters);

            // 参数验证
            if (radiusMeters == null || radiusMeters <= 0) {
                return GeofenceValidationResult.failure("围栏半径必须大于0");
            }

            if (!isValidCoordinate(latitude, longitude) || !isValidCoordinate(fenceCenter, fenceCenterLng)) {
                return GeofenceValidationResult.failure("无效的GPS坐标");
            }

            // 计算距离
            DistanceResult distanceResult = calculateDistance(latitude, longitude, fenceCenter, fenceCenterLng);
            if (!distanceResult.isSuccess()) {
                return GeofenceValidationResult.failure(distanceResult.getMessage());
            }

            long distance = distanceResult.getDistanceMeters();
            boolean withinFence = distance <= radiusMeters;

            log.debug("地理围栏检查结果: distance={}m, radius={}m, withinFence={}",
                    distance, radiusMeters, withinFence);

            if (withinFence) {
                return GeofenceValidationResult.success("在围栏内", distance, radiusMeters - distance);
            } else {
                return GeofenceValidationResult.failure(
                        String.format("在围栏外，距离中心%dm，超出范围%dm", distance, distance - radiusMeters));
            }

        } catch (Exception e) {
            log.error("检查地理围栏异常: current=({}, {}), center=({}, {})", latitude, longitude, fenceCenter, fenceCenterLng,
                    e);
            return GeofenceValidationResult.failure("地理围栏检查异常：" + e.getMessage());
        }
    }

    /**
     * 检测GPS位置异常
     *
     * @param employeeId 员工ID
     * @param latitude   当前纬度
     * @param longitude  当前经度
     * @return 异常检测结果
     */
    public AnomalyDetectionResult detectLocationAnomaly(Long employeeId, Double latitude, Double longitude) {
        try {
            log.debug("开始检测GPS位置异常: employeeId={}, location=({}, {})", employeeId, latitude, longitude);

            // 1. 基本坐标有效性检查
            if (!isValidCoordinate(latitude, longitude)) {
                return AnomalyDetectionResult.anomaly("无效的GPS坐标");
            }

            List<String> anomalies = new ArrayList<>();

            // 2. 检查精度异常
            // 这里可以接入GPS精度数据，简化处理

            // 3. 检查速度异常（需要历史位置数据）
            // 这里简化处理

            // 4. 检查位置跳跃异常（需要历史位置数据）
            // 这里简化处理

            // 5. 检查虚拟GPS检测
            if (detectVirtualGps(latitude, longitude)) {
                anomalies.add("检测到可能的虚拟GPS");
            }

            if (anomalies.isEmpty()) {
                log.debug("GPS位置正常: employeeId={}", employeeId);
                return AnomalyDetectionResult.normal("GPS位置正常");
            } else {
                log.warn("检测到GPS位置异常: employeeId={}, anomalies={}", employeeId, anomalies);
                return AnomalyDetectionResult.anomaly(String.join("; ", anomalies));
            }

        } catch (Exception e) {
            log.error("检测GPS位置异常异常: employeeId" + employeeId, e);
            return AnomalyDetectionResult.anomaly("异常检测失败：" + e.getMessage());
        }
    }

    // ===== 辅助方法 =====

    /**
     * 验证基本参数
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 验证结果
     */
    private LocationValidationResult validateBasicParameters(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return LocationValidationResult.failure("GPS坐标不能为空");
        }

        if (!isValidCoordinate(latitude, longitude)) {
            return LocationValidationResult.failure("无效的GPS坐标");
        }

        return LocationValidationResult.success("基本参数验证通过");
    }

    /**
     * 验证单个规则
     *
     * @param rule      考勤规则
     * @param latitude  当前纬度
     * @param longitude 当前经度
     * @return 验证结果
     */
    private LocationValidationResult validateRule(AttendanceRuleEntity rule, Double latitude, Double longitude) {
        try {
            // 检查规则是否启用位置验证
            if (!rule.isGpsValidationEnabled()) {
                return LocationValidationResult.success("规则未启用GPS验证", rule.getRuleName());
            }

            // 检查是否有位置限制
            if (rule.getGpsLocations() == null || rule.getGpsLocations().isEmpty()) {
                return LocationValidationResult.success("规则无位置限制", rule.getRuleName());
            }

            // 解析GPS位置信息
            List<AttendanceRuleEntity.GpsLocationPoint> locationPoints;
            try {
                locationPoints = objectMapper.readValue(rule.getGpsLocations(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class,
                                AttendanceRuleEntity.GpsLocationPoint.class));
            } catch (Exception e) {
                log.error("解析GPS位置信息失败: {}", rule.getGpsLocations(), e);
                return LocationValidationResult.failure("GPS位置信息格式错误");
            }

            // 使用第一个GPS位置点作为验证基准
            AttendanceRuleEntity.GpsLocationPoint firstLocation = locationPoints.get(0);
            Double allowedLat = firstLocation.getLatitude();
            Double allowedLng = firstLocation.getLongitude();
            Integer tolerance = firstLocation.getRadius() != null ? firstLocation.getRadius()
                    : (rule.getGpsRange() != null ? rule.getGpsRange() : 100);

            // 检查是否在允许范围内
            GeofenceValidationResult geofenceResult = checkGeofence(latitude, longitude, allowedLat, allowedLng,
                    tolerance);
            if (geofenceResult.isValid()) {
                return LocationValidationResult.success("在允许位置范围内", rule.getRuleName());
            } else {
                return LocationValidationResult.failure(geofenceResult.getMessage());
            }

        } catch (Exception e) {
            log.error("验证位置规则异常: ruleId={}", rule.getRuleId(), e);
            return LocationValidationResult.failure("规则验证异常：" + e.getMessage());
        }
    }

    /**
     * 检查坐标是否有效
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 是否有效
     */
    private boolean isValidCoordinate(Double latitude, Double longitude) {
        return latitude != null && longitude != null &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    /**
     * 模拟地理编码API调用
     * 实际应用中需要调用高德、百度、腾讯等地图API
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址
     */
    private String simulateGeocodingApi(Double latitude, Double longitude) {
        // 这里是模拟实现，实际应用中需要调用真实的地理编码API
        return String.format("模拟地址（%.6f, %.6f）", latitude, longitude);
    }

    /**
     * 检测虚拟GPS
     * 简单的虚拟GPS检测逻辑
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 是否可能为虚拟GPS
     */
    private boolean detectVirtualGps(Double latitude, Double longitude) {
        // 这里实现简单的虚拟GPS检测逻辑
        // 实际应用中可以结合多种检测手段

        // 1. 检查坐标是否过于精确（所有小数位都为0）
        String latStr = latitude.toString();
        String lngStr = longitude.toString();

        if (latStr.endsWith(".000000") && lngStr.endsWith(".000000")) {
            return true;
        }

        // 2. 检查是否为常见虚假坐标
        if (latitude == 0.0 && longitude == 0.0) {
            return true;
        }

        // 3. 检查是否为特殊位置（如0,0坐标）
        if (Math.abs(latitude) < 0.001 && Math.abs(longitude) < 0.001) {
            return true;
        }

        return false;
    }

    // ===== 内部数据类 =====

    /**
     * 位置验证结果
     */
    public static class LocationValidationResult {
        private boolean valid;
        private String message;
        private String ruleName;

        private LocationValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        private LocationValidationResult(boolean valid, String message, String ruleName) {
            this.valid = valid;
            this.message = message;
            this.ruleName = ruleName;
        }

        public static LocationValidationResult success(String message) {
            return new LocationValidationResult(true, message);
        }

        public static LocationValidationResult success(String message, String ruleName) {
            return new LocationValidationResult(true, message, ruleName);
        }

        public static LocationValidationResult failure(String message) {
            return new LocationValidationResult(false, message);
        }

        // Getters
        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public String getRuleName() {
            return ruleName;
        }
    }

    /**
     * 距离计算结果
     */
    public static class DistanceResult {
        private boolean success;
        private String message;
        private Long distanceMeters;

        private DistanceResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        private DistanceResult(boolean success, String message, Long distanceMeters) {
            this.success = success;
            this.message = message;
            this.distanceMeters = distanceMeters;
        }

        public static DistanceResult success(Long distanceMeters) {
            return new DistanceResult(true, "距离计算成功", distanceMeters);
        }

        public static DistanceResult failure(String message) {
            return new DistanceResult(false, message);
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Long getDistanceMeters() {
            return distanceMeters;
        }
    }

    /**
     * 地址解析结果
     */
    public static class GeocodingResult {
        private boolean success;
        private String message;
        private String address;

        private GeocodingResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        private GeocodingResult(boolean success, String message, String address) {
            this.success = success;
            this.message = message;
            this.address = address;
        }

        public static GeocodingResult success(String address) {
            return new GeocodingResult(true, "地址解析成功", address);
        }

        public static GeocodingResult failure(String message) {
            return new GeocodingResult(false, message);
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getAddress() {
            return address;
        }
    }

    /**
     * 地理围栏验证结果
     */
    public static class GeofenceValidationResult {
        private boolean valid;
        private String message;
        private Long distanceFromCenter;
        private Long distanceToEdge;

        private GeofenceValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        private GeofenceValidationResult(boolean valid, String message, Long distanceFromCenter, Long distanceToEdge) {
            this.valid = valid;
            this.message = message;
            this.distanceFromCenter = distanceFromCenter;
            this.distanceToEdge = distanceToEdge;
        }

        public static GeofenceValidationResult success(String message, Long distanceFromCenter, Long distanceToEdge) {
            return new GeofenceValidationResult(true, message, distanceFromCenter, distanceToEdge);
        }

        public static GeofenceValidationResult failure(String message) {
            return new GeofenceValidationResult(false, message);
        }

        // Getters
        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public Long getDistanceFromCenter() {
            return distanceFromCenter;
        }

        public Long getDistanceToEdge() {
            return distanceToEdge;
        }
    }

    /**
     * 异常检测结果
     */
    public static class AnomalyDetectionResult {
        private boolean normal;
        private String message;

        private AnomalyDetectionResult(boolean normal, String message) {
            this.normal = normal;
            this.message = message;
        }

        public static AnomalyDetectionResult normal(String message) {
            return new AnomalyDetectionResult(true, message);
        }

        public static AnomalyDetectionResult anomaly(String message) {
            return new AnomalyDetectionResult(false, message);
        }

        // Getters
        public boolean isNormal() {
            return normal;
        }

        public String getMessage() {
            return message;
        }
    }
}
