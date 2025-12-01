package {{package}}.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.base.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import java.time.LocalDateTime;

/**
 * {{EntityName}}实体类
 *
 * @author {{author}}
 * @date {{date}}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Table(name = "t_{{tableName}}", indexes = {
    @Index(name = "idx_create_time", columnList = "create_time"),
    @Index(name = "idx_update_time", columnList = "update_time"),
    @Index(name = "idx_deleted_flag", columnList = "deleted_flag")
})
public class {{EntityName}}Entity extends BaseEntity {

    /**
     * 名称
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 排序字段
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 扩展字段1
     */
    @Column(name = "ext1", length = 200)
    private String ext1;

    /**
     * 扩展字段2
     */
    @Column(name = "ext2", length = 200)
    private String ext2;

    // ==================== 标准业务方法 ====================

    /**
     * 获取业务描述
     */
    public String getDescription() {
        if (this.description != null && !this.description.trim().isEmpty()) {
            return this.description;
        }
        return String.format("%s-%s", this.getClass().getSimpleName(), this.getId());
    }

    /**
     * 获取显示文本
     */
    public String getText() {
        return this.name != null ? this.name : "";
    }

    /**
     * 获取权重值
     */
    public Integer getWeight() {
        return this.sortOrder != null ? this.sortOrder : 0;
    }

    /**
     * 验证实体是否有效
     */
    public boolean isValid() {
        return this.getId() != null && this.getId() > 0 && isValidName();
    }

    /**
     * 验证名称是否有效
     */
    public boolean isValidName() {
        return this.name != null && !this.name.trim().isEmpty();
    }

    /**
     * 验证状态是否有效
     */
    public boolean isValidStatus() {
        return this.status != null && (this.status == 0 || this.status == 1);
    }

    /**
     * 获取格式化的创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getFormattedCreateTime() {
        return this.getCreateTime();
    }

    /**
     * 获取格式化的更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getFormattedUpdateTime() {
        return this.getUpdateTime();
    }

    // ==================== 业务状态方法 ====================

    /**
     * 是否启用状态
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(this.status);
    }

    /**
     * 是否禁用状态
     */
    public boolean isDisabled() {
        return Integer.valueOf(0).equals(this.status);
    }

    /**
     * 启用
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 禁用
     */
    public void disable() {
        this.status = 0;
    }

    // ==================== 扩展业务方法 ====================

    /**
     * 获取状态描述
     */
    public String getStatusText() {
        if (this.status == null) {
            return "未知";
        }
        switch (this.status) {
            case 0:
                return "禁用";
            case 1:
                return "启用";
            default:
                return "未知";
        }
    }

    /**
     * 格式化扩展信息
     */
    public String getExtInfo() {
        StringBuilder sb = new StringBuilder();
        if (this.ext1 != null) {
            sb.append("Ext1: ").append(this.ext1);
        }
        if (this.ext2 != null) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Ext2: ").append(this.ext2);
        }
        return sb.toString();
    }
}