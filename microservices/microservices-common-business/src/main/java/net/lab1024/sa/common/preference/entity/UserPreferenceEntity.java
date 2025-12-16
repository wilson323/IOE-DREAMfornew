package net.lab1024.sa.common.preference.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 用户偏好设置实体
 * <p>
 * 支持用户个性化偏好配置，针对单企业1000台设备、20000人规模优化
 * 包括界面偏好、操作习惯、通知设置等全面的用户个性化配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
@TableName("t_user_preference")
public class UserPreferenceEntity extends BaseEntity {

    /**
     * 偏好设置ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列preference_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "preference_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 偏好类别
     * interface-界面偏好, behavior-行为偏好, notification-通知偏好, dashboard-仪表盘偏好
     */
    @TableField("preference_category")
    private String preferenceCategory;

    /**
     * 偏好键
     */
    @TableField("preference_key")
    private String preferenceKey;

    /**
     * 偏好值
     */
    @TableField("preference_value")
    private String preferenceValue;

    /**
     * 偏好类型
     * string-字符串, number-数字, boolean-布尔, json-JSON对象
     */
    @TableField("preference_type")
    private String preferenceType;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 偏好描述
     */
    @TableField("preference_desc")
    private String preferenceDesc;

    /**
     * 是否系统偏好
     * 0-用户自定义, 1-系统预设
     */
    @TableField("is_system")
    private Integer isSystem;

    /**
     * 是否可见
     * 0-隐藏, 1-可见
     */
    @TableField("is_visible")
    private Integer isVisible;

    /**
     * 是否可编辑
     * 0-只读, 1-可编辑
     */
    @TableField("is_editable")
    private Integer isEditable;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 验证规则 (JSON格式)
     */
    @TableField("validation_rule")
    private String validationRule;

    /**
     * 选项列表 (JSON格式，用于枚举类型)
     */
    @TableField("options")
    private String options;

    /**
     * 偏好分组
     */
    @TableField("preference_group")
    private String preferenceGroup;

    /**
     * 设备类型
     * web-网页端, mobile-移动端, all-通用
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 最后更新时间
     */
    @TableField("last_update_time")
    private java.time.LocalDateTime lastUpdateTime;

    /**
     * 更新次数
     */
    @TableField("update_count")
    private Integer updateCount;

    /**
     * 偏好权重 (用于推荐算法)
     */
    @TableField("preference_weight")
    private Integer preferenceWeight;

    /**
     * 状态
     * 0-禁用, 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 扩展属性 (JSON格式)
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    // 审计字段已在BaseEntity中定义
    // create_time, update_time, create_user_id, update_user_id, deleted_flag, version

    /**
     * 判断是否为系统偏好
     */
    public boolean isSystemPreference() {
        return isSystem != null && isSystem == 1;
    }

    /**
     * 判断是否可见
     */
    public boolean isVisible() {
        return isVisible == null || isVisible == 1;
    }

    /**
     * 判断是否可编辑
     */
    public boolean isEditable() {
        return isEditable == null || isEditable == 1;
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return status == null || status == 1;
    }

    /**
     * 获取偏好值并转换为指定类型
     */
    @SuppressWarnings({"unchecked", "null"})
    @Nullable
    public <T> T getTypedValue(Class<T> type) {
        if (preferenceValue == null) {
            return null;
        }

        try {
            if (type == String.class) {
                return (T) preferenceValue;
            } else if (type == Integer.class) {
                return (T) Integer.valueOf(preferenceValue);
            } else if (type == Long.class) {
                return (T) Long.valueOf(preferenceValue);
            } else if (type == Double.class) {
                return (T) Double.valueOf(preferenceValue);
            } else if (type == Boolean.class) {
                return (T) Boolean.valueOf(preferenceValue);
            } else {
                // 其他类型，假设JSON格式
                // 这里可以引入JSON解析器
                return (T) preferenceValue;
            }
        } catch (Exception e) {
            // 转换失败，返回默认值或null
            log.debug("[用户偏好实体] 类型转换失败，返回null: type={}, preferenceValue={}, error={}", type != null ? type.getName() : "null", preferenceValue, e.getMessage());
            return null;
        }
    }

