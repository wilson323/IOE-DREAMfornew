package net.lab1024.sa.consume.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeOrderingCreateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDishVO;
import net.lab1024.sa.consume.domain.vo.ConsumeOrderingOrderVO;
import net.lab1024.sa.consume.service.ConsumeOrderingService;

/**
 * 移动端在线订餐控制器
 * <p>
 * 提供移动端在线订餐功能，包括：
 * 1. 菜品浏览
 * 2. 订餐下单
 * 3. 订餐记录
 * 4. 订餐取消
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/v1/consume/mobile/ordering")
@Tag(name = "移动端在线订餐", description = "移动端订餐接口")
@Slf4j
public class ConsumeOrderingMobileController {

    @Resource
    private ConsumeOrderingService consumeOrderingService;

    @Operation(summary = "获取菜品列表", description = "获取当前可订菜品列表")
    @GetMapping("/dishes")
    public ResponseDTO<List<ConsumeDishVO>> getDishes(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDate orderDate,
            @RequestParam(required = false) String mealType) {
        log.info("[移动端订餐] 查询菜品列表: userId={}, orderDate={}, mealType={}",
            userId, orderDate, mealType);
        try {
            List<ConsumeDishVO> dishes = consumeOrderingService.getAvailableDishes(userId, orderDate, mealType);
            log.info("[移动端订餐] 查询菜品列表成功: count={}", dishes.size());
            return ResponseDTO.ok(dishes);
        } catch (Exception e) {
            log.error("[移动端订餐] 查询菜品列表异常: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取菜品详情", description = "获取菜品详细信息")
    @GetMapping("/dish/{dishId}")
    public ResponseDTO<ConsumeDishVO> getDishDetail(@PathVariable Long dishId) {
        log.info("[移动端订餐] 查询菜品详情: dishId={}", dishId);
        try {
            ConsumeDishVO dish = consumeOrderingService.getDishDetail(dishId);
            log.info("[移动端订餐] 查询菜品详情成功: dishId={}", dishId);
            return ResponseDTO.ok(dish);
        } catch (Exception e) {
            log.error("[移动端订餐] 查询菜品详情异常: dishId={}, error={}",
                dishId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "创建订餐订单", description = "用户创建订餐订单")
    @PostMapping("/create")
    public ResponseDTO<Map<String, Object>> createOrder(@Valid @RequestBody ConsumeOrderingCreateForm form) {
        log.info("[移动端订餐] 创建订餐订单: userId={}, dishIds={}",
            form.getUserId(), form.getDishIds());
        try {
            Map<String, Object> result = consumeOrderingService.createOrderingOrder(form);
            log.info("[移动端订餐] 创建订餐订单成功: userId={}, orderId={}",
                form.getUserId(), result.get("orderId"));
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端订餐] 创建订餐订单异常: userId={}, error={}",
                form.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取订餐记录", description = "分页获取用户订餐记录")
    @GetMapping("/orders/{userId}")
    public ResponseDTO<PageResult<ConsumeOrderingOrderVO>> getOrderingOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Integer orderStatus) {
        log.info("[移动端订餐] 查询订餐记录: userId={}, pageNum={}, pageSize={}, status={}",
            userId, pageNum, pageSize, orderStatus);
        try {
            PageResult<ConsumeOrderingOrderVO> orders = consumeOrderingService.getOrderingOrders(
                userId, pageNum, pageSize, orderStatus);
            log.info("[移动端订餐] 查询订餐记录成功: userId={}, total={}",
                userId, orders.getTotal());
            return ResponseDTO.ok(orders);
        } catch (Exception e) {
            log.error("[移动端订餐] 查询订餐记录异常: userId={}, error={}",
                userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "取消订餐", description = "用户取消订餐订单")
    @PostMapping("/cancel/{orderId}")
    public ResponseDTO<Void> cancelOrder(@PathVariable Long orderId) {
        log.info("[移动端订餐] 取消订餐: orderId={}", orderId);
        try {
            consumeOrderingService.cancelOrderingOrder(orderId);
            log.info("[移动端订餐] 取消订餐成功: orderId={}", orderId);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端订餐] 取消订餐异常: orderId={}, error={}",
                orderId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "核销订餐", description = "商户核销订餐订单")
    @PostMapping("/verify/{orderId}")
    public ResponseDTO<Void> verifyOrder(@PathVariable Long orderId) {
        log.info("[移动端订餐] 核销订餐: orderId={}", orderId);
        try {
            consumeOrderingService.verifyOrderingOrder(orderId);
            log.info("[移动端订餐] 核销订餐成功: orderId={}", orderId);
            return ResponseDTO.ok();
        } catch (Exception e) {
            log.error("[移动端订餐] 核销订餐异常: orderId={}, error={}",
                orderId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取订餐统计", description = "获取用户订餐统计信息")
    @GetMapping("/statistics/{userId}")
    public ResponseDTO<Map<String, Object>> getOrderingStatistics(@PathVariable Long userId) {
        log.info("[移动端订餐] 查询订餐统计: userId={}", userId);
        try {
            Map<String, Object> statistics = consumeOrderingService.getOrderingStatistics(userId);
            log.info("[移动端订餐] 查询订餐统计成功: userId={}", userId);
            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[移动端订餐] 查询订餐统计异常: userId={}, error={}",
                userId, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取可订餐时段", description = "获取当前可订餐的时间段")
    @GetMapping("/available-times")
    public ResponseDTO<List<Map<String, Object>>> getAvailableOrderingTimes() {
        log.info("[移动端订餐] 查询可订餐时段");
        try {
            List<Map<String, Object>> times = consumeOrderingService.getAvailableOrderingTimes();
            log.info("[移动端订餐] 查询可订餐时段成功: count={}", times.size());
            return ResponseDTO.ok(times);
        } catch (Exception e) {
            log.error("[移动端订餐] 查询可订餐时段异常: error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Operation(summary = "获取菜品分类", description = "获取菜品分类列表")
    @GetMapping("/categories")
    public ResponseDTO<List<Map<String, Object>>> getDishCategories() {
        log.info("[移动端订餐] 查询菜品分类");
        try {
            List<Map<String, Object>> categories = consumeOrderingService.getDishCategories();
            log.info("[移动端订餐] 查询菜品分类成功: count={}", categories.size());
            return ResponseDTO.ok(categories);
        } catch (Exception e) {
            log.error("[移动端订餐] 查询菜品分类异常: error={}", e.getMessage(), e);
            throw e;
        }
    }
}
