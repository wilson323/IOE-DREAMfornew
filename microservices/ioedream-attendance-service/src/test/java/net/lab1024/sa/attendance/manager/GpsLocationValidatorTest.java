package net.lab1024.sa.attendance.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GPS位置验证功能测试
 * <p>
 * 注意：这是单元测试，不需要Spring Boot上下文
 * GpsLocationValidator是独立的工具类，直接实例化测试即可
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 * @updated 2025-12-25 移除@SpringBootTest注解，改为纯单元测试
 */
class GpsLocationValidatorTest {

    private GpsLocationValidator validator;

    private Long testAreaId = 1L;

    @BeforeEach
    void setUp() {
        validator = new GpsLocationValidator();
    }

    @Test
    void testValidateLocationInRange() {
        System.out.println("[GPS验证测试] ========== 测试1：验证位置在有效范围内 ==========");

        // 北京天安门坐标
        Double latitude = 39.9042;
        Double longitude = 116.4074;

        // 模拟在中心点，应该通过验证
        GpsLocationValidator.ValidationResult result = validator.validateLocation(latitude, longitude, testAreaId);

        assertTrue(result.isValid(), "位置应该在有效范围内");
        System.out.println("[GPS验证测试] 位置验证通过 ✓");

        System.out.println("[GPS验证测试] ========== 测试1通过 ==========\\n");
    }

    @Test
    void testCalculateDistance() {
        System.out.println("[GPS验证测试] ========== 测试2：计算两点距离 ==========");

        // 天安门
        Double lat1 = 39.9042;
        Double lon1 = 116.4074;

        // 故宫（距离约1.5公里）
        Double lat2 = 39.9163;
        Double lon2 = 116.3971;

        double distance = validator.calculateDistance(lat1, lon1, lat2, lon2);

        assertTrue(distance > 1000 && distance < 2000, "距离应在1-2公里之间");
        System.out.println("[GPS验证测试] 天安门到故宫距离: " + (int) distance + "米 ✓");

        System.out.println("[GPS验证测试] ========== 测试2通过 ==========\\n");
    }

    @Test
    void testValidateLocationOutOfRange() {
        System.out.println("[GPS验证测试] ========== 测试3：验证位置超出有效范围 ==========");

        // 北京天安门
        Double latitude = 39.9042;
        Double longitude = 116.4074;

        // 模拟距离中心点10公里的位置（超出500米半径）
        Double farLatitude = 39.9042 + 0.09; // 约10公里
        Double farLongitude = 116.4074;

        GpsLocationValidator.ValidationResult result = validator.validateLocation(farLatitude, farLongitude, testAreaId);

        assertFalse(result.isValid(), "位置应超出有效范围");
        assertNotNull(result.getErrorMessage(), "应有错误信息");
        System.out.println("[GPS验证测试] 位置超出范围: " + result.getErrorMessage() + " ✓");

        System.out.println("[GPS验证测试] ========== 测试3通过 ==========\\n");
    }

    @Test
    void testValidateLocationWithTolerance() {
        System.out.println("[GPS验证测试] ========== 测试4：带容忍偏差的位置验证 ==========");

        Double centerLatitude = 39.9042;
        Double centerLongitude = 116.4074;

        // 距离中心点590米（超出500米半径，但在100米容忍范围内）
        Double nearLatitude = 39.9042 + 0.0053;
        Double nearLongitude = 116.4074;

        // 不带容忍：应该失败
        GpsLocationValidator.ValidationResult result1 = validator.validateLocation(nearLatitude, nearLongitude, testAreaId);
        assertFalse(result1.isValid(), "不带容忍应该验证失败");

        // 带容忍：应该通过
        GpsLocationValidator.ValidationResult result2 = validator.validateLocationWithTolerance(
                nearLatitude, nearLongitude, testAreaId, 100.0);
        assertTrue(result2.isValid(), "带100米容忍应该验证通过");

        System.out.println("[GPS验证测试] 容忍偏差验证通过 ✓");

        System.out.println("[GPS验证测试] ========== 测试4通过 ==========\\n");
    }

    @Test
    void testIsPointInPolygon() {
        System.out.println("[GPS验证测试] ========== 测试5：多边形区域验证 ==========");

        // 定义一个简单的正方形区域
        List<GpsLocationValidator.GpsPoint> polygon = Arrays.asList(
                new GpsLocationValidator.GpsPoint(39.9000, 116.4000), // 左下
                new GpsLocationValidator.GpsPoint(39.9100, 116.4000), // 左上
                new GpsLocationValidator.GpsPoint(39.9100, 116.4100), // 右上
                new GpsLocationValidator.GpsPoint(39.9000, 116.4100)  // 右下
        );

        // 区域内的点
        boolean inside1 = validator.isPointInPolygon(39.9050, 116.4050, polygon);
        assertTrue(inside1, "点应该在多边形内");
        System.out.println("[GPS验证测试] 区域内的点验证通过 ✓");

        // 区域外的点
        boolean inside2 = validator.isPointInPolygon(39.8950, 116.4050, polygon);
        assertFalse(inside2, "点应该在多边形外");
        System.out.println("[GPS验证测试] 区域外的点验证通过 ✓");

        System.out.println("[GPS验证测试] ========== 测试5通过 ==========\\n");
    }

    @Test
    void testInvalidCoordinate() {
        System.out.println("[GPS验证测试] ========== 测试6：无效坐标验证 ==========");

        // 无效纬度
        GpsLocationValidator.ValidationResult result1 = validator.validateLocation(95.0, 116.4074, testAreaId);
        assertFalse(result1.isValid(), "无效纬度应该验证失败");
        System.out.println("[GPS验证测试] 无效纬度验证通过 ✓");

        // 无效经度
        GpsLocationValidator.ValidationResult result2 = validator.validateLocation(39.9042, 185.0, testAreaId);
        assertFalse(result2.isValid(), "无效经度应该验证失败");
        System.out.println("[GPS验证测试] 无效经度验证通过 ✓");

        // 空坐标
        GpsLocationValidator.ValidationResult result3 = validator.validateLocation(null, null, testAreaId);
        assertFalse(result3.isValid(), "空坐标应该验证失败");
        System.out.println("[GPS验证测试] 空坐标验证通过 ✓");

        System.out.println("[GPS验证测试] ========== 测试6通过 ==========\\n");
    }

    @Test
    void testCalculateDistanceZero() {
        System.out.println("[GPS验证测试] ========== 测试7：相同点距离计算 ==========");

        Double lat = 39.9042;
        Double lon = 116.4074;

        double distance = validator.calculateDistance(lat, lon, lat, lon);

        assertEquals(0.0, distance, 0.001, "相同点距离应为0");
        System.out.println("[GPS验证测试] 相同点距离: 0米 ✓");

        System.out.println("[GPS验证测试] ========== 测试7通过 ==========\\n");
    }
}
