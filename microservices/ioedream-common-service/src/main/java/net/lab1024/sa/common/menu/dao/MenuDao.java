package net.lab1024.sa.common.menu.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.menu.entity.MenuEntity;

@Mapper
public interface MenuDao extends BaseMapper<MenuEntity> {

    List<MenuEntity> selectMenuListByUserId(Long userId);

    List<MenuEntity> selectMenuListByRoleId(Long roleId);

    List<MenuEntity> selectMenuTree();

    List<MenuEntity> selectChildMenus(Long parentId);

    MenuEntity selectByMenuCode(String menuCode);

    MenuEntity selectByPermission(String permission);

    Integer selectMaxSortOrder(Long parentId);

    int updateSortOrder(Long menuId, Integer sortOrder);

    int updateMenuLevel(Long menuId, Integer menuLevel);

    Integer countChildMenus(Long menuId);
}
