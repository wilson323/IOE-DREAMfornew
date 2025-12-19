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
 * 上墙任务实体类
 * <p>
 * 上墙任务管理实体，记录视频上墙的执行状态和结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_display_task")
public class VideoDisplayTaskEntity extends BaseEntity {

    /**
     * 任务ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long taskId;

    /**
     * 电视墙ID
     */
    @TableField("wall_id")
    private Long wallId;

    /**
     * 窗口ID
     */
    @TableField("window_id")
    private Long windowId;

    /**
     * 视频设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 解码器ID
     */
    @TableField("decoder_id")
    private Long decoderId;

    /**
     * 解码通道号
     */
    @TableField("channel_no")
    private Integer channelNo;

    /**
     * 码流类型：MAIN-主码流，SUB-子码流
     */
    @TableField("stream_type")
    private String streamType;

    /**
     * 任务类型：0-手动，1-预案，2-轮巡，3-告警联动
     */
    @TableField("task_type")
    private Integer taskType;

    /**
     * 关联预案ID
     */
    @TableField("preset_id")
    private Long presetId;

    /**
     * 关联轮巡ID
     */
    @TableField("tour_id")
    private Long tourId;

    /**
     * 关联告警ID
     */
    @TableField("alarm_id")
    private Long alarmId;

    /**
     * 状态：0-等待，1-执行中，2-完成，3-失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 错误信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 操作人ID
     */
    @TableField("create_by")
    private Long createBy;
}
