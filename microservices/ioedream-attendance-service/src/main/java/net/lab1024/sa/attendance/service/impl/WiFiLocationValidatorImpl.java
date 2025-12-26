package net.lab1024.sa.attendance.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.attendance.service.WiFiLocationValidator;
import net.lab1024.sa.common.exception.BusinessException;

/**
 * WiFi定位验证服务实现
 * <p>
 * 实现WiFi定位验证的核心功能：
 * 1. 信号强度验证（-30到-90 dBm为有效范围）
 * 2. MAC地址白名单验证
 * 3. 定位精度计算（基于信号强度衰减模型）
 * 4. WiFi欺骗检测
 * </p>
 * <p>
 * 技术实现：
 * - 使用本地缓存存储白名单（性能优化）
 * - 基于RSSI（接收信号强度指示）计算距离
 * - 使用启发式算法检测WiFi欺骗
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-25
 */
@Slf4j
@Service
public class WiFiLocationValidatorImpl implements WiFiLocationValidator {


    /**
     * 最小有效信号强度（dBm）
     */
    private static final int MIN_SIGNAL_STRENGTH = -90;

    /**
     * 最大有效信号强度（dBm）
     */
    private static final int MAX_SIGNAL_STRENGTH = -30;

    /**
     * 优秀信号强度阈值（dBm）
     */
    private static final int EXCELLENT_SIGNAL_THRESHOLD = -50;

    /**
     * 良好信号强度阈值（dBm）
     */
    private static final int GOOD_SIGNAL_THRESHOLD = -70;

    /**
     * WiFi白名单缓存（MAC地址 -> 位置信息）
     */
    private final Map<String, WiFiLocationInfo> whitelistCache = new HashMap<>();

    /**
     * 已知可疑MAC地址集合
     */
    private final Set<String> suspiciousMacAddresses = new HashSet<>();

    /**
     * 构造函数 - 初始化白名单数据
     */
    public WiFiLocationValidatorImpl() {
        initializeWhitelist();
        initializeSuspiciousMACs();
    }

    /**
     * 验证WiFi信号强度
     * <p>
     * 信号强度范围：-30 dBm（最强）到 -90 dBm（最弱）
     * - 优秀: -30 到 -50 dBm（定位精度<2米）
     * - 良好: -50 到 -70 dBm（定位精度<5米）
     * - 一般: -70 到 -90 dBm（定位精度<10米）
     * - 无效: < -90 dBm（信号太弱，不准许打卡）
     * </p>
     *
     * @param macAddress       MAC地址
     * @param signalStrength  信号强度（dBm）
     * @return true-信号有效, false-信号无效
     */
    @Override
    public boolean validateWiFiSignal(String macAddress, int signalStrength) {
        log.debug("[WiFi定位] 验证信号强度: macAddress={}, signalStrength={}dBm",
                macAddress, signalStrength);

        // 验证信号范围
        if (signalStrength < MIN_SIGNAL_STRENGTH || signalStrength > MAX_SIGNAL_STRENGTH) {
            log.warn("[WiFi定位] 信号强度超出有效范围: macAddress={}, signalStrength={}dBm, range=[{}dBm, {}dBm]",
                    macAddress, signalStrength, MIN_SIGNAL_STRENGTH, MAX_SIGNAL_STRENGTH);
            return false;
        }

        // 检查是否在白名单中
        if (!isWhitelisted(macAddress)) {
            log.warn("[WiFi定位] MAC地址不在白名单中: macAddress={}", macAddress);
            return false;
        }

        log.info("[WiFi定位] 信号强度验证通过: macAddress={}, signalStrength={}dBm",
                macAddress, signalStrength);
        return true;
    }

    /**
     * 检查MAC地址是否在白名单中
     *
     * @param macAddress MAC地址
     * @return true-在白名单中, false-不在
     */
    @Override
    @Cacheable(value = "wifiWhitelist", key = "#macAddress")
    public boolean isWhitelisted(String macAddress) {
        if (macAddress == null || macAddress.isEmpty()) {
            log.warn("[WiFi定位] MAC地址为空");
            return false;
        }

        boolean whitelisted = whitelistCache.containsKey(macAddress);
        log.debug("[WiFi定位] 白名单检查: macAddress={}, whitelisted={}", macAddress, whitelisted);
        return whitelisted;
    }

