package net.lab1024.sa.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;

import lombok.Data;

/**
 * 基础实体类
 * <p>
 * 所有实体类的基类，提供统一的审计字段和软删除支持
 * 严格遵循CLAUDE.md规范：
 * - 包含完整的审计字段（createTime、updateTime、createUserId、updateUserId）
 * - 支持软删除（deletedFlag）
 * - 支持乐观锁（version）
 * - 使用MyBatis-Plus自动填充机制
 * </p>
 * <p>
 * 企业级特性：
 * - 自动填充创建时间和更新时间
 * - 软删除机制（不物理删除数据）
 * - 乐观锁防止并发更新冲突
 * - 完整的审计追踪能力
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     * <p>
     * 使用MyBatis-Plus自动填充，插入时自动设置当前时间
     * </p>
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>
     * 使用MyBatis-Plus自动填充，插入和更新时自动设置当前时间
     * </p>
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     * <p>
     * 记录数据创建者的用户ID，用于审计追踪
     * </p>
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     * <p>
     * 记录数据最后更新者的用户ID，用于审计追踪
     * </p>
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 删除标记
     * <p>
     * 0-正常（未删除）
     * 1-已删除（软删除）
     * 使用MyBatis-Plus的@TableLogic注解，查询时自动过滤已删除数据
     * </p>
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    /**
     * 版本号（乐观锁）
     * <p>
     * 用于防止并发更新冲突，每次更新时自动递增
     * 使用MyBatis-Plus的@Version注解
     * </p>
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 获取删除标记（兼容方法）
     * <p>
     * 提供getDeleted()方法以兼容Lambda表达式中的方法引用
     * 例如：AuditArchiveEntity::getDeleted
     * </p>
     *
     * @return 删除标记（0-正常 1-已删除）
     */
    public Integer getDeleted() {
        return this.deletedFlag;
    }
}
