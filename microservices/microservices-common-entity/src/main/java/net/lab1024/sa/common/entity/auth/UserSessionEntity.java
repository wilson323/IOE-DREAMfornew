package net.lab1024.sa.common.entity.auth;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户会话实体
 * <p>
 * 管理用户登录会话信息，支持多设备登录控制
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 用户登录会话管理
 * - 会话令牌验证
 * - 多设备登录控制
 * - 会话过期管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_session")
@Schema(description = "用户会话实体")
public class UserSessionEntity extends BaseEntity {

    /**
     * 会话ID（主键）
     */
    @TableId(value = "session_id", type = IdType.AUTO)
    @Schema(description = "会话ID")
    private Long sessionId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 登录令牌
     */
    @Schema(description = "登录令牌")
    private String token;

    /**
     * 设备信息
     */
    @Schema(description = "设备信息")
    private String deviceInfo;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 状态（1-有效 0-失效）
     */
    @Schema(description = "状态")
    private Integer status;
}