    /**
     * 计算WiFi定位精度
     * <p>
     * 基于信号强度衰减模型估算距离：
     * - 距离 = 10^((27.55 - signalStrength) / 20)
     * - 这是一个简化的自由空间路径损耗模型
     * </p>
     *
     * @param signalStrength 信号强度（dBm）
     * @return 定位精度（米）
     */
    @Override
    public double calculateAccuracy(int signalStrength) {
        // 使用简化的路径损耗模型计算距离
        // 距离 = 10^((27.55 - signalStrength) / (20 * log10(frequency)))
        // 假设2.4GHz WiFi，距离 ≈ 10^((27.55 - signalStrength) / 69.4)

        double distance = Math.pow(10, (27.55 - signalStrength) / 69.4);

        // 根据信号强度分类定位精度
        double accuracy;
        if (signalStrength >= EXCELLENT_SIGNAL_THRESHOLD) {
            // 优秀信号：精度<2米
            accuracy = Math.min(distance, 2.0);
        } else if (signalStrength >= GOOD_SIGNAL_THRESHOLD) {
            // 良好信号：精度<5米
            accuracy = Math.min(distance, 5.0);
        } else {
            // 一般信号：精度<10米
            accuracy = Math.min(distance, 10.0);
        }

        log.debug("[WiFi定位] 计算定位精度: signalStrength={}dBm, distance={}m, accuracy={}m",
                signalStrength, String.format("%.2f", distance), String.format("%.2f", accuracy));

        return accuracy;
    }

    /**
     * 检测WiFi欺骗
     * <p>
     * 检测方法：
     * 1. 检查MAC地址是否在可疑列表中
     * 2. 检查信号强度是否异常（通常>-30dBm可疑）
     * 3. 检查连接设备数（0或很大可疑）
     * </p>
     *
     * @param macAddress       MAC地址
     * @param signalStrength  信号强度
     * @param connectedDevices 连接设备数
     * @return true-可能是虚假热点, false-正常
     */
    @Override
    public boolean detectMockWiFi(String macAddress, int signalStrength, int connectedDevices) {
        log.debug("[WiFi定位] 检测WiFi欺骗: macAddress={}, signalStrength={}dBm, connectedDevices={}",
                macAddress, signalStrength, connectedDevices);

        // 检查1: MAC地址是否在可疑列表中
        if (suspiciousMacAddresses.contains(macAddress)) {
            log.warn("[WiFi定位] 发现可疑MAC地址: macAddress={}", macAddress);
            return true;
        }

        // 检查2: 信号强度异常（>-30dBm太强，可能是设备很近或伪造）
        if (signalStrength > MAX_SIGNAL_STRENGTH) {
            log.warn("[WiFi定位] 信号强度异常: macAddress={}, signalStrength={}dBm",
                    macAddress, signalStrength);
            return true;
        }

        // 检查3: 连接设备数异常（0或很大）
        if (connectedDevices == 0 || connectedDevices > 100) {
            log.warn("[WiFi定位] 连接设备数异常: macAddress={}, connectedDevices={}",
                    macAddress, connectedDevices);
            return true;
        }

        log.debug("[WiFi定位] WiFi欺骗检测通过: macAddress={}", macAddress);
        return false;
    }

    /**
     * 获取WiFi位置信息
     *
     * @param macAddress MAC地址
     * @return WiFi位置信息
     */
    @Override
    public WiFiLocationInfo getWiFiLocationInfo(String macAddress) {
        log.debug("[WiFi定位] 获取位置信息: macAddress={}", macAddress);

        WiFiLocationInfo locationInfo = whitelistCache.get(macAddress);
        if (locationInfo == null) {
            log.warn("[WiFi定位] 未找到WiFi位置信息: macAddress={}", macAddress);
            throw new BusinessException("WIFI_LOCATION_NOT_FOUND", "未找到WiFi位置信息: " + macAddress);
        }

        log.info("[WiFi定位] 获取位置信息成功: macAddress={}, location={}",
                macAddress, locationInfo.getLocationName());
        return locationInfo;
    }

    /**
     * 初始化白名单数据
     * <p>
     * 实际生产环境应从数据库或配置中心加载
     * </p>
     */
    private void initializeWhitelist() {
        log.info("[WiFi定位] 初始化白名单数据");

        // 示例数据：公司WiFi设备
        whitelistCache.put("AA:BB:CC:DD:EE:01", new WiFiLocationInfo(
                "AA:BB:CC:DD:EE:01", "A栋1楼大厅", 1, 1, "办公区", 116.407526, 39.904030
        ));

        whitelistCache.put("AA:BB:CC:DD:EE:02", new WiFiLocationInfo(
                "AA:BB:CC:DD:EE:02", "A栋2楼会议室", 1, 2, "会议室", 116.407527, 39.904031
        ));

        whitelistCache.put("AA:BB:CC:DD:EE:03", new WiFiLocationInfo(
                "AA:BB:CC:DD:EE:03", "B栋1楼餐厅", 2, 1, "餐厅", 116.407528, 39.904032
        ));

        log.info("[WiFi定位] 白名单初始化完成: count={}", whitelistCache.size());
    }

    /**
     * 初始化可疑MAC地址列表
     * <p>
     * 实际生产环境应从安全策略配置加载
     * </p>
     */
    private void initializeSuspiciousMACs() {
        log.info("[WiFi定位] 初始化可疑MAC地址列表");

        // 示例数据：已知虚假热点
        suspiciousMacAddresses.add("11:22:33:44:55:66");
        suspiciousMacAddresses.add("AA:AA:AA:AA:AA:AA");

        log.info("[WiFi定位] 可疑MAC地址初始化完成: count={}", suspiciousMacAddresses.size());
    }
}
