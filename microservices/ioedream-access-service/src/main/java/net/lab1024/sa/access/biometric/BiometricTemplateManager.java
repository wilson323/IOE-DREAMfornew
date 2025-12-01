package net.lab1024.sa.access.biometric;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 生物特征模板管理器
 * 管理员工生物特征模板的注册、更新、删除、查询等操作
 * 严格遵循repowiki规范，使用jakarta包和@Resource注入
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @date 2025-11-28
 */
@Slf4j
@Data
public class BiometricTemplateManager {

    /**
     * 模板状态枚举
     */
    public enum TemplateStatus {
        ACTIVE("ACTIVE", "激活"),
        INACTIVE("INACTIVE", "未激活"),
        EXPIRED("EXPIRED", "已过期"),
        DISABLED("DISABLED", "已禁用"),
        PENDING("PENDING", "待审核");

        private final String code;
        private final String description;

        TemplateStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 模板质量等级
     */
    public enum QualityGrade {
        EXCELLENT("EXCELLENT", "优秀", new BigDecimal("0.95")),
        GOOD("GOOD", "良好", new BigDecimal("0.80")),
        FAIR("FAIR", "一般", new BigDecimal("0.65")),
        POOR("POOR", "较差", new BigDecimal("0.50")),
        UNACCEPTABLE("UNACCEPTABLE", "不可接受", BigDecimal.ZERO);

        private final String code;
        private final String description;
        private final BigDecimal threshold;

        QualityGrade(String code, String description, BigDecimal threshold) {
            this.code = code;
            this.description = description;
            this.threshold = threshold;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getThreshold() {
            return threshold;
        }
    }

    /**
     * 模板操作结果
     */
    @Data
    public static class TemplateOperationResult {
        private Boolean success;
        private String message;
        private Long templateId;
        private Map<String, Object> details;
        private List<String> warnings;
        private String errorCode;
    }

    /**
     * 生物特征模板
     */
    @Data
    public static class BiometricTemplate {
        private Long templateId;
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private MultimodalAuthEngine.BiometricType biometricType;
        private String featureData; // 加密存储的特征数据
        private String rawFeatureData; // 原始特征数据（用于调试）
        private BigDecimal qualityScore; // 质量评分
        private QualityGrade qualityGrade; // 质量等级
        private TemplateStatus status; // 模板状态
        private String algorithmVersion; // 算法版本
        private String algorithmName; // 算法名称
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private LocalDateTime expireTime; // 过期时间
        private LocalDateTime lastUsedTime; // 最后使用时间
        private Integer usageCount; // 使用次数
        private Integer successCount; // 成功次数
        private Integer failureCount; // 失败次数
        private BigDecimal successRate; // 成功率
        private String captureDeviceId; // 采集设备ID
        private String captureDeviceName; // 采集设备名称
        private String captureEnvironment; // 采集环境
        private Map<String, Object> captureMetadata; // 采集元数据
        private String storageLocation; // 存储位置
        private Boolean isPrimary; // 是否为主模板
        private String templateVersion; // 模板版本
        private Map<String, Object> customAttributes; // 自定义属性
    }

    /**
     * 模板注册请求
     */
    @Data
    public static class TemplateRegistrationRequest {
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        private MultimodalAuthEngine.BiometricType biometricType;
        private String featureData;
        private String algorithmVersion;
        private String captureDeviceId;
        private String captureEnvironment;
        private Map<String, Object> captureMetadata;
        private Boolean setAsPrimary;
        private Map<String, Object> customAttributes;
    }

    /**
     * 模板查询条件
     */
    @Data
    public static class TemplateQuery {
        private Long employeeId;
        private String employeeCode;
        private MultimodalAuthEngine.BiometricType biometricType;
        private TemplateStatus status;
        private QualityGrade qualityGrade;
        private LocalDateTime createTimeFrom;
        private LocalDateTime createTimeTo;
        private Boolean primaryOnly;
        private String algorithmVersion;
        private Integer pageSize;
        private Integer pageNumber;
    }

    /**
     * 模板统计信息
     */
    @Data
    public static class TemplateStatistics {
        private Long totalTemplates;
        private Long activeTemplates;
        private Map<MultimodalAuthEngine.BiometricType, Long> typeDistribution;
        private Map<QualityGrade, Long> qualityDistribution;
        private Map<TemplateStatus, Long> statusDistribution;
        private BigDecimal averageUsageCount;
        private BigDecimal averageSuccessRate;
        private LocalDateTime oldestTemplateTime;
        private LocalDateTime newestTemplateTime;
        private Map<String, Long> deviceDistribution;
    }

