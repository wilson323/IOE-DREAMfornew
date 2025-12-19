package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 电视墙轮巡实体类
 * <p>
 * 电视墙轮巡配置实体，定义轮巡窗口、设备和轮巡间隔
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_wall_tour")
public class VideoWallTourEntity extends BaseEntity {

    /**
     * 轮巡ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long tourId;

    /**
     * 电视墙ID
     */
    @TableField("wall_id")
    private Long wallId;

    /**
     * 轮巡名称
     */
    @TableField("tour_name")
    private String tourName;

    /**
     * 轮巡窗口ID列表（逗号分隔，如：1,2,3）
     */
    @TableField("window_ids")
    private String windowIds;

    /**
     * 轮巡设备ID列表（JSON数组格式）
     */
    @TableField("device_ids")
    private String deviceIds;

    /**
     * 轮巡间隔（秒）
     */
    @TableField("interval_seconds")
    private Integer intervalSeconds;

    /**
     * 状态：0-停止，1-运行中
     */
    @TableField("status")
    private Integer status;

    /**
     * 当前轮巡索引
     */
    @TableField("current_index")
    private Integer currentIndex;

    /**
     * 创建人ID
     */
    @TableField("create_by")
    private Long createBy;
}
