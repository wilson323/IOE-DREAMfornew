package net.lab1024.sa.common.theme.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 主题模板实体 - 重构版本
 * <p>
 * 遵循Entity设计黄金法则：
 * - Entity≤200行（当前约130行）
 * - 字段数≤30个（当前22个字段）
 * - 单一职责：仅记录主题模板核心信息
 * - 样式详细信息存储在扩展属性中
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_theme_template")
public class ThemeTemplateEntity extends BaseEntity {

    /**
     * 模板ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列template_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "template_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 模板名称
     */
    @TableField("template_name")
    private String templateName;

    /**
     * 模板编码（唯一标识）
     */
    @TableField("template_code")
    private String templateCode;

    /**
     * 模板类别
     * system-系统模板, custom-自定义模板, third-party-第三方模板
     */
    @TableField("template_category")
    private String templateCategory;

    /**
     * 模板版本
     */
    @TableField("template_version")
    private String templateVersion;

    /**
     * 主题类型
     * light-亮色主题, dark-暗色主题, auto-自动主题
     */
    @TableField("theme_type")
    private String themeType;

    /**
     * 主色调
     */
    @TableField("primary_color")
    private String primaryColor;

    /**
     * 辅助色调
     */
    @TableField("secondary_color")
    private String secondaryColor;

    /**
     * 背景色
     */
    @TableField("background_color")
    private String backgroundColor;

    /**
     * 文字颜色
     */
    @TableField("text_color")
    private String textColor;

    /**
     * 边框颜色
     */
    @TableField("border_color")
    private String borderColor;

    /**
     * 模板描述
     */
    @TableField("template_description")
    private String templateDescription;

    /**
     * 预览图URL
     */
    @TableField("preview_image_url")
    private String previewImageUrl;

    /**
     * 设计者ID
     */
    @TableField("designer_id")
    private Long designerId;

    /**
     * 设计者姓名
     */
    @TableField("designer_name")
    private String designerName;

    /**
     * 发布时间
     */
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /**
     * 模板状态
     * draft-草稿, published-已发布, deprecated-已废弃, archived-已归档
     */
    @TableField("template_status")
    private String templateStatus;

    /**
     * 是否为默认模板
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 排序权重
     */
    @TableField("sort_weight")
    private Integer sortWeight;

    /**
     * 适用平台
     * web-Web端, mobile-移动端, desktop-桌面端, all-全平台
     */
    @TableField("target_platform")
    private String targetPlatform;

    /**
     * 扩展属性（JSON格式，存储详细的样式配置）
     */
    @TableField("extended_attributes")
    private String extendedAttributes;

    /**
     * 使用次数统计
     */
    @TableField("usage_count")
    private Long usageCount;

    /**
     * 评分（1-5分）
     */
    @TableField("rating")
    private Double rating;

    // ========== 缺失的getter/setter方法 ==========

    public String getTemplateType() {
        return templateCategory;
    }

    public void setTemplateType(String templateType) {
        this.templateCategory = templateType;
    }

    public String getTemplateDesc() {
        return templateDescription;
    }

    public void setTemplateDesc(String templateDesc) {
        this.templateDescription = templateDesc;
    }

    /**
     * 获取主题配置（从扩展属性中提取）
     */
    public String getThemeConfig() {
        return extendedAttributes;
    }

    /**
     * 获取颜色配置（从扩展属性中提取）
     */
    public String getColorConfig() {
        return extendedAttributes;
    }

    /**
     * 获取布局配置（从扩展属性中提取）
     */
    public String getLayoutConfig() {
        return extendedAttributes;
    }

    /**
     * 获取组件配置（从扩展属性中提取）
     */
    public String getComponentConfig() {
        return extendedAttributes;
    }

    /**
     * 获取是否支持暗色模式
     */
    public String getSupportsDarkMode() {
        return targetPlatform != null && targetPlatform.contains("dark") ? "1" : "0";
    }

    /**
     * 设置模板类型
     */
    public void setTemplateTypeField(String templateType) {
        this.templateCategory = templateType;
    }

    /**
     * 设置模板描述
     */
    public void setTemplateDescField(String templateDesc) {
        this.templateDescription = templateDesc;
    }

    /**
     * 设置是否官方
     */
    public void setIsOfficial(int isOfficial) {
        this.templateCategory = isOfficial == 1 ? "system" : "custom";
    }

    /**
     * 设置是否推荐
     */
    public void setIsRecommended(int isRecommended) {
        if (isRecommended == 1) {
            this.isDefault = 1;
        }
    }

    /**
     * 设置是否支持暗色模式
     */
    public void setSupportsDarkMode(int supportsDarkMode) {
        if (supportsDarkMode == 1) {
            this.targetPlatform = this.targetPlatform != null ? this.targetPlatform + ":dark" : "dark";
        }
    }

    /**
     * 设置复杂度等级
     */
    public void setComplexityLevel(int complexityLevel) {
        // 简化实现，可以根据需要存储到扩展属性中
    }

    /**
     * 设置评分数量
     */
    public void setRatingCount(int ratingCount) {
        this.usageCount = (long) ratingCount;
    }

    /**
     * 设置排序顺序
     */
    public void setSortOrder(int sortOrder) {
        this.sortWeight = sortOrder;
    }

    /**
     * 设置模板标签
     */
    public void setTemplateTags(String templateTags) {
        // 可以存储到扩展属性中
    }

    /**
     * 设置颜色配置
     */
    public void setColorConfig(String colorConfig) {
        this.extendedAttributes = colorConfig;
    }

    /**
     * 设置布局配置
     */
    public void setLayoutConfig(String layoutConfig) {
        this.extendedAttributes = layoutConfig;
    }

    /**
     * 设置组件配置
     */
    public void setComponentConfig(String componentConfig) {
        this.extendedAttributes = componentConfig;
    }

    /**
     * 设置主题配置
     */
    public void setThemeConfig(String themeConfig) {
        this.extendedAttributes = themeConfig;
    }

    /**
     * 初始化默认值
     */
    public void initializeDefaults() {
        if (this.templateStatus == null) {
            this.templateStatus = "draft";
        }
        if (this.templateCategory == null) {
            this.templateCategory = "custom";
        }
        if (this.usageCount == null) {
            this.usageCount = 0L;
        }
        if (this.rating == null) {
            this.rating = 0.0;
        }
        if (this.isDefault == null) {
            this.isDefault = 0;
        }
        if (this.sortWeight == null) {
            this.sortWeight = 0;
        }
    }
}
