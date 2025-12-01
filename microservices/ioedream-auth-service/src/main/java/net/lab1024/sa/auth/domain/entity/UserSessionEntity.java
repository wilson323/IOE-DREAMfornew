package net.lab1024.sa.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户会话实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Entity
@Table(name = "sys_user_session", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_refresh_token", columnList = "refresh_token"),
    @Index(name = "idx_expire_time", columnList = "expire_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionEntity extends BaseEntity {

    /**
     * 会话ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    /**
     * 访问令牌
     */
    @Column(name = "token", nullable = false, length = 500)
    private String token;

    /**
     * 刷新令牌
     */
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Column(name = "token_type", length = 20)
    private String tokenType;

    /**
     * 过期时间
     */
    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;

    /**
     * 刷新令牌过期时间
     */
    @Column(name = "refresh_expire_time")
    private LocalDateTime refreshExpireTime;

    /**
     * 登录IP
     */
    @Column(name = "login_ip", length = 50)
    private String loginIp;

    /**
     * 登录地点
     */
    @Column(name = "login_location", length = 200)
    private String loginLocation;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 设备信息
     */
    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    /**
     * 状态：1-有效，0-无效
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 登出时间
     */
    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    /**
     * 最后访问时间
     */
    @Column(name = "last_access_time")
    private LocalDateTime lastAccessTime;

    /**
     * 访问次数
     */
    @Column(name = "access_count")
    private Integer accessCount;

    /**
     * 检查会话是否有效
     */
    public boolean isValid() {
        return status != null && status == 1 &&
               expireTime != null && expireTime.isAfter(LocalDateTime.now());
    }

    /**
     * 检查刷新令牌是否有效
     */
    public boolean isRefreshTokenValid() {
        return refreshExpireTime != null &&
               refreshExpireTime.isAfter(LocalDateTime.now()) &&
               refreshToken != null && !refreshToken.trim().isEmpty();
    }

    /**
     * 使会话失效
     */
    public void invalidate() {
        this.status = 0;
        this.logoutTime = LocalDateTime.now();
    }

    /**
     * 更新最后访问时间
     */
    public void updateLastAccess() {
        this.lastAccessTime = LocalDateTime.now();
        this.accessCount = (this.accessCount == null ? 0 : this.accessCount) + 1;
    }

    /**
     * 检查是否即将过期（30分钟内）
     */
    public boolean isExpiringSoon() {
        return expireTime != null &&
               expireTime.isBefore(LocalDateTime.now().plusMinutes(30));
    }
}