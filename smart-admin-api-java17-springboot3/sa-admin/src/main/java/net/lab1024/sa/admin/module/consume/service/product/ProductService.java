package net.lab1024.sa.admin.module.consume.service.product;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.ProductDao;
import net.lab1024.sa.admin.module.consume.dao.ProductCategoryDao;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.ProductCategoryEntity;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品服务
 * 支持产品管理、扫码消费等功能
 *
 * @author SmartAdmin
 * @since 2025-11-17
 */
@Slf4j
@Service
public class ProductService {

    @Resource
    private ProductDao productDao;

    @Resource
    private ProductCategoryDao productCategoryDao;

    /**
     * 根据条码查询产品
     *
     * @param barcode 产品条码
     * @return 产品信息
     */
    public ResponseDTO<ProductEntity> getProductByBarcode(String barcode) {
        try {
            ProductEntity product = productDao.selectByBarcode(barcode);
            if (product == null) {
                return ResponseDTO.error("产品不存在");
            }

            // 检查产品状态
            if (!"ACTIVE".equals(product.getStatus())) {
                return ResponseDTO.error("产品已下架");
            }

            // 检查库存
            if (product.getStock() != null && product.getStock() <= 0) {
                return ResponseDTO.error("产品库存不足");
            }

            return ResponseDTO.ok(product);
        } catch (Exception e) {
            log.error("根据条码查询产品异常, barcode: {}", barcode, e);
            return ResponseDTO.error("查询产品失败");
        }
    }

    /**
     * 根据二维码查询产品
     *
     * @param qrCode 产品二维码
     * @return 产品信息
     */
    public ResponseDTO<ProductEntity> getProductByQrCode(String qrCode) {
        try {
            ProductEntity product = productDao.selectByQrCode(qrCode);
            if (product == null) {
                return ResponseDTO.error("产品不存在");
            }

            // 检查产品状态
            if (!"ACTIVE".equals(product.getStatus())) {
                return ResponseDTO.error("产品已下架");
            }

            // 检查库存
            if (product.getStock() != null && product.getStock() <= 0) {
                return ResponseDTO.error("产品库存不足");
            }

            return ResponseDTO.ok(product);
        } catch (Exception e) {
            log.error("根据二维码查询产品异常, qrCode: {}", qrCode, e);
            return ResponseDTO.error("查询产品失败");
        }
    }

    /**
     * 根据产品ID查询产品
     *
     * @param productId 产品ID
     * @return 产品信息
     */
    public ResponseDTO<ProductEntity> getProductById(Long productId) {
        try {
            ProductEntity product = productDao.selectById(productId);
            if (product == null) {
                return ResponseDTO.error("产品不存在");
            }

            // 检查产品状态
            if (!"ACTIVE".equals(product.getStatus())) {
                return ResponseDTO.error("产品已下架");
            }

            // 检查库存
            if (product.getStock() != null && product.getStock() <= 0) {
                return ResponseDTO.error("产品库存不足");
            }

            return ResponseDTO.ok(product);
        } catch (Exception e) {
            log.error("根据ID查询产品异常, productId: {}", productId, e);
            return ResponseDTO.error("查询产品失败");
        }
    }

    /**
     * 扣减产品库存
     *
     * @param productId 产品ID
     * @param quantity 扣减数量
     * @return 扣减结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deductStock(Long productId, Integer quantity) {
        try {
            ProductEntity product = productDao.selectById(productId);
            if (product == null) {
                return ResponseDTO.error("产品不存在");
            }

            // 检查库存是否充足
            if (product.getStock() != null && product.getStock() < quantity) {
                return ResponseDTO.error("库存不足");
            }

            // 扣减库存
            int updateCount = productDao.deductStock(productId, quantity);
            if (updateCount <= 0) {
                return ResponseDTO.error("库存扣减失败");
            }

            log.info("产品库存扣减成功, productId: {}, quantity: {}, remainingStock: {}",
                    productId, quantity, product.getStock() - quantity);
            return ResponseDTO.ok("库存扣减成功");

        } catch (Exception e) {
            log.error("扣减产品库存异常, productId: {}, quantity: {}", productId, quantity, e);
            return ResponseDTO.error("库存扣减失败");
        }
    }

    /**
     * 恢复产品库存
     *
     * @param productId 产品ID
     * @param quantity 恢复数量
     * @return 恢复结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> restoreStock(Long productId, Integer quantity) {
        try {
            // 恢复库存
            int updateCount = productDao.restoreStock(productId, quantity);
            if (updateCount <= 0) {
                return ResponseDTO.error("库存恢复失败");
            }

            log.info("产品库存恢复成功, productId: {}, quantity: {}", productId, quantity);
            return ResponseDTO.ok("库存恢复成功");

        } catch (Exception e) {
            log.error("恢复产品库存异常, productId: {}, quantity: {}", productId, quantity, e);
            return ResponseDTO.error("库存恢复失败");
        }
    }

    /**
     * 分页查询产品
     *
     * @param queryParam 查询参数
     * @return 分页结果
     */
    public ResponseDTO<PageResult<ProductEntity>> queryProduct(ProductQueryParam queryParam) {
        try {
            // 查询产品列表
            List<ProductEntity> productList = productDao.queryProduct(queryParam);

            // 查询总数
            Integer totalCount = productDao.queryProductCount(queryParam);

            // 构建分页结果
            PageResult<ProductEntity> pageResult = new PageResult<>();
            pageResult.setRows(productList);
            pageResult.setTotal(totalCount);

            return ResponseDTO.ok(pageResult);
        } catch (Exception e) {
            log.error("分页查询产品异常", e);
            return ResponseDTO.error("查询产品列表失败");
        }
    }

