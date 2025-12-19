package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 电视墙实体类
 * <p>
 * 电视墙配置管理实体，支持多屏拼接和窗口布局配置
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_wall")
public class VideoWallEntity extends BaseEntity {

    /**
     * 电视墙ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long wallId;

    /**
     * 电视墙编码（唯一）
     */
    @TableField("wall_code")
    private String wallCode;

    /**
     * 电视墙名称
     */
    @TableField("wall_name")
    private String wallName;

    /**
     * 行数
     */
    @TableField("rows")
    private Integer rows;

    /**
     * 列数
     */
    @TableField("cols")
    private Integer cols;

    /**
     * 总屏幕数（rows * cols）
     */
    @TableField("total_screens")
    private Integer totalScreens;

    /**
     * 单屏宽度（像素）
     */
    @TableField("screen_width")
    private Integer screenWidth;

    /**
     * 单屏高度（像素）
     */
    @TableField("screen_height")
    private Integer screenHeight;

    /**
     * 总宽度（像素）
     */
    @TableField("total_width")
    private Integer totalWidth;

    /**
     * 总高度（像素）
     */
    @TableField("total_height")
    private Integer totalHeight;

    /**
     * 安装位置
     */
    @TableField("location")
    private String location;

    /**
     * 所属区域ID
     */
    @TableField("region_id")
    private Long regionId;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 描述
     */
    @TableField("description")
    private String description;
}
