package net.lab1024.sa.common.entity.organization;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 系统区域实体
 * <p>
 * 存储系统区域层级信息
 * 兼容 Smart-Admin 管理后台的 /system/area 接口契约
 * 严格遵循CLAUDE.md全局架构规范和Entity设计标准
 * 字段数量: 11个 (符合≤30字段标准)
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-12-27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("t_area")
@Schema(description = "系统区域实体")
public class SystemAreaEntity extends BaseEntity {

    @TableId(value = "area_id", type = IdType.AUTO)
    @Schema(description = "区域ID")
    private Long areaId;

    @TableField("area_name")
    @Schema(description = "区域名称")
    private String areaName;

    @TableField("area_code")
    @Schema(description = "区域编码")
    private String areaCode;

    @TableField("area_type")
    @Schema(description = "区域类型")
    private String areaType;

    @TableField("parent_id")
    @Schema(description = "父级ID")
    private Long parentId;

    @TableField("level")
    @Schema(description = "层级")
    private Integer level;

    @TableField("sort_order")
    @Schema(description = "排序号")
    private Integer sortOrder;

    @TableField("manager_id")
    @Schema(description = "管理员ID")
    private Long managerId;

    @TableField("capacity")
    @Schema(description = "容量")
    private Integer capacity;

    @TableField("description")
    @Schema(description = "描述")
    private String description;

    @TableField("status")
    @Schema(description = "状态")
    private Integer status;
}
