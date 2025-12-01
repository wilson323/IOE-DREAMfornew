package net.lab1024.sa.admin.module.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品DAO
 *
 * 严格遵循repowiki规范:
 * - 继承BaseMapper，提供基础CRUD操作
 * - 使用@Mapper注解标记
 * - 提供复杂查询接口
 * - 支持产品状态和分类查询
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Mapper
public interface ProductDao extends BaseMapper<ProductEntity> {

    /**
     * 根据产品编码查询产品
     *
     * @param productCode 产品编码
     * @return 产品信息
     */
    ProductEntity selectByProductCode(@Param("productCode") String productCode);

    /**
     * 根据分类ID查询产品列表
     *
     * @param categoryId 分类ID
     * @return 产品列表
     */
    List<ProductEntity> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据状态查询产品列表
     *
     * @param status 产品状态
     * @return 产品列表
     */
    List<ProductEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据供应商ID查询产品列表
     *
     * @param supplierId 供应商ID
     * @return 产品列表
     */
    List<ProductEntity> selectBySupplierId(@Param("supplierId") Long supplierId);

    /**
     * 查询推荐产品列表
     *
     * @param limit 限制数量
     * @return 推荐产品列表
     */
    List<ProductEntity> selectRecommendedProducts(@Param("limit") Integer limit);

    /**
     * 查询热销产品列表
     *
     * @param limit 限制数量
     * @return 热销产品列表
     */
    List<ProductEntity> selectHotSaleProducts(@Param("limit") Integer limit);

    /**
     * 查询新品列表
     *
     * @param limit 限制数量
     * @return 新品列表
     */
    List<ProductEntity> selectNewProducts(@Param("limit") Integer limit);

    /**
     * 根据关键词搜索产品
     *
     * @param keyword 关键词
     * @param limit   限制数量
     * @return 产品列表
     */
    List<ProductEntity> selectByKeyword(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 更新产品库存
     *
     * @param productId 产品ID
     * @param stock     库存数量
     * @return 影响行数
     */
    int updateStock(@Param("productId") Long productId, @Param("stock") Integer stock);
}