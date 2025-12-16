package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 录像查询表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 包含分页参数
 * - 支持多条件查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
public class VideoRecordingQueryForm {

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 录像类型
     * <p>
     * manual - 手动录像
     * scheduled - 定时录像
     * event - 事件录像
     * alarm - 报警录像
     * </p>
     */
    private String recordingType;

    /**
     * 录像质量
     * <p>
     * high - 高清
     * medium - 标清
     * low - 流畅
     * </p>
     */
    private String quality;

    /**
     * 文件格式
     * <p>
     * mp4, avi, flv, mkv
     * </p>
     */
    private String fileFormat;

    /**
     * 是否重要录像
     * <p>
     * 0 - 否
     * 1 - 是
     * </p>
     */
    private Integer important;

    /**
     * 是否有事件标记
     * <p>
     * 0 - 否
     * 1 - 是
     * </p>
     */
    private Integer hasEvent;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 最小文件大小（MB）
     */
    private Long minFileSize;

    /**
     * 最大文件大小（MB）
     */
    private Long maxFileSize;

    /**
     * 最小录制时长（秒）
     */
    private Integer minDuration;

    /**
     * 最大录制时长（秒）
     */
    private Integer maxDuration;

    /**
     * 开始时间（yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 排序字段
     * <p>
     * recordingTime - 录制时间
     * fileSize - 文件大小
     * duration - 录制时长
     * important - 重要性
     * </p>
     */
    private String sortBy = "recordingTime";

    /**
     * 排序方向
     * <p>
     * asc - 升序
     * desc - 降序
     * </p>
     */
    private String sortOrder = "desc";
}