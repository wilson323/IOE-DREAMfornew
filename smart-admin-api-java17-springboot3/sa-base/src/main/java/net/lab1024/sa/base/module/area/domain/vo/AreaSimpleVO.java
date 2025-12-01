package net.lab1024.sa.base.module.area.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 区域简单视图对象
 * 用于区域列表展示等简单场景
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Data
@Schema(description = "区域简单视图对象")
public class AreaSimpleVO {

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "办公区A")
    private String areaName;

    /**
     * 区域编码
     */
    @Schema(description = "区域编码", example = "OFFICE_A")
    private String areaCode;

    /**
     * 区域类型
     */
    @Schema(description = "区域类型", example = "OFFICE")
    private String areaType;

    /**
     * 父级区域ID
     */
    @Schema(description = "父级区域ID", example = "0")
    private Long parentId;

    /**
     * 区域层级
     * 根区域=1，子区域=2，孙区域=3
     */
    @Schema(description = "区域层级", example = "1")
    private Integer areaLevel;

    /**
     * 区域路径
     * 如：办公区A/会议室B
     */
    @Schema(description = "区域路径", example = "/办公区A/会议室B")
    private String areaPath;

    /**
     * 排序序号
     */
    @Schema(description = "排序序号", example = "1")
    private Integer sortOrder;

    /**
     * 启用状态
     * 0-禁用，1-启用
     */
    @Schema(description = "启用状态", example = "1")
    private Integer enableStatus;

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return enableStatus != null && enableStatus == 1;
    }

    /**
     * 是否为根区域
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }

    /**
     * 是否有子区域
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 子区域列表
     */
    private List<AreaSimpleVO> children;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-11-25 10:30:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-11-25 15:30:00")
    private LocalDateTime updateTime;
}