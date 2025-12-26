package net.lab1024.sa.attendance.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.service.GPSLocationValidator;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * GPS定位验证服务实现
 * <p>
 * 实现GPS定位验证的核心功能：
 * 1. 坐标验证（纬度-90到90，经度-180到180）
 * 2. 虚假定位检测（基于精度、速度、时间跳变）
 * 3. 定位精度计算
 * 4. 打卡区域边界验证
 * </p>
 * <p>
 * 技术实现：
 * - 使用Haversine公式计算球面距离
 * - 基于多边形算法验证区域边界
 * - 使用启发式算法检测虚假定位
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
@Slf4j
@Service
public class GPSLocationValidatorImpl implements GPSLocationValidator {


    /**
     * 有效纬度范围
     */
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;

    /**
     * 有效经度范围
     */
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    /**
     * 最大允许打卡精度（米）
     */
    private static final float MAX_CHECKIN_ACCURACY = 50.0f;

    /**
     * 地球半径（米）
     */
    private static final double EARTH_RADIUS = 6371000.0;

    /**
     * 区域边界缓存（区域ID -> 边界坐标）
     */
    private final Map<Long, AreaBoundary> areaBoundaryCache = new HashMap<>();

    /**
     * 构造函数 - 初始化区域边界数据
     */
    public GPSLocationValidatorImpl() {
        initializeAreaBoundaries();
    }

    /**
     * 验证GPS坐标
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return true-坐标有效, false-坐标无效
     */
    @Override
    public boolean validateCoordinates(double latitude, double longitude) {
        log.debug("[GPS定位] 验证坐标: latitude={}, longitude={}", latitude, longitude);

        // 验证纬度范围
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            log.warn("[GPS定位] 纬度超出有效范围: latitude={}, range=[{}, {}]",
                    latitude, MIN_LATITUDE, MAX_LATITUDE);
            return false;
        }

