package net.lab1024.sa.base.common.cache;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存模块枚举
 * <p>
 * 严格遵循repowiki模块化架构规范：
 * - 按业务模块划分缓存命名空间
 * - 统一的模块标识和描述
 * - 支持模块权限和隔离控制
 *
 * @author SmartAdmin Team
 * @since 2025-11-17
 */
@Getter
public enum CacheModule {

    /**
     * 消费模块
     * 包含账户管理、消费记录、支付处理等功能
     */
    CONSUME("consume", "消费模块", CacheNamespace.CONSUME,
            Arrays.asList("账户管理", "消费记录", "支付处理", "退款管理"),
            Arrays.asList("CONSUME", "PAYMENT", "ACCOUNT", "REFUND")),

    /**
     * 门禁模块
     * 包含门禁控制、权限验证、访客管理等功能
     */
    ACCESS("access", "门禁模块", CacheNamespace.ACCESS,
            Arrays.asList("门禁控制", "权限验证", "访客管理", "门禁记录"),
            Arrays.asList("ACCESS", "PERMISSION", "VISITOR", "DOOR")),

    /**
     * 考勤模块
     * 包含考勤记录、规则配置、统计报表等功能
     */
    ATTENDANCE("attendance", "考勤模块", CacheNamespace.ATTENDANCE,
            Arrays.asList("考勤记录", "规则配置", "统计报表", "排班管理"),
            Arrays.asList("ATTENDANCE", "SCHEDULE", "RULE", "REPORT")),

    /**
     * 设备模块
     * 包含设备管理、状态监控、配置管理等功能
     */
    DEVICE("device", "设备模块", CacheNamespace.DEVICE,
            Arrays.asList("设备管理", "状态监控", "配置管理", "设备维护"),
            Arrays.asList("DEVICE", "MONITOR", "CONFIG", "MAINTENANCE")),

    /**
     * 视频模块
     * 包含视频监控、录像管理、分析处理等功能
     */
    VIDEO("video", "视频模块", CacheNamespace.VIDEO,
            Arrays.asList("视频监控", "录像管理", "分析处理", "设备联动"),
            Arrays.asList("VIDEO", "RECORD", "ANALYSIS", "LINKAGE")),

    /**
     * 系统模块
     * 包含系统配置、用户管理、权限管理等功能
     */
    SYSTEM("system", "系统模块", CacheNamespace.SYSTEM,
            Arrays.asList("系统配置", "用户管理", "权限管理", "日志管理"),
            Arrays.asList("SYSTEM", "USER", "ROLE", "LOG")),

    /**
     * 配置模块
     * 包含字典管理、参数配置、模板管理等功能
     */
    CONFIG("config", "配置模块", CacheNamespace.CONFIG,
            Arrays.asList("字典管理", "参数配置", "模板管理", "规则引擎"),
            Arrays.asList("DICT", "PARAM", "TEMPLATE", "RULE")),

    /**
     * 文档模块
     * 包含文档管理、知识库、帮助中心等功能
     */
    DOCUMENT("document", "文档模块", CacheNamespace.DOCUMENT,
            Arrays.asList("文档管理", "知识库", "帮助中心", "版本控制"),
            Arrays.asList("DOC", "KNOWLEDGE", "HELP", "VERSION")),

    /**
     * 临时模块
     * 包含临时数据、会话信息、验证码等功能
     */
    TEMP("temp", "临时模块", CacheNamespace.TEMP,
            Arrays.asList("临时数据", "会话信息", "验证码", "临时权限"),
            Arrays.asList("SESSION", "CAPTCHA", "TEMP_DATA", "TEMP_AUTH"));

    private final String code;
    private final String description;
    private final String namespaceCode;
    private final List<String> features;
    private final List<String> keywords;

    CacheModule(String code, String description, CacheNamespace namespace,
                List<String> features, List<String> keywords) {
        this.code = code;
        this.description = description;
        this.namespaceCode = namespace.getPrefix();
        this.features = features;
        this.keywords = keywords;
    }

    /**
     * 根据代码获取模块
     */
    public static CacheModule fromCode(String code) {
        for (CacheModule module : values()) {
            if (module.code.equalsIgnoreCase(code)) {
                return module;
            }
        }
        throw new IllegalArgumentException("未知的缓存模块代码: " + code);
    }

    /**
     * 根据关键词搜索模块
     */
    public static List<CacheModule> findByKeyword(String keyword) {
        return Arrays.stream(values())
                .filter(module -> module.keywords.stream()
                        .anyMatch(k -> k.equalsIgnoreCase(keyword)))
                .collect(Collectors.toList());
    }

    /**
     * 获取所有模块的代码映射
     */
    public static Map<String, CacheModule> getCodeMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(CacheModule::getCode, Function.identity()));
    }

    /**
     * 检查模块是否支持指定功能
     */
    public boolean supportsFeature(String feature) {
        return features.stream().anyMatch(f -> f.equalsIgnoreCase(feature));
    }

    /**
     * 获取模块的关键功能数量
     */
    public int getFeatureCount() {
        return features.size();
    }

    /**
     * 是否为核心业务模块
     */
    public boolean isCoreModule() {
        return this == CONSUME || this == ACCESS || this == ATTENDANCE || this == DEVICE;
    }

    /**
     * 是否为基础设施模块
     */
    public boolean isInfrastructureModule() {
        return this == SYSTEM || this == CONFIG;
    }

    /**
     * 是否为辅助功能模块
     */
    public boolean isAuxiliaryModule() {
        return this == VIDEO || this == DOCUMENT || this == TEMP;
    }

    /**
     * 获取模块的优先级
     * 1-5级，1级最高优先级
     */
    public int getPriority() {
        if (isCoreModule()) {
            return 1; // 核心模块最高优先级
        } else if (isInfrastructureModule()) {
            return 2; // 基础设施次优先级
        } else if (this == TEMP) {
            return 4; // 临时模块较低优先级
        } else {
            return 3; // 其他辅助模块中等优先级
        }
    }

    /**
     * 获取模块的资源消耗等级
     * 1-5级，1级最低消耗，5级最高消耗
     */
    public int getResourceConsumptionLevel() {
        switch (this) {
            case CONSUME: return 5;    // 消费模块消耗最高
            case ACCESS: return 4;     // 门禁模块消耗较高
            case ATTENDANCE: return 3; // 考勤模块消耗中等
            case VIDEO: return 5;      // 视频模块消耗最高
            case DEVICE: return 4;     // 设备模块消耗较高
            case SYSTEM: return 3;     // 系统模块消耗中等
            case CONFIG: return 2;     // 配置模块消耗较低
            case DOCUMENT: return 2;   // 文档模块消耗较低
            case TEMP: return 1;       // 临时模块消耗最低
            default: return 3;
        }
    }

    @Override
    public String toString() {
        return String.format("CacheModule{code='%s', description='%s', priority=%d}",
                code, description, getPriority());
    }
}