package net.lab1024.sa.common.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 用户会话实体
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_session")
public class UserSessionEntity extends BaseEntity {

    @TableId(value = "session_id", type = IdType.AUTO)
    private Long sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("token")
    private String token;

    @TableField("device_info")
    private String deviceInfo;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("status")
    private Integer status;
}

