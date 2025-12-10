package net.lab1024.sa.common.i18n.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 国际化资源实体
 * <p>
 * 支持多语言国际化资源管理，针对单企业1000台设备、20000人规模优化
 * 提供完整的多语言翻译资源管理功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_i18n_resource")
public class I18nResourceEntity extends BaseEntity {

    /**
     * 资源ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列resource_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "resource_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 资源键（唯一标识）
     * 格式: module.key (如: user.login.success)
     */
    @TableField("resource_key")
    private String resourceKey;

    /**
     * 语言代码
     * zh_CN, en_US, zh_TW, ja_JP, ko_KR等
     */
    @TableField("language_code")
    private String languageCode;

    /**
     * 国家/地区代码
     * CN, US, TW, JP, KR等
     */
    @TableField("country_code")
    private String countryCode;

    /**
     * 资源值（翻译内容）
     */
    @TableField("resource_value")
    private String resourceValue;

    /**
     * 资源描述
     */
    @TableField("resource_desc")
    private String resourceDesc;

    /**
     * 资源模块
     * common-通用, user-用户, access-门禁, attendance-考勤, consume-消费, visitor-访客, video-视频
     */
    @TableField("resource_module")
    private String resourceModule;

    /**
     * 资源类别
     * label-标签, button-按钮, message-消息, title-标题, tooltip-提示, error-错误, validation-验证, menu-菜单
     */
    @TableField("resource_category")
    private String resourceCategory;

    /**
     * 资源分组
     */
    @TableField("resource_group")
    private String resourceGroup;

    /**
     * 是否为系统资源
     * 0-用户自定义, 1-系统预设
     */
    @TableField("is_system")
    private Integer isSystem;

    /**
     * 是否为默认语言
     * 0-否, 1-是
     */
    @TableField("is_default_language")
    private Integer isDefaultLanguage;

    /**
     * 是否启用
     * 0-禁用, 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 上下文信息
     * 用于翻译时的上下文参考
     */
    @TableField("context_info")
    private String contextInfo;

    /**
     * 资源版本
     * 用于版本控制和资源更新追踪
     */
    @TableField("resource_version")
    private String resourceVersion;

    /**
     * 翻译状态
     * pending-待翻译, translated-已翻译, reviewed-已审核, approved-已批准
     */
    @TableField("translation_status")
    private String translationStatus;

    /**
     * 翻译质量评分 (1-5)
     */
    @TableField("quality_score")
    private Integer qualityScore;

    /**
     * 字符数
     */
    @TableField("character_count")
    private Integer characterCount;

    /**
     * 是否为富文本
     * 0-否, 1-是
     */
    @TableField("is_rich_text")
    private Integer isRichText;

    /**
     * 翻译来源
     * manual-人工翻译, machine-机器翻译, import-导入
     */
    @TableField("translation_source")
    private String translationSource;

    /**
     * 翻译者ID
     */
    @TableField("translator_id")
    private Long translatorId;

    /**
     * 审核者ID
     */
    @TableField("reviewer_id")
    private Long reviewerId;

    /**
     * 最后审核时间
     */
    @TableField("last_review_time")
    private LocalDateTime lastReviewTime;

    /**
     * 标签（JSON数组格式）
     */
    @TableField("tags")
    private String tags;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    /**
     * 使用频率统计
     */
    @TableField("usage_count")
    private Integer usageCount;

    /**
     * 最后使用时间
     */
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    /**
     * 设备类型
     * web-网页端, mobile-移动端, all-通用
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 缓存优先级
     * 1-低, 2-中, 3-高
     */
    @TableField("cache_priority")
    private Integer cachePriority;

    // 审计字段已在BaseEntity中定义
    // create_time, update_time, create_user_id, update_user_id, deleted_flag, version

    /**
     * 判断是否为系统资源
     */
    public boolean isSystemResource() {
        return isSystem != null && isSystem == 1;
    }

    /**
     * 判断是否为默认语言
     */
    public boolean isDefaultLanguage() {
        return isDefaultLanguage != null && isDefaultLanguage == 1;
    }

    /**
     * 判断是否启用
     */
    public boolean isEnabled() {
        return status == null || status == 1;
    }

    /**
     * 判断是否为富文本
     */
    public boolean isRichTextResource() {
        return isRichText != null && isRichText == 1;
    }

    /**
     * 获取完整的语言标识
     */
    public String getFullLanguageCode() {
        if (countryCode != null && !countryCode.isEmpty()) {
            return languageCode + "_" + countryCode;
        }
        return languageCode;
    }

    /**
     * 获取显示用的语言名称
     */
    public String getDisplayName() {
        switch (languageCode) {
            case "zh_CN":
                return "简体中文";
            case "zh_TW":
                return "繁體中文";
            case "en_US":
                return "English";
            case "ja_JP":
                return "日本語";
            case "ko_KR":
                return "한국어";
            default:
                return languageCode;
        }
    }

    /**
     * 增加使用次数
     */
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
        this.lastUsedTime = LocalDateTime.now();
    }

    /**
     * 计算字符数
     */
    public void calculateCharacterCount() {
        if (resourceValue != null) {
            this.characterCount = resourceValue.length();
        }
    }

    /**
     * 验证资源键格式
     */
    public boolean isValidResourceKey() {
        if (resourceKey == null || resourceKey.isEmpty()) {
            return false;
        }
        // 格式: module.key
        return resourceKey.matches("^[a-zA-Z][a-zA-Z0-9_]*\\.[a-zA-Z][a-zA-Z0-9_.]*$");
    }

    /**
     * 获取模块名
     */
    public String getModuleName() {
        if (resourceKey != null && resourceKey.contains(".")) {
            return resourceKey.substring(0, resourceKey.indexOf("."));
        }
        return "unknown";
    }

    /**
     * 获取键名
     */
    public String getKeyName() {
        if (resourceKey != null && resourceKey.contains(".")) {
            return resourceKey.substring(resourceKey.indexOf(".") + 1);
        }
        return resourceKey;
    }

    /**
     * 初始化系统默认值
     */
    public void initializeSystemDefaults() {
        if (this.isSystem == null) {
            this.isSystem = 1;
        }
        if (this.status == null) {
            this.status = 1;
        }
        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
        if (this.characterCount == null && this.resourceValue != null) {
            this.calculateCharacterCount();
        }
        if (this.usageCount == null) {
            this.usageCount = 0;
        }
        if (this.cachePriority == null) {
            this.cachePriority = 2; // 默认中等缓存优先级
        }
        if (this.deviceType == null) {
            this.deviceType = "all";
        }
        if (this.translationStatus == null) {
            this.translationStatus = "approved";
        }
    }
}
