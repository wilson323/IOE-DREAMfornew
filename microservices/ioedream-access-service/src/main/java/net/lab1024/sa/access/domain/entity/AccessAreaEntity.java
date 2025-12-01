package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.util.List;

/**
 * 门禁区域实体
 * <p>
 * 支持无限层级区域结构，用于门禁权限的精细化管理
 * 包含区域基本信息、层级关系、权限配置等字段
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_access_area")
public class AccessAreaEntity extends BaseEntity {

    /**
     * 区域ID（主键）
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
    private Long parentId;

    /**
     * 层级路径（用逗号分隔的ID链，如：0,1,2,3）
     */
    private String path;

    /**
     * 层级深度（根区域为0）
     */
    private Integer level;

    /**
     * 排序号（同层级排序）
     */
    private Integer sortOrder;

    /**
     * 区域描述
     */
    @Size(max = 500, message = "区域描述长度不能超过500个字符")
    private String description;

    /**
     * 所在建筑ID
     */
    private Long buildingId;

    /**
     * 所在楼层ID
     */
    private Long floorId;

    /**
     * 区域面积（平方米）
     */
    private Double area;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 是否启用门禁（0:禁用 1:启用）
     */
    private Integer accessEnabled;

    /**
     * 访问权限级别（数字越大权限要求越高）
     */
    private Integer accessLevel;

    /**
     * 是否需要特殊授权（0:不需要 1:需要）
     */
    private Integer specialAuthRequired;

    /**
     * 有效时间段开始（HH:mm格式）
     */
    private String validTimeStart;

    /**
     * 有效时间段结束（HH:mm格式）
     */
    private String validTimeEnd;

    /**
     * 有效星期（逗号分隔，1-7代表周一到周日）
     */
    private String validWeekdays;

    /**
     * 区域状态（0:停用 1:正常 2:维护中）
     */
    private Integer status;

    /**
     * 经度坐标
     */
    private Double longitude;

    /**
     * 纬度坐标
     */
    private Double latitude;

    /**
     * 区域平面图路径
     */
    private String mapImage;

    /**
     * 备注信息
     */
    @Size(max = 1000, message = "备注信息长度不能超过1000个字符")
    private String remark;

    /**
     * 子区域列表（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private List<AccessAreaEntity> children;

    /**
     * 子区域数量（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private Integer childrenCount;

    /**
     * 是否有子区域（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private Boolean hasChildren;

    /**
     * 父级区域名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 区域类型名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String areaTypeName;
}