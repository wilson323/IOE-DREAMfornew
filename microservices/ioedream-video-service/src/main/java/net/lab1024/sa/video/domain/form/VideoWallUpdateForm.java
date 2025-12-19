package net.lab1024.sa.video.domain.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 电视墙更新表单
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
public class VideoWallUpdateForm {

    /**
     * 电视墙ID
     */
    @NotNull(message = "电视墙ID不能为空")
    private Long wallId;

    /**
     * 电视墙名称
     */
    @Size(max = 100, message = "电视墙名称长度不能超过100个字符")
    private String wallName;

    /**
     * 行数
     */
    @Positive(message = "行数必须大于0")
    private Integer rows;

    /**
     * 列数
     */
    @Positive(message = "列数必须大于0")
    private Integer cols;

    /**
     * 单屏宽度（像素）
     */
    @Positive(message = "单屏宽度必须大于0")
    private Integer screenWidth;

    /**
     * 单屏高度（像素）
     */
    @Positive(message = "单屏高度必须大于0")
    private Integer screenHeight;

    /**
     * 安装位置
     */
    @Size(max = 200, message = "安装位置长度不能超过200个字符")
    private String location;

    /**
     * 所属区域ID
     */
    private Long regionId;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 描述
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}
