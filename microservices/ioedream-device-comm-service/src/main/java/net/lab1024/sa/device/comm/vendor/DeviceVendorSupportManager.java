package net.lab1024.sa.device.comm.vendor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.factory.ProtocolAdapterFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备厂商支持管理器
 * <p>
 * 管理和统计所有支持的设备厂商：
 * 1. 厂商设备分类和统计
 * 2. 厂商协议版本管理
 * 3. 新厂商设备接入支持
 * 4. 厂商设备性能评估
 * 5. 厂商兼容性检查
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Schema(description = "设备厂商支持管理器")
public class DeviceVendorSupportManager {

    private final ProtocolAdapterFactory protocolAdapterFactory;

    // 厂商分类统计
    private final Map<String, VendorInfo> vendorRegistry = new ConcurrentHashMap<>();
    private final Map<String, List<DeviceInfo>> vendorDevices = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> deviceTypeCounts = new ConcurrentHashMap<>();

    // 支持统计
    private final AtomicInteger totalSupportedVendors = new AtomicInteger(0);
    private final AtomicInteger totalSupportedDevices = new AtomicInteger(0);
    private final AtomicInteger totalSupportedProtocols = new AtomicInteger(0);

    /**
     * 构造函数
     */
    public DeviceVendorSupportManager(ProtocolAdapterFactory protocolAdapterFactory) {
        this.protocolAdapterFactory = protocolAdapterFactory;
        initializeVendorRegistry();
    }

    /**
     * 获取所有支持的厂商信息
     *
     * @return 厂商信息列表
     */
    public List<VendorInfo> getAllSupportedVendors() {
        return new ArrayList<>(vendorRegistry.values());
    }

    /**
     * 根据厂商名称获取厂商信息
     *
     * @param vendorName 厂商名称
     * @return 厂商信息
     */
    public VendorInfo getVendorInfo(String vendorName) {
        if (vendorName == null || vendorName.trim().isEmpty()) {
            return null;
        }
        return vendorRegistry.get(vendorName.toUpperCase());
    }

