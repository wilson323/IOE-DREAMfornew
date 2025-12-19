package net.lab1024.sa.video.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 电视墙视图对象
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
public class VideoWallVO {

    /**
     * 电视墙ID
     */
    private Long wallId;

    /**
     * 电视墙编码
     */
    private String wallCode;

    /**
     * 电视墙名称
     */
    private String wallName;

    /**
     * 行数
     */
    private Integer rows;

    /**
     * 列数
     */
    private Integer cols;

    /**
     * 总屏幕数
     */
    private Integer totalScreens;

    /**
     * 单屏宽度（像素）
     */
    private Integer screenWidth;

    /**
     * 单屏高度（像素）
     */
    private Integer screenHeight;

    /**
     * 总宽度（像素）
     */
    private Integer totalWidth;

    /**
     * 总高度（像素）
     */
    private Integer totalHeight;

    /**
     * 安装位置
     */
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
     * 状态描述
     */
    private String statusDesc;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
