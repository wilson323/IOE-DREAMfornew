package net.lab1024.sa.consume.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.consume.MealMenuEntity;
import net.lab1024.sa.consume.service.MealMenuService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;

/**
 * 菜品管理控制器 - 菜品管理REST API
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/meal/menu")
@Tag(name = "菜品管理")
public class MealMenuController {

    @Resource
    private MealMenuService mealMenuService;

    /**
     * 查询可用菜品列表（分页）
     *
     * @param orderDate 订餐日期
     * @param mealType 餐别（1-早餐 2-午餐 3-晚餐）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 菜品分页数据
     */
    @GetMapping("/available")
    @Operation(summary = "查询可用菜品列表")
    public ResponseDTO<PageResult<MealMenuEntity>> getAvailableMenus(
            @RequestParam LocalDate orderDate,
            @RequestParam Integer mealType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("[菜品管理] 查询可用菜品: orderDate={}, mealType={}", orderDate, mealType);

        Page<MealMenuEntity> page = mealMenuService.getAvailableMenus(orderDate, mealType, pageNum, pageSize);

        return ResponseDTO.ok(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 新增菜品
     *
     * @param menu 菜品实体
     * @return 菜品ID
     */
    @PostMapping
    @Operation(summary = "新增菜品")
    public ResponseDTO<Long> addMenu(@RequestBody MealMenuEntity menu) {
        log.info("[菜品管理] 新增菜品: menuName={}", menu.getMenuName());
        Long menuId = mealMenuService.addMenu(menu);
        return ResponseDTO.ok(menuId);
    }

    /**
     * 更新菜品
     *
     * @param menu 菜品实体
     * @return 成功响应
     */
    @PutMapping
    @Operation(summary = "更新菜品")
    public ResponseDTO<Void> updateMenu(@RequestBody MealMenuEntity menu) {
        log.info("[菜品管理] 更新菜品: menuId={}", menu.getMenuId());
        mealMenuService.updateMenu(menu);
        return ResponseDTO.ok();
    }

    /**
     * 删除菜品
     *
     * @param menuId 菜品ID
     * @return 成功响应
     */
    @DeleteMapping("/{menuId}")
    @Operation(summary = "删除菜品")
    public ResponseDTO<Void> deleteMenu(@PathVariable Long menuId) {
        log.info("[菜品管理] 删除菜品: menuId={}", menuId);
        mealMenuService.deleteMenu(menuId);
        return ResponseDTO.ok();
    }

    /**
     * 上架菜品
     *
     * @param menuId 菜品ID
     * @return 成功响应
     */
    @PutMapping("/{menuId}/on-shelf")
    @Operation(summary = "上架菜品")
    public ResponseDTO<Void> onShelf(@PathVariable Long menuId) {
        log.info("[菜品管理] 上架菜品: menuId={}", menuId);
        mealMenuService.onShelf(menuId);
        return ResponseDTO.ok();
    }

    /**
     * 下架菜品
     *
     * @param menuId 菜品ID
     * @return 成功响应
     */
    @PutMapping("/{menuId}/off-shelf")
    @Operation(summary = "下架菜品")
    public ResponseDTO<Void> offShelf(@PathVariable Long menuId) {
        log.info("[菜品管理] 下架菜品: menuId={}", menuId);
        mealMenuService.offShelf(menuId);
        return ResponseDTO.ok();
    }
}
