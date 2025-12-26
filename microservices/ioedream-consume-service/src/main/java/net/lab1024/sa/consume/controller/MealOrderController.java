package net.lab1024.sa.consume.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.consume.MealOrderEntity;
import net.lab1024.sa.common.entity.consume.MealOrderItemEntity;
import net.lab1024.sa.consume.service.MealOrderService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

/**
 * 订餐订单控制器 - 订单管理REST API
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/meal/order")
@Tag(name = "订单管理")
public class MealOrderController {

    @Resource
    private MealOrderService mealOrderService;

    /**
     * 创建订单
     *
     * @param userId 用户ID
     * @param orderDate 订餐日期
     * @param mealType 餐别（1-早餐 2-午餐 3-晚餐）
     * @param menuIds 菜品ID列表
     * @param quantities 数量列表
     * @return 订单ID
     */
    @PostMapping
    @Operation(summary = "创建订单")
    public ResponseDTO<Long> createOrder(
            @RequestParam Long userId,
            @RequestParam LocalDate orderDate,
            @RequestParam Integer mealType,
            @RequestParam List<Long> menuIds,
            @RequestParam List<Integer> quantities) {

        log.info("[订单管理] 创建订单: userId={}, orderDate={}, mealType={}", userId, orderDate, mealType);

        Long orderId = mealOrderService.createOrder(userId, orderDate, mealType, menuIds, quantities);

        return ResponseDTO.ok(orderId);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param cancelReason 取消原因
     * @return 成功响应
     */
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单")
    public ResponseDTO<Void> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String cancelReason) {

        log.info("[订单管理] 取消订单: orderId={}, reason={}", orderId, cancelReason);
        mealOrderService.cancelOrder(orderId, cancelReason);
        return ResponseDTO.ok();
    }

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param paymentMethod 支付方式（balance-余额 wechat-微信 alipay-支付宝）
     * @return 成功响应
     */
    @PutMapping("/{orderId}/pay")
    @Operation(summary = "支付订单")
    public ResponseDTO<Void> payOrder(
            @PathVariable Long orderId,
            @RequestParam String paymentMethod) {

        log.info("[订单管理] 支付订单: orderId={}, paymentMethod={}", orderId, paymentMethod);
        mealOrderService.payOrder(orderId, paymentMethod);
        return ResponseDTO.ok();
    }

    /**
     * 查询订单列表（分页）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 订单分页数据
     */
    @GetMapping("/list")
    @Operation(summary = "查询订单列表")
    public ResponseDTO<PageResult<MealOrderEntity>> queryOrders(
            @RequestParam Long userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("[订单管理] 查询订单列表: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        Page<MealOrderEntity> page = mealOrderService.queryOrders(userId, startDate, endDate, pageNum, pageSize);

        return ResponseDTO.ok(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单实体
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "查询订单详情")
    public ResponseDTO<MealOrderEntity> getOrderDetail(@PathVariable Long orderId) {
        log.info("[订单管理] 查询订单详情: orderId={}", orderId);
        MealOrderEntity order = mealOrderService.getOrderDetail(orderId);
        return ResponseDTO.ok(order);
    }

    /**
     * 查询订单明细
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    @GetMapping("/{orderId}/items")
    @Operation(summary = "查询订单明细")
    public ResponseDTO<List<MealOrderItemEntity>> getOrderItems(@PathVariable Long orderId) {
        log.info("[订单管理] 查询订单明细: orderId={}", orderId);
        List<MealOrderItemEntity> items = mealOrderService.getOrderItems(orderId);
        return ResponseDTO.ok(items);
    }
}
