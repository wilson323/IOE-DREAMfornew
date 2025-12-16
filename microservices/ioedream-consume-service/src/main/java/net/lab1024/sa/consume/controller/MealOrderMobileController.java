package net.lab1024.sa.consume.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.MealOrderCreateForm;
import net.lab1024.sa.consume.domain.form.MealOrderVerifyForm;
import net.lab1024.sa.consume.domain.vo.MealOrderDetailVO;
import net.lab1024.sa.consume.domain.vo.MealOrderVO;
import net.lab1024.sa.consume.service.MealOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订餐移动端控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Tag(name = "订餐管理-移动端")
@RestController
@RequestMapping("/api/mobile/v1/consume/meal-order")
public class MealOrderMobileController {

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

    @Operation(summary = "查询我的订单")
    @GetMapping("/my-orders/{userId}")
    public ResponseDTO<List<MealOrderVO>> getMyOrders(
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
}
