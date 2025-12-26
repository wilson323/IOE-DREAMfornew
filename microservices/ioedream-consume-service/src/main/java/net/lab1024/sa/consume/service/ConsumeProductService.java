package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeProductAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeProductQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeProductUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 消费产品服务接口
 * <p>
 * 完整的企业级接口定义，包含：
 * - 完整的CRUD操作
 * - 业务管理方法
 * - 统计分析方法
 * - 库存管理操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public interface ConsumeProductService {

    // ==================== 基础CRUD操作 ====================

    /**
     * 分页查询产品列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<ConsumeProductVO> queryPage(ConsumeProductQueryForm queryForm);

    /**
     * 查询产品列表
     *
     * @param queryForm 查询条件
     * @return 产品列表
     */
    PageResult<ConsumeProductVO> queryProducts(ConsumeProductQueryForm queryForm);

    /**
     * 根据产品ID查询详情
     *
     * @param productId 产品ID
     * @return 产品详情
     */
    ConsumeProductVO getById(Long productId);

    /**
     * 获取产品详情
     *
     * @param productId 产品ID
     * @return 产品详情
     */
    ConsumeProductVO getProductDetail(Long productId);

    /**
     * 添加新产品
     *
     * @param addForm 添加表单
     * @return 新增产品信息
     */
    ConsumeProductVO add(@Valid ConsumeProductAddForm addForm);

    /**
     * 创建产品
     *
     * @param addForm 添加表单
     * @return 产品ID
     */
    Long createProduct(@Valid ConsumeProductAddForm addForm);

    /**
     * 更新产品信息
     *
     * @param updateForm 更新表单
     * @return 更新后产品信息
     */
    ConsumeProductVO update(@Valid ConsumeProductUpdateForm updateForm);

    /**
     * 删除产品
     *
     * @param productId 产品ID
     */
    void delete(Long productId);

    /**
     * 批量删除产品
     *
     * @param productIds 产品ID列表
     * @return 删除数量
     */
    Integer batchDelete(List<Long> productIds);

    // ==================== 产品查询操作 ====================

    /**
     * 获取所有上架产品
     *
     * @return 上架产品列表
     */
    List<ConsumeProductVO> getAllOnSale();

    /**
     * 获取推荐产品列表
     *
     * @param limit 限制数量
     * @return 推荐产品列表
     */
    List<ConsumeProductVO> getRecommendedProducts(Integer limit);

    /**
     * 根据分类ID查询产品
     *
     * @param categoryId 分类ID
     * @return 产品列表
     */
    List<ConsumeProductVO> getByCategoryId(Long categoryId);

    /**
     * 查询热销产品
     *
     * @param limit 限制数量
     * @return 热销产品列表
     */
    List<ConsumeProductVO> getHotSales(Integer limit);

    /**
     * 查询高评分产品
     *
     * @param limit 限制数量
     * @param minRating 最低评分
     * @return 高评分产品列表
     */
    List<ConsumeProductVO> getHighRated(Integer limit, BigDecimal minRating);

    /**
     * 搜索产品
     *
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 搜索结果
     */
    List<ConsumeProductVO> searchProducts(String keyword, Integer limit);

    /**
     * 获取库存不足产品
     *
     * @return 库存不足产品列表
     */
    List<ConsumeProductVO> getLowStockProducts();

    /**
     * 获取近期销售产品
     *
     * @param days 天数
     * @param limit 限制数量
     * @return 近期销售产品列表
     */
    List<ConsumeProductVO> getRecentSold(Integer days, Integer limit);

    // ==================== 产品状态管理 ====================

    /**
     * 上架产品
     *
     * @param productId 产品ID
     */
    void putOnSale(Long productId);

    /**
     * 下架产品
     *
     * @param productId 产品ID
     */
    void putOffSale(Long productId);

    /**
     * 批量更新产品状态
     *
     * @param productIds 产品ID列表
     * @param status 新状态
     * @return 更新数量
     */
    Integer batchUpdateStatus(List<Long> productIds, Integer status);

    /**
     * 设置推荐状态
     *
     * @param productId 产品ID
     * @param isRecommended 是否推荐
     * @param recommendSort 推荐排序
     */
    void setRecommended(Long productId, Integer isRecommended, Integer recommendSort);

    // ==================== 库存管理操作 ====================

    /**
     * 更新产品库存
     *
     * @param productId 产品ID
     * @param quantity 库存变化量（正数增加，负数减少）
     * @return 操作结果
     */
    Boolean updateStock(Long productId, Integer quantity);

    /**
     * 批量更新产品库存
     *
     * @param stockUpdates 库存更新列表
     * @return 更新结果
     */
    Map<String, Object> batchUpdateStock(List<Map<String, Object>> stockUpdates);

    /**
     * 检查产品库存
     *
     * @param productId 产品ID
     * @param requiredQuantity 需要数量
     * @return 是否充足
     */
    Boolean checkStock(Long productId, Integer requiredQuantity);

    /**
     * 预扣库存（下单时使用）
     *
     * @param productId 产品ID
     * @param quantity 扣减数量
     * @return 操作结果
     */
    Boolean deductStock(Long productId, Integer quantity);

    /**
     * 恢复库存（取消订单时使用）
     *
     * @param productId 产品ID
     * @param quantity 恢复数量
     * @return 操作结果
     */
    Boolean restoreStock(Long productId, Integer quantity);

    // ==================== 价格管理操作 ====================

    /**
     * 计算实际售价
     *
     * @param productId 产品ID
     * @param discountRate 折扣比例
     * @return 实际售价
     */
    BigDecimal calculateActualPrice(Long productId, BigDecimal discountRate);

    /**
     * 批量更新价格
     *
     * @param priceUpdates 价格更新列表
     * @return 更新结果
     */
    Map<String, Object> batchUpdatePrice(List<Map<String, Object>> priceUpdates);

    /**
     * 验证价格合理性
     *
     * @param productId 产品ID
     * @param basePrice 基础价格
     * @param salePrice 售价
     * @param costPrice 成本价
     * @return 验证结果
     */
    Boolean validatePrice(Long productId, BigDecimal basePrice, BigDecimal salePrice, BigDecimal costPrice);

    // ==================== 统计分析操作 ====================

    /**
     * 获取产品统计数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> getStatistics(String startDate, String endDate);

    /**
     * 获取库存统计
     *
     * @return 库存统计
     */
    Map<String, Object> getStockStatistics();

    /**
     * 获取销售统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销售统计
     */
    Map<String, Object> getSalesStatistics(String startDate, String endDate);

    /**
     * 获取分类统计
     *
     * @return 分类统计
     */
    List<Map<String, Object>> getCategoryStatistics();

    /**
     * 获取价格区间统计
     *
     * @return 价格区间统计
     */
    List<Map<String, Object>> getPriceRangeStatistics();

    // ==================== 业务验证操作 ====================

    /**
     * 验证产品编码唯一性
     *
     * @param productCode 产品编码
     * @param excludeId 排除的产品ID
     * @return 是否唯一
     */
    Boolean checkCodeUnique(String productCode, Long excludeId);

    /**
     * 验证产品是否可删除
     *
     * @param productId 产品ID
     * @return 检查结果
     */
    Map<String, Object> checkCanDelete(Long productId);

    /**
     * 检查产品是否可销售
     *
     * @param productId 产品ID
     * @return 是否可销售
     */
    Boolean checkCanSale(Long productId);

    /**
     * 检查产品是否允许折扣
     *
     * @param productId 产品ID
     * @param discountRate 折扣比例
     * @return 是否允许
     */
    Boolean checkCanDiscount(Long productId, BigDecimal discountRate);

    // ==================== 数据导入导出操作 ====================

    /**
     * 导出产品数据
     *
     * @param queryForm 查询条件
     * @return 导出文件路径
     */
    String exportProducts(ConsumeProductQueryForm queryForm);

    /**
     * 导入产品数据
     *
     * @param filePath 文件路径
     * @return 导入结果
     */
    Map<String, Object> importProducts(String filePath);

    /**
     * 下载产品模板
     *
     * @return 模板文件路径
     */
    String downloadTemplate();
}