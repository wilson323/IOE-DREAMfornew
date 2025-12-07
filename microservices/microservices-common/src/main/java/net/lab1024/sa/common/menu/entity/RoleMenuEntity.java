package net.lab1024.sa.common.menu.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 角色菜单关联实体
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 继承BaseEntity获取公共字段
 * - 使用@TableName指定数据库表名
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_role_menu")
@Schema(description = "角色菜单关联实体")
public class RoleMenuEntity extends BaseEntity {

    /**
     * 关联ID
     */
    @TableId(value = "role_menu_id", type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Long roleMenuId;

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不能为空")
    @TableField("role_id")
    @Schema(description = "角色ID")
    private Long roleId;

    /**
     * 菜单ID
     */
    @NotNull(message = "菜单ID不能为空")
    @TableField("menu_id")
    @Schema(description = "菜单ID")
    private Long menuId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
