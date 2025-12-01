package net.lab1024.sa.base.module.area.domain.entity;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.alibaba.fastjson2.JSON;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 设备下发策略实体类
 * 定义不同设备类型的人员数据下发规则和配置
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_dispatch_strategy")
public class DeviceDispatchStrategyEntity extends BaseEntity {

    /**
     * 策略ID
     */
    @TableId("strategy_id")
    private Long strategyId;

    /**
     * 策略名称
     */
    @TableField("strategy_name")
    private String strategyName;

    /**
     * 策略编码（系统内唯一）
     */
    @TableField("strategy_code")
    private String strategyCode;

    /**
     * 设备类型
     * ACCESS-门禁, ATTENDANCE-考勤, CONSUME-消费, VIDEO-视频
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 设备细分类别
     */
    @TableField("device_category")
    private String deviceCategory;

    /**
     * 设备厂商
     */
    @TableField("manufacturer")
    private String manufacturer;

    /**
     * 协议类型
     */
    @TableField("protocol_type")
    private String protocolType;

    /**
     * 下发条件配置
     * JSON格式存储，包含人员类型、区域类型、时间段等条件
     */
    @TableField("dispatch_condition")
    private String dispatchCondition;

    /**
     * 下发配置
     * JSON格式存储，包含下发数据格式、协议参数等
     */
    @TableField("dispatch_config")
    private String dispatchConfig;

    /**
     * 优先级（1-10，数字越小优先级越高）
     */
    @TableField("priority_level")
    private Integer priorityLevel;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 重试间隔（秒）
     */
    @TableField("retry_interval")
    private Integer retryInterval;

    /**
     * 超时时间（秒）
     */
    @TableField("timeout_seconds")
    private Integer timeoutSeconds;

    /**
     * 启用标记
     */
    @TableField("enabled_flag")
    private Integer enabledFlag;

    /**
     * 策略描述
     */
    @TableField("description")
    private String description;

    // ==================== 非数据库字段 ====================

    /**
     * 下发条件映射（解析后）
     */
    @TableField(exist = false)
    private Map<String, Object> dispatchConditionMap;

    /**
     * 下发配置映射（解析后）
     */
    @TableField(exist = false)
    private Map<String, Object> dispatchConfigMap;

    /**
     * 设备类型描述
     */
    @TableField(exist = false)
    private String deviceTypeDesc;

    /**
     * 是否启用（布尔值）
     */
    @TableField(exist = false)
    private Boolean enabled;

    // ==================== 业务方法 ====================

    /**
     * 判断策略是否启用
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled) || Integer.valueOf(1).equals(enabledFlag);
    }

    /**
     * 设置启用状态
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.enabledFlag = enabled ? 1 : 0;
    }

    /**
     * 获取设备类型描述
     */
    public String getDeviceTypeDesc() {
        switch (deviceType) {
            case "ACCESS":
                return "门禁";
            case "ATTENDANCE":
                return "考勤";
            case "CONSUME":
                return "消费";
            case "VIDEO":
                return "视频";
            default:
                return deviceType;
        }
    }

    /**
     * 获取优先级描述
     */
    public String getPriorityDesc() {
        if (priorityLevel == null) {
            return "普通";
        }

        if (priorityLevel <= 2) {
            return "极高";
        } else if (priorityLevel <= 4) {
            return "高";
        } else if (priorityLevel <= 6) {
            return "普通";
        } else if (priorityLevel <= 8) {
            return "低";
        } else {
            return "极低";
        }
    }

    /**
     * 解析下发条件映射
     */
    public Map<String, Object> getDispatchConditionMap() {
        if (dispatchConditionMap != null) {
            return dispatchConditionMap;
        }

        try {
            if (dispatchCondition != null && !dispatchCondition.trim().isEmpty()) {
                dispatchConditionMap = JSON.parseObject(dispatchCondition, Map.class);
            } else {
                dispatchConditionMap = Map.of();
            }
        } catch (Exception e) {
            log.error("解析下发条件失败: {}", dispatchCondition, e);
            dispatchConditionMap = Map.of();
        }

        return dispatchConditionMap;
    }

