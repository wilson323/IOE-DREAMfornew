package net.lab1024.sa.consume.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeProductQueryForm;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;
import net.lab1024.sa.common.entity.consume.ConsumeProductEntity;

/**
 * 消费产品数据访问对象
 * <p>
 * 遵循MyBatis-Plus规范，使用@Mapper注解而非@Repository
 * 提供产品的完整数据访问操作
 * 支持库存管理、价格统计和销售分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Mapper
public interface ConsumeProductDao extends BaseMapper<ConsumeProductEntity> {

    /**
     * 分页查询产品
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeProductVO> queryPage(@Param("queryForm") ConsumeProductQueryForm queryForm);

    /**
     * 根据产品ID查询详细信息
     *
     * @param productId 产品ID
     * @return 产品详情
     */
    ConsumeProductVO selectDetailById(@Param("productId") Long productId);

    /**
     * 根据产品编码查询产品
     *
     * @param productCode 产品编码
     * @return 产品信息
     */
    ConsumeProductEntity selectByCode(@Param("productCode") String productCode);

    /**
     * 查询所有上架产品（按推荐排序）
     *
     * @return 上架产品列表
     */
    List<ConsumeProductVO> selectAllOnSale();

    /**
     * 查询推荐产品列表
     *
     * @param limit 限制数量
     * @return 推荐产品列表
     */
    List<ConsumeProductVO> selectRecommended(@Param("limit") Integer limit);

    /**
     * 根据分类ID查询产品列表
     *
     * @param categoryId 分类ID
     * @param status 状态筛选（可选）
     * @return 产品列表
     */
    List<ConsumeProductVO> selectByCategoryId(@Param("categoryId") Long categoryId, @Param("status") Integer status);

    /**
     * 查询热销产品（按销量排序）
     *
     * @param limit 限制数量
     * @return 热销产品列表
     */
    List<ConsumeProductVO> selectHotSales(@Param("limit") Integer limit);

    /**
     * 查询高评分产品（按评分排序）
     *
     * @param limit 限制数量
     * @param minRating 最低评分
     * @return 高评分产品列表
     */
    List<ConsumeProductVO> selectHighRated(@Param("limit") Integer limit, @Param("minRating") BigDecimal minRating);

    /**
     * 检查产品编码是否存在
     *
     * @param productCode 产品编码
     * @param excludeId 排除的产品ID（用于更新时检查）
     * @return 存在数量
     */
    int countByCode(@Param("productCode") String productCode, @Param("excludeId") Long excludeId);

    /**
     * 查询库存不足的产品
     *
     * @return 库存不足产品列表
     */
    List<ConsumeProductVO> selectLowStockProducts();

    /**
     * 更新产品库存
     *
     * @param productId 产品ID
     * @param quantity 库存变化量（正数增加，负数减少）
     * @return 更新行数
     */
    int updateStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 批量更新产品库存
     *
     * @param updates 更新列表，包含productId和quantity
     * @return 更新行数
     */
    int batchUpdateStock(@Param("updates") java.util.List<Map<String, Object>> updates);

    /**
     * 增加产品销量
     *
     * @param productId 产品ID
     * @param quantity 销量数量
     * @return 更新行数
     */
    int increaseSalesCount(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 更新产品评分
     *
     * @param productId 产品ID
     * @param rating 新评分
     * @param ratingCount 评分数量
     * @return 更新行数
     */
    int updateRating(@Param("productId") Long productId, @Param("rating") BigDecimal rating, @Param("ratingCount") Integer ratingCount);

    /**
     * 查询产品统计信息
     *
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计信息
     */
    Map<String, Object> getProductStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 按分类统计产品数量
     *
     * @return 分类产品统计
     */
    List<Map<String, Object>> countProductsByCategory();

    /**
     * 按价格区间统计产品数量
     *
     * @return 价格区间统计
     */
    List<Map<String, Object>> countProductsByPriceRange();

    /**
     * 查询库存统计信息
     *
     * @return 库存统计
     */
    Map<String, Object> getStockStatistics();

    /**
     * 更新产品状态
     *
     * @param productId 产品ID
     * @param status 新状态
     * @return 更新行数
     */
    int updateStatus(@Param("productId") Long productId, @Param("status") Integer status);

    /**
     * 批量更新产品状态
     *
     * @param productIds 产品ID列表
     * @param status 新状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("productIds") java.util.List<Long> productIds, @Param("status") Integer status);

    /**
     * 查询产品销售记录
     *
     * @param productId 产品ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 销售记录数量
     */
    Long countSalesRecords(@Param("productId") Long productId,
                          @Param("startDate") String startDate,
                          @Param("endDate") String endDate);

    /**
     * 查询产品总销售额
     *
     * @param productId 产品ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 总销售额
     */
    BigDecimal sumSalesAmount(@Param("productId") Long productId,
                             @Param("startDate") String startDate,
                             @Param("endDate") String endDate);

    /**
     * 查询近期销售的产品
     *
     * @param days 天数
     * @param limit 限制数量
     * @return 近期销售产品列表
     */
    List<ConsumeProductVO> selectRecentSold(@Param("days") Integer days, @Param("limit") Integer limit);

    /**
     * 搜索产品（按名称或编码模糊查询）
     *
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 搜索结果
     */
    List<ConsumeProductVO> searchProducts(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 验证产品是否可删除
     *
     * @param productId 产品ID
     * @return 关联记录数量
     */
    Map<String, Long> countRelatedRecords(@Param("productId") Long productId);
}