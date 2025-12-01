package net.lab1024.sa.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Entity
@Table(name = "sys_role_permission",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"role_id", "permission_id"})
       },
       indexes = {
           @Index(name = "idx_role_id", columnList = "role_id"),
           @Index(name = "idx_permission_id", columnList = "permission_id")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionEntity extends BaseEntity {

    /**
     * 角色权限ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_permission_id")
    private Long rolePermissionId;

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * 角色
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private RoleEntity role;

    /**
     * 权限ID
     */
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    /**
     * 权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", insertable = false, updatable = false)
    private PermissionEntity permission;

    /**
     * 授权时间
     */
    @Column(name = "grant_time")
    private LocalDateTime grantTime;

    /**
     * 授权人
     */
    @Column(name = "grant_by", length = 50)
    private String grantBy;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 状态：1-有效，0-无效
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 检查关联是否有效
     */
    public boolean isValid() {
        return status != null && status == 1;
    }

    /**
     * 使关联失效
     */
    public void invalidate() {
        this.status = 0;
    }
}