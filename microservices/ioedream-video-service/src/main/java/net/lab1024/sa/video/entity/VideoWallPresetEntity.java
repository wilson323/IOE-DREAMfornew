package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 电视墙预案实体类
 * <p>
 * 电视墙预案配置实体，保存窗口-设备映射配置
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_wall_preset")
public class VideoWallPresetEntity extends BaseEntity {

    /**
     * 预案ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long presetId;

    /**
     * 电视墙ID
     */
    @TableField("wall_id")
    private Long wallId;

    /**
     * 预案名称
     */
    @TableField("preset_name")
    private String presetName;

    /**
     * 预案编码
     */
    @TableField("preset_code")
    private String presetCode;

    /**
     * 分组ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 预案配置（JSON格式：窗口-设备映射）
     * 格式：{"window1": {"deviceId": 1001, "streamType": "MAIN"}, ...}
     */
    @TableField("config")
    private String config;

    /**
     * 是否默认预案：0-否，1-是
     */
    @TableField("is_default")
    private Integer isDefault;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;
}
