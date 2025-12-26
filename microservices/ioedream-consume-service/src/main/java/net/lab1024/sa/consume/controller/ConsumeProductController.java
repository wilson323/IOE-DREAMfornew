package net.lab1024.sa.consume.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.consume.domain.form.ConsumeProductAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeProductQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeProductUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeProductVO;
import net.lab1024.sa.consume.service.ConsumeProductService;

/**
 * 消费产品管理控制器
 * <p>
 * 提供消费产品的管理功能，包括：
 * 1. 产品基本信息管理
 * 2. 产品价格管理
 * 3. 产品库存管理
 * 4. 产品分类管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_PRODUCT_MANAGE", description = "消费产品管理权限")
@RequestMapping("/api/v1/consume/products")
@Tag(name = "消费产品管理", description = "消费产品管理、价格、库存、分类等功能")
public class ConsumeProductController {

    @Resource
    private ConsumeProductService consumeProductService;

    /**
     * 分页查询消费产品列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @GetMapping("/query")
    @Operation(summary = "分页查询消费产品", description = "根据条件分页查询消费产品列表")
    public ResponseDTO<PageResult<ConsumeProductVO>> queryProducts(@ModelAttribute ConsumeProductQueryForm queryForm) {
        log.info("[产品管理] 分页查询消费产品: queryForm={}", queryForm);
        PageResult<ConsumeProductVO> result = consumeProductService.queryProducts(queryForm);
        log.info("[产品管理] 分页查询消费产品成功: totalCount={}", result.getTotal());
        return ResponseDTO.ok(result);
    }

    /**
     * 获取消费产品详情
     *
     * @param productId 产品ID
     * @return 产品详情
     */
    @GetMapping("/{productId}")
    @Operation(summary = "获取消费产品详情", description = "根据产品ID获取详细的产品信息")
    public ResponseDTO<ConsumeProductVO> getProductDetail(
            @Parameter(description = "产品ID", required = true) @PathVariable Long productId) {
        ConsumeProductVO product = consumeProductService.getProductDetail(productId);
        return ResponseDTO.ok(product);
    }

    /**
     * 新增消费产品
     *
     * @param addForm 产品新增表单
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增消费产品", description = "创建新的消费产品")
    public ResponseDTO<Long> addProduct(@Valid @RequestBody ConsumeProductAddForm addForm) {
        Long productId = consumeProductService.createProduct(addForm);
        return ResponseDTO.ok(productId);
    }

    /**
     * 更新消费产品
     *
     * @param productId  产品ID
     * @param updateForm 更新表单
     * @return 更新结果
     */
    @PutMapping("/{productId}")
    @Operation(summary = "更新消费产品", description = "更新消费产品的基本信息")
    public ResponseDTO<Void> updateProduct(
            @Parameter(description = "产品ID", required = true) @PathVariable Long productId,
            @Valid @RequestBody ConsumeProductUpdateForm updateForm) {
        updateForm.setProductId(productId);
        consumeProductService.update(updateForm);
        return ResponseDTO.ok();
    }

    /**
     * 删除消费产品
     *
     * @param productId 产品ID
     * @return 删除结果
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "删除消费产品", description = "删除指定的消费产品")
    public ResponseDTO<Void> deleteProduct(
            @Parameter(description = "产品ID", required = true) @PathVariable Long productId) {
        consumeProductService.delete(productId);
        return ResponseDTO.ok();
    }

    /**
     * 获取产品分类列表
     *
     * @return 产品分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取产品分类", description = "获取所有产品分类列表")
    public ResponseDTO<List<String>> getProductCategories() {
        // 暂时返回硬编码的分类，后续可从分类服务获取
        List<String> categories = List.of("主食", "饮品", "小吃", "套餐", "水果");
        return ResponseDTO.ok(categories);
    }

    /**
     * 根据分类获取产品列表
     *
     * @param category 产品分类
     * @return 产品列表
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类获取产品", description = "根据分类ID获取产品列表")
    public ResponseDTO<List<ConsumeProductVO>> getProductsByCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable Long categoryId) {
        List<ConsumeProductVO> products = consumeProductService.getByCategoryId(categoryId);
        return ResponseDTO.ok(products);
    }

    /**
     * 获取热门产品列表
     *
     * @param limit 数量限制
     * @return 热门产品列表
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门产品", description = "获取热门消费产品列表")
    public ResponseDTO<List<ConsumeProductVO>> getPopularProducts(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        List<ConsumeProductVO> products = consumeProductService.getHotSales(limit);
        return ResponseDTO.ok(products);
    }

    /**
     * 获取可用产品列表
     *
     * @return 可用产品列表
     */
    @GetMapping("/available")
    @Operation(summary = "获取可用产品", description = "获取当前可用的消费产品列表")
    public ResponseDTO<List<ConsumeProductVO>> getAvailableProducts() {
        List<ConsumeProductVO> products = consumeProductService.getAllOnSale();
        return ResponseDTO.ok(products);
    }

    /**
     * 更新产品价格
     *
     * @param productId 产品ID
     * @param price     新价格
     * @return 更新结果
     */
    @PutMapping("/{productId}/price")
    @Operation(summary = "更新产品价格", description = "更新指定产品的价格")
    public ResponseDTO<Void> updateProductPrice(
            @Parameter(description = "产品ID", required = true) @PathVariable Long productId,
            @Parameter(description = "新价格", required = true) @RequestParam BigDecimal price) {
        return ResponseDTO.error("501", "产品价格更新功能开发中");
    }

    /**
     * 更新产品库存
     *
     * @param productId 产品ID
     * @param stock     库存数量
     * @return 更新结果
     */
    @PutMapping("/{productId}/stock")
    @Operation(summary = "更新产品库存", description = "更新指定产品的库存数量")
    public ResponseDTO<Void> updateProductStock(
            @Parameter(description = "产品ID", required = true) @PathVariable Long productId,
            @Parameter(description = "库存数量", required = true) @RequestParam Integer stock) {
        return ResponseDTO.error("501", "产品库存更新功能开发中");
    }

    /**
     * 产品上架
     *
     * @param productId 产品ID
     * @return 上架结果
     */
    @PutMapping("/{productId}/enable")
    @Operation(summary = "产品上架", description = "将指定产品设置为上架状态")
    public ResponseDTO<Void> enableProduct(
            @Parameter(description = "产品ID", required = true) @PathVariable Long productId) {
        consumeProductService.putOnSale(productId);
        return ResponseDTO.ok();
    }

    /**
     * 产品下架
     *
     * @param productId 产品ID
     * @return 下架结果
     */
    @PutMapping("/{productId}/disable")
    @Operation(summary = "产品下架", description = "将指定产品设置为下架状态")
    public ResponseDTO<Void> disableProduct(
            @Parameter(description = "产品ID", required = true) @PathVariable Long productId) {
        consumeProductService.putOffSale(productId);
        return ResponseDTO.ok();
    }

    /**
     * 获取产品统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取产品统计信息", description = "获取消费产品的统计信息")
    public ResponseDTO<java.util.Map<String, Object>> getProductStatistics() {
        // 获取当前日期范围的统计数据
        String startDate = java.time.LocalDate.now().minusMonths(1).toString();
        String endDate = java.time.LocalDate.now().toString();
        java.util.Map<String, Object> statistics = consumeProductService.getStatistics(startDate, endDate);
        return ResponseDTO.ok(statistics);
    }

    /**
     * 搜索产品
     *
     * @param keyword 搜索关键词
     * @param limit   数量限制
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索产品", description = "根据关键词搜索消费产品")
    public ResponseDTO<List<ConsumeProductVO>> searchProducts(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "20") Integer limit) {
        List<ConsumeProductVO> products = consumeProductService.searchProducts(keyword, limit);
        return ResponseDTO.ok(products);
    }

    /**
     * 批量操作产品
     *
     * @param productIds 产品ID列表
     * @param operation  操作类型
     * @return 操作结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量操作产品", description = "对多个产品执行批量操作")
    public ResponseDTO<Void> batchOperateProducts(
            @Parameter(description = "产品ID列表", required = true) @RequestParam List<Long> productIds,
            @Parameter(description = "操作类型", required = true) @RequestParam String operation) {
        // 根据操作类型映射到状态值：enable->1, disable->0
        Integer status = null;
        if ("enable".equalsIgnoreCase(operation)) {
            status = 1;
        } else if ("disable".equalsIgnoreCase(operation)) {
            status = 0;
        } else if ("delete".equalsIgnoreCase(operation)) {
            // 删除操作使用batchDelete方法
            consumeProductService.batchDelete(productIds);
            return ResponseDTO.ok();
        } else {
            return ResponseDTO.error("400", "不支持的操作类型: " + operation);
        }

        // 状态更新操作
        consumeProductService.batchUpdateStatus(productIds, status);
        return ResponseDTO.ok();
    }
}
