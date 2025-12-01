package net.lab1024.sa.base.common.device;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 适配器信息
 * <p>
 * 设备适配器的描述信息，包含版本、支持的协议特性、功能列表等
 * 用于适配器的自描述和能力声明
 * </p>
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdapterInfo {

    /**
     * 适配器名称
     */
    private String name;

    /**
     * 适配器版本
     */
    private String version;

    /**
     * 适配器描述
     */
    private String description;

    /**
     * 支持的设备类型
     */
    private String supportedDeviceType;

    /**
     * 支持的制造商列表
     */
    private List<String> supportedManufacturers;

    /**
     * 支持的协议列表
     */
    private List<String> supportedProtocols;

    /**
     * 支持的功能列表
     */
    private List<String> supportedFeatures;

    /**
     * 适配器能力
     */
    private AdapterCapabilities capabilities;

    /**
     * 技术要求
     */
    private TechnicalRequirements technicalRequirements;

    /**
     * 性能指标
     */
    private PerformanceMetrics performanceMetrics;

    /**
     * 限制和注意事项
     */
    private List<String> limitations;

    /**
     * 联系信息
     */
    private Map<String, String> contactInfo;

    /**
     * 最后更新时间
     */
    private String lastUpdateTime;

    /**
     * 适配器能力内部类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdapterCapabilities {

        /**
         * 是否支持连接测试
         */
        private boolean supportsConnectionTest;

        /**
         * 是否支持人员信息下发
         */
        private boolean supportsPersonDispatch;

        /**
         * 是否支持生物特征下发
         */
        private boolean supportsBiometricDispatch;

        /**
         * 是否支持配置下发
         */
        private boolean supportsConfigDispatch;

        /**
         * 是否支持状态查询
         */
        private boolean supportsStatusQuery;

        /**
         * 是否支持批量操作
         */
        private boolean supportsBatchOperation;

        /**
         * 是否支持实时监控
         */
        private boolean supportsRealTimeMonitoring;

        /**
         * 是否支持断线重连
         */
        private boolean supportsReconnection;

        /**
         * 是否支持数据加密
         */
        private boolean supportsEncryption;
    }

    /**
     * 技术要求内部类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TechnicalRequirements {

        /**
         * 最低Java版本
         */
        private String minimumJavaVersion;

        /**
         * 网络协议要求
         */
        private List<String> networkProtocols;

        /**
         * 依赖库列表
         */
        private List<String> dependencies;

        /**
         * 端口要求
         */
        private List<String> portRequirements;

        /**
         * 防火墙规则
         */
        private List<String> firewallRules;

        /**
         * 系统要求
         */
        private Map<String, String> systemRequirements;
    }

    /**
     * 性能指标内部类
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PerformanceMetrics {

        /**
         * 最大并发连接数
         */
        private Integer maxConcurrentConnections;

        /**
         * 平均响应时间（毫秒）
         */
        private Long averageResponseTime;

        /**
         * 连接建立时间（毫秒）
         */
        private Long connectionEstablishmentTime;

        /**
         * 数据传输速率（字节/秒）
         */
        private Integer dataTransferRate;

        /**
         * 支持的设备数量
         */
        private Integer supportedDeviceCount;

        /**
         * 可靠性评级（1-5）
         */
        private Integer reliabilityRating;

        /**
         * 性能评级（1-5）
         */
        private Integer performanceRating;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建默认适配器信息
     *
     * @param name 适配器名称
     * @param deviceType 设备类型
     * @return 默认适配器信息
     */
    public static AdapterInfo createDefault(String name, String deviceType) {
        return AdapterInfo.builder()
            .name(name)
            .version("1.0.0")
            .description("标准" + deviceType + "设备适配器")
            .supportedDeviceType(deviceType)
            .supportedManufacturers(new ArrayList<>())
            .supportedProtocols(new ArrayList<>())
            .supportedFeatures(new ArrayList<>())
            .capabilities(AdapterCapabilities.builder().build())
            .technicalRequirements(TechnicalRequirements.builder().build())
            .performanceMetrics(PerformanceMetrics.builder().build())
            .limitations(new ArrayList<>())
            .contactInfo(new HashMap<>())
            .lastUpdateTime("2025-11-24")
            .build();
    }

    /**
     * 创建完整适配器信息
     *
     * @param name 适配器名称
     * @param version 版本
     * @param description 描述
     * @param deviceType 设备类型
     * @param manufacturers 制造商列表
     * @param protocols 协议列表
     * @param features 功能列表
     * @return 完整的适配器信息
     */
    public static AdapterInfo createFull(String name, String version, String description,
                                         String deviceType, List<String> manufacturers,
                                         List<String> protocols, List<String> features) {
        return AdapterInfo.builder()
            .name(name)
            .version(version)
            .description(description)
            .supportedDeviceType(deviceType)
            .supportedManufacturers(manufacturers != null ? manufacturers : new ArrayList<>())
            .supportedProtocols(protocols != null ? protocols : new ArrayList<>())
            .supportedFeatures(features != null ? features : new ArrayList<>())
            .capabilities(AdapterCapabilities.builder().build())
            .technicalRequirements(TechnicalRequirements.builder().build())
            .performanceMetrics(PerformanceMetrics.builder().build())
            .limitations(new ArrayList<>())
            .contactInfo(new HashMap<>())
            .lastUpdateTime("2025-11-24")
            .build();
    }

    // ==================== 实例方法 ====================

    /**
     * 添加支持的制造商
     *
     * @param manufacturer 制造商
     * @return 当前适配器信息对象
     */
    public AdapterInfo addSupportedManufacturer(String manufacturer) {
        if (this.supportedManufacturers == null) {
            this.supportedManufacturers = new ArrayList<>();
        }
        this.supportedManufacturers.add(manufacturer);
        return this;
    }

    /**
     * 添加支持的协议
     *
     * @param protocol 协议
     * @return 当前适配器信息对象
     */
    public AdapterInfo addSupportedProtocol(String protocol) {
        if (this.supportedProtocols == null) {
            this.supportedProtocols = new ArrayList<>();
        }
        this.supportedProtocols.add(protocol);
        return this;
    }

    /**
     * 添加支持的功能
     *
     * @param feature 功能
     * @return 当前适配器信息对象
     */
    public AdapterInfo addSupportedFeature(String feature) {
        if (this.supportedFeatures == null) {
            this.supportedFeatures = new ArrayList<>();
        }
        this.supportedFeatures.add(feature);
        return this;
    }

    /**
     * 添加限制说明
     *
     * @param limitation 限制说明
     * @return 当前适配器信息对象
     */
    public AdapterInfo addLimitation(String limitation) {
        if (this.limitations == null) {
            this.limitations = new ArrayList<>();
        }
        this.limitations.add(limitation);
        return this;
    }

    /**
     * 添加联系信息
     *
     * @param key 键
     * @param value 值
     * @return 当前适配器信息对象
     */
    public AdapterInfo addContactInfo(String key, String value) {
        if (this.contactInfo == null) {
            this.contactInfo = new HashMap<>();
        }
        this.contactInfo.put(key, value);
        return this;
    }

    /**
     * 设置能力
     *
     * @param capabilities 适配器能力
     * @return 当前适配器信息对象
     */
    public AdapterInfo withCapabilities(AdapterCapabilities capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    /**
     * 设置技术要求
     *
     * @param technicalRequirements 技术要求
     * @return 当前适配器信息对象
     */
    public AdapterInfo withTechnicalRequirements(TechnicalRequirements technicalRequirements) {
        this.technicalRequirements = technicalRequirements;
        return this;
    }

    /**
     * 设置性能指标
     *
     * @param performanceMetrics 性能指标
     * @return 当前适配器信息对象
     */
    public AdapterInfo withPerformanceMetrics(PerformanceMetrics performanceMetrics) {
        this.performanceMetrics = performanceMetrics;
        return this;
    }

    /**
     * 获取基本信息摘要
     *
     * @return 基本信息摘要
     */
    public String getBasicSummary() {
        return String.format("%s v%s - %s (%s)",
            name != null ? name : "Unknown",
            version != null ? version : "1.0.0",
            description != null ? description : "",
            supportedDeviceType != null ? supportedDeviceType : "Unknown");
    }

    /**
     * 获取能力摘要
     *
     * @return 能力摘要
     */
    public String getCapabilitiesSummary() {
        if (capabilities == null) {
            return "能力信息不可用";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("支持功能: ");

        if (capabilities.isSupportsConnectionTest()) {
            summary.append("连接测试, ");
        }
        if (capabilities.isSupportsPersonDispatch()) {
            summary.append("人员下发, ");
        }
        if (capabilities.isSupportsBiometricDispatch()) {
            summary.append("生物特征下发, ");
        }
        if (capabilities.isSupportsConfigDispatch()) {
            summary.append("配置下发, ");
        }
        if (capabilities.isSupportsStatusQuery()) {
            summary.append("状态查询, ");
        }
        if (capabilities.isSupportsBatchOperation()) {
            summary.append("批量操作, ");
        }

        // 移除最后的逗号和空格
        if (summary.length() > 2) {
            summary.setLength(summary.length() - 2);
        }

        return summary.toString();
    }

    /**
     * 检查是否支持指定功能
     *
     * @param feature 功能名称
     * @return 是否支持
     */
    public boolean supportsFeature(String feature) {
        return supportedFeatures != null && supportedFeatures.contains(feature);
    }

    /**
     * 检查是否支持指定制造商
     *
     * @param manufacturer 制造商名称
     * @return 是否支持
     */
    public boolean supportsManufacturer(String manufacturer) {
        return supportedManufacturers != null && supportedManufacturers.contains(manufacturer);
    }

    /**
     * 检查是否支持指定协议
     *
     * @param protocol 协议名称
     * @return 是否支持
     */
    public boolean supportsProtocol(String protocol) {
        return supportedProtocols != null && supportedProtocols.contains(protocol);
    }

    /**
     * 更新最后更新时间
     */
    public void updateLastUpdateTime() {
        this.lastUpdateTime = java.time.LocalDateTime.now().toString();
    }
}