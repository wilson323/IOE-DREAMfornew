package net.lab1024.sa.base.module.area.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 区域表单对象
 * 用于新增和修改区域信息的表单验证
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class AreaForm extends PageParam {

    /**
     * 区域ID（修改时必填）
     */
    private Long areaId;

    /**
     * 区域编码（系统唯一）
     */
    @NotBlank(message = "区域编码不能为空")
    @Size(max = 32, message = "区域编码长度不能超过32个字符")
    private String areaCode;

    /**
     * 区域名称
     */
    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100, message = "区域名称长度不能超过100个字符")
    private String areaName;

    /**
     * 区域类型（1:园区 2:建筑 3:楼层 4:房间 5:区域 6:其他）
     */
    @NotNull(message = "区域类型不能为空")
    private Integer areaType;

    /**
     * 上级区域ID（0表示根区域）
     */
    @NotNull(message = "上级区域ID不能为空")
    private Long parentId;

    /**
     * 排序号（同层级排序）
     */
    private Integer sortOrder;

    /**
     * 区域状态（0:停用 1:正常 2:维护中）
     */
    private Integer status;

    /**
     * 经度坐标
     */
    private BigDecimal longitude;

    /**
     * 纬度坐标
     */
    private BigDecimal latitude;

    /**
     * 区域面积（平方米）
     */
    private BigDecimal areaSize;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 区域描述
     */
    @Size(max = 500, message = "区域描述长度不能超过500个字符")
    private String description;

    /**
     * 区域平面图路径
     */
    private String mapImage;

    /**
     * 备注信息
     */
    @Size(max = 1000, message = "备注信息长度不能超过1000个字符")
    private String remark;
}