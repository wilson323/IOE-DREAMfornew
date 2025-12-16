package net.lab1024.sa.common.menu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.menu.entity.MenuEntity;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单数据访问层
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Mapper
@Transactional(readOnly = true)
public interface MenuDao extends BaseMapper<MenuEntity> {

    /**
     * 根据用户ID查询菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Select("""
        SELECT DISTINCT m.*
        FROM t_menu m
        INNER JOIN t_role_menu rm ON m.menu_id = rm.menu_id
        INNER JOIN t_user_role ur ON rm.role_id = ur.role_id
        WHERE ur.user_id = #{userId}
          AND m.deleted_flag = 0
          AND m.is_visible = 1
          AND m.is_disabled = 0
        ORDER BY m.sort_order ASC, m.menu_id ASC
        """)
    List<MenuEntity> selectMenuListByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询菜单列表
     *
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Select("""
        SELECT m.*
        FROM t_menu m
        INNER JOIN t_role_menu rm ON m.menu_id = rm.menu_id
        WHERE rm.role_id = #{roleId}
          AND m.deleted_flag = 0
        ORDER BY m.sort_order ASC, m.menu_id ASC
        """)
    List<MenuEntity> selectMenuListByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询菜单树结构（包含层级信息）
     *
     * @return 菜单树列表
     */
    @Select("""
        SELECT *,
               (SELECT COUNT(*) FROM t_menu WHERE parent_id = m.menu_id AND deleted_flag = 0) as child_count
        FROM t_menu m
        WHERE m.deleted_flag = 0
        ORDER BY m.sort_order ASC, m.menu_id ASC
        """)
    List<MenuEntity> selectMenuTree();

    /**
     * 根据父菜单ID查询子菜单
     *
     * @param parentId 父菜单ID
     * @return 子菜单列表
     */
    @Select("""
        SELECT *
        FROM t_menu
        WHERE parent_id = #{parentId}
          AND deleted_flag = 0
          AND is_visible = 1
          AND is_disabled = 0
        ORDER BY sort_order ASC, menu_id ASC
        """)
    List<MenuEntity> selectChildMenus(@Param("parentId") Long parentId);

    /**
     * 根据菜单编码查询菜单
     *
     * @param menuCode 菜单编码
     * @return 菜单实体
     */
    @Select("""
        SELECT *
        FROM t_menu
        WHERE menu_code = #{menuCode}
          AND deleted_flag = 0
        LIMIT 1
        """)
    MenuEntity selectByMenuCode(@Param("menuCode") String menuCode);

    /**
     * 根据权限标识查询菜单
     *
     * @param permission 权限标识
     * @return 菜单实体
     */
    @Select("""
        SELECT *
        FROM t_menu
        WHERE permission = #{permission}
          AND deleted_flag = 0
        LIMIT 1
        """)
    MenuEntity selectByPermission(@Param("permission") String permission);

    /**
     * 查询最大排序值
     *
     * @param parentId 父菜单ID
     * @return 最大排序值
     */
    @Select("""
        SELECT COALESCE(MAX(sort_order), 0)
        FROM t_menu
        WHERE parent_id = #{parentId}
          AND deleted_flag = 0
        """)
    Integer selectMaxSortOrder(@Param("parentId") Long parentId);

    /**
     * 更新菜单排序
     *
     * @param menuId 菜单ID
     * @param sortOrder 排序值
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Select("""
        UPDATE t_menu
        SET sort_order = #{sortOrder},
            update_time = NOW()
        WHERE menu_id = #{menuId}
          AND deleted_flag = 0
        """)
    int updateSortOrder(@Param("menuId") Long menuId, @Param("sortOrder") Integer sortOrder);

    /**
     * 批量更新菜单等级
     *
     * @param parentId 父菜单ID
     * @param level 等级
     * @return 影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    @Select("""
        UPDATE t_menu
        SET menu_level = #{level},
            update_time = NOW()
        WHERE parent_id = #{parentId}
          AND deleted_flag = 0
        """)
    int updateMenuLevel(@Param("parentId") Long parentId, @Param("level") Integer level);

    /**
     * 统计子菜单数量
     *
     * @param parentId 父菜单ID
     * @return 子菜单数量
     */
    @Select("""
        SELECT COUNT(*)
        FROM t_menu
        WHERE parent_id = #{parentId}
          AND deleted_flag = 0
        """)
    Integer countChildMenus(@Param("parentId") Long parentId);
}
