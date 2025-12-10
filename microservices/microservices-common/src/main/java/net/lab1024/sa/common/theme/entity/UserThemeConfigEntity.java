package net.lab1024.sa.common.theme.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户主题配置实体
 * <p>
 * 支持用户个性化主题配置，针对单企业1000台设备、20000人规模优化
 * 实现与前端主题系统的完整对接
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_theme_config")
public class UserThemeConfigEntity extends BaseEntity {

    /**
     * 主题配置ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列config_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "config_id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 主题色索引 (0-9 对应前端10种主题色)
     * 默认: 0 (蓝色主题)
     */
    @TableField("color_index")
    private Integer colorIndex;

    /**
     * 主题色值 (十六进制，如 #1677ff)
     * 与color_index二选一，color_index优先
     */
    @TableField("theme_color")
    private String themeColor;

    /**
     * 布局模式
     * side-侧边栏布局, top-顶部布局, mix-混合布局
     * 默认: side
     */
    @TableField("layout_mode")
    private String layoutMode;

    /**
     * 侧边栏主题
     * dark-暗色主题, light-亮色主题
     * 默认: dark
     */
    @TableField("side_menu_theme")
    private String sideMenuTheme;

    /**
     * 是否开启暗黑模式
     * 0-关闭, 1-开启
     * 默认: 0
     */
    @TableField("dark_mode_flag")
    private Integer darkModeFlag;

    /**
     * 侧边栏宽度 (像素)
     * 默认: 200
     */
    @TableField("side_menu_width")
    private Integer sideMenuWidth;

    /**
     * 是否开启页面动效
     * 0-关闭, 1-开启
     * 默认: 1
     */
    @TableField("page_animate_flag")
    private Integer pageAnimateFlag;

    /**
     * 主题配置名称
     * 用户自定义主题名称
     */
    @TableField("theme_name")
    private String themeName;

    /**
     * 是否为默认主题
     * 0-否, 1-是
     * 默认: 0
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 主题状态
     * 0-禁用, 1-启用
     * 默认: 1
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序号
     * 用于多个主题配置的排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 扩展配置 (JSON格式)
     * 存储其他主题相关配置项
     */
    @TableField("extended_config")
    private String extendedConfig;

    /**
     * 最后使用时间
     * 记录用户最后使用该主题的时间
     */
    @TableField("last_used_time")
    private LocalDateTime lastUsedTime;

    /**
     * 设备类型
     * web-网页端, mobile-移动端
     * 用于区分不同设备的主题配置
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 分组标识
     * 用于主题配置分组管理
     */
    @TableField("group_flag")
    private String groupFlag;

    // 审计字段已在BaseEntity中定义
    // create_time, update_time, create_user_id, update_user_id, deleted_flag, version

    /**
     * 获取主题色值
     * 优先使用color_index，其次使用theme_color
     */
    public String getActualThemeColor() {
        if (colorIndex != null && colorIndex >= 0) {
            // 根据color_index返回对应的颜色值
            String[] themeColors = {
                "#1677ff", // 蓝色
                "#f5222d", // 红色
                "#52c41a", // 绿色
                "#faad14", // 黄色
                "#722ed1", // 紫色
                "#13c2c2", // 青色
                "#eb2f96", // 粉色
                "#fa541c", // 橙色
                "#a0d911", // 青绿色
                "#2f54eb"  // 深蓝色
            };
            if (colorIndex < themeColors.length) {
                return themeColors[colorIndex];
            }
        }
        return themeColor; // 返回自定义颜色
    }

    /**
     * 判断是否为暗黑模式
     */
    public boolean isDarkMode() {
        return darkModeFlag != null && darkModeFlag == 1;
    }

    /**
     * 判断是否开启页面动效
     */
    public boolean isPageAnimateEnabled() {
        return pageAnimateFlag == null || pageAnimateFlag == 1;
    }

    /**
     * 判断是否为默认主题
     */
    public boolean isDefaultTheme() {
        return isDefault != null && isDefault == 1;
    }

    /**
     * 判断主题是否启用
     */
    public boolean isThemeEnabled() {
        return status == null || status == 1;
    }
}
