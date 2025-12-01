package net.lab1024.sa.system.menu.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.system.menu.domain.form.MenuAddForm;
import net.lab1024.sa.system.menu.domain.form.MenuUpdateForm;
import net.lab1024.sa.system.menu.domain.vo.MenuVO;

/**
 * 菜单服务接口
 * <p>
 * 提供菜单的CRUD操作和业务逻辑处理
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
public interface MenuService {

    /**
     * 新增菜单
     *
     * @param addForm 新增表单
     * @param userId  操作人ID
     * @return 菜单ID
     */
    ResponseDTO<Long> addMenu(MenuAddForm addForm, Long userId);

    /**
     * 更新菜单
     *
     * @param updateForm 更新表单
     * @param userId     操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> updateMenu(MenuUpdateForm updateForm, Long userId);

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     * @param userId 操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteMenu(Long menuId, Long userId);

    /**
     * 获取菜单详情
     *
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    ResponseDTO<MenuVO> getMenuById(Long menuId);

    /**
     * 获取菜单树
     *
     * @param onlyEnabled 是否只查询启用的菜单
     * @return 菜单树
     */
    ResponseDTO<List<MenuVO>> getMenuTree(Boolean onlyEnabled);

    /**
     * 获取用户菜单
     *
     * @param userId 用户ID
     * @return 用户菜单列表
     */
    ResponseDTO<List<Map<String, Object>>> getUserMenu(Long userId);

    /**
     * 移动菜单
     *
     * @param menuId      菜单ID
     * @param newParentId 新父菜单ID
     * @param userId      操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> moveMenu(Long menuId, Long newParentId, Long userId);

    /**
     * 获取菜单权限
     *
     * @return 菜单权限标识列表
     */
    ResponseDTO<List<String>> getMenuPermissions();

    /**
     * 刷新菜单缓存
     *
     * @param menuId 菜单ID，为空则刷新所有
     * @return 操作结果
     */
    ResponseDTO<Void> refreshMenuCache(Long menuId);

    /**
     * 修改菜单状态
     *
     * @param menuId 菜单ID
     * @param status 状态
     * @param userId 操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> changeMenuStatus(Long menuId, Integer status, Long userId);

    /**
     * 检查菜单名称在父菜单下是否唯一
     *
     * @param parentId  父菜单ID
     * @param menuName  菜单名称
     * @param excludeId 排除的菜单ID
     * @return 是否唯一
     */
    ResponseDTO<Boolean> checkMenuNameUnique(Long parentId, String menuName, Long excludeId);

    /**
     * 检查菜单编码是否唯一
     *
     * @param menuCode  菜单编码
     * @param excludeId 排除的菜单ID
     * @return 是否唯一
     */
    ResponseDTO<Boolean> checkMenuCodeUnique(String menuCode, Long excludeId);

    /**
     * 检查是否存在子菜单
     *
     * @param menuId 菜单ID
     * @return 是否存在子菜单
     */
    ResponseDTO<Boolean> hasChildMenu(Long menuId);

    /**
     * 检查菜单是否被角色使用
     *
     * @param menuId 菜单ID
     * @return 是否被角色使用
     */
    ResponseDTO<Boolean> isMenuUsedByRole(Long menuId);

    /**
     * 获取菜单统计信息
     *
     * @return 菜单统计信息
     */
    ResponseDTO<Map<String, Object>> getMenuStatistics();
}
