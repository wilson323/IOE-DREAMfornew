/*
 * 商品分类 DAO接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-19
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductCategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品分类 DAO接口
 *
 * @author SmartAdmin Team
 * @date 2025/01/19
 */
@Mapper
public interface ProductCategoryDao extends BaseMapper<ProductCategoryEntity> {

    /**
     * 根据父级ID查询分类列表
     *
     * @param parentId 父级分类ID
     * @return 分类列表
     */
    List<ProductCategoryEntity> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据分类编码查询分类
     *
     * @param categoryCode 分类编码
     * @return 分类信息
     */
    ProductCategoryEntity selectByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * 查询启用的分类列表
     *
     * @return 启用的分类列表
     */
    List<ProductCategoryEntity> selectEnabledCategories();

    /**
     * 查询显示的分类列表
     *
     * @return 显示的分类列表
     */
    List<ProductCategoryEntity> selectVisibleCategories();

    /**
     * 根据层级查询分类列表
     *
     * @param level 分类层级
     * @return 分类列表
     */
    List<ProductCategoryEntity> selectByLevel(@Param("level") Integer level);

    /**
     * 更新分类状态
     *
     * @param categoryId 分类ID
     * @param status 状态
     * @return 更新结果
     */
    int updateStatus(@Param("categoryId") Long categoryId, @Param("status") Integer status);

    /**
     * 更新分类显示状态
     *
     * @param categoryId 分类ID
     * @param isShow 显示状态
     * @return 更新结果
     */
    int updateIsShow(@Param("categoryId") Long categoryId, @Param("isShow") Integer isShow);

    /**
     * 统计分类下商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);
}
