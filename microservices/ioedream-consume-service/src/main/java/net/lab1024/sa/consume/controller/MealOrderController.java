package net.lab1024.sa.consume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.MealOrderCreateForm;
import net.lab1024.sa.consume.domain.form.MealOrderQueryForm;
import net.lab1024.sa.consume.domain.form.MealOrderVerifyForm;
import net.lab1024.sa.consume.domain.vo.MealOrderDetailVO;
import net.lab1024.sa.consume.domain.vo.MealOrderStatisticsVO;
import net.lab1024.sa.consume.domain.vo.MealOrderVO;
import net.lab1024.sa.consume.service.MealOrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订餐管理控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Tag(name = "订餐管理")
@RestController
@RequestMapping("/api/v1/consume/meal-order")
public class MealOrderController {

    @Resource
    private MealOrderService mealOrderService;

    @Operation(summary = "创建订餐订单")
    @PostMapping("/create")
    public ResponseDTO<Long> createOrder(@Valid @RequestBody MealOrderCreateForm form) {
        return mealOrderService.createOrder(form);
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/detail/{orderId}")
    public ResponseDTO<MealOrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        return mealOrderService.getOrderDetail(orderId);
    }

    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public ResponseDTO<PageResult<MealOrderVO>> queryOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Long mealTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        // 构建查询表单（保持向后兼容）
        MealOrderQueryForm form = new MealOrderQueryForm();
        form.setUserId(userId);
        form.setAreaId(areaId);
        form.setMealTypeId(mealTypeId);
        form.setStatus(status);
        form.setOrderNo(orderNo);
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setPageNum(pageNum);
        form.setPageSize(pageSize);
        return mealOrderService.queryOrders(form);
    }

    @Operation(summary = "查询用户订单")
    @GetMapping("/user/{userId}")
    public ResponseDTO<List<MealOrderVO>> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(required = false) String status) {
        return mealOrderService.getUserOrders(userId, status);
    }

    @Operation(summary = "核销订单（取餐）")
    @PostMapping("/verify")
    public ResponseDTO<Boolean> verifyOrder(@Valid @RequestBody MealOrderVerifyForm form) {
        return mealOrderService.verifyOrder(form);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel/{orderId}")
    public ResponseDTO<Boolean> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        return mealOrderService.cancelOrder(orderId, reason);
    }

    @Operation(summary = "获取订餐统计")
    @GetMapping("/statistics")
    public ResponseDTO<MealOrderStatisticsVO> getStatistics(
            @RequestParam Long areaId,
            @RequestParam Long mealTypeId,
            @RequestParam String date) {
        return mealOrderService.getStatistics(areaId, mealTypeId, date);
    }
}
