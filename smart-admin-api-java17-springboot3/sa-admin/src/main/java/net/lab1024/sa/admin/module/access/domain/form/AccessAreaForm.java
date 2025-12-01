package net.lab1024.sa.admin.module.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 门禁区域表单
 * <p>
 * 用于区域信息的创建和更新
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */

@Schema(description = "门禁区域表单")
@Data
@Accessors(chain = true)
public class AccessAreaForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区域ID（更新时必填）
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域编码
     */
    @NotBlank(message = "区域编码不能为空")
    @Size(max = 32, message = "区域编码长度不能超过32个字符")
    @Schema(description = "区域编码", example = "AREA_001", required = true)
    private String areaCode;

    /**
     * 区域名称
     */
    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100, message = "区域名称长度不能超过100个字符")
    @Schema(description = "区域名称", example = "主园区", required = true)
    private String areaName;

    /**
     * 区域类型
     */
    @NotNull(message = "区域类型不能为空")
    @Schema(description = "区域类型 1:园区 2:建筑 3:楼层 4:房间 5:区域 6:其他", example = "1", required = true)
    private Integer areaType;

    /**
     * 上级区域ID
     */
    @Schema(description = "上级区域ID，0表示根区域", example = "0")
    private Long parentId;

    /**
     * 排序号
     */
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    /**
     * 区域描述
     */
    @Size(max = 500, message = "区域描述长度不能超过500个字符")
    @Schema(description = "区域描述", example = "主办公园区")
    private String description;

    /**
     * 所在建筑ID
     */
    @Schema(description = "所在建筑ID", example = "1")
    private Long buildingId;

    /**
     * 所在楼层ID
     */
    @Schema(description = "所在楼层ID", example = "1")
    private Long floorId;

    /**
     * 区域面积
     */
    @Schema(description = "区域面积（平方米）", example = "5000.5")
    private Double area;

    /**
     * 容纳人数
     */
    @Schema(description = "容纳人数", example = "1000")
    private Integer capacity;

    /**
     * 是否启用门禁
     */
    @Schema(description = "是否启用门禁 0:禁用 1:启用", example = "1")
    private Integer accessEnabled;

    /**
     * 访问权限级别
     */
    @Schema(description = "访问权限级别（数字越大权限要求越高）", example = "1")
    private Integer accessLevel;

    /**
     * 是否需要特殊授权
     */
    @Schema(description = "是否需要特殊授权 0:不需要 1:需要", example = "0")
    private Integer specialAuthRequired;

    /**
     * 有效时间段开始
     */
    @Schema(description = "有效时间段开始（HH:mm格式）", example = "08:00")
    private String validTimeStart;

    /**
     * 有效时间段结束
     */
    @Schema(description = "有效时间段结束（HH:mm格式）", example = "18:00")
    private String validTimeEnd;

    /**
     * 有效星期
     */
    @Schema(description = "有效星期（逗号分隔，1-7代表周一到周日）", example = "1,2,3,4,5")
    private String validWeekdays;

    /**
     * 区域状态
     */
    @Schema(description = "区域状态 0:停用 1:正常 2:维护中", example = "1")
    private Integer status;

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
     * 区域平面图路径
     */
    @Size(max = 500, message = "区域平面图路径长度不能超过500个字符")
    @Schema(description = "区域平面图路径", example = "/upload/area/map_001.jpg")
    private String mapImage;

    /**
     * 备注信息
     */
    @Size(max = 1000, message = "备注信息长度不能超过1000个字符")
    @Schema(description = "备注信息", example = "主要办公区域，需要严格管理")
    private String remark;
}