    /**
     * 获取指定厂商的所有设备
     *
     * @param vendorName 厂商名称
     * @return 设备信息列表
     */
    public List<DeviceInfo> getVendorDevices(String vendorName) {
        if (vendorName == null || vendorName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return vendorDevices.getOrDefault(vendorName.toUpperCase(), new ArrayList<>());
    }

    /**
     * 根据设备类型获取所有支持的设备
     *
     * @param deviceType 设备类型
     * @return 设备信息列表
     */
    public List<DeviceInfo> getDevicesByType(String deviceType) {
        if (deviceType == null || deviceType.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<DeviceInfo> result = new ArrayList<>();
        for (List<DeviceInfo> deviceList : vendorDevices.values()) {
            for (DeviceInfo device : deviceList) {
                if (deviceType.equalsIgnoreCase(device.getDeviceType())) {
                    result.add(device);
                }
            }
        }

        return result;
    }

    /**
     * 检查厂商是否支持
     *
     * @param vendorName 厂商名称
     * @return 是否支持
     */
    public boolean isVendorSupported(String vendorName) {
        if (vendorName == null || vendorName.trim().isEmpty()) {
            return false;
        }
        return vendorRegistry.containsKey(vendorName.toUpperCase());
    }

    /**
     * 检查设备型号是否支持
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    public boolean isDeviceModelSupported(String deviceModel) {
        if (deviceModel == null || deviceModel.trim().isEmpty()) {
            return false;
        }
        return protocolAdapterFactory.isDeviceModelSupported(deviceModel);
    }

    /**
     * 获取厂商支持统计
     *
     * @return 支持统计信息
     */
    public VendorSupportStatistics getSupportStatistics() {
        VendorSupportStatistics stats = new VendorSupportStatistics();

        // 基础统计
        stats.setTotalVendors(totalSupportedVendors.get());
        stats.setTotalDevices(totalSupportedDevices.get());
        stats.setTotalProtocols(totalSupportedProtocols.get());

        // 按厂商统计
        Map<String, Integer> vendorDeviceCount = new HashMap<>();
        for (Map.Entry<String, List<DeviceInfo>> entry : vendorDevices.entrySet()) {
            vendorDeviceCount.put(entry.getKey(), entry.getValue().size());
        }
        stats.setVendorDeviceCount(vendorDeviceCount);

        // 按设备类型统计
        Map<String, Integer> typeCountMap = new HashMap<>();
        for (Map.Entry<String, AtomicInteger> entry : deviceTypeCounts.entrySet()) {
            typeCountMap.put(entry.getKey(), entry.getValue().get());
        }
        stats.setDeviceTypeCount(typeCountMap);

        // 厂商分类统计
        Map<String, Integer> categoryCount = new HashMap<>();
        for (VendorInfo vendor : vendorRegistry.values()) {
            String category = vendor.getCategory();
            categoryCount.merge(category, 1, Integer::sum);
        }
        stats.setCategoryCount(categoryCount);

        // 协议类型统计
        Set<String> protocolTypes = new HashSet<>();
        for (VendorInfo vendor : vendorRegistry.values()) {
            protocolTypes.addAll(vendor.getSupportedProtocols());
        }
        stats.setProtocolTypeCount(protocolTypes.size());
        stats.setProtocolTypes(new ArrayList<>(protocolTypes));

        return stats;
    }

    /**
     * 获取厂商兼容性报告
     *
     * @return 兼容性报告
     */
    public CompatibilityReport getCompatibilityReport() {
        CompatibilityReport report = new CompatibilityReport();
        report.setReportTime(LocalDateTime.now());

        // 分析兼容性
        Map<String, VendorCompatibility> vendorCompatibility = new HashMap<>();
        for (VendorInfo vendor : vendorRegistry.values()) {
            VendorCompatibility compatibility = analyzeVendorCompatibility(vendor);
            vendorCompatibility.put(vendor.getVendorName(), compatibility);
        }
        report.setVendorCompatibility(vendorCompatibility);

        // 计算总体兼容性
        double totalScore = vendorCompatibility.values().stream()
                .mapToDouble(VendorCompatibility::getCompatibilityScore)
                .average()
                .orElse(0.0);
        report.setOverallCompatibilityScore(Math.round(totalScore * 100.0) / 100.0);

        // 确定兼容性等级
        String grade;
        if (totalScore >= 90) {
            grade = "优秀";
        } else if (totalScore >= 80) {
            grade = "良好";
        } else if (totalScore >= 70) {
            grade = "一般";
        } else if (totalScore >= 60) {
            grade = "及格";
        } else {
            grade = "不及格";
        }
        report.setCompatibilityGrade(grade);

        return report;
    }

    /**
     * 注册新厂商设备
     *
     * @param vendorInfo 厂商信息
     * @param deviceInfo 设备信息
     */
    public boolean registerVendorDevice(VendorInfo vendorInfo, DeviceInfo deviceInfo) {
        try {
            log.info("[厂商支持管理] 注册厂商设备: {} - {}", vendorInfo.getVendorName(), deviceInfo.getDeviceModel());

            // 注册厂商信息
            String vendorKey = vendorInfo.getVendorName().toUpperCase();
            if (!vendorRegistry.containsKey(vendorKey)) {
                vendorRegistry.put(vendorKey, vendorInfo);
                totalSupportedVendors.incrementAndGet();
                log.info("[厂商支持管理] 新增厂商: {}, 总数: {}", vendorInfo.getVendorName(), totalSupportedVendors.get());
            }

            // 注册设备信息
            vendorDevices.computeIfAbsent(vendorKey, k -> new ArrayList<>()).add(deviceInfo);
            totalSupportedDevices.incrementAndGet();

            // 更新设备类型统计
            String deviceType = deviceInfo.getDeviceType();
            deviceTypeCounts.computeIfAbsent(deviceType, k -> new AtomicInteger(0)).incrementAndGet();

            // 更新厂商的协议支持
            vendorInfo.getSupportedProtocols().add(deviceInfo.getProtocolType());
            totalSupportedProtocols.set(new HashSet<>(
                    vendorRegistry.values().stream()
                            .flatMap(vendor -> vendor.getSupportedProtocols().stream())
                            .collect(java.util.stream.Collectors.toSet())
            ).size());

            log.info("[厂商支持管理] 厂商设备注册成功: {} - {}, 总设备数: {}, 总协议数: {}",
                    vendorInfo.getVendorName(), deviceInfo.getDeviceModel(),
                    totalSupportedDevices.get(), totalSupportedProtocols.get());

            return true;

        } catch (Exception e) {
            log.error("[厂商支持管理] 注册厂商设备失败", e);
            return false;
        }
    }

    /**
     * 批量注册厂商设备
     *
     * @param vendorDevices 厂商设备映射
     * @return 注册结果
     */
    public Map<String, Boolean> batchRegisterVendorDevices(Map<String, List<DeviceInfo>> vendorDevices) {
        Map<String, Boolean> results = new HashMap<>();

        for (Map.Entry<String, List<DeviceInfo>> entry : vendorDevices.entrySet()) {
            String vendorName = entry.getKey();
            List<DeviceInfo> devices = entry.getValue();

            boolean allSuccess = true;
            for (DeviceInfo device : devices) {
                VendorInfo vendorInfo = getVendorInfo(vendorName);
                if (vendorInfo == null) {
                    // 创建默认厂商信息
                    vendorInfo = createDefaultVendorInfo(vendorName);
                }

                boolean success = registerVendorDevice(vendorInfo, device);
                if (!success) {
                    allSuccess = false;
                }
            }

            results.put(vendorName, allSuccess);
        }

        return results;
    }

    /**
     * 移除厂商设备
     *
     * @param vendorName 厂商名称
     * @param deviceModel 设备型号
     * @return 是否成功
     */
    public boolean removeVendorDevice(String vendorName, String deviceModel) {
        try {
            log.info("[厂商支持管理] 移除厂商设备: {} - {}", vendorName, deviceModel);

            String vendorKey = vendorName.toUpperCase();
            List<DeviceInfo> devices = vendorDevices.get(vendorKey);
            if (devices != null) {
                boolean removed = devices.removeIf(device -> deviceModel.equals(device.getDeviceModel()));
                if (removed) {
                    totalSupportedDevices.decrementAndGet();
                    log.info("[厂商支持管理] 厂商设备移除成功: {} - {}", vendorName, deviceModel);
                }
                return removed;
            }

            return false;

        } catch (Exception e) {
            log.error("[厂商支持管理] 移除厂商设备失败: {} - {}", vendorName, deviceModel, e);
            return false;
        }
    }

    /**
     * 重新加载厂商支持
     *
     * @return 重新加载结果
     */
    public boolean reloadVendorSupport() {
        try {
            log.info("[厂商支持管理] 开始重新加载厂商支持");

            // 清空现有数据
            vendorRegistry.clear();
            vendorDevices.clear();
            deviceTypeCounts.clear();

            // 重置计数器
            totalSupportedVendors.set(0);
            totalSupportedDevices.set(0);
            totalSupportedProtocols.set(0);

            // 重新初始化
            initializeVendorRegistry();

            // 从协议适配器工厂加载支持的设备
            loadFromProtocolAdapterFactory();

            log.info("[厂商支持管理] 厂商支持重新加载完成");
            return true;

        } catch (Exception e) {
            log.error("[厂商支持管理] 重新加载厂商支持失败", e);
            return false;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化厂商注册表
     */
    private void initializeVendorRegistry() {
        try {
            // 注册视频监控厂商
            registerVideoMonitoringVendors();

            // 注册门禁厂商
            registerAccessControlVendors();

            // 注册考勤厂商
            registerAttendanceVendors();

            // 注册消费厂商
            registerConsumeVendors();

            // 注册其他厂商
            registerOtherVendors();

            log.info("[厂商支持管理] 初始化厂商注册表完成, 支持厂商数: {}, 设备数: {}",
                    vendorRegistry.size(), totalSupportedDevices.get());

        } catch (Exception e) {
            log.error("[厂商支持管理] 初始化厂商注册表失败", e);
        }
    }

    /**
     * 注册视频监控厂商
     */
    private void registerVideoMonitoringVendors() {
        // 海康威视
        VendorInfo hikvision = new VendorInfo();
        hikvision.setVendorName("海康威视");
        hikvision.setEnglishName("Hikvision");
        hikvision.setCategory("视频监控");
        hikvision.setFoundedYear(2001);
        hikvision.setWebsite("https://www.hikvision.com");
        hikvision.setDescription("全球领先的视频监控产品及解决方案提供商");
        hikvision.setSupportedProtocols(new ArrayList<>(Arrays.asList("HIKVISION_VIDEO_V2_0", "HIKVISION_ISAPI_V3.0")));
        hikvision.setQualityLevel("A+");

        // 注册海康威视设备
        registerVendorDevice(hikvision, createDeviceInfo(hikvision, "HIKVISION_VIDEO_V2_0", "DS-2CD2032-I", "网络半球机"));
        registerVendorDevice(hikvision, createDeviceInfo(hikvision, "HIKVISION_VIDEO_V2_0", "DS-2CD2142-I", "网络枪机"));
        registerVendorDevice(hikvision, createDeviceInfo(hikvision, "HIKVISION_VIDEO_V2_0", "DS-7608N-K1", "网络录像机"));

        // 大华技术
        VendorInfo dahua = new VendorInfo();
        dahua.setVendorName("大华技术");
        dahua.setEnglishName("Dahua Technology");
        dahua.setCategory("视频监控");
        dahua.setFoundedYear(2001);
        dahua.setWebsite("https://www.dahuatech.com");
        dahua.setDescription("视频监控领域的领先企业");
        dahua.setSupportedProtocols(new ArrayList<>(Arrays.asList("DAHUA_VIDEO_V2_0", "DAHUA_ISAPI_V3.0")));
        dahua.setQualityLevel("A+");

        // 注册大华设备
        registerVendorDevice(dahua, createDeviceInfo(dahua, "DAHUA_VIDEO_V2_0", "DH-IPC-HFW2431S-ZS", "智能半球机"));
        registerVendorDevice(dahua, createDeviceInfo(dahua, "DAHUA_VIDEO_V2_0", "DH-NVR4208-8P-4KS2", "网络录像机"));

        // 宇视科技
        VendorInfo uniview = new VendorInfo();
        uniview.setVendorName("宇视科技");
        uniview.setEnglishName("Uniview");
        uniview.setCategory("视频监控");
        uniview.setFoundedYear(2005);
        uniview.setWebsite("https://www.uniview.com");
        uniview.setDescription("专业的视频监控产品制造商");
        uniview.setSupportedProtocols(new ArrayList<>(Arrays.asList("UNIVIEW_ONVIF_V2_0")));
        uniview.setQualityLevel("A");

        // 注册宇视设备
        registerVendorDevice(uniview, createDeviceInfo(uniview, "UNIVIEW_ONVIF_V2_0", "IPC3244ER3-DUPFW", "网络摄像机"));
        registerVendorDevice(uniview, createDeviceInfo(uniview, "UNIVIEW_ONVIF_V2_0", "NVR301-16E-4K2", "网络录像机"));
    }

    /**
     * 注册门禁厂商
     */
    private void registerAccessControlVendors() {
        // 熵基科技
        VendorInfo entropy = new VendorInfo();
        entropy.setVendorName("熵基科技");
        entropy.setEnglishName("Entropy Biometric");
        entropy.setCategory("门禁控制");
        entropy.setFoundedYear(2015);
        entropy.setWebsite("https://www.entropybiometric.com");
        entropy.setDescription("生物识别门禁系统解决方案提供商");
        entropy.setSupportedProtocols(new ArrayList<>(Arrays.asList("ENTROPY_ACCESS_V4_8")));
        entropy.setQualityLevel("A");

        // 注册熵基设备
        registerVendorDevice(entropy, createDeviceInfo(entropy, "ENTROPY_ACCESS_V4_8", "EB-AC2000", "门禁控制器"));
        registerVendorDevice(entropy, createDeviceInfo(entropy, "ENTROPY_ACCESS_V4_8", "EB-AR2000", "门禁读卡器"));

        // 萤石科技
        VendorInfo rockstone = new VendorInfo();
        rockstone.setVendorName("营石科技");
        rockstone.setEnglishName("Rockstone");
        rockstone.setCategory("门禁控制");
        rockstone.setFoundedYear(2010);
        rockstone.setWebsite("https://www.rockstone.com.cn");
        rockstone.setDescription("智能门禁系统产品制造商");
        rockstone.setSupportedProtocols(new ArrayList<>(Arrays.asList("ROCKSTONE_ACCESS_V1_5")));
        rockstone.setQualityLevel("B+");

        // 注册营石设备
        registerVendorDevice(rockstone, createDeviceInfo(rockstone, "ROCKSTONE_ACCESS_V1_5", "RS-AC1000", "门禁控制器"));
        registerVendorDevice(rockstone, createDeviceInfo(rockstone, "ROCKSTONE_ACCESS_V1_5", "RS-RD2000", "门禁读卡器"));
    }

    /**
     * 注册考勤厂商
     */
    private void registerAttendanceVendors() {
        // 中控智慧
        VendorInfo zkteco = new VendorInfo();
        zkteco.setVendorName("中控智慧");
        zkteco.setEnglishName("ZKTeco");
        zkteco.setCategory("考勤管理");
        zkteco.setFoundedYear(1985);
        zkteco.setWebsite("https://www.zkteco.com");
        zkteco.setDescription("全球领先的考勤管理解决方案提供商");
        zkteco.setSupportedProtocols(new ArrayList<>(Arrays.asList("ZKTeco_TIME_CLOCK_V1_0")));
        zkteco.setQualityLevel("A+");

        // 注册中控考勤设备
        registerVendorDevice(zkteco, createDeviceInfo(zkteco, "ZKTeco_TIME_CLOCK_V1_0", "SC405", "考勤机"));
        registerVendorDevice(zkteco, createDeviceInfo(zkteco, "ZKTeco_TIME_CLOCK_V1_0", "SC700", "人脸识别考勤机"));
    }

    /**
     * 注册消费厂商
     */
    private void registerConsumeVendors() {
        // 新北洋
        VendorInfo xinbeiyang = new VendorInfo();
        xinbeiyang.setVendorName("新北洋");
        xinbeiyang.setEnglishName("Xinbeiyang");
        xinbeiyang.setCategory("消费管理");
        xinbeiyang.setFoundedYear(1994);
        xinbeiyang.setWebsite("https://www.xinbeiyang.com");
        xinbeiyang.setDescription("智能POS机和收银系统提供商");
        xinbeiyang.setSupportedProtocols(new ArrayList<>(Arrays.asList("XINBEIYANG_POS_V1_2")));
        xinbeiyang.setQualityLevel("A");

        // 注册新北洋设备
        registerVendorDevice(xinbeiyang, createDeviceInfo(xinbeiyang, "XINBEIYANG_POS_V1_2", "NPOS-5890", "智能POS机"));
        registerVendorDevice(xinbeiyang, createDeviceInfo(xinbeiyang, "XINBEIYANG_POS_V1_2", "K8", "桌面式收银机"));
    }

    /**
     * 注册其他厂商
     */
    private void registerOtherVendors() {
        // 可以继续添加其他厂商
        // 比如：海康威视、大华、宇视、熵基科技等的其他设备类型
    }

    /**
     * 从协议适配器工厂加载支持的设备
     */
    private void loadFromProtocolAdapterFactory() {
        try {
            Map<String, ProtocolAdapter> allAdapters = protocolAdapterFactory.getAllAdapters();
            for (Map.Entry<String, ProtocolAdapter> entry : allAdapters.entrySet()) {
                ProtocolAdapter adapter = entry.getValue();
                String[] supportedModels = adapter.getSupportedDeviceModels();

                if (supportedModels != null) {
                    for (String model : supportedModels) {
                        // 这里应该根据协议类型确定厂商信息
                        // 简化处理，直接创建默认厂商信息
                        VendorInfo vendorInfo = createDefaultVendorInfo("未知厂商");
                        DeviceInfo deviceInfo = createDeviceInfo(vendorInfo, adapter.getProtocolType(), model, "未知设备");
                        registerVendorDevice(vendorInfo, deviceInfo);
                    }
                }
            }

        } catch (Exception e) {
            log.error("[厂商支持管理] 从协议适配器工厂加载设备失败", e);
        }
    }

    /**
     * 创建默认厂商信息
     */
    private VendorInfo createDefaultVendorInfo(String vendorName) {
        VendorInfo vendorInfo = new VendorInfo();
        vendorInfo.setVendorName(vendorName);
        vendorInfo.setEnglishName(vendorName);
        vendorInfo.setCategory("未知");
        vendorInfo.setFoundedYear(2000);
        vendorInfo.setWebsite("");
        vendorInfo.setDescription("默认厂商信息");
        vendorInfo.setSupportedProtocols(new ArrayList<>());
        vendorInfo.setQualityLevel("B");
        return vendorInfo;
    }

    /**
     * 创建设备信息
     */
    private DeviceInfo createDeviceInfo(VendorInfo vendorInfo, String protocolType, String deviceModel, String deviceName) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setVendorName(vendorInfo.getVendorName());
        deviceInfo.setProtocolType(protocolType);
        deviceInfo.setDeviceModel(deviceModel);
        deviceInfo.setDeviceName(deviceName);
        deviceInfo.setDeviceType(determineDeviceType(protocolType, deviceModel));
        deviceInfo.setRegisterTime(LocalDateTime.now());
        deviceInfo.setQualityLevel(vendorInfo.getQualityLevel());
        return deviceInfo;
    }

    /**
     * 确定设备类型
     */
    private String determineDeviceType(String protocolType, String deviceModel) {
        if (protocolType.contains("VIDEO") || protocolType.contains("CAMERA")) {
            return "视频监控";
        } else if (protocolType.contains("ACCESS") || protocolType.contains("DOOR")) {
            return "门禁控制";
        } else if (protocolType.contains("TIME_CLOCK") || protocolType.contains("ATTENDANCE")) {
            return "考勤管理";
        } else if (protocolType.contains("POS") || protocolType.contains("CONSUME")) {
            return "消费管理";
        } else {
            return "其他设备";
        }
    }

    /**
     * 分析厂商兼容性
     */
    private VendorCompatibility analyzeVendorCompatibility(VendorInfo vendor) {
        VendorCompatibility compatibility = new VendorCompatibility();
        compatibility.setVendorName(vendor.getVendorName());
        compatibility.setAnalyzedTime(LocalDateTime.now());

        // 计算兼容性评分
        double score = calculateCompatibilityScore(vendor);
        compatibility.setCompatibilityScore(score);

        // 确定兼容性等级
        String grade;
        if (score >= 90) {
            grade = "优秀";
        } else if (score >= 80) {
            grade = "良好";
        } else if (score >= 70) {
            grade = "一般";
        } else if (score >= 60) {
            grade = "及格";
        } else {
            grade = "不及格";
        }
        compatibility.setCompatibilityGrade(grade);

        // 兼容性详情
        Map<String, Object> details = new HashMap<>();
        details.put("protocolCount", vendor.getSupportedProtocols().size());
        details.put("qualityLevel", vendor.getQualityLevel());
        details.put("category", vendor.getCategory());
        details.put("foundedYear", vendor.getFoundedYear());
        details.put("website", vendor.getWebsite());

        // 设备数量
        List<DeviceInfo> devices = vendorDevices.get(vendor.getVendorName().toUpperCase());
        details.put("deviceCount", devices != null ? devices.size() : 0);

        compatibility.setCompatibilityDetails(details);

        return compatibility;
    }

    /**
     * 计算兼容性评分
     */
    private double calculateCompatibilityScore(VendorInfo vendor) {
        double score = 0.0;

        // 基础分数（50分）
        score += 50.0;

        // 厂商知名度和质量等级（30分）
        switch (vendor.getQualityLevel()) {
            case "A+":
                score += 30.0;
                break;
            case "A":
                score += 25.0;
                break;
            case "B+":
                score += 20.0;
                break;
            case "B":
                score += 15.0;
                break;
            default:
                score += 10.0;
        }

        // 协议支持（15分）
        int protocolCount = vendor.getSupportedProtocols().size();
        if (protocolCount >= 3) {
            score += 15.0;
        } else if (protocolCount >= 2) {
            score += 10.0;
        } else if (protocolCount >= 1) {
            score += 5.0;
        }

        // 成立时间（5分）
        int currentYear = LocalDateTime.now().getYear();
        int yearsSinceFounded = currentYear - vendor.getFoundedYear();
        if (yearsSinceFounded >= 20) {
            score += 5.0;
        } else if (yearsSinceFounded >= 10) {
            score += 3.0;
        } else if (yearsSinceFounded >= 5) {
            score += 1.0;
        }

        return Math.min(score, 100.0);
    }

    // ==================== 内部类 ====================

    /**
     * 厂商信息
     */
    @Schema(description = "厂商信息")
    public static class VendorInfo {
        private String vendorName;
        private String englishName;
        private String category;
        private Integer foundedYear;
        private String website;
        private String description;
        private List<String> supportedProtocols = new ArrayList<>();
        private String qualityLevel;

        // getters and setters
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }

        public String getEnglishName() { return englishName; }
        public void setEnglishName(String englishName) { this.englishName = englishName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Integer getFoundedYear() { return foundedYear; }
        public void setFoundedYear(Integer foundedYear) { this.foundedYear = foundedYear; }

        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public List<String> getSupportedProtocols() { return supportedProtocols; }
        public void setSupportedProtocols(List<String> supportedProtocols) { this.supportedProtocols = supportedProtocols; }

        public String getQualityLevel() { return qualityLevel; }
        public void setQualityLevel(String qualityLevel) { this.qualityLevel = qualityLevel; }
    }

    /**
     * 设备信息
     */
    @Schema(description = "设备信息")
    public static class DeviceInfo {
        private String vendorName;
        private String protocolType;
        private String deviceModel;
        private String deviceName;
        private String deviceType;
        private LocalDateTime registerTime;
        private String qualityLevel;

        // getters and setters
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }

        public String getProtocolType() { return protocolType; }
        public void setProtocolType(String protocolType) { this.protocolType = protocolType; }

        public String getDeviceModel() { return deviceModel; }
        public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

        public LocalDateTime getRegisterTime() { return registerTime; }
        public void setRegisterTime(LocalDateTime registerTime) { this.registerTime = registerTime; }

        public String getQualityLevel() { return qualityLevel; }
        public void setQualityLevel(String qualityLevel) { this.qualityLevel = qualityLevel; }
    }

    /**
     * 厂商支持统计
     */
    @Schema(description = "厂商支持统计")
    public static class VendorSupportStatistics {
        private Integer totalVendors;
        private Integer totalDevices;
        private Integer totalProtocols;
        private Map<String, Integer> vendorDeviceCount = new HashMap<>();
        private Map<String, Integer> deviceTypeCount = new HashMap<>();
        private Map<String, Integer> categoryCount = new HashMap<>();
        private Integer protocolTypeCount;
        private List<String> protocolTypes = new ArrayList<>();

        // getters and setters
        public Integer getTotalVendors() { return totalVendors; }
        public void setTotalVendors(Integer totalVendors) { this.totalVendors = totalVendors; }

        public Integer getTotalDevices() { return totalDevices; }
        public void setTotalDevices(Integer totalDevices) { this.totalDevices = totalDevices; }

        public Integer getTotalProtocols() { return totalProtocols; }
        public void setTotalProtocols(Integer totalProtocols) { this.totalProtocols = totalProtocols; }

        public Map<String, Integer> getVendorDeviceCount() { return vendorDeviceCount; }
        public void setVendorDeviceCount(Map<String, Integer> vendorDeviceCount) { this.vendorDeviceCount = vendorDeviceCount; }

        public Map<String, Integer> getDeviceTypeCount() { return deviceTypeCount; }
        public void setDeviceTypeCount(Map<String, Integer> deviceTypeCount) { this.deviceTypeCount = deviceTypeCount; }

        public Map<String, Integer> getCategoryCount() { return categoryCount; }
        public void setCategoryCount(Map<String, Integer> categoryCount) { this.categoryCount = categoryCount; }

        public Integer getProtocolTypeCount() { return protocolTypeCount; }
        public void setProtocolTypeCount(Integer protocolTypeCount) { this.protocolTypeCount = protocolTypeCount; }

        public List<String> getProtocolTypes() { return protocolTypes; }
        public void setProtocolTypes(List<String> protocolTypes) { this.protocolTypes = protocolTypes; }
    }

    /**
     * 兼容性报告
     */
    @Schema(description = "兼容性报告")
    public static class CompatibilityReport {
        private LocalDateTime reportTime;
        private Map<String, VendorCompatibility> vendorCompatibility = new HashMap<>();
        private double overallCompatibilityScore;
        private String compatibilityGrade;

        // getters and setters
        public LocalDateTime getReportTime() { return reportTime; }
        public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }

        public Map<String, VendorCompatibility> getVendorCompatibility() { return vendorCompatibility; }
        public void setVendorCompatibility(Map<String, VendorCompatibility> vendorCompatibility) { this.vendorCompatibility = vendorCompatibility; }

        public double getOverallCompatibilityScore() { return overallCompatibilityScore; }
        public void setOverallCompatibilityScore(double overallCompatibilityScore) { this.overallCompatibilityScore = overallCompatibilityScore; }

        public String getCompatibilityGrade() { return compatibilityGrade; }
        public void setCompatibilityGrade(String compatibilityGrade) { this.compatibilityGrade = compatibilityGrade; }
    }

    /**
     * 厂商兼容性
     */
    @Schema(description = "厂商兼容性")
    public static class VendorCompatibility {
        private String vendorName;
        private LocalDateTime analyzedTime;
        private double compatibilityScore;
        private String compatibilityGrade;
        private Map<String, Object> compatibilityDetails = new HashMap<>();

        // getters and setters
        public String getVendorName() { return vendorName; }
        public void setVendorName(String vendorName) { this.vendorName = vendorName; }

        public LocalDateTime getAnalyzedTime() { return analyzedTime; }
        public void setAnalyzedTime(LocalDateTime analyzedTime) { this.analyzedTime = analyzedTime; }

        public double getCompatibilityScore() { return compatibilityScore; }
        public void setCompatibilityScore(double compatibilityScore) { this.compatibilityScore = compatibilityScore; }

        public String getCompatibilityGrade() { return compatibilityGrade; }
        public void setCompatibilityGrade(String compatibilityGrade) { this.compatibilityGrade = compatibilityGrade; }

        public Map<String, Object> getCompatibilityDetails() { return compatibilityDetails; }
        public void setCompatibilityDetails(Map<String, Object> compatibilityDetails) { this.compatibilityDetails = compatibilityDetails; }
    }
}
