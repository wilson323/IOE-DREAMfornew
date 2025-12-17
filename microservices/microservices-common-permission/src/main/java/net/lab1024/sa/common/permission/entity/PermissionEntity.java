package net.lab1024.sa.common.permission.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体(权限模块用)
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@TableName("t_common_permission")
public class PermissionEntity {

    @TableId
    private Long id;

    private String permissionCode;

    private String permissionName;

    private Long menuId;

    private Integer status;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
