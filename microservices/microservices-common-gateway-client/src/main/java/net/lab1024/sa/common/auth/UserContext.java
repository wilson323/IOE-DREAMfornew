package net.lab1024.sa.common.auth;

import lombok.Data;

/**
 * 用户上下文信息
 *
 * <p>存储从JWT Token中解析出的用户身份信息</p>
 *
 * @author IOE-DREAM Team
 * @since 1.0.0
 */
@Data
public class UserContext {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户角色（多个角色用逗号分隔）
     */
    private String roles;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * Token过期时间
     */
    private Long expireTime;
}
