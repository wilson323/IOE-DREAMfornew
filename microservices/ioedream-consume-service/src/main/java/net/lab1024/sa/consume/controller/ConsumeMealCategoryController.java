package net.lab1024.sa.consume.controller;

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
import net.lab1024.sa.consume.domain.form.ConsumeMealCategoryAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeMealCategoryQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeMealCategoryUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeMealCategoryVO;
import net.lab1024.sa.consume.service.ConsumeMealCategoryService;
import lombok.extern.slf4j.Slf4j;

/**
 * 消费餐别管理控制器
 * <p>
 * 提供消费餐别的管理功能，包括：
 * 1. 餐别基本信息管理
 * 2. 餐别时间设置
 * 3. 餐别价格管理
 * 4. 餐别状态管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@RestController
@PermissionCheck(value = "CONSUME_MEAL_CATEGORY_MANAGE", description = "消费餐别管理权限")
@RequestMapping("/api/v1/consume/meal-categories")
@Tag(name = "消费餐别管理", description = "消费餐别管理、时间、价格等功能")
public class ConsumeMealCategoryController {

    @Resource
    private ConsumeMealCategoryService consumeMealCategoryService;

    /**
     * 分页查询餐别列表
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询餐别", description = "根据条件分页查询消费餐别列表")
    public ResponseDTO<PageResult<ConsumeMealCategoryVO>> queryMealCategories(
            @ModelAttribute ConsumeMealCategoryQueryForm queryForm) {
        PageResult<ConsumeMealCategoryVO> result = consumeMealCategoryService.queryMealCategories(queryForm);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取餐别详情
     *
     * @param categoryId 餐别ID
     * @return 餐别详情
     */
    @GetMapping("/{categoryId}")
    @Operation(summary = "获取餐别详情", description = "根据餐别ID获取详细的餐别信息")
    public ResponseDTO<ConsumeMealCategoryVO> getMealCategoryDetail(
            @Parameter(description = "餐别ID", required = true) @PathVariable Long categoryId) {
        ConsumeMealCategoryVO category = consumeMealCategoryService.getMealCategoryDetail(categoryId);
        return ResponseDTO.ok(category);
    }

    /**
     * 新增餐别
     *
     * @param addForm 餐别新增表单
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增餐别", description = "创建新的消费餐别")
    public ResponseDTO<Long> addMealCategory(@Valid @RequestBody ConsumeMealCategoryAddForm addForm) {
        Long categoryId = consumeMealCategoryService.createMealCategory(addForm);
        return ResponseDTO.ok(categoryId);
    }

    /**
     * 更新餐别
     *
     * @param categoryId 餐别ID
     * @param updateForm 更新表单
     * @return 更新结果
     */
    @PutMapping("/{categoryId}")
    @Operation(summary = "更新餐别", description = "更新消费餐别的基本信息")
    public ResponseDTO<Void> updateMealCategory(
            @Parameter(description = "餐别ID", required = true) @PathVariable Long categoryId,
            @Valid @RequestBody ConsumeMealCategoryUpdateForm updateForm) {
        consumeMealCategoryService.updateMealCategory(categoryId, updateForm);
        return ResponseDTO.ok();
    }

    /**
     * 删除餐别
     *
     * @param categoryId 餐别ID
     * @return 删除结果
     */
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "删除餐别", description = "删除指定的消费餐别")
    public ResponseDTO<Void> deleteMealCategory(
            @Parameter(description = "餐别ID", required = true) @PathVariable Long categoryId) {
        consumeMealCategoryService.deleteMealCategory(categoryId);
        return ResponseDTO.ok();
    }

    /**
     * 获取当前可用餐别
     *
     * @return 可用餐别列表
     */
    @GetMapping("/available")
    @Operation(summary = "获取当前可用餐别", description = "获取当前时间段可用的消费餐别")
    public ResponseDTO<List<ConsumeMealCategoryVO>> getAvailableMealCategories() {
        List<ConsumeMealCategoryVO> categories = consumeMealCategoryService.getAvailableMealCategories();
        return ResponseDTO.ok(categories);
    }

    /**
     * 获取所有餐别列表
     *
     * @return 餐别列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有餐别", description = "获取所有的消费餐别列表")
    public ResponseDTO<List<ConsumeMealCategoryVO>> getAllMealCategories() {
        List<ConsumeMealCategoryVO> categories = consumeMealCategoryService.getAllMealCategories();
        return ResponseDTO.ok(categories);
    }

    /**
     * 启用餐别
     *
     * @param categoryId 餐别ID
     * @return 启用结果
     */
    @PutMapping("/{categoryId}/enable")
    @Operation(summary = "启用餐别", description = "启用指定的消费餐别")
    public ResponseDTO<Void> enableMealCategory(
            @Parameter(description = "餐别ID", required = true) @PathVariable Long categoryId) {
        consumeMealCategoryService.enableMealCategory(categoryId);
        return ResponseDTO.ok();
    }

    /**
     * 禁用餐别
     *
     * @param categoryId 餐别ID
     * @return 禁用结果
     */
    @PutMapping("/{categoryId}/disable")
    @Operation(summary = "禁用餐别", description = "禁用指定的消费餐别")
    public ResponseDTO<Void> disableMealCategory(
            @Parameter(description = "餐别ID", required = true) @PathVariable Long categoryId) {
        consumeMealCategoryService.disableMealCategory(categoryId);
        return ResponseDTO.ok();
    }

    /**
     * 获取默认餐别
     *
     * @return 默认餐别
     */
    @GetMapping("/default")
    @Operation(summary = "获取默认餐别", description = "获取系统设置的默认消费餐别")
    public ResponseDTO<ConsumeMealCategoryVO> getDefaultMealCategory() {
        ConsumeMealCategoryVO category = consumeMealCategoryService.getDefaultMealCategory();
        return ResponseDTO.ok(category);
    }

    /**
     * 设置默认餐别
     *
     * @param categoryId 餐别ID
     * @return 设置结果
     */
    @PutMapping("/{categoryId}/default")
    @Operation(summary = "设置默认餐别", description = "设置指定的餐别为默认餐别")
    public ResponseDTO<Void> setDefaultMealCategory(
            @Parameter(description = "餐别ID", required = true) @PathVariable Long categoryId) {
        consumeMealCategoryService.setDefaultMealCategory(categoryId);
        return ResponseDTO.ok();
    }

    /**
     * 批量设置餐别价格
     *
     * @param categoryId   餐别ID
     * @param basePrice    基础价格
     * @param staffPrice   员工价格
     * @param studentPrice 学生价格
     * @return 设置结果
     */
    @PutMapping("/{categoryId}/prices")
    @Operation(summary = "设置餐别价格", description = "设置餐别的不同类型价格")
    public ResponseDTO<Void> setMealCategoryPrices(
            @Parameter(description = "餐别ID", required = true) @PathVariable Long categoryId,
            @Parameter(description = "基础价格", required = true) @RequestParam java.math.BigDecimal basePrice,
            @Parameter(description = "员工价格", required = true) @RequestParam java.math.BigDecimal staffPrice,
            @Parameter(description = "学生价格", required = true) @RequestParam java.math.BigDecimal studentPrice) {
        consumeMealCategoryService.setMealCategoryPrices(categoryId, basePrice, staffPrice, studentPrice);
        return ResponseDTO.ok();
    }

    /**
     * 获取餐别统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取餐别统计信息", description = "获取消费餐别的统计信息")
    public ResponseDTO<java.util.Map<String, Object>> getMealCategoryStatistics() {
        java.util.Map<String, Object> statistics = consumeMealCategoryService.getMealCategoryStatistics();
        return ResponseDTO.ok(statistics);
    }

    /**
     * 复制餐别
     *
     * @param categoryId 餐别ID
     * @param newName    新餐别名称
     * @return 复制结果
     */
    @PostMapping("/{categoryId}/copy")
    @Operation(summary = "复制餐别", description = "复制指定的餐别创建新的餐别")
    public ResponseDTO<Long> copyMealCategory(
            @Parameter(description = "餐别ID", required = true) @PathVariable Long categoryId,
            @Parameter(description = "新餐别名称", required = true) @RequestParam String newName) {
        Long newCategoryId = consumeMealCategoryService.copyMealCategory(categoryId, newName);
        return ResponseDTO.ok(newCategoryId);
    }

    /**
     * 批量操作餐别
     *
     * @param categoryIds 餐别ID列表
     * @param operation   操作类型
     * @return 操作结果
     */
    @PostMapping("/batch")
    @Operation(summary = "批量操作餐别", description = "对多个餐别执行批量操作")
    public ResponseDTO<Void> batchOperateMealCategories(
            @Parameter(description = "餐别ID列表", required = true) @RequestParam List<Long> categoryIds,
            @Parameter(description = "操作类型", required = true) @RequestParam String operation) {
        consumeMealCategoryService.batchOperateMealCategories(categoryIds, operation);
        return ResponseDTO.ok();
    }
}
