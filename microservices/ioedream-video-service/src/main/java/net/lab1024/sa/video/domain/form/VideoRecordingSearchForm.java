package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 录像搜索表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 支持全文搜索
 * - 支持智能搜索
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Accessors(chain = true)
public class VideoRecordingSearchForm {

    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    @Size(max = 100, message = "搜索关键词长度不能超过100个字符")
    private String keyword;

    /**
     * 搜索范围
     * <p>
     * all - 全部
     * filename - 文件名
     * remark - 备注
     * event - 事件描述
     * device - 设备信息
     * </p>
     */
    private String searchScope = "all";

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 通道ID
     */
    private Long channelId;

    /**
     * 录像类型
     */
    private String recordingType;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 是否重要录像
     */
    private Integer important;

    /**
     * 返回结果数量限制
     */
    private Integer limit = 50;

    /**
     * 是否模糊匹配
     */
    private Boolean fuzzy = true;

    /**
     * 是否高亮显示
     */
    private Boolean highlight = true;
}
