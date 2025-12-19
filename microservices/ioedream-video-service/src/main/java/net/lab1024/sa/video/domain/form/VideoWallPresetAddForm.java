package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 电视墙预案新增表单
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
public class VideoWallPresetAddForm {

    /**
     * 电视墙ID
     */
    @NotNull(message = "电视墙ID不能为空")
    private Long wallId;

    /**
     * 预案名称
     */
    @NotBlank(message = "预案名称不能为空")
    @Size(max = 100, message = "预案名称长度不能超过100个字符")
    private String presetName;

    /**
     * 预案编码
     */
    @Size(max = 50, message = "预案编码长度不能超过50个字符")
    private String presetCode;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 描述
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    /**
     * 预案配置（JSON格式：窗口-设备映射）
     * 格式：{"window1": {"deviceId": 1001, "streamType": "MAIN"}, ...}
     */
    @NotBlank(message = "预案配置不能为空")
    private String config;

    /**
     * 是否默认预案：0-否，1-是
     */
    private Integer isDefault;
}
