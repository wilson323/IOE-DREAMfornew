package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.MealOrderCreateForm;
import net.lab1024.sa.consume.domain.form.MealOrderQueryForm;
import net.lab1024.sa.consume.domain.form.MealOrderVerifyForm;
import net.lab1024.sa.consume.domain.vo.MealOrderDetailVO;
import net.lab1024.sa.consume.domain.vo.MealOrderStatisticsVO;
import net.lab1024.sa.consume.domain.vo.MealOrderVO;

import java.util.List;

/**
 * 订餐服务接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
public interface MealOrderService {

    /**
     * 创建订餐订单
     *
     * @param form 创建表单
     * @return 订单ID
     */
    ResponseDTO<Long> createOrder(MealOrderCreateForm form);

    /**
     * 查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    ResponseDTO<MealOrderDetailVO> getOrderDetail(Long orderId);

    /**
     * 查询订单列表（分页）
     *
     * @param form 查询表单
     * @return 订单列表
     */
    ResponseDTO<PageResult<MealOrderVO>> queryOrders(MealOrderQueryForm form);

    /**
     * 查询用户订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    ResponseDTO<List<MealOrderVO>> getUserOrders(Long userId, String status);

    /**
     * 核销订单（取餐）
     *
     * @param form 核销表单
     * @return 是否成功
     */
    ResponseDTO<Boolean> verifyOrder(MealOrderVerifyForm form);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param reason 取消原因
     * @return 是否成功
     */
    ResponseDTO<Boolean> cancelOrder(Long orderId, String reason);

    /**
     * 获取订餐统计
     *
     * @param areaId 区域ID
     * @param mealTypeId 餐别ID
     * @param dateStr 日期字符串（yyyy-MM-dd）
     * @return 统计信息
     */
    ResponseDTO<MealOrderStatisticsVO> getStatistics(Long areaId, Long mealTypeId, String dateStr);
}