    /**
     * 根据分类查询产品
     *
     * @param categoryId 分类ID
     * @return 产品列表
     */
    public ResponseDTO<List<ProductEntity>> getProductByCategory(Long categoryId) {
        try {
            List<ProductEntity> productList = productDao.selectByCategoryId(categoryId);
            return ResponseDTO.ok(productList);
        } catch (Exception e) {
            log.error("根据分类查询产品异常, categoryId: {}", categoryId, e);
            return ResponseDTO.error("查询产品失败");
        }
    }

    /**
     * 搜索产品
     *
     * @param keyword 关键词
     * @return 产品列表
     */
    public ResponseDTO<List<ProductEntity>> searchProduct(String keyword) {
        try {
            if (SmartStringUtil.isEmpty(keyword)) {
                return ResponseDTO.error("搜索关键词不能为空");
            }

            List<ProductEntity> productList = productDao.searchProduct(keyword);
            return ResponseDTO.ok(productList);
        } catch (Exception e) {
            log.error("搜索产品异常, keyword: {}", keyword, e);
            return ResponseDTO.error("搜索产品失败");
        }
    }

    /**
     * 获取热销产品
     *
     * @param limit 限制数量
     * @return 热销产品列表
     */
    public ResponseDTO<List<ProductEntity>> getHotProducts(Integer limit) {
        try {
            List<ProductEntity> hotProducts = productDao.selectHotProducts(limit);
            return ResponseDTO.ok(hotProducts);
        } catch (Exception e) {
            log.error("获取热销产品异常", e);
            return ResponseDTO.error("获取热销产品失败");
        }
    }

    /**
     * 获取产品分类列表
     *
     * @return 分类列表
     */
    public ResponseDTO<List<ProductCategoryEntity>> getProductCategories() {
        try {
            List<ProductCategoryEntity> categories = productCategoryDao.selectActiveCategories();
            return ResponseDTO.ok(categories);
        } catch (Exception e) {
            log.error("获取产品分类异常", e);
            return ResponseDTO.error("获取产品分类失败");
        }
    }

    /**
     * 计算产品总价（含折扣）
     *
     * @param productId 产品ID
     * @param quantity 数量
     * @param discountRate 折扣率
     * @return 总价
     */
    public ResponseDTO<BigDecimal> calculateProductPrice(Long productId, Integer quantity, BigDecimal discountRate) {
        try {
            ProductEntity product = productDao.selectById(productId);
            if (product == null) {
                return ResponseDTO.error("产品不存在");
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal originalTotal = unitPrice.multiply(new BigDecimal(quantity));

            BigDecimal finalTotal = originalTotal;
            if (discountRate != null && discountRate.compareTo(BigDecimal.ZERO) > 0) {
                finalTotal = originalTotal.multiply(discountRate);
            }

            return ResponseDTO.ok(finalTotal);
        } catch (Exception e) {
            log.error("计算产品价格异常, productId: {}, quantity: {}", productId, quantity, e);
            return ResponseDTO.error("计算价格失败");
        }
    }

    /**
     * 查询产品统计信息
     *
     * @return 统计信息
     */
    public ResponseDTO<Map<String, Object>> getProductStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 总产品数
            Integer totalProducts = productDao.countByStatus("ACTIVE");
            statistics.put("totalProducts", totalProducts);

            // 总分类数
            Integer totalCategories = productCategoryDao.countActiveCategories();
            statistics.put("totalCategories", totalCategories);

            // 低库存产品数
            Integer lowStockProducts = productDao.countLowStockProducts();
            statistics.put("lowStockProducts", lowStockProducts);

            // 热销产品数
            Integer hotProducts = productDao.countHotProducts();
            statistics.put("hotProducts", hotProducts);

            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("查询产品统计信息异常", e);
            return ResponseDTO.error("查询统计信息失败");
        }
    }

    /**
     * 产品查询参数
     */
    public static class ProductQueryParam {
        private String productName;
        private String barcode;
        private Long categoryId;
        private String status;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Integer pageNum;
        private Integer pageSize;
        // getters and setters
    }
}