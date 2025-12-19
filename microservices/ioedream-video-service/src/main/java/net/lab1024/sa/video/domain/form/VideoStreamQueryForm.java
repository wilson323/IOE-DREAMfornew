package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频流查询表单
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
public class VideoStreamQueryForm {

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
     * 关键词（流名称、设备名称）
     */
    private String keyword;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 流类型
     * <p>
     * main - 主流
     * sub - 子流
     * mobile - 移动流
     * </p>
     */
    private String streamType;

    /**
     * 流协议
     * <p>
     * 1 - RTSP
     * 2 - RTMP
     * 3 - HLS
     * 4 - WebRTC
     * 5 - HTTP-FLV
     * </p>
     */
    private Integer protocol;

    /**
     * 流状态
     * <p>
     * 1 - 活跃
     * 2 - 暂停
     * 3 - 停止
     * 4 - 错误
     * </p>
     */
    private Integer streamStatus;

    /**
     * 视频质量
     * <p>
     * high - 高清
     * medium - 标清
     * low - 流畅
     * </p>
     */
    private String quality;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 是否启用音频
     * <p>
     * 0 - 不启用
     * 1 - 启用
     * </p>
     */
    private Integer audioEnabled;

    /**
     * 是否录制中
     * <p>
     * 0 - 未录制
     * 1 - 录制中
     * </p>
     */
    private Integer recording;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 最小观看人数
     */
    private Integer minViewerCount;

    /**
     * 最大观看人数
     */
    private Integer maxViewerCount;

    /**
     * 开始时间（yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向
     * <p>
     * asc - 升序
     * desc - 降序
     * </p>
     */
    private String sortOrder = "desc";
}