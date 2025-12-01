package net.lab1024.sa.common.device;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备连接测试结果
 * <p>
 * 设备连接测试的标准化结果封装，包含连接状态、耗时、详细信息等
 * 支持多种连接协议的测试结果统一表示
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceConnectionTest {

    /**
     * 连接是否成功
     */
    private boolean success;

    /**
     * 连接消息
     */
    private String message;

    /**
     * 连接耗时（毫秒）
     */
    private Long connectionTime;

    /**
     * 测试时间戳
     */
    private LocalDateTime testTime;

    /**
     * 连接协议
     */
    private String protocol;

    /**
     * 连接地址
     */
    private String connectionAddress;

    /**
     * 设备信息
     */
    private Map<String, Object> deviceInfo;

    /**
     * 连接详情
     */
    private Map<String, Object> connectionDetails;

    /**
     * 响应数据
     */
    private Map<String, Object> responseData;

    /**
     * 错误信息
     */
    private String errorDetail;

    /**
     * 建议操作
     */
    private String recommendation;

    /**
     * 连接质量评级（A/B/C/D/F）
     */
    private String qualityRating;

    /**
     * 创建成功结果
     *
     * @param connectionTimeMs  连接耗时
     * @param protocol          连接协议
     * @param connectionAddress 连接地址
     * @return 成功的连接测试结果
     */
    public static DeviceConnectionTest success(Long connectionTimeMs, String protocol, String connectionAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("protocol", protocol);
        details.put("connectionAddress", connectionAddress);
        details.put("connectionTime", connectionTimeMs + "ms");

        return DeviceConnectionTest.builder()
                .success(true)
                .message("设备连接成功")
                .connectionTime(connectionTimeMs)
                .testTime(LocalDateTime.now())
                .protocol(protocol)
                .connectionAddress(connectionAddress)
                .connectionDetails(details)
                .qualityRating(calculateQualityRating(connectionTimeMs, true))
                .recommendation("连接正常，可以进行数据操作")
                .build();
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage      错误消息
     * @param errorDetail       错误详情
     * @param protocol          连接协议
     * @param connectionAddress 连接地址
     * @return 失败的连接测试结果
     */
    public static DeviceConnectionTest failure(String errorMessage, String errorDetail, String protocol,
            String connectionAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("protocol", protocol);
        details.put("connectionAddress", connectionAddress);
        details.put("errorDetail", errorDetail);

        return DeviceConnectionTest.builder()
                .success(false)
                .message(errorMessage)
                .errorDetail(errorDetail)
                .testTime(LocalDateTime.now())
                .protocol(protocol)
                .connectionAddress(connectionAddress)
                .connectionDetails(details)
                .qualityRating("F")
                .recommendation(getFailureRecommendation(errorDetail))
                .build();
    }

    /**
     * 创建超时结果
     *
     * @param timeoutMs         超时毫秒数
     * @param protocol          连接协议
     * @param connectionAddress 连接地址
     * @return 超时的连接测试结果
     */
    public static DeviceConnectionTest timeout(Long timeoutMs, String protocol, String connectionAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("protocol", protocol);
        details.put("connectionAddress", connectionAddress);
        details.put("timeout", timeoutMs + "ms");

        return DeviceConnectionTest.builder()
                .success(false)
                .message("设备连接超时")
                .errorDetail("连接请求在 " + timeoutMs + "ms 内未得到响应")
                .testTime(LocalDateTime.now())
                .protocol(protocol)
                .connectionAddress(connectionAddress)
                .connectionDetails(details)
                .qualityRating("D")
                .recommendation("检查网络连接和设备状态，调整超时时间")
                .build();
    }

    /**
     * 创建网络错误结果
     *
     * @param networkError      网络错误信息
     * @param protocol          连接协议
     * @param connectionAddress 连接地址
     * @return 网络错误的连接测试结果
     */
    public static DeviceConnectionTest networkError(String networkError, String protocol, String connectionAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("protocol", protocol);
        details.put("connectionAddress", connectionAddress);
        details.put("networkError", networkError);

        return DeviceConnectionTest.builder()
                .success(false)
                .message("网络连接错误")
                .errorDetail(networkError)
                .testTime(LocalDateTime.now())
                .protocol(protocol)
                .connectionAddress(connectionAddress)
                .connectionDetails(details)
                .qualityRating("D")
                .recommendation("检查网络配置、防火墙设置和设备网络连接")
                .build();
    }

    /**
     * 创建认证失败结果
     *
     * @param authType          认证类型
     * @param protocol          连接协议
     * @param connectionAddress 连接地址
     * @return 认证失败的连接测试结果
     */
    public static DeviceConnectionTest authenticationFailed(String authType, String protocol,
            String connectionAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("protocol", protocol);
        details.put("connectionAddress", connectionAddress);
        details.put("authType", authType);

        return DeviceConnectionTest.builder()
                .success(false)
                .message("设备认证失败")
                .errorDetail(authType + " 认证失败，请检查认证信息")
                .testTime(LocalDateTime.now())
                .protocol(protocol)
                .connectionAddress(connectionAddress)
                .connectionDetails(details)
                .qualityRating("D")
                .recommendation("验证认证信息（用户名、密码、密钥等）是否正确")
                .build();
    }

    /**
     * 创建设备离线结果
     *
     * @param protocol          连接协议
     * @param connectionAddress 连接地址
     * @return 设备离线的连接测试结果
     */
    public static DeviceConnectionTest deviceOffline(String protocol, String connectionAddress) {
        Map<String, Object> details = new HashMap<>();
        details.put("protocol", protocol);
        details.put("connectionAddress", connectionAddress);
        details.put("deviceStatus", "OFFLINE");

        return DeviceConnectionTest.builder()
                .success(false)
                .message("设备离线")
                .errorDetail("设备当前处于离线状态，无法建立连接")
                .testTime(LocalDateTime.now())
                .protocol(protocol)
                .connectionAddress(connectionAddress)
                .connectionDetails(details)
                .qualityRating("D")
                .recommendation("检查设备电源和网络连接，确认设备已启动")
                .build();
    }

    // ==================== 实例方法 ====================

    /**
     * 添加连接详情信息
     *
     * @param key   键
     * @param value 值
     * @return 当前测试结果对象
     */
    public DeviceConnectionTest addConnectionDetail(String key, Object value) {
        if (this.connectionDetails == null) {
            this.connectionDetails = new HashMap<>();
        }
        this.connectionDetails.put(key, value);
        return this;
    }

    /**
     * 添加响应数据
     *
     * @param key   键
     * @param value 值
     * @return 当前测试结果对象
     */
    public DeviceConnectionTest addResponseData(String key, Object value) {
        if (this.responseData == null) {
            this.responseData = new HashMap<>();
        }
        this.responseData.put(key, value);
        return this;
    }

    /**
     * 添加设备信息
     *
     * @param key   键
     * @param value 值
     * @return 当前测试结果对象
     */
    public DeviceConnectionTest addDeviceInfo(String key, Object value) {
        if (this.deviceInfo == null) {
            this.deviceInfo = new HashMap<>();
        }
        this.deviceInfo.put(key, value);
        return this;
    }

    /**
     * 计算连接质量评级
     *
     * @param connectionTimeMs 连接耗时（毫秒）
     * @param isSuccess        是否成功
     * @return 质量评级（A/B/C/D/F）
     */
    private static String calculateQualityRating(Long connectionTimeMs, boolean isSuccess) {
        if (!isSuccess) {
            return "F";
        }

        if (connectionTimeMs == null) {
            return "C";
        }

        if (connectionTimeMs <= 100) {
            return "A"; // 优秀：<100ms
        } else if (connectionTimeMs <= 500) {
            return "B"; // 良好：100-500ms
        } else if (connectionTimeMs <= 2000) {
            return "C"; // 一般：500ms-2s
        } else if (connectionTimeMs <= 5000) {
            return "D"; // 较差：2s-5s
        } else {
            return "F"; // 很差：>5s
        }
    }

    /**
     * 根据错误详情获取建议操作
     *
     * @param errorDetail 错误详情
     * @return 建议操作
     */
    private static String getFailureRecommendation(String errorDetail) {
        if (errorDetail == null) {
            return "检查设备和网络配置";
        }

        String lowerDetail = errorDetail.toLowerCase();

        if (lowerDetail.contains("timeout") || lowerDetail.contains("超时")) {
            return "检查网络延迟，调整连接超时设置";
        } else if (lowerDetail.contains("refused") || lowerDetail.contains("拒绝")) {
            return "检查设备是否已启动，确认端口是否正确";
        } else if (lowerDetail.contains("unreachable") || lowerDetail.contains("不可达")) {
            return "检查网络连接、防火墙和路由设置";
        } else if (lowerDetail.contains("auth") || lowerDetail.contains("认证")) {
            return "验证用户名、密码或密钥是否正确";
        } else {
            return "检查设备配置和网络连接";
        }
    }

    /**
     * 获取测试结果摘要
     *
     * @return 测试结果摘要字符串
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();

        if (success) {
            summary.append("✅ 连接成功");
        } else {
            summary.append("❌ 连接失败");
        }

        if (protocol != null) {
            summary.append(" (").append(protocol).append(")");
        }

        if (connectionTime != null) {
            summary.append(" [").append(connectionTime).append("ms]");
        }

        if (qualityRating != null) {
            summary.append(" [质量: ").append(qualityRating).append("]");
        }

        return summary.toString();
    }

    /**
     * 检查连接质量是否良好
     *
     * @return 质量是否良好（A或B级）
     */
    public boolean isGoodQuality() {
        return qualityRating != null && (qualityRating.equals("A") || qualityRating.equals("B"));
    }

    /**
     * 检查连接质量是否可接受
     *
     * @return 质量是否可接受（A、B或C级）
     */
    public boolean isAcceptableQuality() {
        return qualityRating != null && !qualityRating.equals("D") && !qualityRating.equals("F");
    }

    /**
     * 检查是否需要立即处理
     *
     * @return 是否需要立即处理（D或F级）
     */
    public boolean needsImmediateAttention() {
        return qualityRating != null && (qualityRating.equals("D") || qualityRating.equals("F"));
    }
}
