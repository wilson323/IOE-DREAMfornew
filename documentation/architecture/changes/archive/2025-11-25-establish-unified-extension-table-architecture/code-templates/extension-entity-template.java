package {package}.domain.entity;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * {base_entity_name}{module_name}扩展实体
 * <p>
 * {module_description}
 * 基于扩展表机制实现{module_name}模块的特定功能扩展
 * 避免在基础实体中添加模块特定字段，保持基础实体的通用性
 *
 * @author {author}
 * @since {create_date}
 */
@Slf4j
@Data
@TableName(value = "t_{base_table}_{module}_ext", autoResultMap = true)
public class {BaseEntityName}{ModuleName}ExtEntity extends BaseEntity {

    /**
     * 扩展ID（主键）
     */
    @TableId("ext_id")
    private Long extId;

    /**
     * 关联{base_entity_comment}ID
     */
    @NotNull(message = "{base_entity_comment}ID不能为空")
    @TableField("{base_table}_id")
    private Long {baseTableId};

    // ==================== 业务特有字段 ====================

    /**
     * {module_name}等级
     */
    @TableField("{module}_level")
    private Integer {module}Level;

    /**
     * {module_name}模式（JSON数组，支持多模式）
     */
    @TableField("{module}_mode")
    private String {module}Mode;

    /**
     * 是否需要特殊处理
     */
    @TableField("special_required")
    private Boolean specialRequired;

    /**
     * 优先级（数字越大优先级越高）
     */
    @TableField("priority")
    private Integer priority;

    /**
     * {module_name}状态
     */
    @TableField("{module}_status")
    private Integer {module}Status;

    // ==================== JSON配置字段（避免字段冗余） ====================