    /**
     * 设置下发条件映射
     */
    public void setDispatchConditionMap(Map<String, Object> conditionMap) {
        this.dispatchConditionMap = conditionMap;
        try {
            if (conditionMap == null || conditionMap.isEmpty()) {
                this.dispatchCondition = null;
            } else {
                this.dispatchCondition = JSON.toJSONString(conditionMap);
            }
        } catch (Exception e) {
            log.error("序列化下发条件失败", e);
            this.dispatchCondition = null;
        }
    }

    /**
     * 解析下发配置映射
     */
    public Map<String, Object> getDispatchConfigMap() {
        if (dispatchConfigMap != null) {
            return dispatchConfigMap;
        }

        try {
            if (dispatchConfig != null && !dispatchConfig.trim().isEmpty()) {
                dispatchConfigMap = JSON.parseObject(dispatchConfig, Map.class);
            } else {
                dispatchConfigMap = Map.of();
            }
        } catch (Exception e) {
            log.error("解析下发配置失败: {}", dispatchConfig, e);
            dispatchConfigMap = Map.of();
        }

        return dispatchConfigMap;
    }

    /**
     * 设置下发配置映射
     */
    public void setDispatchConfigMap(Map<String, Object> configMap) {
        this.dispatchConfigMap = configMap;
        try {
            if (configMap == null || configMap.isEmpty()) {
                this.dispatchConfig = null;
            } else {
                this.dispatchConfig = JSON.toJSONString(configMap);
            }
        } catch (Exception e) {
            log.error("序列化下发配置失败", e);
            this.dispatchConfig = null;
        }
    }

    /**
     * 检查是否匹配人员类型条件
     */
    public boolean matchesPersonType(String personType) {
        Map<String, Object> condition = getDispatchConditionMap();
        Object personTypesObj = condition.get("personTypes");

        if (personTypesObj == null) {
            return true; // 没有条件限制，匹配所有类型
        }

        if (personTypesObj instanceof List) {
            List<String> allowedTypes = (List<String>) personTypesObj;
            return allowedTypes.contains(personType);
        }

        return personType.equals(personTypesObj.toString());
    }

    /**
     * 检查是否匹配区域类型条件
     */
    public boolean matchesAreaType(Integer areaType) {
        Map<String, Object> condition = getDispatchConditionMap();
        Object areaTypesObj = condition.get("areaTypes");

        if (areaTypesObj == null) {
            return true; // 没有条件限制，匹配所有类型
        }

        if (areaTypesObj instanceof List) {
            List<Integer> allowedTypes = (List<Integer>) areaTypesObj;
            return allowedTypes.contains(areaType);
        }

        return areaType.toString().equals(areaTypesObj.toString());
    }

    /**
     * 检查是否匹配关联类型条件
     */
    public boolean matchesRelationType(String relationType) {
        Map<String, Object> condition = getDispatchConditionMap();
        Object relationTypesObj = condition.get("relationTypes");

        if (relationTypesObj == null) {
            return true; // 没有条件限制，匹配所有类型
        }

        if (relationTypesObj instanceof List) {
            List<String> allowedTypes = (List<String>) relationTypesObj;
            return allowedTypes.contains(relationType);
        }

        return relationType.equals(relationTypesObj.toString());
    }

    /**
     * 检查是否匹配设备类别条件
     */
    public boolean matchesDeviceCategory(String deviceCategory) {
        if (this.deviceCategory == null || this.deviceCategory.trim().isEmpty()) {
            return true; // 没有类别限制
        }

        if (deviceCategory == null) {
            return false;
        }

        Map<String, Object> condition = getDispatchConditionMap();
        Object categoriesObj = condition.get("deviceCategories");

        if (categoriesObj == null) {
            return this.deviceCategory.equals(deviceCategory);
        }

        if (categoriesObj instanceof List) {
            List<String> allowedCategories = (List<String>) categoriesObj;
            return allowedCategories.contains(deviceCategory);
        }

        return deviceCategory.equals(categoriesObj.toString());
    }

