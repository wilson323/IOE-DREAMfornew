package net.lab1024.sa.admin.module.smart.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 门禁区域树VO
 * <p>
 * 用于前端展示区域树形结构
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "门禁区域树VO")
public class AccessAreaTreeVO {

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域编码
     */
    @Schema(description = "区域编码", example = "AREA_001")
    private String areaCode;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "主园区")
    private String areaName;

    /**
     * 区域类型
     */
    @Schema(description = "区域类型", example = "1")
    private Integer areaType;

    /**
     * 区域类型名称
     */
    @Schema(description = "区域类型名称", example = "园区")
    private String areaTypeName;

    /**
     * 上级区域ID
     */
    @Schema(description = "上级区域ID", example = "0")
    private Long parentId;

    /**
     * 层级路径
     */
    @Schema(description = "层级路径", example = "0,1")
    private String path;

    /**
     * 层级深度
     */
    @Schema(description = "层级深度", example = "0")
    private Integer level;

    /**
     * 排序号
     */
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    /**
     * 区域描述
     */
    @Schema(description = "区域描述", example = "主办公园区")
    private String description;

    /**
     * 区域状态
     */
    @Schema(description = "区域状态", example = "1")
    private Integer status;

    /**
     * 区域状态名称
     */
    @Schema(description = "区域状态名称", example = "正常")
    private String statusName;

    /**
     * 是否启用门禁
     */
    @Schema(description = "是否启用门禁", example = "1")
    private Integer accessEnabled;

    /**
     * 访问权限级别
     */
    @Schema(description = "访问权限级别", example = "1")
    private Integer accessLevel;

    /**
     * 容纳人数
     */
    @Schema(description = "容纳人数", example = "1000")
    private Integer capacity;

    /**
     * 区域面积
     */
    @Schema(description = "区域面积", example = "5000.5")
    private Double area;

    /**
     * 经度坐标
     */
    @Schema(description = "经度坐标", example = "116.397128")
    private Double longitude;

    /**
     * 纬度坐标
     */
    @Schema(description = "纬度坐标", example = "39.916527")
    private Double latitude;

    /**
     * 子区域列表
     */
    @Schema(description = "子区域列表")
    private List<AccessAreaTreeVO> children;

    /**
     * 子区域数量
     */
    @Schema(description = "子区域数量", example = "3")
    private Integer childrenCount;

    /**
     * 是否有子区域
     */
    @Schema(description = "是否有子区域", example = "true")
    private Boolean hasChildren;

    /**
     * 是否展开（前端展示状态）
     */
    @Schema(description = "是否展开", example = "true")
    private Boolean expanded;

    /**
     * 是否选中（前端选择状态）
     */
    @Schema(description = "是否选中", example = "false")
    private Boolean selected;

    /**
     * 是否禁用（前端交互状态）
     */
    @Schema(description = "是否禁用", example = "false")
    private Boolean disabled;
}