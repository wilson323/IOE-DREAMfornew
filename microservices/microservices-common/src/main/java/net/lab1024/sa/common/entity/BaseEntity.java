package net.lab1024.sa.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;

import lombok.Data;

/**
 * 基础实体类
 *
 * @author SmartAdmin Team
 * @date 2025/01/13
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    /**
     * 删除标记（0：未删除，1：已删除）
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deletedFlag;

    /**
     * 版本号（乐观锁）
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    /**
     * 获取主键ID，用于反射获取
     * 子类需要重写此方法或确保有正确的@Id字段
     */
    public Object getId() {
        // 默认实现，子类可重写
        return null;
    }

    // ==================== 兼容性方法 ====================
    // 为了兼容identity-service等使用createUser/updateUser字段的服务

    /**
     * 获取创建人ID（兼容性方法，等同于getCreateUserId）
     *
     * @return 创建人ID
     */
    public Long getCreateUser() {
        return this.createUserId;
    }

    /**
     * 设置创建人ID（兼容性方法，等同于setCreateUserId）
     *
     * @param createUser 创建人ID
     */
    public void setCreateUser(Long createUser) {
        this.createUserId = createUser;
    }

    /**
     * 获取更新人ID（兼容性方法，等同于getUpdateUserId）
     *
     * @return 更新人ID
     */
    public Long getUpdateUser() {
        return this.updateUserId;
    }

    /**
     * 设置更新人ID（兼容性方法，等同于setUpdateUserId）
     *
     * @param updateUser 更新人ID
     */
    public void setUpdateUser(Long updateUser) {
        this.updateUserId = updateUser;
    }
}
