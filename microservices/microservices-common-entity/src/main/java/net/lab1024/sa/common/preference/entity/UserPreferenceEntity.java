package net.lab1024.sa.common.preference.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户偏好设置实体
 * <p>
 * 对应数据库表: t_user_preference
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_preference")
public class UserPreferenceEntity extends BaseEntity {

    @TableId(value = "preference_id", type = IdType.AUTO)
    private Long preferenceId;

    @TableField("user_id")
    private Long userId;

    @TableField("preference_category")
    private String preferenceCategory;

    @TableField("preference_key")
    private String preferenceKey;

    @TableField("preference_value")
    private String preferenceValue;

    @TableField("preference_type")
    private String preferenceType;

    @TableField("default_value")
    private String defaultValue;

    @TableField("preference_desc")
    private String preferenceDesc;

    @TableField("is_system")
    private Integer isSystem;

    @TableField("is_visible")
    private Integer isVisible;

    @TableField("is_editable")
    private Integer isEditable;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("validation_rule")
    private String validationRule;

    @TableField("options")
    private String options;

    @TableField("preference_group")
    private String preferenceGroup;

    @TableField("device_type")
    private String deviceType;

    @TableField("last_update_time")
    private LocalDateTime lastUpdateTime;

    @TableField("update_count")
    private Integer updateCount;

    @TableField("preference_weight")
    private Integer preferenceWeight;

    @TableField("status")
    private Integer status;

    @TableField("extended_attributes")
    private String extendedAttributes;
}