    /**
     * 时间限制配置（JSON格式）
     * 示例：{{"workdays": ["07:00-09:00", "17:00-19:00"], "weekends": ["09:00-21:00"]}}
     */
    @TableField(value = "time_restrictions", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> timeRestrictions;

    /**
     * 位置限制配置（JSON格式）
     * 示例：{{"latitude": 39.9042, "longitude": 116.4074, "radius": 100}}
     */
    @TableField(value = "location_rules", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> locationRules;

    /**
     * 告警配置（JSON格式）
     * 示例：{{"enabled": true, "threshold": 3, "notification": ["email", "sms"]}}
     */
    @TableField(value = "alert_config", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> alertConfig;

    /**
     * 设备联动配置（JSON格式）
     * 示例：{{"primaryDevice": "camera", "validationOrder": 1, "requiredDevices": ["sensor", "reader"]}}
     */
    @TableField(value = "device_linkage_rules", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> deviceLinkageRules;

    /**
     * 扩展配置（JSON格式，用于未来扩展）
     */
    @TableField(value = "extension_config", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extensionConfig;

    // ==================== 非数据库字段 ====================

    /**
     * {base_entity_comment}名称（关联查询填充）
     */
    @TableField(exist = false)
    private String {baseEntityName}Name;

    /**
     * {base_entity_comment}编码（关联查询填充）
     */
    @TableField(exist = false)
    private String {baseEntityCode};

    /**
     * 是否有特殊配置（计算字段）
     */
    @TableField(exist = false)
    private Boolean hasSpecialConfig;

    /**
     * 配置复杂度评分（计算字段，用于性能监控）
     */
    @TableField(exist = false)
    private Integer configComplexity;

    // ==================== 业务方法 ====================

    /**
     * 是否为高级{module_name}
     *
     * @return true 如果等级>=2
     */
    public boolean isHigh{ModuleName}() {
        return this.{module}Level != null && this.{module}Level >= 2;
    }

    /**
     * 是否支持指定模式
     *
     * @param mode 模式名称
     * @return true 如果支持该模式
     */
    public boolean supports{ModuleName}Mode(String mode) {
        if (StringUtils.isBlank(this.{module}Mode)) {
            return false;
        }
        return this.{module}Mode.contains(mode);
    }

    /**
     * 是否需要特殊处理
     *
     * @return true 如果需要特殊处理
     */
    public boolean requiresSpecialHandling() {
        return Boolean.TRUE.equals(this.specialRequired);
    }

    /**
     * 是否有时间限制
     *
     * @return true 如果配置了时间限制
     */
    public boolean hasTimeRestrictions() {
        return this.timeRestrictions != null && !this.timeRestrictions.isEmpty();
    }

    /**
     * 是否有位置限制
     *
     * @return true 如果配置了位置限制
     */
    public boolean hasLocationRules() {
        return this.locationRules != null && !this.locationRules.isEmpty();
    }

    /**
     * 是否启用了告警
     *
     * @return true 如果启用了告警
     */
    public boolean isAlertEnabled() {
        if (this.alertConfig == null || this.alertConfig.isEmpty()) {
            return false;
        }
        Object enabled = this.alertConfig.get("enabled");
        return Boolean.TRUE.equals(enabled);
    }

    /**
     * 是否有设备联动配置
     *
     * @return true 如果配置了设备联动
     */
    public boolean hasDeviceLinkage() {
        return this.deviceLinkageRules != null && !this.deviceLinkageRules.isEmpty();
    }

    /**
     * 获取告警阈值
     *
     * @return 告警阈值，默认为3
     */
    public Integer getAlertThreshold() {
        if (!isAlertEnabled()) {
            return null;
        }
        Object threshold = this.alertConfig.get("threshold");
        if (threshold instanceof Number) {
            return ((Number) threshold).intValue();
        }
        return 3; // 默认阈值
    }

    /**
     * 获取主要联动设备
     *
     * @return 主要联动设备ID，如果未配置返回null
     */
    public String getPrimaryLinkageDevice() {
        if (!hasDeviceLinkage()) {
            return null;
        }
        Object primaryDevice = this.deviceLinkageRules.get("primaryDevice");
        return primaryDevice != null ? primaryDevice.toString() : null;
    }

    /**
     * 设置默认配置（避免配置冗余）
     */
    public void setDefaultConfig() {
        if (this.{module}Level == null) {
            this.{module}Level = 1;
        }
        if (this.specialRequired == null) {
            this.specialRequired = false;
        }
        if (this.priority == null) {
            this.priority = 1;
        }
        if (this.{module}Status == null) {
            this.{module}Status = 1; // 默认启用
        }
    }

    /**
     * 获取配置复杂度评分
     *
     * @return 复杂度评分（0-10）
     */
    public Integer calculateConfigComplexity() {
        int complexity = 0;

        if (hasTimeRestrictions()) complexity += 2;
        if (hasLocationRules()) complexity += 2;
        if (isAlertEnabled()) complexity += 2;
        if (hasDeviceLinkage()) complexity += 3;
        if (extensionConfig != null && !extensionConfig.isEmpty()) complexity += 1;

        return Math.min(complexity, 10);
    }

    // ==================== 静态工具方法 ====================

    /**
     * 创建默认扩展实例
     *
     * @param {baseTableId} {base_entity_comment}ID
     * @return 默认扩展实例
     */
    public static {BaseEntityName}{ModuleName}ExtEntity createDefault(Long {baseTableId}) {
        {BaseEntityName}{ModuleName}ExtEntity entity = new {BaseEntityName}{ModuleName}ExtEntity();
        entity.set{BaseTableId}({baseTableId});
        entity.setDefaultConfig();
        return entity;
    }

    /**
     * 创建时间限制配置
     *
     * @param workdayTimes 工作日时间段
     * @param weekendTimes 周末时间段
     * @return 时间限制配置Map
     */
    public static Map<String, Object> createTimeRestrictions(List<String> workdayTimes, List<String> weekendTimes) {
        Map<String, Object> config = new java.util.HashMap<>();
        if (workdayTimes != null && !workdayTimes.isEmpty()) {
            config.put("workdays", workdayTimes);
        }
        if (weekendTimes != null && !weekendTimes.isEmpty()) {
            config.put("weekends", weekendTimes);
        }
        return config;
    }

    /**
     * 创建位置限制配置
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 半径（米）
     * @return 位置限制配置Map
     */
    public static Map<String, Object> createLocationRules(Double latitude, Double longitude, Integer radius) {
        Map<String, Object> config = new java.util.HashMap<>();
        if (latitude != null) config.put("latitude", latitude);
        if (longitude != null) config.put("longitude", longitude);
        if (radius != null) config.put("radius", radius);
        return config;
    }

    /**
     * 创建告警配置
     *
     * @param enabled 是否启用
     * @param threshold 阈值
     * @param notificationTypes 通知类型
     * @return 告警配置Map
     */
    public static Map<String, Object> createAlertConfig(Boolean enabled, Integer threshold, List<String> notificationTypes) {
        Map<String, Object> config = new java.util.HashMap<>();
        config.put("enabled", enabled != null ? enabled : false);
        if (threshold != null) config.put("threshold", threshold);
        if (notificationTypes != null && !notificationTypes.isEmpty()) {
            config.put("notification", notificationTypes);
        }
        return config;
    }

    // ==================== 枚举定义 ====================

    /**
     * {module_name}等级枚举
     */
    public enum {ModuleName}Level {{
        BASIC(1, "基础"),
        ADVANCED(2, "高级"),
        PREMIUM(3, "高级");

        private final Integer code;
        private final String desc;

        {ModuleName}Level(Integer code, String desc) {{
            this.code = code;
            this.desc = desc;
        }}

        public Integer getCode() {{
            return code;
        }}

        public String getDesc() {{
            return desc;
        }}

        /**
         * 根据编码获取枚举
         */
        public static {ModuleName}Level getByCode(Integer code) {{
            for ({ModuleName}Level level : values()) {{
                if (level.getCode().equals(code)) {{
                    return level;
                }}
            }}
            return BASIC; // 默认返回基础等级
        }}
    }}

    /**
     * {module_name}状态枚举
     */
    public enum {ModuleName}Status {{
        DISABLED(0, "禁用"),
        ENABLED(1, "启用"),
        MAINTENANCE(2, "维护中");

        private final Integer code;
        private final String desc;

        {ModuleName}Status(Integer code, String desc) {{
            this.code = code;
            this.desc = desc;
        }}

        public Integer getCode() {{
            return code;
        }}

        public String getDesc() {{
            return desc;
        }}

        /**
         * 根据编码获取枚举
         */
        public static {ModuleName}Status getByCode(Integer code) {{
            for ({ModuleName}Status status : values()) {{
                if (status.getCode().equals(code)) {{
                    return status;
                }}
            }}
            return DISABLED; // 默认返回禁用状态
        }}
    }}
}