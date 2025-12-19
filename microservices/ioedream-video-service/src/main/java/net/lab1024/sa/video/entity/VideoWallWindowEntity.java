package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 电视墙窗口实体类
 * <p>
 * 电视墙窗口配置实体，定义窗口位置、大小和绑定的解码通道
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_wall_window")
public class VideoWallWindowEntity extends BaseEntity {

    /**
     * 窗口ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long windowId;

    /**
     * 电视墙ID
     */
    @TableField("wall_id")
    private Long wallId;

    /**
     * 窗口编号（从1开始）
     */
    @TableField("window_no")
    private Integer windowNo;

    /**
     * 窗口名称
     */
    @TableField("window_name")
    private String windowName;

    /**
     * X坐标（像素）
     */
    @TableField("x_pos")
    private Integer xPos;

    /**
     * Y坐标（像素）
     */
    @TableField("y_pos")
    private Integer yPos;

    /**
     * 宽度（像素）
     */
    @TableField("width")
    private Integer width;

    /**
     * 高度（像素）
     */
    @TableField("height")
    private Integer height;

    /**
     * 层级（用于窗口叠加显示）
     */
    @TableField("z_index")
    private Integer zIndex;

    /**
     * 绑定解码器ID
     */
    @TableField("decoder_id")
    private Long decoderId;

    /**
     * 绑定解码通道号
     */
    @TableField("channel_no")
    private Integer channelNo;

    /**
     * 状态：0-空闲，1-播放中
     */
    @TableField("status")
    private Integer status;
}