    /**
     * 模板存储（模拟内存存储）
     */
    private Map<Long, BiometricTemplate> templateStorage = new ConcurrentHashMap<>();
    private Map<Long, List<Long>> employeeTemplateIndex = new ConcurrentHashMap<>(); // 员工ID -> 模板ID列表
    private Map<MultimodalAuthEngine.BiometricType, List<Long>> typeIndex = new ConcurrentHashMap<>(); // 类型 -> 模板ID列表

    /**
     * 全局统计信息
     */
    private Map<String, Object> globalStatistics = new HashMap<>();

    /**
     * 模板配置
     */
    private Map<String, Object> configuration = new HashMap<>();

    /**
     * 初始化模板管理器
     */
    public void initializeManager() {
        log.info("生物特征模板管理器初始化开始");

        // 加载配置
        loadConfiguration();

        // 加载现有模板（模拟）
        loadExistingTemplates();

        log.info("生物特征模板管理器初始化完成，加载了{}个模板", templateStorage.size());
    }

    /**
     * 加载配置
     */
    private void loadConfiguration() {
        configuration.put("maxTemplatesPerEmployee", 10);
        configuration.put("maxTemplatesPerType", 3);
        configuration.put("templateExpireDays", 365);
        configuration.put("minQualityScore", BigDecimal.valueOf(0.65));
        configuration.put("enableAutoCleanup", true);
        configuration.put("backupEnabled", true);

        log.debug("加载配置：{}", configuration);
    }

    /**
     * 加载现有模板（模拟）
     */
    private void loadExistingTemplates() {
        // 模拟加载现有模板
        for (long employeeId = 1L; employeeId <= 50L; employeeId++) {
            // 为每个员工创建人脸模板
            BiometricTemplate faceTemplate = createMockTemplate(employeeId, MultimodalAuthEngine.BiometricType.FACE);
            registerTemplateInternal(faceTemplate);

            // 为部分员工创建指纹模板
            if (employeeId % 2 == 0) {
                BiometricTemplate fingerprintTemplate = createMockTemplate(employeeId,
                        MultimodalAuthEngine.BiometricType.FINGERPRINT);
                registerTemplateInternal(fingerprintTemplate);
            }

            // 为少数员工创建虹膜模板
            if (employeeId % 10 == 0) {
                BiometricTemplate irisTemplate = createMockTemplate(employeeId,
                        MultimodalAuthEngine.BiometricType.IRIS);
                registerTemplateInternal(irisTemplate);
            }
        }

        log.debug("加载了{}个模拟模板", templateStorage.size());
    }

