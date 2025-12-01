package net.lab1024.sa.system.menu.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.SmartBeanUtil;
import net.lab1024.sa.common.util.SmartStringUtil;
import net.lab1024.sa.system.menu.dao.MenuDao;
import net.lab1024.sa.system.menu.domain.entity.MenuEntity;
import net.lab1024.sa.system.menu.domain.form.MenuAddForm;
import net.lab1024.sa.system.menu.domain.form.MenuUpdateForm;
import net.lab1024.sa.system.menu.domain.vo.MenuVO;
import net.lab1024.sa.system.menu.manager.MenuManager;
import net.lab1024.sa.system.menu.service.MenuService;

/**
 * 菜单服务实现
 * <p>
 * 严格遵循四层架构：Service 和 Manager 和 DAO
 * 严格遵循编码规范：jakarta包名、@Resource注入、完整异常处理
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class MenuServiceImpl implements MenuService {

    @Resource
    private MenuDao menuDao;

    @Resource
    private MenuManager menuManager;

    // 菜单类型常量（使用int类型以支持switch case）
    private static final int MENU_TYPE_DIR = 1; // 目录
    private static final int MENU_TYPE_MENU = 2; // 菜单
    private static final int MENU_TYPE_BUTTON = 3; // 按钮

    // 状态常量
    private static final Integer STATUS_DISABLED = 0; // 禁用
    private static final Integer STATUS_ENABLED = 1; // 启用

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public ResponseDTO<Long> addMenu(MenuAddForm addForm, Long userId) {
        log.info("新增菜单，menuName：{}，parentId：{}，userId：{}", addForm.getMenuName(), addForm.getParentId(), userId);

        try {
            // 参数验证
            if (addForm == null || SmartStringUtil.isEmpty(addForm.getMenuName())) {
                return ResponseDTO.userErrorParam("菜单名称不能为空");
            }

            // 检查父菜单是否存在
            if (!Objects.equals(addForm.getParentId(), 0L)) {
                MenuEntity parentMenu = menuDao.selectById(addForm.getParentId());
                if (parentMenu == null) {
                    return ResponseDTO.userErrorParam("父菜单不存在");
                }
            }

            // 检查菜单名称是否重复
            if (!checkMenuNameUnique(addForm.getParentId(), addForm.getMenuName(), null).getData()) {
                return ResponseDTO.userErrorParam("菜单名称已存在");
            }

            // 检查菜单编码是否重复
            if (SmartStringUtil.isNotEmpty(addForm.getMenuCode()) &&
                    !checkMenuCodeUnique(addForm.getMenuCode(), null).getData()) {
                return ResponseDTO.userErrorParam("菜单编码已存在");
            }

            // 构建实体
            MenuEntity entity = new MenuEntity();
            SmartBeanUtil.copyProperties(addForm, entity);
            entity.setCreateUserId(userId);

            // 设置默认值
            if (entity.getStatus() == null) {
                entity.setStatus(STATUS_ENABLED);
            }
            if (entity.getVisible() == null) {
                entity.setVisible(1);
            }
            if (entity.getIsCache() == null) {
                entity.setIsCache(0);
            }
            if (entity.getIsFrame() == null) {
                entity.setIsFrame(0);
            }
            if (entity.getSortOrder() == null) {
                entity.setSortOrder(menuManager.getNextSortOrder(addForm.getParentId()));
            }

            // 菜单类型验证
            if (Objects.equals(entity.getMenuType(), MENU_TYPE_BUTTON)) {
                // 按钮类型的菜单不需要路径和组件
                entity.setPath(null);
                entity.setComponent(null);
            }

            // 保存数据
            menuDao.insert(entity);

            log.info("菜单新增成功，menuId：{}，menuName：{}", entity.getMenuId(), addForm.getMenuName());
            return ResponseDTO.ok(entity.getMenuId());

        } catch (Exception e) {
            log.error("新增菜单失败，menuName：{}，错误：{}", addForm.getMenuName(), e.getMessage(), e);
            throw new BusinessException("新增菜单失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public ResponseDTO<Void> updateMenu(MenuUpdateForm updateForm, Long userId) {
        log.info("更新菜单，menuId：{}，userId：{}", updateForm.getMenuId(), userId);

        try {
            if (updateForm == null || updateForm.getMenuId() == null) {
                return ResponseDTO.userErrorParam("菜单ID不能为空");
            }

            // 检查菜单是否存在
            MenuEntity existingEntity = menuDao.selectById(updateForm.getMenuId());
            if (existingEntity == null) {
                return ResponseDTO.userErrorParam("菜单不存在");
            }

            // 不能将菜单设置为自己的子菜单
            if (Objects.equals(updateForm.getParentId(), updateForm.getMenuId())) {
                return ResponseDTO.userErrorParam("父菜单不能是自己");
            }

            // 检查父菜单是否存在
            if (!Objects.equals(updateForm.getParentId(), 0L)) {
                MenuEntity parentMenu = menuDao.selectById(updateForm.getParentId());
                if (parentMenu == null) {
                    return ResponseDTO.userErrorParam("父菜单不存在");
                }

                // 检查是否会形成循环引用
                if (menuManager.isChildMenu(updateForm.getParentId(), updateForm.getMenuId())) {
                    return ResponseDTO.userErrorParam("不能将菜单移动到自己的子菜单下");
                }
            }

            // 检查菜单名称是否重复
            if (!Objects.equals(existingEntity.getMenuName(), updateForm.getMenuName()) &&
                    !checkMenuNameUnique(updateForm.getParentId(), updateForm.getMenuName(), updateForm.getMenuId())
                            .getData()) {
                return ResponseDTO.userErrorParam("菜单名称已存在");
            }

            // 检查菜单编码是否重复
            if (!Objects.equals(existingEntity.getMenuCode(), updateForm.getMenuCode()) &&
                    SmartStringUtil.isNotEmpty(updateForm.getMenuCode()) &&
                    !checkMenuCodeUnique(updateForm.getMenuCode(), updateForm.getMenuId()).getData()) {
                return ResponseDTO.userErrorParam("菜单编码已存在");
            }

            // 更新数据
            MenuEntity entity = new MenuEntity();
            SmartBeanUtil.copyProperties(updateForm, entity);
            entity.setUpdateUserId(userId);

            menuDao.updateById(entity);

            log.info("菜单更新成功，menuId：{}", updateForm.getMenuId());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("更新菜单失败，menuId：{}，错误：{}", updateForm.getMenuId(), e.getMessage(), e);
            throw new BusinessException("更新菜单失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public ResponseDTO<Void> deleteMenu(Long menuId, Long userId) {
        log.info("删除菜单，menuId：{}，userId：{}", menuId, userId);

        try {
            if (menuId == null) {
                return ResponseDTO.userErrorParam("菜单ID不能为空");
            }

            // 检查菜单是否存在
            MenuEntity entity = menuDao.selectById(menuId);
            if (entity == null) {
                return ResponseDTO.userErrorParam("菜单不存在");
            }

            // 检查是否存在子菜单
            if (hasChildMenu(menuId).getData()) {
                return ResponseDTO.userErrorParam("存在子菜单，不允许删除");
            }

            // 检查菜单是否被角色使用
            if (isMenuUsedByRole(menuId).getData()) {
                return ResponseDTO.userErrorParam("菜单被角色使用，不允许删除");
            }

            // 逻辑删除
            entity.setDeletedFlag(1);
            entity.setUpdateUserId(userId);
            menuDao.updateById(entity);

            log.info("菜单删除成功，menuId：{}", menuId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("删除菜单失败，menuId：{}，错误：{}", menuId, e.getMessage(), e);
            throw new BusinessException("删除菜单失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "menu", key = "#menuId", unless = "#result == null")
    public ResponseDTO<MenuVO> getMenuById(Long menuId) {
        log.debug("获取菜单详情，menuId：{}", menuId);

        try {
            if (menuId == null) {
                return ResponseDTO.userErrorParam("菜单ID不能为空");
            }

            MenuEntity entity = menuDao.selectById(menuId);
            if (entity == null || Objects.equals(entity.getDeletedFlag(), 1)) {
                return ResponseDTO.userErrorParam("菜单不存在");
            }

            MenuVO vo = new MenuVO();
            SmartBeanUtil.copyProperties(entity, vo);

            // 设置父菜单名称
            if (!Objects.equals(vo.getParentId(), 0L)) {
                MenuEntity parentMenu = menuDao.selectById(vo.getParentId());
                if (parentMenu != null) {
                    vo.setParentName(parentMenu.getMenuName());
                }
            } else {
                vo.setParentName("根菜单");
            }

            // 设置菜单类型名称
            vo.setMenuTypeName(getMenuTypeName(vo.getMenuType()));

            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("获取菜单详情失败，menuId：{}，错误：{}", menuId, e.getMessage(), e);
            throw new BusinessException("获取菜单详情失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "menuTree", key = "#onlyEnabled", unless = "#result == null")
    public ResponseDTO<List<MenuVO>> getMenuTree(Boolean onlyEnabled) {
        log.debug("获取菜单树，onlyEnabled：{}", onlyEnabled);

        try {
            // 查询所有菜单
            LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuEntity::getDeletedFlag, 0);
            wrapper.orderByAsc(MenuEntity::getSortOrder, MenuEntity::getMenuId);

            if (Boolean.TRUE.equals(onlyEnabled)) {
                wrapper.eq(MenuEntity::getStatus, STATUS_ENABLED);
            }

            List<MenuEntity> entityList = menuDao.selectList(wrapper);
            List<MenuVO> voList = SmartBeanUtil.copyList(entityList, MenuVO.class);

            // 设置父菜单名称和菜单类型名称
            for (MenuVO vo : voList) {
                // 设置父菜单名称
                if (!Objects.equals(vo.getParentId(), 0L)) {
                    MenuEntity parentMenu = entityList.stream()
                            .filter(e -> Objects.equals(e.getMenuId(), vo.getParentId()))
                            .findFirst()
                            .orElse(null);
                    if (parentMenu != null) {
                        vo.setParentName(parentMenu.getMenuName());
                    }
                } else {
                    vo.setParentName("根菜单");
                }

                // 设置菜单类型名称
                vo.setMenuTypeName(getMenuTypeName(vo.getMenuType()));
            }

            // 构建树形结构
            List<MenuVO> menuTree = buildMenuTree(voList, 0L);

            return ResponseDTO.ok(menuTree);

        } catch (Exception e) {
            log.error("获取菜单树失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("获取菜单树失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getUserMenu(Long userId) {
        log.debug("获取用户菜单，userId：{}", userId);

        try {
            // 这里需要根据用户权限查询菜单
            // 简化实现，查询所有启用的菜单
            ResponseDTO<List<MenuVO>> menuTreeResult = getMenuTree(true);
            if (!menuTreeResult.getOk()) {
                return ResponseDTO.error("查询菜单失败");
            }

            List<Map<String, Object>> userMenu = new ArrayList<>();
            for (MenuVO menu : menuTreeResult.getData()) {
                Map<String, Object> menuMap = convertMenuToMap(menu);
                userMenu.add(menuMap);
            }

            return ResponseDTO.ok(userMenu);

        } catch (Exception e) {
            log.error("获取用户菜单失败，userId：{}，错误：{}", userId, e.getMessage(), e);
            throw new BusinessException("获取用户菜单失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public ResponseDTO<Void> moveMenu(Long menuId, Long newParentId, Long userId) {
        log.info("移动菜单，menuId：{}，newParentId：{}，userId：{}", menuId, newParentId, userId);

        try {
            if (menuId == null || newParentId == null) {
                return ResponseDTO.userErrorParam("参数不能为空");
            }

            // 检查菜单是否存在
            MenuEntity entity = menuDao.selectById(menuId);
            if (entity == null) {
                return ResponseDTO.userErrorParam("菜单不存在");
            }

            // 不能将菜单移动到自己或自己的子菜单下
            if (Objects.equals(menuId, newParentId) ||
                    menuManager.isChildMenu(newParentId, menuId)) {
                return ResponseDTO.userErrorParam("不能移动到该父菜单下");
            }

            // 检查新父菜单是否存在
            if (!Objects.equals(newParentId, 0L)) {
                MenuEntity parentMenu = menuDao.selectById(newParentId);
                if (parentMenu == null) {
                    return ResponseDTO.userErrorParam("父菜单不存在");
                }
            }

            // 更新父菜单ID
            entity.setParentId(newParentId);
            entity.setUpdateUserId(userId);
            menuDao.updateById(entity);

            log.info("菜单移动成功，menuId：{}，newParentId：{}", menuId, newParentId);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("移动菜单失败，menuId：{}，newParentId：{}，错误：{}", menuId, newParentId, e.getMessage(), e);
            throw new BusinessException("移动菜单失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "menuPermissions", unless = "#result == null")
    public ResponseDTO<List<String>> getMenuPermissions() {
        log.debug("获取菜单权限");

        try {
            LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuEntity::getDeletedFlag, 0);
            wrapper.eq(MenuEntity::getStatus, STATUS_ENABLED);
            wrapper.isNotNull(MenuEntity::getPermission);
            wrapper.ne(MenuEntity::getPermission, "");

            List<MenuEntity> menuList = menuDao.selectList(wrapper);
            List<String> permissions = menuList.stream()
                    .map(MenuEntity::getPermission)
                    .filter(Objects::nonNull)
                    .filter(permission -> !permission.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            return ResponseDTO.ok(permissions);

        } catch (Exception e) {
            log.error("获取菜单权限失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("获取菜单权限失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public ResponseDTO<Void> refreshMenuCache(Long menuId) {
        log.info("刷新菜单缓存，menuId：{}", menuId);

        try {
            // 这里可以添加具体的缓存刷新逻辑
            // 由于使用了注解缓存，这个方法主要用于手动触发缓存刷新

            if (menuId != null) {
                MenuEntity entity = menuDao.selectById(menuId);
                if (entity != null) {
                    log.info("刷新菜单缓存成功，menuName：{}", entity.getMenuName());
                }
            } else {
                log.info("刷新所有菜单缓存成功");
            }

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("刷新菜单缓存失败，menuId：{}，错误：{}", menuId, e.getMessage(), e);
            throw new BusinessException("刷新菜单缓存失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "menu", allEntries = true)
    public ResponseDTO<Void> changeMenuStatus(Long menuId, Integer status, Long userId) {
        log.info("修改菜单状态，menuId：{}，status：{}，userId：{}", menuId, status, userId);

        try {
            if (menuId == null || status == null) {
                return ResponseDTO.userErrorParam("参数不能为空");
            }

            MenuEntity entity = menuDao.selectById(menuId);
            if (entity == null) {
                return ResponseDTO.userErrorParam("菜单不存在");
            }

            entity.setStatus(status);
            entity.setUpdateUserId(userId);
            menuDao.updateById(entity);

            log.info("菜单状态修改成功，menuId：{}，status：{}", menuId, status);
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("修改菜单状态失败，menuId：{}，status：{}，错误：{}", menuId, status, e.getMessage(), e);
            throw new BusinessException("修改菜单状态失败：" + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "menuNameUnique", key = "#parentId + '_' + #menuName + '_' + #excludeId", unless = "#result == null")
    public ResponseDTO<Boolean> checkMenuNameUnique(Long parentId, String menuName, Long excludeId) {
        log.debug("检查菜单名称唯一性，parentId：{}，menuName：{}，excludeId：{}", parentId, menuName, excludeId);

        try {
            if (parentId == null || SmartStringUtil.isEmpty(menuName)) {
                return ResponseDTO.ok(false);
            }

            LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuEntity::getParentId, parentId);
            wrapper.eq(MenuEntity::getMenuName, menuName);
            wrapper.eq(MenuEntity::getDeletedFlag, 0);

            if (excludeId != null) {
                wrapper.ne(MenuEntity::getMenuId, excludeId);
            }

            long count = menuDao.selectCount(wrapper);
            return ResponseDTO.ok(count == 0);

        } catch (Exception e) {
            log.error("检查菜单名称唯一性失败，parentId：{}，menuName：{}，错误：{}", parentId, menuName, e.getMessage(), e);
            return ResponseDTO.ok(false);
        }
    }

    @Override
    @Cacheable(value = "menuCodeUnique", key = "#menuCode + '_' + #excludeId", unless = "#result == null")
    public ResponseDTO<Boolean> checkMenuCodeUnique(String menuCode, Long excludeId) {
        log.debug("检查菜单编码唯一性，menuCode：{}，excludeId：{}", menuCode, excludeId);

        try {
            if (SmartStringUtil.isEmpty(menuCode)) {
                return ResponseDTO.ok(true);
            }

            LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuEntity::getMenuCode, menuCode);
            wrapper.eq(MenuEntity::getDeletedFlag, 0);

            if (excludeId != null) {
                wrapper.ne(MenuEntity::getMenuId, excludeId);
            }

            long count = menuDao.selectCount(wrapper);
            return ResponseDTO.ok(count == 0);

        } catch (Exception e) {
            log.error("检查菜单编码唯一性失败，menuCode：{}，错误：{}", menuCode, e.getMessage(), e);
            return ResponseDTO.ok(false);
        }
    }

    @Override
    @Cacheable(value = "hasChildMenu", key = "#menuId", unless = "#result == null")
    public ResponseDTO<Boolean> hasChildMenu(Long menuId) {
        log.debug("检查是否存在子菜单，menuId：{}", menuId);

        try {
            if (menuId == null) {
                return ResponseDTO.ok(false);
            }

            LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuEntity::getParentId, menuId);
            wrapper.eq(MenuEntity::getDeletedFlag, 0);

            long count = menuDao.selectCount(wrapper);
            return ResponseDTO.ok(count > 0);

        } catch (Exception e) {
            log.error("检查是否存在子菜单失败，menuId：{}，错误：{}", menuId, e.getMessage(), e);
            return ResponseDTO.ok(false);
        }
    }

    @Override
    public ResponseDTO<Boolean> isMenuUsedByRole(Long menuId) {
        log.debug("检查菜单是否被角色使用，menuId：{}", menuId);

        try {
            if (menuId == null) {
                return ResponseDTO.ok(false);
            }

            // 这里需要查询角色菜单关联表，暂时返回false
            // 实际实现需要查询 t_role_menu 表
            boolean isUsed = menuManager.isMenuUsedByRole(menuId);
            return ResponseDTO.ok(isUsed);

        } catch (Exception e) {
            log.error("检查菜单是否被角色使用失败，menuId：{}，错误：{}", menuId, e.getMessage(), e);
            return ResponseDTO.ok(false);
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getMenuStatistics() {
        log.debug("获取菜单统计信息");

        try {
            Map<String, Object> statistics = new HashMap<>();

            // 统计总菜单数
            LambdaQueryWrapper<MenuEntity> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(MenuEntity::getDeletedFlag, 0);
            long totalCount = menuDao.selectCount(totalWrapper);

            // 统计启用菜单数
            LambdaQueryWrapper<MenuEntity> enabledWrapper = new LambdaQueryWrapper<>();
            enabledWrapper.eq(MenuEntity::getDeletedFlag, 0);
            enabledWrapper.eq(MenuEntity::getStatus, STATUS_ENABLED);
            long enabledCount = menuDao.selectCount(enabledWrapper);

            // 统计各类型菜单数
            statistics.put("totalCount", totalCount);
            statistics.put("enabledCount", enabledCount);
            statistics.put("disabledCount", totalCount - enabledCount);
            statistics.put("dirCount", getMenuCountByType(MENU_TYPE_DIR));
            statistics.put("menuCount", getMenuCountByType(MENU_TYPE_MENU));
            statistics.put("buttonCount", getMenuCountByType(MENU_TYPE_BUTTON));

            return ResponseDTO.ok(statistics);

        } catch (Exception e) {
            log.error("获取菜单统计信息失败，错误：{}", e.getMessage(), e);
            throw new BusinessException("获取菜单统计信息失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 构建菜单树
     */
    private List<MenuVO> buildMenuTree(List<MenuVO> menuList, Long parentId) {
        List<MenuVO> tree = new ArrayList<>();

        for (MenuVO menu : menuList) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                List<MenuVO> children = buildMenuTree(menuList, menu.getMenuId());
                if (!children.isEmpty()) {
                    menu.setChildren(children);
                }
                tree.add(menu);
            }
        }

        return tree;
    }

    /**
     * 转换菜单为Map格式
     */
    private Map<String, Object> convertMenuToMap(MenuVO menu) {
        Map<String, Object> menuMap = new HashMap<>();
        menuMap.put("menuId", menu.getMenuId());
        menuMap.put("menuName", menu.getMenuName());
        menuMap.put("menuCode", menu.getMenuCode());
        menuMap.put("menuType", menu.getMenuType());
        menuMap.put("icon", menu.getIcon());
        menuMap.put("path", menu.getPath());
        menuMap.put("component", menu.getComponent());
        menuMap.put("permission", menu.getPermission());
        menuMap.put("visible", menu.getVisible());
        menuMap.put("sortOrder", menu.getSortOrder());

        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<Map<String, Object>> children = new ArrayList<>();
            for (MenuVO child : menu.getChildren()) {
                children.add(convertMenuToMap(child));
            }
            menuMap.put("children", children);
        }

        return menuMap;
    }

    /**
     * 获取菜单类型名称
     */
    private String getMenuTypeName(Integer menuType) {
        if (menuType == null) {
            return "";
        }
        if (Objects.equals(menuType, MENU_TYPE_DIR)) {
            return "目录";
        } else if (Objects.equals(menuType, MENU_TYPE_MENU)) {
            return "菜单";
        } else if (Objects.equals(menuType, MENU_TYPE_BUTTON)) {
            return "按钮";
        } else {
            return "";
        }
    }

    /**
     * 根据类型统计菜单数量
     */
    private Long getMenuCountByType(Integer menuType) {
        LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuEntity::getDeletedFlag, 0);
        wrapper.eq(MenuEntity::getMenuType, menuType);
        return menuDao.selectCount(wrapper);
    }
}
