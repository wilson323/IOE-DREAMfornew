package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电视墙预案视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含完整的业务字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Accessors(chain = true)
public class VideoWallPresetVO {

    /**
     * 预案ID
     */
    private Long presetId;

    /**
     * 电视墙ID
     */
    private Long wallId;

    /**
     * 预案名称
     */
    private String presetName;

    /**
     * 预案编码
     */
    private String presetCode;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 描述
     */
    private String description;

    /**
     * 预案配置（JSON格式：窗口-设备映射）
     */
    private String config;

    /**
     * 是否默认预案：0-否，1-是
     */
    private Integer isDefault;

    /**
     * 是否默认预案描述
     */
    private String isDefaultDesc;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
