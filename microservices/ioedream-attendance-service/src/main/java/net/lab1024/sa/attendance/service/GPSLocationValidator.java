package net.lab1024.sa.attendance.service;

/**
 * GPS定位验证服务
 * <p>
 * 用于验证GPS定位的准确性和合法性，防止虚假定位打卡
 * </p>
 * <p>
 * 核心功能：
 * 1. GPS坐标验证（确保在有效范围内）
 * 2. 防虚假定位检测（检测模拟器、VPN、位置伪造）
 * 3. 定位精度计算（提供精确的位置信息）
 * 4. 区域边界验证（确保在打卡区域内）
 * </p>
 * <p>
 * 性能要求：
 * - 验证速度: <50ms
 * - 定位精度: <10米
 * - 误报率: <5%
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
public interface GPSLocationValidator {

    /**
     * 验证GPS坐标
     * <p>
     * 验证GPS坐标是否在有效范围内（地球上任何位置）
     * 纬度范围: -90到90
     * 经度范围: -180到180
     * </p>
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return true-坐标有效, false-坐标无效
     */
    boolean validateCoordinates(double latitude, double longitude);

    /**
     * 检测虚假定位（Mock Location）
     * <p>
     * 检测是否使用了位置伪造工具（如GPS Spoofing）
     * 检测方法：
     * 1. 检查GPS模拟器状态
     * 2. 检查位置跳变（速度不合理）
     * 3. 检查位置精确度异常（过于完美）
     * </p>
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param accuracy  定位精度（米）
     * @param speed     速度（米/秒，如果可用）
     * @return true-可能是虚假定位, false-正常
     */
    boolean detectMockLocation(double latitude, double longitude, float accuracy, float speed);

    /**
     * 计算GPS定位精度
     * <p>
     * 基于GPS精度参数计算定位误差范围
     * </p>
     *
     * @param accuracy GPS定位精度（米）
     * @return 定位精度（米）
     */
    double calculateAccuracy(float accuracy);

    /**
     * 验证是否在打卡区域内
     * <p>
     * 判断GPS坐标是否在公司指定的打卡区域内
     * 使用点到矩形的距离算法
     * </p>
     *
     * @param latitude  用户纬度
     * @param longitude 用户经度
     * @param areaId    区域ID
     * @return true-在区域内, false-不在区域内
     */
    boolean isInCheckInArea(double latitude, double longitude, Long areaId);

    /**
     * 计算两个GPS坐标之间的距离
     * <p>
     * 使用Haversine公式计算大圆距离
     * </p>
     *
     * @param lat1 第一个点纬度
     * @param lon1 第一个点经度
     * @param lat2 第二个点纬度
     * @param lon2 第二个点经度
     * @return 距离（米）
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    /**
     * 验证定位精度是否满足打卡要求
     * <p>
     * 定位精度必须<50米才允许打卡
     * </p>
     *
     * @param accuracy GPS定位精度（米）
     * @return true-精度满足要求, false-精度不足
     */
    boolean validateAccuracy(float accuracy);
}