    /**
     * 设置偏好值
     */
    public void setTypedValue(Object value) {
        if (value == null) {
            this.preferenceValue = null;
        } else {
            this.preferenceValue = value.toString();
            // 自动推断类型
            if (value instanceof Integer || value instanceof Long || value instanceof Double) {
                this.preferenceType = "number";
            } else if (value instanceof Boolean) {
                this.preferenceType = "boolean";
            } else {
                this.preferenceType = "string";
            }
        }
    }

    /**
     * 验证偏好值是否有效
     */
    public boolean validateValue(String value) {
        if (validationRule == null || validationRule.isEmpty()) {
            return true;
        }

        try {
            // 这里可以引入JSON验证器
            // 简单的验证示例
            if (preferenceType.equals("number")) {
                Double.parseDouble(value);
                return true;
            } else if (preferenceType.equals("boolean")) {
                return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
            } else if (preferenceType.equals("string")) {
                return value.length() <= 1000; // 假设最大长度1000
            }
            return true;
        } catch (Exception e) {
            log.debug("[用户偏好实体] 偏好值验证失败，返回false: value={}, preferenceType={}, error={}", value, preferenceType, e.getMessage());
            return false;
        }
    }

    /**
     * 获取显示值
     * 用于前端显示，可能需要格式化
     */
    public String getDisplayValue() {
        if (preferenceValue == null) {
            return "";
        }

        if ("boolean".equals(preferenceType)) {
            return "true".equalsIgnoreCase(preferenceValue) ? "是" : "否";
        }

        return preferenceValue;
    }

    /**
     * 初始化为系统默认值
     */
    public void initializeWithDefaults() {
        if (this.preferenceKey != null) {
            switch (this.preferenceKey) {
                case "language":
                    this.preferenceValue = "zh_CN";
                    this.preferenceType = "string";
                    this.defaultValue = "zh_CN";
                    break;
                case "timezone":
                    this.preferenceValue = "Asia/Shanghai";
                    this.preferenceType = "string";
                    this.defaultValue = "Asia/Shanghai";
                    break;
                case "dateFormat":
                    this.preferenceValue = "yyyy-MM-dd";
                    this.preferenceType = "string";
                    this.defaultValue = "yyyy-MM-dd";
                    break;
                case "timeFormat":
                    this.preferenceValue = "HH:mm:ss";
                    this.preferenceType = "string";
                    this.defaultValue = "HH:mm:ss";
                    break;
                case "enableNotification":
                    this.preferenceValue = "true";
                    this.preferenceType = "boolean";
                    this.defaultValue = "true";
                    break;
                case "enableEmail":
                    this.preferenceValue = "true";
                    this.preferenceType = "boolean";
                    this.defaultValue = "true";
                    break;
                case "enableSms":
                    this.preferenceValue = "false";
                    this.preferenceType = "boolean";
                    this.defaultValue = "false";
                    break;
                case "dashboardLayout":
                    this.preferenceValue = "{\"columns\":3,\"widgets\":[]}";
                    this.preferenceType = "json";
                    this.defaultValue = "{\"columns\":3,\"widgets\":[]}";
                    break;
                case "autoRefreshInterval":
                    this.preferenceValue = "30";
                    this.preferenceType = "number";
                    this.defaultValue = "30";
                    break;
                case "pageSize":
                    this.preferenceValue = "20";
                    this.preferenceType = "number";
                    this.defaultValue = "20";
                    break;
                default:
                    if (this.defaultValue != null) {
                        this.preferenceValue = this.defaultValue;
                    }
                    break;
            }
        }
    }
}
