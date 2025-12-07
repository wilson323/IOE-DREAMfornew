package net.lab1024.sa.common.auth.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 用户会话实体
 *
 * 表名：t_user_session
 * 功能：记录用户登录会话信息
 *
 * 企业级特性：
 * - 会话持久化
 * - 并发登录控制
 * - 会话过期管理
 * - 设备信息追踪
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@TableName("t_user_session")
public class UserSessionEntity {

    /**
     * 会话ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 过期时间
     */
    private LocalDateTime expiryTime;

    /**
     * 状态（1-活跃，0-已登出，2-已过期）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 更新人
     */
    private Long updateUser;

    /**
     * 删除标记（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deletedFlag;
}
