package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电视墙轮巡视图对象
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
public class VideoWallTourVO {

    /**
     * 轮巡ID
     */
    private Long tourId;

    /**
     * 电视墙ID
     */
    private Long wallId;

    /**
     * 轮巡名称
     */
    private String tourName;

    /**
     * 轮巡窗口ID列表（逗号分隔）
     */
    private String windowIds;

    /**
     * 轮巡设备ID列表（JSON数组格式）
     */
    private String deviceIds;

    /**
     * 轮巡间隔（秒）
     */
    private Integer intervalSeconds;

    /**
     * 状态：0-停止，1-运行中
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 当前轮巡索引
     */
    private Integer currentIndex;

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
