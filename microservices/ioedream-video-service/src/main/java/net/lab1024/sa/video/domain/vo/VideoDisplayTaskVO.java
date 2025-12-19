package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 上墙任务视图对象
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
public class VideoDisplayTaskVO {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 电视墙ID
     */
    private Long wallId;

    /**
     * 窗口ID
     */
    private Long windowId;

    /**
     * 视频设备ID
     */
    private Long deviceId;

    /**
     * 解码器ID
     */
    private Long decoderId;

    /**
     * 解码通道号
     */
    private Integer channelNo;

    /**
     * 码流类型：MAIN-主码流，SUB-子码流
     */
    private String streamType;

    /**
     * 任务类型：0-手动，1-预案，2-轮巡，3-告警联动
     */
    private Integer taskType;

    /**
     * 任务类型描述
     */
    private String taskTypeDesc;

    /**
     * 关联预案ID
     */
    private Long presetId;

    /**
     * 关联轮巡ID
     */
    private Long tourId;

    /**
     * 关联告警ID
     */
    private Long alarmId;

    /**
     * 状态：0-等待，1-执行中，2-完成，3-失败
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
