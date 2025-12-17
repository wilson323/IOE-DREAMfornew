package net.lab1024.sa.common.permission.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体(权限模块用)
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@TableName("t_common_menu")
public class MenuEntity {

    @TableId
    private Long id;

    private Long parentId;

    private String menuName;

    private String menuCode;

    private String webPerms;

    private String path;

    private String component;

    private Integer menuType;

    private Integer sort;

    private Integer status;

    private String icon;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
