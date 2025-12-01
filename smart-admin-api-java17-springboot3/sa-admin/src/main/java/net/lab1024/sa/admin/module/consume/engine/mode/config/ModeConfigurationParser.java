package net.lab1024.sa.admin.module.consume.engine.mode.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.SmartException;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * 消费模式配置解析器
 * 负责解析JSON格式的消费模式配置
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class ModeConfigurationParser {

    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        // 初始化Jackson ObjectMapper
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    /**
     * 解析消费模式配置
     *
     * @param configJson 配置JSON字符串
     * @return 消费模式配置对象
     */
    public ConsumptionModeConfig parse(String configJson) {
        try {
            if (configJson == null || configJson.trim().isEmpty()) {
                throw new SmartException("配置JSON不能为空");
            }

            ConsumptionModeConfig config = objectMapper.readValue(configJson, ConsumptionModeConfig.class);

            // 设置默认值
            setDefaultValues(config);

            // 验证配置完整性
            validateConfig(config);

            log.info("消费模式配置解析成功: modeCode={}, modeName={}", config.getModeCode(), config.getModeName());
            return config;

        } catch (Exception e) {
            log.error("消费模式配置解析失败: {}", configJson, e);
            throw new SmartException("消费模式配置解析失败: " + e.getMessage());
        }
    }

    /**
     * 将配置对象序列化为JSON字符串
     *
     * @param config 配置对象
     * @return JSON字符串
     */
    public String toJson(ConsumptionModeConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("消费模式配置序列化失败", e);
            throw new SmartException("消费模式配置序列化失败: " + e.getMessage());
        }
    }

    /**
     * 设置默认值
     *
     * @param config 配置对象
     */
    private void setDefaultValues(ConsumptionModeConfig config) {
        if (config.getCreateTime() == null) {
            config.setCreateTime(LocalDateTime.now());
        }
        config.setUpdateTime(LocalDateTime.now());

        if (config.getVersion() == null) {
            config.setVersion(1L);
        }

        // 设置子配置的默认值
        if (config.getTimeRestriction() == null) {
            config.setTimeRestriction(new ConsumptionModeConfig.TimeRestrictionConfig());
        }

        if (config.getAmountRestriction() == null) {
            config.setAmountRestriction(new ConsumptionModeConfig.AmountRestrictionConfig());
        }

        if (config.getFrequencyRestriction() == null) {
            config.setFrequencyRestriction(new ConsumptionModeConfig.FrequencyRestrictionConfig());
        }

        if (config.getSubsidyConfig() == null) {
            config.setSubsidyConfig(new ConsumptionModeConfig.SubsidyConfig());
        }
    }

    /**
     * 验证配置完整性
     *
     * @param config 配置对象
     */
    private void validateConfig(ConsumptionModeConfig config) {
        // 验证基本字段
        if (config.getModeCode() == null || config.getModeCode().trim().isEmpty()) {
            throw new SmartException("消费模式编码不能为空");
        }

        if (config.getModeName() == null || config.getModeName().trim().isEmpty()) {
            throw new SmartException("消费模式名称不能为空");
        }

        // 验证模式编码格式（只允许字母、数字、下划线）
        if (!config.getModeCode().matches("^[A-Z0-9_]+$")) {
            throw new SmartException("消费模式编码格式错误，只允许大写字母、数字和下划线");
        }

        // 验证优先级
        if (config.getPriority() < 0 || config.getPriority() > 1000) {
            throw new SmartException("优先级必须在0-1000之间");
        }

        // 验证超时时间
        if (config.getTimeoutSeconds() <= 0 || config.getTimeoutSeconds() > 300) {
            throw new SmartException("超时时间必须在1-300秒之间");
        }

        // 验证重试次数
        if (config.getMaxRetryCount() < 0 || config.getMaxRetryCount() > 10) {
            throw new SmartException("重试次数必须在0-10之间");
        }

        // 验证子配置
        validateSubConfigs(config);
    }

    /**
     * 验证子配置
     *
     * @param config 配置对象
     */
    private void validateSubConfigs(ConsumptionModeConfig config) {
        // 验证时间限制配置
        ConsumptionModeConfig.TimeRestrictionConfig timeConfig = config.getTimeRestriction();
        if (timeConfig.isEnabled()) {
            validateTimeRestriction(timeConfig);
        }

        // 验证金额限制配置
        ConsumptionModeConfig.AmountRestrictionConfig amountConfig = config.getAmountRestriction();
        if (amountConfig.isEnabled()) {
            validateAmountRestriction(amountConfig);
        }

        // 验证频率限制配置
        ConsumptionModeConfig.FrequencyRestrictionConfig frequencyConfig = config.getFrequencyRestriction();
        if (frequencyConfig.isEnabled()) {
            validateFrequencyRestriction(frequencyConfig);
        }

        // 验证补贴配置
        ConsumptionModeConfig.SubsidyConfig subsidyConfig = config.getSubsidyConfig();
        if (subsidyConfig.isEnabled()) {
            validateSubsidyConfig(subsidyConfig);
        }
    }

    /**
     * 验证时间限制配置
     */
    private void validateTimeRestriction(ConsumptionModeConfig.TimeRestrictionConfig config) {
        if (config.getAllowedTimeRange() != null && !config.getAllowedTimeRange().isEmpty()) {
            // 验证时间范围格式 HH:mm-HH:mm
            if (!config.getAllowedTimeRange().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                throw new SmartException("允许时间范围格式错误，应为HH:mm-HH:mm");
            }
        }
    }

    /**
     * 验证金额限制配置
     */
    private void validateAmountRestriction(ConsumptionModeConfig.AmountRestrictionConfig config) {
        if (config.getMinAmount() != null && config.getMinAmount().doubleValue() < 0) {
            throw new SmartException("最小金额不能为负数");
        }

        if (config.getMaxAmount() != null && config.getMaxAmount().doubleValue() <= 0) {
            throw new SmartException("最大金额必须大于0");
        }

        if (config.getMinAmount() != null && config.getMaxAmount() != null &&
            config.getMinAmount().compareTo(config.getMaxAmount()) > 0) {
            throw new SmartException("最小金额不能大于最大金额");
        }
    }

    /**
     * 验证频率限制配置
     */
    private void validateFrequencyRestriction(ConsumptionModeConfig.FrequencyRestrictionConfig config) {
        if (config.getMaxConsumptionsPerWindow() <= 0) {
            throw new SmartException("时间窗口内最大消费次数必须大于0");
        }

        if (config.getWindowSizeMinutes() <= 0 || config.getWindowSizeMinutes() > 1440) {
            throw new SmartException("时间窗口长度必须在1-1440分钟之间");
        }

        if (config.getMaxConsumptionsPerDay() <= 0) {
            throw new SmartException("每日最大消费次数必须大于0");
        }

        if (config.getCooldownMinutes() < 0) {
            throw new SmartException("冷却时间不能为负数");
        }
    }

    /**
     * 验证补贴配置
     */
    private void validateSubsidyConfig(ConsumptionModeConfig.SubsidyConfig config) {
        if (config.getSubsidyType() == null) {
            throw new SmartException("补贴类型不能为空");
        }

        // 验证补贴类型
        switch (config.getSubsidyType().toUpperCase()) {
            case "FIXED_AMOUNT":
                if (config.getFixedSubsidyAmount() == null || config.getFixedSubsidyAmount().doubleValue() <= 0) {
                    throw new SmartException("固定补贴金额必须大于0");
                }
                break;
            case "PERCENTAGE":
                if (config.getSubsidyPercentage() == null ||
                    config.getSubsidyPercentage().doubleValue() <= 0 ||
                    config.getSubsidyPercentage().doubleValue() > 100) {
                    throw new SmartException("补贴比例必须在0-100之间");
                }
                break;
            case "TIERED":
                if (config.getTieredSubsidyRules() == null || config.getTieredSubsidyRules().isEmpty()) {
                    throw new SmartException("分级补贴规则不能为空");
                }
                break;
            default:
                throw new SmartException("不支持的补贴类型: " + config.getSubsidyType());
        }

        // 验证每日最大补贴金额
        if (config.getDailyMaxSubsidy() != null && config.getDailyMaxSubsidy().doubleValue() <= 0) {
            throw new SmartException("每日最大补贴金额必须大于0");
        }
    }

    /**
     * 创建默认配置模板
     *
     * @param modeCode 模式编码
     * @return 配置模板JSON
     */
    public String createDefaultTemplate(String modeCode) {
        ConsumptionModeConfig config = new ConsumptionModeConfig();
        config.setModeCode(modeCode);
        config.setModeName(getDefaultModeName(modeCode));
        config.setDescription("自动生成的默认配置模板");
        config.setEnabled(true);
        config.setPriority(100);
        config.setMaxRetryCount(3);
        config.setTimeoutSeconds(30);
        config.setSupportsFallback(true);

        return toJson(config);
    }

    /**
     * 获取默认模式名称
     */
    private String getDefaultModeName(String modeCode) {
        switch (modeCode.toUpperCase()) {
            case "STANDARD":
                return "标准消费模式";
            case "FIXED_AMOUNT":
                return "固定金额模式";
            case "SUBSIDY":
                return "补贴消费模式";
            case "PER_COUNT":
                return "按次消费模式";
            default:
                return "自定义消费模式";
        }
    }
}