    /**
     * 创建模拟模板
     */
    private BiometricTemplate createMockTemplate(Long employeeId, MultimodalAuthEngine.BiometricType biometricType) {
        BiometricTemplate template = new BiometricTemplate();
        template.setTemplateId(System.currentTimeMillis() + employeeId * 1000 + biometricType.ordinal());
        template.setEmployeeId(employeeId);
        template.setEmployeeName("员工" + employeeId);
        template.setEmployeeCode("EMP" + String.format("%06d", employeeId));
        template.setBiometricType(biometricType);
        template.setFeatureData("encrypted_feature_" + biometricType.getCode() + "_" + employeeId);
        template.setQualityScore(BigDecimal.valueOf(0.7 + Math.random() * 0.29));
        template.setQualityGrade(determineQualityGrade(template.getQualityScore()));
        template.setStatus(TemplateStatus.ACTIVE);
        template.setAlgorithmVersion("v2.1.0");
        template.setAlgorithmName("DeepFeatureNet");
        template.setCreateTime(LocalDateTime.now().minusDays((int) (Math.random() * 365)));
        template.setUpdateTime(template.getCreateTime());
        template.setExpireTime(template.getCreateTime().plusDays((Integer) configuration.get("templateExpireDays")));
        template.setUsageCount((int) (Math.random() * 100));
        template.setSuccessCount((int) (template.getUsageCount() * (0.8 + Math.random() * 0.19)));
        template.setFailureCount(template.getUsageCount() - template.getSuccessCount());
        template.setSuccessRate(template.getUsageCount() > 0 ? BigDecimal.valueOf(template.getSuccessCount())
                .divide(BigDecimal.valueOf(template.getUsageCount()), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        template.setCaptureDeviceId("DEVICE_" + (long) (Math.random() * 10 + 1));
        template.setCaptureDeviceName("生物识别设备" + template.getCaptureDeviceId());
        template.setCaptureEnvironment("OFFICE");
        template.setStorageLocation("LOCAL_DB");
        template.setIsPrimary(true);
        template.setTemplateVersion("1.0");

        // 设置最后使用时间
        if (template.getUsageCount() > 0) {
            template.setLastUsedTime(LocalDateTime.now().minusDays((int) (Math.random() * 30)));
        }

        return template;
    }

    /**
     * 注册生物特征模板
     *
     * @param request 注册请求
     * @return 注册结果
     */
    public TemplateOperationResult registerTemplate(TemplateRegistrationRequest request) {
        log.info("开始注册生物特征模板：员工ID={}，类型={}", request.getEmployeeId(), request.getBiometricType());

        TemplateOperationResult result = new TemplateOperationResult();
        result.setSuccess(false);
        result.setDetails(new HashMap<>());
        result.setWarnings(new ArrayList<>());

        try {
            // 1. 验证请求参数
            String validationError = validateRegistrationRequest(request);
            if (validationError != null) {
                result.setMessage("请求参数验证失败：" + validationError);
                return result;
            }

            // 2. 检查模板数量限制
            String limitError = checkTemplateLimits(request);
            if (limitError != null) {
                result.setMessage(limitError);
                return result;
            }

            // 3. 评估模板质量
            BigDecimal qualityScore = assessTemplateQuality(request);
            QualityGrade qualityGrade = determineQualityGrade(qualityScore);

            if (qualityScore.compareTo((BigDecimal) configuration.get("minQualityScore")) < 0) {
                result.setMessage("模板质量不达标，评分：" + qualityScore);
                return result;
            }

            // 4. 创建模板对象
            BiometricTemplate template = createTemplateFromRequest(request, qualityScore, qualityGrade);

            // 5. 处理主模板设置
            if (Boolean.TRUE.equals(request.getSetAsPrimary())) {
                setOtherTemplatesInactive(template);
            }

            // 6. 存储模板
            registerTemplateInternal(template);

            // 7. 更新统计信息
            updateStatistics("template_registered", request.getBiometricType());

            result.setSuccess(true);
            result.setTemplateId(template.getTemplateId());
            result.setMessage("模板注册成功");
            result.getDetails().put("qualityScore", qualityScore);
            result.getDetails().put("qualityGrade", qualityGrade.getDescription());
            result.getDetails().put("algorithmVersion", template.getAlgorithmVersion());

            log.info("生物特征模板注册成功：模板ID={}，员工ID={}，质量分数={}",
                    template.getTemplateId(), template.getEmployeeId(), qualityScore);

        } catch (Exception e) {
            log.error("生物特征模板注册失败", e);
            result.setMessage("注册失败：" + e.getMessage());
            result.setErrorCode("REGISTRATION_ERROR");
        }

        return result;
    }

    /**
     * 验证注册请求
     */
    private String validateRegistrationRequest(TemplateRegistrationRequest request) {
        if (request.getEmployeeId() == null) {
            return "员工ID不能为空";
        }

        if (request.getBiometricType() == null) {
            return "生物特征类型不能为空";
        }

        if (request.getFeatureData() == null || request.getFeatureData().trim().isEmpty()) {
            return "特征数据不能为空";
        }

        if (request.getAlgorithmVersion() == null || request.getAlgorithmVersion().trim().isEmpty()) {
            return "算法版本不能为空";
        }

        return null;
    }

    /**
     * 检查模板数量限制
     */
    private String checkTemplateLimits(TemplateRegistrationRequest request) {
        Long employeeId = request.getEmployeeId();
        MultimodalAuthEngine.BiometricType biometricType = request.getBiometricType();

        // 检查员工模板总数限制
        List<Long> employeeTemplates = employeeTemplateIndex.get(employeeId);
        if (employeeTemplates != null
                && employeeTemplates.size() >= (Integer) configuration.get("maxTemplatesPerEmployee")) {
            return "该员工已达到最大模板数量限制：" + configuration.get("maxTemplatesPerEmployee");
        }

        // 检查类型模板数量限制
        long typeCount = employeeTemplates != null ? employeeTemplates.stream()
                .mapToLong(templateId -> {
                    BiometricTemplate template = templateStorage.get(templateId);
                    return template != null && biometricType.equals(template.getBiometricType()) ? 1 : 0;
                })
                .sum() : 0;

        if (typeCount >= (Integer) configuration.get("maxTemplatesPerType")) {
            return "该员工此类型的模板已达到最大数量限制：" + configuration.get("maxTemplatesPerType");
        }

        return null;
    }

    /**
     * 评估模板质量
     */
    private BigDecimal assessTemplateQuality(TemplateRegistrationRequest request) {
        // 模拟质量评估
        BigDecimal baseScore = BigDecimal.valueOf(0.8);

        // 根据采集环境调整
        if ("OFFICE".equals(request.getCaptureEnvironment())) {
            baseScore = baseScore.add(BigDecimal.valueOf(0.1));
        } else if ("OUTDOOR".equals(request.getCaptureEnvironment())) {
            baseScore = baseScore.subtract(BigDecimal.valueOf(0.05));
        }

        // 根据采集元数据调整
        if (request.getCaptureMetadata() != null) {
            Object lightingCondition = request.getCaptureMetadata().get("lightingCondition");
            if ("GOOD".equals(lightingCondition)) {
                baseScore = baseScore.add(BigDecimal.valueOf(0.05));
            } else if ("POOR".equals(lightingCondition)) {
                baseScore = baseScore.subtract(BigDecimal.valueOf(0.1));
            }
        }

        // 确保分数在合理范围内
        baseScore = baseScore.max(BigDecimal.valueOf(0.3)).min(BigDecimal.valueOf(1.0));

        return baseScore.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 确定质量等级
     */
    private QualityGrade determineQualityGrade(BigDecimal qualityScore) {
        for (QualityGrade grade : QualityGrade.values()) {
            if (qualityScore.compareTo(grade.getThreshold()) >= 0) {
                return grade;
            }
        }
        return QualityGrade.UNACCEPTABLE;
    }

    /**
     * 从请求创建模板对象
     */
    private BiometricTemplate createTemplateFromRequest(TemplateRegistrationRequest request,
            BigDecimal qualityScore,
            QualityGrade qualityGrade) {
        BiometricTemplate template = new BiometricTemplate();
        template.setTemplateId(System.currentTimeMillis());
        template.setEmployeeId(request.getEmployeeId());
        template.setEmployeeName(request.getEmployeeName());
        template.setEmployeeCode(request.getEmployeeCode());
        template.setBiometricType(request.getBiometricType());
        template.setFeatureData(encryptFeatureData(request.getFeatureData()));
        template.setQualityScore(qualityScore);
        template.setQualityGrade(qualityGrade);
        template.setStatus(TemplateStatus.ACTIVE);
        template.setAlgorithmVersion(request.getAlgorithmVersion());
        template.setAlgorithmName("DeepFeatureNet"); // 默认算法
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        template.setExpireTime(LocalDateTime.now().plusDays((Integer) configuration.get("templateExpireDays")));
        template.setUsageCount(0);
        template.setSuccessCount(0);
        template.setFailureCount(0);
        template.setSuccessRate(BigDecimal.ZERO);
        template.setCaptureDeviceId(request.getCaptureDeviceId());
        template.setCaptureEnvironment(request.getCaptureEnvironment());
        template.setCaptureMetadata(request.getCaptureMetadata());
        template.setStorageLocation("LOCAL_DB");
        template.setIsPrimary(Boolean.TRUE.equals(request.getSetAsPrimary()));
        template.setTemplateVersion("1.0");
        template.setCustomAttributes(request.getCustomAttributes());

        return template;
    }

    /**
     * 加密特征数据（模拟）
     */
    private String encryptFeatureData(String featureData) {
        // 模拟加密过程
        return "encrypted_" + featureData + "_" + System.currentTimeMillis();
    }

    /**
     * 将其他模板设置为非主模板
     */
    private void setOtherTemplatesInactive(BiometricTemplate newTemplate) {
        List<Long> employeeTemplates = employeeTemplateIndex.get(newTemplate.getEmployeeId());
        if (employeeTemplates != null) {
            for (Long templateId : employeeTemplates) {
                BiometricTemplate template = templateStorage.get(templateId);
                if (template != null && newTemplate.getBiometricType().equals(template.getBiometricType())) {
                    template.setIsPrimary(false);
                    template.setUpdateTime(LocalDateTime.now());
                }
            }
        }
    }

    /**
     * 内部注册模板
     */
    private void registerTemplateInternal(BiometricTemplate template) {
        templateStorage.put(template.getTemplateId(), template);

        // 更新员工索引
        employeeTemplateIndex.computeIfAbsent(template.getEmployeeId(), k -> new ArrayList<>())
                .add(template.getTemplateId());

        // 更新类型索引
        typeIndex.computeIfAbsent(template.getBiometricType(), k -> new ArrayList<>()).add(template.getTemplateId());

        log.debug("模板已注册到存储：模板ID={}，员工ID={}，类型={}",
                template.getTemplateId(), template.getEmployeeId(), template.getBiometricType());
    }

    /**
     * 删除生物特征模板
     *
     * @param templateId 模板ID
     * @return 删除结果
     */
    public TemplateOperationResult deleteTemplate(Long templateId) {
        log.info("开始删除生物特征模板：{}", templateId);

        TemplateOperationResult result = new TemplateOperationResult();
        result.setSuccess(false);

        try {
            BiometricTemplate template = templateStorage.get(templateId);
            if (template == null) {
                result.setMessage("模板不存在");
                return result;
            }

            // 检查是否可以删除
            if (!canDeleteTemplate(template)) {
                result.setMessage("模板使用中，无法删除");
                return result;
            }

            // 从存储中删除
            templateStorage.remove(templateId);

            // 更新索引
            List<Long> employeeTemplates = employeeTemplateIndex.get(template.getEmployeeId());
            if (employeeTemplates != null) {
                employeeTemplates.remove(templateId);
                if (employeeTemplates.isEmpty()) {
                    employeeTemplateIndex.remove(template.getEmployeeId());
                }
            }

            List<Long> typeTemplates = typeIndex.get(template.getBiometricType());
            if (typeTemplates != null) {
                typeTemplates.remove(templateId);
                if (typeTemplates.isEmpty()) {
                    typeIndex.remove(template.getBiometricType());
                }
            }

            // 更新统计信息
            updateStatistics("template_deleted", template.getBiometricType());

            result.setSuccess(true);
            result.setMessage("模板删除成功");
            result.getDetails().put("deletedTemplateId", templateId);
            result.getDetails().put("employeeId", template.getEmployeeId());

            log.info("生物特征模板删除成功：{}", templateId);

        } catch (Exception e) {
            log.error("删除生物特征模板失败：{}", templateId, e);
            result.setMessage("删除失败：" + e.getMessage());
            result.setErrorCode("DELETE_ERROR");
        }

        return result;
    }

    /**
     * 检查是否可以删除模板
     */
    private Boolean canDeleteTemplate(BiometricTemplate template) {
        // 如果是主模板且该员工还有其他同类型模板，则不能删除
        if (Boolean.TRUE.equals(template.getIsPrimary())) {
            List<Long> employeeTemplates = employeeTemplateIndex.get(template.getEmployeeId());
            if (employeeTemplates != null) {
                long sameTypeCount = employeeTemplates.stream()
                        .mapToLong(id -> {
                            BiometricTemplate t = templateStorage.get(id);
                            return t != null && template.getBiometricType().equals(t.getBiometricType()) &&
                                    !t.getTemplateId().equals(template.getTemplateId()) ? 1 : 0;
                        })
                        .sum();
                return sameTypeCount > 0;
            }
        }
        return true;
    }

    /**
     * 更新模板使用统计
     *
     * @param templateId 模板ID
     * @param success    是否成功
     * @return 更新结果
     */
    public TemplateOperationResult updateTemplateUsage(Long templateId, Boolean success) {
        BiometricTemplate template = templateStorage.get(templateId);
        if (template == null) {
            TemplateOperationResult result = new TemplateOperationResult();
            result.setSuccess(false);
            result.setMessage("模板不存在");
            return result;
        }

        template.setUsageCount(template.getUsageCount() + 1);
        template.setLastUsedTime(LocalDateTime.now());

        if (Boolean.TRUE.equals(success)) {
            template.setSuccessCount(template.getSuccessCount() + 1);
        } else {
            template.setFailureCount(template.getFailureCount() + 1);
        }

        // 重新计算成功率
        template.setSuccessRate(template.getUsageCount() > 0 ? BigDecimal.valueOf(template.getSuccessCount())
                .divide(BigDecimal.valueOf(template.getUsageCount()), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        template.setUpdateTime(LocalDateTime.now());

        TemplateOperationResult result = new TemplateOperationResult();
        result.setSuccess(true);
        result.setMessage("使用统计更新成功");
        result.getDetails().put("usageCount", template.getUsageCount());
        result.getDetails().put("successRate", template.getSuccessRate());

        return result;
    }

    /**
     * 查询模板
     *
     * @param query 查询条件
     * @return 模板列表
     */
    public List<BiometricTemplate> queryTemplates(TemplateQuery query) {
        log.debug("查询生物特征模板：{}", query);

        return templateStorage.values().stream()
                .filter(template -> matchesQuery(template, query))
                .sorted((t1, t2) -> t2.getUpdateTime().compareTo(t1.getUpdateTime())) // 按更新时间倒序
                .collect(Collectors.toList());
    }

    /**
     * 匹配查询条件
     */
    private Boolean matchesQuery(BiometricTemplate template, TemplateQuery query) {
        if (query.getEmployeeId() != null && !query.getEmployeeId().equals(template.getEmployeeId())) {
            return false;
        }

        if (query.getEmployeeCode() != null && !query.getEmployeeCode().equals(template.getEmployeeCode())) {
            return false;
        }

        if (query.getBiometricType() != null && !query.getBiometricType().equals(template.getBiometricType())) {
            return false;
        }

        if (query.getStatus() != null && !query.getStatus().equals(template.getStatus())) {
            return false;
        }

        if (query.getQualityGrade() != null && !query.getQualityGrade().equals(template.getQualityGrade())) {
            return false;
        }

        if (query.getCreateTimeFrom() != null && template.getCreateTime().isBefore(query.getCreateTimeFrom())) {
            return false;
        }

        if (query.getCreateTimeTo() != null && template.getCreateTime().isAfter(query.getCreateTimeTo())) {
            return false;
        }

        if (Boolean.TRUE.equals(query.getPrimaryOnly()) && !Boolean.TRUE.equals(template.getIsPrimary())) {
            return false;
        }

        if (query.getAlgorithmVersion() != null
                && !query.getAlgorithmVersion().equals(template.getAlgorithmVersion())) {
            return false;
        }

        return true;
    }

    /**
     * 获取员工模板
     *
     * @param employeeId 员工ID
     * @return 模板列表
     */
    public List<BiometricTemplate> getEmployeeTemplates(Long employeeId) {
        List<Long> templateIds = employeeTemplateIndex.get(employeeId);
        if (templateIds == null) {
            return new ArrayList<>();
        }

        return templateIds.stream()
                .map(templateStorage::get)
                .filter(template -> template != null && TemplateStatus.ACTIVE.equals(template.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 获取主模板
     *
     * @param employeeId    员工ID
     * @param biometricType 生物特征类型
     * @return 主模板
     */
    public BiometricTemplate getPrimaryTemplate(Long employeeId, MultimodalAuthEngine.BiometricType biometricType) {
        List<Long> templateIds = employeeTemplateIndex.get(employeeId);
        if (templateIds == null) {
            return null;
        }

        return templateIds.stream()
                .map(templateStorage::get)
                .filter(template -> template != null &&
                        biometricType.equals(template.getBiometricType()) &&
                        TemplateStatus.ACTIVE.equals(template.getStatus()) &&
                        Boolean.TRUE.equals(template.getIsPrimary()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取模板统计信息
     *
     * @return 统计信息
     */
    public TemplateStatistics getStatistics() {
        TemplateStatistics statistics = new TemplateStatistics();

        statistics.setTotalTemplates((long) templateStorage.size());

        statistics.setActiveTemplates(templateStorage.values().stream()
                .filter(template -> TemplateStatus.ACTIVE.equals(template.getStatus()))
                .count());

        // 类型分布
        Map<MultimodalAuthEngine.BiometricType, Long> typeDistribution = new HashMap<>();
        for (MultimodalAuthEngine.BiometricType type : MultimodalAuthEngine.BiometricType.values()) {
            long count = templateStorage.values().stream()
                    .filter(template -> type.equals(template.getBiometricType()))
                    .count();
            if (count > 0) {
                typeDistribution.put(type, count);
            }
        }
        statistics.setTypeDistribution(typeDistribution);

        // 质量分布
        Map<QualityGrade, Long> qualityDistribution = new HashMap<>();
        for (QualityGrade grade : QualityGrade.values()) {
            long count = templateStorage.values().stream()
                    .filter(template -> grade.equals(template.getQualityGrade()))
                    .count();
            if (count > 0) {
                qualityDistribution.put(grade, count);
            }
        }
        statistics.setQualityDistribution(qualityDistribution);

        // 状态分布
        Map<TemplateStatus, Long> statusDistribution = new HashMap<>();
        for (TemplateStatus status : TemplateStatus.values()) {
            long count = templateStorage.values().stream()
                    .filter(template -> status.equals(template.getStatus()))
                    .count();
            if (count > 0) {
                statusDistribution.put(status, count);
            }
        }
        statistics.setStatusDistribution(statusDistribution);

        // 平均使用次数和成功率
        if (!templateStorage.isEmpty()) {
            double avgUsage = templateStorage.values().stream()
                    .mapToInt(BiometricTemplate::getUsageCount)
                    .average()
                    .orElse(0.0);
            statistics.setAverageUsageCount(BigDecimal.valueOf(avgUsage));

            double avgSuccessRate = templateStorage.values().stream()
                    .filter(template -> template.getUsageCount() > 0)
                    .mapToDouble(template -> template.getSuccessRate().doubleValue())
                    .average()
                    .orElse(0.0);
            statistics.setAverageSuccessRate(BigDecimal.valueOf(avgSuccessRate));
        }

        // 最旧和最新模板时间
        statistics.setOldestTemplateTime(templateStorage.values().stream()
                .map(BiometricTemplate::getCreateTime)
                .min(LocalDateTime::compareTo)
                .orElse(null));

        statistics.setNewestTemplateTime(templateStorage.values().stream()
                .map(BiometricTemplate::getCreateTime)
                .max(LocalDateTime::compareTo)
                .orElse(null));

        return statistics;
    }

    /**
     * 清理过期模板
     *
     * @return 清理结果
     */
    public TemplateOperationResult cleanupExpiredTemplates() {
        log.info("开始清理过期模板");

        TemplateOperationResult result = new TemplateOperationResult();
        result.setSuccess(true);
        result.setDetails(new HashMap<>());

        LocalDateTime now = LocalDateTime.now();
        List<Long> expiredTemplateIds = new ArrayList<>();

        // 查找过期模板
        for (BiometricTemplate template : templateStorage.values()) {
            if (template.getExpireTime() != null && template.getExpireTime().isBefore(now)) {
                expiredTemplateIds.add(template.getTemplateId());
            }
        }

        // 删除过期模板
        int deletedCount = 0;
        for (Long templateId : expiredTemplateIds) {
            TemplateOperationResult deleteResult = deleteTemplate(templateId);
            if (deleteResult.getSuccess()) {
                deletedCount++;
            }
        }

        result.setMessage("清理完成");
        result.getDetails().put("expiredCount", expiredTemplateIds.size());
        result.getDetails().put("deletedCount", deletedCount);

        log.info("过期模板清理完成：过期{}个，删除{}个", expiredTemplateIds.size(), deletedCount);
        return result;
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics(String operation, MultimodalAuthEngine.BiometricType biometricType) {
        String key = operation + "_" + biometricType.getCode();
        globalStatistics.put(key, ((Integer) globalStatistics.getOrDefault(key, 0)) + 1);
    }

    /**
     * 获取所有生物特征类型
     *
     * @return 生物特征类型列表
     */
    public List<MultimodalAuthEngine.BiometricType> getAllBiometricTypes() {
        return Arrays.asList(MultimodalAuthEngine.BiometricType.values());
    }

    /**
     * 获取所有质量等级
     *
     * @return 质量等级列表
     */
    public List<QualityGrade> getAllQualityGrades() {
        return Arrays.asList(QualityGrade.values());
    }

    /**
     * 获取所有模板状态
     *
     * @return 模板状态列表
     */
    public List<TemplateStatus> getAllTemplateStatuses() {
        return Arrays.asList(TemplateStatus.values());
    }
}
