package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 上墙任务新增表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class VideoDisplayTaskAddForm {

    /**
     * 电视墙ID
     */
    @NotNull(message = "电视墙ID不能为空")
    private Long wallId;

    /**
     * 窗口ID
     */
    @NotNull(message = "窗口ID不能为空")
    private Long windowId;

    /**
     * 视频设备ID
     */
    @NotNull(message = "视频设备ID不能为空")
    private Long deviceId;

    /**
     * 码流类型：MAIN-主码流，SUB-子码流
     */
    @Size(max = 10, message = "码流类型长度不能超过10个字符")
    private String streamType;

    /**
     * 任务类型：0-手动，1-预案，2-轮巡，3-告警联动
     */
    private Integer taskType;

    /**
     * 关联预案ID（任务类型为1时必填）
     */
    private Long presetId;

    /**
     * 关联轮巡ID（任务类型为2时必填）
     */
    private Long tourId;

    /**
     * 关联告警ID（任务类型为3时必填）
     */
    private Long alarmId;
}
