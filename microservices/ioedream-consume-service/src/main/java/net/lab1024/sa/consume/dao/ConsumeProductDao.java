package net.lab1024.sa.consume.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.entity.ConsumeProductEntity;

/**
 * 商品DAO接口
 * <p>
 * 用于商品的数据访问操作
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@org.apache.ibatis.annotations.Mapper
public interface ConsumeProductDao extends BaseMapper<ConsumeProductEntity> {

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<ConsumeProductEntity> selectByCategoryId(@Param("categoryId") String categoryId);

    /**
     * 根据条码查询商品
     *
     * @param barcode 商品条码
     * @return 商品信息
     */
    ConsumeProductEntity selectByBarcode(@Param("barcode") String barcode);

    /**
     * 查询上架商品列表
     *
     * @return 上架商品列表
     */
    List<ConsumeProductEntity> selectOnShelfProducts();

    /**
     * 根据区域ID查询可售商品列表
     *
     * @param areaId 区域ID
     * @return 可售商品列表
     */
    List<ConsumeProductEntity> selectByAreaId(@Param("areaId") String areaId);
}



