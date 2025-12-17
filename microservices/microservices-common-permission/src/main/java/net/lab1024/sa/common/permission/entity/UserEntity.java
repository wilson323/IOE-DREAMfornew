package net.lab1024.sa.common.permission.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体(权限模块用)
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@TableName("t_common_user")
public class UserEntity {

    @TableId
    private Long id;

    private String username;

    private String realName;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
