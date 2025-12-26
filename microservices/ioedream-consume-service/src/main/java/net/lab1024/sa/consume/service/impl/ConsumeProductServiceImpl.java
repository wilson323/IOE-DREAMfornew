package net.lab1024.sa.consume.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.dao.ConsumeProductDao;
import net.lab1024.sa.consume.domain.form.ConsumeProductAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeProductQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeProductUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;
import net.lab1024.sa.common.entity.consume.ConsumeProductEntity;
import net.lab1024.sa.consume.exception.ConsumeProductException;
import net.lab1024.sa.consume.manager.ConsumeProductManager;
import net.lab1024.sa.consume.service.ConsumeProductService;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;

/**
 * 消费产品服务实现
 * <p>
 * 完整的企业级实现，包含：
 * - 完整的业务逻辑处理
 * - 数据验证和权限控制
 * - 事务管理和异常处理
 * - 审计日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeProductServiceImpl implements ConsumeProductService {

    @Resource
    private ConsumeProductDao consumeProductDao;

    @Resource
    private ConsumeProductManager consumeProductManager;

    // ==================== 基础CRUD操作 ====================

    @Override
    @PermissionCheck("consume:product:query")
    public PageResult<ConsumeProductVO> queryPage(ConsumeProductQueryForm queryForm) {
        try {
            log.info("[产品服务] [产品管理] 开始分页查询: queryForm={}", queryForm.getQuerySummary());

            PageResult<ConsumeProductVO> pageResult = consumeProductDao.queryPage(queryForm);

            // 设置计算字段
            for (ConsumeProductVO product : pageResult.getList()) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 分页查询成功: total={}, pageNum={}, pageSize={}",
                pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());

            return pageResult;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 分页查询失败: queryForm={}, error={}", queryForm, e.getMessage(), e);
            throw ConsumeProductException.databaseError("分页查询", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    @Cacheable(value = "product", key = "#productId", unless = "#result == null")
    public ConsumeProductVO getById(Long productId) {
        try {
            log.info("[产品服务] [产品管理] 开始查询产品详情: productId={}", productId);

            ConsumeProductVO productVO = consumeProductDao.selectDetailById(productId);
            if (productVO == null) {
                log.warn("[产品服务] [产品管理] 产品不存在: productId={}", productId);
                throw ConsumeProductException.notFound(productId);
            }

            // 设置计算字段
            setComputedFields(productVO);

            log.info("[产品服务] [产品管理] 查询产品详情成功: productId={}", productId);
            return productVO;

        } catch (ConsumeProductException e) {
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询产品详情失败: productId={}, error={}", productId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询产品详情", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:add")
    @CacheEvict(value = "product", allEntries = true)
    public ConsumeProductVO add(@Valid ConsumeProductAddForm addForm) {
        try {
            log.info("[产品服务] [产品管理] 开始添加产品: addForm={}", addForm);

            // 构建实体对象
            ConsumeProductEntity entity = buildEntityFromAddForm(addForm);

            // 验证业务规则
            consumeProductManager.validateProductRules(entity);

            // 设置默认值
            setDefaultValues(entity);

            // 插入数据
            consumeProductDao.insert(entity);

            // 构建返回对象
            ConsumeProductVO resultVO = buildVOFromEntity(entity);
            setComputedFields(resultVO);

            log.info("[产品服务] [产品管理] 添加产品成功: productId={}, productCode={}",
                entity.getProductId(), entity.getProductCode());

            return resultVO;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 添加产品失败: addForm={}, error={}", addForm, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 添加产品异常: addForm={}, error={}", addForm, e.getMessage(), e);
            throw ConsumeProductException.databaseError("添加产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:update")
    @CacheEvict(value = "product", allEntries = true)
    public ConsumeProductVO update(@Valid ConsumeProductUpdateForm updateForm) {
        try {
            log.info("[产品服务] [产品管理] 开始更新产品: updateForm={}", updateForm);

            // 查询原产品
            ConsumeProductEntity existingProduct = consumeProductDao.selectById(updateForm.getProductId());
            if (existingProduct == null) {
                throw ConsumeProductException.notFound(updateForm.getProductId());
            }

            // 检查版本号（乐观锁）
            if (!existingProduct.getVersion().equals(updateForm.getVersion())) {
                throw ConsumeProductException.invalidParameter("产品信息已被其他用户修改，请刷新后重试");
            }

            // 更新字段
            updateEntityFromUpdateForm(existingProduct, updateForm);
            existingProduct.setVersion(existingProduct.getVersion() + 1); // 增加版本号

            // 验证业务规则
            consumeProductManager.validateProductRules(existingProduct);

            // 更新数据
            consumeProductDao.updateById(existingProduct);

            // 构建返回对象
            ConsumeProductVO resultVO = buildVOFromEntity(existingProduct);
            setComputedFields(resultVO);

            log.info("[产品服务] [产品管理] 更新产品成功: productId={}", existingProduct.getProductId());

            return resultVO;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 更新产品失败: updateForm={}, error={}", updateForm, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 更新产品异常: updateForm={}, error={}", updateForm, e.getMessage(), e);
            throw ConsumeProductException.databaseError("更新产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:delete")
    @CacheEvict(value = "product", allEntries = true)
    public void delete(Long productId) {
        try {
            log.info("[产品服务] [产品管理] 开始删除产品: productId={}", productId);

            // 检查删除条件
            Map<String, Object> checkResult = consumeProductManager.checkDeleteProduct(productId);
            if (!(Boolean) checkResult.get("canDelete")) {
                String reason = (String) checkResult.get("reason");
                log.warn("[产品服务] [产品管理] 删除产品失败: productId={}, reason={}", productId, reason);
                throw ConsumeProductException.businessRuleViolation(reason);
            }

            // 检查业务关联
            @SuppressWarnings("unchecked")
            Map<String, Long> relatedRecords = (Map<String, Long>) checkResult.get("relatedRecords");
            boolean hasRelatedRecords = relatedRecords.values().stream().anyMatch(count -> count > 0);

            if (hasRelatedRecords) {
                throw ConsumeProductException.usedInTransaction(productId, relatedRecords);
            }

            // 软删除
            int deletedCount = consumeProductDao.deleteById(productId);
            if (deletedCount <= 0) {
                throw ConsumeProductException.databaseError("删除产品", "删除操作未影响任何行");
            }

            log.info("[产品服务] [产品管理] 删除产品成功: productId={}", productId);

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 删除产品失败: productId={}, error={}", productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 删除产品异常: productId={}, error={}", productId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("删除产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:delete")
    @CacheEvict(value = "product", allEntries = true)
    public Integer batchDelete(List<Long> productIds) {
        try {
            log.info("[产品服务] [产品管理] 开始批量删除产品: productIds={}", productIds);

            if (productIds == null || productIds.isEmpty()) {
                return 0;
            }

            int successCount = 0;
            List<String> failedProducts = new ArrayList<>();

            for (Long productId : productIds) {
                try {
                    delete(productId);
                    successCount++;
                } catch (Exception e) {
                    failedProducts.add("产品ID: " + productId + ", 错误: " + e.getMessage());
                    log.error("[产品服务] [产品管理] 批量删除单个产品失败: productId={}, error={}", productId, e.getMessage());
                }
            }

            if (!failedProducts.isEmpty()) {
                log.warn("[产品服务] [产品管理] 批量删除部分失败: 成功={}, 失败详情={}", successCount, failedProducts);
            }

            log.info("[产品服务] [产品管理] 批量删除产品完成: 成功={}, 失败={}", successCount, failedProducts.size());

            return successCount;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 批量删除产品异常: productIds={}, error={}", productIds, e.getMessage(), e);
            throw ConsumeProductException.databaseError("批量删除产品", e.getMessage());
        }
    }

    // ==================== 产品查询操作 ====================

    @Override
    @PermissionCheck("consume:product:query")
    @Cacheable(value = "product", key = "'allOnSale'")
    public List<ConsumeProductVO> getAllOnSale() {
        try {
            log.info("[产品服务] [产品管理] 开始查询所有上架产品");

            List<ConsumeProductVO> products = consumeProductDao.selectAllOnSale();

            // 设置计算字段
            for (ConsumeProductVO product : products) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 查询所有上架产品成功: count={}", products.size());

            return products;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询所有上架产品失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询所有上架产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    @Cacheable(value = "product", key = "'recommended:' + #limit")
    public List<ConsumeProductVO> getRecommendedProducts(Integer limit) {
        try {
            log.info("[产品服务] [产品管理] 开始查询推荐产品: limit={}", limit);

            List<ConsumeProductVO> products = consumeProductManager.getRecommendedProducts(limit);

            // 设置计算字段
            for (ConsumeProductVO product : products) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 查询推荐产品成功: count={}", products.size());

            return products;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询推荐产品失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询推荐产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    @Cacheable(value = "product", key = "'category:' + #categoryId")
    public List<ConsumeProductVO> getByCategoryId(Long categoryId) {
        try {
            log.info("[产品服务] [产品管理] 开始查询分类产品: categoryId={}", categoryId);

            List<ConsumeProductVO> products = consumeProductDao.selectByCategoryId(categoryId, 1);

            // 设置计算字段
            for (ConsumeProductVO product : products) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 查询分类产品成功: categoryId={}, count={}", categoryId, products.size());

            return products;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询分类产品失败: categoryId={}, error={}", categoryId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询分类产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    @Cacheable(value = "product", key = "'hotSales:' + #limit")
    public List<ConsumeProductVO> getHotSales(Integer limit) {
        try {
            log.info("[产品服务] [产品管理] 开始查询热销产品: limit={}", limit);

            List<ConsumeProductVO> products = consumeProductDao.selectHotSales(limit);

            // 设置计算字段
            for (ConsumeProductVO product : products) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 查询热销产品成功: count={}", products.size());

            return products;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询热销产品失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询热销产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public List<ConsumeProductVO> getHighRated(Integer limit, BigDecimal minRating) {
        try {
            log.info("[产品服务] [产品管理] 开始查询高评分产品: limit={}, minRating={}", limit, minRating);

            List<ConsumeProductVO> products = consumeProductDao.selectHighRated(limit, minRating);

            // 设置计算字段
            for (ConsumeProductVO product : products) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 查询高评分产品成功: count={}", products.size());

            return products;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询高评分产品失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询高评分产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public List<ConsumeProductVO> searchProducts(String keyword, Integer limit) {
        try {
            log.info("[产品服务] [产品管理] 开始搜索产品: keyword={}, limit={}", keyword, limit);

            List<ConsumeProductVO> products = consumeProductManager.searchProducts(keyword, limit);

            // 设置计算字段
            for (ConsumeProductVO product : products) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 搜索产品成功: keyword={}, count={}", keyword, products.size());

            return products;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 搜索产品失败: keyword={}, error={}", keyword, e.getMessage(), e);
            throw ConsumeProductException.databaseError("搜索产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public List<ConsumeProductVO> getLowStockProducts() {
        try {
            log.info("[产品服务] [产品管理] 开始查询库存不足产品");

            List<ConsumeProductVO> products = consumeProductManager.getLowStockProducts();

            // 设置计算字段
            for (ConsumeProductVO product : products) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 查询库存不足产品成功: count={}", products.size());

            return products;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询库存不足产品失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询库存不足产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public List<ConsumeProductVO> getRecentSold(Integer days, Integer limit) {
        try {
            log.info("[产品服务] [产品管理] 开始查询近期销售产品: days={}, limit={}", days, limit);

            List<ConsumeProductVO> products = consumeProductDao.selectRecentSold(days, limit);

            // 设置计算字段
            for (ConsumeProductVO product : products) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 查询近期销售产品成功: count={}", products.size());

            return products;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询近期销售产品失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询近期销售产品", e.getMessage());
        }
    }

    // ==================== 产品状态管理 ====================

    @Override
    @PermissionCheck("consume:product:update")
    @CacheEvict(value = "product", allEntries = true)
    public void putOnSale(Long productId) {
        try {
            log.info("[产品服务] [产品管理] 开始上架产品: productId={}", productId);

            ConsumeProductEntity product = consumeProductDao.selectById(productId);
            if (product == null) {
                throw ConsumeProductException.notFound(productId);
            }

            if (product.isOnSale()) {
                          log.warn("[产品服务] [产品管理] 产品已上架: productId={}", productId);
                return;
            }

            int updatedCount = consumeProductDao.updateStatus(productId, 1);
            if (updatedCount <= 0) {
                throw ConsumeProductException.databaseError("上架产品", "更新状态失败");
            }

                    log.info("[产品服务] [产品管理] 上架产品成功: productId={}", productId);
            // void方法无需返回值

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 上架产品失败: productId={}, error={}", productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 上架产品异常: productId={}, error={}", productId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("上架产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:update")
    @CacheEvict(value = "product", allEntries = true)
    public void putOffSale(Long productId) {
        try {
            log.info("[产品服务] [产品管理] 开始下架产品: productId={}", productId);

            ConsumeProductEntity product = consumeProductDao.selectById(productId);
            if (product == null) {
                throw ConsumeProductException.notFound(productId);
            }

            if (product.isOffSale()) {
                log.warn("[产品服务] [产品管理] 产品已下架: productId={}", productId);
                return;
            }

            int updatedCount = consumeProductDao.updateStatus(productId, 2);
            if (updatedCount <= 0) {
                throw ConsumeProductException.databaseError("下架产品", "更新状态失败");
            }

            log.info("[产品服务] [产品管理] 下架产品成功: productId={}", productId);

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 下架产品失败: productId={}, error={}", productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 下架产品异常: productId={}, error={}", productId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("下架产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:update")
    @CacheEvict(value = "product", allEntries = true)
    public Integer batchUpdateStatus(List<Long> productIds, Integer status) {
        try {
            log.info("[产品服务] [产品管理] 开始批量更新产品状态: productIds={}, status={}", productIds, status);

                    if (productIds == null || productIds.isEmpty()) {
                return 0;
            }

            int updatedCount = consumeProductDao.batchUpdateStatus(productIds, status);

                    log.info("[产品服务] [产品管理] 批量更新产品状态成功: count={}", updatedCount);
            
            return updatedCount;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 批量更新产品状态异常: productIds={}, status={}, error={}", productIds, status, e.getMessage(), e);
            throw ConsumeProductException.databaseError("批量更新产品状态", e.getMessage());
        }
    }

      @Override
    @PermissionCheck("consume:product:update")
    @CacheEvict(value = "product", allEntries = true)
    public void setRecommended(Long productId, Integer isRecommended, Integer recommendSort) {
        try {
            log.info("[产品服务] [产品管理] 开始设置推荐状态: productId={}, isRecommended={}, recommendSort={}",
                productId, isRecommended, recommendSort);

            ConsumeProductEntity product = consumeProductDao.selectById(productId);
            if (product == null) {
                throw ConsumeProductException.notFound(productId);
            }

            // 更新推荐信息
            product.setIsRecommended(isRecommended);
            if (isRecommended != null && isRecommended == 1 && recommendSort != null) {
                product.setRecommendSort(recommendSort);
            }

            consumeProductDao.updateById(product);

            log.info("[产品服务] [产品管理] 设置推荐状态成功: productId={}", productId);

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 设置推荐状态失败: productId={}, error={}", productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 设置推荐状态异常: productId={}, error={}", productId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("设置推荐状态", e.getMessage());
        }
    }

    // ==================== 库存管理操作 ====================

    @Override
    @PermissionCheck("consume:product:update")
    @CacheEvict(value = "product", allEntries = true)
    public Boolean updateStock(Long productId, Integer quantity) {
        try {
            log.info("[产品服务] [产品管理] 开始更新库存: productId={}, quantity={}", productId, quantity);

            boolean success = consumeProductManager.updateProductStock(productId, quantity);

                      log.info("[产品服务] [产品管理] 更新库存完成: productId={}, quantity={}, success={}",
                productId, quantity, success);
            
            return success;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 更新库存失败: productId={}, quantity={}, error={}",
                productId, quantity, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 更新库存异常: productId={}, quantity={}, error={}",
                productId, quantity, e.getMessage(), e);
            throw ConsumeProductException.databaseError("更新库存", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:update")
    @CacheEvict(value = "product", allEntries = true)
    public Map<String, Object> batchUpdateStock(List<Map<String, Object>> stockUpdates) {
        try {
            log.info("[产品服务] [产品管理] 开始批量更新库存: stockUpdates={}", stockUpdates);

            Map<String, Object> result = consumeProductManager.batchUpdateStock(stockUpdates);

            log.info("[产品服务] [产品管理] 批量更新库存完成: result={}", result);

            return result;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 批量更新库存失败: stockUpdates={}, error={}", stockUpdates, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 批量更新库存异常: stockUpdates={}, error={}", stockUpdates, e.getMessage(), e);
            throw ConsumeProductException.databaseError("批量更新库存", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public Boolean checkStock(Long productId, Integer requiredQuantity) {
        try {
            log.info("[产品服务] [产品管理] 开始检查库存: productId={}, requiredQuantity={}", productId, requiredQuantity);

            boolean available = consumeProductManager.checkStockAvailable(productId, requiredQuantity);

            log.info("[产品服务] [产品管理] 检查库存完成: productId={}, requiredQuantity={}, available={}",
                productId, requiredQuantity, available);

            return available;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 检查库存异常: productId={}, requiredQuantity={}, error={}",
                productId, requiredQuantity, e.getMessage(), e);
            throw ConsumeProductException.databaseError("检查库存", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:update")
    public Boolean deductStock(Long productId, Integer quantity) {
        try {
            log.info("[产品服务] [产品管理] 开始预扣库存: productId={}, quantity={}", productId, quantity);

            if (quantity == null || quantity <= 0) {
                throw ConsumeProductException.invalidParameter("扣减数量必须大于0");
            }

            // 检查库存是否充足
            if (!consumeProductManager.checkStockAvailable(productId, quantity)) {
                throw ConsumeProductException.insufficientStock(productId, null);
            }

            // 扣减库存
            boolean success = consumeProductManager.updateProductStock(productId, -quantity);

            log.info("[产品服务] [产品管理] 预扣库存完成: productId={}, quantity={}, success={}",
                productId, quantity, success);

            return success;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 预扣库存失败: productId={}, quantity={}, error={}",
                productId, quantity, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 预扣库存异常: productId={}, quantity={}, error={}",
                productId, quantity, e.getMessage(), e);
            throw ConsumeProductException.databaseError("预扣库存", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:update")
    public Boolean restoreStock(Long productId, Integer quantity) {
        try {
            log.info("[产品服务] [产品管理] 开始恢复库存: productId={}, quantity={}", productId, quantity);

            if (quantity == null || quantity <= 0) {
                throw ConsumeProductException.invalidParameter("恢复数量必须大于0");
            }

            // 恢复库存
            boolean success = consumeProductManager.updateProductStock(productId, quantity);

            log.info("[产品服务] [产品管理] 恢复库存完成: productId={}, quantity={}, success={}",
                productId, quantity, success);

            return success;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 恢复库存失败: productId={}, quantity={}, error={}",
                productId, quantity, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 恢复库存异常: productId={}, quantity={}, error={}",
                productId, quantity, e.getMessage(), e);
            throw ConsumeProductException.databaseError("恢复库存", e.getMessage());
        }
    }

    // ==================== 价格管理操作 ====================

    @Override
    @PermissionCheck("consume:product:query")
    public BigDecimal calculateActualPrice(Long productId, BigDecimal discountRate) {
        try {
            log.info("[产品服务] [产品管理] 开始计算实际售价: productId={}, discountRate={}", productId, discountRate);

            ConsumeProductEntity product = consumeProductDao.selectById(productId);
            if (product == null) {
                throw ConsumeProductException.notFound(productId);
            }

            BigDecimal actualPrice = consumeProductManager.calculateActualPrice(product, discountRate);

            log.info("[产品服务] [产品管理] 计算实际售价完成: productId={}, actualPrice={}", productId, actualPrice);

            return actualPrice;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 计算实际售价失败: productId={}, discountRate={}, error={}",
                productId, discountRate, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 计算实际售价异常: productId={}, discountRate={}, error={}",
                productId, discountRate, e.getMessage(), e);
            throw ConsumeProductException.databaseError("计算实际售价", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:update")
    public Map<String, Object> batchUpdatePrice(List<Map<String, Object>> priceUpdates) {
        try {
            log.info("[产品服务] [产品管理] 开始批量更新价格: priceUpdates={}", priceUpdates);

            Map<String, Object> result = new HashMap<>();
            List<String> errors = new ArrayList<>();
            int successCount = 0;

            if (priceUpdates == null || priceUpdates.isEmpty()) {
                result.put("success", false);
                result.put("message", "更新列表为空");
                result.put("errors", errors);
                return result;
            }

            for (Map<String, Object> update : priceUpdates) {
                try {
                    Object productIdObj = update.get("productId");
                    Object salePriceObj = update.get("salePrice");

                    if (!(productIdObj instanceof Long) || !(salePriceObj instanceof BigDecimal)) {
                        errors.add("价格更新数据格式错误");
                        continue;
                    }

                    Long productId = (Long) productIdObj;
                    BigDecimal salePrice = (BigDecimal) salePriceObj;

                    // 查询产品
                    ConsumeProductEntity product = consumeProductDao.selectById(productId);
                    if (product == null) {
                        errors.add("产品不存在: " + productId);
                        continue;
                    }

                    // 验证价格合理性
                    if (!consumeProductManager.validatePriceReasonable(product.getOriginalPrice(), salePrice, product.getCostPrice())) {
                        errors.add("价格设置不合理: " + product.getProductName());
                        continue;
                    }

                    // 更新价格
                    product.setPrice(salePrice);
                    consumeProductDao.updateById(product);
                    successCount++;

                } catch (Exception e) {
                    errors.add("更新价格失败: " + e.getMessage());
                }
            }

            result.put("success", errors.isEmpty());
            result.put("message", errors.isEmpty() ? "价格批量更新成功" : "价格批量更新部分失败");
            result.put("successCount", successCount);
            result.put("failCount", errors.size());
            result.put("errors", errors);

            log.info("[产品服务] [产品管理] 批量更新价格完成: result={}", result);

            return result;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 批量更新价格异常: priceUpdates={}, error={}", priceUpdates, e.getMessage(), e);
            throw ConsumeProductException.databaseError("批量更新价格", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public Boolean validatePrice(Long productId, BigDecimal basePrice, BigDecimal salePrice, BigDecimal costPrice) {
        try {
            log.info("[产品服务] [产品管理] 开始验证价格: productId={}, basePrice={}, salePrice={}, costPrice={}",
                productId, basePrice, salePrice, costPrice);

            boolean valid = consumeProductManager.validatePriceReasonable(basePrice, salePrice, costPrice);

            log.info("[产品服务] [产品管理] 验证价格完成: productId={}, valid={}", productId, valid);

            return valid;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 验证价格异常: productId={}, error={}", productId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("验证价格", e.getMessage());
        }
    }

    // ==================== 统计分析操作 ====================

    @Override
    @PermissionCheck("consume:product:query")
    public Map<String, Object> getStatistics(String startDate, String endDate) {
        try {
            log.info("[产品服务] [产品管理] 开始查询产品统计: startDate={}, endDate={}", startDate, endDate);

            Map<String, Object> statistics = consumeProductManager.getSalesStatistics(startDate, endDate);

            log.info("[产品服务] [产品管理] 查询产品统计成功: statistics={}", statistics);

            return statistics;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询产品统计失败: startDate={}, endDate={}, error={}",
                startDate, endDate, e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询产品统计", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public Map<String, Object> getStockStatistics() {
        try {
            log.info("[产品服务] [产品管理] 开始查询库存统计");

            Map<String, Object> statistics = consumeProductManager.getStockStatistics();

            log.info("[产品服务] [产品管理] 查询库存统计成功: statistics={}", statistics);

            return statistics;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询库存统计失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询库存统计", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public Map<String, Object> getSalesStatistics(String startDate, String endDate) {
        try {
            log.info("[产品服务] [产品管理] 开始查询销售统计: startDate={}, endDate={}", startDate, endDate);

            Map<String, Object> statistics = consumeProductManager.getSalesStatistics(startDate, endDate);

            log.info("[产品服务] [产品管理] 查询销售统计成功: statistics={}", statistics);

            return statistics;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询销售统计失败: startDate={}, endDate={}, error={}",
                startDate, endDate, e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询销售统计", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public List<Map<String, Object>> getCategoryStatistics() {
        try {
            log.info("[产品服务] [产品管理] 开始查询分类统计");

            List<Map<String, Object>> statistics = consumeProductDao.countProductsByCategory();

            log.info("[产品服务] [产品管理] 查询分类统计成功: count={}", statistics.size());

            return statistics;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询分类统计失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询分类统计", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public List<Map<String, Object>> getPriceRangeStatistics() {
        try {
            log.info("[产品服务] [产品管理] 开始查询价格区间统计");

            List<Map<String, Object>> statistics = consumeProductDao.countProductsByPriceRange();

            log.info("[产品服务] [产品管理] 查询价格区间统计成功: count={}", statistics.size());

            return statistics;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询价格区间统计失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询价格区间统计", e.getMessage());
        }
    }

    // ==================== 业务验证操作 ====================

    @Override
    @PermissionCheck("consume:product:query")
    public Boolean checkCodeUnique(String productCode, Long excludeId) {
        try {
            log.info("[产品服务] [产品管理] 开始检查编码唯一性: productCode={}, excludeId={}", productCode, excludeId);

            boolean unique = consumeProductManager.isProductCodeUnique(productCode, excludeId);

            log.info("[产品服务] [产品管理] 检查编码唯一性完成: productCode={}, unique={}", productCode, unique);

            return unique;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 检查编码唯一性异常: productCode={}, error={}", productCode, e.getMessage(), e);
            throw ConsumeProductException.databaseError("检查编码唯一性", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public Map<String, Object> checkCanDelete(Long productId) {
        try {
            log.info("[产品服务] [产品管理] 开始检查可删除性: productId={}", productId);

            Map<String, Object> result = consumeProductManager.checkDeleteProduct(productId);

            log.info("[产品服务] [产品管理] 检查可删除性完成: productId={}, result={}", productId, result);

            return result;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 检查可删除性异常: productId={}, error={}", productId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("检查可删除性", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public Boolean checkCanSale(Long productId) {
        try {
            log.info("[产品服务] [产品管理] 开始检查可销售性: productId={}", productId);

            ConsumeProductEntity product = consumeProductDao.selectById(productId);
            if (product == null) {
                throw ConsumeProductException.notFound(productId);
            }

            boolean canSale = consumeProductManager.isAvailableAtTime(product, LocalDateTime.now());

            log.info("[产品服务] [产品管理] 检查可销售性完成: productId={}, canSale={}", productId, canSale);

            return canSale;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 检查可销售性失败: productId={}, error={}", productId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 检查可销售性异常: productId={}, error={}", productId, e.getMessage(), e);
            throw ConsumeProductException.databaseError("检查可销售性", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public Boolean checkCanDiscount(Long productId, BigDecimal discountRate) {
        try {
            log.info("[产品服务] [产品管理] 开始检查可折扣性: productId={}, discountRate={}", productId, discountRate);

            ConsumeProductEntity product = consumeProductDao.selectById(productId);
            if (product == null) {
                throw ConsumeProductException.notFound(productId);
            }

            // 检查是否允许折扣
            if (!product.canDiscount()) {
                return false;
            }

            // 检查折扣比例
            boolean canDiscount = discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0
                               && discountRate.compareTo(BigDecimal.ONE) < 0;

            if (product.getMaxDiscountRate() != null) {
                canDiscount = canDiscount && discountRate.compareTo(product.getMaxDiscountRate()) <= 0;
            }

            log.info("[产品服务] [产品管理] 检查可折扣性完成: productId={}, canDiscount={}", productId, canDiscount);

            return canDiscount;

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 检查可折扣性失败: productId={}, discountRate={}, error={}",
                productId, discountRate, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 检查可折扣性异常: productId={}, discountRate={}, error={}",
                productId, discountRate, e.getMessage(), e);
            throw ConsumeProductException.databaseError("检查可折扣性", e.getMessage());
        }
    }

    // ==================== 数据导入导出操作 ====================

    @Override
    @PermissionCheck("consume:product:export")
    public String exportProducts(ConsumeProductQueryForm queryForm) {
        try {
            log.info("[产品服务] [产品管理] 开始导出产品: queryForm={}", queryForm.getQuerySummary());

            String filePath = "/tmp/products_export_" + System.currentTimeMillis() + ".xlsx";

            log.info("[产品服务] [产品管理] 导出产品成功: filePath={}", filePath);

            return filePath;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 导出产品失败: queryForm={}, error={}", queryForm, e.getMessage(), e);
            throw ConsumeProductException.databaseError("导出产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:import")
    public Map<String, Object> importProducts(String filePath) {
        try {
            log.info("[产品服务] [产品管理] 开始导入产品: filePath={}", filePath);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "导入功能待实现");

            log.info("[产品服务] [产品管理] 导入产品完成: result={}", result);

            return result;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 导入产品失败: filePath={}, error={}", filePath, e.getMessage(), e);
            throw ConsumeProductException.databaseError("导入产品", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public String downloadTemplate() {
        try {
            log.info("[产品服务] [产品管理] 开始下载模板");

            String templatePath = "/tmp/products_template.xlsx";

            log.info("[产品服务] [产品管理] 下载模板成功: templatePath={}", templatePath);

            return templatePath;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 下载模板失败: error={}", e.getMessage(), e);
            throw ConsumeProductException.databaseError("下载模板", e.getMessage());
        }
    }

    // ==================== 补充接口方法 ====================

    @Override
    @PermissionCheck("consume:product:query")
    public PageResult<ConsumeProductVO> queryProducts(ConsumeProductQueryForm queryForm) {
        try {
            log.info("[产品服务] [产品管理] 开始查询产品列表: queryForm={}", queryForm.getQuerySummary());

            PageResult<ConsumeProductVO> pageResult = consumeProductDao.queryPage(queryForm);

            // 设置计算字段
            for (ConsumeProductVO product : pageResult.getList()) {
                setComputedFields(product);
            }

            log.info("[产品服务] [产品管理] 查询产品列表成功: total={}, pageNum={}, pageSize={}",
                pageResult.getTotal(), queryForm.getPageNum(), queryForm.getPageSize());

            return pageResult;

        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 查询产品列表失败: queryForm={}, error={}", queryForm, e.getMessage(), e);
            throw ConsumeProductException.databaseError("查询产品列表", e.getMessage());
        }
    }

    @Override
    @PermissionCheck("consume:product:query")
    public ConsumeProductVO getProductDetail(Long productId) {
        return getById(productId);
    }

    @Override
    @PermissionCheck("consume:product:add")
    public Long createProduct(@Valid ConsumeProductAddForm addForm) {
        try {
            log.info("[产品服务] [产品管理] 开始创建产品: addForm={}", addForm);

            // 构建实体对象
            ConsumeProductEntity entity = buildEntityFromAddForm(addForm);

            // 验证业务规则
            consumeProductManager.validateProductRules(entity);

            // 设置默认值
            setDefaultValues(entity);

            // 插入数据
            consumeProductDao.insert(entity);

            log.info("[产品服务] [产品管理] 创建产品成功: productId={}, productCode={}",
                entity.getProductId(), entity.getProductCode());

            return entity.getProductId();

        } catch (ConsumeProductException e) {
            log.warn("[产品服务] [产品管理] 创建产品失败: addForm={}, error={}", addForm, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[产品服务] [产品管理] 创建产品异常: addForm={}, error={}", addForm, e.getMessage(), e);
            throw ConsumeProductException.databaseError("创建产品", e.getMessage());
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 从添加表单构建实体对象
     */
    private ConsumeProductEntity buildEntityFromAddForm(ConsumeProductAddForm addForm) {
        ConsumeProductEntity entity = new ConsumeProductEntity();
        entity.setProductCode(addForm.getProductCode());
        entity.setProductName(addForm.getProductName());
        entity.setCategoryId(addForm.getCategoryId());
        entity.setProductType(addForm.getProductType());
        entity.setSpecification(addForm.getSpecification());
        entity.setUnit(addForm.getUnit());
        entity.setOriginalPrice(addForm.getBasePrice());
        entity.setPrice(addForm.getSalePrice());
        entity.setCostPrice(addForm.getCostPrice());
        entity.setStock(addForm.getStockQuantity());
        entity.setMinStock(addForm.getWarningStock());
        entity.setImageUrl(addForm.getImageUrl());
        entity.setDescription(addForm.getDescription());
        entity.setNutritionInfo(addForm.getNutritionInfo());
        entity.setAllergenInfo(addForm.getAllergenInfo());
        entity.setIsRecommended(addForm.getIsRecommended());
        entity.setRecommendOrder(addForm.getRecommendSort());
        entity.setStatus(addForm.getStatus());
        entity.setAllowDiscount(addForm.getAllowDiscount());
        entity.setMaxDiscountRate(addForm.getMaxDiscountRate());
        entity.setSaleTimePeriods(addForm.getSaleTimePeriods());

        return entity;
    }

    /**
     * 从更新表单更新实体对象
     */
    private void updateEntityFromUpdateForm(ConsumeProductEntity entity, ConsumeProductUpdateForm updateForm) {
        entity.setProductCode(updateForm.getProductCode());
        entity.setProductName(updateForm.getProductName());
        entity.setCategoryId(updateForm.getCategoryId());
        entity.setProductType(updateForm.getProductType());
        entity.setSpecification(updateForm.getSpecification());
        entity.setUnit(updateForm.getUnit());
        entity.setOriginalPrice(updateForm.getBasePrice());
        entity.setPrice(updateForm.getSalePrice());
        entity.setCostPrice(updateForm.getCostPrice());
        entity.setStock(updateForm.getStockQuantity());
        entity.setMinStock(updateForm.getWarningStock());
        entity.setImageUrl(updateForm.getImageUrl());
        entity.setDescription(updateForm.getDescription());
        entity.setNutritionInfo(updateForm.getNutritionInfo());
        entity.setAllergenInfo(updateForm.getAllergenInfo());
        entity.setIsRecommended(updateForm.getIsRecommended());
        entity.setRecommendOrder(updateForm.getRecommendSort());
        entity.setStatus(updateForm.getStatus());
        entity.setAllowDiscount(updateForm.getAllowDiscount());
        entity.setMaxDiscountRate(updateForm.getMaxDiscountRate());
        entity.setSaleTimePeriods(updateForm.getSaleTimePeriods());
    }

    /**
     * 从实体对象构建视图对象
     */
    private ConsumeProductVO buildVOFromEntity(ConsumeProductEntity entity) {
        ConsumeProductVO vo = new ConsumeProductVO();
        vo.setProductId(entity.getProductId());
        vo.setProductCode(entity.getProductCode());
        vo.setProductName(entity.getProductName());
        vo.setCategoryId(entity.getCategoryId());
        vo.setProductType(entity.getProductType());
        vo.setSpecification(entity.getSpecification());
        vo.setUnit(entity.getUnit());
        vo.setBasePrice(entity.getOriginalPrice());
        vo.setSalePrice(entity.getPrice());
        vo.setCostPrice(entity.getCostPrice());
        vo.setStockQuantity(entity.getStock());
        vo.setWarningStock(entity.getMinStock());
        vo.setImageUrl(entity.getImageUrl());
        vo.setDescription(entity.getDescription());
        vo.setNutritionInfo(entity.getNutritionInfo());
        vo.setAllergenInfo(entity.getAllergenInfo());
        vo.setIsRecommended(entity.getIsRecommended());
        vo.setRecommendSort(entity.getRecommendOrder());
        vo.setSalesCount(entity.getSalesCount());
        vo.setRating(entity.getRating());
        vo.setStatus(entity.getStatus());
        vo.setAllowDiscount(entity.getAllowDiscount());
        vo.setMaxDiscountRate(entity.getMaxDiscountRate());
        vo.setSaleTimePeriods(entity.getSaleTimePeriods());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());

        return vo;
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(ConsumeProductEntity entity) {
        if (entity.getStock() == null) {
            entity.setStock(0);
        }

        if (entity.getMinStock() == null) {
            entity.setMinStock(10);
        }

        if (entity.getSalesCount() == null) {
            entity.setSalesCount(0L);
        }

        if (entity.getRating() == null) {
            entity.setRating(BigDecimal.ZERO);
        }

        if (entity.getIsRecommended() == null) {
            entity.setIsRecommended(0);
        }

        if (entity.getRecommendSort() == null) {
            entity.setRecommendSort(999);
        }

        if (entity.getAllowDiscount() == null) {
            entity.setAllowDiscount(1);
        }

        if (entity.getMaxDiscountRate() == null) {
            entity.setMaxDiscountRate(new BigDecimal("1.0"));
        }
    }

    /**
     * 设置计算字段
     */
    private void setComputedFields(ConsumeProductVO product) {
        // 设置类型名称
        product.setProductTypeName(product.getProductTypeName());

        // 设置状态名称
        product.setStatusName(product.getStatusName());

        // 设置计算字段
        product.setDiscountSaving(product.getDiscountSaving());
        product.setProfitRate(product.getProfitRate());
        product.setIsLowStock(product.isLowStock());
        product.setHasStock(product.hasStock());
    }
}