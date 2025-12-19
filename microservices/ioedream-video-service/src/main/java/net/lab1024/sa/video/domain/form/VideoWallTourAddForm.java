package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 电视墙轮巡新增表单
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
public class VideoWallTourAddForm {

    /**
     * 电视墙ID
     */
    @NotNull(message = "电视墙ID不能为空")
    private Long wallId;

    /**
     * 轮巡名称
     */
    @NotBlank(message = "轮巡名称不能为空")
    @Size(max = 100, message = "轮巡名称长度不能超过100个字符")
    private String tourName;

    /**
     * 轮巡窗口ID列表（逗号分隔，如：1,2,3）
     */
    @NotBlank(message = "轮巡窗口ID列表不能为空")
    private String windowIds;

    /**
     * 轮巡设备ID列表（JSON数组格式）
     */
    @NotBlank(message = "轮巡设备ID列表不能为空")
    private String deviceIds;

    /**
     * 轮巡间隔（秒）
     */
    @NotNull(message = "轮巡间隔不能为空")
    @Positive(message = "轮巡间隔必须大于0")
    private Integer intervalSeconds;
}