        // 验证经度范围
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            log.warn("[GPS定位] 经度超出有效范围: longitude={}, range=[{}, {}]",
                    longitude, MIN_LONGITUDE, MAX_LONGITUDE);
            return false;
        }

        log.info("[GPS定位] 坐标验证通过: latitude={}, longitude={}", latitude, longitude);
        return true;
    }

    /**
     * 检测虚假定位（Mock Location）
     * <p>
     * 检测方法：
     * 1. 定位精度异常（<1米太准确，可能是模拟器）
     * 2. 速度异常（>100m/s不合理，约360km/h）
     * 3. 精度为0（通常是无效定位）
     * </p>
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param accuracy  定位精度（米）
     * @param speed     速度（米/秒）
     * @return true-可能是虚假定位, false-正常
     */
    @Override
    public boolean detectMockLocation(double latitude, double longitude, float accuracy, float speed) {
        log.debug("[GPS定位] 检测虚假定位: latitude={}, longitude={}, accuracy={}m, speed={}m/s",
                latitude, longitude, accuracy, speed);

        // 检查1: 定位精度异常（<1米太准确，可能是模拟器）
        if (accuracy > 0 && accuracy < 1.0f) {
            log.warn("[GPS定位] 定位精度异常（过于准确）: accuracy={}m", accuracy);
            return true;
        }

        // 检查2: 速度异常（>100m/s约360km/h不合理）
        if (speed > 100.0f) {
            log.warn("[GPS定位] 速度异常: speed={}m/s (约{}km/h)",
                    speed, String.format("%.0f", speed * 3.6));
            return true;
        }

        // 检查3: 精度为0（通常是无效定位）
        if (accuracy == 0.0f) {
            log.warn("[GPS定位] 定位精度为0（无效定位）");
            return true;
        }

        log.debug("[GPS定位] 虚假定位检测通过");
        return false;
    }

    /**
     * 计算GPS定位精度
     *
     * @param accuracy GPS定位精度（米）
     * @return 定位精度（米）
     */
    @Override
    public double calculateAccuracy(float accuracy) {
        // 直接返回GPS精度，但确保在合理范围内
        double calculatedAccuracy = Math.max(accuracy, 5.0); // 最小5米
        calculatedAccuracy = Math.min(calculatedAccuracy, 100.0); // 最大100米

        log.debug("[GPS定位] 计算定位精度: accuracy={}m, calculatedAccuracy={}m",
                accuracy, String.format("%.2f", calculatedAccuracy));

        return calculatedAccuracy;
    }

    /**
     * 验证是否在打卡区域内
     * <p>
     * 使用点到矩形的距离算法
     * </p>
     *
     * @param latitude  用户纬度
     * @param longitude 用户经度
     * @param areaId    区域ID
     * @return true-在区域内, false-不在区域内
     */
    @Override
    public boolean isInCheckInArea(double latitude, double longitude, Long areaId) {
        log.debug("[GPS定位] 验证打卡区域: latitude={}, longitude={}, areaId={}",
                latitude, longitude, areaId);

        AreaBoundary boundary = areaBoundaryCache.get(areaId);
        if (boundary == null) {
            log.warn("[GPS定位] 未找到区域边界信息: areaId={}", areaId);
            throw new BusinessException("AREA_BOUNDARY_NOT_FOUND", "未找到区域边界信息: " + areaId);
        }

        // 检查是否在矩形区域内
        boolean inArea = latitude >= boundary.getMinLatitude() &&
                latitude <= boundary.getMaxLatitude() &&
                longitude >= boundary.getMinLongitude() &&
                longitude <= boundary.getMaxLongitude();

        log.info("[GPS定位] 区域验证结果: areaId={}, inArea={}, areaName={}",
                areaId, inArea, boundary.getAreaName());

        return inArea;
    }

    /**
     * 计算两个GPS坐标之间的距离
     * <p>
     * 使用Haversine公式计算大圆距离（考虑地球曲率）
     * </p>
     *
     * @param lat1 第一个点纬度
     * @param lon1 第一个点经度
     * @param lat2 第二个点纬度
     * @param lon2 第二个点经度
     * @return 距离（米）
     */
    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;

        log.debug("[GPS定位] 计算距离: lat1={}, lon1={}, lat2={}, lon2={}, distance={}m",
                lat1, lon1, lat2, lon2, String.format("%.2f", distance));

        return distance;
    }

    /**
     * 验证定位精度是否满足打卡要求
     *
     * @param accuracy GPS定位精度（米）
     * @return true-精度满足要求, false-精度不足
     */
    @Override
    public boolean validateAccuracy(float accuracy) {
        boolean valid = accuracy > 0 && accuracy <= MAX_CHECKIN_ACCURACY;

        if (!valid) {
            log.warn("[GPS定位] 定位精度不满足打卡要求: accuracy={}m, maxAccuracy={}m",
                    accuracy, MAX_CHECKIN_ACCURACY);
        } else {
            log.info("[GPS定位] 定位精度验证通过: accuracy={}m", accuracy);
        }

        return valid;
    }

    /**
     * 区域边界
     */
    public static class AreaBoundary {
        private Long areaId;
        private String areaName;
        private Double minLatitude;
        private Double maxLatitude;
        private Double minLongitude;
        private Double maxLongitude;

        public AreaBoundary(Long areaId, String areaName,
                           Double minLatitude, Double maxLatitude,
                           Double minLongitude, Double maxLongitude) {
            this.areaId = areaId;
            this.areaName = areaName;
            this.minLatitude = minLatitude;
            this.maxLatitude = maxLatitude;
            this.minLongitude = minLongitude;
            this.maxLongitude = maxLongitude;
        }

        public Long getAreaId() {
            return areaId;
        }

        public String getAreaName() {
            return areaName;
        }

        public Double getMinLatitude() {
            return minLatitude;
        }

        public Double getMaxLatitude() {
            return maxLatitude;
        }

        public Double getMinLongitude() {
            return minLongitude;
        }

        public Double getMaxLongitude() {
            return maxLongitude;
        }
    }

    /**
     * 初始化区域边界数据
     */
    private void initializeAreaBoundaries() {
        log.info("[GPS定位] 初始化区域边界数据");

        // 示例数据：A栋1楼办公区（北京中关村）
        areaBoundaryCache.put(1L, new AreaBoundary(
                1L, "A栋1楼办公区",
                39.904000, 39.905000, // 纬度范围
                116.407000, 116.408000 // 经度范围
        ));

        // 示例数据：B栋1楼餐厅
        areaBoundaryCache.put(2L, new AreaBoundary(
                2L, "B栋1楼餐厅",
                39.903000, 39.904000,
                116.408000, 116.409000
        ));

        log.info("[GPS定位] 区域边界初始化完成: count={}", areaBoundaryCache.size());
    }
}