    /**
     * 检查是否匹配时间限制条件
     */
    public boolean matchesTimeRestriction() {
        Map<String, Object> condition = getDispatchConditionMap();
        Object timeRestrictionObj = condition.get("timeRestriction");

        if (timeRestrictionObj == null) {
            return true; // 没有时间限制
        }

        if (timeRestrictionObj instanceof Map) {
            Map<String, Object> timeRestriction = (Map<String, Object>) timeRestrictionObj;
            Boolean enabled = (Boolean) timeRestriction.get("enabled");

            if (!Boolean.TRUE.equals(enabled)) {
                return true; // 时间限制未启用
            }

            // 这里可以实现更复杂的时间检查逻辑
            // 例如：检查当前时间是否在允许的时段内
            return true; // 暂时简化处理
        }

        return true;
    }

    /**
     * 检查是否匹配优先级条件
     */
    public boolean matchesPriorityLevel(Integer priorityLevel) {
        Map<String, Object> condition = getDispatchConditionMap();
        Object priorityObj = condition.get("priorityLevel");

        if (priorityObj == null) {
            return true; // 没有优先级限制
        }

        if (priorityObj instanceof Integer) {
            Integer requiredPriority = (Integer) priorityObj;
            return priorityLevel != null && priorityLevel <= requiredPriority;
        }

        return true;
    }

    /**
     * 综合匹配判断（根据人员区域关联判断策略是否适用）
     */
    public boolean matches(PersonAreaRelationEntity relation) {
        if (!isEnabled()) {
            return false;
        }

        // 检查人员类型
        if (!matchesPersonType(relation.getPersonType())) {
            return false;
        }

        // 检查关联类型
        if (!matchesRelationType(relation.getRelationType())) {
            return false;
        }

        // 检查优先级
        if (!matchesPriorityLevel(relation.getPriorityLevel())) {
            return false;
        }

        // 检查时间限制
        if (!matchesTimeRestriction()) {
            return false;
        }

        return true;
    }

    /**
     * 获取协议配置
     */
    public Map<String, Object> getProtocolConfig() {
        Map<String, Object> config = getDispatchConfigMap();
        Object protocolConfig = config.get("protocol");

        if (protocolConfig instanceof Map) {
            return (Map<String, Object>) protocolConfig;
        }

        return Map.of();
    }

    /**
     * 获取数据格式配置
     */
    public Map<String, Object> getDataFormatConfig() {
        Map<String, Object> config = getDispatchConfigMap();
        Object dataFormat = config.get("dataFormat");

        if (dataFormat instanceof Map) {
            return (Map<String, Object>) dataFormat;
        }

        return Map.of();
    }

    /**
     * 获取认证配置
     */
    public Map<String, Object> getAuthConfig() {
        Map<String, Object> config = getDispatchConfigMap();
        Object authConfig = config.get("auth");

        if (authConfig instanceof Map) {
            return (Map<String, Object>) authConfig;
        }

        return Map.of();
    }

    /**
     * 获取端点URL
     */
    public String getEndpoint() {
        Map<String, Object> config = getDispatchConfigMap();
        Object endpoint = config.get("endpoint");

        return endpoint != null ? endpoint.toString() : null;
    }

    /**
     * 获取超时时间
     */
    public Integer getTimeout() {
        if (timeoutSeconds != null && timeoutSeconds > 0) {
            return timeoutSeconds;
        }

        Map<String, Object> config = getDispatchConfigMap();
        Object timeout = config.get("timeout");

        if (timeout instanceof Number) {
            return ((Number) timeout).intValue();
        }

        return 30; // 默认30秒
    }

    /**
     * 获取重试配置
     */
    public RetryConfig getRetryConfig() {
        return new RetryConfig(
            retryCount != null ? retryCount : 3,
            retryInterval != null ? retryInterval : 60
        );
    }

    /**
     * 重试配置内部类
     */
    @Data
    public static class RetryConfig {
        private final int maxRetries;
        private final int intervalSeconds;

        public RetryConfig(int maxRetries, int intervalSeconds) {
            this.maxRetries = maxRetries;
            this.intervalSeconds = intervalSeconds;
        }
    }

    @Override
    public String toString() {
        return "DeviceDispatchStrategyEntity{" +
                "strategyId" + strategyId +
                ", strategyName='" + strategyName + '\'' +
                ", strategyCode='" + strategyCode + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", deviceCategory='" + deviceCategory + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", protocolType='" + protocolType + '\'' +
                ", priorityLevel" + priorityLevel +
                ", enabled" + isEnabled() +
                '}';
    }
}