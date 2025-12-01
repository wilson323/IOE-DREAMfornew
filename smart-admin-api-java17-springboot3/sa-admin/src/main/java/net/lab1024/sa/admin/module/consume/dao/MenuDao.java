package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 菜单 DAO
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Mapper
public interface MenuDao extends BaseMapper<MenuEntity> {

    /**
     * 根据分类ID查询菜单
     *
     * @param categoryId 分类ID
     * @return 菜单列表
     */
    List<MenuEntity> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查询可用菜单
     *
     * @return 菜单列表
     */
    List<MenuEntity> selectAvailableMenus();

    /**
     * 搜索菜单
     *
     * @param keyword 关键词
     * @return 菜单列表
     */
    List<MenuEntity> searchMenu(@Param("keyword") String keyword);

    /**
     * 查询推荐菜单
     *
     * @param limit 限制数量
     * @return 菜单列表
     */
    List<MenuEntity> selectRecommendMenus(@Param("limit") Integer limit);

    /**
     * 扣减库存
     *
     * @param menuId 菜单ID
     * @param quantity 扣减数量
     * @return 更新行数
     */
    @Update("UPDATE t_menu SET stock = stock - #{quantity} WHERE menu_id = #{menuId} AND stock >= #{quantity}")
    int deductStock(@Param("menuId") Long menuId, @Param("quantity") Integer quantity);

    /**
     * 恢复库存
     *
     * @param menuId 菜单ID
     * @param quantity 恢复数量
     * @return 更新行数
     */
    @Update("UPDATE t_menu SET stock = stock + #{quantity} WHERE menu_id = #{menuId}")
    int restoreStock(@Param("menuId") Long menuId, @Param("quantity") Integer quantity);
}