package net.lab1024.sa.base.module.area.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 基础区域实体
 * <p>
 * 各业务模块共用的基础区域信息，包含区域的基本属性和层级关系
 * 支持无限层级区域结构，如园区→建筑→楼层→房间→区域
 * 通过扩展表机制支持不同业务模块的特定需求
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_area")
public class AreaEntity extends BaseEntity {

    /**
     * 区域ID（主键）
     */
    @TableId("area_id")
    private Long areaId;

    /**
     * 区域编码（系统唯一）
     */
    @NotBlank(message = "区域编码不能为空")
    @Size(max = 32, message = "区域编码长度不能超过32个字符")
    @TableField("area_code")
    private String areaCode;

    /**
     * 区域名称
     */
    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100, message = "区域名称长度不能超过100个字符")
    @TableField("area_name")
    private String areaName;

    /**
     * 区域类型（1:园区 2:建筑 3:楼层 4:房间 5:区域 6:其他）
     */
    @NotNull(message = "区域类型不能为空")
    @TableField("area_type")
    private Integer areaType;

    /**
     * 上级区域ID（0表示根区域）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 层级路径（用逗号分隔的ID链，如：0,1,2,3）
     */
    @TableField("path")
    private String path;

    /**
     * 层级深度（根区域为0）
     */
    @TableField("level")
    private Integer level;

    /**
     * 排序号（同层级排序）
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 区域状态（0:停用 1:正常 2:维护中）
     */
    @TableField("status")
    private Integer status;

    /**
     * 经度坐标
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 纬度坐标
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 区域面积（平方米）
     */
    @TableField("area_size")
    private BigDecimal areaSize;

    /**
     * 容纳人数
     */
    @TableField("capacity")
    private Integer capacity;

    /**
     * 区域描述
     */
    @Size(max = 500, message = "区域描述长度不能超过500个字符")
    @TableField("description")
    private String description;

    /**
     * 区域平面图路径
     */
    @TableField("map_image")
    private String mapImage;

    /**
     * 备注信息
     */
    @Size(max = 1000, message = "备注信息长度不能超过1000个字符")
    @TableField("remark")
    private String remark;

    // ==================== 非数据库字段（用于业务逻辑展示） ====================

    /**
     * 子区域列表（非数据库字段，用于树形结构展示）
     */
    @TableField(exist = false)
    private List<AreaEntity> children;

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

    /**
     * 区域类型英文名（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String areaTypeEnglishName;

    /**
     * 包含自身和所有子区域的ID列表（非数据库字段，用于权限判断）
     */
    @TableField(exist = false)
    private List<Long> selfAndAllChildrenIds;

    /**
     * 当前节点及其所有子节点的ID列表（非数据库字段，用于查询优化）
     */
    @TableField(exist = false)
    private String selfAndAllChildrenIdsStr;

    /**
     * 设备数量（非数据库字段，用于统计展示）
     */
    @TableField(exist = false)
    private Integer deviceCount;

    /**
     * 人员数量（非数据库字段，用于统计展示）
     */
    @TableField(exist = false)
    private Integer personCount;

    // ==================== 业务方法 ====================

    /**
     * 是否为根区域
     *
     * @return true 如果是根区域
     */
    public boolean isRoot() {
        return parentId == null || parentId == 0;
    }

    /**
     * 是否有子区域
     *
     * @return true 如果有子区域
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 获取完整路径编码（如：园区A/建筑B/楼层1）
     *
     * @return 完整路径编码
     */
    public String getFullPathCode() {
        if (isRoot()) {
            return areaCode;
        }

        // 如果有父区域信息，则构建完整路径
        if (parentName != null && !parentName.isEmpty()) {
            return parentName + "/" + areaName;
        }

        return areaName;
    }

    /**
     * 是否启用
     *
     * @return true 如果启用
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(status);
    }

    /**
     * 是否在维护中
     *
     * @return true 如果在维护中
     */
    public boolean isMaintenance() {
        return Integer.valueOf(2).equals(status);
    }
}