package net.lab1024.sa.consume.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeOrderingCreateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeDishVO;
import net.lab1024.sa.consume.domain.vo.ConsumeOrderingOrderVO;

/**
 * 在线订餐服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
public interface ConsumeOrderingService {

    /**
     * 获取可用菜品列表
     *
     * @param userId 用户ID
     * @param orderDate 订餐日期
     * @param mealType 餐别类型
     * @return 菜品列表
     */
    List<ConsumeDishVO> getAvailableDishes(Long userId, LocalDate orderDate, String mealType);

    /**
     * 获取菜品详情
     *
     * @param dishId 菜品ID
     * @return 菜品详情
     */
    ConsumeDishVO getDishDetail(Long dishId);

    /**
     * 创建订餐订单
     *
     * @param form 订餐表单
     * @return 订单结果
     */
    Map<String, Object> createOrderingOrder(ConsumeOrderingCreateForm form);

    /**
     * 获取订餐记录（分页）
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param orderStatus 订单状态（可选）
     * @return 订餐记录
     */
    PageResult<ConsumeOrderingOrderVO> getOrderingOrders(Long userId, Integer pageNum, Integer pageSize, Integer orderStatus);

    /**
     * 取消订餐订单
     *
     * @param orderId 订单ID
     */
    void cancelOrderingOrder(Long orderId);

    /**
     * 核销订餐订单
     *
     * @param orderId 订单ID
     */
    void verifyOrderingOrder(Long orderId);

    /**
     * 获取订餐统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getOrderingStatistics(Long userId);

    /**
     * 获取可订餐时段
     *
     * @return 可订餐时段列表
     */
    List<Map<String, Object>> getAvailableOrderingTimes();

    /**
     * 获取菜品分类
     *
     * @return 菜品分类列表
     */
    List<Map<String, Object>> getDishCategories();
}
