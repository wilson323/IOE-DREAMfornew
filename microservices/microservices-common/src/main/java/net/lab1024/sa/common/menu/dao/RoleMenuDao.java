package net.lab1024.sa.common.menu.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.menu.entity.RoleMenuEntity;

/**
 * 角色菜单关联DAO接口
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Mapper注解（禁止@Repository）
 * - 使用Dao后缀（禁止Repository后缀）
 * - 继承BaseMapper提供基础CRUD
 * - 使用MyBatis-Plus（禁止JPA）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface RoleMenuDao extends BaseMapper<RoleMenuEntity> {

    /**
     * 检查菜单是否被角色使用
     * <p>
     * 查询t_role_menu表中是否存在该菜单ID的记录
     * </p>
     *
     * @param menuId 菜单ID
     * @return 是否存在关联记录
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) > 0 FROM t_role_menu WHERE menu_id = #{menuId} AND deleted_flag = 0")
    boolean existsByMenuId(@Param("menuId") Long menuId);

    /**
     * 统计菜单被使用的角色数量
     *
     * @param menuId 菜单ID
     * @return 角色数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(DISTINCT role_id) FROM t_role_menu WHERE menu_id = #{menuId} AND deleted_flag = 0")
    Integer countRolesByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据角色ID查询菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT menu_id FROM t_role_menu WHERE role_id = #{roleId} AND deleted_flag = 0")
    java.util.List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据菜单ID查询角色ID列表
     *
     * @param menuId 菜单ID
     * @return 角色ID列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT role_id FROM t_role_menu WHERE menu_id = #{menuId} AND deleted_flag = 0")
    java.util.List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);
}
