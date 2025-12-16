package net.lab1024.sa.common.permission.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 权限实体
 * <p>
 * 统一的权限数据模型，支持：
 * - 菜单权限
 * - 按钮权限
 * - API接口权限
 * - 数据权限
 * - 资源权限
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_permission")
public class PermissionEntity extends BaseEntity {

    /**
     * 权限ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列permission_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码
     * <p>
     * 唯一标识权限的编码，格式：MODULE:ACTION:RESOURCE
     * 例如：ACCESS:MANAGE:DEVICE, CONSUME:VIEW:ACCOUNT
     * </p>
     */
    private String permissionCode;

    /**
     * 权限名称
     * <p>
     * 权限的中文名称，用于前端显示和用户理解
     * </p>
     */
    private String permissionName;

    /**
     * 权限描述
     * <p>
     * 权限的详细描述说明
     * </p>
     */
    private String permissionDesc;

    /**
     * 权限类型
     * <p>
     * 1-菜单权限：控制菜单显示和访问
     * 2-按钮权限：控制按钮显示和操作
     * 3-API权限：控制API接口访问
     * 4-数据权限：控制数据访问范围
     * 5-资源权限：控制特定资源访问
     * </p>
     */
    private Integer permissionType;

    /**
     * 所属模块
     * <p>
     * 权限所属的业务模块，如：ACCESS（门禁）、CONSUME（消费）、ATTENDANCE（考勤）等
     * 用于模块级权限管理和统计
     * </p>
     */
    private String moduleCode;

    /**
     * 资源路径
     * <p>
     * 权限对应的资源路径，支持Ant表达式
     * 例如：/api/v1/access/**, /api/v1/consume/account/**
     * </p>
     */
    private String resourcePath;

    /**
     * HTTP方法
     * <p>
     * 权限限制的HTTP方法，多个方法用逗号分隔
     * 例如：GET,POST,PUT,DELETE
     * 空值表示所有HTTP方法
     * </p>
     */
    private String httpMethod;

    /**
     * 父权限ID
     * <p>
     * 父权限的ID，用于权限层级管理
     * 父权限和子权限形成树形结构
     * </p>
     */
    private Long parentId;

    /**
     * 权限层级
     * <p>
     * 权限在权限树中的层级
     * 1-一级权限，2-二级权限，3-三级权限
     * </p>
     */
    private Integer level;

    /**
     * 权限状态
     * <p>
     * 1-启用，2-禁用
     * 禁用的权限不参与权限验证
     * </p>
     */
    private Integer status;

    /**
     * 是否系统权限
     * <p>
     * 0-否（业务权限），1-是（系统权限）
     * 系统权限不可删除，只能禁用
     * </p>
     */
    private Integer isSystem;

    /**
     * 是否核心权限
     * <p>
     * 0-否，1-是
     * 核心权限影响系统基本功能，需要特殊保护
     * </p>
     */
    private Integer isCore;

    /**
     * 排序
     * <p>
     * 权限在同级中的显示顺序
     * 数值越小排序越靠前
     * </p>
     */
    private Integer sortOrder;

    /**
     * 权限图标
     * <p>
     * 权限在UI中显示的图标
     * 支持Font Awesome、Element UI等图标库
     * </p>
     */
    private String icon;

    /**
     * 扩展属性
     * <p>
     * JSON格式的扩展属性，用于存储自定义权限配置
     * 例如：{"maxAccessTimes": 100, "timeRestriction": "09:00-18:00"}
     * </p>
     */
    private String extendedAttributes;

    // 枚造函数
    public PermissionEntity() {
        this.permissionType = 1; // 默认为菜单权限
        this.status = 1; // 默认启用
        this.isSystem = 0; // 默认非系统权限
        this.isCore = 0; // 默认非核心权限
        this.level = 1; // 默认一级权限
        this.sortOrder = 0; // 默认排序为0
    }

    /**
     * 判断是否为菜单权限
     */
    public boolean isMenuPermission() {
        return this.permissionType != null && this.permissionType == 1;
    }

    /**
     * 判断是否为按钮权限
     */
    public boolean isButtonPermission() {
        return this.permissionType != null && this.permissionType == 2;
    }

    /**
     * 判断是否为API权限
     */
    public boolean isApiPermission() {
        return this.permissionType != null && this.permissionType == 3;
    }

    /**
     * 判断是否为数据权限
     */
    public boolean isDataPermission() {
        return this.permissionType != null && this.permissionType == 4;
    }

    /**
     * 判断是否为资源权限
     */
    public boolean isResourcePermission() {
        return this.permissionType != null && this.permissionType == 5;
    }

    /**
     * 判断权限是否启用
     */
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 判断是否为系统权限
     */
    public boolean isSystemPermission() {
        return this.isSystem != null && this.isSystem == 1;
    }

    /**
     * 判断是否为核心权限
     */
    public boolean isCorePermission() {
        return this.isCore != null && this.isCore == 1;
    }
}