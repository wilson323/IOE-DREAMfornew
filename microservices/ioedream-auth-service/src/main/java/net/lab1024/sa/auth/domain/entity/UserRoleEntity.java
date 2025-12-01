package net.lab1024.sa.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Entity
@Table(name = "sys_user_role",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role_id"}),
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_role_id", columnList = "role_id")
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    /**
     * 关联用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    /**
     * 关联角色
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", insertable = false, updatable = false)
    private RoleEntity role;

    /**
     * 授权时间
     */
    @Column(name = "grant_time")
    private LocalDateTime grantTime;

    /**
     * 授权人ID
     */
    @Column(name = "grant_by")
    private Long grantBy;

    /**
     * 过期时间
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 检查关联是否有效
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        // 如果设置了过期时间且已过期，则关联无效
        return expireTime == null || expireTime.isAfter(now);
    }

    /**
     * 检查是否即将过期（7天内）
     */
    public boolean isExpiringSoon() {
        if (expireTime == null) {
            return false;
        }
        LocalDateTime sevenDaysLater = LocalDateTime.now().plusDays(7);
        return expireTime.isBefore(sevenDaysLater);
    }
}